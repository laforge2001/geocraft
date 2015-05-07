/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo;


import org.geocraft.abavo.defs.ABavoDataMode;
import org.geocraft.abavo.defs.ABavoTimeMode;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.Property;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.TextField;


public abstract class ABavoAbstractAlgorithm extends StandaloneAlgorithm {

  protected static final float EPSILON = 0.01f;

  public static final String VOLUME_A = "Volume A";

  public static final String VOLUME_B = "Volume B";

  public static final String USE_AREA_OF_INTEREST = "Use Area of Interest";

  public static final String AREA_OF_INTEREST = "Area of Interest";

  /** Input data properties. */

  protected EnumProperty<ABavoDataMode> _dataMode;

  protected BooleanProperty _convertNearFarToInterceptGradient;

  protected FloatProperty _nearAngle;

  protected FloatProperty _farAngle;

  protected FloatProperty _volumeScalarA;

  protected FloatProperty _interceptScalar;

  protected FloatProperty _volumeScalarB;

  protected FloatProperty _gradientScalar;

  protected BooleanProperty _useWaveletScalarA;

  protected BooleanProperty _useWaveletScalarB;

  protected EntityProperty<Wavelet> _waveletScalarA;

  protected EntityProperty<Wavelet> _waveletScalarB;

  protected BooleanProperty _autoAlignBtoA;

  protected IntegerProperty _correlationWindow;

  protected IntegerProperty _maximumShift;

  protected FloatProperty _correlationThreshold;

  protected IntegerProperty _smoothingFilterLength;

  protected FloatProperty _nearAmplitudeThreshold;

  protected IntegerProperty _amplitudeThresholdWindow;

  protected FloatProperty _outputSampleRate;

  /** Processing bounds properties. */
  protected BooleanProperty _useAreaOfInterest;

  protected EntityProperty<AreaOfInterest> _areaOfInterest;

  protected EnumProperty<ABavoTimeMode> _timeMode;

  protected FloatProperty _timeStart;

  protected FloatProperty _timeEnd;

  protected FloatProperty _relativeStart;

  protected FloatProperty _relativeEnd;

  protected BooleanProperty _useFullBounds;

  protected boolean _alwaysUseAllData;

  public ABavoAbstractAlgorithm(boolean alwaysUseAllData) {
    super();
    _alwaysUseAllData = alwaysUseAllData;
    addVolumeProperties();
    _dataMode = addEnumProperty("Data Mode", ABavoDataMode.class, ABavoDataMode.ALL_DATA);
    _convertNearFarToInterceptGradient = addBooleanProperty("Calculate Pseudo Intercept and Gradient from Near/Far",
        false);
    _nearAngle = addFloatProperty("Near Angle (deg)", 0);
    _farAngle = addFloatProperty("Far Angle (deg)", 90);
    _volumeScalarA = addFloatProperty("Volume A Scalar", 1);
    _interceptScalar = addFloatProperty("Intercept Scalar", 1);
    _volumeScalarB = addFloatProperty("Volume B Scalar", 1);
    _gradientScalar = addFloatProperty("Gradient Scalar", 1);
    _useWaveletScalarA = addBooleanProperty("Use Wavelet Scalar (A)", false);
    _useWaveletScalarB = addBooleanProperty("Use Wavelet Scalar (B)", false);
    _waveletScalarA = addEntityProperty("Wavelet Scalar (A)", Wavelet.class);
    _waveletScalarB = addEntityProperty("Wavelet Scalar (B)", Wavelet.class);
    _autoAlignBtoA = addBooleanProperty("Auto-align Far to Near", false);
    _correlationWindow = addIntegerProperty("Correlation Window", 80);
    _maximumShift = addIntegerProperty("Maximum Shift", 12);
    _correlationThreshold = addFloatProperty("Correlation Threshold", 0.5f);
    _smoothingFilterLength = addIntegerProperty("Smoothing Filter Length", 40);
    _nearAmplitudeThreshold = addFloatProperty("Near Amplitude Threshold", 20f);
    _amplitudeThresholdWindow = addIntegerProperty("Amplitude Threshold Window", 40);
    _outputSampleRate = addFloatProperty("Output Sample Rate", 0);

    _useAreaOfInterest = addBooleanProperty(USE_AREA_OF_INTEREST, false);
    _areaOfInterest = addEntityProperty(AREA_OF_INTEREST, AreaOfInterest.class);
    addBoundsProperties();
    _useFullBounds = addBooleanProperty("Full Bounds", false);
    _timeMode = addEnumProperty("Time Mode", ABavoTimeMode.class, ABavoTimeMode.BETWEEN_TIMES);
    _timeStart = addFloatProperty("Start Time", 0);
    _timeEnd = addFloatProperty("End Time", 0);
    addGridProperties();
    _relativeStart = addFloatProperty("Relative Start", 0);
    _relativeEnd = addFloatProperty("Relative End", 0);
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection inputData = form.addSection("Input Data");

    // The ALL_DATA options is required for the A+B and Class Background tasks, so set the model accordingly and disable the UI component.
    if (_alwaysUseAllData) {
      _dataMode.set(ABavoDataMode.ALL_DATA);
    } else {
      ABavoDataMode[] dataModes = { ABavoDataMode.ALL_DATA, ABavoDataMode.PEAKS_AND_TROUGHS };
      inputData.addRadioGroupField(_dataMode, dataModes);
    }

    addVolumeFields(inputData);

    inputData.addCheckboxField(_convertNearFarToInterceptGradient).setLabel(
        "Calculate Pseudo Intercept and\nGradient from Near/Far");

    FormSection prepA = form.addSection("Volume A Pre-Processing");

    prepA.addTextField(_volumeScalarA);

    prepA.addTextField(_interceptScalar);

    ComboField waveletA = prepA.addEntityComboField(_waveletScalarA, Wavelet.class);
    waveletA.showActiveFieldToggle(_useWaveletScalarA);

    FormSection prepB = form.addSection("Volume B Pre-Processing");

    TextField volumeScalarB = prepB.addTextField(_volumeScalarB);
    volumeScalarB.setLabel("Volume B Scalar");

    prepB.addTextField(_gradientScalar);

    ComboField waveletB = prepB.addEntityComboField(_waveletScalarB, Wavelet.class);
    waveletB.showActiveFieldToggle(_useWaveletScalarB);

    FormSection alignOpts = form.addSection("A/B Alignment Options");

    alignOpts.addCheckboxField(_autoAlignBtoA);

    alignOpts.addTextField(_correlationWindow);

    alignOpts.addTextField(_maximumShift);

    alignOpts.addTextField(_correlationThreshold);

    alignOpts.addTextField(_smoothingFilterLength);

    alignOpts.addTextField(_nearAmplitudeThreshold);

    alignOpts.addTextField(_amplitudeThresholdWindow);

    FormSection xyBounds = form.addSection("Area of Interest");

    addBoundsFields(xyBounds);

    //xyBounds.addCheckboxField(_useFullBounds);

    FormSection zBounds = form.addSection("Time or Depth Bounds");

    zBounds.addRadioGroupField(_timeMode, ABavoTimeMode.values());

    zBounds.addTextField(_timeStart);

    zBounds.addTextField(_timeEnd);

    addGridFields(zBounds);

    zBounds.addTextField(_relativeStart);

    zBounds.addTextField(_relativeEnd);
  }

