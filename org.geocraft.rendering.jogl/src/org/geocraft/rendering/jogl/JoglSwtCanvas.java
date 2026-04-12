package org.geocraft.rendering.jogl;

import java.io.File;
import java.net.URL;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.rendering.backend.RenderSurface;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class JoglSwtCanvas implements RenderSurface {
    private static boolean nativesConfigured = false;

    /**
     * Ensure JOGL native libraries are findable before any GL calls.
     * In a built product, Bundle-NativeCode handles this. In Eclipse PDE
     * dev mode, we need to set java.library.path to point at the natives
     * directory inside the org.geocraft.jogl.bundle workspace project.
     */
    private static synchronized void ensureNativesConfigured() {
        if (nativesConfigured) return;
        nativesConfigured = true;
        try {
            // Find the org.geocraft.jogl.bundle's location on disk
            Bundle joglBundle = null;
            for (Bundle b : FrameworkUtil.getBundle(JoglSwtCanvas.class).getBundleContext().getBundles()) {
                if ("org.geocraft.jogl.bundle".equals(b.getSymbolicName())) {
                    joglBundle = b;
                    break;
                }
            }
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
                            // Force ClassLoader to re-read java.library.path
                            try {
                                java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                                fieldSysPath.setAccessible(true);
                                fieldSysPath.set(null, null);
                            } catch (Exception e) {
                                // Not available on all JVMs — fall through
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

        // Pre-load jawt so libnativewindow_awt can find it via @rpath
        try {
            System.loadLibrary("jawt");
        } catch (Throwable ignore) {
        }

        // Initialize JOGL profiles
        try {
            GLProfile.initSingleton();
        } catch (Throwable t) {
            System.err.println("[JoglSwtCanvas] GLProfile.initSingleton() failed: " + t.getMessage());
        }
    }

    private final GLCanvas canvas;

    public JoglSwtCanvas(Composite parent) {
        ensureNativesConfigured();
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
        if (canvas.isDisposed()) return;
        try {
            GLContext ctx = canvas.getContext();
            if (ctx != null && !ctx.isCurrent()) ctx.makeCurrent();
        } catch (Exception e) {
            // Context not yet available — widget may not be realized
        }
    }

    @Override public void release() {
        if (canvas.isDisposed()) return;
        try {
            GLContext ctx = canvas.getContext();
            if (ctx != null && ctx.isCurrent()) ctx.release();
        } catch (Exception e) {
            // ignore
        }
    }

    @Override public void swapBuffers() {
        if (!canvas.isDisposed()) canvas.swapBuffers();
    }

    @Override public void dispose() {
        if (!canvas.isDisposed()) canvas.dispose();
    }
}
