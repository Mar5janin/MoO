package model;

import java.util.ArrayList;
import java.util.List;

public class Fleet {
    private final List<Ship> ships = new ArrayList<>();
    private StarSystem location;
    private List<StarSystem> route = null; // Pełna trasa podróży
    private int currentRouteIndex = 0; // Indeks aktualnego celu w trasie
    private int turnsToNextSystem = 0; // Tury do następnego systemu w trasie

    public Fleet(StarSystem location) {
        this.location = location;
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
        if (route == null || route.isEmpty()) {
            return null;
        }
        return route.get(route.size() - 1); // Końcowy cel
    }

    public StarSystem getNextSystem() {
        if (route == null || currentRouteIndex >= route.size()) {
            return null;
        }
        return route.get(currentRouteIndex);
    }

    public int getTurnsToDestination() {
        if (route == null) {
            return 0;
        }
        // Tury do następnego systemu + pozostałe systemy w trasie
        int remaining = turnsToNextSystem;
        if (currentRouteIndex < route.size()) {
            remaining += (route.size() - currentRouteIndex - 1);
        }
        return remaining;
    }

    public boolean isMoving() {
        return route != null && currentRouteIndex < route.size();
    }

    /**
     * Ustawia trasę do celu. Używa pathfindingu do znalezienia najkrótszej drogi.
     */
    public boolean setDestination(StarSystem destination) {
        if (destination == null || destination == location) {
            this.route = null;
            this.currentRouteIndex = 0;
            this.turnsToNextSystem = 0;
            return false;
        }

        List<StarSystem> path = Pathfinder.findPath(location, destination);

        if (path == null || path.size() <= 1) {
            // Brak ścieżki
            return false;
        }

        // Usuń pierwszy element (obecna lokalizacja)
        this.route = new ArrayList<>(path.subList(1, path.size()));
        this.currentRouteIndex = 0;
        this.turnsToNextSystem = 1; // Na razie zawsze 1 tura między sąsiednimi systemami

        return true;
    }

    public void processTurn() {
        if (isMoving()) {
            turnsToNextSystem--;

            if (turnsToNextSystem <= 0) {
                // Dotarliśmy do następnego systemu w trasie
                StarSystem nextSystem = route.get(currentRouteIndex);

                location.removeFleet(this);
                nextSystem.addFleet(this);
                location = nextSystem;

                currentRouteIndex++;

                // Sprawdź czy dotarliśmy do końca trasy
                if (currentRouteIndex >= route.size()) {
                    // Koniec podróży
                    route = null;
                    currentRouteIndex = 0;
                    turnsToNextSystem = 0;
                } else {
                    // Przygotuj się do następnego skoku
                    turnsToNextSystem = 1;
                }
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

    /**
     * Zwraca pełną trasę podróży (dla wyświetlenia)
     */
    public List<StarSystem> getRoute() {
        return route;
    }
}