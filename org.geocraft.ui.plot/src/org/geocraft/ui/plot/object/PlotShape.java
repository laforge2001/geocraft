/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.ObjectType;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.defs.TextAnchor;
import org.geocraft.ui.plot.event.PointEvent;
import org.geocraft.ui.plot.event.ShapeEvent;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.listener.IPlotShapeListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.ModelSpaceBounds;


/**
 * The abstract implementation of a plot shape.
 */
public abstract class PlotShape extends PlotObject implements IPlotShape {

  /** The shape type. */
  protected ShapeType _shapeType;

  /** The plot points contained in the shape. */
  protected List<IPlotPoint> _points;

  /** The active plot point (or null if none). */
  protected IPlotPoint _pointActive;

  /** The movable status of the shape. */
  protected boolean _isMovable;

  /** The fixed-count-count status of the shape (true for line and rectangle). */
  protected boolean _isFixedPointCount;

  /** The previous shape in the plot group. */
  protected IPlotShape _prevShape;

  /** The next shape in the plot group. */
  protected IPlotShape _nextShape;

  /** The plot shape listeners. */
  protected List<IPlotShapeListener> _listeners;

  /** The minimum model x-coordinate. */
  protected double _xmin;

  /** The maximum model x-coordinate. */
  protected double _xmax;

  /** The minimum model y-coordinate. */
  protected double _ymin;

  /** The maximum model y-coordinate. */
  protected double _ymax;

  /** The minimum model z-coordinate. */
  protected double _zmin;

  /** The maximum model z-coordinate. */
  protected double _zmax;

  /** The transparency (0-100). */
  protected int _transparency;

  /** The object text properties. */
  protected TextProperties _textProperties;

  /**
   * Constructs a plot shape.
   * @param type the shape type.
   * @param name the shape name.
   * @param textProps the text properties.
   * @param pointProps the pointProperties.
   */
  public PlotShape(final ShapeType type, final String name, final TextProperties textProps, final PointProperties pointProps) {
    super(ObjectType.SHAPE, name, pointProps);
    setShapeType(type);
    _transparency = 0;
    _textProperties = new TextProperties(textProps);
    _isMovable = false;
    _pointActive = null;
    _points = Collections.synchronizedList(new ArrayList<IPlotPoint>());
    _listeners = Collections.synchronizedList(new ArrayList<IPlotShapeListener>());
    setFixedPointCount(false);
    // TODO: addTreePopupAction(new RenamePlotShapeAction(this));
    // TODO: addTreePopupAction(new EditPlotShapePropertiesAction(this));
  }

  /**
   * Constructs a plot shape.
   * @param type the shape type.
   * @param name the shape name.
   */
  public PlotShape(final ShapeType type, final String name) {
    super(ObjectType.SHAPE, name);
    setShapeType(type);
    _textProperties = new TextProperties();
    _isMovable = false;
    _pointActive = null;
    _points = Collections.synchronizedList(new ArrayList<IPlotPoint>());
    _listeners = Collections.synchronizedList(new ArrayList<IPlotShapeListener>());
    setFixedPointCount(false);
    // TODO: addTreePopupAction(new RenamePlotShapeAction(this));
    // TODO: addTreePopupAction(new EditPlotShapePropertiesAction(this));
  }

  public ShapeType getShapeType() {
    return _shapeType;
  }

  public void setShapeType(final ShapeType shapeType) {
    _shapeType = shapeType;
  }

  public boolean isMovable() {
    return _isMovable;
  }

  @Override
  public void setLayer(final IPlotLayer group) {
    super.setLayer(group);
    for (IPlotPoint point : _points) {
      point.setLayer(group);
    }
    updated();
  }

  public void clear() {
    _points.clear();
    _pointActive = null;
    _xmin = 0;
    _xmax = 0;
    _ymin = 0;
    _ymax = 0;
    _zmin = 0;
    _zmax = 0;
    updated();
  }

  public double getMinimumX() {
    return _xmin;
  }

  public double getMaximumX() {
    return _xmax;
  }

  public double getMinimumY() {
    return _ymin;
  }

  public double getMaximumY() {
    return _ymax;
  }

  public double getMinimumZ() {
    return _zmin;
  }

  public double getMaximumZ() {
    return _zmax;
  }

  public boolean isFixedPointCount() {
    return _isFixedPointCount;
  }

  public void setFixedPointCount(final boolean isFixedPointCount) {
    _isFixedPointCount = isFixedPointCount;
  }

  @Override
  public void setModelSpace(final IModelSpace modelSpace) {
    super.setModelSpace(modelSpace);
    for (IPlotPoint point : _points) {
      point.setModelSpace(modelSpace);
    }
  }

  public void selectPoint(final IPlotPoint point) {
    point.select();
    updated();
  }

  public void deselectPoint(final IPlotPoint point) {
    point.deselect();
    updated();
  }

  public void moveBy(final double dx, final double dy) {
    moveBy(dx, dy, 0);
  }

  public void moveBy(final double dx, final double dy, final double dz) {
    blockUpdate();
    for (IPlotPoint point : _points) {
      point.blockUpdate();
      point.moveBy(dx, dy, dz);
      point.unblockUpdate();
    }
    unblockUpdate();
    updated();
  }

