package model;

public enum BuildingType {

    FARMA(
            "Farma",
            20,
            false,
            null,
            false,
            1,
            0,
            2,
            1,
            0, 0,
            0, 0,
            0, 0,
            1
    ),

    FABRYKA(
            "Fabryka",
            30,
            false,
            null,
            false,
            1,
            0,
            0, 0,
            1,
            1,
            0, 0,
            0, 0,
            2
    ),

    LABORATORIUM(
            "Laboratorium",
            25,
            false,
            null,
            false,
            1,
            0,
            0, 0,
            0, 0,
            1,
            1,
            0, 0,
            2
    ),

    OSADA_GORNICZA(
            "Osada Górnicza",
            20,
            false,
            null,
            false,
            1,
            0,
            0, 0,
            1,
            1,
            0, 0,
            0, 0,
            1
    ),

    CENTRUM_ADMINISTRACYJNE(
            "Centrum Administracyjne",
            20,
            false,
            null,
            false,
            1,
            0,
            0, 0,
            0, 0,
            0, 0,
            2,
            1,
            1
    ),

    WIEZA_KOMUNIKACYJNA(
            "Wieża Komunikacyjna",
            22,
            false,
            null,
            false,
            1,
            0,
            0, 0,
            0, 0,
            1,
            0,
            0, 0,
            1
    ),

    TARG_KOLONIALNY(
            "Targ Kolonialny",
            25,
            false,
            null,
            false,
            1,
            0,
            0, 0,
            0, 0,
            0, 0,
            1,
            2,
            1
    ),

    KOPALNIA_KSIĘŻYCOWA(
            "Kopalnia Księżycowa",
            35,
            true,
            "KOPALNIA_KSIĘŻYCOWA",
            false,
            1,
            0,
            0, 0,
            0,
            1,
            0, 0,
            3,
            0,
            2
    ),

    ZAAWANSOWANA_FARMA(
            "Zaawansowana Farma",
            40,
            false,
            "ZAAWANSOWANA_FARMA",
            false,
            1,
            2,
            3,
            2,
            0, 0,
            0, 0,
            0, 0,
            2
    ),

    CENTRUM_BADAWCZE(
            "Centrum Badawcze",
            50,
            false,
            "CENTRUM_BADAWCZE",
            false,
            1,
            0,
            0, 0,
            0, 0,
            3,
            2,
            0, 0,
            3
    ),

    FABRYKA_KOSMICZNA(
            "Fabryka Kosmiczna",
            80,
            false,
            "FABRYKA_KOSMICZNA",
            false,
            1,
            0,
            0, 0,
            3,
            2,
            0, 0,
            0, 0,
            4
    ),

    POSTERUNEK_BOJOWY(
            "Posterunek Bojowy",
            60,
            false,
            "POSTERUNEK_BOJOWY",
            false,
            1,
            0,
            0, 0,
            0, 0,
            0, 0,
            0, 0,
            3
    ),

    BANK_GALAKTYCZNY(
            "Bank Galaktyczny",
            45,
            false,
            "BANK_GALAKTYCZNY",
            false,
            1,
            0,
            0, 0,
            0, 0,
            0, 0,
            5,
            1,
            2
    ),

    AKADEMIA_KOLONIALNA(
            "Akademia Kolonialna",
            70,
            false,
            "AKADEMIA_KOLONIALNA",
            false,
            1,
            1,
            0, 0,
            0, 0,
            2,
            1,
            0, 0,
            3
    ),

    HUTA_PLANETARNA(
            "Huta Planetarna",
            65,
            false,
            "HUTA_PLANETARNA",
            false,
            1,
            0,
            0, 0,
            4,
            1,
            0, 0,
            0, 0,
            3
    ),

    CENTRUM_LOGISTYCZNE(
            "Centrum Logistyczne",
            55,
            false,
            "CENTRUM_LOGISTYCZNE",
            false,
            1,
            0,
            0, 0,
            1,
            0,
            0, 0,
            3,
            0,
            2
    ),

    MEGAMIASTO(
            "Megamiasto",
            90,
            false,
            "MEGAMIASTO",
            true,
            2,
            5,
            0, 0,
            0, 0,
            0, 0,
            2,
            1,
            4
    ),

