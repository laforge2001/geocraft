package org.geocraft.ui.chartviewer.renderer.polar;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.chartviewer.ChartViewerFactory;
import org.geocraft.ui.chartviewer.ScatterChartViewer;
import org.geocraft.ui.multiplot.MultiPlotFactory;
import org.geocraft.ui.multiplot.MultiPlotPart;


public class PolarChartTask extends BackgroundTask {

  protected PolarChartModel _model;

  public PolarChartTask(PolarChartModel model) {
    _model = model;
  }

  @Override
  public Object compute(final ILogger logger, final IProgressMonitor monitor) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        int numColumns = 1;
        MultiPlotPart part;
        try {
          part = MultiPlotFactory.createPart(numColumns);
          part.setPartTitle(_model.getPlotTitle());
        } catch (PartInitException ex) {
          logger.error(ex.toString(), ex);
          monitor.done();
          return;
        }

        // Change it so the plot size is the same in both X and Y directions
        Composite composite = part.getViewerParent();
        Point point = composite.getSize();
        double minSize = Math.min(point.x, point.y);
        int xSize = (int) Math.rint(minSize + minSize * 0.44);
        int ySize = (int) Math.rint(minSize);
        Point newPoint = new Point(xSize, ySize);
        composite.setSize(newPoint);

        // Create a scatter chart and add it to the multi-chart part.
        ScatterChartViewer scatterChart = ChartViewerFactory.createScatterChart(part.getViewerParent(), _model
            .getPlotHeading(), _model.getXLabel(), _model.getYLabel());
        scatterChart.setMainLayerName("Polar Plot");

        // Make sure the Layer tree is visible
        scatterChart.setLayerTreeVisible(true);

        part.addViewer(scatterChart);

        // Set the maximum number of circles
        int mxCircles = 500;
        int nCircles = mxCircles;

        // determine the maximum # of angles
        int maxAngleDegs = 360;
        int angleIncrDegs = 5;
        int mxAngles = (int) Math.rint(maxAngleDegs / angleIncrDegs);
        int numAngles = mxAngles;

        // Set the number of circles based on number of rows
        Grid3d inputGrid = _model.getGrid();
        double zDelta = 1;
        Unit zUnit = Unit.METER;
        if (inputGrid != null) {
          nCircles = inputGrid.getNumRows();
          numAngles = inputGrid.getNumColumns();
          zUnit = inputGrid.getDataUnit();
          zDelta = (float) inputGrid.getGeometry().getColumnSpacing();
          // set the number of circles based on number of trace values
        } else {
          PostStack3d volume = _model.getVolume();
          if (volume != null) {
            nCircles = volume.getNumSamplesPerTrace();
            numAngles = volume.getNumXlines();
            // Determine the zDelta for the volume
            zUnit = volume.getZUnit();
            zDelta = volume.getZDelta();
          }
        }

        // Set a maximum for the number of circles
        if (nCircles > mxCircles) {
          nCircles = mxCircles;
        }

        // Set a maximum for the number of angles
        if (numAngles > mxAngles) {
          numAngles = mxAngles;
        }

        // Determine the angle increment based on the number of angles
        if (numAngles < mxAngles) {
          angleIncrDegs = maxAngleDegs / numAngles;
        }

        // Number of points around circle
        int nCirclePnts = 500;

        // determine the angle increment in degrees
        double angleIncr = 2 * Math.PI / numAngles;

        // Increase line at each direction so users sees the whole plot
        int nDirections = 4;

        // Create angles around a circle
        double minValue = 0;
        double maxValue = 2 * Math.PI;
        double startValue = maxValue / 4;
        double incr = maxValue / (nCirclePnts - 1);
        int nVals = (int) Math.rint(maxValue / incr) + 1;

        // Determine number of circles based on plotting every 4 Kilometers
        double maxZValue = zDelta * nCircles;
        double circleDist = 4000.0 / zUnit.getScale();
        int circlesToPlot = (int) Math.floor(maxZValue / circleDist);

