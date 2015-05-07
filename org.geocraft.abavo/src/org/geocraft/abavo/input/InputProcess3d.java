/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.input;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.abavo.ABavoBaseAlgorithm3d;
import org.geocraft.abavo.defs.ABavoTimeMode;
import org.geocraft.abavo.process.MaskFloatArray;
import org.geocraft.abavo.process.NearFarToInterceptGradient;
import org.geocraft.abavo.process.ScaleFloatArray;
import org.geocraft.abavo.process.TraceAlignment;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.math.wavelet.WaveletFilter;


/**
 * Defines the trace input process that is common to all the trace processing flows.
 * 1) Loads the specified Near/Far or Intercept/Gradient files.
 * 2) Filters the A,B traces based on the specified wavelets (optional).
 * 3) Aligns the Far trace to the near trace (optional, Near/Far data only).
 * 4) Scales the A,B traces (optional).
 */
public class InputProcess3d extends AbstractInputProcess {

  protected int _numWorkers;

  /** The input volume A. */
  protected PostStack3d _volumeA;

  /** The input volume B. */
  protected PostStack3d _volumeB;

  /** The top horizon grid (optional). */
  protected Grid3d _topGrid;

  /** The base horizon grid (optional). */
  protected Grid3d _baseGrid;

  /** The top horizon grid geometry (optional). */
  protected GridGeometry3d _topGridGeometry;

  /** The base horizon grid geometry (optional). */
  protected GridGeometry3d _baseGridGeometry;

  protected int _oldWork;

  /** The trace reader for the input volumes (3D only). */
  protected TraceReader3d _traceReader;

  /**
   * Constructs the input process.
   * The project dimension currently must be 3 for 3D (2D is not yet supported).
   * The process also takes 2 models: the ABAVO input model (containing the data and pre-processing) and
   * the ABAVO bounds model (containing the x,y,z bounds over which to process).
   * @param projectDimension the project dimension.
   * @param inputModel the input model.
   * @param boundsModel the bounds model.
   */
  public InputProcess3d(final ABavoBaseAlgorithm3d inputModel, TraceReader3d traceReader) {
    super(3);
    _traceReader = traceReader;
    _status = Status.Idle;
    initialize(inputModel);
  }

  @Override
  public void setProgressMonitor(final IProgressMonitor monitor) {
    _monitor = monitor;
  }

