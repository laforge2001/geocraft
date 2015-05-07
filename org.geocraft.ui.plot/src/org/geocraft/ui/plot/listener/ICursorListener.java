/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.listener;


/**
 * The interface for a plot cursor listener.
 */
public interface ICursorListener {

  /**
   * Invoked when the plot cursor is updated.
   * @param x the cursor x-coordinate (in model space).
   * @param y the cursor y-coordinate (in model space).
   */
  void cursorUpdated(double x, double y, boolean broadcast);

  /**
   * Invoked when the plot cursor is updated with a selection event.
   * @param x the cursor x-coordinate (in model space).
   * @param y the cursor y-coordinate (in model space).
   */
  void cursorSelectionUpdated(double x, double y);
}
