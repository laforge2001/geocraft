/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


/**
 * Inspired by the code in Effective Java by Bloch.
 * 
 * You must not add any fields into the hashcode that are 
 * not tested in your overridden equals() method. 
 */
public class HashCode {

  /** Non zero initial hash. */
  private int _hashCode = 17;

  public int getHashCode() {
    return _hashCode;
  }

  public void add(boolean value) {
    _hashCode = 37 * _hashCode + (value ? 0 : 1);
  }

  public void add(byte value) {
    _hashCode = 37 * _hashCode + value;
  }

  public void add(char value) {
    _hashCode = 37 * _hashCode + value;
  }

  public void add(short value) {
    _hashCode = 37 * _hashCode + value;
  }

  public void add(int value) {
    _hashCode = 37 * _hashCode + value;
  }

  public void add(long value) {
    _hashCode = 37 * _hashCode + (int) (value ^ value >>> 32);
  }

  public void add(float value) {
    add(Float.floatToIntBits(value));
  }

  public void add(double value) {
    add(Double.doubleToLongBits(value));
  }

  public void add(Object value) {
    if (value != null) {
      _hashCode = 37 * _hashCode + value.hashCode();
    }
  }

  public void add(boolean[] values) {
    for (boolean val : values) {
      add(val);
    }
  }

  public void add(byte[] values) {
    for (byte val : values) {
      add(val);
    }
  }

  public void add(char[] values) {
    for (char val : values) {
      add(val);
    }
  }

  public void add(short[] values) {
    for (short val : values) {
      add(val);
    }
  }

  public void add(int[] values) {
    for (int val : values) {
      add(val);
    }
  }

  public void add(long[] values) {
    for (long val : values) {
      add(val);
    }
  }

  public void add(float[] values) {
    for (float val : values) {
      add(val);
    }
  }

  public void add(double[] values) {
    for (double val : values) {
      add(val);
    }
  }

  public void add(Object[] values) {
    for (Object val : values) {
      add(val);
    }
  }

}
