/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.pointset;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.IColorMap;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorBarProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;


/**
 * Defines the model of display parameters to use when rendering
 * a <code>PointSet</code> entity in the 3D viewer.
 */
public class PointSetRendererModel extends Model implements IPointSetRendererConstants {

  private final IntegerProperty _decimation;

  private final EnumProperty<PointStyle> _pointStyle;

  private final IntegerProperty _pointSize;

  private final BooleanProperty _pointSizeByAttribute;

  private final StringProperty _pointSizeAttribute;

  private final FloatProperty _sizeAttributeMin;

  private final FloatProperty _sizeAttributeMax;

  private final IntegerProperty _pointSizeMin;

  private final IntegerProperty _pointSizeMax;

  private final ColorProperty _pointColor;

  private final BooleanProperty _pointColorByAttribute;

  private final StringProperty _pointColorAttribute;

  private final BooleanProperty _thresholdByAttribute;

  private final StringProperty _thresholdAttribute;

  private final DoubleProperty _thresholdMinValue;

  private final DoubleProperty _thresholdMaxValue;

  private final ColorBarProperty _colorBar;

  /**
   * The default constructor.
   */
  public PointSetRendererModel() {
    super();
    // Initialize the default renderer settings from the preferences.
    final IPreferenceStore preferences = PointSetRendererPreferencePage.PREFERENCE_STORE;

    final int decimation = Math.max(1, preferences.getInt(DECIMATION));
    final String pointStyleStr = preferences.getString(POINT_STYLE);
    final PointStyle pointStyle = PointStyle.lookup(pointStyleStr);
    final int pointSize = preferences.getInt(POINT_SIZE);
    final boolean sizeByAttr = preferences.getBoolean(SIZE_BY_ATTRIBUTE);
    final float sizeAttrMin = preferences.getFloat(SIZE_ATTRIBUTE_MIN);
    final float sizeAttrMax = preferences.getFloat(SIZE_ATTRIBUTE_MAX);
    final int pointSizeMin = preferences.getInt(POINT_SIZE_MIN);
    final int pointSizeMax = preferences.getInt(POINT_SIZE_MAX);
    final RGB pointColor = PreferenceConverter.getColor(preferences, POINT_COLOR);
    final boolean colorByAttribute = preferences.getBoolean(COLOR_BY_ATTRIBUTE);
    final String colorMapName = preferences.getString(COLOR_MAP);
    IColorMap colorMap = null;
    for (final ColorMapDescription colorMapDesc : ServiceProvider.getColorMapService().getAll()) {
      if (colorMapName.equals(colorMapDesc.getName())) {
        colorMap = colorMapDesc.createMap();
      }
    }
    if (colorMap == null) {
      colorMap = new SpectrumColorMap();
    }
    final boolean thresholdByAttr = preferences.getBoolean(THRESHOLD_BY_ATTRIBUTE);

    _decimation = addIntegerProperty(DECIMATION, decimation);
    _pointStyle = addEnumProperty(POINT_STYLE, PointStyle.class, pointStyle);
    _pointSizeByAttribute = addBooleanProperty(SIZE_BY_ATTRIBUTE, sizeByAttr);
    _pointSizeAttribute = addStringProperty(SIZE_ATTRIBUTE, Z_ATTRIBUTE);
    _sizeAttributeMin = addFloatProperty(SIZE_ATTRIBUTE_MIN, sizeAttrMin);
    _sizeAttributeMax = addFloatProperty(SIZE_ATTRIBUTE_MAX, sizeAttrMax);
    _pointSizeMin = addIntegerProperty(POINT_SIZE_MIN, pointSizeMin);
    _pointSizeMax = addIntegerProperty(POINT_SIZE_MAX, pointSizeMax);
    _pointSize = addIntegerProperty(POINT_SIZE, pointSize);
    _pointColor = addColorProperty(POINT_COLOR, pointColor);
    _pointColorByAttribute = addBooleanProperty(COLOR_BY_ATTRIBUTE, colorByAttribute);
    _pointColorAttribute = addStringProperty(COLOR_ATTRIBUTE, Z_ATTRIBUTE);
    _thresholdByAttribute = addBooleanProperty(THRESHOLD_BY_ATTRIBUTE, thresholdByAttr);
    _thresholdAttribute = addStringProperty(THRESHOLD_ATTRIBUTE, Z_ATTRIBUTE);
    _thresholdMinValue = addDoubleProperty(THRESHOLD_ATTRIBUTE_MIN, 0);
    _thresholdMaxValue = addDoubleProperty(THRESHOLD_ATTRIBUTE_MAX, 1);
    _colorBar = addColorBarProperty(COLOR_BAR, new ColorBar(64, colorMap, 0, 100, 10));
  }

