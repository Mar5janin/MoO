package controller;

import model.*;
import java.util.*;

public class CombatResolver {

    private static final double CLOSE_COMBAT_THRESHOLD = 0.15;

    public static class CombatResult {
        public final Fleet winner;
        public final Fleet loser;
        public final boolean wasClose;
        public final String report;

        public CombatResult(Fleet winner, Fleet loser, boolean wasClose, String report) {
            this.winner = winner;
            this.loser = loser;
            this.wasClose = wasClose;
            this.report = report;
        }
    }

    public static CombatResult resolveBattle(StarSystem system, ResearchManager playerRM, ResearchManager enemyRM) {
        List<Fleet> playerFleets = new ArrayList<>();
        List<Fleet> enemyFleets = new ArrayList<>();

        for (Fleet fleet : system.getFleets()) {
            if (fleet.getOwner() == null) {
                playerFleets.add(fleet);
            } else {
                enemyFleets.add(fleet);
            }
        }

        if (playerFleets.isEmpty() || enemyFleets.isEmpty()) {
            return null;
        }

        int playerAttack = 0;
        int playerDefense = 0;
        int enemyAttack = 0;
        int enemyDefense = 0;

        for (Fleet fleet : playerFleets) {
            playerAttack += fleet.getTotalAttack(playerRM);
            playerDefense += fleet.getTotalDefense(playerRM);
        }

        for (Fleet fleet : enemyFleets) {
            enemyAttack += fleet.getTotalAttack(enemyRM);
            enemyDefense += fleet.getTotalDefense(enemyRM);
        }

        if (system.hasBattleStation()) {
            SpaceInstallation station = system.getBattleStation();
            if (station.getOwner() == null) {
                playerAttack += station.getAttack();
                playerDefense += station.getDefense();
            } else {
                enemyAttack += station.getAttack();
                enemyDefense += station.getDefense();
            }
        }

        int playerPower = playerAttack + playerDefense;
        int enemyPower = enemyAttack + enemyDefense;

        boolean isClose = Math.abs(playerPower - enemyPower) <= (Math.max(playerPower, enemyPower) * CLOSE_COMBAT_THRESHOLD);

        Fleet winner;
        Fleet loser;
        boolean playerWon;

        if (isClose) {
            playerWon = Math.random() < 0.5;
        } else {
            playerWon = playerPower > enemyPower;
        }

        StringBuilder report = new StringBuilder();
        report.append("=== WALKA W SYSTEMIE ").append(system.getName()).append(" ===\n");
        report.append("Siły gracza: Atak ").append(playerAttack).append(", Obrona ").append(playerDefense).append("\n");
        report.append("Siły wroga: Atak ").append(enemyAttack).append(", Obrona ").append(enemyDefense).append("\n");

        if (playerWon) {
            winner = playerFleets.get(0);
            loser = enemyFleets.get(0);

            for (Fleet enemyFleet : enemyFleets) {
                int shipsBeforeDestroyed = enemyFleet.getShipCount();
                applyDamageToFleet(enemyFleet, playerAttack, enemyRM);
                int shipsAfter = enemyFleet.getShipCount();
                report.append("Zniszczono ").append(shipsBeforeDestroyed - shipsAfter).append(" wrogich statków\n");
            }

            for (Fleet playerFleet : playerFleets) {
                int shipsBeforeDestroyed = playerFleet.getShipCount();
                applyDamageToFleet(playerFleet, enemyAttack * 0.3, playerRM);
                int shipsAfter = playerFleet.getShipCount();
                int lost = shipsBeforeDestroyed - shipsAfter;
                if (lost > 0) {
                    report.append("Stracono ").append(lost).append(" własnych statków\n");
                }
            }

            if (system.hasBattleStation() && system.getBattleStation().getOwner() != null) {
                system.getBattleStation().takeDamage(playerAttack);
                if (system.getBattleStation().isDestroyed()) {
                    system.setBattleStation(null);
                    report.append("Posterunek bojowy wroga został zniszczony!\n");
                }
            }

            report.append("\nZWYCIĘZCA: Gracz\n");
        } else {
            winner = enemyFleets.get(0);
            loser = playerFleets.get(0);

            for (Fleet playerFleet : playerFleets) {
                int shipsBeforeDestroyed = playerFleet.getShipCount();
                applyDamageToFleet(playerFleet, enemyAttack, playerRM);
                int shipsAfter = playerFleet.getShipCount();
                report.append("Zniszczono ").append(shipsBeforeDestroyed - shipsAfter).append(" twoich statków\n");
            }

            for (Fleet enemyFleet : enemyFleets) {
                int shipsBeforeDestroyed = enemyFleet.getShipCount();
                applyDamageToFleet(enemyFleet, playerAttack * 0.3, enemyRM);
                int shipsAfter = enemyFleet.getShipCount();
                int lost = shipsBeforeDestroyed - shipsAfter;
                if (lost > 0) {
                    report.append("Wróg stracił ").append(lost).append(" statków\n");
                }
            }

            if (system.hasBattleStation() && system.getBattleStation().getOwner() == null) {
                system.getBattleStation().takeDamage(enemyAttack);
                if (system.getBattleStation().isDestroyed()) {
                    system.setBattleStation(null);
                    report.append("Twój posterunek bojowy został zniszczony!\n");
                }
            }

            report.append("\nZWYCIĘZCA: Przeciwnik\n");
        }

        for (Fleet fleet : system.getFleets()) {
            fleet.getShips().removeIf(Ship::isDestroyed);
        }
        system.getFleets().removeIf(Fleet::isEmpty);

        return new CombatResult(winner, loser, isClose, report.toString());
    }