  protected abstract void addVolumeProperties();

  protected abstract void addBoundsProperties();

  protected abstract void addGridProperties();

  protected abstract void addVolumeFields(FormSection section);

  protected abstract void addBoundsFields(FormSection section);

  protected abstract void addGridFields(FormSection section);

  protected abstract String getTaskName();

  public ABavoDataMode getDataMode() {
    return _dataMode.get();
  }

  public boolean getConvertNearFarToInterceptGradient() {
    return _convertNearFarToInterceptGradient.get();
  }

  public float getNearAngle() {
    return _nearAngle.get();
  }

  public float getFarAngle() {
    return _farAngle.get();
  }

  public float getVolumeScalarA() {
    return _volumeScalarA.get();
  }

  public float getInterceptScalar() {
    return _interceptScalar.get();
  }

  public float getVolumeScalarB() {
    return _volumeScalarB.get();
  }

  public float getGradientScalar() {
    return _gradientScalar.get();
  }

  public Wavelet getWaveletScalarA() {
    return _waveletScalarA.get();
  }

  public Wavelet getWaveletScalarB() {
    return _waveletScalarB.get();
  }

  public boolean useWaveletScalarA() {
    return _useWaveletScalarA.get();
  }

  public boolean useWaveletScalarB() {
    return _useWaveletScalarB.get();
  }

  public boolean getAutoAlignBtoA() {
    return _autoAlignBtoA.get();
  }

  public int getCorrelationWindow() {
    return _correlationWindow.get();
  }

  public int getMaximumShift() {
    return _maximumShift.get();
  }

  public float getCorrelationThreshold() {
    return _correlationThreshold.get();
  }

  public int getSmoothingFilterLength() {
    return _smoothingFilterLength.get();
  }

  public float getNearAmplitudeThreshold() {
    return _nearAmplitudeThreshold.get();
  }

  public int getAmplitudeThresholdWindow() {
    return _amplitudeThresholdWindow.get();
  }

  public float getOutputSampleRate() {
    return _outputSampleRate.get();
  }

  public boolean useAreaOfInterest() {
    return _useAreaOfInterest.get();
  }

  public AreaOfInterest getAreaOfInterest() {
    return _areaOfInterest.get();
  }

  public ABavoTimeMode getTimeMode() {
    return _timeMode.get();
  }

  public float getTimeStart() {
    return _timeStart.get();
  }

  public float getTimeEnd() {
    return _timeEnd.get();
  }

  public float getRelativeStart() {
    return _relativeStart.get();
  }

  public float getRelativeEnd() {
    return _relativeEnd.get();
  }

  protected boolean useTopGrid() {
    return !_timeMode.get().equals(ABavoTimeMode.BETWEEN_TIMES);
  }

  protected boolean useBaseGrid() {
    return _timeMode.get().equals(ABavoTimeMode.RELATIVE_TO_HORIZONS);
  }

  public int getDataDimension() {
    return 3;
  }

  protected void validateValueInRange(final IValidation results, final Property property, final float value,
      final float start, final float end, final float delta) {
    String key = property.getKey();
    if (Math.abs((value - start) % delta) > EPSILON && Math.abs((value - start) % delta) < Math.abs(delta) - EPSILON) {
      results.error(key, key + " value is invalid");
    }
    if (start < end && (value < start || value > end)) {
      results.error(key, key + " value " + value + " not between " + start + " and " + end);
    }
    if (start > end && (value > start || value < end)) {
      results.error(key, key + " value " + value + " not between " + end + " and " + start);
    }
  }

}
