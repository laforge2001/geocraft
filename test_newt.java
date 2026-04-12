import com.jogamp.opengl.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.swt.NewtCanvasSWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class test_newt {
    public static void main(String[] args) {
        System.out.println("Test: JOGL NewtCanvasSWT");

        // Create SWT shell
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("NEWT in SWT Test");
        shell.setSize(640, 480);
        shell.setLayout(new FillLayout());

        // Create JOGL NEWT window
        System.out.println("Creating GLWindow...");
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setDoubleBuffered(true);
        GLWindow glWindow = GLWindow.create(caps);

        // Embed in SWT
        System.out.println("Embedding in SWT...");
        NewtCanvasSWT newtCanvas = NewtCanvasSWT.create(shell, SWT.NONE, glWindow);

        glWindow.addGLEventListener(new GLEventListener() {
            int frame = 0;
            public void init(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                gl.glClearColor(0.1f, 0.2f, 0.4f, 1f);
                System.out.println("GL init: " + gl.glGetString(GL.GL_RENDERER));
            }
            public void display(GLAutoDrawable d) {
                GL2 gl = d.getGL().getGL2();
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                gl.glBegin(GL2.GL_TRIANGLES);
                gl.glColor3f(1, 0, 0); gl.glVertex2f(-0.5f, -0.5f);
                gl.glColor3f(0, 1, 0); gl.glVertex2f( 0.5f, -0.5f);
                gl.glColor3f(0, 0, 1); gl.glVertex2f( 0.0f,  0.5f);
                gl.glEnd();
                if (frame == 0) System.out.println("First frame rendered!");
                frame++;
            }
            public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
                d.getGL().getGL2().glViewport(0, 0, w, h);
            }
            public void dispose(GLAutoDrawable d) {}
        });

        shell.open();
        System.out.println("Shell open.");

        // Render loop
        display.timerExec(100, new Runnable() {
            public void run() {
                if (!newtCanvas.isDisposed()) {
                    glWindow.display();
                    display.timerExec(33, this);
                }
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        glWindow.destroy();
        System.out.println("Done.");
        display.dispose();
    }
}
