package controller;

import model.*;

import java.util.*;

public class EnemyController {

    private final Enemy enemy;
    private final Galaxy galaxy;
    private final ResearchManager researchManager;

    private static final double FOOD_RATIO = 0.35;
    private static final double PRODUCTION_RATIO = 0.40;
    private static final double RESEARCH_RATIO = 0.25;

    private static final Map<Technology, Integer> TECH_PRIORITIES = Map.ofEntries(
            Map.entry(Technology.BASIC_WEAPONS, 100),
            Map.entry(Technology.IMPROVED_FARMING, 90),
            Map.entry(Technology.ADVANCED_MINING, 85),
            Map.entry(Technology.IMPROVED_PRODUCTION, 80),
            Map.entry(Technology.ADVANCED_RESEARCH, 75),
            Map.entry(Technology.DESTROYER_TECH, 70),
            Map.entry(Technology.IMPROVED_WEAPONS, 65),
            Map.entry(Technology.IMPROVED_ARMOR, 65),
            Map.entry(Technology.HEAVY_SHIPS, 60),
            Map.entry(Technology.SPACE_CONSTRUCTION, 55),
            Map.entry(Technology.TRADE_NETWORKS, 50),
            Map.entry(Technology.POPULATION_GROWTH, 45),
            Map.entry(Technology.DEFENSIVE_PLATFORMS, 40),
            Map.entry(Technology.CAPITAL_SHIPS, 35),
            Map.entry(Technology.ADVANCED_FLEET_DOCTRINE, 30),
            Map.entry(Technology.URBANIZATION, 25),
            Map.entry(Technology.ADVANCED_ECONOMICS, 20),
            Map.entry(Technology.COLONIAL_ADMINISTRATION, 15)
    );

    public EnemyController(Enemy enemy, Galaxy galaxy) {
        this.enemy = enemy;
        this.galaxy = galaxy;
        this.researchManager = new ResearchManager();
    }

    public ResearchManager getResearchManager() {
        return researchManager;
    }

    public void processTurn() {
        manageResearch();
        managePlanets();
        manageFleets();
    }

    private void manageResearch() {
        if (researchManager.getCurrentResearch() != null) {
            return;
        }

        List<Technology> availableTechs = new ArrayList<>();

        for (Technology tech : Technology.values()) {
            if (researchManager.canResearch(tech)) {
                availableTechs.add(tech);
            }
        }

        if (availableTechs.isEmpty()) {
            return;
        }

        Technology selected = selectBestTechnology(availableTechs);
        if (selected != null) {
            researchManager.setCurrentResearch(selected);
        }
    }

    private Technology selectBestTechnology(List<Technology> available) {
        return available.stream()
                .max(Comparator.comparingInt(tech -> TECH_PRIORITIES.getOrDefault(tech, 0)))
                .orElse(null);
    }

