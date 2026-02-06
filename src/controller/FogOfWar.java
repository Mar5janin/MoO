package controller;

import model.galaxy.Galaxy;
import model.galaxy.StarSystem;
import model.orbits.OrbitSlot;
import model.orbits.planets.Planet;
import model.ships.Fleet;
import model.ships.ShipType;

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

        Set<StarSystem> toVisit = new HashSet<>();
        toVisit.add(center);

        for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
            Set<StarSystem> nextLevel = new HashSet<>();

            for (StarSystem current : toVisit) {
                for (StarSystem neighbor : current.getNeighbors()) {
                    visibleSystems.add(neighbor);
                    nextLevel.add(neighbor);
                }
            }

            toVisit = nextLevel;
        }
    }

    public boolean isSystemVisible(StarSystem system) {
        return visibleSystems.contains(system);
    }

    public Set<StarSystem> getVisibleSystems() {
        return new HashSet<>(visibleSystems);
    }
}