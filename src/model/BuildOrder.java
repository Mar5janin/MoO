package model;

public class BuildOrder {

    private final BuildingType type;
    private int remainingCost;

    public BuildOrder(BuildingType type) {
        this.type = type;
        this.remainingCost = type.getCost();
    }

    public BuildingType getType() {
        return type;
    }

    public int getRemainingCost() {
        return remainingCost;
    }

    public void progress(int production) {
        remainingCost -= production;
    }

    public boolean isFinished() {
        return remainingCost <= 0;
    }
}
