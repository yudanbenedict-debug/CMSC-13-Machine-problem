package com.mygame.game;

/*
    The three consumables. weight = drop weight (higher = more common), used by the weighted roll in
    ConsumableInventory.rollDrop(). NullSlinger is the most common, Refresher the rarest.
 */
public enum Consumable {
    NULLSLINGER("NullSlinger", "CONSUMABLE_NULLSLINGER", "CONSUMABLE_NULLSLINGER_USED", 60,
        "Nullifies one wrong choice."),
    SCRIPTERS_SACRIFICE("Scripter's Sacrifice", "CONSUMABLE_SCRIPTERS_SACRIFICE",
        "CONSUMABLE_SCRIPTERS_SACRIFICE_USED", 28,
        "Your turn deals double damage. Overusing it hurts you every turn until discarded."),
    REFRESHER("Refresher", "CONSUMABLE_REFRESHER", "CONSUMABLE_REFRESHER_USED", 12,
        "Resets your Declarative Dynamo hint calls.");

    public final String displayName;
    public final String imageKey;
    public final String usedSound;
    public final int weight;
    public final String description;

    Consumable(String displayName, String imageKey, String usedSound, int weight, String description) {
        this.displayName = displayName;
        this.imageKey = imageKey;
        this.usedSound = usedSound;
        this.weight = weight;
        this.description = description;
    }
}
