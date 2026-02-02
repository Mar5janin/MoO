package model;

import controller.Pathfinder;

import java.util.ArrayList;
import java.util.List;

public class Fleet {
    private final List<Ship> ships = new ArrayList<>();
    private StarSystem location;
    private Enemy owner;
    private List<StarSystem> route = null;
    private int currentRouteIndex = 0;
    private int turnsToNextSystem = 0;
    private InstallationOrder currentProject = null;

    public Fleet(StarSystem location) {
        this(location, null);
    }

    public Fleet(StarSystem location, Enemy owner) {
        this.location = location;
        this.owner = owner;
    }

    public Enemy getOwner() {
        return owner;
    }

    public void setOwner(Enemy owner) {
        this.owner = owner;
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
        return route.get(route.size() - 1);
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
        int remaining = turnsToNextSystem;
        if (currentRouteIndex < route.size()) {
            remaining += (route.size() - currentRouteIndex - 1);
        }
        return remaining;
    }

    public boolean isMoving() {
        return route != null && currentRouteIndex < route.size();
    }

    public boolean setDestination(StarSystem destination) {
        if (destination == null || destination == location) {
            this.route = null;
            this.currentRouteIndex = 0;
            this.turnsToNextSystem = 0;
            return false;
        }

        List<StarSystem> path = Pathfinder.findPath(location, destination);

        if (path == null || path.size() <= 1) {
            return false;
        }

        this.route = new ArrayList<>(path.subList(1, path.size()));
        this.currentRouteIndex = 0;
        this.turnsToNextSystem = 1;

        return true;
    }

    public InstallationOrder getCurrentProject() {
        return currentProject;
    }

    public boolean canStartProject(SpaceInstallationType type, OrbitObject target) {
        if (currentProject != null) return false;
        if (isMoving()) return false;

        int factoryCount = countShipType(ShipType.SPACE_FACTORY);
        if (factoryCount == 0) return false;

        if (type == SpaceInstallationType.BATTLE_STATION) {
            return !location.hasBattleStation();
        }

        if (target instanceof AsteroidField asteroid) {
            return !asteroid.hasInstallation();
        } else if (target instanceof GasGiant giant) {
            return !giant.hasInstallation() && type == SpaceInstallationType.GAS_MINE;
        }

        return false;
    }

    public void startProject(SpaceInstallationType type, OrbitObject target) {
        if (!canStartProject(type, target)) return;
        currentProject = new InstallationOrder(type, target);
    }

    public void cancelProject() {
        currentProject = null;
    }

    public void processTurn() {
        if (isMoving()) {
            turnsToNextSystem--;

            if (turnsToNextSystem <= 0) {
                StarSystem nextSystem = route.get(currentRouteIndex);

                location.removeFleet(this);
                nextSystem.addFleet(this);
                location = nextSystem;

                currentRouteIndex++;

                if (currentRouteIndex >= route.size()) {
                    route = null;
                    currentRouteIndex = 0;
                    turnsToNextSystem = 0;
                } else {
                    turnsToNextSystem = 1;
                }
            }
        }

        if (currentProject != null && !isMoving()) {
            int production = 0;
            for (Ship ship : ships) {
                if (ship.getType() == ShipType.SPACE_FACTORY) {
                    production += ship.getType().getProductionPerTurn();
                }
            }

            currentProject.progress(production);

            if (currentProject.isFinished()) {
                SpaceInstallationType type = currentProject.getType();
                OrbitObject target = currentProject.getTarget();
                SpaceInstallation installation = new SpaceInstallation(type);

                if (type == SpaceInstallationType.BATTLE_STATION) {
                    location.setBattleStation(installation);
                } else if (target instanceof AsteroidField asteroid) {
                    asteroid.setInstallation(installation);
                } else if (target instanceof GasGiant giant) {
                    giant.setInstallation(installation);
                }

                currentProject = null;
            }
        }

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

    public int countShipType(ShipType type) {
        return (int) ships.stream()
                .filter(ship -> ship.getType() == type)
                .count();
    }

    public List<StarSystem> getRoute() {
        return route;
    }
}