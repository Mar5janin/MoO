package model.buildings;

import model.Enemy;

public class SpaceInstallation {
    private final SpaceInstallationType type;
    private int currentHP;
    private final int maxHP;
    private Enemy owner;

    public SpaceInstallation(SpaceInstallationType type) {
        this(type, null);
    }

    public SpaceInstallation(SpaceInstallationType type, Enemy owner) {
        this.type = type;
        this.owner = owner;
        this.maxHP = type == SpaceInstallationType.BATTLE_STATION ? 500 : 100;
        this.currentHP = maxHP;
    }

    public SpaceInstallationType getType() {
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