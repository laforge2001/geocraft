/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.scatter.well;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorUtil;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.model.well.WellDomain;
import org.geocraft.ui.chartviewer.renderer.scatter.ScatterDataRendererModel;
import org.geocraft.ui.plot.defs.PointStyle;


/**
 * The model of rendering properties for a crossplot of <code>WellLogTrace</code> entities
 * in the scatter plot view.
 */
public class WellLogTracesRendererModel extends ScatterDataRendererModel {

  /** The key for the point color property. */
  public static final String POINT_COLOR = "Point Color";

  /** The key for the point style property. */
  public static final String POINT_STYLE = "Point Style";

  /** The key for the point size property. */
  public static final String POINT_SIZE = "Point Size";

  /** The key for the domain of the z bounds property. */
  public static final String Z_DOMAIN = "Z Domain";

  /** The key for the starting z property. */
  public static final String Z_START = "Start Z";

  /** The key for the ending z property. */
  public static final String Z_END = "End Z";

  /** The property for the color of the scatter points. */
  private ColorProperty _pointColor;

  /** The property for the style of the scatter points. */
  private EnumProperty<PointStyle> _pointStyle;

  /** The property for the size of the scatter points. */
  private IntegerProperty _pointSize;

  /** The property for the domain of the z bounds. */
  private EnumProperty<WellDomain> _zDomain;

  /** The property for the starting z value. */
  private FloatProperty _zStart;

  /** The property for the ending z value. */
  private FloatProperty _zEnd;

  public WellLogTracesRendererModel() {
    super();
    _pointColor = addColorProperty(POINT_COLOR, ColorUtil.getCommonRGB());
    _pointStyle = addEnumProperty(POINT_STYLE, PointStyle.class, PointStyle.CROSS);
    _pointSize = addIntegerProperty(POINT_SIZE, 3);
    _zDomain = addEnumProperty(Z_DOMAIN, WellDomain.class, WellDomain.MEASURED_DEPTH);
    _zStart = addFloatProperty(Z_START, 0);
    _zEnd = addFloatProperty(Z_END, 0);
  }

  public WellLogTracesRendererModel(WellLogTracesRendererModel model) {
    this();
    updateFrom(model);
  }

  @Override
  public void validate(IValidation results) {
    if (_pointColor.isNull()) {
      results.error(_pointColor, "No point color specified.");
    }
    if (_pointStyle.isNull()) {
      results.error(_pointStyle, "No point style specified.");
    }
    if (_pointSize.get() < 0) {
      results.error(_pointColor, "Point size must be positive.");
    }
    if (_zDomain.isNull()) {
      results.error(_zDomain, "No z domain specified.");
    }
    if (_zEnd.get() <= _zStart.get()) {
      results.error(_zStart, "The ending z value must be greater than the starting z value.");
    }
  }

  /**
   * Returns the color to use for rendering the scatter points.
   */
  public RGB getPointColor() {
    return _pointColor.get();
  }

  /**
   * Returns the style (e.g. circle, square, cross, etc) to use for rendering the scatter points.
   */
  public PointStyle getPointStyle() {
    return _pointStyle.get();
  }

  /**
   * Returns the size to use for rendering the scatter points.
   */
  public int getPointSize() {
    return _pointSize.get();
  }

  /**
   * Returns the domain of the z bounds to use.
   */
  public WellDomain getZDomain() {
    return _zDomain.get();
  }

  /**
   * Returns the starting z value to use.
   */
  public float getZStart() {
    return _zStart.get();
  }

  /**
   * Returns the ending z value to use.
   */
  public float getZEnd() {
    return _zEnd.get();
  }
}