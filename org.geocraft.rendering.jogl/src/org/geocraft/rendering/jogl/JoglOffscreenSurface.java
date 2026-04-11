package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.geocraft.core.rendering.backend.RenderSurface;

public class JoglOffscreenSurface implements RenderSurface {
    private final GLOffscreenAutoDrawable drawable;
    private final int width;
    private final int height;

    public JoglOffscreenSurface(int width, int height) {
        this.width = width;
        this.height = height;
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setHardwareAccelerated(true);
        caps.setDoubleBuffered(false);
        caps.setAlphaBits(8);
        caps.setDepthBits(24);
        caps.setFBO(true);
        this.drawable = GLDrawableFactory.getFactory(profile)
            .createOffscreenAutoDrawable(null, caps, null, width, height);
        drawable.display(); // forces context creation
    }

    public GLOffscreenAutoDrawable getDrawable() { return drawable; }

    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }

    @Override public void makeCurrent() {
        drawable.getContext().makeCurrent();
    }

    @Override public void release() {
        if (drawable.getContext().isCurrent()) drawable.getContext().release();
    }

    @Override public void swapBuffers() { /* no-op for offscreen */ }

    @Override public void dispose() {
        drawable.destroy();
    }
}
