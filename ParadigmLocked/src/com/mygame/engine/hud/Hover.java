package com.mygame.engine.hud;

import com.mygame.engine.core.SoundManager;

// Tracks which item (index, -1 = none) is hovered and plays the menu hover sound when a new one is entered.
public final class Hover {
    private int index = -1;

    public boolean update(boolean hovered) {
        return update(hovered ? 0 : -1);
    }

    // Returns true when the hover moved onto a new item.
    public boolean update(int newIndex) {
        boolean entered = newIndex >= 0 && newIndex != index;
        if (entered) {
            SoundManager.playMenuHover();
        }
        index = newIndex;
        return entered;
    }

    public int index() {
        return index;
    }

    public boolean isHovered() {
        return index >= 0;
    }

    public boolean is(int candidate) {
        return index == candidate;
    }

    public void clear() {
        index = -1;
    }
}
