package org.geocraft.rendering.jogl;

import java.io.File;
import java.net.URL;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.rendering.backend.RenderSurface;
import org.osgi.framework.Bundle;

/**
 * RenderSurface backed by SWT's native GLCanvas (not JOGL's).
 * SWT's GLCanvas creates an OpenGL context directly via Cocoa NSOpenGLView,
 * avoiding the JOGL GLProfile initialization that deadlocks on macOS when
 * called from the SWT event dispatch thread.
 *
 * JOGL's GL2 interface is obtained from the SWT-managed context via
 * GLContext.getCurrent() after makeCurrent().
 */
public class JoglSwtCanvas implements RenderSurface {
    private static boolean nativesConfigured = false;

    private static synchronized void ensureNativesConfigured() {
        if (nativesConfigured) return;
        nativesConfigured = true;
        try {
            Bundle joglBundle = org.eclipse.core.runtime.Platform.getBundle("org.geocraft.jogl.bundle");
            if (joglBundle != null) {
                URL bundleUrl = FileLocator.resolve(joglBundle.getEntry("/"));
                if (bundleUrl != null) {
                    File bundleDir = new File(bundleUrl.toURI());
                    File nativesDir = new File(bundleDir, "natives/macosx-universal");
                    if (nativesDir.isDirectory()) {
                        String existingPath = System.getProperty("java.library.path", "");
                        String nativePath = nativesDir.getAbsolutePath();
                        if (!existingPath.contains(nativePath)) {
                            System.setProperty("java.library.path",
                                nativePath + File.pathSeparator + existingPath);
                            try {
                                java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                                fieldSysPath.setAccessible(true);
                                fieldSysPath.set(null, null);
                            } catch (Exception e) {
                                // Not available on all JVMs
                            }
                            System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
                            System.out.println("[JoglSwtCanvas] Set native library path: " + nativePath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[JoglSwtCanvas] Could not configure native library path: " + e.getMessage());
        }
    }

    private final GLCanvas canvas;

    public JoglSwtCanvas(Composite parent) {
        ensureNativesConfigured();
        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;
        this.canvas = new GLCanvas(parent, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE, data);
        System.out.println("[JoglSwtCanvas] SWT GLCanvas created successfully");
    }

    public GLCanvas getSwtCanvas() { return canvas; }

    @Override public int getWidth() {
        return canvas.isDisposed() ? 0 : canvas.getSize().x;
    }

    @Override public int getHeight() {
        return canvas.isDisposed() ? 0 : canvas.getSize().y;
    }

    @Override public void makeCurrent() {
        if (canvas.isDisposed()) return;
        try {
            canvas.setCurrent();
        } catch (Exception e) {
            // Canvas not yet realized
        }
    }

    @Override public void release() {
        // SWT GLCanvas doesn't have an explicit release — context is
        // automatically released when another canvas calls setCurrent()
    }

    @Override public void swapBuffers() {
        if (!canvas.isDisposed()) canvas.swapBuffers();
    }

    @Override public void dispose() {
        if (!canvas.isDisposed()) canvas.dispose();
    }
}
