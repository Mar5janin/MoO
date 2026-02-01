package model;

public class Ship {
    private final ShipType type;
    private int currentHP;
    private final int maxHP;

    public Ship(ShipType type) {
        this.type = type;
        // HP bazuje na obronie statku
        this.maxHP = type.getDefense() * 10;
        this.currentHP = maxHP;
    }

    public ShipType getType() {
        return type;
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