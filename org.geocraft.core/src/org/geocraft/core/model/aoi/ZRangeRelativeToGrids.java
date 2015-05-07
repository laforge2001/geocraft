package org.geocraft.core.model.aoi;


import org.geocraft.core.model.datatypes.ZDomain;
import org.geocraft.core.model.grid.Grid3d;


/**
 * This class defines a processing z-range relative to top and base reference grids.
 * <p>
 * The starting and ending z values are relative to the reference grids.
 */
public final class ZRangeRelativeToGrids extends ZRange {

  /** The top reference grid. */
  private final Grid3d _topGrid;

  /** The base reference grid. */
  private final Grid3d _baseGrid;

  /** The shift of the starting z values relative to the top reference grid. */
  private final float _zRelativeStart;

  /** The shift of the ending z values relative to the base reference grid. */
  private final float _zRelativeEnd;

  /**
   * Constructs a z-range that is define relative to top and base reference grids.
   * 
   * @param topGrid the top reference grid.
   * @param baseGrid the base reference grid.
   * @param zRelativeStart the shift of the starting z values relative to the top reference grid.
   * @param zRelativeEnd the shift of the ending z values relative to the base reference grid.
   */
  public ZRangeRelativeToGrids(final Grid3d topGrid, final Grid3d baseGrid, final float zRelativeStart, final float zRelativeEnd) {
    super(ZRange.Type.RELATIVE_TO_GRIDS, ZDomain.getFromDomain(topGrid.getZDomain()));
    _topGrid = topGrid;
    _baseGrid = baseGrid;
    _zRelativeStart = zRelativeStart;
    _zRelativeEnd = zRelativeEnd;
  }

  @Override
  public synchronized float[] getZStartAndEnd(final double x, final double y) {
    float zStart = Float.NaN;
    float zEnd = Float.NaN;

    // Synchronize on the top grid to prevent its modification.
    synchronized (_topGrid) {
      // Transform the x,y coordinates to row,column coordinates.
      int[] rowcol = transformXYToRowCol(_topGrid.getGeometry(), x, y);
      int row = rowcol[0];
      int col = rowcol[1];
      // Check if the row,column coordinates are within the top grid.
      if (row >= 0 && row < _topGrid.getNumRows() && col >= 0 && col < _topGrid.getNumColumns()) {
        // If so, then check if the grid value is non-null.
        if (!_topGrid.isNull(row, col)) {
          // If so, then compute the starting z value.
          zStart = _topGrid.getValueAtRowCol(row, col) + _zRelativeStart;
        }
      }
      // If the starting z is undefined, then go ahead and return.
      if (Float.isNaN(zStart)) {
        return new float[0];
      }
    }

    // Synchronize on the base grid to prevent its modification.
    synchronized (_baseGrid) {
      // Transform the x,y coordinates to row,column coordinates.
      int[] rowcol = transformXYToRowCol(_baseGrid.getGeometry(), x, y);
      int row = rowcol[0];
      int col = rowcol[1];
      // Check if the row,column coordinates are within the base grid.
      if (row >= 0 && row < _baseGrid.getNumRows() && col >= 0 && col < _baseGrid.getNumColumns()) {
        // If so, then check if the grid value is non-null.
        if (!_baseGrid.isNull(row, col)) {
          // If so, then compute the ending z value.
          zEnd = _baseGrid.getValueAtRowCol(row, col) + _zRelativeEnd;
          return new float[] { zStart, zEnd };
        }
      }
      return new float[0];
    }
  }
}
