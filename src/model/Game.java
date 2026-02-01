package model;

public class Game {

    private int turn = 1;
    private final Galaxy galaxy;

    public Game(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public void nextTurn() {
        turn++;

        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    planet.processTurn();
                }
            }
        }
    }

    public int getTurn() {
        return turn;
    }
}
