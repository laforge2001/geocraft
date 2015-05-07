/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.defs.ObjectType;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.event.PointEvent;
import org.geocraft.ui.plot.listener.IPlotPointListener;


/**
 * The basic implementation of a plot point object.
 */
public class PlotPoint extends PlotObject implements IPlotPoint {

  /** The model x-coordinate. */
  protected double _x;

  /** The model y-coordinate. */
  protected double _y;

  /** The model z-coordinate. */
  protected double _z;

  /** The shape with which the point is associated (or null if none). */
  protected IPlotShape _shape;

  /** The plot point listeners. */
  protected List<IPlotPointListener> _listeners;

  /** The point property inheritance flag. */
  protected boolean _propertyInheritance;

  protected Image _image = null;

  /**
   * Constructs a plot point (with NaN coordinates).
   */
  public PlotPoint() {
    this(Double.NaN, Double.NaN, Double.NaN);
  }

  /**
   * Constructs a plot point.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @param z the z-coordinate.
   */
  public PlotPoint(final double x, final double y, final double z) {
    this("", x, y, z, new PointProperties());
  }

  /**
   * Constructs a plot point.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @param z the z-coordinate.
   */
  public PlotPoint(final String text, final double x, final double y, final double z) {
    this(text, x, y, z, new PointProperties());
  }

  /**
   * Constructs a plot point.
   * @param name the point name.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @param z the z-coordinate.
   * @param textProps the text properties.
   * @param pointProps the point properties.
   */
  public PlotPoint(final String name, final double x, final double y, final double z, final PointProperties pointProps) {
    super(ObjectType.POINT, name, pointProps);
    _shape = null;
    _listeners = Collections.synchronizedList(new ArrayList<IPlotPointListener>());
    setXYZ(x, y, z);
    setName(name);
    setPropertyInheritance(false);
  }

  /**
   * Constructs a plot point, copied from another plot point.
   * @param point the point to copy.
   */
  public PlotPoint(final IPlotPoint point) {
    this(point.getName(), point.getX(), point.getY(), point.getZ(), point.getPointProperties());
  }

  public IPlotShape getShape() {
    return _shape;
  }

  public void setShape(final IPlotShape shape) {
    _shape = shape;
    if (shape != null) {
      added();
    } else {
      removed();
    }
  }

  /**
   * Moves the point by the specified delta-x, delta-y.
   * @param dx the delta-x to move the point.
   * @param dy the delta-y to move the point.
   */
  public void moveBy(final double dx, final double dy) {
    moveTo(_x + dx, _y + dy, getZ());
  }

  /**
   * Moves the point by the specified delta-x, delta-y, delta-z.
   * @param dx the delta-x to move the point.
   * @param dy the delta-y to move the point.
   * @param dz the delta-z to move the point.
   */
  public void moveBy(final double dx, final double dy, final double dz) {
    moveTo(_x + dx, _y + dy, _z + dz);
  }

  /**
   * Moves the point to the specified x,y coordinate.
   * @param x the x-coordinate to move the point.
   * @param y the y-coordinate to move the point.
   */
  public void moveTo(final double x, final double y) {
    moveTo(x, y, getZ());
  }

  /**
   * Moves the point to the specified x,y,z coordinate.
   * @param x the x-coordinate to move the point.
   * @param y the y-coordinate to move the point.
   * @param z the z-coordinate to move the point.
   */
  public void moveTo(final double x, final double y, final double z) {
    _x = x;
    _y = y;
    _z = z;
    motion();
  }

  public double getX() {
    return _x;
  }

  public double getY() {
    return _y;
  }

  public double getZ() {
    return _z;
  }

  public void setX(final double x) {
    moveTo(x, getY(), getZ());
  }

  public void setY(final double y) {
    moveTo(getX(), y, getZ());
  }

  public void setZ(final double z) {
    moveTo(getX(), getY(), z);
  }

  public void setXY(final double x, final double y) {
    moveTo(x, y, getZ());
  }

  public void setXYZ(final double x, final double y, final double z) {
    moveTo(x, y, z);
  }

  public void addPlotPointListener(final IPlotPointListener listener) {
    _listeners.add(listener);
  }

  public void removePlotPointListener(final IPlotPointListener listener) {
    _listeners.remove(listener);
  }

  public void updated() {
    firePlotPointEvent(new PointEvent(this, PlotEventType.POINT_UPDATED));
  }

  public void added() {
    firePlotPointEvent(new PointEvent(this, PlotEventType.POINT_ADDED));
  }

  public void removed() {
    firePlotPointEvent(new PointEvent(this, PlotEventType.POINT_REMOVED));
  }

  public void selected() {
    firePlotPointEvent(new PointEvent(this, PlotEventType.POINT_SELECTED));
  }

  public void deselected() {
    firePlotPointEvent(new PointEvent(this, PlotEventType.POINT_DESELECTED));
  }

  public void motionStart() {
    firePlotPointEvent(new PointEvent(this, PlotEventType.POINT_START_MOTION));
  }

  public void motion() {
    firePlotPointEvent(new PointEvent(this, PlotEventType.POINT_MOTION));
  }

  public void motionEnd() {
    firePlotPointEvent(new PointEvent(this, PlotEventType.POINT_END_MOTION));
  }

  public boolean getPropertyInheritance() {
    return _propertyInheritance;
  }

  public void setPropertyInheritance(final boolean propertyInheritance) {
    _propertyInheritance = propertyInheritance;
    updated();
  }

  /**
   * Fires a plot point event to the listeners.
   * @param event the event to fire.
   */
  protected void firePlotPointEvent(final PointEvent event) {
    if (_listeners == null || isUpdateBlocked()) {
      return;
    }
    IPlotPointListener[] listeners = _listeners.toArray(new IPlotPointListener[0]);
    for (IPlotPointListener listener : listeners) {
      listener.pointUpdated(event);
    }
  }

  @Override
  public String toString() {
    return _name + " : x=" + _x + " y=" + _y + " z=" + _z;
  }

  public void propertyChange(final PropertyChangeEvent event) {
    updated();
  }

  @Override
  public void dispose() {
    _listeners.clear();
    super.dispose();
  }

  public void redraw() {
    updated();
  }

  public Image getPointImage() {
    return _image;
  }

  public void setPointImage(Image image) {
    _image = image;
  }
}
