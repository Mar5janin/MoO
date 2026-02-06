package model.galaxy;

public enum MapSize {
    SMALL(15),
    MEDIUM(25),
    LARGE(40);

    private final int starCount;

    MapSize(int starCount) {
        this.starCount = starCount;
    }

    public int getStarCount() {
        return starCount;
    }
}
