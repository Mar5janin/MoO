package model;

public enum SpaceInstallationType {
    ASTEROID_LABORATORY(
            "Laboratorium Asteroidowe",
            "Dostarcza badania z pasa asteroid",
            50,
            0,
            5
    ),

    ASTEROID_MINE(
            "Kopalnia Asteroidowa",
            "Dostarcza kredyty z pasa asteroid",
            50,
            5,
            0
    ),

    GAS_MINE(
            "Kopalnia Gazowa",
            "Wydobywa gaz z gazowego giganta",
            60,
            8,
            0
    ),

    BATTLE_STATION(
            "Posterunek Bojowy",
            "Broni systemu przed wrogimi flotami",
            100,
            0,
            0
    );

    private final String displayName;
    private final String description;
    private final int cost;
    private final int creditsBonus;
    private final int researchBonus;

    SpaceInstallationType(String displayName, String description, int cost,
                          int creditsBonus, int researchBonus) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.creditsBonus = creditsBonus;
        this.researchBonus = researchBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public int getCreditsBonus() {
        return creditsBonus;
    }

    public int getResearchBonus() {
        return researchBonus;
    }
}