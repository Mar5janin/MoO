package view;

import model.Game;
import model.Planet;
import model.StarSystem;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private JPanel rootPanel;
    private JPanel galaxyPanelContainer;
    private JPanel sidePanel;
    private JPanel topPanel;
    private JLabel turnLabel;
    private JButton endTurnButton;
    private Game game;

    // Labele dla zasobów
    private JLabel creditsLabel;
    private JLabel researchLabel;


    public MainWindow() {
        setTitle("Galactic Settlers");
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);

        initPanels();
        setVisible(true);
    }

    private void initPanels() {
        galaxyPanelContainer.setLayout(new BorderLayout());

        GalaxyPanel galaxyPanel = new GalaxyPanel(this);
        galaxyPanelContainer.add(galaxyPanel, BorderLayout.CENTER);

        sidePanel.setPreferredSize(new Dimension(320, 0));
        sidePanel.setVisible(false);

        // Inicjalizacja topPanel z zasobami
        initTopPanel();
    }

    private void initTopPanel() {
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.setBackground(new Color(30, 30, 30));
        topPanel.setPreferredSize(new Dimension(0, 50));

        // Tura
        turnLabel = new JLabel("Tura: 1");
        turnLabel.setForeground(Color.WHITE);
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(turnLabel);

        // Separator
        topPanel.add(createSeparator());

        // Kredyty
        creditsLabel = new JLabel("💰 Kredyty: 500");
        creditsLabel.setForeground(new Color(255, 215, 0)); // Złoty kolor
        creditsLabel.setFont(creditsLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(creditsLabel);

        // Badania
        researchLabel = new JLabel("🔬 Badania: 0");
        researchLabel.setForeground(new Color(100, 200, 255)); // Niebieski kolor
        researchLabel.setFont(researchLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(researchLabel);

        // Separator
        topPanel.add(createSeparator());

        // Przycisk badań
        JButton researchButton = new JButton("Badania");
        researchButton.setFocusPainted(false);
        researchButton.addActionListener(e -> {
            if (game != null) {
                new ResearchPanel(this, game).setVisible(true);
            }
        });
        topPanel.add(researchButton);

        // Przycisk flot
        JButton fleetsButton = new JButton("Floty");
        fleetsButton.setFocusPainted(false);
        fleetsButton.addActionListener(e -> {
            if (game != null) {
                new AllFleetsPanel(this, game).setVisible(true);
            }
        });
        topPanel.add(fleetsButton);

        // Przycisk zakończenia tury
        endTurnButton = new JButton("Zakończ turę");
        endTurnButton.setFocusPainted(false);
        endTurnButton.addActionListener(e -> {
            if (game == null) return;

            // Sprawdź czy można zakończyć turę
            if (!game.canEndTurn()) {
                String reason = game.getEndTurnBlockReason();

                // Pokaż dialog z możliwością przejścia do problemu
                int result = JOptionPane.showOptionDialog(
                        this,
                        reason,
                        "Nie można zakończyć tury",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new String[]{"Przejdź", "Anuluj"},
                        "Przejdź"
                );

                if (result == JOptionPane.YES_OPTION) {
                    // Sprawdź co jest problemem i otwórz odpowiedni panel
                    if (reason.contains("nie ma kolejki budowy")) {
                        // Znajdź planetę bez kolejki
                        for (StarSystem system : game.getGalaxy().getSystems()) {
                            for (model.OrbitSlot orbit : system.getOrbits()) {
                                if (orbit.getObject() instanceof Planet planet) {
                                    if (planet.isColonized() && planet.getBuildQueue().isEmpty()) {
                                        // Otwórz panel tej planety
                                        showPlanet(planet, system);
                                        return;
                                    }
                                }
                            }
                        }
                    } else if (reason.contains("projekt badawczy")) {
                        // Otwórz panel badań
                        new ResearchPanel(this, game).setVisible(true);
                    }
                }

                return;
            }

            // Zamknij panele
            sidePanel.removeAll();
            sidePanel.setVisible(false);

            // Przelicz turę
            game.nextTurn();

            // Zaktualizuj wyświetlanie
            updateResourceDisplay();

            repaint();
        });
        topPanel.add(endTurnButton);
    }

    private JPanel createSeparator() {
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(2, 30));
        separator.setBackground(new Color(100, 100, 100));
        return separator;
    }

    public void updateResourceDisplay() {
        if (game != null && turnLabel != null) {
            turnLabel.setText("Tura: " + game.getTurn());
            creditsLabel.setText("💰 Kredyty: " + game.getTotalCredits());
            researchLabel.setText("🔬 Badania: " + game.getTotalResearch());
        }
    }


    public void onSystemSelected(StarSystem system) {
        sidePanel.removeAll();

        if (system == null) {
            sidePanel.setVisible(false);
        } else {
            sidePanel.setVisible(true);
            sidePanel.setLayout(new BorderLayout());

            JScrollPane scrollPane = new JScrollPane(new SystemInfoPanel(system, this, game));
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);

            sidePanel.add(scrollPane, BorderLayout.CENTER);
        }

        sidePanel.revalidate();
        sidePanel.repaint();
    }

    public void showPlanet(Planet planet, StarSystem system) {
        sidePanel.removeAll();
        sidePanel.setVisible(true);
        sidePanel.setLayout(new BorderLayout());
        sidePanel.add(new PlanetInfoPanel(planet, system, this, game), BorderLayout.NORTH);
        sidePanel.revalidate();
        sidePanel.repaint();
    }

    public void setGame(Game game) {
        this.game = game;
        updateResourceDisplay();
    }

}