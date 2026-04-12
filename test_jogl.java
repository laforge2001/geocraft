import com.jogamp.opengl.*;
import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class test_jogl {
    public static void main(String[] args) {
        System.out.println("Test: JOGL SWT GLCanvas on macOS aarch64");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("JOGL SWT Test");
        shell.setSize(640, 480);
        shell.setLayout(new FillLayout());

        System.out.println("Getting GL profile...");
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        System.out.println("Profile: " + profile);

        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);
        caps.setDepthBits(24);

        System.out.println("Creating JOGL GLCanvas...");
        GLCanvas canvas = new GLCanvas(shell, SWT.NO_BACKGROUND, caps, null);
        System.out.println("Canvas created!");

        canvas.addGLEventListener(new GLEventListener() {
            int frame = 0;
            public void init(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                gl.glClearColor(0.2f, 0.3f, 0.8f, 1f);
                System.out.println("GL init: " + gl.glGetString(GL.GL_RENDERER));
            }
            public void display(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                if (frame == 0) System.out.println("First frame rendered!");
                frame++;
            }
            public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {}
            public void dispose(GLAutoDrawable d) {}
        });

        shell.open();
        System.out.println("Shell open. Entering event loop...");

        Runnable renderLoop = new Runnable() {
            public void run() {
                if (!canvas.isDisposed()) {
                    canvas.display();
                    display.timerExec(33, this);
                }
            }
        };
        display.timerExec(100, renderLoop);

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        System.out.println("Done.");
        display.dispose();
    }
}
