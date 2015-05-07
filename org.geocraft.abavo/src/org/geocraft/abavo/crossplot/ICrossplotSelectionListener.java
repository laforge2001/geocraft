/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.geocraft.core.model.datatypes.Point3d;


/**
 * The listener interface for ABAVO crossplot selection events.
 */
public interface ICrossplotSelectionListener {

  /**
   * Invoked when points are selected in the crossplot.
   * @param points the array of points selected.
   */
  void pointsSelected(final Point3d[] points);
}
