package model.ships;

import model.Enemy;
import model.tech.ResearchManager;

public class Ship {
    private final ShipType type;
    private final Enemy owner;

    public Ship(ShipType type) {
        this(type, null);
    }

    public Ship(ShipType type, Enemy owner) {
        this.type = type;
        this.owner = owner;
    }

    public ShipType getType() {
        return type;
    }

    public Enemy getOwner() {
        return owner;
    }

    public int getAttack(ResearchManager researchManager) {
        return type.getEffectiveAttack(researchManager);
    }

    public int getDefense(ResearchManager researchManager) {
        return type.getEffectiveDefense(researchManager);
    }
}