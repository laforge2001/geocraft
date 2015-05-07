/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import java.awt.geom.Point2D;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


/**
 * The plot mouse event class.
 * Passed to listeners when a mouse event occurs in the plot.
 */
public class PlotMouseEvent {

  /** The pixel coordinate. */
  private final Point _pixelCoord;

  /** The model coordinate. */
  private final Point2D.Double _modelCoord;

  /** The mouse event. */
  private final MouseEvent _event;

  /** The plot. */
  private final IPlot _plot;

  /** The model canvas. */
  private final IModelSpaceCanvas _canvas;

  /**
   * Constructs a plot mouse event.
   * @param event the original mouse event.
   * @param plot the associated plot.
   * @param canvas the model space canvas.
   * @param pixelCoord the pixel coordinate.
   * @param modelCoord the model coordinate.
   */
  public PlotMouseEvent(final MouseEvent event, final IPlot plot, final IModelSpaceCanvas canvas, final Point pixelCoord, final Point2D.Double modelCoord) {
    _event = event;
    _pixelCoord = pixelCoord;
    _modelCoord = modelCoord;
    _plot = plot;
    _canvas = canvas;
  }

  public MouseEvent getMouseEvent() {
    return _event;
  }

  /**
   * Gets the pixel coordinate.
   * @return the pixel coordinate.
   */
  public Point getPixelCoord() {
    return _pixelCoord;
  }

  /**
   * Gets the model coordinate.
   * @return the model coordinate.
   */
  public Point2D.Double getModelCoord() {
    return _modelCoord;
  }

  /**
   * Gets the plot.
   * @return the plot.
   */
  public IPlot getPlot() {
    return _plot;
  }

  /**
   * Gets the model canvas.
   * @return the model canvas.
   */
  public IModelSpaceCanvas getModelCanvas() {
    return _canvas;
  }

}
