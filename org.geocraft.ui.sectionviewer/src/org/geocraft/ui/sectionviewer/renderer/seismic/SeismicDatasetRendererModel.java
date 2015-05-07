/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.beans.PropertyChangeEvent;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.GrayscaleColorMap;
import org.geocraft.core.color.map.IColorMap;
import org.geocraft.core.color.map.SeismicColorMap;
import org.geocraft.core.common.math.AGC;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorBarProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.sectionviewer.InterpolationMethod;
import org.geocraft.ui.sectionviewer.NormalizationMethod;
import org.geocraft.ui.sectionviewer.component.ITraceRenderStyle;


public class SeismicDatasetRendererModel extends Model implements ISeismicDatasetRendererConstants {

  protected Unit _dataUnit;

  protected EntityProperty<SeismicDataset> _overlayDataset;

  /** The flag to reverse the displayed trace polarity. */
  protected BooleanProperty _reversePolarity;

  /** The trace interpolation method. */
  protected EnumProperty<InterpolationMethod> _interpolationMethod;

  /** The trace interpolation method. */
  protected EnumProperty<NormalizationMethod> _normalizationMethod;

  /** The trace exageration scalar. */
  protected FloatProperty _traceExaggeration;

  /** The maximum trace width for clipping. */
  protected IntegerProperty _traceClipping;

  /** The color opacity (0-100). */
  protected IntegerProperty _transparency;

  protected BooleanProperty _wiggleTrace;

  protected BooleanProperty _positiveColorFill;

  protected BooleanProperty _negativeColorFill;

  protected BooleanProperty _positiveDensityFill;

  protected BooleanProperty _negativeDensityFill;

  protected BooleanProperty _variableDensity;

  /** The null-value color. */
  protected ColorProperty _nullColor;

  /** The wiggle trace color. */
  protected ColorProperty _wiggleTraceColor;

  /** The positive fill color. */
  protected ColorProperty _positiveFillColor;

  /** The negative fill color. */
  protected ColorProperty _negativeFillColor;

  private final ColorBarProperty _overlayColorBar;

  /** The clipping percentile. */
  private final IntegerProperty _percentile;

  /** The flag indicating whether to recompute start/end ranges based on clipping percentile. */
  private boolean _recomputePercentile = true;

  private final BooleanProperty _agcApply;

  private final EnumProperty<AGC.Type> _agcType;

  private final FloatProperty _agcWindowLength;

  private final BooleanProperty _geometricGainApply;

  private final FloatProperty _geometricGainT0;

  private final FloatProperty _geometricGainTMax;

  private final FloatProperty _geometricGainN;

  /**
   * The default constructor.
   */
  public SeismicDatasetRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    IPreferenceStore preferences = SeismicDatasetRendererPreferencePage.PREFERENCE_STORE;

