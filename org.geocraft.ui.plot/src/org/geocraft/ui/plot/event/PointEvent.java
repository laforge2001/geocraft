/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.event;


import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * The plot point event class.
 * Passed to listeners when a plot point is added, removed, selected, deselected, moved or updated.
 */
public class PointEvent extends AbstractPlotEvent {

  /** The plot point. */
  protected IPlotPoint _point;

  /** The plot shape. */
  protected IPlotShape _shape;

  /**
   * Constructs a plot point event.
   * @param point the plot point.
   * @param type the event type.
   */
  public PointEvent(final IPlotPoint point, final PlotEventType type) {
    this(point, null, type);
  }

  /**
   * Constructs a plot point event.
   * @param point the plot point.
   * @param shape the plot shape.
   * @param type the event type.
   */
  public PointEvent(final IPlotPoint point, final IPlotShape shape, final PlotEventType type) {
    super(type);
    _point = point;
    _shape = shape;
  }

  /**
   * gets the plot point.
   * @return the plot point.
   */
  public IPlotPoint getPoint() {
    return _point;
  }

  /**
   * Gets the plot shape.
   * @return the plot shape.
   */
  public IPlotShape getShape() {
    return _shape;
  }

}
