package model;

public enum PlanetType {

    TERRAN("Ziemiopodobna", true),
    OCEAN("Oceaniczna", true),
    DESERT("Pustynna", true),
    TUNDRA("Tundrowa", true),

    BARREN("Jałowa", false),
    TOXIC("Toksyczna", false),
    RADIATED("Radioaktywna", false),
    VOLCANIC("Wulkaniczna", false),
    ICE("Lodowa", false);

    private final String displayName;
    private final boolean habitable;

    PlanetType(String displayName, boolean habitable) {
        this.displayName = displayName;
        this.habitable = habitable;
    }

    public boolean isHabitable() {
        return habitable;
    }

    public String getDisplayName() {
        return displayName;
    }
}
