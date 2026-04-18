package org.geocraft.rendering.jogl;

import org.geocraft.core.rendering.backend.TextureHandle;

public class JoglTextureHandle implements TextureHandle {
    private int glTextureId;
    private final int width;
    private final int height;
    private boolean disposed;

    public JoglTextureHandle(int glTextureId, int width, int height) {
        this.glTextureId = glTextureId;
        this.width = width;
        this.height = height;
    }

    public int getGlId() { return glTextureId; }
    public void markDisposed() { this.disposed = true; this.glTextureId = 0; }

    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override public boolean isDisposed() { return disposed; }
}
