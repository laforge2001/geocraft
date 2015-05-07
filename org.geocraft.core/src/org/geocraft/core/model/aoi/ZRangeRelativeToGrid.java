package org.geocraft.core.model.aoi;


import org.geocraft.core.model.datatypes.ZDomain;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;


/**
 * This class defines a processing z-range relative to a reference grid.
 * <p>
 * The starting and ending z values are relative to the reference grid.
 */
public final class ZRangeRelativeToGrid extends ZRange {

  /** The reference grid. */
  private final Grid3d _referenceGrid;

  /** The shift of the starting z values relative to the reference grid. */
  private final float _zRelativeStart;

  /** The shift of the ending z values relative to the reference grid. */
  private final float _zRelativeEnd;

  /**
   * Constructs a z-range that is defined relative to a reference grid.
   * 
   * @param referenceGrid the reference grid.
   * @param zRelativeStart the shift of the starting z values relative to the reference grid.
   * @param zRelativeEnd the shift of the ending z values relative to the reference grid.
   */
  public ZRangeRelativeToGrid(final Grid3d referenceGrid, final float zRelativeStart, final float zRelativeEnd) {
    super(ZRange.Type.RELATIVE_TO_GRID, ZDomain.getFromDomain(referenceGrid.getZDomain()));
    _referenceGrid = referenceGrid;
    _zRelativeStart = zRelativeStart;
    _zRelativeEnd = zRelativeEnd;
  }

  @Override
  public float[] getZStartAndEnd(final double x, final double y) {
    // Synchronize on the reference grid to prevent its modification.
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
          // If so, then compute the starting and ending z values.
          float zGrid = _referenceGrid.getValueAtRowCol(row, col);
          float zStart = zGrid + _zRelativeStart;
          float zEnd = zGrid + _zRelativeEnd;
          return new float[] { zStart, zEnd };
        }
      }
      return new float[0];
    }
  }
}
