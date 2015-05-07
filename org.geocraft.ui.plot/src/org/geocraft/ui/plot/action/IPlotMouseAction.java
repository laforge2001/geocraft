/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;

import org.geocraft.ui.plot.listener.IPlotMouseListener;


public interface IPlotMouseAction extends IPlotMouseListener {

  /**
   * Gets the action name.
   * @return the action name.
   */
  String getName();

  /**
   * Gets the action description.
   * @return the action description.
   */
  String getDescription();

  /**
   * Gets the action mask (mouse buttons, clicks, modifiers).
   * @return the action mask.
   */
  PlotActionMask getMask();

  /**
   * Gets the action name.
   * @param name the action name to set.
   */
  void setName(String name);

  /**
   * Sets the action description.
   * @param description the action description to set.
   */
  void setDescription(String description);

  /**
   * Sets the action mask (mouse buttons, clicks, modifiers).
   * @param mask the action mask to set.
   */
  void setMask(PlotActionMask mask);

  /**
   * Handles the Plot actionPerformed mouse events.
   * @param event the mouse event to handle.
   */
  void actionPerformed(PlotMouseEvent event);
}
