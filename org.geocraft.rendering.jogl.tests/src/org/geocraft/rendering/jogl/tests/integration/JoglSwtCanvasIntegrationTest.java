package org.geocraft.rendering.jogl.tests.integration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.rendering.jogl.JoglRenderBackend;
import org.geocraft.rendering.jogl.JoglSwtCanvas;
import org.joml.Vector3f;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Validates that JoglSwtCanvas can be created in a SWT Shell and render
 * a frame without errors. This is the critical test proving JOGL works
 * natively on Apple Silicon — the volume viewer uses exactly this path.
 */
public class JoglSwtCanvasIntegrationTest {

    /**
     * JOGL requires native library loading with a specific dependency chain
     * (gluegen_rt, nativewindow_macosx, libjawt from the JDK, nativewindow_awt).
     * Getting this to work reliably inside Tycho's surefire-OSGi runtime on
     * Apple Silicon macOS is brittle: libnativewindow_awt.dylib links against
     * {@code @rpath/libjawt.dylib}, and macOS SIP strips DYLD_LIBRARY_PATH
     * from inherited environments. The production GeoCraft launcher controls
     * its own native library environment, so this integration test is
     * gated on a system property and only runs when explicitly requested.
     * Run manually with:
     * {@code mvn -pl org.geocraft.rendering.jogl.tests test -Dgeocraft.jogl.integration=true}
     */
    private static boolean shouldRun() {
        return Boolean.getBoolean("geocraft.jogl.integration");
    }

    @Test
    public void canvasInitializesAndRendersFrame() {
        Assume.assumeTrue("integration test disabled; set -Dgeocraft.jogl.integration=true to run", shouldRun());
        final boolean[] ok = { false };
        final Throwable[] err = { null };

        Display display = Display.getDefault();
        display.syncExec(() -> {
            Shell shell = new Shell(display, SWT.NONE);
            try {
                shell.setSize(256, 256);
                shell.setLayout(new FillLayout());
                JoglSwtCanvas canvas = new JoglSwtCanvas(shell);

                // Force the canvas to be sized and realized
                shell.open();
                shell.layout();
                while (display.readAndDispatch()) { /* pump events */ }

                assertFalse("canvas should not be disposed", canvas.getSwtControl().isDisposed());
                assertTrue("canvas width should be > 0", canvas.getWidth() > 0);
                assertTrue("canvas height should be > 0", canvas.getHeight() > 0);

                // Initialize backend on this canvas and render one frame
                RenderBackend backend = new JoglRenderBackend();
                backend.initialize(canvas);

                GroupNode root = new GroupNode("root");
                Camera cam = new Camera();
                cam.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
                cam.setLocation(new Vector3f(0, 0, 10));
                cam.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
                cam.setViewport(canvas.getWidth(), canvas.getHeight());

                backend.renderPass(root, cam, null);
                canvas.swapBuffers();

                // Pump once more to let SWT process the redraw
                while (display.readAndDispatch()) { /* pump events */ }

                canvas.dispose();
                backend.dispose();
                ok[0] = true;
            } catch (Throwable t) {
                err[0] = t;
            } finally {
                if (!shell.isDisposed()) shell.dispose();
            }
        });

        if (err[0] != null) {
            throw new AssertionError("JoglSwtCanvas integration failed", err[0]);
        }
        assertTrue("integration test did not complete successfully", ok[0]);
    }
}
