package model;

public enum PlanetAttribute {
    GOLD_DEPOSITS("Złoża złota", 3, 0, 0),
    ANCIENT_ARTIFACTS("Starożytne artefakty", 0, 3, 0),
    FERTILE_ALGAE("Żyzne glony", 0, 0, 2),
    NONE("Brak", 0, 0, 0);

    private final String displayName;
    private final int creditsBonus;
    private final int researchBonus;
    private final int foodBonus;

    PlanetAttribute(String displayName, int creditsBonus, int researchBonus, int foodBonus) {
        this.displayName = displayName;
        this.creditsBonus = creditsBonus;
        this.researchBonus = researchBonus;
        this.foodBonus = foodBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCreditsBonus() {
        return creditsBonus;
    }

    public int getResearchBonus() {
        return researchBonus;
    }

    public int getFoodBonus() {
        return foodBonus;
    }

    public String getDescription() {
        if (this == NONE) return "";

        StringBuilder desc = new StringBuilder();
        if (creditsBonus > 0) desc.append("+" + creditsBonus);
        if (researchBonus > 0) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("+" + researchBonus);
        }
        if (foodBonus > 0) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("+" + foodBonus);
        }
        return desc.toString();
    }
}