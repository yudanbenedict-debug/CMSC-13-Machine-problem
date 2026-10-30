package com.mygame.game;

import com.mygame.engine.helpers.constants.GameConstants;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/*
    Holds how many of each consumable the player owns.
    Scripter's Sacrifice is special: each one picked up adds SCRIPTERS_SACRIFICE_SAFE_USES "safe" uses.
    When the safe uses run out the item is NOT removed: it can still be used (double damage), but
    while you keep it every turn deals 1 damage to you, until you discard it.
 */
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

    /** Weighted random pick: NullSlinger 60, Scripter's Sacrifice 28, Refresher 12. */
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

    /** Amount shown in the panel (for Scripter's Sacrifice: remaining safe uses). */
    public int count(Consumable consumable) {
        return counts.get(consumable);
    }

    public boolean canUse(Consumable consumable) {
        if (consumable == Consumable.SCRIPTERS_SACRIFICE) {
            return scriptersOwned;
        }
        return counts.get(consumable) > 0;
    }

    /** Spends one use. Returns false if the player doesn't have it. */
    public boolean consume(Consumable consumable) {
        if (!canUse(consumable)) {
            return false;
        }
        int current = counts.get(consumable);
        if (consumable == Consumable.SCRIPTERS_SACRIFICE && current == 0) {
            scriptersOverused = true; // used past the suggested amount
            return true;
        }
        counts.put(consumable, current - 1);
        return true;
    }

    public boolean scriptersOwned() {
        return scriptersOwned;
    }

    /** Safe uses spent but the item is still kept: it can be discarded. */
    public boolean scriptersDepleted() {
        return scriptersOwned && counts.get(Consumable.SCRIPTERS_SACRIFICE) == 0;
    }

    /** True while an overused Scripter's Sacrifice is kept: you take 1 damage every turn. */
    public boolean scriptersPunishing() {
        return scriptersOwned && scriptersOverused;
    }

    public void discardScripters() {
        scriptersOwned = false;
        scriptersOverused = false;
        counts.put(Consumable.SCRIPTERS_SACRIFICE, 0);
    }
}
