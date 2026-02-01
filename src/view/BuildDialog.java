package view;

import model.*;

import javax.swing.*;
import java.awt.*;

public class BuildDialog extends JDialog {

    public BuildDialog(JFrame parent, Planet planet, Game game, Runnable onClose) {
        super(parent, "Dodaj do kolejki", true);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(400, 500);
        setLocationRelativeTo(parent);

        ResearchManager researchManager = game.getResearchManager();
        boolean anyBuildings = false;
        boolean anyShips = false;

        // SEKCJA: BUDYNKI
        JLabel buildingsHeader = new JLabel("━━━ BUDYNKI ━━━");
        buildingsHeader.setFont(buildingsHeader.getFont().deriveFont(Font.BOLD, 12f));
        buildingsHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(5));
        add(buildingsHeader);
        add(Box.createVerticalStrut(5));

        for (BuildingType type : BuildingType.values()) {
            if (!planet.canBuild(type, researchManager)) continue;
            if (planet.isBuildingInQueue(type)) continue;
            if (planet.hasBuilding(type)) continue;

            anyBuildings = true;

            JPanel btnPanel = new JPanel(new BorderLayout());
            btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btnPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            JButton btn = new JButton(type.getDisplayName());
            btn.setHorizontalAlignment(SwingConstants.LEFT);

            btn.addActionListener(e -> {
                planet.addBuildingToQueue(type, researchManager);
                dispose();
                onClose.run();
            });

            JLabel costLabel = new JLabel(type.getCost() + " prod.");
            costLabel.setForeground(Color.GRAY);
            costLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            btnPanel.add(btn, BorderLayout.CENTER);
            btnPanel.add(costLabel, BorderLayout.EAST);

            add(btnPanel);
        }

        if (!anyBuildings) {
            JLabel noBuildings = new JLabel("Brak dostępnych budynków");
            noBuildings.setForeground(Color.GRAY);
            noBuildings.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(noBuildings);
        }

        // SEKCJA: STATKI
        add(Box.createVerticalStrut(15));
        JLabel shipsHeader = new JLabel("━━━ STATKI ━━━");
        shipsHeader.setFont(shipsHeader.getFont().deriveFont(Font.BOLD, 12f));
        shipsHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(shipsHeader);
        add(Box.createVerticalStrut(5));

        for (ShipType type : ShipType.values()) {
            if (!planet.canBuildShip(type, researchManager)) continue;

            anyShips = true;

            JPanel btnPanel = new JPanel(new BorderLayout());
            btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btnPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            String shipInfo = type.getDisplayName() + " (A:" +
                    type.getEffectiveAttack(researchManager) + " D:" +
                    type.getEffectiveDefense(researchManager) + ")";

            JButton btn = new JButton(shipInfo);
            btn.setHorizontalAlignment(SwingConstants.LEFT);

            btn.addActionListener(e -> {
                planet.addShipToQueue(type, researchManager);
                dispose();
                onClose.run();
            });

            JLabel costLabel = new JLabel(type.getCost() + " prod.");
            costLabel.setForeground(Color.GRAY);
            costLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            btnPanel.add(btn, BorderLayout.CENTER);
            btnPanel.add(costLabel, BorderLayout.EAST);

            add(btnPanel);
        }

        if (!anyShips) {
            JLabel noShips = new JLabel("Brak dostępnych statków");
            noShips.setForeground(Color.GRAY);
            noShips.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(noShips);
        }

        if (!anyBuildings && !anyShips) {
            add(Box.createVerticalStrut(10));
            JLabel hint = new JLabel("<html><center>Zbadaj nowe technologie,<br>aby odblokować więcej opcji</center></html>");
            hint.setForeground(Color.GRAY);
            hint.setFont(hint.getFont().deriveFont(11f));
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(hint);
        }

        add(Box.createVerticalGlue());
    }
}