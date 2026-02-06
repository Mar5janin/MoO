package model.orbits;

import model.buildings.SpaceInstallation;
import model.ships.Fleet;
import model.ships.ShipType;

public class GasGiant implements OrbitObject {
    private SpaceInstallation installation;

    @Override
    public OrbitType getType() {
        return OrbitType.GAS_GIANT;
    }

    public SpaceInstallation getInstallation() {
        return installation;
    }

    public void setInstallation(SpaceInstallation installation) {
        this.installation = installation;
    }

    public boolean hasInstallation() {
        return installation != null;
    }

    public boolean canBuildInstallation(Fleet fleet) {
        if (hasInstallation()) return false;
        if (fleet == null) return false;
        return fleet.countShipType(ShipType.SPACE_FACTORY) > 0;
    }
}