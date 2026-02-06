package controller;

import model.*;
import model.buildings.SpaceInstallation;
import model.galaxy.StarSystem;
import model.orbits.OrbitSlot;
import model.orbits.planets.Planet;
import model.ships.Fleet;
import model.tech.ResearchManager;

import java.util.*;

public class CombatResolver {

    public static class CombatResult {
        public final Fleet winner;
        public final Fleet loser;
        public final String report;

        public CombatResult(Fleet winner, Fleet loser, String report) {
            this.winner = winner;
            this.loser = loser;
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

        int playerShipsBeforeBattle = 0;
        int enemyShipsBeforeBattle = 0;

        // Sumowanie statystyk wszystkich flot gracza
        for (Fleet fleet : playerFleets) {
            playerAttack += fleet.getTotalAttack(playerRM);
            playerDefense += fleet.getTotalDefense(playerRM);
            playerShipsBeforeBattle += fleet.getShipCount();
        }

        // Sumowanie statystyk wszystkich flot wroga
        for (Fleet fleet : enemyFleets) {
            enemyAttack += fleet.getTotalAttack(enemyRM);
            enemyDefense += fleet.getTotalDefense(enemyRM);
            enemyShipsBeforeBattle += fleet.getShipCount();
        }

        // Posterunki bojowe dodają moc do swojej strony
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

        Fleet winner;
        Fleet loser;
        boolean playerWon = playerPower > enemyPower;

        StringBuilder report = new StringBuilder();
        report.append(" WALKA W SYSTEMIE ").append(system.getName()).append(" \n");
        report.append("Siły gracza: ").append(playerShipsBeforeBattle).append(" statków | Atak ").append(playerAttack).append(", Obrona ").append(playerDefense).append(" (MOC: ").append(playerPower).append(")\n");
        report.append("Siły wroga: ").append(enemyShipsBeforeBattle).append(" statków | Atak ").append(enemyAttack).append(", Obrona ").append(enemyDefense).append(" (MOC: ").append(enemyPower).append(")\n\n");

//        przegrany traci wszystkie statki i ewentualny posterunek z systemu
        if (playerWon) {
            winner = playerFleets.get(0);
            loser = enemyFleets.get(0);

            // Zniszczenie wszystkich wrogich statków
            for (Fleet fleet : enemyFleets) {
                fleet.getShips().clear();
            }

            // Zniszczenie wrogiego posterunku
            if (system.hasBattleStation() && system.getBattleStation().getOwner() != null) {
                system.setBattleStation(null);
                report.append("Posterunek bojowy wroga został zniszczony!\n");
            }

            report.append("Zniszczono wszystkie wrogie statki (").append(enemyShipsBeforeBattle).append(")\n");
            report.append("\nZWYCIĘZCA: Gracz\n");
        } else {
            winner = enemyFleets.get(0);
            loser = playerFleets.get(0);

            for (Fleet fleet : playerFleets) {
                fleet.getShips().clear();
            }

            if (system.hasBattleStation() && system.getBattleStation().getOwner() == null) {
                system.setBattleStation(null);
                report.append("Twój posterunek bojowy został zniszczony!\n");
            }

            report.append("Zniszczono wszystkie twoje statki (").append(playerShipsBeforeBattle).append(")\n");
            report.append("\nZWYCIĘZCA: Przeciwnik\n");
        }

        // Usunięcie pustych flot
        system.getFleets().removeIf(Fleet::isEmpty);

        return new CombatResult(winner, loser, report.toString());
    }

//    Określa kto kontroluje system żeby wiedzieć do kogo mają należeć ewentualne kolonie w systemie
    public static void resolveSystemControl(StarSystem system) {
        boolean hasPlayerFleet = system.getFleets().stream().anyMatch(f -> f.getOwner() == null);
        boolean hasEnemyFleet = system.getFleets().stream().anyMatch(f -> f.getOwner() != null);
        boolean hasPlayerStation = system.hasBattleStation() && system.getBattleStation().getOwner() == null;
        boolean hasEnemyStation = system.hasBattleStation() && system.getBattleStation().getOwner() != null;

        Enemy systemOwner = null;
        boolean playerControls = hasPlayerFleet || hasPlayerStation;
        boolean enemyControls = hasEnemyFleet || hasEnemyStation;

        // Określenie kontroli systemu
        if (playerControls && !enemyControls) {
            systemOwner = null;  // Gracz (null = gracz)
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
            // obie strony obecne, nie zmieniaj kontroli planet
            return;
        }

        // Aktualizacja właściciela planet zgodnie z kontrolą systemu
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