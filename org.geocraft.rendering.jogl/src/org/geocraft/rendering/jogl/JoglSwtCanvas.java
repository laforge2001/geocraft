package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.rendering.backend.RenderSurface;

public class JoglSwtCanvas implements RenderSurface {
    private final GLCanvas canvas;

    public JoglSwtCanvas(Composite parent) {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);
        caps.setDepthBits(24);
        this.canvas = new GLCanvas(parent, SWT.NONE, caps, null);
    }

    public GLCanvas getSwtCanvas() { return canvas; }

    @Override public int getWidth() {
        return canvas.isDisposed() ? 0 : canvas.getSize().x;
    }

    @Override public int getHeight() {
        return canvas.isDisposed() ? 0 : canvas.getSize().y;
    }

    @Override public void makeCurrent() {
        GLContext ctx = canvas.getContext();
        if (ctx != null && !ctx.isCurrent()) ctx.makeCurrent();
    }

    @Override public void release() {
        GLContext ctx = canvas.getContext();
        if (ctx != null && ctx.isCurrent()) ctx.release();
    }

    @Override public void swapBuffers() {
        if (!canvas.isDisposed()) canvas.swapBuffers();
    }

    @Override public void dispose() {
        if (!canvas.isDisposed()) canvas.dispose();
    }
}
