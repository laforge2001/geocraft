import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class test_gl {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT OpenGL Test");
        shell.setSize(640, 480);
        shell.setLayout(new FillLayout());

        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;

        // Test different style combos
        int style = SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE;
        System.out.println("Creating GLCanvas with style: " + style);
        GLCanvas canvas = new GLCanvas(shell, style, data);

        // Override drawBackground to prevent 2D painting
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                canvas.setCurrent();
                // GL rendering would go here
                canvas.swapBuffers();
            }
        });

        shell.open();
        System.out.println("Shell open. Entering event loop...");

        int frame = 0;
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
            if (frame == 0) System.out.println("First event loop iteration OK");
            frame++;
        }
        System.out.println("Exited after " + frame + " iterations. Done.");
        display.dispose();
    }
}
