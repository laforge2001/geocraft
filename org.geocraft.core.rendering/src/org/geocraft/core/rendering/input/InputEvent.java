package org.geocraft.core.rendering.input;

public abstract class InputEvent {
    public final long timestamp;
    protected InputEvent() { this.timestamp = System.nanoTime(); }
}
