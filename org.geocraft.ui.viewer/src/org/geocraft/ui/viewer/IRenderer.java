/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer;


import org.eclipse.swt.graphics.Image;
import org.geocraft.core.model.Model;


/**
 * The interface for all viewer renderers.
 * Each renderer is capable of supplying readout info corresponding to
 * given x-y view coordinates.
 */
public interface IRenderer {

  /**
   * Returns the unique ID of the renderer.
   * <p>
   * For entity renderer's, this can be the unique ID of the entity itself.
   * 
   * @return the unique ID of the renderer.
   */
  String getUniqueID();

  /**
   * Gets the display name of the renderer.
   * 
   * @return the display name.
   */
  String getName();

  /**
   * Sets the display name of the renderer.
   * 
   * @param name the display name.
   */
  void setName(String name);

  /**
   * Gets the image to use for the renderer icon.
   * 
   * @return the renderer image.
   */
  Image getImage();

  /**
   * Returns the flag for whether or not to show cursor readout info for the renderer.
   * @returns <i>true</i> if readout info is shown; <i>false</i> if readout info is hidden.
   */
  boolean showReadoutInfo();

  /**
   * Sets the flag for whether or not to show cursor readout info for the renderer.
   * @param show <i>true</i> to show readout info; <i>false</i> to hide readout info.
   */
  void showReadoutInfo(boolean show);

  /**
   * Returns the renderer readout info for the given x-y coordinates.
   * The x,y coordinate are specific to a particular view (i.e. they may
   * be real-world x-y coordinates for a map view, trace and z coordinates
   * for a section view, etc.
   * 
   * @param x the viewer x coordinate.
   * @param y the viewer y coordinate.
   * @return the renderer readout info.
   */
  ReadoutInfo getReadoutInfo(double x, double y);

  /**
   * Gets the visibility of the renderer.
   * 
   * @return <i>true</i> if visible; otherwise <i>false</i>.
   */
  boolean isVisible();

  /**
   * Get the objects rendered by the renderer.
   */
  Object[] getRenderedObjects();

  /**
   * Get the settings model for the renderer.
   * 
   * @return the renderer settings model.
   */
  Model getSettingsModel();

  /**
   * Refreshes the renderer.
   */
  void refresh();

  /**
   * Redraws the renderer.
   */
  void redraw();

  /**
   * Triggers the action to popup the renderer settings dialog.
   */
  void editSettings();

  /**
   * Clears the renderer when removing from the viewer.
   */
  void clear();
}
