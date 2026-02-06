package model.orbits.planets;

public enum PlanetSize {
    SMALL("Mała", -2),
    MEDIUM("Średnia", 0),
    LARGE("Duża", 3),
    HUGE("Ogromna", 6);

    private final String displayName;
    private final int populationBonus;

    PlanetSize(String displayName, int populationBonus) {
        this.displayName = displayName;
        this.populationBonus = populationBonus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPopulationBonus() {
        return populationBonus;
    }
}