    private static void applyDamageToFleet(Fleet fleet, double totalDamage, ResearchManager rm) {
        List<Ship> ships = new ArrayList<>(fleet.getShips());
        if (ships.isEmpty()) return;

        double damagePerShip = totalDamage / ships.size();

        for (Ship ship : ships) {
            int effectiveDefense = ship.getDefense(rm);
            int actualDamage = (int)(damagePerShip * (100.0 / (100.0 + effectiveDefense)));
            ship.takeDamage(actualDamage);
        }

        fleet.getShips().removeIf(Ship::isDestroyed);
    }

    public static void resolveSystemControl(StarSystem system) {
        boolean hasPlayerFleet = system.getFleets().stream().anyMatch(f -> f.getOwner() == null);
        boolean hasEnemyFleet = system.getFleets().stream().anyMatch(f -> f.getOwner() != null);
        boolean hasPlayerStation = system.hasBattleStation() && system.getBattleStation().getOwner() == null;
        boolean hasEnemyStation = system.hasBattleStation() && system.getBattleStation().getOwner() != null;

        Enemy systemOwner = null;
        boolean playerControls = hasPlayerFleet || hasPlayerStation;
        boolean enemyControls = hasEnemyFleet || hasEnemyStation;

        if (playerControls && !enemyControls) {
            systemOwner = null;
        } else if (enemyControls && !playerControls) {
            for (Fleet fleet : system.getFleets()) {
                if (fleet.getOwner() != null) {
                    systemOwner = fleet.getOwner();
                    break;
                }
            }
            if (systemOwner == null && hasEnemyStation) {
                systemOwner = system.getBattleStation().getOwner();
            }
        } else if (playerControls && enemyControls) {
            return;
        }

        for (OrbitSlot orbit : system.getOrbits()) {
            if (orbit.getObject() instanceof Planet planet) {
                if (planet.isColonized()) {
                    Enemy originalOwner = planet.getOriginalOwner();

                    if (systemOwner == null && originalOwner == null) {
                        planet.setOwner(null);
                    } else if (systemOwner != null && originalOwner != null && systemOwner == originalOwner) {
                        planet.setOwner(originalOwner);
                    } else if (systemOwner == null && originalOwner != null && !playerControls) {
                        planet.setOwner(originalOwner);
                    } else {
                        planet.setOwner(systemOwner);
                    }
                }
            }
        }
    }
}