package com.mygame.game;

import com.mygame.engine.helpers.constants.GameConstants;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

// Owned consumables; Scripter's Sacrifice hurts you each turn after its safe uses until discarded.
public final class ConsumableInventory {
    private final Map<Consumable, Integer> counts = new EnumMap<>(Consumable.class);
    private final Random random = new Random();
    private boolean scriptersOwned;
    private boolean scriptersOverused;

    public ConsumableInventory() {
        for (Consumable consumable : Consumable.values()) {
            counts.put(consumable, 0);
        }
    }

    // Weighted pick: 60 / 28 / 12.
    public Consumable rollDrop() {
        int total = 0;
        for (Consumable consumable : Consumable.values()) {
            total += consumable.weight;
        }
        int roll = random.nextInt(total);
        for (Consumable consumable : Consumable.values()) {
            roll -= consumable.weight;
            if (roll < 0) {
                return consumable;
            }
        }
        return Consumable.NULLSLINGER;
    }

    public boolean shouldDrop() {
        return random.nextDouble() < GameConstants.CONSUMABLE_DROP_CHANCE;
    }

    public void add(Consumable consumable) {
        if (consumable == Consumable.SCRIPTERS_SACRIFICE) {
            counts.merge(consumable, GameConstants.SCRIPTERS_SACRIFICE_SAFE_USES, Integer::sum);
            scriptersOwned = true;
            scriptersOverused = false;
        } else {
            counts.merge(consumable, 1, Integer::sum);
        }
    }

    // Scripter's Sacrifice shows its remaining safe uses.
    public int count(Consumable consumable) {
        return counts.get(consumable);
    }

    public boolean canUse(Consumable consumable) {
        if (consumable == Consumable.SCRIPTERS_SACRIFICE) {
            return scriptersOwned;
        }
        return counts.get(consumable) > 0;
    }

    public boolean consume(Consumable consumable) {
        if (!canUse(consumable)) {
            return false;
        }
        int current = counts.get(consumable);
        if (consumable == Consumable.SCRIPTERS_SACRIFICE && current == 0) {
            scriptersOverused = true;
            return true;
        }
        counts.put(consumable, current - 1);
        return true;
    }

    public boolean scriptersOwned() {
        return scriptersOwned;
    }

    public boolean scriptersDepleted() {
        return scriptersOwned && counts.get(Consumable.SCRIPTERS_SACRIFICE) == 0;
    }

    // Overused and still kept: 1 self-damage per turn.
    public boolean scriptersPunishing() {
        return scriptersOwned && scriptersOverused;
    }

    public void discardScripters() {
        scriptersOwned = false;
        scriptersOverused = false;
        counts.put(Consumable.SCRIPTERS_SACRIFICE, 0);
    }
}
