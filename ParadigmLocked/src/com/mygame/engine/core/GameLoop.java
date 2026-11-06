package com.mygame.engine.core;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

// Fixed 60 FPS loop: this thread only keeps time; each tick (update + paint) runs on the Swing EDT.
public class GameLoop implements Runnable {
    private static final long FRAME_NANOS = 1_000_000_000L / 60;
    private static final double MAX_DELTA_SECONDS = 0.25;
    private static final long SPIN_NANOS = 2_000_000L;

    private final Consumer<Double> tick;
    private Thread gameThread;
    private volatile boolean running;

    public GameLoop(Consumer<Double> tick) {
        this.tick = tick;
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        gameThread = new Thread(this, "GameLoop");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    public synchronized void stop() {
        running = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long nextFrame = lastTime;
        while (running) {
            long frameStart = System.nanoTime();
            double deltaTime = Math.min((frameStart - lastTime) / 1_000_000_000.0, MAX_DELTA_SECONDS);
            lastTime = frameStart;
            try {
                SwingUtilities.invokeAndWait(() -> tick.accept(deltaTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (InvocationTargetException e) {
                e.getCause().printStackTrace(); // keep the loop alive if one frame throws
            }

            // fixed deadlines (not "sleep what's left") so small delays don't add up; skip ahead if far behind
            nextFrame += FRAME_NANOS;
            long now = System.nanoTime();
            if (now - nextFrame > FRAME_NANOS * 2) {
                nextFrame = now;
            }
            try {
                waitUntil(nextFrame);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running = false;
    }

    // Thread.sleep oversleeps 1-4 ms on macOS/Windows, so sleep until ~2 ms before the deadline and spin the rest
    private static void waitUntil(long deadline) throws InterruptedException {
        long remaining = deadline - System.nanoTime();
        if (remaining > SPIN_NANOS) {
            Thread.sleep((remaining - SPIN_NANOS) / 1_000_000);
        }
        while (System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
    }
}
