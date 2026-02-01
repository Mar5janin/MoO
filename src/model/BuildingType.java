package model;

public enum BuildingType {

    // ========== PODSTAWOWE BUDYNKI ==========

    FARMA(
            "Farma",
            20,
            false,
            null,
            0,      // populationCapacity
            3,      // foodBonus (pasywny)
            2,      // foodPerCapita (+2 za każdego pracownika)
            0, 0,   // production
            0, 0,   // research
            0, 0, 0 // credits
    ),

    FABRYKA(
            "Fabryka",
            30,
            false,
            null,
            0,      // populationCapacity
            0, 0,   // food
            2,      // productionBonus (pasywny - bez tego 0 produkcji!)
            2,      // productionPerCapita (+2 za każdego pracownika)
            0, 0,   // research
            0, 0, 0 // credits
    ),

    LABORATORIUM(
            "Laboratorium",
            25,
            false,
            null,
            0,      // populationCapacity
            0, 0,   // food
            0, 0,   // production
            1,      // researchBonus (pasywny)
            2,      // researchPerCapita (+2 za każdego pracownika)
            0, 0, 0 // credits
    ),

    // ========== BUDYNKI WYMAGAJĄCE BADAŃ ==========

    KOPALNIA_KSIĘŻYCOWA(
            "Kopalnia Księżycowa",
            35,
            true,
            "KOPALNIA_KSIĘŻYCOWA",
            0,      // populationCapacity
            0, 0,   // food
            0,      // productionBonus (pasywny)
            1,      // productionPerCapita
            0, 0,   // research
            3,      // creditsBonus (pasywny)
            0, 0    // creditsPerCapita, creditsPerTotal
    ),

    ZAAWANSOWANA_FARMA(
            "Zaawansowana Farma",
            40,
            false,
            "ZAAWANSOWANA_FARMA",
            2,      // populationCapacity (+2 max populacji)
            5,      // foodBonus (pasywny)
            3,      // foodPerCapita (+3 za każdego pracownika)
            0, 0,   // production
            0, 0,   // research
            0, 0, 0 // credits
    ),

    CENTRUM_BADAWCZE(
            "Centrum Badawcze",
            50,
            false,
            "CENTRUM_BADAWCZE",
            0,      // populationCapacity
            0, 0,   // food
            0, 0,   // production
            4,      // researchBonus (pasywny)
            3,      // researchPerCapita (+3 za każdego pracownika)
            0, 0, 0 // credits
    ),

    FABRYKA_KOSMICZNA(
            "Fabryka Kosmiczna",
            80,
            false,
            "FABRYKA_KOSMICZNA",
            0,      // populationCapacity
            0, 0,   // food
            5,      // productionBonus (pasywny)
            3,      // productionPerCapita (+3 za każdego pracownika)
            0, 0,   // research
            0, 0, 0 // credits
    ),

    POSTERUNEK_BOJOWY(
            "Posterunek Bojowy",
            60,
            false,
            "POSTERUNEK_BOJOWY",
            0,      // populationCapacity
            0, 0,   // food
            0, 0,   // production
            0, 0,   // research
            0, 0, 0 // credits (obrona, póki co bez efektu)
    ),

    // ========== NOWE BUDYNKI: EKONOMIA ==========

    BANK_GALAKTYCZNY(
            "Bank Galaktyczny",
            45,
            false,
            "BANK_GALAKTYCZNY",
            0,      // populationCapacity
            0, 0,   // food
            0, 0,   // production
            0, 0,   // research
            5,      // creditsBonus (pasywny)
            0,      // creditsPerCapita
            1       // creditsPerTotalPopulation (podatki: +1 za każdego na planecie)
    ),

    OSIEDLE(
            "Osiedle Mieszkalne",
            30,
            false,
            null,
            3,      // populationCapacity (+3 max populacji)
            0, 0,   // food
            0, 0,   // production
            0, 0,   // research
            0, 0, 0 // credits
    );

    private final String displayName;
    private final int cost;
    private final boolean requiresMoon;
    private final String techRequirement;

    // Bonusy
    private final int populationCapacityBonus;

    private final int foodBonus;                    // Pasywny bonus do żywności
    private final int foodPerCapita;                // Bonus za każdego pracownika na food

    private final int productionBonus;              // Pasywny bonus do produkcji
    private final int productionPerCapita;          // Bonus za każdego pracownika na production

    private final int researchBonus;                // Pasywny bonus do badań
    private final int researchPerCapita;            // Bonus za każdego pracownika na research

    private final int creditsBonus;                 // Pasywny bonus do kredytów
    private final int creditsPerCapita;             // Bonus za każdego BEZROBOTNEGO
    private final int creditsPerTotalPopulation;    // Podatki - za każdego na planecie

    BuildingType(
            String displayName,
            int cost,
            boolean requiresMoon,
            String techRequirement,
            int populationCapacityBonus,
            int foodBonus,
            int foodPerCapita,
            int productionBonus,
            int productionPerCapita,
            int researchBonus,
            int researchPerCapita,
            int creditsBonus,
            int creditsPerCapita,
            int creditsPerTotalPopulation
    ) {
        this.displayName = displayName;
        this.cost = cost;
        this.requiresMoon = requiresMoon;
        this.techRequirement = techRequirement;
        this.populationCapacityBonus = populationCapacityBonus;
        this.foodBonus = foodBonus;
        this.foodPerCapita = foodPerCapita;
        this.productionBonus = productionBonus;
        this.productionPerCapita = productionPerCapita;
        this.researchBonus = researchBonus;
        this.researchPerCapita = researchPerCapita;
        this.creditsBonus = creditsBonus;
        this.creditsPerCapita = creditsPerCapita;
        this.creditsPerTotalPopulation = creditsPerTotalPopulation;
    }

    public String getDisplayName() { return displayName; }
    public int getCost() { return cost; }
    public boolean requiresMoon() { return requiresMoon; }
    public String getTechRequirement() { return techRequirement; }

    public int getPopulationCapacityBonus() { return populationCapacityBonus; }

    public int getFoodBonus() { return foodBonus; }
    public int getFoodPerCapita() { return foodPerCapita; }

    public int getProductionBonus() { return productionBonus; }
    public int getProductionPerCapita() { return productionPerCapita; }

    public int getResearchBonus() { return researchBonus; }
    public int getResearchPerCapita() { return researchPerCapita; }

    public int getCreditsBonus() { return creditsBonus; }
    public int getCreditsPerCapita() { return creditsPerCapita; }
    public int getCreditsPerTotalPopulation() { return creditsPerTotalPopulation; }

    public boolean isAvailable(ResearchManager researchManager) {
        if (techRequirement == null) return true;
        return researchManager.isUnlocked(techRequirement);
    }

    /**
     * Zwraca opis efektów budynku
     */
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
        if (creditsPerCapita > 0) {
            sb.append("Kredyty +").append(creditsPerCapita).append("/bezrob. | ");
        }
        if (creditsPerTotalPopulation > 0) {
            sb.append("Kredyty +").append(creditsPerTotalPopulation).append("/osoba | ");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 3); // Usuń ostatni " | "
        }

        return sb.toString();
    }
}