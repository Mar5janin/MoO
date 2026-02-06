package model.production;

import model.orbits.OrbitObject;
import model.buildings.SpaceInstallationType;

public class InstallationOrder {
    private final SpaceInstallationType type;
    private final OrbitObject target;
    private int remainingCost;

    public InstallationOrder(SpaceInstallationType type, OrbitObject target) {
        this.type = type;
        this.target = target;
        this.remainingCost = type.getCost();
    }

    public SpaceInstallationType getType() {
        return type;
    }

    public OrbitObject getTarget() {
        return target;
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

    public int getOriginalCost() {
        return type.getCost();
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }
}