  /**
   * Initializes the input process.
   */
  public void initialize(ABavoBaseAlgorithm3d inputModel) {
    _numWorkers = inputModel.getNumWorkerThreads();
    _oldWork = 0;

    unpackVolumeA(inputModel);

    unpackVolumeB(inputModel);

    // Check that the input volumes sample rates are equal.
    float deltaZA = _volumeA.getZDelta();
    float deltaZB = _volumeB.getZDelta();

    if (deltaZA != deltaZB) {
      throw new IllegalArgumentException("Incompatible sample rates (" + deltaZA + " and " + deltaZB + ").");
    }
    _deltaZ = deltaZA;

    _convertNearFarToInterceptGradient = inputModel.getConvertNearFarToInterceptGradient();
    _dataMode = inputModel.getDataMode();
    _timeMode = inputModel.getTimeMode();

    float outputSampleRate = inputModel.getOutputSampleRate();
    int ratio = Math.round(_volumeA.getZDelta() / outputSampleRate);
    _resampleCount = 0;
    int temp = 1;
    while (temp < ratio) {
      temp *= 2;
      _resampleCount++;
      _deltaZ /= 2;
    }

    unpackGrids(inputModel);

    float aScalar = inputModel.getVolumeScalarA();
    float interceptScalar = inputModel.getInterceptScalar();
    _aPrepWaveletFilterFlag = inputModel.useWaveletScalarA();
    if (_aPrepWaveletFilterFlag) {
      Wavelet waveletA = inputModel.getWaveletScalarA();
      _traceFilterA = new WaveletFilter("Wavelet Filter (A)", waveletA);
    }

    float bScalar = inputModel.getVolumeScalarB();
    float gradientScalar = inputModel.getGradientScalar();
    _bPrepWaveletFilterFlag = inputModel.useWaveletScalarB();
    if (_bPrepWaveletFilterFlag) {
      Wavelet waveletB = inputModel.getWaveletScalarB();
      _traceFilterB = new WaveletFilter("Wavelet Filter (B)", waveletB);
    }

    // Create the trace alignment process.
    _bPrepAutoAlign = inputModel.getAutoAlignBtoA();
    int bPrepCorrWindow = inputModel.getCorrelationWindow();
    int bPrepMaxShift = inputModel.getMaximumShift();
    float bPrepCorrThreshold = inputModel.getCorrelationThreshold();
    int bPrepFilterLength = inputModel.getSmoothingFilterLength();
    float bPrepAmpThreshold = inputModel.getNearAmplitudeThreshold();
    int bPrepAmpThresholdWindow = inputModel.getAmplitudeThresholdWindow();
    _traceAlignment = new TraceAlignment(bPrepCorrWindow, bPrepMaxShift, bPrepCorrThreshold, bPrepFilterLength,
        bPrepAmpThreshold, bPrepAmpThresholdWindow);
    if (_convertNearFarToInterceptGradient) {
      _traceScaleA = new ScaleFloatArray(interceptScalar);
      _traceScaleB = new ScaleFloatArray(gradientScalar);
    } else {
      _traceScaleA = new ScaleFloatArray(aScalar);
      _traceScaleB = new ScaleFloatArray(bScalar);
    }

    float nearAngle = inputModel.getNearAngle();
    float farAngle = inputModel.getFarAngle();
    _xformNearFar = new NearFarToInterceptGradient(nearAngle, farAngle, aScalar, bScalar);

    // Set the default inlne/xline/time processing range.
    float iln0 = 0;
    float iln1 = 0;
    float xln0 = 0;
    float xln1 = 0;
    StorageOrder order = StorageOrder.INLINE_XLINE_Z;

    PostStack3d ps3d = _volumeA;
    iln0 = ps3d.getInlineStart();
    iln1 = ps3d.getInlineEnd();
    xln0 = ps3d.getXlineStart();
    xln1 = ps3d.getXlineEnd();
    order = ps3d.getPreferredOrder();

    if (order.equals(StorageOrder.INLINE_XLINE_Z)) {
      _isPrimInline = true;
    } else if (order.equals(StorageOrder.XLINE_INLINE_Z)) {
      _isPrimInline = false;
    } else {
      throw new IllegalArgumentException("Invalid processing direction: " + order);
    }

    iln0 = inputModel.getInlineStart();
    iln1 = inputModel.getInlineEnd();
    xln0 = inputModel.getXlineStart();
    xln1 = inputModel.getXlineEnd();

    _seismicAOI = new SeismicSurvey3dAOI("aoi", _volumeA.getSurvey(), iln0, iln1, xln0, xln1);

    unpackAOI(inputModel);

    _startZ = inputModel.getTimeStart();
    _endZ = inputModel.getTimeEnd();

    _traceDataIndex = 0;
    _traceBufferA = new Trace[0];
    _traceBufferB = new Trace[0];
  }

