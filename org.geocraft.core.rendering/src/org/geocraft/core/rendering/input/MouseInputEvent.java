package org.geocraft.core.rendering.input;

public class MouseInputEvent extends InputEvent {
    public enum Kind { PRESS, RELEASE, MOVE, DRAG, WHEEL }
    public enum Button { NONE, LEFT, MIDDLE, RIGHT }

    public final Kind kind;
    public final Button button;
    public final int x;
    public final int y;
    public final int wheelDelta;
    public final boolean shift;
    public final boolean ctrl;
    public final boolean alt;

    public MouseInputEvent(Kind kind, Button button, int x, int y, int wheelDelta,
                           boolean shift, boolean ctrl, boolean alt) {
        this.kind = kind;
        this.button = button;
        this.x = x;
        this.y = y;
        this.wheelDelta = wheelDelta;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
    }
}
