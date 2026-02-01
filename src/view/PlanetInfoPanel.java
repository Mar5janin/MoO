package view;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PlanetInfoPanel extends JPanel {

    private final Planet planet;
    private final StarSystem system;
    private final MainWindow mainWindow;
    private final Game game;
    private JPanel queuePanel;

    private static final int QUEUE_WIDTH = 550;
    private static final int ROW_HEIGHT = 32;
    private static final int BUTTON_WIDTH = 42;
    private static final int BUTTON_COUNT = 3;
    private static final int BUTTON_GAP = 2;

    public PlanetInfoPanel(
            Planet planet,
            StarSystem system,
            MainWindow mainWindow,
            Game game
    ) {
        this.planet = planet;
        this.system = system;
        this.mainWindow = mainWindow;
        this.game = game;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildUI();
    }

    private void buildUI() {

        add(title("PLANETA"));
        add(Box.createVerticalStrut(10));

        add(new JLabel("Typ: " + planet.getPlanetType().getDisplayName()));
        add(new JLabel("Zdatna do życia: " + yesNo(planet.isHabitable())));
        add(new JLabel("Księżyc: " + yesNo(planet.hasMoon())));
        add(Box.createVerticalStrut(10));

        if (!planet.isColonized()) {
            renderUncolonized();
        } else {
            renderColonized();
        }

        add(Box.createVerticalStrut(15));

        JButton back = new JButton("← Powrót do systemu");
        back.addActionListener(e ->
                mainWindow.onSystemSelected(system)
        );
        add(back);
    }

    // =============================
    // NIESKOLONIZOWANA
    // =============================
    private void renderUncolonized() {

        add(new JLabel("Status: Nie skolonizowana"));

        if (planet.isHabitable()) {
            add(Box.createVerticalStrut(10));

            // Sprawdź czy w systemie jest statek kolonizacyjny
            Fleet fleet = system.getPlayerFleet();
            boolean hasColonyShip = fleet != null && fleet.countShipType(ShipType.COLONY_SHIP) > 0;

            if (hasColonyShip) {
                JButton colonizeButton = new JButton("Skolonizuj planetę (użyje statku kolonizacyjnego)");
                colonizeButton.addActionListener(e -> {
                    planet.colonize(fleet);
                    mainWindow.showPlanet(planet, system);
                });
                add(colonizeButton);
            } else {
                JLabel needShip = new JLabel("Potrzebujesz statku kolonizacyjnego aby skolonizować tę planetę");
                needShip.setForeground(Color.GRAY);
                add(needShip);
            }
        }
    }

    // =============================
    // SKOLONIZOWANA
    // =============================
    private void renderColonized() {

        add(new JLabel("Status: Skolonizowana"));
        add(Box.createVerticalStrut(10));

        // === POPULACJA I ZASOBY ===
        add(sectionTitle("Populacja i Zasoby"));

        JLabel popLabel = new JLabel(String.format("Populacja: %d / %d",
                planet.getTotalPopulation(), planet.getMaxPopulation()));
        popLabel.setFont(popLabel.getFont().deriveFont(Font.BOLD));
        add(popLabel);

        // Progres wzrostu populacji
        int foodAcc = planet.getFoodAccumulated();
        int foodNeeded = planet.getFoodNeededForGrowth();
        JProgressBar growthBar = new JProgressBar(0, foodNeeded);
        growthBar.setValue(foodAcc);
        growthBar.setString("Wzrost: " + foodAcc + "/" + foodNeeded);
        growthBar.setStringPainted(true);
        growthBar.setMaximumSize(new Dimension(300, 20));
        add(growthBar);

        add(Box.createVerticalStrut(5));
        add(new JLabel("🌾 Żywność: " + planet.getFoodProduction() + " / turę"));
        add(new JLabel("🏭 Produkcja: " + planet.getProduction()));
        add(new JLabel("🔬 Badania: " + planet.getResearch()));
        add(new JLabel("💰 Kredyty: " + planet.getCredits()));

        add(Box.createVerticalStrut(12));

        // === ZARZĄDZANIE POPULACJĄ ===
        add(sectionTitle("Zarządzanie Populacją"));

        JPanel popManagement = new JPanel();
        popManagement.setLayout(new BoxLayout(popManagement, BoxLayout.Y_AXIS));
        popManagement.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        popManagement.setMaximumSize(new Dimension(500, 300));

        // Żywność
        popManagement.add(createPopulationControl(
                "🌾 Produkcja żywności",
                planet.getPopulationOnFood(),
                planet.getTotalPopulation(),
                planet::setPopulationOnFood
        ));

        popManagement.add(Box.createVerticalStrut(8));

        // Produkcja
        popManagement.add(createPopulationControl(
                "🏭 Budowa",
                planet.getPopulationOnProduction(),
                planet.getTotalPopulation(),
                planet::setPopulationOnProduction
        ));

        popManagement.add(Box.createVerticalStrut(8));

        // Badania
        popManagement.add(createPopulationControl(
                "🔬 Badania",
                planet.getPopulationOnResearch(),
                planet.getTotalPopulation(),
                planet::setPopulationOnResearch
        ));

        popManagement.add(Box.createVerticalStrut(8));

        // Bezrobotni
        int unemployed = planet.getUnassignedPopulation();
        JLabel unemployedLabel = new JLabel("💼 Bezrobotni (generują kredyty): " + unemployed);
        unemployedLabel.setForeground(unemployed > 0 ? new Color(255, 150, 0) : new Color(255, 215, 0));
        unemployedLabel.setFont(unemployedLabel.getFont().deriveFont(Font.BOLD, 11f));
        popManagement.add(unemployedLabel);

        add(popManagement);

        add(Box.createVerticalStrut(12));

        // === KOLEJKA BUDOWY ===
        add(sectionTitle("Kolejka budowy"));

        queuePanel = new JPanel();
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(queuePanel);
        scroll.setPreferredSize(new Dimension(QUEUE_WIDTH, 160));
        scroll.setMaximumSize(new Dimension(QUEUE_WIDTH, 160));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll);

        renderBuildQueueContent();
    }

    // =============================
    // KONTROLA POPULACJI Z PRZYCISKAMI +/-
    // =============================
    private JPanel createPopulationControl(String label, int current, int max, java.util.function.Consumer<Integer> onChange) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Lewa część - label z wartością
        JLabel titleLabel = new JLabel(label + ": " + current);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 11f));
        panel.add(titleLabel, BorderLayout.WEST);

        // Prawa część - przyciski
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton minusButton = new JButton("-");
        JButton plusButton = new JButton("+");

        Dimension btnSize = new Dimension(45, 25);
        minusButton.setPreferredSize(btnSize);
        plusButton.setPreferredSize(btnSize);

        // Aktualizuj stan przycisków
        Runnable updateButtons = () -> {
            int val = planet.getPopulationOnFood() + planet.getPopulationOnProduction() + planet.getPopulationOnResearch();
            int available = planet.getTotalPopulation() - val;

            // Sprawdź aktualną wartość dla tego kontrolera
            int currentValue = 0;
            if (label.contains("żywność")) {
                currentValue = planet.getPopulationOnFood();
            } else if (label.contains("Produkcja")) {
                currentValue = planet.getPopulationOnProduction();
            } else if (label.contains("Badania")) {
                currentValue = planet.getPopulationOnResearch();
            }

            minusButton.setEnabled(currentValue > 0);
            plusButton.setEnabled(available > 0);
            titleLabel.setText(label + ": " + currentValue);
        };

        minusButton.addActionListener(e -> {
            int newValue = Math.max(0, current - 1);
            onChange.accept(newValue);
            mainWindow.showPlanet(planet, system);
        });

        plusButton.addActionListener(e -> {
            int newValue = Math.min(max, current + 1);
            onChange.accept(newValue);
            mainWindow.showPlanet(planet, system);
        });

        updateButtons.run();

        buttonsPanel.add(minusButton);
        buttonsPanel.add(plusButton);
        panel.add(buttonsPanel, BorderLayout.EAST);

        return panel;
    }

    // =============================
    // KOLEJKA BUDOWY - Z WYŚWIETLANIEM TUR I RUSH BUY
    // =============================
    private void renderBuildQueueContent() {

        queuePanel.removeAll();

        if (planet.getBuildQueue().isEmpty()) {
            queuePanel.add(new JLabel("Kolejka pusta"));
        } else {
            for (int i = 0; i < planet.getBuildQueue().size(); i++) {
                int index = i;
                ProductionOrder order = planet.getBuildQueue().get(i);

                // Oblicz ilość tur
                int turnsRemaining = calculateTurnsRemaining(order, i);
                String turnsText = turnsRemaining + (turnsRemaining == 1 ? " tura" :
                        turnsRemaining < 5 ? " tury" : " tur");

                String fullText = (i + 1) + ". " +
                        order.getDisplayName() +
                        " (" + turnsText + ")";

                JPanel row = new JPanel(new GridBagLayout());
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridy = 0;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.insets = new Insets(0, 4, 0, 4);

                // ===== NAZWA (ELASTYCZNA Z TOOLTIP) =====
                JLabel nameLabel = new JLabel(fullText);
                nameLabel.setToolTipText(fullText);

                gbc.gridx = 0;
                gbc.weightx = 1.0;
                row.add(nameLabel, gbc);

                // ===== PANEL PRZYCISKÓW =====
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, BUTTON_GAP, 4));

                int rushBuyButtonWidth = 80; // Szerszy przycisk dla "Rush"
                int buttonsPanelWidth =
                        BUTTON_COUNT * BUTTON_WIDTH +
                                rushBuyButtonWidth +
                                (BUTTON_COUNT) * BUTTON_GAP + 8;

                buttons.setPreferredSize(new Dimension(buttonsPanelWidth, ROW_HEIGHT));
                buttons.setMinimumSize(new Dimension(buttonsPanelWidth, ROW_HEIGHT));
                buttons.setMaximumSize(new Dimension(buttonsPanelWidth, ROW_HEIGHT));

                JButton up = new JButton("↑");
                JButton down = new JButton("↓");
                JButton remove = new JButton("X");

                Dimension btnSize = new Dimension(BUTTON_WIDTH, 24);
                up.setPreferredSize(btnSize);
                down.setPreferredSize(btnSize);
                remove.setPreferredSize(btnSize);

                up.addActionListener(e -> {
                    planet.moveQueueUp(index);
                    mainWindow.showPlanet(planet, system);
                });

                down.addActionListener(e -> {
                    planet.moveQueueDown(index);
                    mainWindow.showPlanet(planet, system);
                });

                remove.addActionListener(e -> {
                    planet.removeFromQueue(index);
                    mainWindow.showPlanet(planet, system);
                });

                // Przycisk Rush Buy (tylko dla pierwszego elementu)
                if (i == 0) {
                    int rushCost = planet.getRushBuyCost();
                    JButton rushButton = new JButton("Rush: " + rushCost + "💰");
                    rushButton.setPreferredSize(new Dimension(rushBuyButtonWidth, 24));
                    rushButton.setToolTipText("Natychmiast zakończ produkcję za " + rushCost + " kredytów");

                    if (game.getTotalCredits() >= rushCost) {
                        rushButton.setBackground(new Color(100, 200, 100));
                    } else {
                        rushButton.setEnabled(false);
                        rushButton.setBackground(Color.LIGHT_GRAY);
                    }

                    rushButton.addActionListener(e -> {
                        boolean success = game.rushBuyOnPlanet(planet, system);
                        if (success) {
                            mainWindow.updateResourceDisplay();
                            mainWindow.showPlanet(planet, system);
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Produkcja zakończona natychmiast!",
                                    "Rush Buy",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    });

                    buttons.add(rushButton);
                }

                buttons.add(up);
                buttons.add(down);
                buttons.add(remove);

                gbc.gridx = 1;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.EAST;
                row.add(buttons, gbc);

                // ===== AUTOMATYCZNE SKRACANIE TEKSTU =====
                row.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        FontMetrics fm = nameLabel.getFontMetrics(nameLabel.getFont());
                        int availableWidth = row.getWidth() - buttonsPanelWidth - 20;
                        if (availableWidth > 0) {
                            nameLabel.setText(
                                    ellipsizeToWidth(fullText, fm, availableWidth)
                            );
                        }
                    }
                });

                queuePanel.add(row);
            }

        }

        queuePanel.revalidate();
        queuePanel.repaint();

        JButton addButton = new JButton("Dodaj do kolejki");
        addButton.addActionListener(e ->
                new BuildDialog(
                        mainWindow,
                        planet,
                        game,
                        () -> mainWindow.showPlanet(planet, system)
                ).setVisible(true)
        );
        add(addButton);
    }

    // =============================
    // OBLICZANIE TUR
    // =============================
    private int calculateTurnsRemaining(ProductionOrder order, int queueIndex) {
        int production = planet.getProduction();
        if (production <= 0) return 999; // Zabezpieczenie

        // Jeśli to pierwszy element w kolejce, używamy jego aktualnego pozostałego kosztu
        if (queueIndex == 0) {
            return (int) Math.ceil((double) order.getRemainingCost() / production);
        }

        // Dla kolejnych elementów używamy pełnego kosztu
        return (int) Math.ceil((double) order.getOriginalCost() / production);
    }

    // =============================
    // UI HELPERS
    // =============================
    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }

    private String yesNo(boolean value) {
        return value ? "Tak" : "Nie";
    }

    private String ellipsizeToWidth(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;

        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);

        for (int i = text.length(); i > 0; i--) {
            String sub = text.substring(0, i);
            if (fm.stringWidth(sub) + ellipsisWidth <= maxWidth) {
                return sub + ellipsis;
            }
        }
        return ellipsis;
    }
}