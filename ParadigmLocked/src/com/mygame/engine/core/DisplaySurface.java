package com.mygame.engine.core;

import com.mygame.engine.helpers.constants.GameConstants;
import com.mygame.inputs.InputHandler;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

public class DisplaySurface extends JPanel {
    private final GameStateManager stateManager;
    private final ObjectManager objectManager;
    private final InputHandler inputHandler;
    private double scale = 1.0;
    private int offsetX;
    private int offsetY;

    public DisplaySurface(GameStateManager stateManager, ObjectManager objectManager, Runnable toggleFullscreen) {
        this.stateManager = stateManager;
        this.objectManager = objectManager;
        setFocusable(true);
        inputHandler = new InputHandler(this, stateManager, this::toLogicalEvent, toggleFullscreen);
        setBackground(Color.BLACK);
    }

    public void updateGame(double deltaTime) {
        stateManager.update(deltaTime);
        repaint();
    }

    // fix for bleeding when refreshing fullscreen
    // this is called by a listener in gamewindow so it remains active the entire time negligble performance effect
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
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        stateManager.draw(g);
        objectManager.drawAll(g);
        g.dispose();
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
