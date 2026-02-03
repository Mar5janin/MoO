package view;

import controller.Game;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FleetInfoPanel extends JPanel {

    private final StarSystem system;
    private final Game game;
    private final MainWindow mainWindow;

    public FleetInfoPanel(StarSystem system, Game game, MainWindow mainWindow) {
        this.system = system;
        this.game = game;
        this.mainWindow = mainWindow;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(title("FLOTY W SYSTEMIE"));
        add(Box.createVerticalStrut(10));

        if (system.getFleets().isEmpty()) {
            add(new JLabel("Brak flot w systemie"));
        } else {
            for (Fleet fleet : system.getFleets()) {
                JPanel fleetPanel = createFleetPanel(fleet);
                fleetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                fleetPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
                add(fleetPanel);
                add(Box.createVerticalStrut(10));
            }
        }
    }

    private JPanel createFleetPanel(Fleet fleet) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        boolean isPlayerFleet = fleet.getOwner() == null;
        Color borderColor = isPlayerFleet ? new Color(100, 150, 255) : new Color(255, 100, 100);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        ResearchManager rm = isPlayerFleet ? game.getResearchManager() : game.getEnemyResearchManager();

        String fleetOwner = isPlayerFleet ? "Flota gracza" : "Flota przeciwnika";
        JLabel fleetLabel = new JLabel(fleetOwner + " (" + fleet.getShipCount() + " statków)");
        fleetLabel.setFont(fleetLabel.getFont().deriveFont(Font.BOLD, 12f));
        if (!isPlayerFleet) {
            fleetLabel.setForeground(new Color(255, 100, 100));
        }
        fleetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(fleetLabel);
        panel.add(Box.createVerticalStrut(5));

        int totalAttack = fleet.getTotalAttack(rm);
        int totalDefense = fleet.getTotalDefense(rm);

        JLabel attackLabel = new JLabel("Całkowity atak: " + totalAttack);
        attackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(attackLabel);

        JLabel defenseLabel = new JLabel("Całkowita obrona: " + totalDefense);
        defenseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(defenseLabel);

        panel.add(Box.createVerticalStrut(5));

        JLabel shipsLabel = new JLabel("Skład floty:");
        shipsLabel.setFont(shipsLabel.getFont().deriveFont(Font.BOLD, 11f));
        shipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(shipsLabel);

        for (ShipType type : ShipType.values()) {
            int count = fleet.countShipType(type);
            if (count > 0) {
                String shipInfo = "  • " + type.getDisplayName() + " x" + count +
                        " (A:" + type.getEffectiveAttack(rm) +
                        " D:" + type.getEffectiveDefense(rm) + ")";
                JLabel shipLabel = new JLabel(shipInfo);
                shipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(shipLabel);
            }
        }

        if (fleet.isMoving()) {
            panel.add(Box.createVerticalStrut(5));

            StarSystem dest = fleet.getDestination();
            StarSystem next = fleet.getNextSystem();

            JLabel moving = new JLabel("→ W drodze do: " + dest.getName());
            moving.setForeground(new Color(100, 150, 255));
            moving.setFont(moving.getFont().deriveFont(Font.BOLD));
            moving.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(moving);

            JLabel nextLabel = new JLabel("   Następny: " + next.getName() +
                    " (" + fleet.getTurnsToDestination() + " tur)");
            nextLabel.setForeground(new Color(150, 180, 255));
            nextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(nextLabel);

            List<StarSystem> route = fleet.getRoute();
            if (route != null && route.size() > 1) {
                StringBuilder routeStr = new StringBuilder("   Trasa: ");
                for (int i = 0; i < route.size(); i++) {
                    if (i > 0) routeStr.append(" → ");
                    routeStr.append(route.get(i).getName());
                }
                JLabel routeLabel = new JLabel(routeStr.toString());
                routeLabel.setForeground(Color.LIGHT_GRAY);
                routeLabel.setFont(routeLabel.getFont().deriveFont(10f));
                routeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(routeLabel);
            }
        }

        if (isPlayerFleet) {
            panel.add(Box.createVerticalStrut(8));

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
            buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton manageBtn = new JButton("Zarządzaj flotą");
            manageBtn.setFocusPainted(false);
            manageBtn.addActionListener(e -> {
                new FleetManagementDialog(mainWindow, fleet, system, game, () -> {
                    mainWindow.onSystemSelected(system);
                }).setVisible(true);
            });
            buttonsPanel.add(manageBtn);

            buttonsPanel.add(Box.createHorizontalStrut(5));

            boolean hasEnemyFleet = system.getFleets().stream().anyMatch(f -> f.getOwner() != null);

            JButton attackBtn = new JButton("⚔️ Atak!");
            attackBtn.setFocusPainted(false);

            if (hasEnemyFleet) {
                attackBtn.setBackground(new Color(255, 100, 100));
                attackBtn.setEnabled(true);
                attackBtn.addActionListener(ev -> {
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "Czy na pewno chcesz zaatakować wrogą flotę w tym systemie?\nWalka rozpocznie się po zakończeniu tury.",
                            "Potwierdzenie ataku",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (result == JOptionPane.YES_OPTION) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Atak zostanie przeprowadzony po zakończeniu tury.\nWalka rozpocznie się automatycznie gdy obie floty będą w tym samym systemie.",
                                "Atak zaplanowany",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                });
            } else {
                attackBtn.setEnabled(false);
                attackBtn.setToolTipText("Brak wrogich flot w systemie");
            }
            buttonsPanel.add(attackBtn);

            panel.add(buttonsPanel);
        }

        return panel;
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }
}