        // Reduce circle to 1 Kilometer if only one circle
        if (circlesToPlot <= 1) {
          circleDist = 1000.0 / zUnit.getScale();
          circlesToPlot = (int) Math.floor(maxZValue / circleDist);
        }

        // Reduce circle to 1 Meter if only one circle
        if (circlesToPlot <= 1) {
          circleDist = 1.0 / zUnit.getScale();
          circlesToPlot = (int) Math.floor(maxZValue / circleDist);
        }

        float[][] xCirclePnts = new float[nVals][circlesToPlot];
        float[][] yCirclePnts = new float[nVals][circlesToPlot];

        // Plot every circle that is 4 Kilometers
        for (int i1 = 0; i1 < circlesToPlot; i1++) {
          double angle = minValue;
          for (int i2 = 0; i2 < nVals; i2++) {
            // Convert the angle into a plot angle
            double plotAngle = startValue - angle;
            if (plotAngle < 0) {
              plotAngle = plotAngle + maxValue;
            }

            // set points for the circle
            double zVal = (i1 + 1) * circleDist;
            xCirclePnts[i2][i1] = (float) (Math.cos(plotAngle) * zVal);
            yCirclePnts[i2][i1] = (float) (Math.sin(plotAngle) * zVal);
            angle = (i2 + 1) * incr;
          }
        }

        // Draw lines for each angle around a circle
        double angle = minValue;
        int nLinePnts = 2;
        int nLines = numAngles;
        float[][] xs = new float[nLinePnts][numAngles];
        float[][] ys = new float[nLinePnts][numAngles];
        for (int i1 = 0; i1 < numAngles; i1++) {
          // Convert the angle into a plot angle
          double plotAngle = startValue - angle;
          if (plotAngle < 0) {
            plotAngle = plotAngle + maxValue;
          }

          // Set the current line
          xs[0][i1] = 0;
          ys[0][i1] = 0;
          double zVal = nCircles * zDelta;

          // Increase the value at directions so user sees the whole plot
          int directionLoc = i1 % (numAngles / nDirections);
          if (directionLoc == 0) {
            zVal = (nCircles + 1) * zDelta;
          }

          xs[1][i1] = (float) (Math.cos(plotAngle) * zVal);
          ys[1][i1] = (float) (Math.sin(plotAngle) * zVal);
          angle = (i1 + 1) * angleIncr;
        }

        int nPnts = nCirclePnts * circlesToPlot + nLinePnts * nLines;
        float[] xs1 = new float[nPnts];
        float[] ys1 = new float[nPnts];
        int pntIndx = 0;
        for (int i2 = 0; i2 < circlesToPlot; i2++) {
          for (int i1 = 0; i1 < nCirclePnts; i1++) {
            xs1[pntIndx] = xCirclePnts[i1][i2];
            ys1[pntIndx] = yCirclePnts[i1][i2];
            pntIndx++;
          }
        }
        for (int i2 = 0; i2 < nLines; i2++) {
          for (int i1 = 0; i1 < nLinePnts; i1++) {
            xs1[pntIndx] = xs[i1][i2];
            ys1[pntIndx] = ys[i1][i2];
            pntIndx++;
          }
        }
        scatterChart.addData("Polar Plot Axis", xs1, ys1, _model.getPlotColor(), _model.getScatterPointStyle(), _model
            .getScatterPointSize(), true);

        // Set the Annotation density of the plot to 10
        scatterChart.getPlot().setHorizontalAxisAnnotationDensity(10);
        scatterChart.getPlot().setVerticalAxisAnnotationDensity(10);

        // determine the angle to plot labels
        angle = Math.PI * 3.0 / 4.0;

        // Convert the angle into a plot angle
        double labelAngle = startValue - angle;
        if (labelAngle < 0) {
          labelAngle = labelAngle + maxValue;
        }

        // Determine an index to how many labels to plot
        int circleIndx = 1;
        circleIndx = (int) Math.ceil(circlesToPlot / 20.0);

        // Add a label for each direction
        nDirections = 4;

        // Initialize for labels
        int nLabelPnts = circlesToPlot / circleIndx + nDirections;
        float[] xLabelPnts = new float[nLabelPnts];
        float[] yLabelPnts = new float[nLabelPnts];
        String[] labels = new String[nLabelPnts];

