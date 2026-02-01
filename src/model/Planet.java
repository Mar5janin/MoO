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
    private final List<BuildOrder> buildQueue = new ArrayList<>();

    // Mapa pamiętająca postęp w budynkach
    private final Map<BuildingType, Integer> savedProgress = new HashMap<>();

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

    // === KOLONIZACJA ===
    public void colonize() {
        if (!habitable || colonized) return;

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

    public List<BuildOrder> getBuildQueue() {
        return buildQueue;
    }


    // === BUDYNKI ===
    public boolean hasBuilding(BuildingType type) {
        return buildings.stream()
                .anyMatch(b -> b.getType() == type);
    }

    public boolean isInQueue(BuildingType type) {
        return buildQueue.stream()
                .anyMatch(o -> o.getType() == type);
    }


    public boolean canBuild(BuildingType type, ResearchManager researchManager) {
        if (!colonized) return false;
        if (type.requiresMoon() && !hasMoon) return false;
        if (!type.isAvailable(researchManager)) return false;
        return true;
    }

    // === KOLEJKA ===
    public void addToQueue(BuildingType type, ResearchManager researchManager) {

        if (!canBuild(type, researchManager)) return;
        if (hasBuilding(type)) return;
        if (isInQueue(type)) return;

        if (buildQueue.size() >= MAX_QUEUE) {
            buildQueue.remove(buildQueue.size() - 1);
        }

        // Przywróć zapisany postęp jeśli istnieje
        int savedCost = savedProgress.getOrDefault(type, type.getCost());
        BuildOrder order = new BuildOrder(type);
        order.setRemainingCost(savedCost);

        buildQueue.add(order);
    }

    public void removeFromQueue(int index) {
        if (index >= 0 && index < buildQueue.size()) {
            BuildOrder order = buildQueue.get(index);
            // Zapisz postęp przed usunięciem
            if (order.getRemainingCost() < order.getType().getCost()) {
                savedProgress.put(order.getType(), order.getRemainingCost());
            }
            buildQueue.remove(index);
        }
    }

    public void moveQueueUp(int index) {
        if (index > 0) {
            var tmp = buildQueue.get(index);
            buildQueue.set(index, buildQueue.get(index - 1));
            buildQueue.set(index - 1, tmp);
        }
    }

    public void moveQueueDown(int index) {
        if (index < buildQueue.size() - 1) {
            var tmp = buildQueue.get(index);
            buildQueue.set(index, buildQueue.get(index + 1));
            buildQueue.set(index + 1, tmp);
        }
    }

    public void processTurn() {

        if (!colonized) return;
        if (buildQueue.isEmpty()) return;

        BuildOrder current = buildQueue.get(0);
        current.progress(getProduction());

        if (current.isFinished()) {
            buildings.add(new Building(current.getType()));
            // Usuń zapisany postęp po ukończeniu
            savedProgress.remove(current.getType());
            buildQueue.remove(0);
        }
    }

}