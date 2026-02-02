package model;

import java.awt.*;

public class Enemy {
    private final String name;
    private final Color color;
    private StarSystem homeSystem;

    public Enemy(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public StarSystem getHomeSystem() {
        return homeSystem;
    }

    public void setHomeSystem(StarSystem homeSystem) {
        this.homeSystem = homeSystem;
    }
}