    private void managePlanets() {
        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized() && planet.getOwner() == enemy) {
                        managePlanet(planet, system);
                    }
                }
            }
        }
    }

    private void managePlanet(Planet planet, StarSystem system) {
        assignPopulation(planet);
        manageBuildQueue(planet, system);
    }

    private void assignPopulation(Planet planet) {
        int total = planet.getTotalPopulation();

        if (total == 0) return;

        int onFood = (int) Math.ceil(total * FOOD_RATIO);
        int onProduction = (int) Math.ceil(total * PRODUCTION_RATIO);
        int onResearch = total - onFood - onProduction;

        double netFood = planet.getNetFoodProduction();
        if (netFood < 0) {
            onFood = Math.min(total, onFood + 2);
            onProduction = Math.max(0, total - onFood - onResearch);
        }

        if (planet.getTotalPopulation() >= planet.getMaxPopulation()) {
            onFood = Math.max(1, (int)(total * 0.2));
            int remaining = total - onFood;
            onProduction = (int)(remaining * 0.6);
            onResearch = remaining - onProduction;
        }

        planet.setPopulationOnFood(onFood);
        planet.setPopulationOnProduction(onProduction);
        planet.setPopulationOnResearch(onResearch);
    }

    private void manageBuildQueue(Planet planet, StarSystem system) {
        if (!planet.getBuildQueue().isEmpty()) {
            return;
        }

        int population = planet.getTotalPopulation();
        boolean hasMoon = planet.hasMoon();

        if (population < 3) {
            if (planet.canBuild(BuildingType.FARMA, researchManager)) {
                planet.addBuildingToQueue(BuildingType.FARMA, researchManager);
                return;
            }
        }

        int farmCount = planet.countBuilding(BuildingType.FARMA);
        int factoryCount = planet.countBuilding(BuildingType.FABRYKA);
        int labCount = planet.countBuilding(BuildingType.LABORATORIUM);

        if (farmCount == 0 && planet.canBuild(BuildingType.FARMA, researchManager)) {
            planet.addBuildingToQueue(BuildingType.FARMA, researchManager);
            return;
        }

        if (factoryCount == 0 && planet.canBuild(BuildingType.FABRYKA, researchManager)) {
            planet.addBuildingToQueue(BuildingType.FABRYKA, researchManager);
            return;
        }

        if (labCount == 0 && planet.canBuild(BuildingType.LABORATORIUM, researchManager)) {
            planet.addBuildingToQueue(BuildingType.LABORATORIUM, researchManager);
            return;
        }

        Fleet fleet = system.getFleets().stream()
                .filter(f -> f.getOwner() == enemy)
                .findFirst()
                .orElse(null);

        int scoutCount = fleet != null ? fleet.countShipType(ShipType.SCOUT) : 0;

        if (scoutCount < 4 && planet.canBuildShip(ShipType.SCOUT, researchManager)) {
            planet.addShipToQueue(ShipType.SCOUT, researchManager);
            return;
        }

        if (planet.canBuildShip(ShipType.COLONY_SHIP, researchManager) &&
                planet.getTotalPopulation() >= 3) {

            long colonyShipsInQueue = planet.getBuildQueue().stream()
                    .filter(o -> o.getProductionType() == ProductionType.SHIP)
                    .filter(o -> o.getShipType() == ShipType.COLONY_SHIP)
                    .count();

            if (colonyShipsInQueue == 0) {
                planet.addShipToQueue(ShipType.COLONY_SHIP, researchManager);
                return;
            }
        }

        if (planet.canBuildShip(ShipType.FIGHTER, researchManager)) {
            planet.addShipToQueue(ShipType.FIGHTER, researchManager);
            return;
        }

        if (planet.canBuild(BuildingType.ZAAWANSOWANA_FARMA, researchManager)) {
            planet.addBuildingToQueue(BuildingType.ZAAWANSOWANA_FARMA, researchManager);
            return;
        }

        if (planet.canBuild(BuildingType.CENTRUM_BADAWCZE, researchManager)) {
            planet.addBuildingToQueue(BuildingType.CENTRUM_BADAWCZE, researchManager);
            return;
        }

        if (planet.canBuild(BuildingType.FABRYKA_KOSMICZNA, researchManager)) {
            planet.addBuildingToQueue(BuildingType.FABRYKA_KOSMICZNA, researchManager);
            return;
        }

        if (planet.canBuild(BuildingType.OSIEDLE, researchManager)) {
            planet.addBuildingToQueue(BuildingType.OSIEDLE, researchManager);
        }
    }

    private void manageFleets() {
        for (StarSystem system : galaxy.getSystems()) {
            Fleet aiFleet = system.getFleets().stream()
                    .filter(f -> f.getOwner() == enemy)
                    .findFirst()
                    .orElse(null);

            if (aiFleet != null && !aiFleet.isMoving()) {
                manageFleet(aiFleet, system);
            }
        }
    }

    private void manageFleet(Fleet fleet, StarSystem currentSystem) {
        int colonyShips = fleet.countShipType(ShipType.COLONY_SHIP);

        if (colonyShips > 0) {
            StarSystem targetColony = findBestColonizationTarget(currentSystem);
            if (targetColony != null) {
                fleet.setDestination(targetColony);
                return;
            }
        }

        int scouts = fleet.countShipType(ShipType.SCOUT);
        if (scouts > 0 && fleet.getShipCount() <= 3) {
            StarSystem unexplored = findUnexploredSystem(currentSystem);
            if (unexplored != null) {
                fleet.setDestination(unexplored);
                return;
            }
        }
    }

    private StarSystem findBestColonizationTarget(StarSystem from) {
        List<StarSystem> candidates = new ArrayList<>();

        for (StarSystem system : galaxy.getSystems()) {
            if (system == from) continue;

            boolean hasHabitablePlanet = false;
            boolean alreadyColonized = false;

            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isHabitable()) {
                        hasHabitablePlanet = true;
                    }
                    if (planet.isColonized()) {
                        alreadyColonized = true;
                        break;
                    }
                }
            }

            if (hasHabitablePlanet && !alreadyColonized) {
                candidates.add(system);
            }
        }

        if (candidates.isEmpty()) return null;

        return candidates.stream()
                .min(Comparator.comparingDouble(from::distanceTo))
                .orElse(null);
    }

    private StarSystem findUnexploredSystem(StarSystem from) {
        List<StarSystem> neighbors = new ArrayList<>(from.getNeighbors());

        for (StarSystem neighbor : neighbors) {
            boolean hasAIPresence = false;

            for (OrbitSlot orbit : neighbor.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized() && planet.getOwner() == enemy) {
                        hasAIPresence = true;
                        break;
                    }
                }
            }

            boolean hasAIFleet = neighbor.getFleets().stream()
                    .anyMatch(f -> f.getOwner() == enemy);

            if (!hasAIPresence && !hasAIFleet) {
                return neighbor;
            }
        }

        return null;
    }

    public void colonizePlanet(Fleet fleet, StarSystem system) {
        if (fleet.countShipType(ShipType.COLONY_SHIP) == 0) return;

        for (OrbitSlot orbit : system.getOrbits()) {
            if (orbit.getObject() instanceof Planet planet) {
                if (planet.canColonize(fleet)) {
                    planet.colonize(fleet);
                    return;
                }
            }
        }
    }
}