    OSIEDLE(
            "Osiedle Mieszkalne",
            30,
            false,
            null,
            true,
            5,
            3,
            0, 0,
            0, 0,
            0, 0,
            0, 0,
            1
    );

    private final String displayName;
    private final int cost;
    private final boolean requiresMoon;
    private final String techRequirement;
    private final boolean canBuildMultiple;
    private final int maxCount;

    private final int populationCapacityBonus;

    private final int foodBonus;
    private final int foodPerCapita;

    private final int productionBonus;
    private final int productionPerCapita;

    private final int researchBonus;
    private final int researchPerCapita;

    private final int creditsBonus;
    private final int creditsPerTotalPopulation;

    private final int maintenanceCost;

    BuildingType(
            String displayName,
            int cost,
            boolean requiresMoon,
            String techRequirement,
            boolean canBuildMultiple,
            int maxCount,
            int populationCapacityBonus,
            int foodBonus,
            int foodPerCapita,
            int productionBonus,
            int productionPerCapita,
            int researchBonus,
            int researchPerCapita,
            int creditsBonus,
            int creditsPerTotalPopulation,
            int maintenanceCost
    ) {
        this.displayName = displayName;
        this.cost = cost;
        this.requiresMoon = requiresMoon;
        this.techRequirement = techRequirement;
        this.canBuildMultiple = canBuildMultiple;
        this.maxCount = maxCount;
        this.populationCapacityBonus = populationCapacityBonus;
        this.foodBonus = foodBonus;
        this.foodPerCapita = foodPerCapita;
        this.productionBonus = productionBonus;
        this.productionPerCapita = productionPerCapita;
        this.researchBonus = researchBonus;
        this.researchPerCapita = researchPerCapita;
        this.creditsBonus = creditsBonus;
        this.creditsPerTotalPopulation = creditsPerTotalPopulation;
        this.maintenanceCost = maintenanceCost;
    }

    public String getDisplayName() { return displayName; }
    public int getCost() { return cost; }
    public boolean requiresMoon() { return requiresMoon; }
    public String getTechRequirement() { return techRequirement; }
    public boolean canBuildMultiple() { return canBuildMultiple; }
    public int getMaxCount() { return maxCount; }

    public int getPopulationCapacityBonus() { return populationCapacityBonus; }

    public int getFoodBonus() { return foodBonus; }
    public int getFoodPerCapita() { return foodPerCapita; }

    public int getProductionBonus() { return productionBonus; }
    public int getProductionPerCapita() { return productionPerCapita; }

    public int getResearchBonus() { return researchBonus; }
    public int getResearchPerCapita() { return researchPerCapita; }

    public int getCreditsBonus() { return creditsBonus; }
    public int getCreditsPerTotalPopulation() { return creditsPerTotalPopulation; }

    public int getMaintenanceCost() { return maintenanceCost; }

    public boolean isAvailable(ResearchManager researchManager) {
        if (techRequirement == null) return true;
        return researchManager.isUnlocked(techRequirement);
    }

    public String getEffectsDescription() {
        StringBuilder sb = new StringBuilder();

        if (populationCapacityBonus > 0) {
            sb.append("Max populacja +").append(populationCapacityBonus).append(" | ");
        }

        if (foodBonus > 0) {
            sb.append("Żywność +").append(foodBonus).append(" | ");
        }
        if (foodPerCapita > 0) {
            sb.append("Żywność +").append(foodPerCapita).append("/prac. | ");
        }

        if (productionBonus > 0) {
            sb.append("Produkcja +").append(productionBonus).append(" | ");
        }
        if (productionPerCapita > 0) {
            sb.append("Produkcja +").append(productionPerCapita).append("/prac. | ");
        }

        if (researchBonus > 0) {
            sb.append("Badania +").append(researchBonus).append(" | ");
        }
        if (researchPerCapita > 0) {
            sb.append("Badania +").append(researchPerCapita).append("/prac. | ");
        }

        if (creditsBonus > 0) {
            sb.append("Kredyty +").append(creditsBonus).append(" | ");
        }
        if (creditsPerTotalPopulation > 0) {
            sb.append("Kredyty +").append(creditsPerTotalPopulation).append("/osoba | ");
        }

        if (maintenanceCost > 0) {
            sb.append("Utrzymanie -").append(maintenanceCost).append("💰 | ");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 3);
        }

        return sb.toString();
    }
}