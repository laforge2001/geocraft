/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


/**
 * The listener interface for the AB crossplot ellipse model.
 */
public interface EllipseRegionsModelListener {

  /**
   * Invoked when the ellipse model is updated.
   * @param event the ellipse model event.
   */
  void ellipseModelUpdated(EllipseRegionsModelEvent event);
}
