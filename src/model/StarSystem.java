package model;

import java.util.ArrayList;
import java.util.List;

public class StarSystem {

    private String name;
    private int x;
    private int y;

    private List<StarSystem> neighbors = new ArrayList<>();
    private List<OrbitSlot> orbits = new ArrayList<>();
    private List<Fleet> fleets = new ArrayList<>();
    private SpaceInstallation battleStation;

    public StarSystem(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }


    public List<OrbitSlot> getOrbits() {
        return orbits;
    }

    public void addOrbit(OrbitSlot orbit) {
        orbits.add(orbit);
    }


    public void addNeighbor(StarSystem system) {
        if (system == null || system == this) return;
        if (!neighbors.contains(system)) {
            neighbors.add(system);
        }
    }

    public List<StarSystem> getNeighbors() {
        return neighbors;
    }


    public double distanceTo(StarSystem other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public String getName() { return name; }

    public List<Fleet> getFleets() {
        return fleets;
    }

    public void addFleet(Fleet fleet) {
        if (!fleets.contains(fleet)) {
            fleets.add(fleet);
        }
    }

    public void removeFleet(Fleet fleet) {
        fleets.remove(fleet);
    }

    public Fleet getPlayerFleet() {
        return fleets.stream()
                .filter(f -> f.getOwner() == null)
                .findFirst()
                .orElse(null);
    }

    public Fleet getOrCreatePlayerFleet() {
        Fleet fleet = getPlayerFleet();
        if (fleet == null) {
            fleet = new Fleet(this);
            fleets.add(fleet);
        }
        return fleet;
    }

    public SpaceInstallation getBattleStation() {
        return battleStation;
    }

    public void setBattleStation(SpaceInstallation battleStation) {
        this.battleStation = battleStation;
    }

    public boolean hasBattleStation() {
        return battleStation != null;
    }

    public boolean canBuildBattleStation(Fleet fleet, ResearchManager researchManager) {
        if (hasBattleStation()) return false;
        if (!researchManager.isUnlocked("POSTERUNEK_BOJOWY")) return false;
        if (fleet == null) return false;
        if (fleet.countShipType(ShipType.SPACE_FACTORY) == 0) return false;

        if (!hasFullControl(fleet.getOwner())) return false;

        return true;
    }

    public boolean hasFullControl(Enemy owner) {
        boolean hasEnemyPresence = false;

        for (Fleet fleet : fleets) {
            if (fleet.getOwner() != owner) {
                hasEnemyPresence = true;
                break;
            }
        }

        if (hasEnemyPresence) return false;

        if (hasBattleStation() && battleStation.getOwner() != owner) {
            return false;
        }

        return true;
    }

    public int getTotalCreditsBonus() {
        int bonus = 0;
        for (OrbitSlot orbit : orbits) {
            if (orbit.getObject() instanceof AsteroidField asteroid) {
                if (asteroid.hasInstallation()) {
                    bonus += asteroid.getInstallation().getType().getCreditsBonus();
                }
            } else if (orbit.getObject() instanceof GasGiant giant) {
                if (giant.hasInstallation()) {
                    bonus += giant.getInstallation().getType().getCreditsBonus();
                }
            }
        }
        return bonus;
    }

    public int getTotalResearchBonus() {
        int bonus = 0;
        for (OrbitSlot orbit : orbits) {
            if (orbit.getObject() instanceof AsteroidField asteroid) {
                if (asteroid.hasInstallation()) {
                    bonus += asteroid.getInstallation().getType().getResearchBonus();
                }
            }
        }
        return bonus;
    }
}