package view;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FleetManagementDialog extends JDialog {

    private final Fleet fleet;
    private final StarSystem location;
    private final Game game;
    private final Runnable onClose;

    public FleetManagementDialog(
            JFrame parent,
            Fleet fleet,
            StarSystem location,
            Game game,
            Runnable onClose
    ) {
        super(parent, "Zarządzanie flotą", true);
        this.fleet = fleet;
        this.location = location;
        this.game = game;
        this.onClose = onClose;

        setLayout(new BorderLayout());
        setSize(500, 500);
        setLocationRelativeTo(parent);

        buildUI();
    }

    private void buildUI() {
        // Górny panel - info o flocie
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(40, 40, 40));

        JLabel titleLabel = new JLabel("Flota w: " + location.getName());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));

        ResearchManager rm = game.getResearchManager();
        JLabel statsLabel = new JLabel(
                "Statków: " + fleet.getShipCount() +
                        " | Atak: " + fleet.getTotalAttack(rm) +
                        " | Obrona: " + fleet.getTotalDefense(rm)
        );
        statsLabel.setForeground(Color.LIGHT_GRAY);

        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(statsLabel);

        add(topPanel, BorderLayout.NORTH);

        // Środkowy panel - opcje
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Skład floty
        JLabel compositionLabel = new JLabel("Skład floty:");
        compositionLabel.setFont(compositionLabel.getFont().deriveFont(Font.BOLD, 12f));
        centerPanel.add(compositionLabel);
        centerPanel.add(Box.createVerticalStrut(10));

        for (ShipType type : ShipType.values()) {
            int count = fleet.countShipType(type);
            if (count > 0) {
                JLabel shipLabel = new JLabel("  • " + count + "× " + type.getDisplayName());
                centerPanel.add(shipLabel);
            }
        }

        centerPanel.add(Box.createVerticalStrut(20));

        // Akcje
        JLabel actionsLabel = new JLabel("Akcje:");
        actionsLabel.setFont(actionsLabel.getFont().deriveFont(Font.BOLD, 12f));
        centerPanel.add(actionsLabel);
        centerPanel.add(Box.createVerticalStrut(10));

        // Przycisk ruchu
        if (!fleet.isMoving()) {
            JButton moveButton = new JButton("🚀 Przenieś flotę do wybranego systemu");
            moveButton.setFocusPainted(false);
            moveButton.addActionListener(e -> showMoveDialog());
            centerPanel.add(moveButton);
            centerPanel.add(Box.createVerticalStrut(5));
        } else {
            StarSystem dest = fleet.getDestination();
            StarSystem next = fleet.getNextSystem();

            String routeInfo = "→ W drodze do: " + dest.getName() +
                    " (następny: " + next.getName() +
                    ", " + fleet.getTurnsToDestination() + " tur)";

            JLabel movingLabel = new JLabel(routeInfo);
            movingLabel.setForeground(new Color(100, 200, 100));
            centerPanel.add(movingLabel);
            centerPanel.add(Box.createVerticalStrut(5));

            // Przycisk anulowania podróży
            JButton cancelButton = new JButton("❌ Anuluj podróż");
            cancelButton.setFocusPainted(false);
            cancelButton.addActionListener(e -> {
                fleet.setDestination(null);
                dispose();
                onClose.run();
            });
            centerPanel.add(cancelButton);
            centerPanel.add(Box.createVerticalStrut(5));
        }

        // Przycisk oddzielenia statków
        if (fleet.getShipCount() > 1 && !fleet.isMoving()) {
            JButton splitButton = new JButton("✂️ Oddziel statki (utwórz nową flotę)");
            splitButton.setFocusPainted(false);
            splitButton.addActionListener(e -> showSplitDialog());
            centerPanel.add(splitButton);
        }

        add(centerPanel, BorderLayout.CENTER);

        // Dolny panel - zamknij
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Zamknij");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void showMoveDialog() {
        // Pobierz wszystkie systemy z galaktyki
        List<StarSystem> allSystems = game.getGalaxy().getSystems();

        // Usuń obecny system z listy
        List<StarSystem> availableSystems = allSystems.stream()
                .filter(s -> s != location)
                .sorted(Comparator.comparingDouble(location::distanceTo))
                .toList();

        if (availableSystems.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Brak dostępnych systemów!",
                    "Nie można przenieść",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Stwórz opcje z informacją o dystansie
        String[] options = availableSystems.stream()
                .map(s -> {
                    List<StarSystem> path = Pathfinder.findPath(location, s);
                    int distance = path != null ? path.size() - 1 : -1;

                    if (distance == -1) {
                        return s.getName() + " (NIEOSIĄGALNY)";
                    } else if (distance == 1) {
                        return s.getName() + " (1 tura)";
                    } else {
                        return s.getName() + " (" + distance + " tury)";
                    }
                })
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Wybierz system docelowy:",
                "Przenieś flotę",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (selected != null) {
            // Wyciągnij nazwę systemu (przed nawiasem)
            String systemName = selected.split(" \\(")[0];

            StarSystem destination = availableSystems.stream()
                    .filter(s -> s.getName().equals(systemName))
                    .findFirst()
                    .orElse(null);

            if (destination != null) {
                boolean success = fleet.setDestination(destination);

                if (success) {
                    List<StarSystem> path = fleet.getRoute();
                    int turns = fleet.getTurnsToDestination();

                    StringBuilder routeText = new StringBuilder("Trasa: " + location.getName());
                    for (StarSystem sys : path) {
                        routeText.append(" → ").append(sys.getName());
                    }

                    JOptionPane.showMessageDialog(
                            this,
                            routeText + "\n\nPrzybycie za: " + turns +
                                    (turns == 1 ? " turę" : turns < 5 ? " tury" : " tur"),
                            "Flota w drodze",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Nie można znaleźć ścieżki do " + destination.getName(),
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

                dispose();
                onClose.run();
            }
        }
    }

    private void showSplitDialog() {
        JDialog splitDialog = new JDialog(this, "Oddziel statki", true);
        splitDialog.setLayout(new BorderLayout(10, 10));
        splitDialog.setSize(450, 400);
        splitDialog.setLocationRelativeTo(this);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("Wybierz ile statków oddzielić do nowej floty:");
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD, 12f));
        centerPanel.add(infoLabel);
        centerPanel.add(Box.createVerticalStrut(10));

        Map<ShipType, JSpinner> spinners = new HashMap<>();

        for (ShipType type : ShipType.values()) {
            int count = fleet.countShipType(type);
            if (count > 0) {
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                JLabel label = new JLabel(type.getDisplayName() + " (dostępnych: " + count + ")");

                // Można oddzielić wszystkie statki danego typu (max = count)
                SpinnerNumberModel model = new SpinnerNumberModel(0, 0, count, 1);
                JSpinner spinner = new JSpinner(model);
                spinner.setPreferredSize(new Dimension(80, 25));

                spinners.put(type, spinner);

                row.add(label, BorderLayout.CENTER);
                row.add(spinner, BorderLayout.EAST);

                centerPanel.add(row);
            }
        }

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        splitDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton confirmButton = new JButton("Potwierdź");
        confirmButton.addActionListener(e -> {
            // Zbierz statki do oddzielenia
            List<Ship> shipsToMove = new ArrayList<>();

            for (ShipType type : ShipType.values()) {
                if (spinners.containsKey(type)) {
                    int toMove = (int) spinners.get(type).getValue();

                    if (toMove > 0) {
                        int moved = 0;

                        // Skopiuj listę, żeby uniknąć ConcurrentModificationException
                        List<Ship> fleetShips = new ArrayList<>(fleet.getShips());

                        for (Ship ship : fleetShips) {
                            if (moved >= toMove) break;

                            if (ship.getType() == type) {
                                shipsToMove.add(ship);
                                moved++;
                            }
                        }
                    }
                }
            }

            if (shipsToMove.isEmpty()) {
                JOptionPane.showMessageDialog(
                        splitDialog,
                        "Wybierz przynajmniej jeden statek!",
                        "Błąd",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Sprawdź czy nie próbujemy przenieść WSZYSTKICH statków
            if (shipsToMove.size() >= fleet.getShipCount()) {
                JOptionPane.showMessageDialog(
                        splitDialog,
                        "Musisz zostawić przynajmniej jeden statek w oryginalnej flocie!",
                        "Błąd",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Utwórz nową flotę
            Fleet newFleet = new Fleet(location);

            for (Ship ship : shipsToMove) {
                fleet.removeShip(ship);
                newFleet.addShip(ship);
            }

            location.addFleet(newFleet);

            JOptionPane.showMessageDialog(
                    splitDialog,
                    "Utworzono nową flotę z " + shipsToMove.size() + " statkami",
                    "Sukces",
                    JOptionPane.INFORMATION_MESSAGE
            );

            splitDialog.dispose();
            dispose();
            onClose.run();
        });

        JButton cancelButton = new JButton("Anuluj");
        cancelButton.addActionListener(e -> splitDialog.dispose());

        bottomPanel.add(confirmButton);
        bottomPanel.add(cancelButton);
        splitDialog.add(bottomPanel, BorderLayout.SOUTH);

        splitDialog.setVisible(true);
    }
}