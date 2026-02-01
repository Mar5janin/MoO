package model;

public class Game {

    private int turn = 1;
    private final Galaxy galaxy;

    // Globalne zasoby gracza
    private int totalCredits = 500;
    private int totalResearch = 0;

    // System badań
    private final ResearchManager researchManager = new ResearchManager();

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

                        // Przetwórz turę planety i odbierz nowy statek jeśli został wyprodukowany
                        Ship newShip = planet.processTurn(system);
                        if (newShip != null) {
                            // Dodaj statek do floty w tym systemie
                            Fleet fleet = system.getOrCreatePlayerFleet();
                            fleet.addShip(newShip);
                        }
                    }
                }
            }

            // Przetwórz tury dla wszystkich flot w systemie
            for (Fleet fleet : system.getFleets()) {
                fleet.processTurn();
            }
        }

        totalCredits += creditsThisTurn;
        totalResearch += researchThisTurn;

        // Dodaj punkty badań do aktualnego projektu
        researchManager.addResearchPoints(researchThisTurn);
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

    public boolean canEndTurn() {
        // Sprawdź czy wszystkie skolonizowane planety mają kolejkę budowy
        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized() && planet.getBuildQueue().isEmpty()) {
                        return false;
                    }
                }
            }
        }

        // Sprawdź czy są włączone badania
        if (researchManager.getCurrentResearch() == null) {
            return false;
        }

        return true;
    }

    public String getEndTurnBlockReason() {
        // Sprawdź planety bez kolejki
        for (StarSystem system : galaxy.getSystems()) {
            for (OrbitSlot orbit : system.getOrbits()) {
                if (orbit.getObject() instanceof Planet planet) {
                    if (planet.isColonized() && planet.getBuildQueue().isEmpty()) {
                        return "Planeta w systemie " + system.getName() + " nie ma kolejki budowy!";
                    }
                }
            }
        }

        // Sprawdź badania
        if (researchManager.getCurrentResearch() == null) {
            return "Nie wybrano projektu badawczego!";
        }

        return null;
    }
}