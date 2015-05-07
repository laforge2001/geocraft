/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.action;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.ui.chartviewer.renderer.polar.PolarChartModel;
import org.geocraft.ui.repository.RepositoryViewData;


public class PolarPlotAction implements IWorkbenchWindowActionDelegate {

  public void dispose() {
    // Nothing to do.
  }

  public void init(final IWorkbenchWindow window) {
    // Nothing to do.
  }

  public void run(final IAction action) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {

        // Get the entities currently selected in the repository.
        Entity[] entities = RepositoryViewData.getSelectedEntities();

        // If no entities selected, simply return.
        if (entities.length < 1) {
          return;
        }

        // Create empty lists for grids
        List<Grid3d> grids = new ArrayList<Grid3d>();
        List<PostStack3d> volumes = new ArrayList<PostStack3d>();

        // Loop thru the selected entities, adding each one to the appropriate list.
        for (Entity entity : entities) {
          if (entity.getClass().equals(Grid3d.class)) {
            grids.add((Grid3d) entity);
          } else if (entity.getClass().equals(PostStack3d.class)) {
            volumes.add((PostStack3d) entity);
          }
        }

        // display the polar plot for the grids
        for (int i1 = 0; i1 < grids.size(); i1++) {
          Grid3d inputGrid = grids.get(i1);
          displayPlot(inputGrid);
        }

        // display the polar plot for the volumes
        for (int i1 = 0; i1 < volumes.size(); i1++) {
          PostStack3d inputVolume = volumes.get(i1);
          displayPlot(inputVolume);
        }
      }

    });
  }

  /**
   * Display the plot
   * @param input Volume
   */
  public void displayPlot(PostStack3d volume) {

    // Determine the zDelta for the volume
    float zDelta = volume.getZDelta();

    // Determine the maximum number of circles
    int mxCircles = 500;
    int numCircles = mxCircles;

    // Set the number of circles based on number of samples
    numCircles = volume.getNumSamplesPerTrace();
    if (numCircles > mxCircles) {
      numCircles = mxCircles;
    }

    // Determine the maximum number of angles
    int maxAngleDegs = 360;
    int angleIncrDegs = 5;
    int mxAngles = (int) Math.rint(maxAngleDegs / angleIncrDegs) + 1;
    int numAngles = mxAngles;

    // Set the number of X lines
    numAngles = volume.getNumXlines();
    if (numAngles > mxAngles) {
      numAngles = mxAngles;
    }

    // determine the cell data from the grid data
    float[][] cellData = readVolumeData(volume, null, mxCircles);

    // determine the angle increment
    double angleIncr = 2 * Math.PI / numAngles;

    // Determine the number values per angle
    int numAngleVals = numCircles;

    // Initialize to create angles around a circle
    double minValue = 0;
    double maxValue = 2 * Math.PI;
    double startValue = maxValue / 4;

    // Determine Polar plot cells
    int nCells = numAngles * numAngleVals;
    int nVals = 4;
    float[][] xCellPnts = new float[nVals][nCells];
    float[][] yCellPnts = new float[nVals][nCells];
    int cellIndx = 0;
    double angle = minValue;
    for (int i1 = 0; i1 < numAngles; i1++) {
      // Convert the current angle to an actual plot angle
      double plotAngle = startValue - angle;
      if (plotAngle < 0) {
        plotAngle = plotAngle + maxValue;
      }

      // determine the angle the next angle to plot
      double angle2 = angle + angleIncr;
      if (i1 + 1 == numAngles) {
        angle2 = minValue;
      }

      // Convert angle #2 to an actual plot angle
      double plotAngle2 = startValue - angle2;
      if (plotAngle2 < 0) {
        plotAngle2 = plotAngle2 + maxValue;
      }

      for (int i2 = 0; i2 < numAngleVals; i2++) {
        float zVal1 = i2 * zDelta;
        float zVal2 = (i2 + 1) * zDelta;
        xCellPnts[0][cellIndx] = (float) Math.cos(plotAngle) * zVal1;
        yCellPnts[0][cellIndx] = (float) Math.sin(plotAngle) * zVal1;
        xCellPnts[1][cellIndx] = (float) Math.cos(plotAngle2) * zVal1;
        yCellPnts[1][cellIndx] = (float) Math.sin(plotAngle2) * zVal1;
        xCellPnts[2][cellIndx] = (float) Math.cos(plotAngle2) * zVal2;
        yCellPnts[2][cellIndx] = (float) Math.sin(plotAngle2) * zVal2;
        xCellPnts[3][cellIndx] = (float) Math.cos(plotAngle) * zVal2;
        yCellPnts[3][cellIndx] = (float) Math.sin(plotAngle) * zVal2;
        cellIndx++;
      }
      angle = angle + angleIncr;
    }

    PolarChartModel polarModel = new PolarChartModel();

    // Set the title and Heading
    polarModel.setPlotTitle("Polar Plot");
    polarModel.setPlotHeading("Polar Plot");

    // Set the X and Y labels
    String zUnit = volume.getZUnit().getName();
    polarModel.setXLabel("Offset (" + zUnit + ")");
    polarModel.setYLabel("Offset (" + zUnit + ")");

    // Set the data label
    String[] dataLabels = { "Polar Plot Data" };
    polarModel.setDataLabels(dataLabels);

    // Set the data points
    polarModel.setXCellPnts(xCellPnts);
    polarModel.setYCellPnts(yCellPnts);
    polarModel.setCellData(cellData);

    // Set the volume
    polarModel.setVolume(volume);

    // Set the limits of the polar plot
    float zMaxVal = numAngleVals * zDelta;
    float xPlotMinT1 = -zMaxVal;
    float xPlotMaxT1 = zMaxVal;
    float yPlotMinT1 = -zMaxVal;
    float yPlotMaxT1 = zMaxVal;
    polarModel.setPlotBounds(xPlotMinT1, xPlotMaxT1, yPlotMinT1, yPlotMaxT1);
    polarModel.compute();
  }

  /**
   * Display the plot
   * @param input Grid
   */
  public void displayPlot(Grid3d inputGrid) {

    // Determine the column width
    float zDelta = (float) inputGrid.getGeometry().getColumnSpacing();

    // Determine the maximum number of circles
    int mxCircles = 500;
    int numCircles = mxCircles;

    // Set the number of circles based on number of samples
    numCircles = inputGrid.getNumRows();
    if (numCircles > mxCircles) {
      numCircles = mxCircles;
    }

    // Determine the maximum number of angles
    int maxAngleDegs = 360;
    int angleIncrDegs = 5;
    int mxAngles = (int) Math.rint(maxAngleDegs / angleIncrDegs);
    int numAngles = mxAngles;

    // Set the number of angles
    numAngles = inputGrid.getNumColumns();
    if (numAngles > mxAngles) {
      numAngles = mxAngles;
    }

    // determine the cell data from the grid data
    float[][] cellData = readGridData(inputGrid, null, mxCircles);

    // determine the angle increment
    double angleIncr = 2 * Math.PI / numAngles;

    // Determine the number values per angle
    int numAngleVals = numCircles;

    // Initialize to create angles around a circle
    double minValue = 0;
    double maxValue = 2 * Math.PI;
    double startValue = maxValue / 4;

    // Determine Polar plot cells
    int nCells = numAngles * numAngleVals;
    int nVals = 4;
    float[][] xCellPnts = new float[nVals][nCells];
    float[][] yCellPnts = new float[nVals][nCells];
    int cellIndx = 0;
    double angle = minValue;
    for (int i1 = 0; i1 < numAngles; i1++) {
      // Convert the current angle to an actual plot angle
      double plotAngle = startValue - angle;
      if (plotAngle < 0) {
        plotAngle = plotAngle + maxValue;
      }

      // determine the angle the next angle to plot
      double angle2 = angle + angleIncr;
      if (i1 + 1 == numAngles) {
        angle2 = minValue;
      }

      // Convert angle #2 to an actual plot angle
      double plotAngle2 = startValue - angle2;
      if (plotAngle2 < 0) {
        plotAngle2 = plotAngle2 + maxValue;
      }

      for (int i2 = 0; i2 < numAngleVals; i2++) {
        float zVal1 = i2 * zDelta;
        float zVal2 = (i2 + 1) * zDelta;
        xCellPnts[0][cellIndx] = (float) Math.cos(plotAngle) * zVal1;
        yCellPnts[0][cellIndx] = (float) Math.sin(plotAngle) * zVal1;
        xCellPnts[1][cellIndx] = (float) Math.cos(plotAngle2) * zVal1;
        yCellPnts[1][cellIndx] = (float) Math.sin(plotAngle2) * zVal1;
        xCellPnts[2][cellIndx] = (float) Math.cos(plotAngle2) * zVal2;
        yCellPnts[2][cellIndx] = (float) Math.sin(plotAngle2) * zVal2;
        xCellPnts[3][cellIndx] = (float) Math.cos(plotAngle) * zVal2;
        yCellPnts[3][cellIndx] = (float) Math.sin(plotAngle) * zVal2;
        cellIndx++;
      }
      angle = angle + angleIncr;
    }

    PolarChartModel polarModel = new PolarChartModel();

    // Set the title and Heading
    polarModel.setPlotTitle("Polar Plot");
    polarModel.setPlotHeading("Polar Plot");

    // Set the X and Y labels
    String zUnit = inputGrid.getDataUnit().getName();
    polarModel.setXLabel("Offset (" + zUnit + ")");
    polarModel.setYLabel("Offset (" + zUnit + ")");

    // Set the data label
    String[] dataLabels = { "Polar Plot Data" };
    polarModel.setDataLabels(dataLabels);

    // Set the data points
    polarModel.setXCellPnts(xCellPnts);
    polarModel.setYCellPnts(yCellPnts);
    polarModel.setCellData(cellData);

    // Set the grid
    polarModel.setGrid(inputGrid);

    // Set the limits of the polar plot
    float zMaxVal = numAngleVals * zDelta;
    float xPlotMinT1 = -zMaxVal;
    float xPlotMaxT1 = zMaxVal;
    float yPlotMinT1 = -zMaxVal;
    float yPlotMaxT1 = zMaxVal;
    polarModel.setPlotBounds(xPlotMinT1, xPlotMaxT1, yPlotMinT1, yPlotMaxT1);
    polarModel.compute();
  }

  /**
   * Determine values that should plotted on the polar plot
   * 
   * @param inputGrid the input Grid.
   * @param AreaOfInterest Area of Interest to display data
   * @return an array of values to display in the polar plot
   */
  public float[][] readGridData(final Grid3d inputGrid, final AreaOfInterest aoi, final int mxCircles) {

    float nullValue = inputGrid.getNullValue();
    GridGeometry3d geometry = inputGrid.getGeometry();

    // define number of circles and angles based on the number of rows and columns
    int numRows = inputGrid.getNumRows();
    int mxAngleVals = mxCircles;
    int numAngleVals = numRows;
    if (numAngleVals > mxAngleVals) {
      numAngleVals = mxAngleVals;
    }

    int numCols = inputGrid.getNumColumns();
    int maxAngleDegs = 360;
    int angleIncrDegs = 5;
    int mxAngles = (int) Math.rint(maxAngleDegs / angleIncrDegs);
    int numAngles = numCols;
    if (numAngles > mxAngles) {
      numAngles = mxAngles;
    }
    float[][] outputData = new float[numAngleVals][numAngles];

    // Loop over the angle values
    for (int i1 = 0; i1 < numAngleVals; i1++) {

      // Loop over the angles
      for (int i2 = 0; i2 < numAngles; i2++) {

        // Determine the x,y coordinates.
        double[] xy = geometry.transformRowColToXY(i1, i2);

        // If no AOI specified, or the coordinate is contained in the
        // AOI, then continue to the next test.
        if (aoi == null || aoi.contains(xy[0], xy[1])) {

          // If the input grid value if non-null, then continue to the
          // next test.
          if (!inputGrid.isNull(i1, i2)) {
            outputData[i1][i2] = inputGrid.getValueAtRowCol(i1, i2);
          } else {
            // The input grid value is null, then set the output
            // grid value to null.
            outputData[i1][i2] = nullValue;
          }
        } else {
          // The coordinate is not contained in the AOI, then set the
          // output grid value to null.
          outputData[i1][i2] = nullValue;
        }
      }
    }

    return outputData;
  }

  /**
   * Determine values that should plotted on the polar plot
   * 
   * @param volume the input Volume.
   * @param aoi Area of Interest to display data
   * @param mxCircles Maximum number of circles to display
   * @return an array of values to display in the polar plot
   */
  public float[][] readVolumeData(final PostStack3d volume, final AreaOfInterest aoi, final int mxCircles) {

    // Determine the zmin and zmax for the volume
    float zMin = volume.getZStart();
    float zMax = volume.getZEnd();

    // define number of circles and angles based on the number of samples
    int numSamples = volume.getNumSamplesPerTrace();
    int mxAngleVals = mxCircles;
    int numAngleVals = numSamples;
    if (numAngleVals > mxAngleVals) {
      numAngleVals = mxAngleVals;
    }

    // We just want one inline and one xline at a time
    float[] inlines = new float[1];
    float[] xlines = new float[1];

    // determine the number of angles based on number of Xlines
    int numXlines = volume.getNumXlines();
    int maxAngleDegs = 360;
    int angleIncrDegs = 5;
    int mxAngles = (int) Math.rint(maxAngleDegs / angleIncrDegs);
    int numAngles = numXlines;
    if (numAngles > mxAngles) {
      numAngles = mxAngles;
    }

    // Determine the angle increment based on the number of angles
    if (numAngles < mxAngles) {
      angleIncrDegs = maxAngleDegs / numAngles;
    }
    float[][] outputData = new float[numAngleVals][numAngles];

    for (int i1 = 0; i1 < numAngles; i1++) {
      // Determine the current inline and xline
      inlines[0] = volume.getInlineStart();
      xlines[0] = volume.getXlineStart() + i1 * volume.getXlineDelta();

      // Determine trace based on the inline and crossline
      // (Make sure trace is valid)
      TraceData traceData = volume.getTraces(inlines, xlines, zMin, zMax);
      Trace trace = traceData.getTrace(0);

      // Determine trace based on the inline and crossline
      // (Make sure trace is valid)
      Boolean processTrace = false;

      // Make sure that the trace is a live trace
      if (trace.isLive()) {
        processTrace = true;
      } else {
        processTrace = false;
      }

      // Determine if Inline and crossline is in the area of interest
      if (processTrace) {
        if (aoi == null) {
          processTrace = true;
        } else if (aoi.contains(trace.getX(), trace.getY())) {
          processTrace = true;
        } else {
          processTrace = false;
        }
      }

      if (processTrace) {
        float[] tvals = trace.getData();
        for (int i2 = 0; i2 < numAngleVals; i2++) {
          outputData[i2][i1] = tvals[i2];
        }
      }
    }

    return outputData;
  }

  public void selectionChanged(final IAction action, final ISelection selection) {
    // does nothing for now
  }

}
