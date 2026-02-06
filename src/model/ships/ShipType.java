package model.ships;

import model.tech.ResearchManager;

public enum ShipType {
    SCOUT(
            "Zwiadowca",
            "Szybki statek rozpoznawczy",
            30,
            5,
            3,
            null,
            0,
            1
    ),

    FIGHTER(
            "Myśliwiec",
            "Lekki statek bojowy",
            50,
            15,
            10,
            "Myśliwiec",
            0,
            2
    ),

    DESTROYER(
            "Niszczyciel",
            "Uniwersalny średni statek bojowy",
            80,
            25,
            20,
            "Niszczyciel",
            0,
            3
    ),

    CRUISER(
            "Krążownik",
            "Ciężki statek bojowy",
            120,
            40,
            35,
            "Krążownik",
            0,
            5
    ),

    BATTLESHIP(
            "Pancernik",
            "Potężny okręt liniowy",
            200,
            70,
            60,
            "Pancernik",
            0,
            8
    ),

    CARRIER(
            "Lotniskowiec",
            "Wsparcie floty i projekcja siły",
            180,
            35,
            70,
            "Lotniskowiec",
            0,
            7
    ),

    COLONY_SHIP(
            "Statek kolonizacyjny",
            "Pozwala kolonizować nowe planety",
            100,
            0,
            5,
            null,
            0,
            3
    ),

    SPACE_FACTORY(
            "Fabryka Kosmiczna",
            "Pozwala budować instalacje na asteroidach i gazowych gigantach",
            150,
            0,
            10,
            "Fabryka Kosmiczna",
            10,
            4
    );

    private final String displayName;
    private final String description;
    private final int cost;
    private final int attack;
    private final int defense;
    private final String requiredTech;
    private final int productionPerTurn;
    private final int maintenanceCost;

    ShipType(String displayName, String description, int cost,
             int attack, int defense, String requiredTech, int productionPerTurn, int maintenanceCost) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.attack = attack;
        this.defense = defense;
        this.requiredTech = requiredTech;
        this.productionPerTurn = productionPerTurn;
        this.maintenanceCost = maintenanceCost;
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

    public int getProductionPerTurn() {
        return productionPerTurn;
    }

    public int getMaintenanceCost() {
        return maintenanceCost;
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