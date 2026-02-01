package model;

public enum ShipType {
    SCOUT(
            "Zwiadowca",
            "Szybki statek rozpoznawczy",
            30,
            5,
            3,
            null
    ),

    FIGHTER(
            "Myśliwiec",
            "Lekki statek bojowy",
            50,
            15,
            10,
            "Myśliwiec"
    ),

    DESTROYER(
            "Niszczyciel",
            "Uniwersalny średni statek bojowy",
            80,
            25,
            20,
            "Niszczyciel"
    ),

    CRUISER(
            "Krążownik",
            "Ciężki statek bojowy",
            120,
            40,
            35,
            "Krążownik"
    ),

    BATTLESHIP(
            "Pancernik",
            "Potężny okręt liniowy",
            200,
            70,
            60,
            "Pancernik"
    ),

    CARRIER(
            "Lotniskowiec",
            "Wsparcie floty i projekcja siły",
            180,
            35,
            70,
            "Lotniskowiec"
    ),

    COLONY_SHIP(
            "Statek kolonizacyjny",
            "Pozwala kolonizować nowe planety",
            100,
            0,
            5,
            null
    ),

    SPACE_FACTORY(
            "Fabryka Kosmiczna",
            "Pozwala budować instalacje na asteroidach i gazowych gigantach",
            150,
            0,
            10,
            "FABRYKA_KOSMICZNA"
    );

    private final String displayName;
    private final String description;
    private final int cost;
    private final int attack;
    private final int defense;
    private final String requiredTech;

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

    public int getEffectiveAttack(ResearchManager researchManager) {
        int base = attack;
        int bonus = researchManager.getShipAttackBonus();
        return base + (base * bonus / 100);
    }

    public int getEffectiveDefense(ResearchManager researchManager) {
        int base = defense;
        int bonus = researchManager.getShipDefenseBonus();
        return base + (base * bonus / 100);
    }
}