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

    private PlanetAttribute attribute;
    private PlanetSize size;
    private PlanetRichness richness;

    private final List<Building> buildings = new ArrayList<>();

    private static final int MAX_QUEUE = 5;
    private final List<ProductionOrder> productionQueue = new ArrayList<>();

    private final Map<BuildingType, Integer> savedBuildingProgress = new HashMap<>();
    private final Map<ShipType, Integer> savedShipProgress = new HashMap<>();

    private int totalPopulation = 0;
    private int maxPopulation = 10;

    private int populationOnFood = 0;
    private int populationOnProduction = 0;
    private int populationOnResearch = 0;

    private double foodAccumulated = 0;

    private static final double FOOD_UPKEEP_PER_CITIZEN = 0.4;

    public static final int CREDITS_PER_PRODUCTION = 2;

    public Planet(PlanetType type) {
        this.type = type;
        this.habitable = type.isHabitable();
        this.colonized = false;
        this.hasMoon = habitable && Math.random() < 0.55;

        this.size = randomSize();
        this.richness = randomRichness();
        this.attribute = randomAttribute();
    }

    private PlanetSize randomSize() {
        double r = Math.random();
        if (r < 0.15) return PlanetSize.SMALL;
        if (r < 0.60) return PlanetSize.MEDIUM;
        if (r < 0.90) return PlanetSize.LARGE;
        return PlanetSize.HUGE;
    }

    private PlanetRichness randomRichness() {
        double r = Math.random();
        if (r < 0.20) return PlanetRichness.POOR;
        if (r < 0.70) return PlanetRichness.NORMAL;
        if (r < 0.95) return PlanetRichness.RICH;
        return PlanetRichness.ULTRA_RICH;
    }

    private PlanetAttribute randomAttribute() {
        double r = Math.random();
        if (r < 0.60) return PlanetAttribute.NONE;
        if (r < 0.75) return PlanetAttribute.GOLD_DEPOSITS;
        if (r < 0.90) return PlanetAttribute.ANCIENT_ARTIFACTS;
        return PlanetAttribute.FERTILE_ALGAE;
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

    public PlanetAttribute getAttribute() {
        return attribute;
    }

    public PlanetSize getSize() {
        return size;
    }

    public PlanetRichness getRichness() {
        return richness;
    }

    public void colonizeHomePlanet(){
        colonized = true;
        totalPopulation = 5;
        maxPopulation = calculateMaxPopulation();
        populationOnFood = 2;
        populationOnProduction = 2;
        populationOnResearch = 1;
    }

    private int calculateMaxPopulation() {
        int base = switch (type) {
            case TERRAN, OCEAN -> 12;
            case DESERT, TUNDRA -> 10;
            case BARREN, TOXIC, RADIATED -> 6;
            case VOLCANIC -> 5;
            case ICE -> 4;
        };
        return base + size.getPopulationBonus();
    }

    public boolean canColonize(Fleet fleet) {
        if (!habitable || colonized) return false;
        if (fleet == null) return false;

        return fleet.countShipType(ShipType.COLONY_SHIP) > 0;
    }

    public void colonize(Fleet fleet) {
        if (!canColonize(fleet)) return;

        Ship colonyShip = fleet.getShips().stream()
                .filter(s -> s.getType() == ShipType.COLONY_SHIP)
                .findFirst()
                .orElse(null);

        if (colonyShip != null) {
            fleet.removeShip(colonyShip);
        }

        colonized = true;
        totalPopulation = 1;
        maxPopulation = calculateMaxPopulation();

        populationOnFood = 1;
        populationOnProduction = 0;
        populationOnResearch = 0;
    }

    public int getTotalPopulation() {
        return totalPopulation;
    }

    public int getMaxPopulation() {
        int max = maxPopulation;
        for (Building b : buildings) {
            max += b.getType().getPopulationCapacityBonus();
        }
        return max;
    }

    public int getUnassignedPopulation() {
        return totalPopulation - populationOnFood - populationOnProduction - populationOnResearch;
    }

    public int getPopulationOnFood() {
        return populationOnFood;
    }

    public int getPopulationOnProduction() {
        return populationOnProduction;
    }

    public int getPopulationOnResearch() {
        return populationOnResearch;
    }

    public void setPopulationOnFood(int value) {
        populationOnFood = Math.max(0, Math.min(value, totalPopulation));
        rebalancePopulation();
    }

    public void setPopulationOnProduction(int value) {
        populationOnProduction = Math.max(0, Math.min(value, totalPopulation));
        rebalancePopulation();
    }

    public void setPopulationOnResearch(int value) {
        populationOnResearch = Math.max(0, Math.min(value, totalPopulation));
        rebalancePopulation();
    }

    private void rebalancePopulation() {
        int sum = populationOnFood + populationOnProduction + populationOnResearch;
        if (sum > totalPopulation) {
            double ratio = (double) totalPopulation / sum;
            populationOnFood = (int) (populationOnFood * ratio);
            populationOnProduction = (int) (populationOnProduction * ratio);
            populationOnResearch = (int) (populationOnResearch * ratio);
        }
    }

    public int getFoodProduction() {
        int baseProduction = populationOnFood;
        int passive = 0;
        int perCapita = 0;

        for (Building b : buildings) {
            passive += b.getType().getFoodBonus();
            perCapita += b.getType().getFoodPerCapita();
        }

        if (attribute != null) {
            passive += attribute.getFoodBonus();
        }

        return baseProduction + passive + (populationOnFood * perCapita);
    }

    public double getNetFoodProduction() {
        double production = getFoodProduction();
        double upkeep = totalPopulation * FOOD_UPKEEP_PER_CITIZEN;
        return production - upkeep;
    }

    public int getProduction() {
        int baseProduction = populationOnProduction;
        int passive = 0;
        int perCapita = 0;

        for (Building b : buildings) {
            passive += b.getType().getProductionBonus();
            perCapita += b.getType().getProductionPerCapita();
        }

        if (richness != null) {
            passive += richness.getProductionBonus();
        }

        return baseProduction + passive + (populationOnProduction * perCapita);
    }

    public int getResearch() {
        int baseProduction = populationOnResearch;
        int passive = 0;
        int perCapita = 0;

        for (Building b : buildings) {
            passive += b.getType().getResearchBonus();
            perCapita += b.getType().getResearchPerCapita();
        }

        if (attribute != null) {
            passive += attribute.getResearchBonus();
        }

        return baseProduction + passive + (populationOnResearch * perCapita);
    }

    public int getCredits() {
        int passive = 0;
        int perTotalPopulation = 1;

        for (Building b : buildings) {
            passive += b.getType().getCreditsBonus();
            perTotalPopulation += b.getType().getCreditsPerTotalPopulation();
        }

        if (attribute != null) {
            passive += attribute.getCreditsBonus();
        }

        return passive + (totalPopulation * perTotalPopulation);
    }

    @Deprecated
    public int getPopulation() {
        return getTotalPopulation();
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public List<ProductionOrder> getBuildQueue() {
        return productionQueue;
    }

    public boolean hasBuilding(BuildingType type) {
        return buildings.stream()
                .anyMatch(b -> b.getType() == type);
    }

    public int countBuilding(BuildingType type) {
        return (int) buildings.stream()
                .filter(b -> b.getType() == type)
                .count();
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

        if (!type.canBuildMultiple()) {
            if (hasBuilding(type) || isBuildingInQueue(type)) {
                return false;
            }
        } else {
            int currentCount = countBuilding(type);
            int inQueue = (int) productionQueue.stream()
                    .filter(o -> o.getProductionType() == ProductionType.BUILDING)
                    .filter(o -> o.getBuildingType() == type)
                    .count();

            if (currentCount + inQueue >= type.getMaxCount()) {
                return false;
            }
        }

        return true;
    }

    public boolean canBuildShip(ShipType type, ResearchManager researchManager) {
        if (!colonized) return false;
        if (!type.isAvailable(researchManager)) return false;
        return true;
    }

    public void addBuildingToQueue(BuildingType type, ResearchManager researchManager) {
        if (!canBuild(type, researchManager)) return;

        if (productionQueue.size() >= MAX_QUEUE) {
            productionQueue.remove(productionQueue.size() - 1);
        }

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

        int savedCost = savedShipProgress.getOrDefault(type, type.getCost());
        ProductionOrder order = new ProductionOrder(type);
        order.setRemainingCost(savedCost);

        productionQueue.add(order);
    }

    public void removeFromQueue(int index) {
        if (index >= 0 && index < productionQueue.size()) {
            ProductionOrder order = productionQueue.get(index);

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

    public boolean rushBuy(Game game) {
        if (productionQueue.isEmpty()) return false;

        ProductionOrder current = productionQueue.get(0);
        int remainingCost = current.getRemainingCost();
        int creditsNeeded = remainingCost * CREDITS_PER_PRODUCTION;

        if (game.getTotalCredits() < creditsNeeded) {
            return false;
        }

        game.spendCredits(creditsNeeded);

        productionQueue.remove(0);

        if (current.getProductionType() == ProductionType.BUILDING) {
            buildings.add(new Building(current.getBuildingType()));
            savedBuildingProgress.remove(current.getBuildingType());
        } else {
            savedShipProgress.remove(current.getShipType());

            if (current.getShipType() == ShipType.COLONY_SHIP && totalPopulation > 1) {
                totalPopulation--;
                rebalancePopulation();
            }
        }

        return true;
    }

    public int getRushBuyCost() {
        if (productionQueue.isEmpty()) return 0;
        return productionQueue.get(0).getRemainingCost() * CREDITS_PER_PRODUCTION;
    }

    public Ship processTurn(StarSystem system) {
        if (!colonized) return null;

        double netFoodProduction = getNetFoodProduction();

        if (netFoodProduction < 0) {
            foodAccumulated += netFoodProduction;

            if (foodAccumulated <= -10 && totalPopulation > 1) {
                int deaths = (int)(Math.abs(foodAccumulated) / 10);
                deaths = Math.min(deaths, totalPopulation - 1);
                totalPopulation -= deaths;
                foodAccumulated += (deaths * 10);
                rebalancePopulation();
            }
        } else {
            foodAccumulated += netFoodProduction;

            double foodNeeded = getFoodNeededForGrowth();

            if (foodAccumulated >= foodNeeded && totalPopulation < getMaxPopulation()) {
                foodAccumulated -= foodNeeded;
                totalPopulation++;
            } else if (totalPopulation >= getMaxPopulation()) {
                if (foodAccumulated > foodNeeded) {
                    foodAccumulated = foodNeeded;
                }
            }
        }

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

                if (current.getShipType() == ShipType.COLONY_SHIP && totalPopulation > 1) {
                    totalPopulation--;
                    rebalancePopulation();
                }

                return ship;
            }
        }

        return null;
    }

    public double getFoodAccumulated() {
        return foodAccumulated;
    }

    public double getFoodNeededForGrowth() {
        int pop = totalPopulation;

        if (pop <= 0) return 10;

        if (pop <= 5) {
            return 10 + (pop * 2);
        } else if (pop <= 10) {
            return 25 + ((pop - 5) * 2);
        } else if (pop <= 20) {
            return 40 + ((pop - 10) * 2);
        } else {
            return 70 + ((pop - 20) * 2);
        }
    }

    public boolean isPopulationFullyAssigned() {
        return getUnassignedPopulation() == 0;
    }
}