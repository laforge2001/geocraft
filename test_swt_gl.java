import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Rectangle;

class test_swt_gl {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT GL Test");
        shell.setSize(640, 480);
        shell.setLayout(new FillLayout());

        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;
        GLCanvas canvas = new GLCanvas(shell, SWT.NONE, data);

        // Intercept ALL paint events at the Display level to call setCurrent
        // before SWT's internal drawRect can fire
        display.addFilter(SWT.Paint, new Listener() {
            public void handleEvent(Event event) {
                if (event.widget == canvas && !canvas.isDisposed()) {
                    canvas.setCurrent();
                }
            }
        });

        shell.open();
        canvas.setCurrent(); // Make current immediately
        System.out.println("Shell open, GL current.");

        int frame = 0;
        while (!shell.isDisposed()) {
            if (!canvas.isDisposed()) {
                canvas.setCurrent();
                canvas.swapBuffers();
                if (frame == 0) System.out.println("First frame OK!");
                if (frame % 100 == 0 && frame > 0) System.out.println("Frame " + frame);
                frame++;
            }
            if (!display.readAndDispatch()) display.sleep();
        }
        System.out.println(frame + " frames. Done.");
        display.dispose();
    }
}
