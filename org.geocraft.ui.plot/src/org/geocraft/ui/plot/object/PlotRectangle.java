/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import org.geocraft.ui.plot.attribute.FillProperties;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.ShapeType;


/**
 * The basic implementation of a plot rectangle.
 * TODO: Finish this class!
 */
public class PlotRectangle extends PlotPolygon implements IPlotRectangle {

  /**
   * Constructs a plot rectangle.
   */
  public PlotRectangle() {
    this("", new TextProperties(), new PointProperties(), new LineProperties(), new FillProperties());
  }

  /**
   * Constructs a plot rectangle, copied from another plot rectangle.
   * @param rectangle the plot rectangle to copy.
   */
  public PlotRectangle(final IPlotRectangle rectangle) {
    this(rectangle.getName(), rectangle.getTextProperties(), rectangle.getPointProperties(), rectangle.getLineProperties(), rectangle.getFillProperties());
  }

  /**
   * Constructs a plot rectangle.
   * @param name the rectangle name.
   * @param textProps the text properties.
   * @param pointProps the point properties.
   * @param lineProps the line properties.
   * @param fillProps the fill properties.
   */
  public PlotRectangle(final String name, final TextProperties textProps, final PointProperties pointProps, final LineProperties lineProps, final FillProperties fillProps) {
    super(name, textProps, pointProps, lineProps, fillProps);
    setShapeType(ShapeType.RECTANGLE);
    setFixedPointCount(true);
  }

}
