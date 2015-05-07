/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.input;


import java.util.HashMap;
import java.util.Map;

import org.geocraft.abavo.ABavoBaseAlgorithm2d;
import org.geocraft.abavo.defs.ABavoTimeMode;
import org.geocraft.abavo.process.MaskFloatArray;
import org.geocraft.abavo.process.NearFarToInterceptGradient;
import org.geocraft.abavo.process.ScaleFloatArray;
import org.geocraft.abavo.process.TraceAlignment;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.LineGeometry;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.io.util.MultiVolumeTraceIterator;
import org.geocraft.math.wavelet.WaveletFilter;


/**
 * Defines the trace input process that is common to all the trace processing flows.
 * 1) Loads the specified Near/Far or Intercept/Gradient files.
 * 2) Filters the A,B traces based on the specified wavelets (optional).
 * 3) Aligns the Far trace to the near trace (optional, Near/Far data only).
 * 4) Scales the A,B traces (optional).
 */
public class InputProcess2d extends AbstractInputProcess {

  /** The input volume A. */
  protected PostStack2dLine _volumeA;

  /** The input volume B. */
  protected PostStack2dLine _volumeB;

  /** The top horizon grid (optional). */
  protected Grid2d _topGrid;

  /** The base horizon grid (optional). */
  protected Grid2d _baseGrid;

  /** The 2D line name. */
  protected String _lineName;

  /**
   * Constructs the input process.
   * The project dimension currently must be 3 for 3D (2D is not yet supported).
   * The process also takes 2 models: the ABAVO input model (containing the data and pre-processing) and
   * the ABAVO bounds model (containing the x,y,z bounds over which to process).
   * @param projectDimension the project dimension.
   * @param inputModel the input model.
   * @param boundsModel the bounds model.
   */
  public InputProcess2d(final ABavoBaseAlgorithm2d inputModel, final String lineName) {
    super(2);
    _lineName = lineName;
    _status = Status.Idle;
    initialize(inputModel, lineName);
  }

