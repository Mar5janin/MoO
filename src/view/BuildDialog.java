package view;

import model.*;

import javax.swing.*;
import java.awt.*;

public class BuildDialog extends JDialog {

    public BuildDialog(JFrame parent, Planet planet, Runnable onClose) {
        super(parent, "Dodaj do kolejki", true);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setSize(300, 300);
        setLocationRelativeTo(parent);

        boolean any = false;

        for (BuildingType type : BuildingType.values()) {

            if (!planet.canBuild(type)) continue;
            if (planet.isInQueue(type)) continue;
            if (planet.hasBuilding(type)) continue;

            any = true;

            JButton btn = new JButton(type.getDisplayName());
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);

            btn.addActionListener(e -> {
                planet.addToQueue(type);
                dispose();
                onClose.run();
            });

            add(btn);
        }

        if (!any) {
            add(new JLabel("Brak dostępnych budynków"));
        }
    }
}
