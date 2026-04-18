package org.geocraft.core.rendering.input;

public class KeyInputEvent extends InputEvent {
    public enum Kind { PRESS, RELEASE }
    public final Kind kind;
    public final int keyCode;
    public final char character;
    public final boolean shift;
    public final boolean ctrl;
    public final boolean alt;

    public KeyInputEvent(Kind kind, int keyCode, char character, boolean shift, boolean ctrl, boolean alt) {
        this.kind = kind;
        this.keyCode = keyCode;
        this.character = character;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
    }
}
