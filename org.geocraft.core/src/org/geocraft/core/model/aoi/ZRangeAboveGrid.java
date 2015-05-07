package org.geocraft.core.model.aoi;


import org.geocraft.core.model.datatypes.ZDomain;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;


/**
 * This class defines a processing z-range above a reference grid.
 * <p>
 * The starting z value is constant, and the ending z value is relative to the reference grid.
 */
public final class ZRangeAboveGrid extends ZRange {

  /** The reference grid for the ending z values. */
  private final Grid3d _referenceGrid;

  /** The starting z value. */
  private final float _zStart;

  /** The shift of the ending z values relative to the reference grid. */
  private final float _zRelativeEnd;

  /**
   * Constructs a z-range that is defined above a reference grid.
   * 
   * @param referenceGrid the reference grid.
   * @param zStart the starting z value.
   * @param zRelativeEnd the shift of the ending z values relative to the reference grid.
   */
  public ZRangeAboveGrid(final Grid3d referenceGrid, final float zStart, final float zRelativeEnd) {
    super(ZRange.Type.ABOVE_GRID, ZDomain.getFromDomain(referenceGrid.getZDomain()));
    _zStart = zStart;
    _zRelativeEnd = zRelativeEnd;
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
      // Check if the row,column coordinate are within the reference grid.
      if (row >= 0 && row < _referenceGrid.getNumRows() && col >= 0 && col < _referenceGrid.getNumColumns()) {
        // If so, then check if the grid value is non-null.
        if (!_referenceGrid.isNull(row, col)) {
          // If so, then compute the ending z value.
          float zEnd = _referenceGrid.getValueAtRowCol(row, col) + _zRelativeEnd;
          return new float[] { _zStart, zEnd };
        }
      }
      return new float[0];
    }
  }
}
