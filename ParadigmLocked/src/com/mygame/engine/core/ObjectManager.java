package com.mygame.engine.core;

import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObjectManager {
    private final List<GameObject> objects;
    private boolean paused = false;

    public ObjectManager() {
        this.objects = new CopyOnWriteArrayList<>();
    }

    public void addObject(GameObject obj) {
        objects.add(obj);
    }

    public void removeObject(GameObject obj) {
        objects.remove(obj);
    }

    public void updateAll(double deltaTime) {
        if (paused) return;

        for (GameObject obj : objects) {
            obj.update(deltaTime);
            // TODO - ADD ITEMS HERE basically the update logic 
        }
    }

    public void drawAll(Graphics2D g2d) {
        //TODO - Don't know how we'll implement this yet
    }

    public void setPaused(boolean paused) { this.paused = paused; }
    public List<GameObject> getObjects() { return List.copyOf(objects); }
    public void clear() { objects.clear();}
}
