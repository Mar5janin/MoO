package view;

import model.*;

import javax.swing.*;
import java.awt.*;

public class SystemInfoPanel extends JPanel {

    public SystemInfoPanel(StarSystem system, MainWindow mainWindow, Game game) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(title("SYSTEM: " + system.getName()));
        add(Box.createVerticalStrut(10));

        // SEKCJA: ORBITY
        JLabel orbitsLabel = new JLabel("Orbity:");
        orbitsLabel.setFont(orbitsLabel.getFont().deriveFont(Font.BOLD, 12f));
        add(orbitsLabel);
        add(Box.createVerticalStrut(5));

        int orbitIndex = 1;
        for (OrbitSlot orbit : system.getOrbits()) {
            add(createOrbitComponent(orbitIndex++, orbit, system, mainWindow, game));
        }

        // SEKCJA: FLOTY
        add(Box.createVerticalStrut(15));
        add(new FleetInfoPanel(system, game));
    }

    private JComponent createOrbitComponent(
            int index,
            OrbitSlot orbit,
            StarSystem system,
            MainWindow mainWindow,
            Game game
    ) {

        if (orbit.getObject() == null) {
            return new JLabel("Orbita " + index + ": Pusta");
        }

        if (orbit.getObject() instanceof Planet planet) {
            JButton button = new JButton(
                    "Orbita " + index + ": Planeta (" +
                            planet.getPlanetType().getDisplayName() + ")"
            );

            if (planet.isColonized()) {
                button.setText(button.getText() + " [SKOLONIZOWANA]");
            }

            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);

            button.addActionListener(e ->
                    mainWindow.showPlanet(planet, system)
            );

            return button;
        }

        if (orbit.getObject() instanceof AsteroidField) {
            return new JLabel("Orbita " + index + ": Pole asteroid");
        }

        if (orbit.getObject() instanceof GasGiant) {
            return new JLabel("Orbita " + index + ": Gazowy gigant");
        }

        return new JLabel("Orbita " + index + ": Nieznane");
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }
}