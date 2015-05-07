/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


/**
 * This class defines a range (start, end and delta) of float values.
 * <p>
 * This class is immutable and thus thread-safe.
 */
public final class FloatRange {

  /** The epsilon value to use for float comparisons. */
  protected static final float EPSILON = 0.01f;

  /** The starting value. */
  private final float _start;

  /** The ending value. */
  private final float _end;

  /** The delta value. */
  private final float _delta;

  public FloatRange(final float start, final float end, final float delta) {
    _start = start;
    _end = end;
    _delta = delta;
  }

  public FloatRange(final FloatRange range) {
    this(range.getStart(), range.getEnd(), range.getDelta());
  }

  /**
   * Returns the starting value.
   */
  public float getStart() {
    return _start;
  }

  /**
   * Returns the ending value.
   */
  public float getEnd() {
    return _end;
  }

  /**
   * Returns the delta value.
   */
  public float getDelta() {
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
  public float[] toArray() {
    float[] values = new float[getNumSteps()];
    for (int index = 0; index < getNumSteps(); index++) {
      values[index] = _start + index * _delta;
    }
    return values;
  }

  /**
   * Returns an index for a value
   * @param value
   * @return
   */
  public int getIndex(final float value) {
    int index = Math.round((value - _start) / _delta);
    if (index < 0 || index >= getNumSteps()) {
      throw new IllegalArgumentException("Invalid index: " + index);
    }
    return index;
  }

  public float getFractionalIndex(final float value) {
    return (value - _start) / _delta;
  }

  /**
   * Return the nearest value in the range to the provided value
   * @param f
   * @return
   */
  public float getNearest(final float v) {
    if (_delta >= 0) {
      if (v <= _start) {
        return _start;
      }
      if (v >= _end) {
        return _end;
      }
    } else {
      if (v >= _start) {
        return _start;
      }
      if (v <= _end) {
        return _end;
      }
    }
    // now we know the value is in range (so getValue won't throw an IllegalArg
    float fracIndex = getFractionalIndex(v);
    int index = Math.round(fracIndex);
    return getValue(index);
  }

  /**
   * Returns a value in the range.
   * @param index the index of the value to return.
   * @return the requested value.
   */
  public float getValue(final int index) {
    if (index < 0 || index >= getNumSteps()) {
      throw new IllegalArgumentException("Invalid index: " + index);
    }
    return _start + index * _delta;
  }

  public boolean contains(final float value) {
    // Check if the value falls on an exact increment.
    if (Math.abs((value - _start) % _delta) > EPSILON
        && Math.abs((value - _start) % _delta) < Math.abs(_delta) - EPSILON) {
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

  @Override
  public boolean equals(final Object object) {
    if (object != null || object instanceof FloatRange) {
      FloatRange other = (FloatRange) object;
      if (_start == other.getStart() && _end == other.getEnd() && _delta == other.getDelta()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Float.floatToIntBits(_start);
    result = 31 * result + Float.floatToIntBits(_end);
    result = 31 * result + Float.floatToIntBits(_delta);
    return result;
  }

  @Override
  public String toString() {
    return "[" + _start + "," + _end + "," + _delta + "]";
  }

  public FloatRange getSubRange(final int startIndex, final int endIndex) {
    return new FloatRange(getValue(startIndex), getValue(endIndex), _delta);
  }

}
