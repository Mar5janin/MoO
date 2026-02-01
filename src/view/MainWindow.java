package view;

import model.Game;
import model.OrbitSlot;
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

        initTopPanel();
    }

    private void initTopPanel() {
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.setBackground(new Color(30, 30, 30));
        topPanel.setPreferredSize(new Dimension(0, 50));

        turnLabel = new JLabel("Tura: 1");
        turnLabel.setForeground(Color.WHITE);
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(turnLabel);

        topPanel.add(createSeparator());

        creditsLabel = new JLabel("💰 Kredyty: 500");
        creditsLabel.setForeground(new Color(255, 215, 0));
        creditsLabel.setFont(creditsLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(creditsLabel);

        researchLabel = new JLabel("🔬 Badania: 0");
        researchLabel.setForeground(new Color(100, 200, 255));
        researchLabel.setFont(researchLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(researchLabel);

        topPanel.add(createSeparator());

        JButton researchButton = new JButton("Badania");
        researchButton.setFocusPainted(false);
        researchButton.addActionListener(e -> {
            if (game != null) {
                new ResearchPanel(this, game).setVisible(true);
            }
        });
        topPanel.add(researchButton);

        JButton fleetsButton = new JButton("Floty");
        fleetsButton.setFocusPainted(false);
        fleetsButton.addActionListener(e -> {
            if (game != null) {
                new AllFleetsPanel(this, game).setVisible(true);
            }
        });
        topPanel.add(fleetsButton);

        endTurnButton = new JButton("Zakończ turę");
        endTurnButton.setFocusPainted(false);
        endTurnButton.addActionListener(e -> {
            if (game == null) return;

            if (!game.canEndTurn()) {
                String reason = game.getEndTurnBlockReason();

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
                    if (reason.contains("nie ma kolejki budowy") || reason.contains("nieprzypisanych")) {
                        for (StarSystem system : game.getGalaxy().getSystems()) {
                            for (OrbitSlot orbit : system.getOrbits()) {
                                if (orbit.getObject() instanceof Planet planet) {
                                    if (planet.isColonized()) {
                                        if (planet.getBuildQueue().isEmpty() || !planet.isPopulationFullyAssigned()) {
                                            onSystemSelected(system);
                                            showPlanet(planet, system);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (reason.contains("projektu badawczego")) {
                        ResearchPanel researchPanel = new ResearchPanel(this, game);
                        researchPanel.setVisible(true);
                    }
                }

                return;
            }

            sidePanel.removeAll();
            sidePanel.setVisible(false);

            game.nextTurn();

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

        JPanel container = new JPanel(new BorderLayout());
        container.add(new PlanetInfoPanel(planet, system, this, game), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        sidePanel.add(scrollPane, BorderLayout.CENTER);
        sidePanel.revalidate();
        sidePanel.repaint();
    }

    public void setGame(Game game) {
        this.game = game;
        updateResourceDisplay();
    }

}