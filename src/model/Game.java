package model;

public class Game {

    private int turn = 1;
    private final Galaxy galaxy;

    // Globalne zasoby gracza
    private int totalCredits = 500;
    private int totalResearch = 0;

    public Game(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public void nextTurn() {
        turn++;

        // Zbierz zasoby ze wszystkich skolonizowanych planet
        int creditsThisTurn = 0;
        int researchThisTurn = 0;

        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized()) {
                        creditsThisTurn += planet.getCredits();
                        researchThisTurn += planet.getResearch();
                        planet.processTurn();
                    }
                }
            }
        }

        totalCredits += creditsThisTurn;
        totalResearch += researchThisTurn;
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
}