/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


/**
 * The basic implementation of a plot line.
 */
public class PlotLine extends PlotShape implements IPlotLine {

  /** The plot line properties. */
  protected LineProperties _lineProperties;

  /**
   * Constructs an plot line with a blank name and default properties.
   */
  public PlotLine() {
    this("", new TextProperties(), new PointProperties(), new LineProperties());
  }

  /**
   * Constructs an plot line with properties copied from another plot line.
   * @param line the plot line from which to copy properties.
   */
  public PlotLine(final IPlotLine line) {
    this(line.getName(), line.getTextProperties(), line.getPointProperties(), line.getLineProperties());
  }

  /**
   * Constructs a plot line with specified properties.
   * @param name the line name.
   * @param textProps the text properties.
   * @param pointProps the point properties.
   * @param lineProps the line properties.
   */
  public PlotLine(final String name, final TextProperties textProps, final PointProperties pointProps, final LineProperties lineProps) {
    super(ShapeType.LINE, name);
    setFixedPointCount(true);
    _isMovable = true;
    // Create the text, point and line properties.
    _textProperties = new TextProperties(textProps);
    _pointProperties = new PointProperties(pointProps);
    _lineProperties = new LineProperties(lineProps);
    // Create two dummy points and set them.
    setPoints(new PlotPoint(), new PlotPoint());
  }

  public void setPoints(final IPlotPoint point1, final IPlotPoint point2) {
    // Clear the shape of any existing points.
    clear();
    // Block the points from firing updates.
    point1.blockUpdate();
    point2.blockUpdate();

    _xmin = Math.min(point1.getX(), point2.getX());
    _xmax = Math.max(point1.getX(), point2.getX());
    _ymin = Math.min(point1.getY(), point2.getY());
    _ymax = Math.max(point1.getY(), point2.getY());

    // Update the points as necessary.
    point1.setModelSpace(getModelSpace());
    point2.setModelSpace(getModelSpace());
    _points.add(point1);
    _points.add(point2);
    point1.setLayer(_group);
    point1.addPlotPointListener(this);
    point1.setShape(this);
    point1.setSelected(_isSelected);
    point2.setLayer(_group);
    point2.addPlotPointListener(this);
    point2.setShape(this);
    point2.setSelected(_isSelected);

    // Unblock the points from firing updates.
    point1.unblockUpdate();
    point2.unblockUpdate();

    // Fire an updated event from the line shape.
    updated();
  }

  /**
   * 
   */
  public void movePointBy(final IPlotPoint point, final double dx, final double dy) {
    movePointBy(point, dx, dy, 0);
    updated();
  }

  public void movePointBy(final IPlotPoint point, final double dx, final double dy, final double dz) {
    if (point == null) {
      return;
    }
    point.blockUpdate();
    point.moveBy(dx, dy, dz);
    point.unblockUpdate();
    updated();
  }

  public void movePointTo(final IPlotPoint point, final double x, final double y) {
    movePointTo(point, x, y, 0);
  }

  public void movePointTo(final IPlotPoint point, final double x, final double y, final double z) {
    if (point == null) {
      return;
    }
    point.blockUpdate();
    point.moveTo(x, y, z);
    point.unblockUpdate();
    updated();
  }

  @Override
  public void moveBy(final double dx, final double dy) {
    moveBy(dx, dy, 0);
  }

  @Override
  public void moveBy(final double dx, final double dy, final double dz) {
    for (IPlotPoint point : _points) {
      point.blockUpdate();
      movePointBy(point, dx, dy, dz);
      point.unblockUpdate();
    }
    updated();
  }

  @Override
  public void select() {
    super.select();
    int size = _points.size();
    for (int i = 0; i < size; i++) {
      IPlotPoint point = _points.get(i);
      point.select();
    }
  }

  @Override
  public void deselect() {
    super.deselect();
    int size = _points.size();
    for (int i = 0; i < size; i++) {
      IPlotPoint point = _points.get(i);
      point.deselect();
    }
  }

  public Rectangle getRectangle(final IModelSpaceCanvas canvas) {
    Rectangle rect = null;
    int ps;
    int pxmin = 0;
    int pxmax = 0;
    int pymin = 0;
    int pymax = 0;
    boolean first = true;
    for (int i = 0; i < getPointCount(); i++) {
      IPlotPoint point = getPoint(i);
      if (point != null) {
        IModelSpace model = point.getLayer().getModelSpace();
        if (model != null) {
          Point2D.Double p = new Point2D.Double(0, 0);
          canvas.transformModelToPixel(model, point.getX(), point.getY(), p);
          ps = point.getPointProperties().getSize();
          if (ps < 0) {
            ps = getPointSize();
          }
          if (ps < 5) {
            ps = 5;
          }
          ps *= 2;
          if (first) {
            first = false;
            pxmin = (int) (p.x - ps);
            pxmax = (int) (p.x + ps);
            pymin = (int) (p.y - ps);
            pymax = (int) (p.y + ps);
          } else {
            pxmin = Math.min(pxmin, (int) (p.x - ps));
            pxmax = Math.max(pxmax, (int) (p.x + ps));
            pymin = Math.min(pymin, (int) (p.y - ps));
            pymax = Math.max(pymax, (int) (p.y + ps));
          }
        }
      }
    }

    if (!first) {
      int width = pxmax - pxmin + 1;
      int height = pymax - pymin + 1;
      rect = new Rectangle(pxmin, pymin, width, height);
      rect.x += 0;
      rect.y += 0;
    }
    return rect;
  }

  public LineProperties getLineProperties() {
    return _lineProperties;
  }

  public LineStyle getLineStyle() {
    return _lineProperties.getStyle();
  }

  public RGB getLineColor() {
    return _lineProperties.getColor();
  }

  public int getLineWidth() {
    return _lineProperties.getWidth();
  }

  public void setLineStyle(final LineStyle style) {
    _lineProperties.setStyle(style);
    updated();
  }

  public void setLineColor(final RGB color) {
    _lineProperties.setColor(color);
    updated();
  }

  public void setLineWidth(final int width) {
    _lineProperties.setWidth(width);
    updated();
  }

  public void propertyChange(final PropertyChangeEvent event) {
    updated();
  }

  @Override
  public void dispose() {
    super.dispose();
    _lineProperties.dispose();
  }
}
