package org.geocraft.rendering.jogl;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Control;
import org.geocraft.core.rendering.input.InputListener;
import org.geocraft.core.rendering.input.KeyInputEvent;
import org.geocraft.core.rendering.input.MouseInputEvent;

public class SwtInputAdapter implements MouseListener, MouseMoveListener, MouseWheelListener, KeyListener {
    private final List<InputListener> listeners = new ArrayList<>();
    private boolean leftDown, middleDown, rightDown;

    public SwtInputAdapter(Control control) {
        control.addMouseListener(this);
        control.addMouseMoveListener(this);
        control.addMouseWheelListener(this);
        control.addKeyListener(this);
    }

    public void addListener(InputListener l) { listeners.add(l); }
    public void removeListener(InputListener l) { listeners.remove(l); }

    private MouseInputEvent.Button toButton(int b) {
        if (b == 1) return MouseInputEvent.Button.LEFT;
        if (b == 2) return MouseInputEvent.Button.MIDDLE;
        if (b == 3) return MouseInputEvent.Button.RIGHT;
        return MouseInputEvent.Button.NONE;
    }

    private boolean has(int stateMask, int flag) { return (stateMask & flag) != 0; }

    @Override public void mouseDown(MouseEvent e) {
        if (e.button == 1) leftDown = true;
        else if (e.button == 2) middleDown = true;
        else if (e.button == 3) rightDown = true;
        fire(new MouseInputEvent(MouseInputEvent.Kind.PRESS, toButton(e.button),
                 e.x, e.y, 0,
                 has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void mouseUp(MouseEvent e) {
        if (e.button == 1) leftDown = false;
        else if (e.button == 2) middleDown = false;
        else if (e.button == 3) rightDown = false;
        fire(new MouseInputEvent(MouseInputEvent.Kind.RELEASE, toButton(e.button),
                 e.x, e.y, 0,
                 has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void mouseDoubleClick(MouseEvent e) { /* no-op */ }

    @Override public void mouseMove(MouseEvent e) {
        MouseInputEvent.Kind kind = (leftDown || middleDown || rightDown)
            ? MouseInputEvent.Kind.DRAG : MouseInputEvent.Kind.MOVE;
        MouseInputEvent.Button b = leftDown ? MouseInputEvent.Button.LEFT
                                  : middleDown ? MouseInputEvent.Button.MIDDLE
                                  : rightDown  ? MouseInputEvent.Button.RIGHT
                                  : MouseInputEvent.Button.NONE;
        fire(new MouseInputEvent(kind, b, e.x, e.y, 0,
                 has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void mouseScrolled(MouseEvent e) {
        fire(new MouseInputEvent(MouseInputEvent.Kind.WHEEL, MouseInputEvent.Button.NONE,
                 e.x, e.y, e.count,
                 has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void keyPressed(KeyEvent e) {
        fire(new KeyInputEvent(KeyInputEvent.Kind.PRESS, e.keyCode, e.character,
            has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void keyReleased(KeyEvent e) {
        fire(new KeyInputEvent(KeyInputEvent.Kind.RELEASE, e.keyCode, e.character,
            has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    private void fire(MouseInputEvent e) { for (InputListener l : listeners) l.onMouse(e); }
    private void fire(KeyInputEvent e)   { for (InputListener l : listeners) l.onKey(e); }
}
