/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.core.model.aoi;


import org.geocraft.core.model.geometry.GridGeometry3d;


// Does this really help? 
public class AreaOfInterestHelper {

  public static boolean isInAreaOfInterest(final AreaOfInterest aoi, final GridGeometry3d geometry, final int row,
      final int col) {

    double[] xy = geometry.transformRowColToXY(row, col);

    return aoi == null || aoi.contains(xy[0], xy[1]);
  }

}
