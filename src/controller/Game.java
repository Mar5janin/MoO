package controller;

import model.*;
import java.util.*;

public class Game {

    private int turn = 1;
    private final Galaxy galaxy;

    private int totalCredits = 500;
    private int totalResearch = 0;

    private final ResearchManager researchManager = new ResearchManager();
    private final FogOfWar fogOfWar;
    private EnemyController enemyController;

    private final List<String> combatReports = new ArrayList<>();

    private boolean gameOver = false;
    private boolean playerWon = false;

    public Game(Galaxy galaxy) {
        this.galaxy = galaxy;
        this.fogOfWar = new FogOfWar(galaxy);

        Enemy enemy = galaxy.getEnemy();
        if (enemy != null) {
            this.enemyController = new EnemyController(enemy, galaxy);
        }

        this.fogOfWar.updateVisibility();
    }

    public void nextTurn() {
        if (gameOver) return;

        turn++;
        combatReports.clear();

        int creditsThisTurn = 0;
        int researchThisTurn = 0;
        int maintenanceCosts = 0;

        for (StarSystem system : galaxy.getSystems()) {
            creditsThisTurn += system.getTotalCreditsBonus();
            researchThisTurn += system.getTotalResearchBonus();

            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized()) {
                        if (planet.getOwner() == null) {
                            creditsThisTurn += planet.getCredits();
                            researchThisTurn += planet.getResearch();
                            maintenanceCosts += planet.getMaintenanceCost();
                        }

                        Ship newShip = planet.processTurn(system);
                        if (newShip != null) {
                            if (planet.getOwner() == null) {
                                Fleet fleet = system.getOrCreatePlayerFleet();
                                fleet.addShip(newShip);
                            } else {
                                Fleet aiFleet = system.getFleets().stream()
                                        .filter(f -> f.getOwner() == planet.getOwner())
                                        .findFirst()
                                        .orElse(null);

                                if (aiFleet == null) {
                                    aiFleet = new Fleet(system, planet.getOwner());
                                    system.addFleet(aiFleet);
                                }
                                aiFleet.addShip(newShip);
                            }
                        }
                    }
                }
            }
        }

        for (StarSystem system : galaxy.getSystems()) {
            for (Fleet fleet : system.getFleets()) {
                if (fleet.getOwner() == null) {
                    for (Ship ship : fleet.getShips()) {
                        maintenanceCosts += ship.getType().getMaintenanceCost();
                    }
                }
            }
        }

        totalCredits += creditsThisTurn;
        totalCredits -= maintenanceCosts;
        totalResearch += researchThisTurn;
        researchManager.addResearchPoints(researchThisTurn);

        if (enemyController != null) {
            enemyController.processTurn();

            int aiResearch = 0;
            for (StarSystem system : galaxy.getSystems()) {
                for (OrbitSlot orbit : system.getOrbits()) {
                    if (orbit.getObject() instanceof Planet planet) {
                        if (planet.isColonized() && planet.getOwner() != null) {
                            aiResearch += planet.getResearch();
                        }
                    }
                }
            }

            enemyController.getResearchManager().addResearchPoints(aiResearch);
        }

        for (StarSystem system : galaxy.getSystems()) {
            ResearchManager enemyRM = enemyController != null ?
                    enemyController.getResearchManager() : new ResearchManager();

            CombatResolver.CombatResult result = CombatResolver.resolveBattle(system, researchManager, enemyRM);

            if (result != null) {
                combatReports.add(result.report);
            }

            CombatResolver.resolveSystemControl(system);
        }

        List<Fleet> allFleets = new ArrayList<>();
        for (StarSystem system : galaxy.getSystems()) {
            allFleets.addAll(system.getFleets());
        }

        for (Fleet fleet : allFleets) {
            fleet.processTurn();

            if (fleet.getOwner() != null && !fleet.isMoving()) {
                if (enemyController != null) {
                    enemyController.colonizePlanet(fleet, fleet.getLocation());
                }
            }
        }

        fogOfWar.updateVisibility();

        checkGameOver();
    }

    private void checkGameOver() {
        boolean playerHasAnything = false;
        boolean enemyHasAnything = false;

        for (StarSystem system : galaxy.getSystems()) {
            for (Fleet fleet : system.getFleets()) {
                if (fleet.getOwner() == null) {
                    playerHasAnything = true;
                } else {
                    enemyHasAnything = true;
                }
            }

            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized()) {
                        if (planet.getOwner() == null) {
                            playerHasAnything = true;
                        } else {
                            enemyHasAnything = true;
                        }
                    }
                }
            }

            if (system.hasBattleStation()) {
                if (system.getBattleStation().getOwner() == null) {
                    playerHasAnything = true;
                } else {
                    enemyHasAnything = true;
                }
            }
        }

        if (!playerHasAnything && !enemyHasAnything) {
            return;
        }

        if (!playerHasAnything) {
            gameOver = true;
            playerWon = false;
        } else if (!enemyHasAnything) {
            gameOver = true;
            playerWon = true;
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean hasPlayerWon() {
        return playerWon;
    }

    public List<String> getCombatReports() {
        return new ArrayList<>(combatReports);
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

    public ResearchManager getEnemyResearchManager() {
        if (enemyController != null) {
            return enemyController.getResearchManager();
        }
        return new ResearchManager();
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
        if (gameOver) return false;

        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized() && planet.getOwner() == null) {
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
        if (gameOver) {
            return playerWon ? "Gra zakończona - WYGRANA!" : "Gra zakończona - PRZEGRANA!";
        }

        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized() && planet.getOwner() == null) {
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