  public int getPointCount() {
    int pointCount = _points.size();
    return pointCount;
  }

  public IPlotPoint getFirstPoint() {
    IPlotPoint point;
    try {
      point = _points.get(0);
    } catch (NoSuchElementException e) {
      point = null;
    }
    return point;
  }

  public IPlotPoint getLastPoint() {
    IPlotPoint point;
    try {
      point = _points.get(_points.size() - 1);
    } catch (NoSuchElementException e) {
      point = null;
    }
    return point;
  }

  public IPlotPoint getPrevPoint(final IPlotPoint point) {
    IPlotPoint pointPrevShape = null;
    int size = _points.size();
    int ndx = _points.indexOf(point);
    if (size < 1) {
      return pointPrevShape;
    }
    if (ndx > 0 && ndx < size) {
      pointPrevShape = _points.get(ndx - 1);
    }
    return pointPrevShape;
  }

  public IPlotPoint getNextPoint(final IPlotPoint point) {
    IPlotPoint pointNextShape = null;
    int size = _points.size();
    int ndx = _points.indexOf(point);
    if (size < 1) {
      return pointNextShape;
    }
    if (ndx >= 0 && ndx < size - 1) {
      pointNextShape = _points.get(ndx + 1);
    }
    return pointNextShape;
  }

  public IPlotPoint getPoint(final int index) {
    IPlotPoint point = null;
    try {
      point = _points.get(index);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Point index out of bounds: " + index);
    }
    return point;
  }

  public IPlotPoint[] getPoints() {
    IPlotPoint[] points = new IPlotPoint[getPointCount()];
    for (int i = 0; i < points.length; i++) {
      points[i] = _points.get(i);
    }
    return points;
  }

  public IPlotShape getPrevShape() {
    return _prevShape;
  }

  public IPlotShape getNextShape() {
    return _nextShape;
  }

  public void setPrevShape(final IPlotShape prevShape) {
    _prevShape = prevShape;
    updated();
  }

  public void setNextShape(final IPlotShape nextShape) {
    _nextShape = nextShape;
    updated();
  }

  public void addShapeListener(final IPlotShapeListener listener) {
    _listeners.add(listener);
  }

  public void removeShapeListener(final IPlotShapeListener listener) {
    _listeners.remove(listener);
  }

  @Override
  public void redraw() {
    updated();
  }

  @Override
  public String toString() {
    return getName();
  }

  public void updated() {
    firePlotShapeEvent(new ShapeEvent(this, PlotEventType.SHAPE_UPDATED));
  }

  public void added() {
    firePlotShapeEvent(new ShapeEvent(this, PlotEventType.SHAPE_ADDED));
  }

  public void removed() {
    firePlotShapeEvent(new ShapeEvent(this, PlotEventType.SHAPE_REMOVED));
  }

  public void selected() {
    firePlotShapeEvent(new ShapeEvent(this, PlotEventType.SHAPE_SELECTED));
  }

  public void deselected() {
    firePlotShapeEvent(new ShapeEvent(this, PlotEventType.SHAPE_DESELECTED));
  }

  public void motion() {
    firePlotShapeEvent(new ShapeEvent(this, PlotEventType.SHAPE_MOTION));
  }

  public void motionStart() {
    firePlotShapeEvent(new ShapeEvent(this, PlotEventType.SHAPE_START_MOTION));
  }

  public void motionEnd() {
    firePlotShapeEvent(new ShapeEvent(this, PlotEventType.SHAPE_END_MOTION));
  }

  public void pointUpdated(final PointEvent event) {
    firePlotShapeEvent(new ShapeEvent(this, event.getEventType()));
  }

  /**
   * Fires a plot shape event to the listeners.
   * @param event the event to fire.
   */
  protected void firePlotShapeEvent(final ShapeEvent event) {
    if (_listeners == null || isUpdateBlocked()) {
      return;
    }

    IPlotShapeListener[] listeners = _listeners.toArray(new IPlotShapeListener[0]);
    for (IPlotShapeListener listener : listeners) {
      listener.shapeUpdated(event);
    }
  }

  public ModelSpaceBounds getBounds() {
    return new ModelSpaceBounds(getMinimumX(), getMaximumX(), getMinimumY(), getMaximumY());
  }

  public TextProperties getTextProperties() {
    return _textProperties;
  }

  public Font getTextFont() {
    return _textProperties.getFont();
  }

  public RGB getTextColor() {
    return _textProperties.getColor();
  }

  public TextAnchor getTextAnchor() {
    return _textProperties.getAnchor();
  }

  public void setTextFont(final Font font) {
    _textProperties.setFont(font);
    updated();
  }

  public void setTextColor(final RGB color) {
    _textProperties.setColor(color);
    updated();
  }

  public void setTextAnchor(final TextAnchor anchor) {
    _textProperties.setAnchor(anchor);
    updated();
  }

  public int getTransparency() {
    return _transparency;
  }

  public void setTransparency(final int transparency) {
    _transparency = transparency;
    updated();
  }

  @Override
  public void dispose() {
    _textProperties.dispose();
    for (IPlotPoint point : _points) {
      point.dispose();
    }
    _points.clear();
    _listeners.clear();
    super.dispose();
  }

}
