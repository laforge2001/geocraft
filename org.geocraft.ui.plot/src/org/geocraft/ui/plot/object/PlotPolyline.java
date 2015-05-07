/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.ShapeType;


/**
 * The basic implementation of a plot polyline.
 */
public class PlotPolyline extends PlotPointGroup implements IPlotPolyline {

  /** The plot line properties. */
  protected LineProperties _lineProperties;

  /**
   * Constructs a plot polyline.
   */
  public PlotPolyline() {
    this("", new TextProperties(), new PointProperties(), new LineProperties());
  }

  /**
   * Constructs a plot polyline, copied from another plot polyline.
   * @param polyline the plot polyline to copy.
   */
  public PlotPolyline(final IPlotPolyline polyline) {
    this(polyline.getName(), polyline.getTextProperties(), polyline.getPointProperties(), polyline.getLineProperties());
  }

  /**
   * Constructs a plot polyline.
   * @param name the polyline name.
   * @param textProps the text properties.
   * @param pointProps the point properties.
   * @param lineProps the line properties.
   */
  public PlotPolyline(final String name, final TextProperties textProps, final PointProperties pointProps, final LineProperties lineProps) {
    super(name, textProps, pointProps);
    setShapeType(ShapeType.POLYLINE);
    _lineProperties = new LineProperties(lineProps);
    //_lineProperties.addPropertyChangeListener(this);
  }

  public LineStyle getLineStyle() {
    return _lineProperties.getStyle();
  }

  public int getLineWidth() {
    return _lineProperties.getWidth();
  }

  public RGB getLineColor() {
    return _lineProperties.getColor();
  }

  public LineProperties getLineProperties() {
    return _lineProperties;
  }

  public void setLineProperties(final LineProperties lineProps) {
    _lineProperties = lineProps;
    updated();
  }

  public void setLineStyle(final LineStyle style) {
    _lineProperties.setStyle(style);
    updated();
  }

  public void setLineWidth(final int width) {
    _lineProperties.setWidth(width);
    updated();
  }

  public void setLineColor(final RGB color) {
    _lineProperties.setColor(color);
    updated();
  }

  @Override
  public void dispose() {
    super.dispose();
    _lineProperties.dispose();
  }

  //  @Override
  //  protected IPlotEditor createEditor() {
  //    return new PlotPolylineEditor(this);
  //  }
}
