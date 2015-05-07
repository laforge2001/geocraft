/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


/**
 * The listener interface for the AB crossplot polygon model.
 */
public interface PolygonRegionsModelListener {

  /**
   * Invoked when the polygon model is updated.
   * @param event the polygon model event.
   */
  void polygonModelUpdated(PolygonRegionsModelEvent event);
}
