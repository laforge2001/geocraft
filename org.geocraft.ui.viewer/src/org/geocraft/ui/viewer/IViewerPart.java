/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved. 
 */
package org.geocraft.ui.viewer;


public interface IViewerPart {

  /**
   * Get the viewers associated with a viewer part
   * @return The list of viewers associated with a viewer part
   */
  IViewer[] getViewers();

  /**
   * Get the ID of the viewer part
   * @return Viewer part's ID
   */
  public String getPartId();

  /**
   * Get the name of the viewer part
   * @return Viewer part's name
   */
  public String getPartName();

  /**
   * Set the name of a viewer part
   * @param partName The viewer part's name
   */
  public void setViewerPartName(String partName);
}
