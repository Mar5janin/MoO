package view;

import model.*;

import javax.swing.*;
import java.awt.*;

public class BuildDialog extends JDialog {

    public BuildDialog(JFrame parent, Planet planet, Game game, Runnable onClose) {
        super(parent, "Dodaj do kolejki", true);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(350, 350);
        setLocationRelativeTo(parent);

        ResearchManager researchManager = game.getResearchManager();
        boolean any = false;

        for (BuildingType type : BuildingType.values()) {

            if (!planet.canBuild(type, researchManager)) continue;
            if (planet.isInQueue(type)) continue;
            if (planet.hasBuilding(type)) continue;

            any = true;

            JPanel btnPanel = new JPanel(new BorderLayout());
            btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btnPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            JButton btn = new JButton(type.getDisplayName());
            btn.setHorizontalAlignment(SwingConstants.LEFT);

            btn.addActionListener(e -> {
                planet.addToQueue(type, researchManager);
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

        if (!any) {
            add(Box.createVerticalStrut(10));
            JLabel noBuildings = new JLabel("Brak dostępnych budynków");
            noBuildings.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(noBuildings);
            add(Box.createVerticalStrut(5));

            JLabel hint = new JLabel("<html><center>Zbadaj nowe technologie,<br>aby odblokować budynki</center></html>");
            hint.setForeground(Color.GRAY);
            hint.setFont(hint.getFont().deriveFont(11f));
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(hint);
        }
    }
}