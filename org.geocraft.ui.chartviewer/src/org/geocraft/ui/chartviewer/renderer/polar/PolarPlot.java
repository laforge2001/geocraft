/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.ui.chartviewer.renderer.polar;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;


public class PolarPlot extends StandaloneAlgorithm {

  // UI TYPES
  // Input section

  /** Type of input data. */
  public enum InputType {
    Seismic("Seismic Data"),
    Grids("Grid Data"),
    Wells("Well Data");

    private final String _displayName;

    InputType(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  /** The input type */
  private InputType _inputType;

  /** The seismic input volume property */
  private EntityProperty<SeismicDataset> _inputVolume;

  /** The input grid property. */
  protected final EntityProperty<Grid3d> _inputGrid;

  /** The input well property. */
  private final EntityProperty<Well> _inputWell;

  /** The input well log trace property. */
  private final EntityProperty<WellLogTrace> _inputLogTrace;

  private ComboField _logTraceField;

  public PolarPlot(InputType inputType) {
    // Set the input type to Grid data
    _inputType = inputType;
    _inputVolume = addEntityProperty("Input Volume", SeismicDataset.class);
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _inputWell = addEntityProperty("Well", Well.class);
    _inputLogTrace = addEntityProperty("Log Trace", WellLogTrace.class);
  }

  @Override
  public void propertyChanged(String key) {

    if (key.equals(_inputWell.getKey())) {
      if (!_inputWell.isNull()) {
        WellLogTrace[] logTraces = _inputWell.get().getWellLogTraces();
        _logTraceField.setOptions(logTraces);
        _inputLogTrace.setValueObject(null);
      }
    }
  }

  @Override
  public void validate(IValidation results) {
    if (_inputType.equals(InputType.Seismic)) {
      // Validate the input grid is non-null.
      if (_inputVolume.isNull()) {
        results.error(_inputVolume, "No input volume specified.");
      }
    } else if (_inputType.equals(InputType.Grids)) {
      // Validate the input grid is non-null.
      if (_inputGrid.isNull()) {
        results.error(_inputGrid, "No input grid specified.");
      }
    } else if (_inputType.equals(InputType.Wells)) {
      // Validate the input well bore is non-null.
      if (_inputWell.isNull()) {
        results.error(_inputWell, "No well bore specified.");
      }
      // Validate the input log trace is non-null.
      if (_inputLogTrace.isNull()) {
        results.error(_inputLogTrace, "No log trace specified.");
      }
    }
  }

  /* (non-Javadoc)
   * Construct the algorithm's UI consisting of form fields partitioned into sections: Input,
   * Output, and algorithm Parameters.
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.algorithm.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {
    FormSection inputSection = modelForm.addSection("Input");

    ComboField inputVolume = inputSection.addEntityComboField(_inputVolume, SeismicDataset.class);
    inputVolume.setTooltip("Select the input volume.");
    if (!_inputType.equals(InputType.Seismic)) {
      inputVolume.setVisible(false);
    }

    ComboField inputGrid = inputSection.addEntityComboField(_inputGrid, Grid3d.class);
    inputGrid.setTooltip("Select the input grid.");
    if (!_inputType.equals(InputType.Grids)) {
      inputGrid.setVisible(false);
    }

    ComboField inputWellField = inputSection.addEntityComboField(_inputWell, Well.class);

    ComboField logTraceField = inputSection.addComboField(_inputLogTrace, new Object[0]);
    if (!_inputType.equals(InputType.Wells)) {
      inputWellField.setVisible(false);
      logTraceField.setVisible(false);
    }
    _logTraceField = logTraceField;
  }

  /**
   * Runs the domain logic of the algorithm.
   * @param monitor the progress monitor.
   * @param logger the logger to log messages.
   * @param repository the repository in which to add output entities.
   */

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // Start the progress monitor.
    monitor.beginTask("Determining the polar plot", 2);

    InputType inputType = _inputType;

    // Read the volume data
    if (inputType.equals(InputType.Seismic)) {
      // Determine the input volume
      SeismicDataset inputVolume = _inputVolume.get();

      // Display plot
      displayPlot((PostStack3d) inputVolume, monitor);

      // Read the grid data
    } else if (inputType.equals(InputType.Grids)) {
      // Determine the input grid
      Grid3d inputGrid = _inputGrid.get();

      // Display plot
      displayPlot(inputGrid, monitor);

      // Read the well data
    } else if (inputType.equals(InputType.Wells)) {

      // Determine the input log trace
      WellLogTrace inputLogTrace = _inputLogTrace.get();

      // determine the cell data from the log data
      float[][] cellData = readLogData(inputLogTrace);

      throw new RuntimeException("The input Type of well data has not been implemented yet");

      // display error if input type is invalid
    } else {
      throw new RuntimeException("The input Type should be Grids");
    }
  }