  /**
   * The copy constructor.
   * @param model the point set renderer model to copy.
   */
  public PointSetRendererModel(final PointSetRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(final IValidation results) {
    // Validate the decimation is non-negative.
    if (_decimation.get() < 1) {
      results.error(DECIMATION, "Decimation must be >= 0");
    }

    // Validate the point size is non-negative.
    if (_pointSize.get() < 0) {
      results.error(POINT_SIZE, "Point size must be >= 0");
    }

    // Validate the point color is non-null.
    if (_pointColor.isNull()) {
      results.error(POINT_COLOR, "Point color not specified");
    }

    // Validate the point style is non-null.
    if (_pointStyle.isNull()) {
      results.error(POINT_STYLE, "Point style not specified");
    }

    // Validate the color attribute is not empty.
    if (_pointColorByAttribute.get() && _pointColorAttribute.isEmpty()) {
      results.error(COLOR_ATTRIBUTE, "No color attribute specified");
    }

    if (_pointSizeByAttribute.get() && _pointSizeAttribute.isEmpty()) {
      results.error(SIZE_ATTRIBUTE, "No size attribute specified");
    }

    if (_thresholdByAttribute.get() && _thresholdAttribute.isEmpty()) {
      results.error(THRESHOLD_ATTRIBUTE, "No threshold attribute specified");
    }
  }

  /**
   * Gets the decimation factor.
   * <p>
   * 1=no decimation.
   * 
   * @return the decimation factor.
   */
  public int getDecimation() {
    return _decimation.get();
  }

  /**
   * Sets the decimation factor.
   * <p>
   * 1=no decimation.
   * 
   * @param decimation the decimation factor.
   */
  public void setDecimation(final int decimation) {
    _decimation.set(decimation);
  }

  /**
   * Returns the point style.
   */
  public PointStyle getPointStyle() {
    return _pointStyle.get();
  }

  /**
   * Returns the point size (in pixels).
   */
  public int getPointSize() {
    return _pointSize.get();
  }

  /**
   * Returns the point color.
   */
  public RGB getPointColor() {
    return _pointColor.get();
  }

  /**
   * Returns <i>true</i> if points are to be sized based on an attribute value; <i>false</i> to use a single size for all points.
   */
  public boolean getSizeByAttribute() {
    return _pointSizeByAttribute.get();
  }

  /**
   * Returns <i>true</i> if points are to be colored based on an attribute value; <i>false</i> to use a single color for all points.
   */
  public boolean getColorByAttribute() {
    return _pointColorByAttribute.get();
  }

  /**
   * Returns the name of the attribute to use when sizing points by attribute.
   */
  public String getSizeAttribute() {
    return _pointSizeAttribute.get();
  }

  /**
   * Returns the name of the attribute to use when coloring points by attribute.
   */
  public String getColorAttribute() {
    return _pointColorAttribute.get();
  }

  public float getSizeAttributeMin() {
    return _sizeAttributeMin.get();
  }

  public float getSizeAttributeMax() {
    return _sizeAttributeMax.get();
  }

  public void setSizeAttributeMin(final float sizeAttributeMin) {
    _sizeAttributeMin.set(sizeAttributeMin);
  }

  public void setSizeAttributeMax(final float sizeAttributeMax) {
    _sizeAttributeMax.set(sizeAttributeMax);
  }

  public int getPointSizeMin() {
    return _pointSizeMin.get();
  }

  public int getPointSizeMax() {
    return _pointSizeMax.get();
  }

  /**
   * Returns <i>true</i> if points are to be thresholded based on an attribute value; <i>false</i> not to connect.
   */
  public boolean getThresholdByAttribute() {
    return _thresholdByAttribute.get();
  }

  /**
   * Returns the name of the attribute to use when thresholding points by attribute.
   */
  public String getThresholdAttribute() {
    return _thresholdAttribute.get();
  }

  /**
   * Gets the minimum value to use when thresholding points by attribute.
   * 
   * @return the minimum threshold value.
   */
  public double getThresholdAttributeMin() {
    return _thresholdMinValue.get();
  }

  /**
   * Gets the maximum value to use when thresholding points by attribute.
   * 
   * @return the maximum threshold value.
   */
  public double getThresholdAttributeMax() {
    return _thresholdMaxValue.get();
  }

  /**
   * Sets the minimum value to use when thresholding points by attribute.
   * 
   * @param minValue the minimum threshold value.
   */
  public void setThresholdAttributeMin(final double minValue) {
    _thresholdMinValue.set(minValue);
  }

  /**
   * Sets the maximum value to use when thresholding points by attribute.
   * 
   * @param maxValue the maximum threshold value.
   */
  public void setThresholdAttributeMax(final double maxValue) {
    _thresholdMaxValue.set(maxValue);
  }

  public ColorBar getColorBar() {
    return _colorBar.get();
  }

  @Override
  public void updateFrom(final IModel model) {
    super.updateFrom(model);
    _colorBar.set(((PointSetRendererModel) model).getColorBar());
  }
}
