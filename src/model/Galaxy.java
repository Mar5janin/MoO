package model;

import java.util.ArrayList;
import java.util.List;

public class Galaxy {
    private List<StarSystem> systems = new ArrayList<>();
    private StarSystem homeSystem;

    public List<StarSystem> getSystems() {
        return systems;
    }

    public StarSystem getHomeSystem() {
        return homeSystem;
    }

    public void setHomeSystem(StarSystem homeSystem) {
        this.homeSystem = homeSystem;
    }
}
