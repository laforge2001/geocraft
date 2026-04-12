package org.geocraft.rendering.jogl;

import java.io.File;
import java.net.URL;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.swt.NewtCanvasSWT;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.geocraft.core.rendering.backend.RenderSurface;
import org.osgi.framework.Bundle;

/**
 * RenderSurface backed by JOGL's NewtCanvasSWT — a NEWT GLWindow
 * embedded inside an SWT Composite.
 *
 * This avoids two broken paths on macOS aarch64:
 * - SWT's org.eclipse.swt.opengl.GLCanvas crashes with
 *   NSGraphicsContext null in Widget.drawRect
 * - JOGL's com.jogamp.opengl.swt.GLCanvas has a DPIUtil API
 *   mismatch with Eclipse 2025-12's SWT
 *
 * NewtCanvasSWT uses JOGL's own NEWT windowing (NSOpenGLView) and
 * embeds it as a child of the SWT Composite, bypassing both issues.
 */
public class JoglSwtCanvas implements RenderSurface {
    private static boolean nativesConfigured = false;

    private static synchronized void ensureNativesConfigured() {
        if (nativesConfigured) return;
        nativesConfigured = true;

        // In Eclipse PDE dev mode, Bundle-NativeCode doesn't apply.
        // Find the jogl.bundle project on disk and set java.library.path.
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

    private final GLWindow glWindow;
    private final NewtCanvasSWT newtCanvas;

    public JoglSwtCanvas(Composite parent) {
        ensureNativesConfigured();
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setDoubleBuffered(true);
        caps.setDepthBits(24);
        glWindow = GLWindow.create(caps);
        newtCanvas = NewtCanvasSWT.create(parent, SWT.NONE, glWindow);
        System.out.println("[JoglSwtCanvas] NewtCanvasSWT created successfully");
    }

    /** Get the NEWT GLWindow for adding GLEventListeners or direct GL access. */
    public GLWindow getGLWindow() { return glWindow; }

    /** Get the SWT control for layout/input purposes. */
    public Control getSwtControl() { return newtCanvas; }

    @Override public int getWidth() {
        return newtCanvas.isDisposed() ? 0 : newtCanvas.getSize().x;
    }

    @Override public int getHeight() {
        return newtCanvas.isDisposed() ? 0 : newtCanvas.getSize().y;
    }

    @Override public void makeCurrent() {
        glWindow.getContext().makeCurrent();
    }

    @Override public void release() {
        if (glWindow.getContext().isCurrent()) {
            glWindow.getContext().release();
        }
    }

    @Override public void swapBuffers() {
        glWindow.swapBuffers();
    }

    /** Trigger a NEWT display cycle (calls GLEventListener.display). */
    public void display() {
        if (!newtCanvas.isDisposed()) {
            glWindow.display();
        }
    }

    @Override public void dispose() {
        glWindow.destroy();
        if (!newtCanvas.isDisposed()) {
            newtCanvas.dispose();
        }
    }
}
