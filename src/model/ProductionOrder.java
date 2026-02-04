package model;

public class ProductionOrder {

    private final ProductionType productionType;
    private final BuildingType buildingType;
    private final ShipType shipType;
    private int remainingCost;

    public ProductionOrder(BuildingType buildingType) {
        this.productionType = ProductionType.BUILDING;
        this.buildingType = buildingType;
        this.shipType = null;
        this.remainingCost = buildingType.getCost();
    }

    public ProductionOrder(ShipType shipType) {
        this.productionType = ProductionType.SHIP;
        this.buildingType = null;
        this.shipType = shipType;
        this.remainingCost = shipType.getCost();
    }

    public ProductionType getProductionType() {
        return productionType;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public int getRemainingCost() {
        return remainingCost;
    }

    public void setRemainingCost(int cost) {
        this.remainingCost = cost;
    }

    public void progress(int production) {
        remainingCost -= production;
    }

    public boolean isFinished() {
        return remainingCost <= 0;
    }

    public int getOriginalCost() {
        return switch (productionType) {
            case BUILDING -> buildingType.getCost();
            case SHIP -> shipType.getCost();
        };
    }

    public String getDisplayName() {
        return switch (productionType) {
            case BUILDING -> buildingType.getDisplayName();
            case SHIP -> shipType.getDisplayName();
        };
    }
}