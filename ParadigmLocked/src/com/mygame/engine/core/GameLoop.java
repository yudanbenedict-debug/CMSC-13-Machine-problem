package com.mygame.engine.core;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/*
    Important Notes:
    This File is main/current engine logic for the game, study timers.

    Optimization notes:
    - The loop thread now only handles TIMING. Every tick (object updates + state update + repaint
      request) is handed to the Swing EDT with invokeAndWait, so updates, input and painting all
      run on ONE thread. Before, update() ran on this thread while paint/input ran on the EDT,
      which could cause random flicker/state glitches (race conditions).
    - Frame pacing targets ~60 FPS instead of "work + sleep(10)" (~100 uncapped ticks/sec).
    - deltaTime is clamped so a long stall (window drag, heavy asset load) doesn't make
      animations jump.
 */

public class GameLoop implements Runnable {
    private static final double TARGET_FPS = 60.0;
    private static final long FRAME_NANOS = (long) (1_000_000_000L / TARGET_FPS);
    private static final double MAX_DELTA_SECONDS = 0.25;

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
            gameThread.setDaemon(true);
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
            long frameStart = System.nanoTime();
            double deltaTime = Math.min((frameStart - lastTime) / 1_000_000_000.0, MAX_DELTA_SECONDS);
            lastTime = frameStart;

            try {
                // run the tick on the EDT and wait for it, so ticks never pile up
                SwingUtilities.invokeAndWait(() -> tick(deltaTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (InvocationTargetException e) {
                // keep the loop alive if one frame throws, but still show the error
                e.getCause().printStackTrace();
            }

            long sleepNanos = FRAME_NANOS - (System.nanoTime() - frameStart);
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                Thread.yield(); // frame ran long, don't sleep but let other threads breathe
            }
        }

        running = false;
    }

    private void tick(double deltaTime) {
        ObjectManager manager = objectManager;
        if (manager != null) {
            manager.updateAll(deltaTime);
        }
        updateTask.accept(deltaTime);
    }
}
