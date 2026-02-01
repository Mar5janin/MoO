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
        setSize(500, 400);
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
            JButton moveButton = new JButton("🚀 Przenieś flotę do sąsiedniego systemu");
            moveButton.setFocusPainted(false);
            moveButton.addActionListener(e -> showMoveDialog());
            centerPanel.add(moveButton);
            centerPanel.add(Box.createVerticalStrut(5));
        } else {
            JLabel movingLabel = new JLabel(
                    "→ W drodze do: " + fleet.getDestination().getName() +
                            " (" + fleet.getTurnsToDestination() + " tur)"
            );
            movingLabel.setForeground(new Color(100, 200, 100));
            centerPanel.add(movingLabel);
            centerPanel.add(Box.createVerticalStrut(5));
        }

        // Przycisk oddzielenia statków
        if (fleet.getShipCount() > 1) {
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
        List<StarSystem> neighbors = location.getNeighbors();

        if (neighbors.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Brak sąsiednich systemów!",
                    "Nie można przenieść",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String[] options = neighbors.stream()
                .map(StarSystem::getName)
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
            StarSystem destination = neighbors.stream()
                    .filter(s -> s.getName().equals(selected))
                    .findFirst()
                    .orElse(null);

            if (destination != null) {
                // Oblicz liczbę tur (możesz dostosować wzór)
                int turns = 1; // Na razie zawsze 1 tura do sąsiedniego systemu

                fleet.setDestination(destination, turns);

                JOptionPane.showMessageDialog(
                        this,
                        "Flota wyruszy do " + destination.getName() +
                                "\nPrzybycie za: " + turns + " turę/tury",
                        "Flota w drodze",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();
                onClose.run();
            }
        }
    }

    private void showSplitDialog() {
        JDialog splitDialog = new JDialog(this, "Oddziel statki", true);
        splitDialog.setLayout(new BorderLayout(10, 10));
        splitDialog.setSize(400, 300);
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
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                JLabel label = new JLabel(type.getDisplayName() + " (max: " + count + ")");
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, count - 1, 1));

                spinners.put(type, spinner);

                row.add(label, BorderLayout.CENTER);
                row.add(spinner, BorderLayout.EAST);

                centerPanel.add(row);
                centerPanel.add(Box.createVerticalStrut(5));
            }
        }

        splitDialog.add(centerPanel, BorderLayout.CENTER);

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
                        Iterator<Ship> it = fleet.getShips().iterator();

                        while (it.hasNext() && moved < toMove) {
                            Ship ship = it.next();
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