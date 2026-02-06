package model.tech;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResearchManager {
    private final Set<Technology> researchedTechs = new HashSet<>();
    private Technology currentResearch = null;
    private int currentProgress = 0;

    private final Map<Technology, Integer> savedProgress = new HashMap<>();

    private int shipAttackBonus = 0;
    private int shipDefenseBonus = 0;
    private int researchBonusPercent = 0;

    public void setCurrentResearch(Technology tech) {
        if (currentResearch != null && currentProgress > 0) {
            savedProgress.put(currentResearch, currentProgress);
        }

        if (tech == null) {
            this.currentResearch = null;
            this.currentProgress = 0;
            return;
        }

        if (!canResearch(tech)) {
            return;
        }

        this.currentResearch = tech;
        this.currentProgress = savedProgress.getOrDefault(tech, 0);
    }

    public void addResearchPoints(int points) {
        if (currentResearch == null) return;

        int effectivePoints = points;
        if (researchBonusPercent > 0) {
            effectivePoints = points + (points * researchBonusPercent / 100);
        }

        currentProgress += effectivePoints;

        if (currentProgress >= currentResearch.getCost()) {
            completeResearch();
        }
    }

    private void completeResearch() {
        if (currentResearch == null) return;

        researchedTechs.add(currentResearch);
        applyTechEffects(currentResearch);

        savedProgress.remove(currentResearch);

        currentResearch = null;
        currentProgress = 0;
    }

    private void applyTechEffects(Technology tech) {
        for (TechEffect effect : tech.getEffects()) {
            switch (effect.getType()) {
                case SHIP_ATTACK_BONUS -> shipAttackBonus += effect.getValue();
                case SHIP_DEFENSE_BONUS -> shipDefenseBonus += effect.getValue();
                case RESEARCH_BONUS -> researchBonusPercent += effect.getValue();
            }
        }
    }

    public boolean canResearch(Technology tech) {
        if (isResearched(tech)) return false;
        if (currentResearch == tech) return false;

        for (Technology prereq : tech.getPrerequisites()) {
            if (!isResearched(prereq)) {
                return false;
            }
        }

        return true;
    }

    public boolean isResearched(Technology tech) {
        return researchedTechs.contains(tech);
    }

    public boolean isUnlocked(String buildingOrShipName) {
        for (Technology tech : researchedTechs) {
            for (TechEffect effect : tech.getEffects()) {
                if ((effect.getType() == TechEffectType.UNLOCK_BUILDING ||
                        effect.getType() == TechEffectType.UNLOCK_SHIP) &&
                        buildingOrShipName.equals(effect.getStringValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Technology getCurrentResearch() {
        return currentResearch;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }


    public int getShipAttackBonus() { return shipAttackBonus; }
    public int getShipDefenseBonus() { return shipDefenseBonus; }
}