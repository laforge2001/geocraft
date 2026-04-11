package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.rendering.backend.RenderSurface;

public class JoglSwtCanvas implements RenderSurface {
    static {
        // JOGL's native libraries (libnativewindow_awt) link against
        // @rpath/libjawt.dylib from the JDK. When JOGL runs with
        // jogamp.gluegen.UseTempJarCache=false, it won't preload JAWT
        // itself, so the JNI method lookup fails later with
        // UnsatisfiedLinkError on JAWT_GetAWT1. Forcing System.loadLibrary
        // here pulls libjawt.dylib (and its JVM-side dependencies) into
        // the process early so JOGL's subsequent native loads resolve.
        try {
            System.loadLibrary("jawt");
        } catch (Throwable ignore) {
            // If jawt isn't available at load time (e.g. headless), let
            // JOGL surface its own error later.
        }
    }

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