        // Convert the angle into a plot angle
        double plotAngle = startValue - angle;
        if (plotAngle < 0) {
          plotAngle = plotAngle + maxValue;
        }

        // Plot a label for each circle (Based on the # of Kilometers of each circle)
        int plotIndx = 0;
        if (circleDist * zUnit.getScale() / 1000.0 >= 1.0) {
          for (int i1 = 0; i1 < circlesToPlot; i1++) {
            if (circleIndx == 1 || (i1 + 1) % circleIndx == 0) {
              // set the label for the current circle
              double zVal = (i1 + 1) * circleDist;
              xLabelPnts[plotIndx] = (float) (Math.cos(plotAngle) * zVal);
              yLabelPnts[plotIndx] = (float) (Math.sin(plotAngle) * zVal);

              double value = zVal * zUnit.getScale() / 1000.0;
              labels[plotIndx] = Double.toString(value);
              if (i1 + circleIndx >= circlesToPlot) {
                labels[plotIndx] = Double.toString(value) + "Km";
              }
              plotIndx++;
            }
          }
          // Plot a label for each circle (Based on the # of Meters of each circle)
        } else {
          for (int i1 = 0; i1 < circlesToPlot; i1++) {
            // set the label for the current circle
            if (circleIndx == 1 || (i1 + 1) % circleIndx == 0) {
              double zVal = (i1 + 1) * circleDist;
              xLabelPnts[plotIndx] = (float) (Math.cos(plotAngle) * zVal);
              yLabelPnts[plotIndx] = (float) (Math.sin(plotAngle) * zVal);

              double value = zVal * zUnit.getScale();
              labels[plotIndx] = Double.toString(value);
              if (i1 + circleIndx >= circlesToPlot) {
                labels[plotIndx] = Double.toString(value) + "M";
              }
              plotIndx++;
            }
          }
        }

        // determine the angle to plot labels
        angle = minValue;
        double angleDegs = minValue;
        angleIncr = 2 * Math.PI / nDirections;
        angleIncrDegs = maxAngleDegs / nDirections;

        // Plot a labels in degrees in North, South, East, and West direction
        for (int i1 = 0; i1 < nDirections; i1++) {
          // Convert the angle into a plot angle
          labelAngle = startValue - angle;
          if (labelAngle < 0) {
            labelAngle = labelAngle + maxValue;
          }

          // set the label for the current direction
          double zVal = circlesToPlot * circleDist;
          xLabelPnts[plotIndx] = (float) (Math.cos(labelAngle) * zVal);
          yLabelPnts[plotIndx] = (float) (Math.sin(labelAngle) * zVal);
          labels[plotIndx] = String.format("%.0f\u00B0", angleDegs);

          // Increment angle
          angle = angle + angleIncr;
          angleDegs = angleDegs + angleIncrDegs;
          plotIndx++;
        }

        scatterChart.addData("Plot Labels", xLabelPnts, yLabelPnts, labels, _model.getLabelColor(), _model
            .getLabelFont());

        // Set the cell data
        float[][] xCellPnts = _model.getXCellPnts();
        float[][] yCellPnts = _model.getYCellPnts();
        float[][] cellData = _model.getCellData();
        if (inputGrid != null) {
          scatterChart.addData(_model.getDataLabel(0), xCellPnts, yCellPnts, cellData, inputGrid);
        } else {
          PostStack3d volume = _model.getVolume();
          if (volume != null) {
            scatterChart.addData(_model.getDataLabel(0), xCellPnts, yCellPnts, cellData, volume);
          }
        }

        // Set the plot bounds if a plot boundary wanted
        if (_model.getPlotBoundsWanted()) {
          float[] plotBounds = _model.getPlotBounds();
          float xMin = plotBounds[0];
          float xMax = plotBounds[1];
          float yMin = plotBounds[2];
          float yMax = plotBounds[3];

          scatterChart.setBounds(xMin, xMax, yMin, yMax);
        }
      }

    });
    return null;
  }
}
