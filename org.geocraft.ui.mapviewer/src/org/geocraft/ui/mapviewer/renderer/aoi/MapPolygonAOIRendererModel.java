/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.aoi;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.attribute.FillProperties;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


public class MapPolygonAOIRendererModel extends Model implements IAOIRendererConstants {

  private FillProperties _inclusiveFillProperties;

  private LineProperties _inclusiveLineProperties;

  private FillProperties _exclusiveFillProperties;

  private LineProperties _exclusiveLineProperties;

  private PointProperties _pointProperties;

  private EnumProperty<LineStyle> _inclusiveLineStyle;

  private IntegerProperty _inclusiveLineWidth;

  private ColorProperty _inclusiveLineColor;

  private EnumProperty<FillStyle> _inclusiveFillStyle;

  private ColorProperty _inclusiveFillColor;

  private EnumProperty<LineStyle> _exclusiveLineStyle;

  private IntegerProperty _exclusiveLineWidth;

  private ColorProperty _exclusiveLineColor;

  private EnumProperty<FillStyle> _exclusiveFillStyle;

  private ColorProperty _exclusiveFillColor;

  private EnumProperty<PointStyle> _pointStyle;

  private IntegerProperty _pointSize;

  private ColorProperty _pointColor;

  private IntegerProperty _transparency;

  public MapPolygonAOIRendererModel() {
    super();
    _inclusiveFillProperties = new FillProperties();
    _inclusiveFillProperties.setStyle(FillStyle.SOLID);
    _inclusiveFillProperties.setRGB(new RGB(0, 255, 0));
    _inclusiveLineProperties = new LineProperties();
    _inclusiveLineProperties.setStyle(LineStyle.DASHED);
    _inclusiveLineProperties.setWidth(1);
    _inclusiveLineProperties.setColor(new RGB(0, 255, 0));

    _exclusiveFillProperties = new FillProperties();
    _exclusiveFillProperties.setStyle(FillStyle.SOLID);
    _exclusiveFillProperties.setRGB(new RGB(255, 0, 0));
    _exclusiveLineProperties = new LineProperties();
    _exclusiveLineProperties.setStyle(LineStyle.DASHED);
    _exclusiveLineProperties.setWidth(1);
    _exclusiveLineProperties.setColor(new RGB(255, 0, 0));

    _pointProperties = new PointProperties();
    _pointProperties.setStyle(PointStyle.NONE);
    _pointProperties.setColor(new RGB(0, 0, 255));

    _inclusiveLineStyle = addEnumProperty(INCLUSIVE_LINE_STYLE, LineStyle.class, _inclusiveLineProperties.getStyle());
    _inclusiveLineWidth = addIntegerProperty(INCLUSIVE_LINE_WIDTH, _inclusiveLineProperties.getWidth());
    _inclusiveLineColor = addColorProperty(INCLUSIVE_LINE_COLOR, _inclusiveLineProperties.getColor());

    _inclusiveFillStyle = addEnumProperty(INCLUSIVE_FILL_STYLE, FillStyle.class, _inclusiveFillProperties.getStyle());
    _inclusiveFillColor = addColorProperty(INCLUSIVE_FILL_COLOR, _inclusiveFillProperties.getRGB());

    _exclusiveLineStyle = addEnumProperty(EXCLUSIVE_LINE_STYLE, LineStyle.class, _exclusiveLineProperties.getStyle());
    _exclusiveLineWidth = addIntegerProperty(EXCLUSIVE_LINE_WIDTH, _exclusiveLineProperties.getWidth());
    _exclusiveLineColor = addColorProperty(EXCLUSIVE_LINE_COLOR, _exclusiveLineProperties.getColor());

    _exclusiveFillStyle = addEnumProperty(EXCLUSIVE_FILL_STYLE, FillStyle.class, _exclusiveFillProperties.getStyle());
    _exclusiveFillColor = addColorProperty(EXCLUSIVE_FILL_COLOR, _exclusiveFillProperties.getRGB());

    _pointStyle = addEnumProperty(POINT_STYLE, PointStyle.class, _pointProperties.getStyle());
    _pointSize = addIntegerProperty(POINT_SIZE, _pointProperties.getSize());
    _pointColor = addColorProperty(POINT_COLOR, _pointProperties.getColor());

    _transparency = addIntegerProperty(TRANSPARENCY, 50);
  }

  public MapPolygonAOIRendererModel(final MapPolygonAOIRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(final IValidation results) {
    if (_transparency.get() < 0 || _transparency.get() > 100) {
      results.error(TRANSPARENCY, "Transparency must be in the range 0 to 100");
    }

    if (_inclusiveLineProperties.getWidth() < 0) {
      results.error(INCLUSIVE_LINE_WIDTH, "Line width must be >= 0");
    }

    if (_exclusiveLineProperties.getWidth() < 0) {
      results.error(EXCLUSIVE_LINE_WIDTH, "Line width must be >= 0");
    }

    if (_pointProperties.getSize() < 0) {
      results.error(POINT_SIZE, "Point size must be >= 0");
    }
  }

  public int getTransparency() {
    return _transparency.get();
  }

  public FillProperties getInclusiveFillProperties() {
    _inclusiveFillProperties.setStyle(_inclusiveFillStyle.get());
    _inclusiveFillProperties.setRGB(_inclusiveFillColor.get());
    return _inclusiveFillProperties;
  }

  public LineProperties getInclusiveLineProperties() {
    _inclusiveLineProperties.setStyle(_inclusiveLineStyle.get());
    _inclusiveLineProperties.setWidth(_inclusiveLineWidth.get());
    _inclusiveLineProperties.setColor(_inclusiveLineColor.get());
    return _inclusiveLineProperties;
  }

  public FillProperties getExclusiveFillProperties() {
    _exclusiveFillProperties.setStyle(_exclusiveFillStyle.get());
    _exclusiveFillProperties.setRGB(_exclusiveFillColor.get());
    return _exclusiveFillProperties;
  }

  public LineProperties getExclusiveLineProperties() {
    _exclusiveLineProperties.setStyle(_exclusiveLineStyle.get());
    _exclusiveLineProperties.setWidth(_exclusiveLineWidth.get());
    _exclusiveLineProperties.setColor(_exclusiveLineColor.get());
    return _exclusiveLineProperties;
  }

  public PointProperties getPointProperties() {
    _pointProperties.setStyle(_pointStyle.get());
    _pointProperties.setSize(_pointSize.get());
    _pointProperties.setColor(_pointColor.get());
    return _pointProperties;
  }

}
