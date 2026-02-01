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
    private JPanel queuePanel;

    private static final int QUEUE_WIDTH = 550;
    private static final int ROW_HEIGHT = 32;
    private static final int BUTTON_WIDTH = 42;
    private static final int BUTTON_COUNT = 3;
    private static final int BUTTON_GAP = 2;

    public PlanetInfoPanel(
            Planet planet,
            StarSystem system,
            MainWindow mainWindow
    ) {
        this.planet = planet;
        this.system = system;
        this.mainWindow = mainWindow;

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

            JButton colonizeButton = new JButton("Skolonizuj planetę");
            colonizeButton.addActionListener(e -> {
                planet.colonize();
                mainWindow.showPlanet(planet, system);
            });

            add(colonizeButton);
        }
    }

    // =============================
    // SKOLONIZOWANA
    // =============================
    private void renderColonized() {

        add(new JLabel("Status: Skolonizowana"));
        add(Box.createVerticalStrut(10));

        add(sectionTitle("Kolonia"));
        add(new JLabel("Populacja: " + planet.getPopulation()));
        add(new JLabel("Produkcja: " + planet.getProduction()));
        add(new JLabel("Badania: " + planet.getResearch()));
        add(new JLabel("Kredyty: " + planet.getCredits()));

        add(Box.createVerticalStrut(12));

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
    // KOLEJKA BUDOWY - Z WYŚWIETLANIEM TUR
    // =============================
    private void renderBuildQueueContent() {

        queuePanel.removeAll();

        if (planet.getBuildQueue().isEmpty()) {
            queuePanel.add(new JLabel("Kolejka pusta"));
        } else {
            for (int i = 0; i < planet.getBuildQueue().size(); i++) {
                int index = i;
                BuildOrder order = planet.getBuildQueue().get(i);
                BuildingType type = order.getType();

                // Oblicz ilość tur
                int turnsRemaining = calculateTurnsRemaining(order, i);
                String turnsText = turnsRemaining + (turnsRemaining == 1 ? " tura" :
                        turnsRemaining < 5 ? " tury" : " tur");

                String fullText = (i + 1) + ". " +
                        type.getDisplayName() +
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

                // ===== PANEL PRZYCISKÓW (STAŁA SZEROKOŚĆ) =====
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, BUTTON_GAP, 4));

                int buttonsPanelWidth =
                        BUTTON_COUNT * BUTTON_WIDTH +
                                (BUTTON_COUNT - 1) * BUTTON_GAP + 8;

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
                        () -> mainWindow.showPlanet(planet, system)
                ).setVisible(true)
        );
        add(addButton);
    }

    // =============================
    // OBLICZANIE TUR
    // =============================
    private int calculateTurnsRemaining(BuildOrder order, int queueIndex) {
        int production = planet.getProduction();
        if (production <= 0) return 999; // Zabezpieczenie

        // Jeśli to pierwszy element w kolejce, używamy jego aktualnego pozostałego kosztu
        if (queueIndex == 0) {
            return (int) Math.ceil((double) order.getRemainingCost() / production);
        }

        // Dla kolejnych elementów używamy pełnego kosztu
        return (int) Math.ceil((double) order.getType().getCost() / production);
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