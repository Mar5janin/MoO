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

    public Planet getColonizedPlanet() {
        for (OrbitSlot orbit : orbits) {
            if (orbit.getObject() instanceof Planet planet && planet.isColonized()) {
                return planet;
            }
        }
        return null;
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

    // === FLOTY ===
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

    /**
     * Zwraca pierwszą flotę gracza w systemie
     * UWAGA: Może być wiele osobnych flot!
     */
    public Fleet getPlayerFleet() {
        return fleets.isEmpty() ? null : fleets.get(0);
    }

    /**
     * Zwraca istniejącą flotę lub tworzy nową
     * UWAGA: Ta metoda jest używana tylko przy tworzeniu statków na planecie
     */
    public Fleet getOrCreatePlayerFleet() {
        Fleet fleet = getPlayerFleet();
        if (fleet == null) {
            fleet = new Fleet(this);
            fleets.add(fleet);
        }
        return fleet;
    }
}