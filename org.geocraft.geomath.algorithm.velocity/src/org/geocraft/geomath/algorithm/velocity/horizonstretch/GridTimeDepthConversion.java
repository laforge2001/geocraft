/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.velocity.horizonstretch;


import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityArrayProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter.Method;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityVolumeSpecification;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class GridTimeDepthConversion extends StandaloneAlgorithm {

  private static UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  /**
   * The property containing the input grids on which to perform time/depth conversion.
   * Grids in the time domain will be converted to depth.
   * Grids in the depth domain will be converted to time.
   */
  private EntityArrayProperty<Grid3d> _inputGrids;

  /** The property containing the velocity volume to use in the time/depth conversion. */
  private EntityProperty<PostStack3d> _velocityVolume;

  /** The property containing the optional area-of-interest. */
  private EntityProperty<AreaOfInterest> _aoi;

  /** The property containing the area-of-interest usage flag. */
  private BooleanProperty _aoiFlag;

  /** The property containing the conversion method: <code>TIME_TO_DEPTH</code> or <code>DEPTH_TO_TIME</code>. */
  private EnumProperty<Method> _conversionMethod;

  /** The property containing the suffix to append to the output grid names. */
  private StringProperty _outputGridSuffix;

  /** Enumeration of the conversion directions. */
  private enum ConversionDirection {
    /** Convert grids from time to depth. */
    TIME_TO_DEPTH,
    /** Convert grids from depth to time. */
    DEPTH_TO_TIME
  }

  public GridTimeDepthConversion() {
    // Add the algorithm properties.
    _inputGrids = addEntityArrayProperty("Input Grid(s)", Grid3d.class);
    _velocityVolume = addEntityProperty("Velocity Volume", PostStack3d.class);
    _aoi = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _aoiFlag = addBooleanProperty("Use AOI?", false);
    _conversionMethod = addEnumProperty("Conversion Method", Method.class, Method.KneeBased);
    _outputGridSuffix = addStringProperty("Output Grid Suffix", "stretch");
  }

  @Override
  public void buildView(IModelForm modelForm) {
    // Build the input section.
    FormSection inputSection = modelForm.addSection("Input");
    inputSection.addEntityListField(_inputGrids, new Grid3dTimeDepthSpecification());
    inputSection.addEntityComboField(_velocityVolume, new VelocityVolumeSpecification());
    inputSection.addEntityComboField(_aoi, AreaOfInterest.class).showActiveFieldToggle(_aoiFlag);

    // Build the conversion section.
    FormSection parameterSection = modelForm.addSection("Conversion");
    parameterSection.addRadioGroupField(_conversionMethod, Method.values());

    // Build the output section.
    FormSection outputSection = modelForm.addSection("Output");
    outputSection.addTextField(_outputGridSuffix);
  }

  public void propertyChanged(String key) {
    // No action required.
  }

  public void validate(IValidation results) {
    // Validate that at least 1 grid is selected.
    if (_inputGrids.isEmpty()) {
      results.error(_inputGrids, "No input grids specified.");
    } else {
      // Validate all selected grids are in either the time or depth domain.
      for (Grid3d inputGrid : _inputGrids.get()) {
        if (!inputGrid.isTimeGrid() && !inputGrid.isDepthGrid()) {
          results.error(_inputGrids, "One or more input grids in not the time or depth domain.");
        }
      }
    }

    // Validate that a velocity volume is selected.
    if (_velocityVolume.isNull()) {
      results.error(_velocityVolume, "No velocity volume specified");
    } else {
      // Validate the unit of the velocity volume are in the velocity domain.
      PostStack3d velocityVolume = _velocityVolume.get();
      Unit velocityUnit = velocityVolume.getDataUnit();
      if (velocityUnit != Unit.METERS_PER_SECOND && velocityUnit != Unit.FEET_PER_SECOND) {
        results.error(_velocityVolume, "Invalid velocity units. Must be " + Unit.METERS_PER_SECOND.getSymbol() + " or "
            + Unit.FEET_PER_SECOND.getSymbol() + ".");
      }
      StorageOrder storageOrder = velocityVolume.getPreferredOrder();
      if (!storageOrder.equals(StorageOrder.INLINE_XLINE_Z) && !storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
        results.error(_velocityVolume, "Unsupported storage order: " + storageOrder);
      }
    }

    // Validate that an area-of-interest is selected, if used.
    if (_aoiFlag.get()) {
      if (_aoi.isNull()) {
        results.error(_aoi, "No area-of-interest specified.");
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {
    // Unpack the algorithm parameters.
    List<Grid3d> inputGrids = Arrays.asList(_inputGrids.get());
    PostStack3d velocityVolume = _velocityVolume.get();
    Method method = _conversionMethod.get();
    AreaOfInterest aoi = _aoi.get();

    // Loop thru the grids, converting them one at a time.
    for (Grid3d grid : inputGrids) {
      String outputGridName = grid.getMapper().createOutputDisplayName(grid.getDisplayName(),
          "_" + _outputGridSuffix.get());
      convertGrid(grid, velocityVolume, method, aoi, outputGridName, grid, monitor, logger, repository);
    }
  }

  // Comments:
  // In most cases the reference Grid should be the same as the input Grid
  // unless your input grid is an in-memory grid and you want to create a grid that is not in-memory.
  // This is done so that this routine can be run twice without saving the grid on the first run
  // because the grid created on the first run will be an in-memory grid 
  // and the reference grid on the 2nd run will be the original input grid

  public Grid3d convertGrid(final Grid3d inputGrid, final PostStack3d velocityVolume, final Method conversionMethod,
      final AreaOfInterest areaOfInterest, final String outputGridName, final Grid3d referenceGrid,
      final IProgressMonitor monitor, final ILogger logger, final IRepository repository) {

    // Determine the grid.
    GridGeometry3d gridGeometry = inputGrid.getGeometry();
    Domain inputDomain = inputGrid.getZDomain();
    ConversionDirection conversionMode = null;

    if (inputDomain.equals(Domain.TIME)) {
      conversionMode = ConversionDirection.TIME_TO_DEPTH;
    } else if (inputDomain.equals(Domain.DISTANCE)) {
      conversionMode = ConversionDirection.DEPTH_TO_TIME;
    } else {
      logger.error("Invalid domain type. Must be time or depth.");
      return null;
    }

    // Initialize.
    Grid3d outputGrid = null;

    try {

      // Convert the grid z values.
      float[][] result = convertGridValues(inputGrid, velocityVolume, conversionMethod, conversionMode, areaOfInterest,
          monitor, logger);

      // Create a new grid object.
      switch (conversionMode) {

        case TIME_TO_DEPTH:
          if (repository == null) {
            // If the repository is null we just want an in memory grid.
            outputGrid = Grid3dFactory.createInMemory(outputGridName, gridGeometry,
                UNIT_PREFS.getVerticalDistanceUnit(), result, inputGrid.getNullValue());
          } else {
            outputGrid = Grid3dFactory.create(repository, referenceGrid, result, outputGridName, gridGeometry,
                UNIT_PREFS.getVerticalDistanceUnit());
          }
          outputGrid.setZDomain(Domain.DISTANCE);
          outputGrid.update();
          break;
        case DEPTH_TO_TIME:
          if (repository == null) {
            // If the repository is null we just want an in memory grid.
            outputGrid = Grid3dFactory.createInMemory(outputGridName, gridGeometry, UNIT_PREFS.getTimeUnit(), result,
                inputGrid.getNullValue());
          } else {
            // Otherwise create the grid in the repository.
            outputGrid = Grid3dFactory.create(repository, referenceGrid, result, outputGridName, gridGeometry,
                UNIT_PREFS.getTimeUnit());
          }
          outputGrid.setZDomain(Domain.TIME);
          outputGrid.update();
          break;
        default:
          logger.error("Invalid conversion mode - Convert from (Depth to Time) or (Time to Depth)");
          return null;
      }
      outputGrid.update();

    } catch (Exception e) {
      logger.error("Error occurred when executing the horizonStretch algorithm", e);
      outputGrid = null;
    }

    return outputGrid;
  }

  /**
   * Converts the grid values from time to depth or depth to time.
   * 
   * @param grid the input grid.
   * @param velocityVolume the velocity volume.
   * @param conversionMethod the conversion method.
   * @param conversionMode the conversion mode.
   * @param areaOfInterest the area-of-interest.
   * @param monitor the progress monitor.
   * @param logger the error logger.
   * @return the array of converted grid values.
   */
  public float[][] convertGridValues(final Grid3d grid, final PostStack3d velocityVolume,
      final Method conversionMethod, final ConversionDirection conversionMode, final AreaOfInterest areaOfInterest,
      final IProgressMonitor monitor, final ILogger logger) {

    // Determine the geometry.
    GridGeometry3d gridGeometry = grid.getGeometry();
    float[][] outputData = new float[gridGeometry.getNumRows()][gridGeometry.getNumColumns()];

    // Get size of grid.
    int numRows = gridGeometry.getNumRows();
    int numCols = gridGeometry.getNumColumns();

    Unit velocityUnit = velocityVolume.getDataUnit();
    float zMin = velocityVolume.getZStart();
    float zMax = velocityVolume.getZEnd();

    // Determine the domain of the input grid.
    Domain inputGridDomain = grid.getZDomain();

    if (conversionMode.equals(ConversionDirection.TIME_TO_DEPTH)) {
      if (inputGridDomain.equals(Domain.DISTANCE)) {
        throw new RuntimeException("Grid should contain times in order to go from time to depth");
      }
    } else if (conversionMode.equals(ConversionDirection.DEPTH_TO_TIME)) {
      if (inputGridDomain.equals(Domain.TIME)) {
        throw new RuntimeException("Grid should contain depths in order to go from depth to time");
      }
    }

    // Determine data unit of the grid.
    Unit inputGridDataUnit = grid.getDataUnit();

    // We just want one inline and one xline.
    float[] inlines = new float[1];
    float[] xlines = new float[1];
    float inlineMin = Math.min(velocityVolume.getInlineStart(), velocityVolume.getInlineEnd());
    float inlineMax = Math.max(velocityVolume.getInlineStart(), velocityVolume.getInlineEnd());
    float xlineMin = Math.min(velocityVolume.getXlineStart(), velocityVolume.getXlineEnd());
    float xlineMax = Math.max(velocityVolume.getXlineStart(), velocityVolume.getXlineEnd());
    boolean processingDone = false;
    boolean validXYdata = false;

    StorageOrder storageOrder = velocityVolume.getPreferredOrder();

    float nullValue = grid.getNullValue();
    float[][] inputGridValues = grid.getValues();

    // Convert values for each point in the grid.
    if (storageOrder.equals(StorageOrder.INLINE_XLINE_Z)) {
      monitor.beginTask("Grid Stretch 3D", numCols);
      for (int col = 0; col < numCols; col++) {
        for (int row = 0; row < numRows; row++) {

          float inputGridValue = inputGridValues[row][col];

          outputData[row][col] = nullValue;
          if (!grid.isNull(inputGridValue)) {

            // Determine x,y coordinates at the current point
            double[] xy = gridGeometry.transformRowColToXY(row, col);
            double x = xy[0];
            double y = xy[1];

            // Determine inline and xline that correspond to x,y.
            // (Get the closest inline and xline).
            SeismicSurvey3d geometry = velocityVolume.getSurvey();
            float[] lines = geometry.transformXYToInlineXline(x, y, true);

            inlines[0] = lines[0];
            xlines[0] = lines[1];

            // Determine trace based on the inline and crossline.
            // (Make sure trace is valid).
            Trace trace = null;
            Boolean processTrace = false;
            if (inlines[0] >= inlineMin && inlines[0] <= inlineMax && xlines[0] >= xlineMin && xlines[0] <= xlineMax) {
              validXYdata = true;
              // Determine if Inline and crossline is in the area of interest
              if (areaOfInterest == null) {
                processTrace = true;
              } else if (areaOfInterest.contains(x, y)) {
                processTrace = true;
              } else {
                processTrace = false;
              }
              // Determine the trace in the volume.
              if (processTrace) {
                TraceData traceData = velocityVolume.getTraces(inlines, xlines, zMin, zMax);
                trace = traceData.getTrace(0);
                // Make sure that the trace is a live trace.
                if (trace.isLive()) {
                  processTrace = true;
                } else {
                  processTrace = false;
                }
              }
            }

            // Process trace if it is valid.
            if (processTrace) {
              VelocityArrayTimeDepthConverter converter = new VelocityArrayTimeDepthConverter(trace.getData(),
                  trace.getZDelta(), velocityVolume.getZDomain(), velocityUnit, conversionMethod);
              float outputGridValue = Float.NaN;

              if (conversionMode.equals(ConversionDirection.TIME_TO_DEPTH)) {
                outputGridValue = Unit.convert(inputGridValue, inputGridDataUnit, UNIT_PREFS.getTimeUnit());
                outputGridValue = converter.getDepth(outputGridValue);
              } else if (conversionMode.equals(ConversionDirection.DEPTH_TO_TIME)) {
                outputGridValue = Unit.convert(inputGridValue, inputGridDataUnit, UNIT_PREFS.getVerticalDistanceUnit());
                outputGridValue = converter.getTime(outputGridValue);
              }
              if (!Float.isNaN(outputGridValue)) {
                outputData[row][col] = outputGridValue;
                processingDone = true;
              }
            }
          }
        }
        monitor.subTask("Completed column " + (col + 1) + " of " + numCols);
        monitor.worked(1);
        if (monitor.isCanceled()) {
          break;
        }
      }
    } else if (storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
      monitor.beginTask("Grid Stretch 3D", numRows);
      for (int row = 0; row < numRows; row++) {

        for (int col = 0; col < numCols; col++) {

          float inputGridValue = inputGridValues[row][col];

          outputData[row][col] = nullValue;
          if (!grid.isNull(inputGridValue)) {

            // Determine x,y coordinates at the current point.
            double[] xy = gridGeometry.transformRowColToXY(row, col);
            double x = xy[0];
            double y = xy[1];

            // Determine inline and xline that correspond to x,y.
            // (Get the closest inline and xline).
            SeismicSurvey3d geometry = velocityVolume.getSurvey();
            float[] lines = geometry.transformXYToInlineXline(x, y, true);

            inlines[0] = lines[0];
            xlines[0] = lines[1];

            // Determine trace based on the inline and crossline.
            // (Make sure trace is valid).
            Trace trace = null;
            Boolean processTrace = false;
            if (inlines[0] >= inlineMin && inlines[0] <= inlineMax && xlines[0] >= xlineMin && xlines[0] <= xlineMax) {
              validXYdata = true;
              // Determine if inline and crossline is in the area of interest.
              if (areaOfInterest == null) {
                processTrace = true;
              } else if (areaOfInterest.contains(x, y)) {
                processTrace = true;
              } else {
                processTrace = false;
              }
              // Determine the trace in the volume.
              if (processTrace) {
                TraceData traceData = velocityVolume.getTraces(inlines, xlines, zMin, zMax);
                trace = traceData.getTrace(0);
                // Make sure that the trace is a live trace.
                if (trace.isLive()) {
                  processTrace = true;
                } else {
                  processTrace = false;
                }
              }
            }

            // Process trace if it is valid.
            if (processTrace) {
              VelocityArrayTimeDepthConverter converter = new VelocityArrayTimeDepthConverter(trace.getData(),
                  trace.getZDelta(), velocityVolume.getZDomain(), velocityUnit, conversionMethod);
              float outputGridValue = Float.NaN;

              if (conversionMode.equals(ConversionDirection.TIME_TO_DEPTH)) {
                outputGridValue = Unit.convert(inputGridValue, inputGridDataUnit, UNIT_PREFS.getTimeUnit());
                outputGridValue = converter.getDepth(outputGridValue);
              } else if (conversionMode.equals(ConversionDirection.DEPTH_TO_TIME)) {
                outputGridValue = Unit.convert(inputGridValue, inputGridDataUnit, UNIT_PREFS.getVerticalDistanceUnit());
                outputGridValue = converter.getTime(outputGridValue);
              }
              if (!Float.isNaN(outputGridValue)) {
                outputData[row][col] = outputGridValue;
                processingDone = true;
              }
            }
          }
        }
        monitor.subTask("Completed row " + (row + 1) + " of " + numRows);
        monitor.worked(1);
        if (monitor.isCanceled()) {
          break;
        }
      }
    }

    // Make sure some processing has been done.
    if (!processingDone) {
      String msg = "Unable to convert grid depths from depth to time";
      if (validXYdata) {
        if (conversionMode.equals(ConversionDirection.TIME_TO_DEPTH)) {
          msg = "Unable to convert grid times from time to depth";
        }
      } else {
        msg = "X,Y locations in the grid do not match the volume";
      }
      logger.error(msg);
      throw new RuntimeException(msg);
    }

    return outputData;
  }

  private class Grid3dTimeDepthSpecification extends TypeSpecification {

    public Grid3dTimeDepthSpecification() {
      super(Grid3d.class);
    }

    @Override
    public boolean isSatisfiedBy(Object object) {
      boolean typeSatisfied = super.isSatisfiedBy(object);
      if (typeSatisfied) {
        Grid3d grid = (Grid3d) object;
        if (grid.isTimeGrid() || grid.isDepthGrid()) {
          return true;
        }
      }
      return false;
    }
  }
}