  /**
   * Outputs 2 trace data objects.
   * 1st) the input A (near of intercept) trace.
   * 2nd) the input B (far or gradient) trace.
   * 
   * @return the A,B trace data.
   */
  public TraceData[] process() {
    TraceData[] dataOut = new TraceData[0];
    _status = Status.Running;

    // Do work.
    boolean tracesGenerated = false;

    while (!tracesGenerated) {
      _traceDataIndex++;
      // If at the end of the trace data buffer, get next block from the iterator.
      if (_traceDataIndex >= _traceBufferA.length || _traceDataIndex >= _traceBufferB.length) {
        // If iterator has no more traces, then module is complete.
        //        if (_traceReader.isDone()) {
        //          _traceBufferA = null;
        //          _traceBufferB = null;
        //          _status = Status.Completed;
        //          return new TraceData[0];
        //        }
        _traceDataIndex = 0;
        TraceData[] tempData = _traceReader.next();
        if (tempData.length == 0) {
          _status = Status.Completed;
          return tempData;
        }
        if (tempData.length != 2) {
          throw new IllegalArgumentException("Invalid number of trace data objects.");
        }

        _traceBufferA = tempData[0].getTraces();
        _traceBufferB = tempData[1].getTraces();
        _printPrimCoord = true;
      }

      Trace traceAoriginal = _traceBufferA[_traceDataIndex];
      Trace traceBoriginal = _traceBufferB[_traceDataIndex];
      Trace traceA = _traceBufferA[_traceDataIndex];
      Trace traceB = _traceBufferB[_traceDataIndex];
      for (int i = 0; i < _resampleCount; i++) {
        traceA = traceA.resample();
        traceB = traceB.resample();
      }

      double x = traceA.getX();
      double y = traceA.getY();

      computeInlineXline(x, y);

      computeStartAndEndZ(x, y);

      if (!Float.isNaN(_startZ) && !Float.isNaN(_endZ)) {

        // Only pass along if both A and B traces are valid.
        if (!traceA.isMissing() && !traceB.isMissing()) {
          int numSamples = 1 + Math.round((_endZ - _startZ) / _deltaZ);

          float startZA = traceA.getZStart();
          float endZA = traceA.getZEnd();
          float startZB = traceB.getZStart();
          float endZB = traceB.getZEnd();
          Unit unitZA = traceA.getUnitOfZ();
          Unit unitZB = traceB.getUnitOfZ();

          _startZ = Math.max(_startZ, startZA);
          _startZ = Math.max(_startZ, startZB);
          _endZ = Math.min(_endZ, endZA);
          _endZ = Math.min(_endZ, endZB);
          int firstIndexA = Math.round((_startZ - startZA) / _deltaZ);
          int lastIndexA = Math.round((_endZ - startZA) / _deltaZ);
          int firstIndexB = Math.round((_startZ - startZB) / _deltaZ);
          int lastIndexB = Math.round((_endZ - startZB) / _deltaZ);

          // Retrieve the sample data from the traces.
          float[] dataA = traceA.getData();
          float[] dataB = traceB.getData();
          float[] dataX = new float[dataA.length];
          System.arraycopy(dataA, 0, dataX, 0, dataA.length);

          // Run thru the wavelet filters.
          int numSamplesA = traceA.getNumSamples();
          int numSamplesB = traceB.getNumSamples();
          if (_aPrepWaveletFilterFlag) {
            dataA = _traceFilterA.filterTrace(dataA, numSamples, firstIndexA, numSamplesA);
            traceA = new Trace(traceA, dataA);
          }
          if (_bPrepWaveletFilterFlag) {
            dataB = _traceFilterB.filterTrace(dataB, numSamples, firstIndexB, numSamplesB);
            traceB = new Trace(traceB, dataB);
          }

          // Align the traces.
          Trace[] tracesAligned = alignTraces(traceA, traceB, firstIndexA, lastIndexA, firstIndexB, lastIndexB,
              numSamples);
          traceA = tracesAligned[0];
          traceB = tracesAligned[1];

          if (traceA == null) {
            throw new RuntimeException("A trace should not be null");
          }

          if (traceB == null) {
            throw new RuntimeException("B trace should not be null");
          }

          dataA = traceA.getData();
          dataB = traceB.getData();

          // Scale the near,far,intercept,gradient traces.
          scaleTraceData(dataA, dataB);

          // Mask the data in the intercept trace.
          MaskFloatArray.process(dataA, _dataMode);

          // Create new traces from the modified sample data.
          traceA = new Trace(traceA, dataA);
          traceB = new Trace(traceB, dataB);

          Header headerA = new Header(traceA.getHeader());
          float[] tempA = new float[numSamples];
          System.arraycopy(traceA.getData(), firstIndexA, tempA, 0, numSamples);
          traceA = new Trace(_startZ, _deltaZ, unitZA, tempA, traceA.getStatus(), headerA);

          Header headerB = new Header(traceB.getHeader());
          float[] tempB = new float[numSamples];
          System.arraycopy(traceB.getData(), firstIndexB, tempB, 0, numSamples);
          traceB = new Trace(_startZ, _deltaZ, unitZB, tempB, traceB.getStatus(), headerB);

          TraceData traceDataA = new TraceData(new Trace[] { traceA });
          TraceData traceDataB = new TraceData(new Trace[] { traceB });
          dataOut = new TraceData[2];
          dataOut[PRE_PROCESSES_A_TRACE] = traceDataA;
          dataOut[PRE_PROCESSES_B_TRACE] = traceDataB;
          //dataOut[ORIGINAL_A_TRACE] = new TraceData(new Trace[] { traceAoriginal });
          //dataOut[ORIGINAL_B_TRACE] = new TraceData(new Trace[] { traceBoriginal });

          tracesGenerated = true;
        }
      }
    }

    return dataOut;
  }

