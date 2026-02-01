package model;

public enum BuildingType {

    FARMA("Farma", 20, false, "FARMA", 1, 0, 0, 0),
    FABRYKA("Fabryka", 30, false, "FABRYKA", 0, 2, 0, 0),
    LABORATORIUM("Laboratorium", 25, false, "LABORATORIUM", 0, 0, 2, 0),
    KOPALNIA_KSIĘŻYCOWA("Kopalnia Księżycowa", 35, true, "KOPALNIA_KSIĘŻYCOWA", 0, 1, 0, 3);

    private final String displayName;
    private final int cost;
    private final boolean requiresMoon;
    private final String techRequirement;  // Nazwa technologii wymaganej do budowy

    private final int populationBonus;
    private final int productionBonus;
    private final int researchBonus;
    private final int creditsBonus;

    BuildingType(
            String displayName,
            int cost,
            boolean requiresMoon,
            String techRequirement,
            int populationBonus,
            int productionBonus,
            int researchBonus,
            int creditsBonus
    ) {
        this.displayName = displayName;
        this.cost = cost;
        this.requiresMoon = requiresMoon;
        this.techRequirement = techRequirement;
        this.populationBonus = populationBonus;
        this.productionBonus = productionBonus;
        this.researchBonus = researchBonus;
        this.creditsBonus = creditsBonus;
    }

    public String getDisplayName() { return displayName; }
    public int getCost() { return cost; }
    public boolean requiresMoon() { return requiresMoon; }
    public String getTechRequirement() { return techRequirement; }

    public int getPopulationBonus() { return populationBonus; }
    public int getProductionBonus() { return productionBonus; }
    public int getResearchBonus() { return researchBonus; }
    public int getCreditsBonus() { return creditsBonus; }

    public boolean isAvailable(ResearchManager researchManager) {
        if (techRequirement == null) return true;
        return researchManager.isUnlocked(techRequirement);
    }
}