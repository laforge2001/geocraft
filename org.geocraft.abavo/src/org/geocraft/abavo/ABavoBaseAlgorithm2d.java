/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.abavo.defs.ABavoDataMode;
import org.geocraft.abavo.defs.ABavoTimeMode;
import org.geocraft.abavo.input.InputProcess2d;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.Property;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.field.aoi.AOIComboField;


public abstract class ABavoBaseAlgorithm2d extends ABavoAbstractAlgorithm {

  public static final String LINE_NAME = "Line Name";

  /** Input data properties. */

  protected EntityProperty<PostStack2d> _volumeA;

  protected EntityProperty<PostStack2d> _volumeB;

  protected StringProperty _lineName;

  /** The seismic starting shotpoint property. */
  protected FloatProperty _shotpointStart;

  /** The seismic ending shotpoint property. */
  protected FloatProperty _shotpointEnd;

  protected EntityProperty<Grid2d> _topGrid;

  protected EntityProperty<Grid2d> _baseGrid;

  protected boolean _multiLineEnabled;

  public ABavoBaseAlgorithm2d(final boolean alwaysUseAllData, final boolean multiLineEnabled) {
    super(alwaysUseAllData);
    _multiLineEnabled = multiLineEnabled;
  }

  @Override
  protected void addVolumeProperties() {
    _volumeA = addEntityProperty(VOLUME_A, PostStack2d.class);
    _volumeB = addEntityProperty(VOLUME_B, PostStack2d.class);
    _lineName = addStringProperty(LINE_NAME, "");
  }

  @Override
  protected void addBoundsProperties() {
    _shotpointStart = addFloatProperty("Shotpoint Start", 0);
    _shotpointEnd = addFloatProperty("Shotpoint End", 0);
  }

  @Override
  protected void addGridProperties() {
    _topGrid = addEntityProperty("Top Grid", Grid2d.class);
    _baseGrid = addEntityProperty("Base Grid", Grid2d.class);
  }

  @Override
  protected void addVolumeFields(FormSection section) {
    section.addEntityComboField(_volumeA, PostStack2d.class);
    section.addEntityComboField(_volumeB, PostStack2d.class);
  }

  @Override
  protected void addBoundsFields(FormSection section) {
    if (_multiLineEnabled) {
      AOIComboField aoiField = section.addAOIComboField(_areaOfInterest, 2, SeismicSurvey2dAOI.class);
      aoiField.setLabel("Lines of Interest");
      aoiField.showActiveFieldToggle(_useAreaOfInterest);
    }

    section.addListSelectionField(LINE_NAME, new String[0]);
    section.addTextField(_shotpointStart);
    section.addTextField(_shotpointEnd);
  }

  @Override
  protected void addGridFields(FormSection section) {
    section.addEntityComboField(_topGrid, Grid2d.class);
    section.addEntityComboField(_baseGrid, Grid2d.class);
  }

