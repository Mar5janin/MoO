package model;

public class Building {

    private final BuildingType type;

    public Building(BuildingType type) {
        this.type = type;
    }

    public BuildingType getType() {
        return type;
    }
}
