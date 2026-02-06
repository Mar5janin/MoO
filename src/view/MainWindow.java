package view;

import controller.Game;
import model.orbits.OrbitSlot;
import model.orbits.planets.Planet;
import model.galaxy.StarSystem;
import view.fleets.AllFleetsPanel;
import view.galaxy.GalaxyPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

        creditsLabel = new JLabel("Kredyty: 500");
        creditsLabel.setForeground(new Color(255, 215, 0));
        creditsLabel.setFont(creditsLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(creditsLabel);

        researchLabel = new JLabel("Badania: 0");
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

            if (game.isGameOver()) {
                showGameOverDialog();
                return;
            }

            if (!game.canEndTurn()) {
                handleCannotEndTurn();
                return;
            }

            sidePanel.removeAll();
            sidePanel.setVisible(false);

            game.nextTurn();

            showCombatReports();

            if (game.isGameOver()) {
                showGameOverDialog();
                return;
            }

            updateResourceDisplay();
            repaint();
        });
        topPanel.add(endTurnButton);
    }

    private void showGameOverDialog() {
        String message = game.hasPlayerWon() ?
                "WYGRANA!\n\nPokonałeś przeciwnika!\nGra zakończona w turze " + game.getTurn() :
                "PRZEGRANA!\n\nPrzeciwnik cię pokonał!\nGra zakończona w turze " + game.getTurn();

        int result = JOptionPane.showOptionDialog(
                this, message,
                game.hasPlayerWon() ? "Wygrana!" : "Przegrana!",
                JOptionPane.DEFAULT_OPTION,
                game.hasPlayerWon() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE,
                null, new String[]{"Nowa gra", "Wyjdź"}, "Nowa gra"
        );

        if (result == 0) {
            dispose();
            SwingUtilities.invokeLater(() -> new MainWindow());
        } else {
            System.exit(0);
        }
    }

    private void handleCannotEndTurn() {
        String reason = game.getEndTurnBlockReason();
        int result = JOptionPane.showOptionDialog(
                this, reason, "Nie można zakończyć tury",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                null, new String[]{"Przejdź", "Anuluj"}, "Przejdź"
        );

        if (result != JOptionPane.YES_OPTION) return;

        if (reason.contains("nie ma kolejki budowy") || reason.contains("nieprzypisanych")) {
            navigateToFirstProblemPlanet();
        } else if (reason.contains("projektu badawczego")) {
            new ResearchPanel(this, game).setVisible(true);
        }
    }

    private void navigateToFirstProblemPlanet() {
        for (StarSystem system : game.getGalaxy().getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet && planet.isColonized()) {
                    if (planet.getBuildQueue().isEmpty() || !planet.isPopulationFullyAssigned()) {
                        onSystemSelected(system);
                        showPlanet(planet, system);
                        return;
                    }
                }
            }
        }
    }

    private void showCombatReports() {
        List<String> combatReports = game.getCombatReports();
        if (combatReports.isEmpty()) return;

        StringBuilder allReports = new StringBuilder();
        for (String report : combatReports) {
            allReports.append(report).append("\n\n");
        }

        JTextArea textArea = new JTextArea(allReports.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Raporty z walk", JOptionPane.INFORMATION_MESSAGE);
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

            int netCredits = game.getNextTurnNetCredits();
            String creditsSign = netCredits >= 0 ? "+" : "";
            creditsLabel.setText("Kredyty: " + game.getTotalCredits() +
                    " (" + creditsSign + netCredits + ")");

            int nextResearch = game.getNextTurnResearch();
            researchLabel.setText("Badania: +" + nextResearch);
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