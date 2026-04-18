package org.geocraft.rendering.jogl;

import java.io.File;
import java.net.URL;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
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

        // Prevent JAWT/AWT deadlock on macOS: when -XstartOnFirstThread is
        // active, JOGL's GLProfile.initSingleton() tries to initialize AWT
        // which deadlocks the main thread. Headless mode skips AWT entirely.
        System.setProperty("java.awt.headless", "true");

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
                            System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Could not configure native library path; JOGL may still
            // succeed if natives are on the default library path.
        }
    }

    private final GLWindow glWindow;
    private final NewtCanvasSWT newtCanvas;
    private volatile int cachedWidth;
    private volatile int cachedHeight;
    private FPSAnimator animator;

    public JoglSwtCanvas(Composite parent) {
        ensureNativesConfigured();
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setDoubleBuffered(true);
        caps.setDepthBits(24);
        glWindow = GLWindow.create(caps);
        newtCanvas = NewtCanvasSWT.create(parent, SWT.NONE, glWindow);
        // Start hidden — NEWT windows on macOS take over their parent's
        // drawing area. Show only when data is added via setContentVisible(true).
        newtCanvas.setVisible(false);
    }

    /** Show or hide the NEWT canvas. Hidden by default to prevent the
     *  GL window from taking over the workbench on startup. */
    public void setContentVisible(boolean visible) {
        if (!newtCanvas.isDisposed()) {
            newtCanvas.setVisible(visible);
        }
    }

    /**
     * Callback interface for rendering. Hides JOGL GL2 type behind
     * the rendering.jogl package so consumers don't need JOGL imports.
     */
    public interface RenderCallback {
        void onInit(GL2 gl, String rendererName);
        void onDisplay(GL2 gl);
        void onReshape(GL2 gl, int width, int height);
    }

    /**
     * Set up a GLEventListener and start an FPSAnimator at the given rate.
     * The callback methods receive an already-current GL2 context.
     */
    public void startAnimator(int fps, RenderCallback callback) {
        glWindow.addGLEventListener(new GLEventListener() {
            @Override public void init(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                String renderer = gl.glGetString(GL.GL_RENDERER);
                callback.onInit(gl, renderer);
            }
            @Override public void display(GLAutoDrawable d) {
                callback.onDisplay(d.getGL().getGL2());
            }
            @Override public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
                cachedWidth = w;
                cachedHeight = h;
                callback.onReshape(d.getGL().getGL2(), w, h);
            }
            @Override public void dispose(GLAutoDrawable d) { }
        });
        animator = new FPSAnimator(glWindow, fps);
        animator.start();
    }

    /** Get the SWT control for layout purposes. */
    public Control getSwtControl() { return newtCanvas; }

    /**
     * Register input listeners on the NEWT GLWindow. NEWT captures its own
     * input events — SWT listeners on the parent composite won't receive them.
     */
    public void addInputListener(org.geocraft.core.rendering.input.InputListener listener) {
        glWindow.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) { }
            @Override public void mouseEntered(MouseEvent e) { }
            @Override public void mouseExited(MouseEvent e) { }

            @Override public void mousePressed(MouseEvent e) {
                listener.onMouse(new org.geocraft.core.rendering.input.MouseInputEvent(
                    org.geocraft.core.rendering.input.MouseInputEvent.Kind.PRESS,
                    newtButton(e.getButton()), e.getX(), e.getY(), 0,
                    e.isShiftDown(), e.isControlDown(), e.isAltDown()));
            }

            @Override public void mouseReleased(MouseEvent e) {
                listener.onMouse(new org.geocraft.core.rendering.input.MouseInputEvent(
                    org.geocraft.core.rendering.input.MouseInputEvent.Kind.RELEASE,
                    newtButton(e.getButton()), e.getX(), e.getY(), 0,
                    e.isShiftDown(), e.isControlDown(), e.isAltDown()));
            }

            @Override public void mouseMoved(MouseEvent e) {
                listener.onMouse(new org.geocraft.core.rendering.input.MouseInputEvent(
                    org.geocraft.core.rendering.input.MouseInputEvent.Kind.MOVE,
                    org.geocraft.core.rendering.input.MouseInputEvent.Button.NONE,
                    e.getX(), e.getY(), 0,
                    e.isShiftDown(), e.isControlDown(), e.isAltDown()));
            }

            @Override public void mouseDragged(MouseEvent e) {
                listener.onMouse(new org.geocraft.core.rendering.input.MouseInputEvent(
                    org.geocraft.core.rendering.input.MouseInputEvent.Kind.DRAG,
                    newtButton(e.getButton()), e.getX(), e.getY(), 0,
                    e.isShiftDown(), e.isControlDown(), e.isAltDown()));
            }

            @Override public void mouseWheelMoved(MouseEvent e) {
                listener.onMouse(new org.geocraft.core.rendering.input.MouseInputEvent(
                    org.geocraft.core.rendering.input.MouseInputEvent.Kind.WHEEL,
                    org.geocraft.core.rendering.input.MouseInputEvent.Button.NONE,
                    e.getX(), e.getY(), (int) e.getRotation()[1],
                    e.isShiftDown(), e.isControlDown(), e.isAltDown()));
            }
        });

        glWindow.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {
                listener.onKey(new org.geocraft.core.rendering.input.KeyInputEvent(
                    org.geocraft.core.rendering.input.KeyInputEvent.Kind.PRESS,
                    e.getKeyCode(), e.getKeyChar(),
                    e.isShiftDown(), e.isControlDown(), e.isAltDown()));
            }
            @Override public void keyReleased(KeyEvent e) {
                listener.onKey(new org.geocraft.core.rendering.input.KeyInputEvent(
                    org.geocraft.core.rendering.input.KeyInputEvent.Kind.RELEASE,
                    e.getKeyCode(), e.getKeyChar(),
                    e.isShiftDown(), e.isControlDown(), e.isAltDown()));
            }
        });
    }

    private static org.geocraft.core.rendering.input.MouseInputEvent.Button newtButton(short b) {
        switch (b) {
            case MouseEvent.BUTTON1: return org.geocraft.core.rendering.input.MouseInputEvent.Button.LEFT;
            case MouseEvent.BUTTON2: return org.geocraft.core.rendering.input.MouseInputEvent.Button.MIDDLE;
            case MouseEvent.BUTTON3: return org.geocraft.core.rendering.input.MouseInputEvent.Button.RIGHT;
            default: return org.geocraft.core.rendering.input.MouseInputEvent.Button.NONE;
        }
    }

    @Override public int getWidth() { return cachedWidth; }
    @Override public int getHeight() { return cachedHeight; }

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
        // Stop the animator first so its render thread doesn't try to call
        // display() on a destroyed GLWindow.
        if (animator != null && animator.isAnimating()) {
            animator.stop();
        }
        glWindow.destroy();
        if (!newtCanvas.isDisposed()) {
            newtCanvas.dispose();
        }
    }
}
