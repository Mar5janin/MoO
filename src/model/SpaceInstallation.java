package model;

public class SpaceInstallation {
    private final SpaceInstallationType type;
    private int currentHP;
    private final int maxHP;

    public SpaceInstallation(SpaceInstallationType type) {
        this.type = type;
        this.maxHP = type == SpaceInstallationType.BATTLE_STATION ? 500 : 100;
        this.currentHP = maxHP;
    }

    public SpaceInstallationType getType() {
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

    public int getDefense() {
        if (type == SpaceInstallationType.BATTLE_STATION) {
            return 50;
        }
        return 0;
    }

    public int getAttack() {
        if (type == SpaceInstallationType.BATTLE_STATION) {
            return 40;
        }
        return 0;
    }
}