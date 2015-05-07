/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.axis;


/**
 * A range (start and end) of values.
 * The start value does not have to be be less than the end value.
 */
public class AxisRange {

  /** The starting value. */
  private final double _start;

  /** The ending value. */
  private final double _end;

  /**
   * The default constructor.
   * 
   * @param start the starting value.
   * @param end the ending value.
   */
  public AxisRange(final double start, final double end) {
    _start = start;
    _end = end;
  }

  /**
   * Gets the starting value.
   * 
   * @return the starting value.
   */
  public double getStart() {
    return _start;
  }

  /**
   * Gets the ending value.
   * 
   * @return the ending value.
   */
  public double getEnd() {
    return _end;
  }

  @Override
  public String toString() {
    return _start + ", " + _end;
  }
}
