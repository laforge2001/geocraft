/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FontProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


public class SeismicSurvey3dRendererModel extends Model implements ISeismicSurvey3dRendererConstants {

  private TextProperties _textProperties;

  private LineProperties _lineProperties;

  private PointProperties _pointProperties;

  private FontProperty _textFont;

  private ColorProperty _textColor;

  private EnumProperty<LineStyle> _lineStyle;

  private IntegerProperty _lineWidth;

  private ColorProperty _lineColor;

  private EnumProperty<PointStyle> _pointStyle;

  private IntegerProperty _pointSize;

  private ColorProperty _pointColor;

  public SeismicSurvey3dRendererModel() {
    super();
    _textProperties = new TextProperties();
    _textProperties.setColor(new RGB(255, 255, 0));
    _lineProperties = new LineProperties();
    _lineProperties.setColor(new RGB(255, 255, 255));
    _pointProperties = new PointProperties();
    _pointProperties.setStyle(PointStyle.NONE);
    _pointProperties.setColor(new RGB(255, 255, 255));

    _textFont = addFontProperty(TEXT_FONT, _textProperties.getFont().getFontData());
    _textColor = addColorProperty(TEXT_COLOR, _textProperties.getColor());
    _lineStyle = addEnumProperty(LINE_STYLE, LineStyle.class, _lineProperties.getStyle());
    _lineWidth = addIntegerProperty(LINE_WIDTH, _lineProperties.getWidth());
    _lineColor = addColorProperty(LINE_COLOR, _lineProperties.getColor());
    _pointStyle = addEnumProperty(POINT_STYLE, PointStyle.class, _pointProperties.getStyle());
    _pointSize = addIntegerProperty(POINT_SIZE, _pointProperties.getSize());
    _pointColor = addColorProperty(POINT_COLOR, _pointProperties.getColor());
  }

  public SeismicSurvey3dRendererModel(final SeismicSurvey3dRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(final IValidation results) {
    if (_lineWidth.get() < 0) {
      results.error(LINE_WIDTH, "Line width must be >= 0");
    }

    if (_pointSize.get() < 0) {
      results.error(POINT_SIZE, "Point size must be >= 0");
    }
  }

  public TextProperties getTextProperties() {
    _textProperties.setFont(new Font(null, _textFont.get()));
    _textProperties.setColor(_textColor.get());
    return _textProperties;
  }

  public LineProperties getLineProperties() {
    _lineProperties.setStyle(_lineStyle.get());
    _lineProperties.setWidth(_lineWidth.get());
    _lineProperties.setColor(_lineColor.get());
    return _lineProperties;
  }

  public PointProperties getPointProperties() {
    _pointProperties.setStyle(_pointStyle.get());
    _pointProperties.setSize(_pointSize.get());
    _pointProperties.setColor(_pointColor.get());
    return _pointProperties;
  }

}