  public void propertyChanged(String key) {
    String propertyName = key;
    if (propertyName.equals(VOLUME_A)) {
      if (!_volumeA.isNull()) {
        PostStack2d volumeA = _volumeA.get();
        String[] lineNames = volumeA.getLineNames(true);
        setFieldOptions(_lineName, lineNames);
      }
    }
    // If the input volume A, line name, AOI or AOI flag changes, then see if the output sample rate is to be updated.
    if (propertyName.equals(VOLUME_A) || propertyName.equals(LINE_NAME) || propertyName.equals(AREA_OF_INTEREST)
        || propertyName.equals(USE_AREA_OF_INTEREST)) {

      // First check that the input volume A is specified.
      if (_volumeA.isNull()) {
        // If not, them disable the output sample rate field.
        setFieldEnabled(_outputSampleRate, false);
      } else {
        // Otherwise, get the input volume A.
        PostStack2d volumeA = _volumeA.get();

        // Check if an AOI is set to be used.
        boolean useAOI = _useAreaOfInterest.get();
        if (!useAOI) {
          // If not, then use the line name field, if specified.
          setFieldEnabled(_outputSampleRate, !_lineName.isEmpty());
          if (!_lineName.isEmpty()) {
            String lineName = _lineName.get();
            PostStack2dLine volumeLineA = volumeA.getPostStack2dLine(lineName);
            if (volumeLineA != null) {
              float deltaZ = volumeLineA.getZDelta();
              Float[] options = new Float[3];
              for (int i = 0; i < 3; i++) {
                options[i] = new Float(deltaZ);
                deltaZ *= 0.5f;
              }
              setFieldOptions(_outputSampleRate, options);
              _outputSampleRate.set(volumeLineA.getZDelta());
            }
          }
        } else {
          // If so, then use one of the lines from the AOI, if specified.
          setFieldEnabled(_outputSampleRate, !_areaOfInterest.isNull());
          if (!_areaOfInterest.isNull()) {
            SeismicSurvey2dAOI aoi = (SeismicSurvey2dAOI) _areaOfInterest.get();
            String lineName = aoi.getCdpRanges().keySet().iterator().next();
            PostStack2dLine volumeLineA = volumeA.getPostStack2dLine(lineName);
            if (volumeLineA != null) {
              float deltaZ = volumeLineA.getZDelta();
              Float[] options = new Float[3];
              for (int i = 0; i < 3; i++) {
                options[i] = new Float(deltaZ);
                deltaZ *= 0.5f;
              }
              setFieldOptions(_outputSampleRate, options);
              _outputSampleRate.set(volumeLineA.getZDelta());
            }
          }
        }
      }
    }
    if (propertyName.equals(USE_AREA_OF_INTEREST)) {
      boolean useAOI = _useAreaOfInterest.get();
      setFieldEnabled(_lineName, !useAOI);
      setFieldEnabled(_shotpointStart, !useAOI);
      setFieldEnabled(_shotpointEnd, !useAOI);
    }

    if (propertyName.equals(_convertNearFarToInterceptGradient.getKey())) {
      boolean convert = _convertNearFarToInterceptGradient.get();
      setFieldEnabled(_interceptScalar, convert);
      setFieldEnabled(_gradientScalar, convert);
    } else if (propertyName.equals(_autoAlignBtoA.getKey())) {
      boolean autoAlign = _autoAlignBtoA.get();
      setFieldEnabled(_correlationWindow, autoAlign);
      setFieldEnabled(_maximumShift, autoAlign);
      setFieldEnabled(_correlationThreshold, autoAlign);
      setFieldEnabled(_smoothingFilterLength, autoAlign);
      setFieldEnabled(_nearAmplitudeThreshold, autoAlign);
      setFieldEnabled(_amplitudeThresholdWindow, autoAlign);
    } else if (propertyName.equals(VOLUME_A) || propertyName.equals(LINE_NAME)) {
      if (!_volumeA.isNull() && !_lineName.isEmpty()) {
        PostStack2d volumeA = _volumeA.get();
        String lineName = _lineName.get();
        PostStack2dLine volumeLineA = volumeA.getPostStack2dLine(lineName);
        if (volumeLineA != null) {
          String zUnitStr = volumeLineA.getZUnit().getSymbol();
          setFieldLabel(_correlationWindow, "Correlation Window (" + zUnitStr + ")");
          setFieldLabel(_maximumShift, "Maximum Shift (" + zUnitStr + ")");
          setFieldLabel(_smoothingFilterLength, "Smoothing Filter Length (" + zUnitStr + ")");
          setFieldLabel(_amplitudeThresholdWindow, "Ampl. Threshold Window (" + zUnitStr + ")");
        }
      }
    } else if (propertyName.equals(AREA_OF_INTEREST)) {
      if (!_volumeA.isNull() && !_areaOfInterest.isNull()) {
        PostStack2d volumeA = _volumeA.get();
        SeismicSurvey2dAOI aoi = (SeismicSurvey2dAOI) _areaOfInterest.get();
        String lineName = aoi.getCdpRanges().keySet().iterator().next();
        PostStack2dLine volumeLineA = volumeA.getPostStack2dLine(lineName);
        if (volumeLineA != null) {
          String zUnitStr = volumeLineA.getZUnit().getSymbol();
          setFieldLabel(_correlationWindow, "Correlation Window (" + zUnitStr + ")");
          setFieldLabel(_maximumShift, "Maximum Shift (" + zUnitStr + ")");
          setFieldLabel(_smoothingFilterLength, "Smoothing Filter Length (" + zUnitStr + ")");
          setFieldLabel(_amplitudeThresholdWindow, "Ampl. Threshold Window (" + zUnitStr + ")");
        }
      }
    }

    // Update the bounds sections.
    if (propertyName.equals(_timeMode.getKey())) {
      setFieldEnabled(_timeStart, !useTopGrid());
      setFieldEnabled(_timeEnd, !useTopGrid());
      setFieldEnabled(_topGrid, useTopGrid());
      setFieldEnabled(_baseGrid, useBaseGrid());
      setFieldEnabled(_relativeStart, useTopGrid() || useBaseGrid());
      setFieldEnabled(_relativeEnd, useTopGrid() || useBaseGrid());
    } else if (propertyName.equals(VOLUME_A) || propertyName.equals(LINE_NAME)) {
      if (!_volumeA.isNull() && !_lineName.isEmpty()) {
        PostStack2d volumeA = _volumeA.get();
        String lineName = _lineName.get();
        PostStack2dLine volumeLineA = volumeA.getPostStack2dLine(lineName);
        if (volumeLineA != null) {
          _shotpointStart.set(volumeLineA.getShotpointStart());
          _shotpointEnd.set(volumeLineA.getShotpointEnd());
          _timeStart.set(volumeLineA.getZStart());
          _timeEnd.set(volumeLineA.getZEnd());
          Domain domain = volumeLineA.getZDomain();
          String zUnitStr = volumeLineA.getZUnit().getSymbol();
          setFieldLabel(_timeMode, domain + " Mode");
          setFieldLabel(_timeStart, "Start " + domain + " (" + zUnitStr + ")");
          setFieldLabel(_timeEnd, "End " + domain + " (" + zUnitStr + ")");
          setFieldLabel(_relativeStart, "Relative Start (" + zUnitStr + ")");
          setFieldLabel(_relativeEnd, "Relative End (" + zUnitStr + ")");
        }
      }
    }
  }

