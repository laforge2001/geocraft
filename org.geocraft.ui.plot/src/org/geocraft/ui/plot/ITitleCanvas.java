/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;

import org.geocraft.ui.plot.label.ILabel;




/**
 * The interface for a plot label canvas. This extends the plot canvas
 * interface.
 */
public interface ITitleCanvas extends ICanvas {

  /**
   * Gets the label currently displayed in the label canvas.
   * 
   * @return the label currently displayed in the label canvas.
   */
  ILabel getLabel();

  /**
   * Sets the label to display in the label canvas.
   *  
   * @param label
   *            the label to display in the label canvas.
   */
  void setLabel(ILabel label);

  /**
   * Brings up the editor for the title canvas.
   */
  void editTitle();
}
