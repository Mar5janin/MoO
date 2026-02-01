package view;

import model.*;

import javax.swing.*;
import java.awt.*;

public class ResearchPanel extends JDialog {

    private final Game game;
    private final MainWindow mainWindow;
    private JPanel techListPanel;

    public ResearchPanel(MainWindow parent, Game game) {
        super(parent, "Badania technologiczne", true);
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

        ResearchManager rm = game.getResearchManager();
        Technology current = rm.getCurrentResearch();

        JLabel currentLabel = new JLabel();
        currentLabel.setForeground(Color.WHITE);
        currentLabel.setFont(currentLabel.getFont().deriveFont(Font.BOLD, 14f));

        if (current != null) {
            int progress = rm.getCurrentProgress();
            int cost = current.getCost();
            int percent = (progress * 100) / cost;
            currentLabel.setText("Aktualnie: " + current.getDisplayName() +
                    " (" + progress + "/" + cost + " - " + percent + "%)");
        } else {
            currentLabel.setText("Brak aktualnych badań");
        }

        topPanel.add(currentLabel);
        add(topPanel, BorderLayout.NORTH);

        // Panel z listą dostępnych technologii
        techListPanel = new JPanel();
        techListPanel.setLayout(new BoxLayout(techListPanel, BoxLayout.Y_AXIS));
        techListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(techListPanel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // Dolny panel z przyciskiem zamknięcia
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Zamknij");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTechList();
    }

    private void refreshTechList() {
        techListPanel.removeAll();
        ResearchManager rm = game.getResearchManager();

        // Grupuj technologie według kategorii
        addCategorySection("Podstawowe technologie",
                Technology.BASIC_FARMING, Technology.BASIC_INDUSTRY,
                Technology.BASIC_RESEARCH, Technology.BASIC_WEAPONS);

        addCategorySection("Przemysł i produkcja",
                Technology.ADVANCED_MINING, Technology.IMPROVED_PRODUCTION);

        addCategorySection("Badania i rozwój",
                Technology.ADVANCED_RESEARCH, Technology.POPULATION_GROWTH);

        addCategorySection("Technologie wojskowe",
                Technology.IMPROVED_WEAPONS, Technology.IMPROVED_ARMOR, Technology.HEAVY_SHIPS);

        addCategorySection("Ekonomia",
                Technology.TRADE_NETWORKS);

        techListPanel.revalidate();
        techListPanel.repaint();
    }

    private void addCategorySection(String categoryName, Technology... techs) {
        JLabel categoryLabel = new JLabel(categoryName);
        categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD, 13f));
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        techListPanel.add(categoryLabel);

        for (Technology tech : techs) {
            JPanel techPanel = createTechPanel(tech);
            techPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            techListPanel.add(techPanel);
            techListPanel.add(Box.createVerticalStrut(5));
        }
    }

    private JPanel createTechPanel(Technology tech) {
        ResearchManager rm = game.getResearchManager();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Lewa część - informacje
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(tech.getDisplayName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));

        JLabel descLabel = new JLabel(tech.getDescription());
        descLabel.setFont(descLabel.getFont().deriveFont(10f));
        descLabel.setForeground(Color.GRAY);

        JLabel costLabel = new JLabel("Koszt: " + tech.getCost() + " punktów badań");
        costLabel.setFont(costLabel.getFont().deriveFont(10f));

        // Efekty
        StringBuilder effectsText = new StringBuilder("Efekty: ");
        for (int i = 0; i < tech.getEffects().size(); i++) {
            if (i > 0) effectsText.append(", ");
            effectsText.append(tech.getEffects().get(i).getDescription());
        }
        JLabel effectsLabel = new JLabel(effectsText.toString());
        effectsLabel.setFont(effectsLabel.getFont().deriveFont(10f));
        effectsLabel.setForeground(new Color(100, 200, 255));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(costLabel);
        infoPanel.add(effectsLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        // Prawa część - przycisk
        JButton actionButton = new JButton();
        actionButton.setFocusPainted(false);

        if (rm.isResearched(tech)) {
            actionButton.setText("✓ Zbadane");
            actionButton.setEnabled(false);
            actionButton.setBackground(new Color(100, 200, 100));
        } else if (rm.getCurrentResearch() == tech) {
            actionButton.setText("W trakcie...");
            actionButton.setEnabled(true);
            actionButton.setBackground(new Color(255, 200, 100));
            actionButton.addActionListener(e -> {
                rm.setCurrentResearch(null);
                mainWindow.updateResourceDisplay();
                refreshTechList();
            });
        } else if (rm.canResearch(tech)) {
            actionButton.setText("Badaj");
            actionButton.setEnabled(true);
            actionButton.addActionListener(e -> {
                rm.setCurrentResearch(tech);
                mainWindow.updateResourceDisplay();
                refreshTechList();
            });
        } else {
            actionButton.setText("Niedostępne");
            actionButton.setEnabled(false);

            // Pokaż brakujące prerequisity
            if (tech.hasPrerequisites()) {
                StringBuilder missing = new StringBuilder("<html>Wymaga:<br>");
                for (Technology prereq : tech.getPrerequisites()) {
                    if (!rm.isResearched(prereq)) {
                        missing.append("• ").append(prereq.getDisplayName()).append("<br>");
                    }
                }
                missing.append("</html>");
                actionButton.setToolTipText(missing.toString());
            }
        }

        panel.add(actionButton, BorderLayout.EAST);

        return panel;
    }
}