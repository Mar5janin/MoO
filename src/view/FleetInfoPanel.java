package view;

import controller.Game;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FleetInfoPanel extends JPanel {

    public FleetInfoPanel(StarSystem system, Game game) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(title("FLOTY W SYSTEMIE"));
        add(Box.createVerticalStrut(10));

        if (system.getFleets().isEmpty()) {
            add(new JLabel("Brak flot w systemie"));
        } else {
            for (Fleet fleet : system.getFleets()) {
                add(createFleetPanel(fleet, game));
                add(Box.createVerticalStrut(5));
            }
        }
    }

    private JPanel createFleetPanel(Fleet fleet, Game game) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        boolean isPlayerFleet = fleet.getOwner() == null;
        Color borderColor = isPlayerFleet ? Color.GRAY : new Color(255, 100, 100);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ResearchManager rm = isPlayerFleet ? game.getResearchManager() : game.getEnemyResearchManager();

        String fleetOwner = isPlayerFleet ? "Flota gracza" : "Flota przeciwnika";
        JLabel fleetLabel = new JLabel(fleetOwner + " (" + fleet.getShipCount() + " statków)");
        fleetLabel.setFont(fleetLabel.getFont().deriveFont(Font.BOLD, 12f));
        if (!isPlayerFleet) {
            fleetLabel.setForeground(new Color(255, 100, 100));
        }
        panel.add(fleetLabel);
        panel.add(Box.createVerticalStrut(5));

        int totalAttack = fleet.getTotalAttack(rm);
        int totalDefense = fleet.getTotalDefense(rm);

        panel.add(new JLabel("Całkowity atak: " + totalAttack));
        panel.add(new JLabel("Całkowita obrona: " + totalDefense));
        panel.add(Box.createVerticalStrut(5));

        JLabel shipsLabel = new JLabel("Skład floty:");
        shipsLabel.setFont(shipsLabel.getFont().deriveFont(Font.BOLD, 11f));
        panel.add(shipsLabel);

        for (ShipType type : ShipType.values()) {
            int count = fleet.countShipType(type);
            if (count > 0) {
                String shipInfo = "  • " + type.getDisplayName() + " x" + count +
                        " (A:" + type.getEffectiveAttack(rm) +
                        " D:" + type.getEffectiveDefense(rm) + ")";
                panel.add(new JLabel(shipInfo));
            }
        }

        if (fleet.isMoving()) {
            panel.add(Box.createVerticalStrut(5));

            StarSystem dest = fleet.getDestination();
            StarSystem next = fleet.getNextSystem();

            JLabel moving = new JLabel("→ W drodze do: " + dest.getName());
            moving.setForeground(new Color(100, 150, 255));
            moving.setFont(moving.getFont().deriveFont(Font.BOLD));
            panel.add(moving);

            JLabel nextLabel = new JLabel("   Następny: " + next.getName() +
                    " (" + fleet.getTurnsToDestination() + " tur)");
            nextLabel.setForeground(new Color(150, 180, 255));
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
                panel.add(routeLabel);
            }
        }

        return panel;
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }
}