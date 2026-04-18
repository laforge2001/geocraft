package org.geocraft.core.rendering.backend;

public interface RenderSurface {
    int getWidth();
    int getHeight();
    void makeCurrent();
    void release();
    void swapBuffers();
    void dispose();
}
