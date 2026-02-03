package controller;

import model.*;

import java.util.*;

public class FogOfWar {

    private final Galaxy galaxy;
    private final Set<StarSystem> visibleSystems = new HashSet<>();

    public FogOfWar(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public void updateVisibility() {
        visibleSystems.clear();

        for (StarSystem system : galaxy.getSystems()) {
            if (hasPlayerColony(system)) {
                visibleSystems.add(system);
                addNeighbors(system, 1);
            }

            if (hasPlayerFleet(system)) {
                visibleSystems.add(system);

                Fleet fleet = system.getPlayerFleet();
                if (hasScout(fleet)) {
                    addNeighbors(system, 2);
                } else {
                    addNeighbors(system, 1);
                }
            }
        }

        for (StarSystem system : galaxy.getSystems()) {
            for (Fleet fleet : system.getFleets()) {
                if (fleet.getOwner() == null && fleet.isMoving()) {
                    StarSystem currentPosition = fleet.getLocation();
                    visibleSystems.add(currentPosition);

                    if (hasScout(fleet)) {
                        addNeighbors(currentPosition, 2);
                    } else {
                        addNeighbors(currentPosition, 1);
                    }
                }
            }
        }
    }

    private boolean hasPlayerColony(StarSystem system) {
        for (OrbitSlot orbit : system.getOrbits()) {
            if (orbit.getObject() instanceof Planet planet) {
                if (planet.isColonized() && planet.getOwner() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasPlayerFleet(StarSystem system) {
        return system.getFleets().stream()
                .anyMatch(f -> f.getOwner() == null);
    }

    private boolean hasScout(Fleet fleet) {
        if (fleet == null) return false;
        if (fleet.getOwner() != null) return false;
        return fleet.countShipType(ShipType.SCOUT) > 0;
    }

    private void addNeighbors(StarSystem center, int depth) {
        if (depth <= 0) return;

        for (StarSystem neighbor : center.getNeighbors()) {
            if (!visibleSystems.contains(neighbor)) {
                visibleSystems.add(neighbor);
                addNeighbors(neighbor, depth - 1);
            }
        }
    }

    public boolean isSystemVisible(StarSystem system) {
        return visibleSystems.contains(system);
    }

    public Set<StarSystem> getVisibleSystems() {
        return new HashSet<>(visibleSystems);
    }
}