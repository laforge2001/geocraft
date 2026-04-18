package org.geocraft.core.rendering.material;

public final class WireframeMode {
    public final boolean enabled;
    public final float lineWidth;
    public final boolean antialiased;

    public WireframeMode(boolean enabled, float lineWidth, boolean antialiased) {
        this.enabled = enabled;
        this.lineWidth = lineWidth;
        this.antialiased = antialiased;
    }
}
