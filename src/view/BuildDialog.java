package view;

import controller.Game;
import model.*;

import javax.swing.*;
import java.awt.*;

public class BuildDialog extends JDialog {

    public BuildDialog(JFrame parent, Planet planet, Game game, Runnable onClose) {
        super(parent, "Dodaj do kolejki", true);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(500, 600);
        setLocationRelativeTo(parent);

        ResearchManager researchManager = game.getResearchManager();
        boolean anyBuildings = false;
        boolean anyShips = false;

        JLabel buildingsHeader = new JLabel(" BUDYNKI ");
        buildingsHeader.setFont(buildingsHeader.getFont().deriveFont(Font.BOLD, 12f));
        buildingsHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(5));
        add(buildingsHeader);
        add(Box.createVerticalStrut(5));

        for (BuildingType type : BuildingType.values()) {
            if (!planet.canBuild(type, researchManager)) continue;

            anyBuildings = true;

            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
            btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
            btnPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            String buttonText = type.getDisplayName();
            if (type.canBuildMultiple()) {
                int currentCount = planet.countBuilding(type);
                int inQueue = (int) planet.getBuildQueue().stream()
                        .filter(o -> o.getProductionType() == ProductionType.BUILDING)
                        .filter(o -> o.getBuildingType() == type)
                        .count();
                int total = currentCount + inQueue;
                buttonText += " (" + total + "/" + type.getMaxCount() + ")";
            }

            JButton btn = new JButton(buttonText);
            btn.setHorizontalAlignment(SwingConstants.LEFT);

            btn.addActionListener(e -> {
                planet.addBuildingToQueue(type, researchManager);
                dispose();
                onClose.run();
            });

            JLabel costLabel = new JLabel(type.getCost() + " prod.");
            costLabel.setForeground(Color.GRAY);
            costLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            topRow.add(btn, BorderLayout.CENTER);
            topRow.add(costLabel, BorderLayout.EAST);

            btnPanel.add(topRow);

            String effects = type.getEffectsDescription();
            if (!effects.isEmpty()) {
                JLabel effectsLabel = new JLabel("  " + effects);
                effectsLabel.setFont(effectsLabel.getFont().deriveFont(9f));
                effectsLabel.setForeground(new Color(100, 150, 255));
                effectsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                btnPanel.add(effectsLabel);
            }

            add(btnPanel);
        }

        if (!anyBuildings) {
            JLabel noBuildings = new JLabel("Brak dostępnych budynków");
            noBuildings.setForeground(Color.GRAY);
            noBuildings.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(noBuildings);
        }

        add(Box.createVerticalStrut(15));
        JLabel shipsHeader = new JLabel(" STATKI ");
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

            if (type == ShipType.COLONY_SHIP) {
                shipInfo += " Zabiera 1 pop.";
            }

            JButton btn = new JButton(shipInfo);
            btn.setHorizontalAlignment(SwingConstants.LEFT);

            btn.addActionListener(e -> {
                if (type == ShipType.COLONY_SHIP && planet.getTotalPopulation() <= 1) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Nie możesz zbudować statku kolonizacyjnego!\n" +
                                    "Potrzebujesz przynajmniej 2 populacji (statek zabiera 1 osobę).",
                            "Za mało populacji",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

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