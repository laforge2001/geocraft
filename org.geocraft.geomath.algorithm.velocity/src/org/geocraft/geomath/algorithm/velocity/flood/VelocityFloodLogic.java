/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.velocity.flood;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.ConstantOrGridSelection;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodDirection;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodType;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.ReferenceSelection;
import org.geocraft.io.util.TraceIterator;
import org.geocraft.io.util.TraceIteratorFactory;


public class VelocityFloodLogic {

  /** UI field values used by the algorithm */
  private PostStack3d _inputVelocityVolume;

  private AreaOfInterest _areaOfInterest;

  private Grid3d _velocityGrid, _gradientGrid, _referenceDepthGrid, _topHorizon, _baseHorizon;

  float _multiplier, _adder;

  private FloodType _floodType;

  private ConstantOrGridSelection _constantSelection;

  private FloodDirection _floodDirection;

  private ReferenceSelection _referenceSelection;

  private String _outputVolumeName;

  /**
   * @return true if to use constants for Flood Velocity and Flood Gradient; otherwise false (use horizons)
   */
  private boolean useFloodParmsConstant(ConstantOrGridSelection sel) {
    return sel.equals(ConstantOrGridSelection.Grid) ? false : true;
  }

  /**
   * @return true if to use a constant reference depth; otherwise false (use a reference horizon)
   */
  private boolean useReferenceConstant(ReferenceSelection sel) {
    return sel.equals(ReferenceSelection.Grid) ? false : true;
  }

  /**
   * Convert decimal number represented as a string to a float
   */
  private float toFloat(String num, float defaultNum) {
    try {
      return Float.parseFloat(num);
    } catch (NumberFormatException nfe) {
      return defaultNum;
    }
  }

  /**
   * Velocity flood algorithm.
   * @param monitor Execution progress monitor
   * @param logger Where to log execution execption information
   * @param repository Data repository to write results
   * @param _model UI input fields
   * @return Result of velocity flood algorithm - a PostStack3d data file.
   * @throws CoreException
   */
  public PostStack3d compute(IProgressMonitor monitor, ILogger logger, IRepository repository,
      VelocityFloodAlgorithm _model) throws CoreException {
    IFloodStrategy floodStrategy = null;
    // Get field values required by the algorithm
    _inputVelocityVolume = (PostStack3d) _model.getValueObject(VelocityFloodAlgorithm.VEL_VELOCITY);
    _areaOfInterest = (AreaOfInterest) _model.getValueObject(VelocityFloodAlgorithm.AOI);
    _velocityGrid = null;
    _gradientGrid = null;
    _referenceDepthGrid = null;
    _multiplier = toFloat((String) _model.getValueObject(VelocityFloodAlgorithm.MULTIPLIER), 1.0f);
    _adder = toFloat((String) _model.getValueObject(VelocityFloodAlgorithm.ADDER), 0.0f);
    _floodType = (FloodType) _model.getValueObject(VelocityFloodAlgorithm.FLOOD_TYPE);
    _constantSelection = (ConstantOrGridSelection) _model.getValueObject(VelocityFloodAlgorithm.CONSTANT_SELECTION);
    _floodDirection = (FloodDirection) _model.getValueObject(VelocityFloodAlgorithm.FLOOD_DIRECTION);
    _topHorizon = (Grid3d) _model.getValueObject(VelocityFloodAlgorithm.TOP_GRID);
    _baseHorizon = _floodDirection.equals(FloodDirection.Below) ? null : (Grid3d) _model
        .getValueObject(VelocityFloodAlgorithm.BASE_GRID);
    _referenceSelection = (ReferenceSelection) _model.getValueObject(VelocityFloodAlgorithm.REF_DEPTH_OR_HORIZON);
    _outputVolumeName = (String) _model.getValueObject(VelocityFloodAlgorithm.OUTPUT_VOL_NAME);

    // SET UP THE PARAMETERS TO THE ALGORITHM
    // Determine time or depth grids.
    Grid3d[] zGrids = new Grid3d[0];
    if (_floodDirection.equals(FloodDirection.Between)) {
      Grid3d hprop1 = _topHorizon;
      Grid3d hprop2 = _baseHorizon;
      zGrids = new Grid3d[] { hprop1, hprop2 };
    } else {
      Grid3d hprop1 = _floodDirection.equals(FloodDirection.Below) ? _topHorizon : _baseHorizon;
      zGrids = new Grid3d[] { hprop1 };
    }

    if (_floodType.equals(FloodType.Constant)) {

      float velocity = 5000.0f;
      if (useFloodParmsConstant(_constantSelection)) {
        // Use a constant flood velocity.
        velocity = toFloat((String) _model.getValueObject(VelocityFloodAlgorithm.FLOOD_VELOCITY), velocity);
      } else {
        // Note: The flood velocity comes from a velocity grid.
        // Use a flood horizon
        _velocityGrid = (Grid3d) _model.getValueObject(VelocityFloodAlgorithm.VELOCITY_GRID);
      }
      floodStrategy = new ConstantVelocityFloodStrategy(_floodDirection, velocity);

    } else if (_floodType.equals(FloodType.Gradient)) {

      float velocity = 5000.0f;
      float gradient = 1.0f;
      float referenceDepth = 0.0f;
      if (useReferenceConstant(_referenceSelection)) {
        // Use a constant reference depth
        referenceDepth = toFloat((String) _model.getValueObject(VelocityFloodAlgorithm.REF_DEPTH), referenceDepth);
      } else {
        // Note: The reference depth comes from a reference depth grid.
        // Use a reference horizon
        _referenceDepthGrid = (Grid3d) _model.getValueObject(VelocityFloodAlgorithm.REF_GRID);
      }

      if (useFloodParmsConstant(_constantSelection)) {
        // Use constant flood velocity and gradient.
        velocity = toFloat((String) _model.getValueObject(VelocityFloodAlgorithm.FLOOD_VELOCITY), velocity);
        gradient = toFloat((String) _model.getValueObject(VelocityFloodAlgorithm.FLOOD_GRADIENT), gradient);
      } else {
        // Note: The velocity and gradient come from velocity and gradient grids.
        // Use a velocity and gradient horizon
        _velocityGrid = (Grid3d) _model.getValueObject(VelocityFloodAlgorithm.VELOCITY_GRID);
        _gradientGrid = (Grid3d) _model.getValueObject(VelocityFloodAlgorithm.GRADIENT_GRID);
      }
      floodStrategy = new GradientVelocityFloodStrategy(_floodDirection, velocity, gradient, referenceDepth);

    } else if (_floodType.equals(FloodType.Dataset)) {
      floodStrategy = new DatasetVelocityFloodStrategy((PostStack3d) _model
          .getValueObject(VelocityFloodAlgorithm.DATA_SET_VOL), _floodDirection, _multiplier, _adder);
    } else {
      throw new IllegalArgumentException("The flood type \'" + _floodType + "\' is not currently supported.");
    }

    try {

      // Flood the the input volume, returning an output volume.
      PostStack3d outputVelocityVolume = floodVolume(monitor, floodStrategy, _inputVelocityVolume, _areaOfInterest,
          zGrids, _velocityGrid, _gradientGrid, _referenceDepthGrid, _outputVolumeName, logger, repository);
      //outputVelocityVolume.setDirty(true);

      // Add the output volume to the repository.
      //ServiceProvider.getRepository().add(outputVelocityVolume);

      return outputVelocityVolume;
    } catch (Exception ex) {
      logger.error(ex.toString(), ex);
      throw new RuntimeException("Algorithm error: " + ex.toString(), ex);
      //throw new CoreException(new Status(IStatus.ERROR, ServiceComponent.PLUGIN_ID, ex.getMessage()));
    }
  }

