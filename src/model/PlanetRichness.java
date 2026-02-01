package model;

public enum PlanetRichness {
    POOR("Uboga", -1),
    NORMAL("Normalna", 0),
    RICH("Bogata", 2),
    ULTRA_RICH("Ultra bogata", 4);

    private final String displayName;
    private final int productionBonus;

    PlanetRichness(String displayName, int productionBonus) {
        this.displayName = displayName;
        this.productionBonus = productionBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getProductionBonus() {
        return productionBonus;
    }
}