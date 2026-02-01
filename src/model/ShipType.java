package model;

public enum ShipType {
    SCOUT(
            "Zwiadowca",
            "Szybki statek rozpoznawczy",
            30,
            5,
            3,
            null  // Dostępny od początku
    ),

    FIGHTER(
            "Myśliwiec",
            "Lekki statek bojowy",
            50,
            15,
            10,
            "FIGHTER"  // Wymaga technologii BASIC_WEAPONS
    ),

    CRUISER(
            "Krążownik",
            "Ciężki statek bojowy",
            120,
            40,
            35,
            "CRUISER"  // Wymaga technologii HEAVY_SHIPS
    ),

    COLONY_SHIP(
            "Statek kolonizacyjny",
            "Pozwala kolonizować nowe planety",
            100,
            0,
            5,
            null  // Dostępny od początku
    );

    private final String displayName;
    private final String description;
    private final int cost;
    private final int attack;
    private final int defense;
    private final String requiredTech;  // Nazwa wymaganej technologii (stringValue z TechEffect)

    ShipType(String displayName, String description, int cost,
             int attack, int defense, String requiredTech) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.attack = attack;
        this.defense = defense;
        this.requiredTech = requiredTech;
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

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public String getRequiredTech() {
        return requiredTech;
    }

    public boolean isAvailable(ResearchManager researchManager) {
        if (requiredTech == null) return true;
        return researchManager.isUnlocked(requiredTech);
    }

    // Zwraca efektywny atak z uwzględnieniem bonusów
    public int getEffectiveAttack(ResearchManager researchManager) {
        int base = attack;
        int bonus = researchManager.getShipAttackBonus();
        return base + (base * bonus / 100);
    }

    // Zwraca efektywną obronę z uwzględnieniem bonusów
    public int getEffectiveDefense(ResearchManager researchManager) {
        int base = defense;
        int bonus = researchManager.getShipDefenseBonus();
        return base + (base * bonus / 100);
    }
}