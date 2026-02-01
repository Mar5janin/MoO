package model;

public class TechEffect {
    private final TechEffectType type;
    private final int value;           // Wartość bonusu (np. +5 do ataku)
    private final String stringValue;  // Dla efektów typu UNLOCK_BUILDING lub UNLOCK_SHIP

    // Konstruktor dla bonusów numerycznych
    public TechEffect(TechEffectType type, int value) {
        this.type = type;
        this.value = value;
        this.stringValue = null;
    }

    // Konstruktor dla odblokowań
    public TechEffect(TechEffectType type, String stringValue) {
        this.type = type;
        this.value = 0;
        this.stringValue = stringValue;
    }

    public TechEffectType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        return stringValue;
    }

    public String getDescription() {
        return switch (type) {
            case UNLOCK_BUILDING -> "Odblokowuje: " + stringValue;
            case UNLOCK_SHIP -> "Odblokowuje statek: " + stringValue;
            case SHIP_ATTACK_BONUS -> "Atak statków +" + value;
            case SHIP_DEFENSE_BONUS -> "Obrona statków +" + value;
            case PRODUCTION_BONUS -> "Produkcja +" + value + "%";
            case RESEARCH_BONUS -> "Badania +" + value + "%";
            case CREDITS_BONUS -> "Kredyty +" + value + "%";
            case POPULATION_BONUS -> "Przyrost populacji +" + value + "%";
        };
    }
}