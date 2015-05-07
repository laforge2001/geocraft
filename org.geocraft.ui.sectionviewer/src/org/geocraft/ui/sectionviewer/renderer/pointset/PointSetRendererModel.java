/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.pointset;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.IColorMap;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.ColorBarProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


/**
 * Defines the model of display parameters to use when rendering
 * a <code>PointSet</code> entity in the section viewer.
 */
public class PointSetRendererModel extends Model implements IPointSetRendererConstants {

  private final EnumProperty<PointStyle> _pointStyle;

  private final IntegerProperty _pointSize;

  private final ColorProperty _pointColor;

  private final StringProperty _colorAttribute;

  private final EnumProperty<LineStyle> _lineStyle;

  private final IntegerProperty _lineWidth;

  private final ColorProperty _lineColor;

  private final StringProperty _connectionAttribute;

  private final StringProperty _thresholdAttribute;

  private final FloatProperty _thresholdMinValue;

  private final FloatProperty _thresholdMaxValue;

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
    String lineStyleStr = preferences.getString(LINE_STYLE);
    LineStyle lineStyle = LineStyle.lookup(lineStyleStr);
    int lineWidth = preferences.getInt(LINE_WIDTH);
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
    _pointColor = addColorProperty(POINT_COLOR, new RGB(255, 255, 0));
    _colorAttribute = addStringProperty(COLOR_ATTRIBUTE, NO_ATTRIBUTE);
    _lineStyle = addEnumProperty(LINE_STYLE, LineStyle.class, lineStyle);
    _lineWidth = addIntegerProperty(LINE_WIDTH, lineWidth);
    _lineColor = addColorProperty(LINE_COLOR, new RGB(255, 255, 0));
    _connectionAttribute = addStringProperty(CONNECTION_ATTRIBUTE, NO_ATTRIBUTE);
    _thresholdAttribute = addStringProperty(THRESHOLD_ATTRIBUTE, NO_ATTRIBUTE);
    _thresholdMinValue = addFloatProperty(THRESHOLD_MIN_VALUE, 0);
    _thresholdMaxValue = addFloatProperty(THRESHOLD_MAX_VALUE, 1);
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
   * Returns <i>true</i> if points are to be colored based on an attribute value; <i>false</i> to use a single color for all points.
   */
  public boolean getColorByAttribute() {
    return !_colorAttribute.get().equals(NO_ATTRIBUTE);
  }

  /**
   * Returns the name of the attribute to use when coloring points by attribute.
   */
  public String getColorAttribute() {
    return _colorAttribute.get();
  }

  /**
   * Returns the line style.
   */
  public LineStyle getLineStyle() {
    return _lineStyle.get();
  }

  /**
   * Returns the line width (in pixels).
   */
  public int getLineWidth() {
    return _lineWidth.get();
  }

  /**
   * Returns the line color.
   */
  public RGB getLineColor() {
    return _lineColor.get();
  }

  /**
   * Returns <i>true</i> if points are to be connected based on an attribute value; <i>false</i> not to connect.
   */
  public boolean getConnectionByAttribute() {
    return !_connectionAttribute.get().equals(NO_ATTRIBUTE);
  }

  /**
   * Returns the name of the attribute to use when connecting points by attribute.
   */
  public String getConnectionAttribute() {
    return _connectionAttribute.get();
  }

  /**
   * Returns <i>true</i> if points are to be thresholded based on an attribute value; <i>false</i> not to connect.
   */
  public boolean getThresholdByAttribute() {
    return !_thresholdAttribute.get().equals(NO_ATTRIBUTE);
  }

  /**
   * Returns the name of the attribute to use when thresholding points by attribute.
   */
  public String getThresholdAttribute() {
    return _thresholdAttribute.get();
  }

  /**
   * Returns the threshold minimum value to use when thresholding points by attribute.
   */
  public float getThresholdMinValue() {
    return _thresholdMinValue.get();
  }

  /**
   * Returns the threshold maximum value to use when thresholding points by attribute.
   */
  public float getThresholdMaxValue() {
    return _thresholdMaxValue.get();
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
