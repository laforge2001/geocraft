package org.geocraft.core.rendering.input;

public interface InputListener {
    default void onMouse(MouseInputEvent e) {}
    default void onKey(KeyInputEvent e) {}
}
