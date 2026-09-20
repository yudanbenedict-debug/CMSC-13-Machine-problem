package com.mygame.inputs;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/*
    Small helper to register key press/release actions over Swing Components.
    This is intentional, creates an easily overridable InputMap/ActionMap usage.
 */
public class KeyboardInput {
    private final JComponent component;

    public KeyboardInput(JComponent component) {
        this.component = component;
    }

    // Binds a key stroke (ex: W) to press and release runnables.
    // The key stroke itself is bound to the component that calls it, makes it very easy to implement.
    public void bindKey(String keyStroke, String actionName, Runnable onPress, Runnable onRelease) {
        InputMap im = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = component.getActionMap();

        im.put(KeyStroke.getKeyStroke(keyStroke), actionName + "_press");
        im.put(KeyStroke.getKeyStroke("released " + keyStroke), actionName + "_release");

        am.put(actionName + "_press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { onPress.run(); }
        });

        am.put(actionName + "_release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { onRelease.run(); }
        });
    }
}