  /**
   * Loops through volume and selects each trace associated with a point in the horizon. The trace
   * is flooded and saved in new volume.
   */
  private PostStack3d floodVolume(final IProgressMonitor monitor, final IFloodStrategy floodStrategy,
      final PostStack3d inputVelocityVolume, final AreaOfInterest areaOfInterest, final Grid3d[] zGrids,
      final Grid3d velocityGrid, final Grid3d gradientGrid, final Grid3d referenceDepthGrid,
      final String outputVolumeName, ILogger logger, final IRepository repository) throws Exception {
    // Create the output volume.
    PostStack3d outputVelocityVolume = PostStack3dFactory.create(repository, inputVelocityVolume, outputVolumeName);

    // The rows and colums will contain the x,y coordinates.
    int[] rows = new int[zGrids.length];
    int[] cols = new int[zGrids.length];

    float[] horizonRange = new float[zGrids.length];
    boolean floodingDone = false;

    // Initialize the progress monitor.
    monitor.beginTask("Velocity Flood of \'" + inputVelocityVolume.getDisplayName() + "\'", inputVelocityVolume
        .getNumInlines());

    // Create a trace iterator for reading the input volume in the optimal direction.
    TraceIterator traceIterator = TraceIteratorFactory.create(inputVelocityVolume, areaOfInterest);
    while (traceIterator.hasNext()) {
      // Get the next trace collection.
      TraceData traceDataIn = traceIterator.next();
      Trace[] tracesIn = traceDataIn.getTraces();
      // Allocate the output traces.
      Trace[] tracesOut = new Trace[traceDataIn.getNumTraces()];

      // Update the progress monitor message.
      monitor.subTask(traceIterator.getMessage());

      // Determine which traces are inside the area of interest
      int numTraces = traceDataIn.getNumTraces();

      // Loop thru the traces in the trace collection.
      for (int i = 0; i < numTraces; i++) {

        Trace trace = tracesIn[i];

        // Make sure input trace is 'live' before processing.
        if (trace.isLive()) {
          double x = trace.getX();
          double y = trace.getY();

          boolean doFlood = true;

          // Make sure x,y coordinates of the trace are found in the horizons.
          boolean isValidXY = true;
          for (int j = 0; j < zGrids.length; j++) {
            Grid3d prop = zGrids[j];
            GridGeometry3d geometry = prop.getGeometry();
            double[] rowcol = geometry.transformXYToRowCol(x, y, true);
            int row = (int) rowcol[0];
            if (row < 0 || row > geometry.getNumRows()) {
              isValidXY = false;
            }
            int col = (int) rowcol[1];
            if (col < 0 || col > geometry.getNumColumns()) {
              isValidXY = false;
            }
            rows[j] = row;
            cols[j] = col;
          }

          // determine the current velocity in the velocity horizon
          if (isValidXY) {
            if (velocityGrid != null) {
              GridGeometry3d geometry = velocityGrid.getGeometry();
              double[] res = geometry.transformXYToRowCol(x, y, true);
              int row = (int) res[0];
              if (row < 0 || row > geometry.getNumRows()) {
                throw new Exception(
                    "Cannot match x,y coordinates in the volume to the velocity horizon! x,y coordinates = " + x + ","
                        + y);
              }
              int col = (int) res[1];
              if (col < 0 || col > geometry.getNumColumns()) {
                throw new Exception(
                    "Cannot match x,y coordinates in the volume to the velocity horizon! x,y coordinates = " + x + ","
                        + y);
              }
              float velocity = velocityGrid.getValueAtRowCol(row, col);
              floodStrategy.setVelocity(velocity);
            }

            // Determine the current gradient in the gradient horizon.
            if (gradientGrid != null) {
              GridGeometry3d geometry = gradientGrid.getGeometry();
              double[] rowcol = geometry.transformXYToRowCol(x, y, true);
              int row = (int) rowcol[0];
              if (row < 0 || row > geometry.getNumRows()) {
                throw new Exception(
                    "Cannot match x,y coordinates in the volume to the gradient horizon. x,y coordinates = " + x + ","
                        + y);
              }
              int col = (int) rowcol[1];
              if (col < 0 || col > geometry.getNumColumns()) {
                throw new Exception(
                    "Cannot match x,y coordinates in the volume to the gradient horizon. x,y coordinates = " + x + ","
                        + y);
              }
              float gradient = gradientGrid.getValueAtRowCol(row, col);
              floodStrategy.setGradient(gradient);
            }

            // Determine the current reference depth in the reference horizon.
            if (referenceDepthGrid != null) {
              GridGeometry3d geometry = referenceDepthGrid.getGeometry();
              double[] rowcol = geometry.transformXYToRowCol(x, y, true);
              int row = (int) rowcol[0];
              if (row < 0 || row > geometry.getNumRows()) {
                throw new Exception(
                    "Cannot match x,y coordinates in the volume to the reference horizon. x,y coordinates = " + x + ","
                        + y);
              }
              int col = (int) rowcol[1];
              if (col < 0 || col > geometry.getNumColumns()) {
                throw new Exception(
                    "Cannot match x,y coordinates in the volume to the reference horizon. x,y coordinates = " + x + ","
                        + y);
              }
              float referenceDepth = referenceDepthGrid.getValueAtRowCol(row, col);
              floodStrategy.setReferenceDepth(referenceDepth);
            }

            for (int j = 0; j < zGrids.length; j++) {
              Grid3d prop = zGrids[j];

              float depth = prop.getValueAtRowCol(rows[j], cols[j]);
              if (!prop.isNull(depth)) {
                horizonRange[j] = depth;
              } else {
                // Do not flood if horizon has a null value.
                // If ANY horizons are undefined we do nothing.
                // This will need to be improved to handle flooding between more than 2 layers.
                // One pair could be null but the next might not.
                doFlood = false;
                break;
              }
            }
            // Do not flood if x,y is invalid
          } else {
            doFlood = false;
          }

          // Perform the flooding for the current trace
          if (doFlood) {
            tracesOut[i] = floodStrategy.flood(trace, horizonRange, logger);
            floodingDone = true;
          } else {
            tracesOut[i] = new Trace(trace);
          }

        } else {
          // If trace is 'dead' or 'missing', simply pass along the input trace.
          tracesOut[i] = trace;
        }
      }
      // Create an output trace collection and write it to the output volume.
      TraceData traceDataOut = new TraceData(tracesOut);
      outputVelocityVolume.putTraces(traceDataOut);

      // Update the progress monitor.
      monitor.worked(1);
      if (monitor.isCanceled()) {
        break;
      }
    }
    if (!floodingDone) {
      throw new Exception("No flooding done! The x,y coordinates in the horizon do not match the volume");
    }

    // Close the input and output volumes.
    monitor.subTask("Closing volume...");
    inputVelocityVolume.close();
    outputVelocityVolume.close();
    outputVelocityVolume.load();
    //monitor.done();

    return outputVelocityVolume;
  }

}