  /**
   * Display the plot
   * @param input Volume
   * @param monitor the progress monitor.
   */
  public void displayPlot(PostStack3d volume, IProgressMonitor monitor) {

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
    float[][] cellData = readVolumeData(monitor, volume, null, mxCircles);
    monitor.worked(1);

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

    // Task is done.
    monitor.done();
  }

  /**
   * Display the plot
   * @param input Grid
   * @param monitor the progress monitor
   */
  public void displayPlot(Grid3d inputGrid, IProgressMonitor monitor) {

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
    float[][] cellData = readGridData(monitor, inputGrid, null, mxCircles);
    monitor.worked(1);

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

    // Task is done.
    monitor.done();
  }

  /**
   * Determine values that should plotted on the polar plot
   * 
   * @param inputLogTrace the input Trace.
   * @return an array of values to display in the polar plot
   */
  public float[][] readLogData(final WellLogTrace inputLogTrace) {
    float[] inputData = inputLogTrace.getTraceData();
    int numSamples = inputLogTrace.getNumSamples();

    // determine the number of angles around circle
    int numCircles = 30;
    int maxAngleDegs = 360;
    int angleIncrDegs = 5;
    int numAngles = (int) Math.rint(maxAngleDegs / angleIncrDegs) + 1;
    double angleIncr = 2 * Math.PI / (numAngles - 1);
    double minValue = 0;
    double maxValue = 2 * Math.PI;
    double startValue = maxValue / 4;

    // Determine the number values per angle
    int numAngleVals = numCircles - 1;

    // define number of circles and angles based on the number of rows and columns
    float[][] outputData = new float[numAngleVals][numAngles];

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
        int circleIndx = i2 + 2;
        xCellPnts[0][cellIndx] = (float) Math.cos(plotAngle) * (circleIndx - 1);
        yCellPnts[0][cellIndx] = (float) Math.sin(plotAngle) * (circleIndx - 1);
        xCellPnts[1][cellIndx] = (float) Math.cos(plotAngle2) * (circleIndx - 1);
        yCellPnts[1][cellIndx] = (float) Math.sin(plotAngle2) * (circleIndx - 1);
        xCellPnts[2][cellIndx] = (float) Math.cos(plotAngle2) * circleIndx;
        yCellPnts[2][cellIndx] = (float) Math.sin(plotAngle2) * circleIndx;
        xCellPnts[3][cellIndx] = (float) Math.cos(plotAngle) * circleIndx;
        yCellPnts[3][cellIndx] = (float) Math.sin(plotAngle) * circleIndx;

        // Set the output data
        outputData[i2][i1] = inputData[cellIndx % numSamples];
        cellIndx++;
      }
    }
    return outputData;
  }

  /**
   * Determine values that should plotted on the polar plot
   *
   * @param monitor the progress monitor.
   * @param inputGrid the input Grid.
   * @param AreaOfInterest Area of Interest to display data
   * @return an array of values to display in the polar plot
   */
  public float[][] readGridData(final IProgressMonitor monitor, final Grid3d inputGrid, final AreaOfInterest aoi,
      final int mxCircles) {

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
    for (int i1 = 0; i1 < numAngleVals && !monitor.isCanceled(); i1++) {

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

      // Update the progress monitor.
      monitor.worked(1);
      monitor.subTask("Completed row " + i1);
    }

    return outputData;
  }

  /**
   * Determine values that should plotted on the polar plot
   * 
   * @param monitor the progress monitor.
   * @param volume the input Volume.
   * @param aoi Area of Interest to display data
   * @param mxCircles Maximum number of circles to display
   * @return an array of values to display in the polar plot
   */
  public float[][] readVolumeData(final IProgressMonitor monitor, final PostStack3d volume, final AreaOfInterest aoi,
      final int mxCircles) {

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

    for (int i1 = 0; i1 < numAngles && !monitor.isCanceled(); i1++) {
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

      // Update the progress monitor.
      monitor.worked(1);
      monitor.subTask("Completed xline " + xlines[0]);
    }

    return outputData;
  }
}
