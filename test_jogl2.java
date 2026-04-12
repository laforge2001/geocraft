import com.jogamp.opengl.*;
import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class test_jogl2 {
    public static void main(String[] args) throws Exception {
        System.out.println("Test: init JOGL BEFORE SWT Display");

        // Init JOGL profiles before SWT takes over the main thread
        System.out.println("Calling GLProfile.initSingleton()...");
        GLProfile.initSingleton();
        System.out.println("GLProfile initialized: " + GLProfile.getDefault());

        System.out.println("Creating Display...");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("JOGL SWT Test");
        shell.setSize(640, 480);
        shell.setLayout(new FillLayout());

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);

        System.out.println("Creating JOGL GLCanvas...");
        GLCanvas canvas = new GLCanvas(shell, SWT.NO_BACKGROUND, caps, null);
        System.out.println("Canvas created!");

        canvas.addGLEventListener(new GLEventListener() {
            int frame = 0;
            public void init(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                gl.glClearColor(0.2f, 0.3f, 0.8f, 1f);
                System.out.println("GL init OK: " + gl.glGetString(GL.GL_RENDERER));
            }
            public void display(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                // Draw a triangle
                gl.glBegin(GL2.GL_TRIANGLES);
                gl.glColor3f(1, 0, 0); gl.glVertex2f(-0.5f, -0.5f);
                gl.glColor3f(0, 1, 0); gl.glVertex2f( 0.5f, -0.5f);
                gl.glColor3f(0, 0, 1); gl.glVertex2f( 0.0f,  0.5f);
                gl.glEnd();
                if (frame == 0) System.out.println("First frame with triangle rendered!");
                frame++;
            }
            public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
                d.getGL().getGL2().glViewport(0, 0, w, h);
            }
            public void dispose(GLAutoDrawable d) {}
        });

        shell.open();
        System.out.println("Shell open. Running...");

        display.timerExec(100, new Runnable() {
            public void run() {
                if (!canvas.isDisposed()) {
                    canvas.display();
                    display.timerExec(33, this);
                }
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        System.out.println("Done.");
        display.dispose();
    }
}