  /**
   * Initializes the input process.
   */
  public void initialize(final ABavoBaseAlgorithm2d inputModel, final String lineName) {

    unpackVolumeA(inputModel, lineName);

    unpackVolumeB(inputModel, lineName);

    SeismicLine2d seismicLine = _volumeA.getSeismicLine();

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

    _isPrimInline = true;

    // Set the default inlne/xline/time processing range.
    float shot0 = _volumeA.getShotpointStart();
    float shot1 = _volumeA.getShotpointEnd();
    float cdp0 = seismicLine.transformShotpointToCDP(shot0);
    float cdp1 = seismicLine.transformShotpointToCDP(shot1);

    shot0 = inputModel.getShotpointStart();
    shot1 = inputModel.getShotpointEnd();
    cdp0 = Math.round(seismicLine.transformShotpointToCDP(shot0));
    cdp1 = Math.round(seismicLine.transformShotpointToCDP(shot1));

    Map<String, FloatRange> cdpRanges = new HashMap<String, FloatRange>();
    cdpRanges.put(_volumeA.getLineName(), new FloatRange(cdp0, cdp1, 1));
    _seismicAOI = new SeismicSurvey2dAOI("aoi", cdpRanges);

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
        if (!_traceIterator.hasNext()) {
          _traceBufferA = null;
          _traceBufferB = null;
          _status = Status.Completed;
          return new TraceData[0];
        }
        _traceDataIndex = 0;
        TraceData[] tempData = _traceIterator.next();
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

      //CoordinateSeries coords = _volumeA.getSeismicGeometry().transformInlineXlineToXY(_inlines, _xlines);
      double x = traceA.getX();
      double y = traceA.getY();
      float shotpoint = traceA.getHeader().getFloat(TraceHeaderCatalog.SHOTPOINT_NO);
      int bin = 0;

      if (_traceDataIndex % 1000 == 0) {
        updateTaskMessage(_volumeA.getLineName(), shotpoint);
      }

      float cdp = traceA.getHeader().getInteger(TraceHeaderCatalog.CDP_NO);
      PostStack2dLine volumeA = _volumeA;
      bin = (int) Math.round(volumeA.getSeismicLine().transformCdpToBin(cdp));

      computeStartAndEndZ(x, y, bin);

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
  private void computeStartAndEndZ(final double x, final double y, final int bin) {

    float startZ = _startZ;
    float endZ = _endZ;
    if (_timeMode.equals(ABavoTimeMode.RELATIVE_TO_HORIZON)) {
      int lineNumber = _volumeA.getLineNumber();
      Grid2d zProp1 = _topGrid;
      float h1 = zProp1.getValues(lineNumber)[bin];
      boolean validStartZ = !zProp1.isNull(bin);
      if (validStartZ) {
        startZ = h1 + _relativeStartZ;
        endZ = h1 + _relativeEndZ;
      } else {
        _startZ = Float.NaN;
        _endZ = Float.NaN;
        return;
      }
    } else if (_timeMode.equals(ABavoTimeMode.RELATIVE_TO_HORIZONS)) {
      int lineNumber = _volumeA.getLineNumber();
      Grid2d zProp1 = _topGrid;
      float h1 = zProp1.getValues(lineNumber)[bin];
      boolean validStartZ = !zProp1.isNull(bin);

      Grid2d zProp2 = _baseGrid;
      float h2 = zProp2.getValues(lineNumber)[bin];
      boolean validEndZ = !zProp2.isNull(bin);
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

  private void updateTaskMessage(final String lineName, final float shotpoint) {
    if (_monitor != null) {
      String message = "Processing line " + lineName + " shotpoint " + shotpoint;
      _monitor.subTask(message);
    }
  }

  /**
   * Unpacks the parameters related to volume A.
   */
  private void unpackVolumeA(final ABavoBaseAlgorithm2d inputModel, final String lineName) {
    // Open the A input volume (near or amplitude).
    _volumeA = inputModel.getVolumeA().getPostStack2dLine(lineName);
    if (_volumeA == null || _volumeA.getDisplayName().length() <= 0) {
      throw new IllegalArgumentException("Volume A not specified.");
    }
    _startZA = _volumeA.getZStart();
    _endZA = _volumeA.getZEnd();
  }

  /**
   * Unpacks the parameters related to volume B.
   */
  private void unpackVolumeB(final ABavoBaseAlgorithm2d inputModel, final String lineName) {
    // Open the B input volume (far or gradient).
    _volumeB = inputModel.getVolumeB().getPostStack2dLine(lineName);
    if (_volumeB == null || _volumeB.getDisplayName().length() <= 0) {
      throw new IllegalArgumentException("Volume B not specified.");
    }
    _startZB = _volumeB.getZStart();
    _endZB = _volumeB.getZEnd();
  }

  /**
   * Unpacks the parameters related to the horizon(s).
   */
  private void unpackGrids(final ABavoBaseAlgorithm2d inputModel) {
    if (_timeMode.equals(ABavoTimeMode.RELATIVE_TO_HORIZON)) {

      int lineNumber = _volumeA.getLineNumber();
      _topGrid = inputModel.getTopGrid();
      LineGeometry topLineGeometry = _topGrid.getGridGeometry().getLineByNumber(lineNumber);
      if (topLineGeometry == null || topLineGeometry.getDisplayName().length() <= 0) {
        throw new IllegalArgumentException("Horizon H1 not specified.");
      }
    } else if (_timeMode.equals(ABavoTimeMode.RELATIVE_TO_HORIZONS)) {

      int lineNumber = _volumeA.getLineNumber();
      _topGrid = inputModel.getTopGrid();
      LineGeometry topLineGeometry = _topGrid.getGridGeometry().getLineByNumber(lineNumber);
      if (topLineGeometry == null || topLineGeometry.getDisplayName().length() <= 0) {
        throw new IllegalArgumentException("Horizon H1 not specified.");
      }

      _baseGrid = inputModel.getBaseGrid();
      LineGeometry baseLineGeometry = _baseGrid.getGridGeometry().getLineByNumber(lineNumber);
      if (baseLineGeometry == null || baseLineGeometry.getDisplayName().length() <= 0) {
        throw new IllegalArgumentException("Horizon H2 not specified.");
      }
    }
    _relativeStartZ = inputModel.getRelativeStart();
    _relativeEndZ = inputModel.getRelativeEnd();
  }

  /**
   * Unpacks the parameters related to the area-of-interest.
   */
  private void unpackAOI(final ABavoBaseAlgorithm2d inputModel) {
    AreaOfInterest aoi = inputModel.getAreaOfInterest();
    if (aoi == null || !inputModel.useAreaOfInterest()) {
      _aoi = _seismicAOI;
    } else {
      _aoi = aoi;
    }
    _traceIterator = new MultiVolumeTraceIterator(_aoi, _volumeA, _volumeB);
  }

}