    String interpMethodStr = preferences.getString(INTERPOLATION_METHOD);
    InterpolationMethod interpMethod = InterpolationMethod.lookup(interpMethodStr);
    String normMethodStr = preferences.getString(NORMALIZATION_METHOD);
    NormalizationMethod normMethod = NormalizationMethod.lookup(normMethodStr);
    float traceEx = preferences.getFloat(TRACE_EXAGGERATION);
    int traceClip = preferences.getInt(TRACE_CLIPPING);
    int percentile = preferences.getInt(PERCENTILE);
    int transparency = preferences.getInt(TRANSPARENCY);
    boolean reversePolarity = preferences.getBoolean(REVERSE_POLARITY);
    boolean wiggleTrace = preferences.getBoolean(WIGGLE_TRACE);
    boolean posColorFill = preferences.getBoolean(POSITIVE_COLOR_FILL);
    boolean negColorFill = preferences.getBoolean(NEGATIVE_COLOR_FILL);
    boolean posDensityFill = preferences.getBoolean(POSITIVE_DENSITY_FILL);
    boolean negDensityFill = preferences.getBoolean(NEGATIVE_DENSITY_FILL);
    boolean varDensity = preferences.getBoolean(VARIABLE_DENSITY);
    RGB colorWiggle = PreferenceConverter.getColor(preferences, COLOR_WIGGLE);
    RGB colorPosFill = PreferenceConverter.getColor(preferences, COLOR_POSITIVE_FILL);
    RGB colorNegFill = PreferenceConverter.getColor(preferences, COLOR_NEGATIVE_FILL);
    RGB colorNull = PreferenceConverter.getColor(preferences, COLOR_NULL);
    String colorMapName = preferences.getString(OTHER_COLOR_MAP);
    IColorMap colorMap = null;
    for (ColorMapDescription colorMapDesc : ServiceProvider.getColorMapService().getAll()) {
      if (colorMapName.equals(colorMapDesc.getName())) {
        colorMap = colorMapDesc.createMap();
      }
    }
    if (colorMap == null) {
      colorMap = new SeismicColorMap();
    }
    boolean agcApply = preferences.getBoolean(AGC_APPLY);
    String agcTypeStr = preferences.getString(AGC_TYPE);
    AGC.Type agcType = AGC.Type.lookup(agcTypeStr);
    float agcWindow = preferences.getFloat(AGC_WINDOW_LENGTH);
    boolean gainApply = preferences.getBoolean(GEOMETRIC_GAIN_APPLY);
    float gainT0 = preferences.getFloat(GEOMETRIC_GAIN_T0);
    float gainN = preferences.getFloat(GEOMETRIC_GAIN_N);
    float gainTmax = preferences.getFloat(GEOMETRIC_GAIN_TMAX);

    _interpolationMethod = addEnumProperty(INTERPOLATION_METHOD, InterpolationMethod.class, interpMethod);

    _normalizationMethod = addEnumProperty(NORMALIZATION_METHOD, NormalizationMethod.class, normMethod);

    _traceExaggeration = addFloatProperty(TRACE_EXAGGERATION, traceEx);

    _traceClipping = addIntegerProperty(TRACE_CLIPPING, traceClip);

    _transparency = addIntegerProperty(TRANSPARENCY, transparency);

    _percentile = addIntegerProperty(PERCENTILE, percentile);

    _reversePolarity = addBooleanProperty(REVERSE_POLARITY, reversePolarity);

    _wiggleTrace = addBooleanProperty(WIGGLE_TRACE, wiggleTrace);

    _positiveColorFill = addBooleanProperty(POSITIVE_COLOR_FILL, posColorFill);

    _negativeColorFill = addBooleanProperty(NEGATIVE_COLOR_FILL, negColorFill);

    _positiveDensityFill = addBooleanProperty(POSITIVE_DENSITY_FILL, posDensityFill);

    _negativeDensityFill = addBooleanProperty(NEGATIVE_DENSITY_FILL, negDensityFill);

    _variableDensity = addBooleanProperty(VARIABLE_DENSITY, varDensity);

    _nullColor = addColorProperty(COLOR_NULL, colorNull);

    _wiggleTraceColor = addColorProperty(COLOR_WIGGLE, colorWiggle);

    _positiveFillColor = addColorProperty(COLOR_POSITIVE_FILL, colorPosFill);

    _negativeFillColor = addColorProperty(COLOR_NEGATIVE_FILL, colorNegFill);

    _agcApply = addBooleanProperty(AGC_APPLY, agcApply);

    _agcType = addEnumProperty(AGC_TYPE, AGC.Type.class, agcType);

    _agcWindowLength = addFloatProperty(AGC_WINDOW_LENGTH, agcWindow);

    _geometricGainApply = addBooleanProperty(GEOMETRIC_GAIN_APPLY, gainApply);

    _geometricGainT0 = addFloatProperty(GEOMETRIC_GAIN_T0, gainT0);

    _geometricGainTMax = addFloatProperty(GEOMETRIC_GAIN_TMAX, gainTmax);

    _geometricGainN = addFloatProperty(GEOMETRIC_GAIN_N, gainN);

    _recomputePercentile = true;

    _dataUnit = Unit.SEISMIC_AMPLITUDE;

    double startValue = -5000;
    double endValue = 5000;
    Labels labels = new Labels(startValue, endValue, 10);

