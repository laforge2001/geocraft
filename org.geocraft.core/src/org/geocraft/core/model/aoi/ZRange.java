package org.geocraft.core.model.aoi;


import org.geocraft.core.model.datatypes.ZDomain;
import org.geocraft.core.model.geometry.GridGeometry3d;


/**
 * The abstract base class for a processing z-range.
 * <p>
 * This consists of a single method that returns the starting and ending z values for a given x,y coordinate.
 */
public abstract class ZRange {

  /**
   * Enumeration of the types of z-ranges.
   */
  public enum Type {
    CONSTANT("Z Range Constant"),
    ABOVE_GRID("Z Range Above Grid"),
    BELOW_GRID("Z Range Below Grid"),
    RELATIVE_TO_GRID("Z Range Relative to Grid"),
    RELATIVE_TO_GRIDS("Z Range Between Grids");

    private String _text;

    private Type(final String text) {
      _text = text;
    }

    @Override
    public String toString() {
      return _text;
    }

    public static Type lookup(final String name) {
      if (name == null) {
        return null;
      }
      for (Type type : Type.values()) {
        if (type.equals(name)) {
          return type;
        }
      }
      return null;
    }
  }

  /** The type of z-range. */
  private final Type _type;

  /** The domain of the z-range. */
  private final ZDomain _domain;

  /**
   * The base constructor.
   * 
   * @param type the type of z-range.
   * @param domain the domain of the z-range.
   */
  public ZRange(final Type type, final ZDomain domain) {
    if (type == null) {
      throw new IllegalArgumentException("Invalid z-range type: " + type);
    }
    if (domain == null) {
      throw new IllegalArgumentException("Invalid z-range domain: " + domain);
    }
    _type = type;
    _domain = domain;
  }

  /**
   * Returns the type of z-range.
   * 
   * @return the z-range type.
   */
  public Type getType() {
    return _type;
  }

  /**
   * Returns the domain of the z-range.
   * 
   * @return the z-range domain.
   */
  public ZDomain getDomain() {
    return _domain;
  }

  /**
   * Returns an array containing the z-range (start and end) for the given x,y coordinate.
   * <p>
   * If no z-range exists at the coordinate, then an array of length 0 is returned.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the array containing the z-range (start and end, respectively).
   */
  public abstract float[] getZStartAndEnd(double x, double y);

  /**
   * Transform the given x,y coordinates to row,column coordinates based on the given grid geometry.
   * 
   * @param geometry the grid geometry.
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the row,column coordinates.
   */
  protected final int[] transformXYToRowCol(final GridGeometry3d geometry, final double x, final double y) {
    double[] rowcol = geometry.transformXYToRowCol(x, y, true);
    int row = Math.round((float) rowcol[0]);
    int col = Math.round((float) rowcol[1]);
    return new int[] { row, col };
  }
}
