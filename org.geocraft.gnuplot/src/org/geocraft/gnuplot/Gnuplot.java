/**
 * 
 */
package org.geocraft.gnuplot;

import org.geocraft.core.service.ServiceProvider;

import com.panayotis.gnuplot.GNUPlotException;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.layout.StripeLayout;

/**
 * @author georde
 * 
 */
public class Gnuplot {

	private JavaPlot _jp;

	/**
	 * create the instance of Gnuplot to interact with
	 * 
	 */
	public Gnuplot() {
		try {
			_jp = new JavaPlot();
			_jp.setPersist(false);
		} catch (GNUPlotException e) {
			ServiceProvider.getLoggingService().getLogger(getClass()).fatal("gnuplot not found", e);
		}

	}

	public void exec(String command) {
		_jp.addPlot(command);

		try {
			_jp.plot();
		} catch (GNUPlotException e) {
			ServiceProvider.getLoggingService().getLogger(getClass()).fatal("Error plotting", e);
		}

	}

	@Override
	public void finalize() {
		_jp.setPersist(false);
	}

	/*
	 * This demo code uses default terminal. Use it as reference for other
	 * javaplot arguments
	 */
	private static JavaPlot defaultTerminal(String gnuplotpath) {
		JavaPlot p = new JavaPlot(gnuplotpath);
		// JavaPlot.getDebugger().setLevel(Debug.VERBOSE);

		p.setTitle("Default Terminal Title");
		p.getAxis("x").setLabel("X axis", "Arial", 20);
		p.getAxis("y").setLabel("Y axis");

		p.getAxis("x").setBoundaries(-30, 20);
		p.setKey(JavaPlot.Key.TOP_RIGHT);

		p.addPlot("sin(x)");
		p.plot();

		p.newGraph();
		p.addPlot("cos(x)");
		p.plot();

		p.newGraph3D();
		double[][] plot3d = { { 1, 1.1, 3 }, { 2, 2.2, 3 }, { 3, 3.3, 3.4 },
				{ 4, 4.3, 5 } };
		p.addPlot(plot3d);

		p.newGraph3D();
		p.addPlot("sin(x)*sin(y)");

		p.setMultiTitle("Global test title");
		StripeLayout lo = new StripeLayout();
		lo.setColumns(9999);
		p.getPage().setLayout(lo);
		p.plot();

		return p;
	}

	public static void main(String[] args) {
		Gnuplot gp = new Gnuplot();
		gp.exec("sin(x)");
	}

	/**
	 * @param b
	 */
	public void setPersist(boolean b) {
		_jp.setPersist(false);

	}
}
