package model;

public class AsteroidField implements OrbitObject {
    @Override
    public OrbitType getType() {
        return OrbitType.ASTEROIDS;
    }
}