  public void validate(IValidation results) {

    PostStack2d volumeA = _volumeA.get();
    PostStack2d volumeB = _volumeB.get();
    String lineName = _lineName.get();
    PostStack2dLine volumeLineA = null;
    PostStack2dLine volumeLineB = null;

    if (volumeA == null) {
      results.error(_volumeA, "Volume A not specified.");
    } else {
      if (!useAreaOfInterest()) {
        if (!lineName.isEmpty()) {
          if (volumeA.containsPostStack2d(lineName)) {
            volumeLineA = volumeA.getPostStack2dLine(lineName);
          } else {
            results.error(_volumeA, "Volume A not defined on line " + lineName);
          }
        }
      }
    }
    if (volumeB == null) {
      results.error(_volumeB, "Volume B not specified.");
    } else {
      if (!useAreaOfInterest()) {
        if (!lineName.isEmpty()) {
          if (volumeB.containsPostStack2d(lineName)) {
            volumeLineB = volumeB.getPostStack2dLine(lineName);
          } else {
            results.error(_volumeB, "Volume B not defined on line " + lineName);
          }
        }
      }
    }

    if (volumeLineA != null) {
      float outputSampleRate = _outputSampleRate.get();
      if (MathUtil.isEqual(outputSampleRate, 0)) {
        results.error(_outputSampleRate, "Output sample rate cannot be zero.");
      } else {
        int ratio = Math.round(volumeLineA.getZDelta() / outputSampleRate);
        if (ratio != 1 && ratio != 2 && ratio != 4) {
          results.error(_outputSampleRate, "Output sample rate is invalid: " + outputSampleRate + ".");
        }
      }
    }
    if (volumeLineA != null && volumeLineB != null) {
      if (!volumeLineA.getZDomain().equals(volumeLineB.getZDomain())) {
        results.error(_volumeB, "Volume B domain (" + volumeLineB.getZDomain() + ") does not match volume A domain ("
            + volumeLineA.getZDomain() + ")");
      }
      if (!volumeLineA.getClass().equals(volumeLineB.getClass())) {
        results.error(_volumeB, "Volume B dataset type (" + volumeLineB.getClass().getSimpleName()
            + ") does not match volume A dataset type (" + volumeLineA.getClass().getSimpleName() + ")");
      }
    }
    if (!useAreaOfInterest() && lineName.isEmpty()) {
      results.error(_lineName, "Line not specified.");
    }
    ABavoDataMode dataMode = _dataMode.get();
    if (dataMode == null) {
      results.error(_dataMode, "Invalid data mode: " + dataMode);
    }
    float nearAngle = _nearAngle.get();
    if (nearAngle < 0 || nearAngle > 90) {
      results.error(_nearAngle, "Near angle must be between 0 and 90");
    }
    float farAngle = _farAngle.get();
    if (farAngle < 0 || farAngle > 90) {
      results.error(_farAngle, "Far angle must be between 0 and 90");
    }
    if (farAngle < nearAngle) {
      results.error(_farAngle, "Far angle must be greater than near angle");
    }
    if (_autoAlignBtoA.get()) {
      if (_correlationWindow.get() < 0) {
        results.error(_correlationWindow, "Correlation window cannot be negative");
      }
      if (_maximumShift.get() < 0) {
        results.error(_maximumShift, "Maximum shift cannot be negative");
      }
      if (_correlationThreshold.get() < 0 || _correlationThreshold.get() > 1) {
        results.error(_correlationThreshold, "Correlation threshold must be between 0 and 1");
      }
      if (_smoothingFilterLength.get() < 0) {
        results.error(_smoothingFilterLength, "Smoothing filter length cannot be negative");
      }
      if (_nearAmplitudeThreshold.get() < 0) {
        results.error(_nearAmplitudeThreshold, "Near amplitude threshold cannot be negative");
      }
      if (_amplitudeThresholdWindow.get() < 0) {
        results.error(_amplitudeThresholdWindow, "Amplitude threshold window cannot be negative");
      }
    }
    float outputSampleRate = _outputSampleRate.get();

    if (useWaveletScalarA()) {
      if (_waveletScalarA.isNull()) {
        results.error(_waveletScalarA, "The A wavelet filter must be specified.");
      } else {
        if (volumeLineA != null) {
          Wavelet waveletA = _waveletScalarA.get();
          if (Float.compare(outputSampleRate, waveletA.getTimeInterval()) != 0) {
            results.error(_waveletScalarA, "The interval of the A wavelet filter (" + waveletA.getTimeInterval()
                + ") does not match the output sample rate (" + outputSampleRate + ").");
          }
        }
      }
    }

    if (useWaveletScalarB()) {
      if (_waveletScalarB.isNull()) {
        results.error(_waveletScalarB, "The B wavelet filter must be specified.");
      } else {
        if (volumeLineB != null) {
          Wavelet waveletB = _waveletScalarB.get();
          if (Float.compare(outputSampleRate, waveletB.getTimeInterval()) != 0) {
            results.error(_waveletScalarB, "The interval of the B wavelet filter (" + waveletB.getTimeInterval()
                + ") does not match the output sample rate (" + outputSampleRate + ").");
          }
        }
      }
    }
    if (useAreaOfInterest()) {
      if (_areaOfInterest.isNull()) {
        results.error(_areaOfInterest, "The area of interest must be specified");
      }
    } else {
      if (volumeLineA != null) {
        float shotpointStart = _shotpointStart.get();
        float shotpointEnd = _shotpointEnd.get();
        //validateValueInRange(results, _inlineStart, inlineStart, ps2d.getLineNumber(), ps2d.getLineNumber(), 1);
        //validateValueInRange(results, _inlineEnd, inlineEnd, ps2d.getLineNumber(), ps2d.getLineNumber(), 1);
        validateValueInRange(results, _shotpointStart, shotpointStart, volumeLineA.getShotpointStart(),
            volumeLineA.getShotpointEnd(), Float.NaN);
        validateValueInRange(results, _shotpointEnd, shotpointEnd, volumeLineA.getShotpointStart(),
            volumeLineA.getShotpointEnd(), Float.NaN);

      }
    }
    ABavoTimeMode timeMode = _timeMode.get();
    if (timeMode == null) {
      results.error(_timeMode, "Invalid time bounds: " + timeMode);
    } else {
      if (timeMode.equals(ABavoTimeMode.BETWEEN_TIMES)) {
        float timeStart = _timeStart.get();
        float timeEnd = _timeEnd.get();
        if (timeEnd < timeStart) {
          results.error(_timeEnd, "End time must be >= start time");
        }
        if (volumeLineA != null) {
          if (timeStart < volumeLineA.getZStart() || timeStart > volumeLineA.getZEnd()) {
            results.error(_timeStart,
                "Start time must be between " + volumeLineA.getZStart() + " and " + volumeLineA.getZEnd());
          }
          if (timeEnd < volumeLineA.getZStart() || timeEnd > volumeLineA.getZEnd()) {
            results.error(_timeEnd,
                "End time must be between " + volumeLineA.getZStart() + " and " + volumeLineA.getZEnd());
          }
        }
      }
      Grid2d topGrid = _topGrid.get();
      Grid2d baseGrid = _baseGrid.get();
      if (useTopGrid() && _topGrid.isNull()) {
        results.error(_topGrid, "Top horizon must be specified.");
      }
      if (useBaseGrid() && _baseGrid.isNull()) {
        results.error(_baseGrid, "Base horizon must be specified.");
      }
      if (useTopGrid() && topGrid != null && volumeLineA != null) {
        Unit topGridUnit = topGrid.getDataUnit();
        Domain topGridDomain = topGridUnit.getDomain();
        if (topGridDomain == null || !volumeLineA.getZDomain().equals(topGridDomain)) {
          results.error(_topGrid, "Top horizon domain (" + topGridDomain + ") does not match seismic z domain ("
              + volumeLineA.getZDomain() + ")");
        }
      }
      if (useBaseGrid() && baseGrid != null && volumeLineA != null) {
        Unit baseGridUnit = baseGrid.getDataUnit();
        Domain baseGridDomain = baseGridUnit.getDomain();
        if (baseGridDomain == null || !volumeLineA.getZDomain().equals(baseGridDomain)) {
          results.error(_baseGrid, "Base horizon domain (" + baseGridDomain + ") does not match seismic z domain ("
              + volumeLineA.getZDomain() + ")");
        }
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    StringBuilder errors = new StringBuilder();

    String[] lineNames = getLines();
    int numLines = lineNames.length;

    // Begin the task.
    String taskName = getTaskName();
    monitor.beginTask(taskName, 100 * numLines);
    for (String lineName : lineNames) {
      InputProcess2d inputProcess = null;
      try {
        initialize(repository, lineName);

        inputProcess = new InputProcess2d(this, lineName);
        inputProcess.setProgressMonitor(monitor);

        int oldWork = 0;
        while (!inputProcess.isDone()) {
          TraceData[] traceData = inputProcess.process();

          processTraceData(traceData, lineName);

          int completion = inputProcess.getProgress();
          int work = completion - oldWork;
          monitor.worked(work);
          oldWork = completion;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        // For now, store any errors and allow rest of lines to process.
        errors.append("Error processing line: " + lineName + ".\n" + ex.getMessage());
      } finally {
        // Cleanup.
        if (inputProcess != null) {
          inputProcess.cleanup();
        }
        cleanup();
      }
    }

    // When all the lines are done, log any of the stored errors.
    if (errors.length() > 0) {
      logger.error(errors.toString());
    }

    // Long the appropriate end-of-task message.
    if (!monitor.isCanceled()) {
      logger.info(taskName + " task done!");
    } else {
      logger.info(taskName + " task canceled!");
    }
    monitor.done();
  }

  protected String[] getLines() {
    boolean useAOI = useAreaOfInterest();
    if (useAOI) {
      AreaOfInterest aoi = _areaOfInterest.get();
      if (aoi instanceof SeismicSurvey2dAOI) {
        SeismicSurvey2dAOI seismicAOI = (SeismicSurvey2dAOI) aoi;
        List<String> lineNames = new ArrayList<String>();
        for (String lineName : seismicAOI.getCdpRanges().keySet()) {
          lineNames.add(lineName);
        }
        Collections.sort(lineNames);
        return lineNames.toArray(new String[0]);
      }
    }
    return new String[] { _lineName.get() };
  }

  protected void initialize(IRepository repository, final String lineName) {
    // Nothing to do.
  }

  protected void cleanup() {
    // Nothing to do.
  }

  protected abstract void processTraceData(final TraceData[] traceData, final String lineName);

  @Override
  protected abstract String getTaskName();

  public PostStack2d getVolumeA() {
    return _volumeA.get();
  }

  public PostStack2d getVolumeB() {
    return _volumeB.get();
  }

  public float getShotpointStart() {
    return _shotpointStart.get();
  }

  public float getShotpointEnd() {
    return _shotpointEnd.get();
  }

  public Grid2d getTopGrid() {
    return _topGrid.get();
  }

  public Grid2d getBaseGrid() {
    return _baseGrid.get();
  }

  @Override
  public int getDataDimension() {
    return 2;
  }

  @Override
  protected void validateValueInRange(final IValidation results, final Property property, final float value,
      final float start, final float end, final float delta) {
    String key = property.getKey();

    // Ignore the delta epsilon check if the delta value is NaN.
    if (!Float.isNaN(delta)) {
      if (Math.abs((value - start) % delta) > EPSILON && Math.abs((value - start) % delta) < Math.abs(delta) - EPSILON) {
        results.error(key, key + " value is invalid");
      }
    }
    if (start < end && (value < start || value > end)) {
      results.error(key, key + " value " + value + " not between " + start + " and " + end);
    }
    if (start > end && (value > start || value < end)) {
      results.error(key, key + " value " + value + " not between " + end + " and " + start);
    }
  }

  @Override
  protected String[] getUnpickleKeyOrder() {
    return new String[] { VOLUME_A, VOLUME_B };
  }
}