    _overlayColorBar = addColorBarProperty(COLOR_BAR, new ColorBar(64, colorMap, startValue, endValue, labels
        .getIncrement()));
    _overlayColorBar.get().setReversedRange(true);
  }

  /**
   * The copy constructor.
   * @param model the trace renderer model to copy.
   */
  public SeismicDatasetRendererModel(final SeismicDatasetRendererModel model) {
    this();
    setDataUnit(model.getDataUnit());
    updateFrom(model);
  }

  public void updateFrom(final SeismicDatasetRendererModel model) {
    super.updateFrom(model);
    _overlayColorBar.set(new ColorBar(model.getColorBar()));
    _recomputePercentile = true;
  }

  public void validate(final IValidation results) {

    if (_traceExaggeration.get() < 0) {
      results.error(TRACE_EXAGGERATION, " trace exaggeration must be >= 0");
    }

    if (_traceClipping.get() < 1) {
      results.error(TRACE_CLIPPING, " trace exaggeration must be >= 1");
    }

    if (_transparency.get() < 0 || _transparency.get() > 100) {
      results.error(TRANSPARENCY, " transparency must be in the range 0 to 100");
    }

    if (_percentile.get() < 0 || _percentile.get() > 100) {
      results.error(PERCENTILE, " percentile must be in the range 0 to 100");
    }

    if (_agcWindowLength.get() < 0) {
      results.error(AGC_WINDOW_LENGTH, "AGC window length cannot be negative");
    }
  }

  public Unit getDataUnit() {
    return _dataUnit;
  }

  public void setDataUnit(final Unit dataUnit) {
    _dataUnit = dataUnit;

    // Initialize the default renderer settings from the preferences.
    final IPreferenceStore preferences = SeismicDatasetRendererPreferencePage.PREFERENCE_STORE;

    String colorMapName = preferences.getString(OTHER_COLOR_MAP);

    Domain dataDomain = dataUnit.getDomain();
    int numColors = 64;
    double startValue = -100;
    double endValue = 100;
    int percentile = 0;
    if (dataUnit.equals(Unit.SEISMIC_AMPLITUDE)) {
      _normalizationMethod.set(NormalizationMethod.BY_MAXIMUM);
      percentile = 1;
      colorMapName = preferences.getString(SEISMIC_COLOR_MAP);
      startValue = -5000;
      endValue = 5000;
    } else if (dataDomain.equals(Domain.VELOCITY)) {
      _normalizationMethod.set(NormalizationMethod.BY_LIMITS);
      colorMapName = preferences.getString(VELOCITY_COLOR_MAP);
      if (dataUnit.equals(Unit.FEET_PER_SECOND)) {
        startValue = 5000;
        endValue = 15000;
      } else if (dataUnit.equals(Unit.METERS_PER_SECOND)) {
        startValue = 1500;
        endValue = 5000;
      }
    } else {
      startValue = -5000;
      endValue = 5000;
    }

    // Lookup the color map based on its name.
    IColorMap colorMap = null;
    for (final ColorMapDescription colorMapDesc : ServiceProvider.getColorMapService().getAll()) {
      if (colorMapName.equals(colorMapDesc.getName())) {
        colorMap = colorMapDesc.createMap();
      }
    }
    // Default to grayscale if no match found.
    if (colorMap == null) {
      colorMap = new GrayscaleColorMap();
    }

    Labels labels = new Labels(startValue, endValue, 10);
    _overlayColorBar.set(new ColorBar(numColors, colorMap, startValue, endValue, labels.getIncrement()));
    _overlayColorBar.get().setReversedRange(true);
    setValueObject(PERCENTILE, percentile);
  }

  public ColorBar getColorBar() {
    return _overlayColorBar.get();
  }

  public void setColorBar(final ColorBar colorBar) {
    _overlayColorBar.set(colorBar);
  }

  public ColorBar getColorBar(final NormalizationMethod normalization, final float[] values, final float nullValue,
      final float minValue, final float maxValue) {
    if (_recomputePercentile && _percentile.get() > 0 && normalization.equals(NormalizationMethod.BY_MAXIMUM)) {
      float[] clipped = MathUtil.computePercentiles(values, nullValue, _percentile.get());
      if (MathUtil.isEqual(clipped[0], 0.0f) && MathUtil.isEqual(clipped[1], 0.0f)) {
        // Recompute the clipped bounds as if the percentile is zero.
        clipped = MathUtil.computePercentiles(values, nullValue, 0.0f);
      }
      float absmax = Math.max(Math.abs(clipped[0]), Math.abs(clipped[1]));
      clipped[0] = -absmax;
      clipped[1] = absmax;
      _overlayColorBar.get().setStartValue(clipped[0]);
      _overlayColorBar.get().setEndValue(clipped[1]);
      _recomputePercentile = false;
    } else if (_recomputePercentile && _percentile.get() == 0) {
      _overlayColorBar.get().setStartValue(minValue);
      _overlayColorBar.get().setEndValue(maxValue);
      _recomputePercentile = false;
    }
    return _overlayColorBar.get();
  }

  public int getPercentile() {
    return _percentile.get();
  }

  public void setPercentile(final int percentile) {
    _percentile.set(percentile);
  }

  protected static boolean hasRenderMethod(final int renderMethod, final int renderMethodToCheck) {
    return (renderMethod & renderMethodToCheck) == renderMethodToCheck;
  }

  public int getRenderMethod() {
    int renderMethod = 0;
    if (getWiggleTrace()) {
      renderMethod = renderMethod | ITraceRenderStyle.WIGGLE_TRACE;
    }
    if (getPositiveColorFill()) {
      renderMethod = renderMethod | ITraceRenderStyle.POSITIVE_COLOR_FILL;
    }
    if (getNegativeColorFill()) {
      renderMethod = renderMethod | ITraceRenderStyle.NEGATIVE_COLOR_FILL;
    }
    if (getPositiveDensityFill()) {
      renderMethod = renderMethod | ITraceRenderStyle.POSITIVE_DENSITY_FILL;
    }
    if (getNegativeDensityFill()) {
      renderMethod = renderMethod | ITraceRenderStyle.NEGATIVE_DENSITY_FILL;
    }
    if (getVariableDensity()) {
      renderMethod = renderMethod | ITraceRenderStyle.VARIABLE_DENSITY;
    }
    return renderMethod;
  }

  public InterpolationMethod getInterpolationMethod() {
    return _interpolationMethod.get();
  }

  public void setInterpolationMethod(final InterpolationMethod interpolationMethod) {
    _interpolationMethod.set(interpolationMethod);
  }

  public NormalizationMethod getNormalizationMethod() {
    return _normalizationMethod.get();
  }

  public void setNormalizationMethod(final NormalizationMethod normalizationMethod) {
    _normalizationMethod.set(normalizationMethod);
  }

  public float getTraceExaggeration() {
    return _traceExaggeration.get();
  }

  public void setTraceExaggeration(final int traceExaggeration) {
    _traceExaggeration.set(traceExaggeration);
  }

  public int getTraceClipping() {
    return _traceClipping.get();
  }

  public void setTraceClipping(final int traceClipping) {
    _traceClipping.set(traceClipping);
  }

  public int getTransparency() {
    return _transparency.get();
  }

  public void setTransparency(final int transparency) {
    _transparency.set(transparency);
  }

  public int getOpacityMin() {
    return 0;
  }

  public int getOpacityMax() {
    return 100;
  }

  public boolean getWiggleTrace() {
    return _wiggleTrace.get();
  }

  public void setWiggleTrace(final boolean wiggleTrace) {
    _wiggleTrace.set(wiggleTrace);
  }

  public boolean getPositiveColorFill() {
    return _positiveColorFill.get();
  }

  public void setPositiveColorFill(final boolean positiveColorFill) {
    _positiveColorFill.set(positiveColorFill);
  }

  public boolean getNegativeColorFill() {
    return _negativeColorFill.get();
  }

  public void setNegativeColorFill(final boolean negativeColorFill) {
    _negativeColorFill.set(negativeColorFill);
  }

  public boolean getPositiveDensityFill() {
    return _positiveDensityFill.get();
  }

  public void setPositiveDensityFill(final boolean positiveDensityFill) {
    _positiveDensityFill.set(positiveDensityFill);
  }

  public boolean getNegativeDensityFill() {
    return _negativeDensityFill.get();
  }

  public void setNegativeDensityFill(final boolean negativeDensityFill) {
    _negativeDensityFill.set(negativeDensityFill);
  }

  public boolean getVariableDensity() {
    return _variableDensity.get();
  }

  public void setVariableDensity(final boolean variableDensity) {
    _variableDensity.set(variableDensity);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    String triggerKey = event.getPropertyName();
    if (triggerKey == null || triggerKey.length() == 0) {
      return;
    }
    if (triggerKey.equals(PERCENTILE)) {
      _recomputePercentile = true;
    }
    if (triggerKey.equals(POSITIVE_COLOR_FILL)) {
      if (_positiveColorFill.get()) {
        _positiveDensityFill.set(false);
        _variableDensity.set(false);
      }
    } else if (triggerKey.equals(NEGATIVE_COLOR_FILL)) {
      if (_negativeColorFill.get()) {
        _negativeDensityFill.set(false);
        _variableDensity.set(false);
      }
    } else if (triggerKey.equals(POSITIVE_DENSITY_FILL)) {
      if (_positiveDensityFill.get()) {
        _positiveColorFill.set(false);
        _variableDensity.set(false);
      }
    } else if (triggerKey.equals(NEGATIVE_DENSITY_FILL)) {
      if (_negativeDensityFill.get()) {
        _negativeColorFill.set(false);
        _variableDensity.set(false);
      }
    } else if (triggerKey.equals(VARIABLE_DENSITY)) {
      if (_variableDensity.get()) {
        _positiveColorFill.set(false);
        _negativeColorFill.set(false);
        _positiveDensityFill.set(false);
        _negativeDensityFill.set(false);
      }
    }
  }

  public RGB getColorNull() {
    return _nullColor.get();
  }

  public void setColorNull(final RGB nullColor) {
    _nullColor.set(nullColor);
  }

  public RGB getColorWiggle() {
    return _wiggleTraceColor.get();
  }

  public void setColorWiggle(final RGB wiggleTraceColor) {
    _wiggleTraceColor.set(wiggleTraceColor);
  }

  public RGB getColorPositiveFill() {
    return _positiveFillColor.get();
  }

  public void setColorPositiveFill(final RGB positiveFillColor) {
    _positiveFillColor.set(positiveFillColor);
  }

  public RGB getColorNegativeFill() {
    return _negativeFillColor.get();
  }

  public void setColorNegativeFill(final RGB negativeFillColor) {
    _negativeFillColor.set(negativeFillColor);
  }

  public boolean getReversePolarity() {
    return _reversePolarity.get();
  }

  public void setReversePolarity(final boolean reversePolarity) {
    _reversePolarity.set(reversePolarity);
  }

  public SeismicDataset getOverlayDataset() {
    return _overlayDataset.get();
  }

  public boolean getAgcApply() {
    return _agcApply.get();
  }

  public void setAgcApply(final boolean agcApply) {
    _agcApply.set(agcApply);
  }

  public AGC.Type getAgcType() {
    return _agcType.get();
  }

  public void setAgcType(final AGC.Type agcType) {
    _agcType.set(agcType);
  }

  public float getAgcWindowLength() {
    return _agcWindowLength.get();
  }

  public void setAgcWindowLength(final float agcWindowLength) {
    _agcWindowLength.set(agcWindowLength);
  }

  public boolean getGeometricGainApply() {
    return _geometricGainApply.get();
  }

  public void setGeometricGainApply(final boolean geometricGainApply) {
    _geometricGainApply.set(geometricGainApply);
  }

  public float getGeometricGainT0() {
    return _geometricGainT0.get();
  }

  public void setGeometricGainTO(final float geometricGainT0) {
    _geometricGainT0.set(geometricGainT0);
  }

  public float getGeometricGainN() {
    return _geometricGainN.get();
  }

  public void setGeometricGainTN(final float geometricGainTN) {
    _geometricGainT0.set(geometricGainTN);
  }

  public float getGeometricGainTMax() {
    return _geometricGainTMax.get();
  }

  public void setGeometricGainTMax(final float tmax) {
    _geometricGainTMax.set(tmax);
  }

  public void setRecomputePercentile() {
    _recomputePercentile = true;
  }
}
