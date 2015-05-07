/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


/**
 * This class defines a range (start, end and delta) of integer values.
 * <p>
 * This class is immutable and thus thread-safe.
 */
public final class IntRange {

  /** The starting value. */
  private final int _start;

  /** The ending value. */
  private final int _end;

  /** The delta value. */
  private final int _delta;

  public IntRange(final int start, final int end, final int delta) {
    _start = start;
    _end = end;
    _delta = delta;
  }

  public IntRange(final IntRange range) {
    this(range.getStart(), range.getEnd(), range.getDelta());
  }

  /**
   * Returns the starting value.
   */
  public int getStart() {
    return _start;
  }

  /**
   * Returns the ending value.
   */
  public int getEnd() {
    return _end;
  }

  /**
   * Returns the delta value.
   */
  public int getDelta() {
    return _delta;
  }

  /**
   * Returns the number of steps within the range.
   * (e.g. if start=10,end=20,delta=2; then the # of steps=6...10,12,14,16,18,20)
   */
  public int getNumSteps() {
    return 1 + Math.round((_end - _start) / _delta);
  }

  /**
   * Creates an array of the values in the range.
   * @return an array of the values in the range.
   */
  public int[] toArray() {
    int[] values = new int[getNumSteps()];
    for (int index = 0; index < getNumSteps(); index++) {
      values[index] = _start + index * _delta;
    }
    return values;
  }

  public boolean contains(final float value) {
    // Check if the value falls on an exact increment.
    if (Math.abs((value - _start) % _delta) != 0) {
      return false;
    }

    // Check if the value is within the start/end bounds.
    if (_start < _end && (value < _start || value > _end)) {
      return false;
    }
    if (_start > _end && (value > _start || value < _end)) {
      return false;
    }

    // Passed all tests, so return true.
    return true;
  }

  /**
   * Returns a value in the range.
   * @param index the index of the value to return.
   * @return the requested value.
   */
  public int getValue(final int index) {
    if (index < 0 || index >= getNumSteps()) {
      throw new IllegalArgumentException("Invalid index: " + index);
    }
    return _start + index * _delta;
  }

  @Override
  public boolean equals(final Object object) {
    if (object != null || object instanceof IntRange) {
      IntRange other = (IntRange) object;
      if (_start == other.getStart() && _end == other.getEnd() && _delta == other.getDelta()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + _start;
    result = 31 * result + _end;
    result = 31 * result + _delta;
    return result;
  }

  @Override
  public String toString() {
    return "[" + _start + "," + _end + "," + _delta + "]";
  }
}
