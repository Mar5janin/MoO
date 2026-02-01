package view;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AllFleetsPanel extends JDialog {

    private final Game game;
    private final MainWindow mainWindow;
    private JPanel fleetsListPanel;

    public AllFleetsPanel(MainWindow parent, Game game) {
        super(parent, "Wszystkie floty", true);
        this.game = game;
        this.mainWindow = parent;

        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(parent);

        buildUI();
    }

    private void buildUI() {
        // Górny panel z informacjami
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topPanel.setBackground(new Color(40, 40, 40));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int totalFleets = 0;
        int totalShips = 0;

        for (StarSystem system : game.getGalaxy().getSystems()) {
            totalFleets += system.getFleets().size();
            for (Fleet fleet : system.getFleets()) {
                totalShips += fleet.getShipCount();
            }
        }

        JLabel statsLabel = new JLabel("Floty: " + totalFleets + " | Statki: " + totalShips);
        statsLabel.setForeground(Color.WHITE);
        statsLabel.setFont(statsLabel.getFont().deriveFont(Font.BOLD, 14f));
        topPanel.add(statsLabel);

        add(topPanel, BorderLayout.NORTH);

        // Panel z listą flot
        fleetsListPanel = new JPanel();
        fleetsListPanel.setLayout(new BoxLayout(fleetsListPanel, BoxLayout.Y_AXIS));
        fleetsListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(fleetsListPanel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // Dolny panel z przyciskiem zamknięcia
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Zamknij");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshFleetsList();
    }

    private void refreshFleetsList() {
        fleetsListPanel.removeAll();

        List<FleetLocation> allFleets = new ArrayList<>();

        // Zbierz wszystkie floty
        for (StarSystem system : game.getGalaxy().getSystems()) {
            for (Fleet fleet : system.getFleets()) {
                allFleets.add(new FleetLocation(fleet, system));
            }
        }

        if (allFleets.isEmpty()) {
            JLabel noFleets = new JLabel("Brak flot");
            noFleets.setAlignmentX(Component.CENTER_ALIGNMENT);
            fleetsListPanel.add(noFleets);
        } else {
            for (FleetLocation fl : allFleets) {
                JPanel fleetPanel = createFleetPanel(fl.fleet, fl.location);
                fleetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                fleetsListPanel.add(fleetPanel);
                fleetsListPanel.add(Box.createVerticalStrut(10));
            }
        }

        fleetsListPanel.revalidate();
        fleetsListPanel.repaint();
    }

    private JPanel createFleetPanel(Fleet fleet, StarSystem location) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Lewa część - informacje
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel locationLabel = new JLabel("📍 " + location.getName());
        locationLabel.setFont(locationLabel.getFont().deriveFont(Font.BOLD, 13f));

        ResearchManager rm = game.getResearchManager();
        int totalAttack = fleet.getTotalAttack(rm);
        int totalDefense = fleet.getTotalDefense(rm);

        JLabel statsLabel = new JLabel(
                fleet.getShipCount() + " statków | Atak: " + totalAttack + " | Obrona: " + totalDefense
        );
        statsLabel.setFont(statsLabel.getFont().deriveFont(11f));

        // Skład floty
        StringBuilder composition = new StringBuilder("Skład: ");
        boolean first = true;
        for (ShipType type : ShipType.values()) {
            int count = fleet.countShipType(type);
            if (count > 0) {
                if (!first) composition.append(", ");
                composition.append(count).append("× ").append(type.getDisplayName());
                first = false;
            }
        }
        JLabel compositionLabel = new JLabel(composition.toString());
        compositionLabel.setFont(compositionLabel.getFont().deriveFont(10f));
        compositionLabel.setForeground(Color.GRAY);

        infoPanel.add(locationLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(statsLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(compositionLabel);

        // Status ruchu z trasą
        if (fleet.isMoving()) {
            StarSystem dest = fleet.getDestination();
            StarSystem next = fleet.getNextSystem();

            JLabel movingLabel = new JLabel(
                    "→ Cel: " + dest.getName() + " | Następny: " + next.getName() +
                            " (" + fleet.getTurnsToDestination() + " tur)"
            );
            movingLabel.setForeground(new Color(100, 200, 100));
            movingLabel.setFont(movingLabel.getFont().deriveFont(Font.BOLD, 11f));
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(movingLabel);

            // Trasa
            List<StarSystem> route = fleet.getRoute();
            if (route != null && route.size() > 0) {
                StringBuilder routeStr = new StringBuilder("Trasa: " + location.getName());
                for (StarSystem sys : route) {
                    routeStr.append(" → ").append(sys.getName());
                }
                JLabel routeLabel = new JLabel(routeStr.toString());
                routeLabel.setFont(routeLabel.getFont().deriveFont(9f));
                routeLabel.setForeground(Color.LIGHT_GRAY);
                infoPanel.add(routeLabel);
            }
        }

        panel.add(infoPanel, BorderLayout.CENTER);

        // Prawa część - przyciski
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        JButton goToButton = new JButton("Przejdź");
        goToButton.setFocusPainted(false);
        goToButton.addActionListener(e -> {
            dispose();
            mainWindow.onSystemSelected(location);
        });

        JButton manageButton = new JButton("Zarządzaj");
        manageButton.setFocusPainted(false);
        manageButton.addActionListener(e -> {
            new FleetManagementDialog(mainWindow, fleet, location, game, this::refreshFleetsList)
                    .setVisible(true);
        });

        buttonsPanel.add(goToButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(manageButton);

        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    // Klasa pomocnicza do trzymania floty i jej lokacji
    private static class FleetLocation {
        Fleet fleet;
        StarSystem location;

        FleetLocation(Fleet fleet, StarSystem location) {
            this.fleet = fleet;
            this.location = location;
        }
    }
}