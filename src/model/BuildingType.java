package model;

public enum BuildingType {

    // Podstawowe budynki - dostępne od początku
    FARMA("Farma", 20, false, null, 1, 0, 0, 0),
    FABRYKA("Fabryka", 30, false, null, 0, 2, 0, 0),
    LABORATORIUM("Laboratorium", 25, false, null, 0, 0, 2, 0),

    // Budynki wymagające badań
    KOPALNIA_KSIĘŻYCOWA("Kopalnia Księżycowa", 35, true, "KOPALNIA_KSIĘŻYCOWA", 0, 1, 0, 3),
    ZAAWANSOWANA_FARMA("Zaawansowana Farma", 40, false, "ZAAWANSOWANA_FARMA", 2, 0, 0, 0),
    CENTRUM_BADAWCZE("Centrum Badawcze", 50, false, "CENTRUM_BADAWCZE", 0, 0, 4, 0),
    FABRYKA_KOSMICZNA("Fabryka Kosmiczna", 80, false, "FABRYKA_KOSMICZNA", 0, 5, 0, 0),
    POSTERUNEK_BOJOWY("Posterunek Bojowy", 60, false, "POSTERUNEK_BOJOWY", 0, 0, 0, 0);

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