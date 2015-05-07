/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.listener;

import org.geocraft.ui.plot.event.ShapeEvent;


/**
 * The listener interface for receiving plot shape events.
 */
public interface IPlotShapeListener {

  /**
   * Invoked when the plot shape is updated.
   * @param event the plot shape event.
   */
  void shapeUpdated(ShapeEvent event);
}
