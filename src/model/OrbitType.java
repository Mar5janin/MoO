package model;

public enum OrbitType {

    PLANET("Planeta"),
    ASTEROIDS("Pole asteroid"),
    GAS_GIANT("Gazowy gigant");

    private final String displayName;

    OrbitType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
