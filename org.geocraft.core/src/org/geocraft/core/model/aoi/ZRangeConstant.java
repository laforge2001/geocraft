package org.geocraft.core.model.aoi;


import org.geocraft.core.model.datatypes.ZDomain;


/**
 * This class defines a processing z-range that is defined everywhere
 * and whose start and end values are both constants.
 */
public final class ZRangeConstant extends ZRange {

  /** The starting z value. */
  private final float _zStart;

  /** The ending z value. */
  private final float _zEnd;

  /**
   * Constructs a constant z-range.
   * 
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param zDomain the domain of the z-range.
   */
  public ZRangeConstant(final float zStart, final float zEnd, final ZDomain zDomain) {
    super(ZRange.Type.CONSTANT, zDomain);
    _zStart = zStart;
    _zEnd = zEnd;
  }

  @Override
  public float[] getZStartAndEnd(final double x, final double y) {
    // The x,y coordinates are irrelevant,
    // so simply return the starting and ending z values.
    return new float[] { _zStart, _zEnd };
  }

  public float getZStart() {
    return _zStart;
  }

  public float getZEnd() {
    return _zEnd;
  }

  @Override
  public String toString() {
    return "(" + _zStart + "," + _zEnd + ") " + getDomain().toString();
  }
}
