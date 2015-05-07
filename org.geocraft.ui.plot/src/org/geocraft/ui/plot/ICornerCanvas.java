/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.geocraft.ui.plot.defs.CornerPlacement;


/**
 * The interface for a corner canvas of a plot.
 */
public interface ICornerCanvas extends ICanvas {

  /**
   * Returns the placement of the corner canvas.
   * @return the placement of the corner canvas.
   */
  CornerPlacement getPlacement();

  void setRenderer(ICornerRenderer renderer);
}
