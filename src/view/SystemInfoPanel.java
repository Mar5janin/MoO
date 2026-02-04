package view;

import controller.Game;
import model.*;

import javax.swing.*;
import java.awt.*;

public class SystemInfoPanel extends JPanel {

    public SystemInfoPanel(StarSystem system, MainWindow mainWindow, Game game) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (!game.getFogOfWar().isSystemVisible(system)) {
            add(title("SYSTEM: " + system.getName()));
            add(Box.createVerticalStrut(10));

            JLabel unknown = new JLabel("Brak danych rozpoznawczych");
            unknown.setForeground(Color.GRAY);
            add(unknown);

            JLabel hint = new JLabel("<html>Wyślij zwiadowcę<br>aby rozpoznać system</html>");
            hint.setForeground(Color.LIGHT_GRAY);
            hint.setFont(hint.getFont().deriveFont(10f));
            add(Box.createVerticalStrut(10));
            add(hint);

            return;
        }

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
            SpaceInstallation station = system.getBattleStation();
            boolean isPlayerStation = station.getOwner() == null;

            String stationOwner = isPlayerStation ? "Twój Posterunek Bojowy" : "Posterunek Bojowy przeciwnika";
            JLabel stationLabel = new JLabel(stationOwner);
            stationLabel.setFont(stationLabel.getFont().deriveFont(Font.BOLD, 12f));
            stationLabel.setForeground(isPlayerStation ? new Color(100, 200, 100) : new Color(255, 100, 100));
            add(stationLabel);

            JLabel hpLabel = new JLabel("HP: " + station.getCurrentHP() + "/" + station.getMaxHP());
            add(hpLabel);
            add(Box.createVerticalStrut(10));
        } else {
            Fleet fleet = system.getPlayerFleet();
            if (fleet != null && system.canBuildBattleStation(fleet, game.getResearchManager())) {
                JButton buildStationBtn = new JButton("Zbuduj Posterunek Bojowy (100 prod.)");
                buildStationBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                buildStationBtn.addActionListener(e -> {
                    fleet.startProject(SpaceInstallationType.BATTLE_STATION, null);
                    mainWindow.onSystemSelected(system);
                });
                add(buildStationBtn);
                add(Box.createVerticalStrut(10));
            }
        }

        add(Box.createVerticalStrut(5));
        add(new FleetInfoPanel(system, game, mainWindow));
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
            String planetOwner = "";
            if (planet.isColonized()) {
                planetOwner = planet.getOwner() == null ? " [TWOJA KOLONIA]" : " [KOLONIA PRZECIWNIKA]";
            }

            JButton button = new JButton(
                    "Orbita " + index + ": Planeta (" +
                            planet.getPlanetType().getDisplayName() + ")" + planetOwner
            );

            if (planet.isColonized() && planet.getOwner() != null) {
                button.setForeground(new Color(255, 100, 100));
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
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            JLabel label = new JLabel("Orbita " + index + ": Pole asteroid");
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (asteroid.hasInstallation()) {
                label.setText(label.getText() + " [" + asteroid.getInstallation().getType().getDisplayName() + "]");
                panel.add(label);
            } else {
                panel.add(label);

                Fleet fleet = system.getPlayerFleet();
                if (fleet != null) {
                    InstallationOrder project = fleet.getCurrentProject();

                    if (project != null && project.getTarget() == asteroid) {
                        JLabel buildingLabel = new JLabel("Budowa: " + project.getDisplayName() +
                                " (" + project.getRemainingCost() + "/" + project.getOriginalCost() + ")");
                        buildingLabel.setForeground(new Color(255, 200, 100));
                        buildingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panel.add(buildingLabel);

                        JButton cancelBtn = new JButton("Anuluj budowę");
                        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        cancelBtn.setFocusPainted(false);
                        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                        cancelBtn.addActionListener(e -> {
                            fleet.cancelProject();
                            mainWindow.onSystemSelected(system);
                        });
                        panel.add(cancelBtn);
                    } else if (asteroid.canBuildInstallation(fleet) && project == null) {
                        JButton buildBtn = new JButton("Buduj instalację (50 prod.)");
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

                                fleet.startProject(type, asteroid);
                                mainWindow.onSystemSelected(system);
                            }
                        });
                        panel.add(buildBtn);
                    }
                }
            }

            return panel;
        }

        if (orbit.getObject() instanceof GasGiant giant) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            JLabel label = new JLabel("Orbita " + index + ": Gazowy gigant");
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            if (giant.hasInstallation()) {
                label.setText(label.getText() + " [" + giant.getInstallation().getType().getDisplayName() + "]");
                panel.add(label);
            } else {
                panel.add(label);

                Fleet fleet = system.getPlayerFleet();
                if (fleet != null) {
                    InstallationOrder project = fleet.getCurrentProject();

                    if (project != null && project.getTarget() == giant) {
                        JLabel buildingLabel = new JLabel("Budowa: " + project.getDisplayName() +
                                " (" + project.getRemainingCost() + "/" + project.getOriginalCost() + ")");
                        buildingLabel.setForeground(new Color(255, 200, 100));
                        buildingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panel.add(buildingLabel);

                        JButton cancelBtn = new JButton("Anuluj budowę");
                        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        cancelBtn.setFocusPainted(false);
                        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                        cancelBtn.addActionListener(e -> {
                            fleet.cancelProject();
                            mainWindow.onSystemSelected(system);
                        });
                        panel.add(cancelBtn);
                    } else if (giant.canBuildInstallation(fleet) && project == null) {
                        JButton buildBtn = new JButton("Zbuduj Kopalnię Gazową (60 prod.)");
                        buildBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                        buildBtn.setFocusPainted(false);
                        buildBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                        buildBtn.addActionListener(e -> {
                            fleet.startProject(SpaceInstallationType.GAS_MINE, giant);
                            mainWindow.onSystemSelected(system);
                        });
                        panel.add(buildBtn);
                    }
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