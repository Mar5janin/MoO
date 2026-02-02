package controller;

import model.*;

public class Game {

    private int turn = 1;
    private final Galaxy galaxy;

    private int totalCredits = 500;
    private int totalResearch = 0;

    private final ResearchManager researchManager = new ResearchManager();
    private final FogOfWar fogOfWar;

    public Game(Galaxy galaxy) {
        this.galaxy = galaxy;
        this.fogOfWar = new FogOfWar(galaxy);
        this.fogOfWar.updateVisibility();
    }

    public void nextTurn() {
        turn++;

        int creditsThisTurn = 0;
        int researchThisTurn = 0;

        for (StarSystem system : galaxy.getSystems()) {
            creditsThisTurn += system.getTotalCreditsBonus();
            researchThisTurn += system.getTotalResearchBonus();

            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized()) {
                        creditsThisTurn += planet.getCredits();
                        researchThisTurn += planet.getResearch();

                        Ship newShip = planet.processTurn(system);
                        if (newShip != null) {
                            Fleet fleet = system.getOrCreatePlayerFleet();
                            fleet.addShip(newShip);
                        }
                    }
                }
            }

            for (Fleet fleet : system.getFleets()) {
                fleet.processTurn();
            }
        }

        totalCredits += creditsThisTurn;
        totalResearch += researchThisTurn;

        researchManager.addResearchPoints(researchThisTurn);

        fogOfWar.updateVisibility();
    }

    public int getTurn() {
        return turn;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public int getTotalResearch() {
        return totalResearch;
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }

    public ResearchManager getResearchManager() {
        return researchManager;
    }

    public FogOfWar getFogOfWar() {
        return fogOfWar;
    }

    public boolean spendCredits(int amount) {
        if (totalCredits >= amount) {
            totalCredits -= amount;
            return true;
        }
        return false;
    }

    public boolean rushBuyOnPlanet(Planet planet, StarSystem system) {
        if (planet.getBuildQueue().isEmpty()) {
            return false;
        }

        int cost = planet.getRushBuyCost();

        if (totalCredits < cost) {
            return false;
        }

        ProductionOrder current = planet.getBuildQueue().get(0);
        boolean isShip = (current.getProductionType() == ProductionType.SHIP);
        ShipType shipType = isShip ? current.getShipType() : null;

        boolean success = planet.rushBuy(this);

        if (success && isShip && shipType != null) {
            Ship newShip = new Ship(shipType);
            Fleet fleet = system.getOrCreatePlayerFleet();
            fleet.addShip(newShip);
        }

        return success;
    }

    public boolean canEndTurn() {
        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized()) {
                        if (planet.getBuildQueue().isEmpty()) {
                            return false;
                        }
                        if (!planet.isPopulationFullyAssigned()) {
                            return false;
                        }
                    }
                }
            }
        }

        if (researchManager.getCurrentResearch() == null) {
            return false;
        }

        return true;
    }

    public String getEndTurnBlockReason() {
        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized()) {
                        if (planet.getBuildQueue().isEmpty()) {
                            return "Planeta w systemie " + system.getName() + " nie ma kolejki budowy!";
                        }
                        if (!planet.isPopulationFullyAssigned()) {
                            int unassigned = planet.getUnassignedPopulation();
                            return "Planeta w systemie " + system.getName() + " ma " + unassigned +
                                    " nieprzypisanych " + (unassigned == 1 ? "pracownika" : "pracowników") + "!";
                        }
                    }
                }
            }
        }

        if (researchManager.getCurrentResearch() == null) {
            return "Nie wybrano projektu badawczego!";
        }

        return null;
    }
}