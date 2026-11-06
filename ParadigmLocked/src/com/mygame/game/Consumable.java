package com.mygame.game;

import com.mygame.engine.core.Img;
import com.mygame.engine.core.Sfx;

// The three consumables; weight = drop weight.
public enum Consumable {
    NULLSLINGER("NullSlinger", Img.CONSUMABLE_NULLSLINGER, Sfx.CONSUMABLE_NULLSLINGER_USED, 60,
        "Nullifies one wrong choice."),
    SCRIPTERS_SACRIFICE("Scripter's Sacrifice", Img.CONSUMABLE_SCRIPTERS_SACRIFICE,
        Sfx.CONSUMABLE_SCRIPTERS_SACRIFICE_USED, 28,
        "Your turn deals double damage. Overusing it hurts you every turn until discarded."),
    REFRESHER("Refresher", Img.CONSUMABLE_REFRESHER, Sfx.CONSUMABLE_REFRESHER_USED, 12,
        "Resets your Declarative Dynamo hint calls.");

    public final String displayName;
    public final Img image;
    public final Sfx usedSound;
    public final int weight;
    public final String description;

    Consumable(String displayName, Img image, Sfx usedSound, int weight, String description) {
        this.displayName = displayName;
        this.image = image;
        this.usedSound = usedSound;
        this.weight = weight;
        this.description = description;
    }
}
