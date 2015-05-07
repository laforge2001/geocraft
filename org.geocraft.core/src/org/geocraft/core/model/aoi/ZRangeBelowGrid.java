package org.geocraft.core.model.aoi;


import org.geocraft.core.model.datatypes.ZDomain;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;


/**
 * This class defines a processing z-range below a reference grid.
 * <p>
 * The starting z value is relative to the reference grid, and the ending z value is a constant.
 */
public final class ZRangeBelowGrid extends ZRange {

  /** The reference grid for the starting z values. */
  private final Grid3d _referenceGrid;

  /** The shift of the starting z values relative to the reference grid. */
  private final float _zRelativeStart;

  /** The ending z value. */
  private final float _zEnd;

  /**
   * Constructs a z-range that is defined below a reference grid.
   * 
   * @param referenceGrid the reference grid.
   * @param zRelativeStart the shift of the starting z values relative to the reference grid.
   * @param zEnd the ending z value.
   */
  public ZRangeBelowGrid(final Grid3d referenceGrid, final float zRelativeStart, final float zEnd) {
    super(ZRange.Type.BELOW_GRID, ZDomain.getFromDomain(referenceGrid.getZDomain()));
    _zRelativeStart = zRelativeStart;
    _zEnd = zEnd;
    _referenceGrid = referenceGrid;
  }

  @Override
  public float[] getZStartAndEnd(final double x, final double y) {
    // Synchronize on the grid to prevent its modification.
    synchronized (_referenceGrid) {
      // Transform the x,y coordinates to row,column coordinates.
      GridGeometry3d geometry = _referenceGrid.getGeometry();
      int[] rowcol = transformXYToRowCol(geometry, x, y);
      int row = rowcol[0];
      int col = rowcol[1];
      // Check if the row,column coordinates are within the reference grid.
      if (row >= 0 && row < _referenceGrid.getNumRows() && col >= 0 && col < _referenceGrid.getNumColumns()) {
        // If so, then check if the grid value is non-null.
        if (!_referenceGrid.isNull(row, col)) {
          // If so, then compute the starting z value.
          float zStart = _referenceGrid.getValueAtRowCol(row, col) + _zRelativeStart;
          return new float[] { zStart, _zEnd };
        }
      }
      return new float[0];
    }
  }
}
