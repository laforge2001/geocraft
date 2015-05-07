/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.eclipse.swt.widgets.Composite;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisPlacement;


public interface IAxisCanvas extends ICanvas {

  /**
   * Gets the axis currently displayed in the axis canvas.
   * 
   * @return the axis currently displayed in the axis canvas.
   */
  IAxis getAxis();

  /**
   * Sets the axis to display in the axis canvas.
   * 
   * @param axis the axis to display in the axis canvas.
   */
  void setAxis(IAxis axis);

  /**
   * Gets the placement of the axis canvas.
   * 
   * @return the placement of the axis canvas.
   */
  AxisPlacement getPlacement();

  void redraw();

  Composite getComposite();
}
