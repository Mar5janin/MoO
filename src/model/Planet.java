package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Planet implements OrbitObject {

    private final PlanetType type;
    private final boolean habitable;
    private boolean colonized;
    private boolean hasMoon;

    private final List<Building> buildings = new ArrayList<>();

    private static final int MAX_QUEUE = 5;
    private final List<ProductionOrder> productionQueue = new ArrayList<>();

    // Mapy pamiętające postęp
    private final Map<BuildingType, Integer> savedBuildingProgress = new HashMap<>();
    private final Map<ShipType, Integer> savedShipProgress = new HashMap<>();

    private int basePopulation;
    private int baseProduction;
    private int baseResearch;
    private int baseCredits;

    public Planet(PlanetType type) {
        this.type = type;
        this.habitable = type.isHabitable();
        this.colonized = false;
        this.hasMoon = habitable && Math.random() < 0.55;
    }

    @Override
    public OrbitType getType() {
        return OrbitType.PLANET;
    }

    public PlanetType getPlanetType() {
        return type;
    }

    public boolean isHabitable() {
        return habitable;
    }

    public boolean isColonized() {
        return colonized;
    }

    public boolean hasMoon() {
        return hasMoon;
    }

    public void setMoon(Planet planet){
        planet.hasMoon = true;
    }

    public void colonizeHomePlanet(){
        colonized = true;
        basePopulation = 10;
        baseProduction = 5;
        baseResearch = 3;
        baseCredits = 4;
    }

    // === KOLONIZACJA ===
    public boolean canColonize(Fleet fleet) {
        if (!habitable || colonized) return false;
        if (fleet == null) return false;

        // Sprawdź czy flota ma statek kolonizacyjny
        return fleet.countShipType(ShipType.COLONY_SHIP) > 0;
    }

    public void colonize(Fleet fleet) {
        if (!canColonize(fleet)) return;

        // Usuń statek kolonizacyjny z floty
        Ship colonyShip = fleet.getShips().stream()
                .filter(s -> s.getType() == ShipType.COLONY_SHIP)
                .findFirst()
                .orElse(null);

        if (colonyShip != null) {
            fleet.removeShip(colonyShip);
        }

        colonized = true;
        basePopulation = 1;
        baseProduction = 5;
        baseResearch = 3;
        baseCredits = 4;
    }

    // === GETTERY ===
    public int getProduction() {
        int value = baseProduction;
        for (Building b : buildings) {
            value += b.getType().getProductionBonus();
        }
        return value;
    }

    public int getResearch() {
        int value = baseResearch;
        for (Building b : buildings) {
            value += b.getType().getResearchBonus();
        }
        return value;
    }

    public int getPopulation() {
        int value = basePopulation;
        for (Building b : buildings) {
            value += b.getType().getPopulationBonus();
        }
        return value;
    }

    public int getCredits() {
        int value = baseCredits;
        for (Building b : buildings) {
            value += b.getType().getCreditsBonus();
        }
        return value;
    }


    public List<Building> getBuildings() {
        return buildings;
    }

    public List<ProductionOrder> getBuildQueue() {
        return productionQueue;
    }


    // === BUDYNKI ===
    public boolean hasBuilding(BuildingType type) {
        return buildings.stream()
                .anyMatch(b -> b.getType() == type);
    }

    public boolean isBuildingInQueue(BuildingType type) {
        return productionQueue.stream()
                .filter(o -> o.getProductionType() == ProductionType.BUILDING)
                .anyMatch(o -> o.getBuildingType() == type);
    }

    public boolean isShipInQueue(ShipType type) {
        return productionQueue.stream()
                .filter(o -> o.getProductionType() == ProductionType.SHIP)
                .anyMatch(o -> o.getShipType() == type);
    }


    public boolean canBuild(BuildingType type, ResearchManager researchManager) {
        if (!colonized) return false;
        if (type.requiresMoon() && !hasMoon) return false;
        if (!type.isAvailable(researchManager)) return false;
        return true;
    }

    public boolean canBuildShip(ShipType type, ResearchManager researchManager) {
        if (!colonized) return false;
        if (!type.isAvailable(researchManager)) return false;
        return true;
    }

    // === KOLEJKA PRODUKCJI ===
    public void addBuildingToQueue(BuildingType type, ResearchManager researchManager) {
        if (!canBuild(type, researchManager)) return;
        if (hasBuilding(type)) return;
        if (isBuildingInQueue(type)) return;

        if (productionQueue.size() >= MAX_QUEUE) {
            productionQueue.remove(productionQueue.size() - 1);
        }

        // Przywróć zapisany postęp jeśli istnieje
        int savedCost = savedBuildingProgress.getOrDefault(type, type.getCost());
        ProductionOrder order = new ProductionOrder(type);
        order.setRemainingCost(savedCost);

        productionQueue.add(order);
    }

    public void addShipToQueue(ShipType type, ResearchManager researchManager) {
        if (!canBuildShip(type, researchManager)) return;

        if (productionQueue.size() >= MAX_QUEUE) {
            productionQueue.remove(productionQueue.size() - 1);
        }

        // Przywróć zapisany postęp jeśli istnieje
        int savedCost = savedShipProgress.getOrDefault(type, type.getCost());
        ProductionOrder order = new ProductionOrder(type);
        order.setRemainingCost(savedCost);

        productionQueue.add(order);
    }

    public void removeFromQueue(int index) {
        if (index >= 0 && index < productionQueue.size()) {
            ProductionOrder order = productionQueue.get(index);

            // Zapisz postęp przed usunięciem
            if (order.getRemainingCost() < order.getOriginalCost()) {
                if (order.getProductionType() == ProductionType.BUILDING) {
                    savedBuildingProgress.put(order.getBuildingType(), order.getRemainingCost());
                } else {
                    savedShipProgress.put(order.getShipType(), order.getRemainingCost());
                }
            }

            productionQueue.remove(index);
        }
    }

    public void moveQueueUp(int index) {
        if (index > 0) {
            var tmp = productionQueue.get(index);
            productionQueue.set(index, productionQueue.get(index - 1));
            productionQueue.set(index - 1, tmp);
        }
    }

    public void moveQueueDown(int index) {
        if (index < productionQueue.size() - 1) {
            var tmp = productionQueue.get(index);
            productionQueue.set(index, productionQueue.get(index + 1));
            productionQueue.set(index + 1, tmp);
        }
    }

    public Ship processTurn(StarSystem system) {
        if (!colonized) return null;
        if (productionQueue.isEmpty()) return null;

        ProductionOrder current = productionQueue.get(0);
        current.progress(getProduction());

        if (current.isFinished()) {
            productionQueue.remove(0);

            if (current.getProductionType() == ProductionType.BUILDING) {
                buildings.add(new Building(current.getBuildingType()));
                savedBuildingProgress.remove(current.getBuildingType());
                return null;
            } else {
                Ship ship = new Ship(current.getShipType());
                savedShipProgress.remove(current.getShipType());
                return ship; // Zwróć statek do dodania do floty
            }
        }

        return null;
    }

}