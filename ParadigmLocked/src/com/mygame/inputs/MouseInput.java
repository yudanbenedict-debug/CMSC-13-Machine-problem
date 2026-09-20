package com.mygame.inputs;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

/*
    Small mouse input helper; attaches itself to the provided component.
    To Override the event methods as necessary when using this class.
 */
public class MouseInput extends MouseAdapter  {
    private final JComponent component;

    public MouseInput(JComponent component) {
        this.component = component;
        this.component.addMouseListener(this);
        this.component.addMouseMotionListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}
}
