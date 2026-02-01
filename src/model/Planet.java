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

    // === SYSTEM POPULACJI ===
    private int totalPopulation = 0;
    private int maxPopulation = 10; // Zależy od typu planety i budynków

    // Przypisanie populacji do różnych zadań
    private int populationOnFood = 0;
    private int populationOnProduction = 0;
    private int populationOnResearch = 0;

    // Akumulacja żywności
    private int foodAccumulated = 0;
    private static final int BASE_FOOD_PER_CITIZEN = 10; // Bazowa ilość żywności na pierwszą osobę

    // Przelicznik kredytów na produkcję (dla rush buy)
    public static final int CREDITS_PER_PRODUCTION = 2;

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
        totalPopulation = 5;
        maxPopulation = calculateMaxPopulation();
        // Początkowe przypisanie
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
        return base;
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
        totalPopulation = 1;
        maxPopulation = calculateMaxPopulation();

        // Początkowe przypisanie - 1 osoba początkowo na produkcji żywności
        populationOnFood = 1;
        populationOnProduction = 0;
        populationOnResearch = 0;
    }

    // === ZARZĄDZANIE POPULACJĄ ===
    public int getTotalPopulation() {
        return totalPopulation;
    }

    public int getMaxPopulation() {
        // Bazowa + bonusy z budynków
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

    // Upewnij się że suma nie przekracza total
    private void rebalancePopulation() {
        int sum = populationOnFood + populationOnProduction + populationOnResearch;
        if (sum > totalPopulation) {
            // Proporcjonalna redukcja
            double ratio = (double) totalPopulation / sum;
            populationOnFood = (int) (populationOnFood * ratio);
            populationOnProduction = (int) (populationOnProduction * ratio);
            populationOnResearch = (int) (populationOnResearch * ratio);
        }
    }

    // === GETTERY ZASOBÓW (uwzględniają populację i budynki) ===

    /**
     * Produkcja żywności = (populacja × 1) + pasywna + (populacja × bonus per capita)
     */
    public int getFoodProduction() {
        int baseProduction = populationOnFood; // Każda osoba produkuje 1 punkt bazowo
        int passive = 0;
        int perCapita = 0;

        for (Building b : buildings) {
            passive += b.getType().getFoodBonus();
            perCapita += b.getType().getFoodPerCapita();
        }

        return baseProduction + passive + (populationOnFood * perCapita);
    }

    /**
     * Produkcja = (populacja × 1) + pasywna + (populacja × bonus per capita)
     */
    public int getProduction() {
        int baseProduction = populationOnProduction; // Każda osoba produkuje 1 punkt bazowo
        int passive = 0;
        int perCapita = 0;

        for (Building b : buildings) {
            passive += b.getType().getProductionBonus();
            perCapita += b.getType().getProductionPerCapita();
        }

        return baseProduction + passive + (populationOnProduction * perCapita);
    }

    /**
     * Badania = (populacja × 1) + pasywna + (populacja × bonus per capita)
     */
    public int getResearch() {
        int baseProduction = populationOnResearch; // Każda osoba produkuje 1 punkt bazowo
        int passive = 0;
        int perCapita = 0;

        for (Building b : buildings) {
            passive += b.getType().getResearchBonus();
            perCapita += b.getType().getResearchPerCapita();
        }

        return baseProduction + passive + (populationOnResearch * perCapita);
    }

    /**
     * Kredyty = pasywna + (całkowita populacja × (1 + podatki per capita))
     * ZMIANA: Teraz wszystkie osoby płacą podatki, nie tylko bezrobotni
     */
    public int getCredits() {
        int passive = 0;
        int perTotalPopulation = 1; // Bazowy podatek od każdej osoby

        for (Building b : buildings) {
            passive += b.getType().getCreditsBonus();
            perTotalPopulation += b.getType().getCreditsPerTotalPopulation();
        }

        // Wszystkie osoby płacą podatki
        return passive + (totalPopulation * perTotalPopulation);
    }

    // Stara metoda dla kompatybilności
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

    /**
     * Rush buy - natychmiast kończy obecną produkcję za kredyty
     * @param game Obiekt gry (potrzebny do pobrania/odjęcia kredytów)
     * @return true jeśli się udało
     */
    public boolean rushBuy(Game game) {
        if (productionQueue.isEmpty()) return false;

        ProductionOrder current = productionQueue.get(0);
        int remainingCost = current.getRemainingCost();
        int creditsNeeded = remainingCost * CREDITS_PER_PRODUCTION;

        if (game.getTotalCredits() < creditsNeeded) {
            return false; // Za mało kredytów
        }

        // Odejmij kredyty
        game.spendCredits(creditsNeeded);

        // Zakończ produkcję natychmiast
        productionQueue.remove(0);

        if (current.getProductionType() == ProductionType.BUILDING) {
            buildings.add(new Building(current.getBuildingType()));
            savedBuildingProgress.remove(current.getBuildingType());
        } else {
            // Statek będzie dodany przez Game.rushBuyShip()
            savedShipProgress.remove(current.getShipType());
        }

        return true;
    }

    /**
     * Zwraca koszt rush buy dla obecnego elementu w kolejce
     */
    public int getRushBuyCost() {
        if (productionQueue.isEmpty()) return 0;
        return productionQueue.get(0).getRemainingCost() * CREDITS_PER_PRODUCTION;
    }

    /**
     * Przetwarza turę - produkcja, żywność, wzrost populacji
     * @return Nowo wyprodukowany statek (jeśli jakiś)
     */
    public Ship processTurn(StarSystem system) {
        if (!colonized) return null;

        // 1. Produkcja żywności i wzrost populacji
        int foodThisTurn = getFoodProduction();
        foodAccumulated += foodThisTurn;

        int foodNeeded = getFoodNeededForGrowth();
        if (foodAccumulated >= foodNeeded && totalPopulation < getMaxPopulation()) {
            foodAccumulated -= foodNeeded;
            totalPopulation++;
            // Nowa populacja jest początkowo nieprzypisana
        }

        // 2. Produkcja budynków/statków
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
                return ship;
            }
        }

        return null;
    }

    public int getFoodAccumulated() {
        return foodAccumulated;
    }

    /**
     * Oblicza ile żywności potrzeba na następną populację
     * Wzór: 10 + (aktualna_populacja × 2)
     * Przykłady: 1→10, 2→14, 3→16, 4→18, 5→20, 10→30
     */
    public int getFoodNeededForGrowth() {
        return BASE_FOOD_PER_CITIZEN + (totalPopulation * 2);
    }

    /**
     * Sprawdza czy cała populacja jest przypisana
     */
    public boolean isPopulationFullyAssigned() {
        return getUnassignedPopulation() == 0;
    }
}