package model;

import java.util.ArrayList;
import java.util.List;

public enum Technology {
    // Technologie wymagające prerequisite
    ADVANCED_MINING(
            "Zaawansowane górnictwo",
            "Umożliwia budowę kopalń księżycowych",
            80,
            null,
            List.of(new TechEffect(TechEffectType.UNLOCK_BUILDING, "KOPALNIA_KSIĘŻYCOWA"))
    ),

    IMPROVED_FARMING(
            "Ulepszone rolnictwo",
            "Umożliwia budowę zaawansowanych farm",
            100,
            null,
            List.of(new TechEffect(TechEffectType.UNLOCK_BUILDING, "ZAAWANSOWANA_FARMA"))
    ),

    ADVANCED_RESEARCH(
            "Zaawansowane badania",
            "Umożliwia budowę centrów badawczych i zwiększa szybkość badań o 15%",
            120,
            null,
            List.of(
                    new TechEffect(TechEffectType.UNLOCK_BUILDING, "CENTRUM_BADAWCZE"),
                    new TechEffect(TechEffectType.RESEARCH_BONUS, 15)
            )
    ),

    IMPROVED_PRODUCTION(
            "Ulepszona produkcja",
            "Zwiększa produkcję na wszystkich planetach o 10%",
            100,
            null,
            List.of(new TechEffect(TechEffectType.PRODUCTION_BONUS, 10))
    ),

    SPACE_CONSTRUCTION(
            "Konstrukcje kosmiczne",
            "Umożliwia budowę fabryk kosmicznych",
            150,
            List.of(IMPROVED_PRODUCTION),
            List.of(new TechEffect(TechEffectType.UNLOCK_BUILDING, "FABRYKA_KOSMICZNA"))
    ),

    // Technologie wojskowe
    BASIC_WEAPONS(
            "Podstawowe uzbrojenie",
            "Odblokowuje lekkie statki bojowe",
            80,
            null,
            List.of(new TechEffect(TechEffectType.UNLOCK_SHIP, "FIGHTER"))
    ),

    IMPROVED_WEAPONS(
            "Ulepszone uzbrojenie",
            "Zwiększa siłę ataku statków o 20%",
            150,
            List.of(BASIC_WEAPONS),
            List.of(new TechEffect(TechEffectType.SHIP_ATTACK_BONUS, 20))
    ),

    IMPROVED_ARMOR(
            "Ulepszone opancerzenie",
            "Zwiększa obronę statków o 20%",
            150,
            List.of(BASIC_WEAPONS),
            List.of(new TechEffect(TechEffectType.SHIP_DEFENSE_BONUS, 20))
    ),

    HEAVY_SHIPS(
            "Ciężkie statki",
            "Odblokowuje krążowniki",
            200,
            List.of(IMPROVED_WEAPONS),
            List.of(new TechEffect(TechEffectType.UNLOCK_SHIP, "CRUISER"))
    ),

    DEFENSIVE_PLATFORMS(
            "Platformy obronne",
            "Umożliwia budowę posterunków bojowych",
            130,
            List.of(BASIC_WEAPONS),
            List.of(new TechEffect(TechEffectType.UNLOCK_BUILDING, "POSTERUNEK_BOJOWY"))
    ),

    // Technologie ekonomiczne
    TRADE_NETWORKS(
            "Sieci handlowe",
            "Zwiększa dochody z kredytów o 15%",
            100,
            null,
            List.of(new TechEffect(TechEffectType.CREDITS_BONUS, 15))
    ),

    POPULATION_GROWTH(
            "Wzrost populacji",
            "Zwiększa przyrost populacji o 25%",
            130,
            List.of(IMPROVED_FARMING),
            List.of(new TechEffect(TechEffectType.POPULATION_BONUS, 25))
    );

    private final String displayName;
    private final String description;
    private final int cost;
    private final List<Technology> prerequisites;
    private final List<TechEffect> effects;

    Technology(String displayName, String description, int cost,
               List<Technology> prerequisites, List<TechEffect> effects) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
        this.effects = effects;
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

    public List<Technology> getPrerequisites() {
        return prerequisites;
    }

    public List<TechEffect> getEffects() {
        return effects;
    }

    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }
}