  /**
   * Cleans up the input process.
   * This includes closing the input volumes.
   */
  public void cleanup() {
    if (_volumeA != null) {
      _volumeA.close();
    }
    if (_volumeB != null) {
      _volumeB.close();
    }
    _monitor = null;
  }

  /**
   * Computes the start z and end z values at the specified x,y coordinates.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   */
  private void computeStartAndEndZ(final double x, final double y) {

    float startZ = _startZ;
    float endZ = _endZ;
    if (_timeMode.equals(ABavoTimeMode.RELATIVE_TO_HORIZON)) {
      double[] rc = _topGridGeometry.transformXYToRowCol(x, y, true);
      Grid3d zProp1 = _topGrid;
      float h1 = zProp1.getValueAtRowCol((int) Math.round(rc[0]), (int) Math.round(rc[1]));
      boolean validStartZ = !zProp1.isNull(h1);
      if (validStartZ) {
        startZ = h1 + _relativeStartZ;
        endZ = h1 + _relativeEndZ;
      } else {
        _startZ = Float.NaN;
        _endZ = Float.NaN;
        return;
      }
    } else if (_timeMode.equals(ABavoTimeMode.RELATIVE_TO_HORIZONS)) {
      double[] rc = _topGridGeometry.transformXYToRowCol(x, y, true);
      Grid3d zProp1 = _topGrid;
      float h1 = zProp1.getValueAtRowCol((int) Math.round(rc[0]), (int) Math.round(rc[1]));
      boolean validStartZ = !zProp1.isNull(h1);

      rc = _baseGridGeometry.transformXYToRowCol(x, y, true);
      Grid3d zProp2 = _baseGrid;
      float h2 = zProp2.getValueAtRowCol((int) Math.round(rc[0]), (int) Math.round(rc[1]));
      boolean validEndZ = !zProp2.isNull(h2);
      if (validStartZ && validEndZ) {
        startZ = h1 + _relativeStartZ;
        endZ = h2 + _relativeEndZ;
      } else {
        _startZ = Float.NaN;
        _endZ = Float.NaN;
        return;
      }
    }
    int index = Math.round(startZ / _deltaZ);
    startZ = index * _deltaZ;
    _startZ = validateZRangeAgainstVolumes(startZ);
    index = Math.round(endZ / _deltaZ);
    endZ = index * _deltaZ;
    _endZ = validateZRangeAgainstVolumes(endZ);
  }

