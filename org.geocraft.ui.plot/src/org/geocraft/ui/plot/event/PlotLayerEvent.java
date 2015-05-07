/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.event;


import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * The plot layer event class.
 * Passed to listeners when a plot layer is added, removed, selected, deselected or updated.
 */
public class PlotLayerEvent extends AbstractPlotEvent {

  /** The plot layer. */
  protected IPlotLayer _layer;

  /** The plot shape. */
  protected IPlotShape _shape;

  /**
   * Constructs a plot layer event (with no shape).
   * @param layer the plot layer.
   * @param type the event type.
   */
  public PlotLayerEvent(final IPlotLayer layer, final PlotEventType type) {
    this(layer, null, type);
  }

  /**
   * Constructs a plot layer event.
   * @param layer the plot layer.
   * @param shape the plot shape.
   * @param type the event type.
   */
  public PlotLayerEvent(final IPlotLayer layer, final IPlotShape shape, final PlotEventType type) {
    super(type);
    _layer = layer;
    _shape = shape;
  }

  /**
   * Gets the plot layer.
   * @return the plot layer.
   */
  public IPlotLayer getLayer() {
    return _layer;
  }

  /**
   * Gets the plot shape.
   * @return the plot shape.
   */
  public IPlotShape getShape() {
    return _shape;
  }
}
