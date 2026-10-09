package com.mygame.engine.core;

import com.mygame.engine.helpers.GameConstants;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;

public class DisplaySurface extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private final GameStateManager stateManager;
    private final ObjectManager objectManager;
    private final Runnable toggleFullscreen;
    private double scale = 1.0;
    private int offsetX;
    private int offsetY;

    public DisplaySurface(GameStateManager stateManager, ObjectManager objectManager, Runnable toggleFullscreen) {
        this.stateManager = stateManager;
        this.objectManager = objectManager;
        this.toggleFullscreen = toggleFullscreen;
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setBackground(Color.BLACK);
    }

    public void updateGame(double deltaTime) {
        stateManager.update(deltaTime);
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

    @Override public void mousePressed(MouseEvent event) { stateManager.mousePressed(toLogicalEvent(event)); }
    @Override public void mouseReleased(MouseEvent event) { stateManager.mouseReleased(toLogicalEvent(event)); }
    @Override public void mouseMoved(MouseEvent event) { stateManager.mouseMoved(toLogicalEvent(event)); }
    @Override public void mouseDragged(MouseEvent event) { stateManager.mouseMoved(toLogicalEvent(event)); }
    @Override public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_F11) {
            toggleFullscreen.run();
        } else {
            stateManager.keyPressed(event);
        }
    }
    @Override public void keyReleased(KeyEvent event) { stateManager.keyReleased(event); }
    @Override public void keyTyped(KeyEvent event) { }
    @Override public void mouseClicked(MouseEvent event) { }
    @Override public void mouseEntered(MouseEvent event) { requestFocusInWindow(); }
    @Override public void mouseExited(MouseEvent event) { }
}
