package org.geocraft.geomath.algorithm.util;


import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


public class HorizonUtil {

  public static Float getInlineRange(final PostStack3d velVol) {
    return Math.abs((velVol.getInlineStart() - velVol.getInlineEnd()));
  }

  public static Float getXlineRange(final PostStack3d velVol) {
    return Math.abs((velVol.getXlineStart() - velVol.getXlineEnd()));
  }

  public static boolean isHorizonRowPreferred(final PostStack3d velVol, final GridGeometry3d gridGeometry) {
    int nRows = gridGeometry.getNumRows();
    int nCols = gridGeometry.getNumColumns();
    StorageOrder storageOrder = velVol.getPreferredOrder();

    if (storageOrder == StorageOrder.INLINE_XLINE_Z) {
      return getInlineRange(velVol) / nRows > getInlineRange(velVol) / nCols;
    }
    return true;
  }
}
