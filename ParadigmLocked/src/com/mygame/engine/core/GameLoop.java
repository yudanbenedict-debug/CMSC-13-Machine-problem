package com.mygame.engine.core;
import java.util.function.Consumer;

/*
    Important Notes:
    This File is main/current engine logic for the game, study timers.
 */

public class GameLoop implements Runnable {
    private Thread gameThread;
    private final Consumer<Double> updateTask;
    private final ThreadManager threadManager;
    private volatile boolean running;
    private volatile ObjectManager objectManager;

    public GameLoop(ObjectManager objectManager, Consumer<Double> updateTask) {
        this(objectManager, updateTask, null);
    }

    public GameLoop(ObjectManager objectManager, Consumer<Double> updateTask, ThreadManager threadManager) {
        this.objectManager = objectManager;
        this.updateTask = updateTask;
        this.threadManager = threadManager;
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        //Now uses ThreadManager, calls the ThreadManager method to run under the gameThreadGroup group.
        //The ThreadManager was never used, but the implementation is too deep, was not removed for safety purposes. Refactor might break the game
        running = true;
        if (threadManager != null) {
            gameThread = threadManager.startThread("GameLoop", this);
        } else {
            gameThread = new Thread(this, "GameLoop");
            gameThread.start();
        }
    }

    public synchronized void stop() {
        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    public void setObjectManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    @Override
    public void run(){
        long lastTime = System.nanoTime();
        while(running){
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;

            if (objectManager != null){
                objectManager.updateAll(deltaTime);
            }

            updateTask.accept(deltaTime);

            try {
                Thread.sleep(10); // ~10ms to reduce CPU usage
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        running = false;
    }
}
