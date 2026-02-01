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

        JLabel orbitsLabel = new JLabel("Orbity:");
        orbitsLabel.setFont(orbitsLabel.getFont().deriveFont(Font.BOLD, 12f));
        add(orbitsLabel);
        add(Box.createVerticalStrut(5));

        int orbitIndex = 1;
        for (OrbitSlot orbit : system.getOrbits()) {
            JComponent component = createOrbitComponent(orbitIndex++, orbit, system, mainWindow, game);
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(component);
            add(Box.createVerticalStrut(3));
        }

        add(Box.createVerticalStrut(15));

        if (system.hasBattleStation()) {
            JLabel stationLabel = new JLabel("⚔️ Posterunek Bojowy");
            stationLabel.setFont(stationLabel.getFont().deriveFont(Font.BOLD, 12f));
            stationLabel.setForeground(new Color(255, 100, 100));
            add(stationLabel);

            SpaceInstallation station = system.getBattleStation();
            JLabel hpLabel = new JLabel("HP: " + station.getCurrentHP() + "/" + station.getMaxHP());
            add(hpLabel);
            add(Box.createVerticalStrut(10));
        } else {
            Fleet fleet = system.getPlayerFleet();
            if (fleet != null && system.canBuildBattleStation(fleet, game.getResearchManager())) {
                JButton buildStationBtn = new JButton("Zbuduj Posterunek Bojowy (100 prod., użyje Fabryki Kosmicznej)");
                buildStationBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                buildStationBtn.addActionListener(e -> {
                    if (game.spendCredits(100 * Planet.CREDITS_PER_PRODUCTION)) {
                        Ship factory = fleet.getShips().stream()
                                .filter(s -> s.getType() == ShipType.SPACE_FACTORY)
                                .findFirst()
                                .orElse(null);
                        if (factory != null) {
                            fleet.removeShip(factory);
                            system.setBattleStation(new SpaceInstallation(SpaceInstallationType.BATTLE_STATION));
                            mainWindow.updateResourceDisplay();
                            mainWindow.onSystemSelected(system);
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Posterunek Bojowy zbudowany!",
                                    "Sukces",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Za mało kredytów!",
                                "Błąd",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
                add(buildStationBtn);
                add(Box.createVerticalStrut(10));
            }
        }

        add(Box.createVerticalStrut(5));
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
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            button.addActionListener(e ->
                    mainWindow.showPlanet(planet, system)
            );

            return button;
        }

        if (orbit.getObject() instanceof AsteroidField asteroid) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JLabel label = new JLabel("Orbita " + index + ": Pole asteroid");
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (asteroid.hasInstallation()) {
                label.setText(label.getText() + " [" + asteroid.getInstallation().getType().getDisplayName() + "]");
                panel.add(label);
            } else {
                panel.add(label);

                Fleet fleet = system.getPlayerFleet();
                if (fleet != null && asteroid.canBuildInstallation(fleet)) {
                    JButton buildBtn = new JButton("Buduj instalację (50 prod., użyje Fabryki Kosmicznej)");
                    buildBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                    buildBtn.setFocusPainted(false);
                    buildBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                    buildBtn.addActionListener(e -> {
                        String[] options = {"Laboratorium (+5 badań)", "Kopalnia (+5 kredytów)"};
                        int choice = JOptionPane.showOptionDialog(
                                this,
                                "Wybierz typ instalacji:",
                                "Budowa na asteroidzie",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[0]
                        );

                        if (choice >= 0) {
                            SpaceInstallationType type = choice == 0 ?
                                    SpaceInstallationType.ASTEROID_LABORATORY :
                                    SpaceInstallationType.ASTEROID_MINE;

                            int cost = type.getCost() * Planet.CREDITS_PER_PRODUCTION;

                            if (game.spendCredits(cost)) {
                                Ship factory = fleet.getShips().stream()
                                        .filter(s -> s.getType() == ShipType.SPACE_FACTORY)
                                        .findFirst()
                                        .orElse(null);
                                if (factory != null) {
                                    fleet.removeShip(factory);
                                    asteroid.setInstallation(new SpaceInstallation(type));
                                    mainWindow.updateResourceDisplay();
                                    mainWindow.onSystemSelected(system);
                                    JOptionPane.showMessageDialog(
                                            this,
                                            "Instalacja zbudowana!",
                                            "Sukces",
                                            JOptionPane.INFORMATION_MESSAGE
                                    );
                                }
                            } else {
                                JOptionPane.showMessageDialog(
                                        this,
                                        "Za mało kredytów!",
                                        "Błąd",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            }
                        }
                    });
                    panel.add(buildBtn);
                }
            }

            return panel;
        }

        if (orbit.getObject() instanceof GasGiant giant) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JLabel label = new JLabel("Orbita " + index + ": Gazowy gigant");
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (giant.hasInstallation()) {
                label.setText(label.getText() + " [" + giant.getInstallation().getType().getDisplayName() + "]");
                panel.add(label);
            } else {
                panel.add(label);

                Fleet fleet = system.getPlayerFleet();
                if (fleet != null && giant.canBuildInstallation(fleet)) {
                    JButton buildBtn = new JButton("Zbuduj Kopalnię Gazową (60 prod., użyje Fabryki Kosmicznej)");
                    buildBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                    buildBtn.setFocusPainted(false);
                    buildBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                    buildBtn.addActionListener(e -> {
                        int cost = SpaceInstallationType.GAS_MINE.getCost() * Planet.CREDITS_PER_PRODUCTION;

                        if (game.spendCredits(cost)) {
                            Ship factory = fleet.getShips().stream()
                                    .filter(s -> s.getType() == ShipType.SPACE_FACTORY)
                                    .findFirst()
                                    .orElse(null);
                            if (factory != null) {
                                fleet.removeShip(factory);
                                giant.setInstallation(new SpaceInstallation(SpaceInstallationType.GAS_MINE));
                                mainWindow.updateResourceDisplay();
                                mainWindow.onSystemSelected(system);
                                JOptionPane.showMessageDialog(
                                        this,
                                        "Kopalnia Gazowa zbudowana!",
                                        "Sukces",
                                        JOptionPane.INFORMATION_MESSAGE
                                );
                            }
                        } else {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Za mało kredytów!",
                                    "Błąd",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                    panel.add(buildBtn);
                }
            }

            return panel;
        }

        return new JLabel("Orbita " + index + ": Nieznane");
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }
}