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

        techListPanel = new JPanel();
        techListPanel.setLayout(new BoxLayout(techListPanel, BoxLayout.Y_AXIS));
        techListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(techListPanel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

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

        addCategorySection("Przemysł i produkcja",
                Technology.IMPROVED_PRODUCTION, Technology.IMPROVED_FARMING,
                Technology.SPACE_CONSTRUCTION, Technology.ADVANCED_MINING);

        addCategorySection("Badania i rozwój",
                Technology.ADVANCED_RESEARCH, Technology.POPULATION_GROWTH);

        addCategorySection("Technologie wojskowe - podstawowe",
                Technology.BASIC_WEAPONS, Technology.IMPROVED_WEAPONS,
                Technology.IMPROVED_ARMOR, Technology.DESTROYER_TECH,
                Technology.DEFENSIVE_PLATFORMS);

        addCategorySection("Technologie wojskowe - zaawansowane",
                Technology.HEAVY_SHIPS, Technology.CAPITAL_SHIPS,
                Technology.ADVANCED_FLEET_DOCTRINE);

        addCategorySection("Ekonomia i rozwój cywilny",
                Technology.TRADE_NETWORKS, Technology.ADVANCED_ECONOMICS,
                Technology.COLONIAL_ADMINISTRATION, Technology.URBANIZATION);

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
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(tech.getDisplayName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));

        JLabel descLabel = new JLabel(tech.getDescription());
        descLabel.setFont(descLabel.getFont().deriveFont(10f));
        descLabel.setForeground(Color.GRAY);

        JLabel costLabel = new JLabel("Koszt: " + tech.getCost() + " punktów badań");
        costLabel.setFont(costLabel.getFont().deriveFont(10f));

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

        if (tech.hasPrerequisites() && !rm.isResearched(tech)) {
            StringBuilder prereqText = new StringBuilder("Wymaga: ");
            boolean first = true;
            for (Technology prereq : tech.getPrerequisites()) {
                if (!first) prereqText.append(", ");
                prereqText.append(prereq.getDisplayName());
                if (rm.isResearched(prereq)) {
                    prereqText.append(" ✓");
                }
                first = false;
            }
            JLabel prereqLabel = new JLabel(prereqText.toString());
            prereqLabel.setFont(prereqLabel.getFont().deriveFont(10f));
            prereqLabel.setForeground(new Color(255, 150, 100));
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(prereqLabel);
        }

        panel.add(infoPanel, BorderLayout.CENTER);

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
        }

        panel.add(actionButton, BorderLayout.EAST);

        return panel;
    }
}