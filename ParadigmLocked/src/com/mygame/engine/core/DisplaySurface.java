package com.mygame.engine.core;

import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.inputs.InputHandler;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

public class DisplaySurface extends JPanel {
    private final GameStateManager stateManager;
    private final InputHandler inputHandler;
    private double scale = 1.0;
    private int offsetX;
    private int offsetY;
    private boolean showFps;
    private int framesThisSecond;
    private int fps;
    private long fpsWindowStart = System.nanoTime();

    public DisplaySurface(GameStateManager stateManager, Runnable toggleFullscreen) {
        this.stateManager = stateManager;
        setFocusable(true);
        inputHandler = new InputHandler(this, stateManager, this::toLogicalEvent, toggleFullscreen);
        inputHandler.setToggleFpsCounter(() -> showFps = !showFps);
        setBackground(Color.BLACK);
    }

    public void updateGame(double deltaTime) {
        stateManager.update(deltaTime);
        // draw now, inside the tick, so every update gets exactly one frame (repaint() could merge or delay frames)
        paintImmediately(0, 0, getWidth(), getHeight());
    }

    // this refreshes the scale when the window changes (fixes bleeding when toggling fullscreen)
    public void refreshViewport() {
        calculateViewport();
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        calculateViewport();
        // background is already cleared to black by super.paintComponent (setBackground(BLACK))
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        stateManager.draw(g);
        g.dispose();
        countFrame();
        if (showFps) {
            graphics.setColor(new Color(0, 0, 0, 160));
            graphics.fillRect(6, 6, 74, 22);
            graphics.setColor(fps >= 55 ? Color.GREEN : fps >= 40 ? Color.YELLOW : Color.RED);
            graphics.drawString("FPS: " + fps, 12, 22);
        }
        // flush the frame to the screen right away (smoother animation, mainly on Linux)
        Toolkit.getDefaultToolkit().sync();
    }

    // frames actually painted during the last second (F3 toggles the counter)
    private void countFrame() {
        framesThisSecond++;
        long now = System.nanoTime();
        if (now - fpsWindowStart >= 1_000_000_000L) {
            fps = framesThisSecond;
            framesThisSecond = 0;
            fpsWindowStart = now;
        }
    }

    private void calculateViewport() {
        scale = Math.min((double) getWidth() / GameConstants.LOGICAL_WIDTH,
                (double) getHeight() / GameConstants.LOGICAL_HEIGHT);
        offsetX = (int) ((getWidth() - GameConstants.LOGICAL_WIDTH * scale) / 2);
        offsetY = (int) ((getHeight() - GameConstants.LOGICAL_HEIGHT * scale) / 2);
    }

    private Point toLogicalPoint(MouseEvent event) {
        calculateViewport();
        int x = (int) ((event.getX() - offsetX) / scale);
        int y = (int) ((event.getY() - offsetY) / scale);
        return new Point(x, y);
    }

    private MouseEvent toLogicalEvent(MouseEvent event) {
        Point point = toLogicalPoint(event);
        return new MouseEvent(this, event.getID(), event.getWhen(), event.getModifiersEx(),
                point.x, point.y, event.getClickCount(), event.isPopupTrigger(), event.getButton());
    }

}
