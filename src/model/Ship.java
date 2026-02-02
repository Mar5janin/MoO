package model;

public class Ship {
    private final ShipType type;
    private final Enemy owner;
    private int currentHP;
    private final int maxHP;

    public Ship(ShipType type) {
        this(type, null);
    }

    public Ship(ShipType type, Enemy owner) {
        this.type = type;
        this.owner = owner;
        this.maxHP = type.getDefense() * 10;
        this.currentHP = maxHP;
    }

    public ShipType getType() {
        return type;
    }

    public Enemy getOwner() {
        return owner;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void takeDamage(int damage) {
        currentHP = Math.max(0, currentHP - damage);
    }

    public void repair(int amount) {
        currentHP = Math.min(maxHP, currentHP + amount);
    }

    public boolean isDestroyed() {
        return currentHP <= 0;
    }

    public int getAttack(ResearchManager researchManager) {
        return type.getEffectiveAttack(researchManager);
    }

    public int getDefense(ResearchManager researchManager) {
        return type.getEffectiveDefense(researchManager);
    }
}