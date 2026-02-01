package model;

import java.util.ArrayList;
import java.util.List;

public class Fleet {
    private final List<Ship> ships = new ArrayList<>();
    private StarSystem location;
    private StarSystem destination;
    private int turnsToDestination;

    public Fleet(StarSystem location) {
        this.location = location;
        this.destination = null;
        this.turnsToDestination = 0;
    }

    public void addShip(Ship ship) {
        ships.add(ship);
    }

    public void removeShip(Ship ship) {
        ships.remove(ship);
    }

    public List<Ship> getShips() {
        return ships;
    }

    public StarSystem getLocation() {
        return location;
    }

    public StarSystem getDestination() {
        return destination;
    }

    public int getTurnsToDestination() {
        return turnsToDestination;
    }

    public boolean isMoving() {
        return destination != null && turnsToDestination > 0;
    }

    public void setDestination(StarSystem destination, int turns) {
        this.destination = destination;
        this.turnsToDestination = turns;
    }

    public void processTurn() {
        if (isMoving()) {
            turnsToDestination--;
            if (turnsToDestination <= 0) {
                // Przenieś flotę do nowego systemu
                location.removeFleet(this);
                destination.addFleet(this);

                location = destination;
                destination = null;
            }
        }

        // Usuń zniszczone statki
        ships.removeIf(Ship::isDestroyed);
    }

    public boolean isEmpty() {
        return ships.isEmpty();
    }

    public int getTotalAttack(ResearchManager researchManager) {
        return ships.stream()
                .mapToInt(ship -> ship.getAttack(researchManager))
                .sum();
    }

    public int getTotalDefense(ResearchManager researchManager) {
        return ships.stream()
                .mapToInt(ship -> ship.getDefense(researchManager))
                .sum();
    }

    public int getShipCount() {
        return ships.size();
    }

    // Zwraca liczbę statków danego typu
    public int countShipType(ShipType type) {
        return (int) ships.stream()
                .filter(ship -> ship.getType() == type)
                .count();
    }
}