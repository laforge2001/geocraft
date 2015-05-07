/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.pointset;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.IColorMap;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorBarProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.defs.PointStyle;


/**
 * Defines the model of display parameters to use when rendering
 * a <code>PointSet</code> entity in the section viewer.
 */
public class PointSetRendererModel extends Model implements IPointSetRendererConstants {

  private final EnumProperty<PointStyle> _pointStyle;

  private final IntegerProperty _pointSize;

  private final BooleanProperty _sizeByAttribute;

  private final StringProperty _sizeAttribute;

  private final FloatProperty _sizeAttributeMin;

  private final FloatProperty _sizeAttributeMax;

  private final IntegerProperty _pointSizeMin;

  private final IntegerProperty _pointSizeMax;

  private final ColorProperty _pointColor;

  private final BooleanProperty _colorByAttribute;

  private final StringProperty _colorAttribute;

  private final ColorBarProperty _colorBar;

  /**
   * The default constructor.
   */
  public PointSetRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    IPreferenceStore preferences = PointSetRendererPreferencePage.PREFERENCE_STORE;

    String pointStyleStr = preferences.getString(POINT_STYLE);
    PointStyle pointStyle = PointStyle.lookup(pointStyleStr);
    int pointSize = preferences.getInt(POINT_SIZE);
    boolean sizeByAttr = preferences.getBoolean(SIZE_BY_ATTRIBUTE);
    final float sizeAttrMin = preferences.getFloat(SIZE_ATTRIBUTE_MIN);
    final float sizeAttrMax = preferences.getFloat(SIZE_ATTRIBUTE_MAX);
    final int pointSizeMin = preferences.getInt(POINT_SIZE_MIN);
    final int pointSizeMax = preferences.getInt(POINT_SIZE_MAX);
    RGB pointColor = PreferenceConverter.getColor(preferences, POINT_COLOR);
    boolean colorByAttr = preferences.getBoolean(COLOR_BY_ATTRIBUTE);
    String colorMapName = preferences.getString(COLOR_MAP);
    IColorMap colorMap = null;
    for (ColorMapDescription colorMapDesc : ServiceProvider.getColorMapService().getAll()) {
      if (colorMapName.equals(colorMapDesc.getName())) {
        colorMap = colorMapDesc.createMap();
      }
    }
    if (colorMap == null) {
      colorMap = new SpectrumColorMap();
    }
    _pointStyle = addEnumProperty(POINT_STYLE, PointStyle.class, pointStyle);
    _pointSize = addIntegerProperty(POINT_SIZE, pointSize);
    _sizeByAttribute = addBooleanProperty(SIZE_BY_ATTRIBUTE, sizeByAttr);
    _sizeAttribute = addStringProperty(SIZE_ATTRIBUTE, Z_ATTRIBUTE);
    _sizeAttributeMin = addFloatProperty(SIZE_ATTRIBUTE_MIN, sizeAttrMin);
    _sizeAttributeMax = addFloatProperty(SIZE_ATTRIBUTE_MAX, sizeAttrMax);
    _pointSizeMin = addIntegerProperty(POINT_SIZE_MIN, pointSizeMin);
    _pointSizeMax = addIntegerProperty(POINT_SIZE_MAX, pointSizeMax);
    _pointColor = addColorProperty(POINT_COLOR, pointColor);
    _colorByAttribute = addBooleanProperty(COLOR_BY_ATTRIBUTE, colorByAttr);
    _colorAttribute = addStringProperty(COLOR_ATTRIBUTE, Z_ATTRIBUTE);
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
    if (_pointStyle.isNull()) {
      results.error(POINT_STYLE, "Point style not specified.");
    }
    if (_pointSize.get() < 0) {
      results.error(POINT_SIZE, "Point size must be >= 0.");
    }
    if (_pointColor.isNull()) {
      results.error(POINT_COLOR, "Point color not specified.");
    }

    if (_colorByAttribute.get()) {
      if (_colorAttribute.isEmpty()) {
        results.error(COLOR_ATTRIBUTE, "Color attribute not specified.");
      }
    }

    if (_sizeByAttribute.get()) {
      if (_sizeAttribute.isEmpty()) {
        results.error(SIZE_ATTRIBUTE, "Size attribute not specified.");
      }
    }
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
   * Returns <i>true</i> if points are to be sized based on an attribute value; <i>false</i> to use a single size for all points.
   */
  public boolean getSizeByAttribute() {
    return _sizeByAttribute.get();
  }

  /**
   * Returns the name of the attribute to use when sizing points by attribute.
   */
  public String getSizeAttribute() {
    return _sizeAttribute.get();
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
   * Returns the point color.
   */
  public RGB getPointColor() {
    return _pointColor.get();
  }

  /**
   * Returns <i>true</i> if points are to be colored based on an attribute value; <i>false</i> to use a single color for all points.
   */
  public boolean getColorByAttribute() {
    return _colorByAttribute.get();
  }

  /**
   * Returns the name of the attribute to use when coloring points by attribute.
   */
  public String getColorAttribute() {
    return _colorAttribute.get();
  }

  public ColorBar getColorBar() {
    return _colorBar.get();
  }

  public static void updateColorBarRangeBasedOnAttribute(final PointSet pointSet, final ColorBar colorBar,
      final String attribute) {
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    if (attribute.equals(Z_ATTRIBUTE)) {
      for (int i = 0; i < pointSet.getNumPoints(); i++) {
        float value = (float) pointSet.getZ(i);
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    } else {
      for (int i = 0; i < pointSet.getNumPoints(); i++) {
        float value = pointSet.getAttribute(attribute).getFloat(i);
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    }
    colorBar.setStartValue(min);
    colorBar.setEndValue(max);
    Labels labels = new Labels(min, max, 10);
    colorBar.setStepValue(labels.getIncrement());
  }

  public PointProperties getPointProperties() {
    return new PointProperties(getPointStyle(), getPointColor(), getPointSize());
  }
}