  /**
   * Validates the specified z value against the input volumes.
   * @param z the z value to validate.
   * @return the specified z value, or NaN if invalid.
   */
  private float validateZRangeAgainstVolumes(final float z) {
    if (z < _startZA || z > _endZA || z < _startZB || z > _endZB) {
      return Float.NaN;
    }
    return z;
  }

  /**
   * Computed the inline and xline coordinates from the specified x,y coordinates.
   * The geometry from volume A is used to do the conversion.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   */
  private void computeInlineXline(final double x, final double y) {
    if (!_printPrimCoord) {
      return;
    }
    float inline = 0;
    float xline = 0;
    float[] ixln = _volumeA.getSurvey().transformXYToInlineXline(x, y, true);
    inline = ixln[0];
    xline = ixln[1];
    String message = "";
    if (_isPrimInline) {
      message = "Processing inline: " + inline;
    } else {
      message = "Processing crossline: " + xline;
    }
    if (_monitor != null) {
      _monitor.subTask(message);
    }
    int completion = (int) _traceReader.getCompletion();
    int work = completion - _oldWork;
    _monitor.worked(work);
    _oldWork = completion;
    _printPrimCoord = false;
  }

  /**
   * Unpacks the parameters related to volume A.
   */
  private void unpackVolumeA(final ABavoBaseAlgorithm3d inputModel) {
    // Open the A input volume (near or amplitude).
    _volumeA = inputModel.getVolumeA();
    if (_volumeA == null || _volumeA.getDisplayName().length() <= 0) {
      throw new IllegalArgumentException("Volume A not specified.");
    }
    _startZA = _volumeA.getZStart();
    _endZA = _volumeA.getZEnd();
  }

  /**
   * Unpacks the parameters related to volume B.
   */
  private void unpackVolumeB(final ABavoBaseAlgorithm3d inputModel) {
    // Open the B input volume (far or gradient).
    _volumeB = inputModel.getVolumeB();
    if (_volumeB == null || _volumeB.getDisplayName().length() <= 0) {
      throw new IllegalArgumentException("Volume B not specified.");
    }
    _startZB = _volumeB.getZStart();
    _endZB = _volumeB.getZEnd();
  }

  /**
   * Unpacks the parameters related to the horizon(s).
   */
  private void unpackGrids(final ABavoBaseAlgorithm3d inputModel) {
    if (_timeMode.equals(ABavoTimeMode.RELATIVE_TO_HORIZON)) {

      _topGrid = inputModel.getTopGrid();
      _topGridGeometry = _topGrid.getGeometry();
      if (_topGridGeometry == null || _topGridGeometry.getDisplayName().length() <= 0) {
        throw new IllegalArgumentException("Horizon H1 not specified.");
      }
    } else if (_timeMode.equals(ABavoTimeMode.RELATIVE_TO_HORIZONS)) {

      _topGrid = inputModel.getTopGrid();
      _topGridGeometry = _topGrid.getGeometry();
      if (_topGridGeometry == null || _topGridGeometry.getDisplayName().length() <= 0) {
        throw new IllegalArgumentException("Horizon H1 not specified.");
      }

      _baseGrid = inputModel.getBaseGrid();
      _baseGridGeometry = _baseGrid.getGeometry();
      if (_baseGridGeometry == null || _baseGridGeometry.getDisplayName().length() <= 0) {
        throw new IllegalArgumentException("Horizon H2 not specified.");
      }
    }
    _relativeStartZ = inputModel.getRelativeStart();
    _relativeEndZ = inputModel.getRelativeEnd();
  }

  /**
   * Unpacks the parameters related to the area-of-interest.
   */
  private void unpackAOI(final ABavoBaseAlgorithm3d inputModel) {
    AreaOfInterest aoi = inputModel.getAreaOfInterest();
    if (aoi == null || !inputModel.useAreaOfInterest()) {
      _aoi = _seismicAOI;
    } else {
      _aoi = aoi;
    }
  }

  @Override
  public int getProgress() {
    return Math.max(1, (int) _traceReader.getCompletion());
  }
}
