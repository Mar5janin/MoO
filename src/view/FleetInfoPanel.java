package view;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

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
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ResearchManager rm = game.getResearchManager();

        // Nagłówek floty
        JLabel fleetLabel = new JLabel("Flota gracza (" + fleet.getShipCount() + " statków)");
        fleetLabel.setFont(fleetLabel.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(fleetLabel);
        panel.add(Box.createVerticalStrut(5));

        // Statystyki floty
        int totalAttack = fleet.getTotalAttack(rm);
        int totalDefense = fleet.getTotalDefense(rm);

        panel.add(new JLabel("Całkowity atak: " + totalAttack));
        panel.add(new JLabel("Całkowita obrona: " + totalDefense));
        panel.add(Box.createVerticalStrut(5));

        // Grupuj statki według typu
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

        // Status ruchu
        if (fleet.isMoving()) {
            panel.add(Box.createVerticalStrut(5));
            JLabel moving = new JLabel("→ W drodze do: " + fleet.getDestination().getName() +
                    " (" + fleet.getTurnsToDestination() + " tur)");
            moving.setForeground(new Color(100, 150, 255));
            panel.add(moving);
        }

        return panel;
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }
}