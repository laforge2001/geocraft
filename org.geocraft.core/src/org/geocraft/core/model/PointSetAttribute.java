package org.geocraft.core.model;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class PointSetAttribute {

  public enum Type {
    SHORT,
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    TIMESTAMP;
  }

  private final Type _type;

  private final String _name;

  private final PointSet _pointSet;

  private final List<String> _values;

  /**
   * Restricted to limit construction via the <code>addAttribute</code> method in <code>PointSet</code>.
   *
   * @param type the pointset attribute type.
   * @param name the pointset attribute name.
   * @param pointSet the parent pointset.
   */
  PointSetAttribute(final Type type, final String name, final PointSet pointSet) {
    _type = type;
    _name = name;
    _pointSet = pointSet;
    _values = new ArrayList<String>();
  }

  public Type getType() {
    return _type;
  }

  public String getName() {
    return _name;
  }

  public PointSet getPointSet() {
    return _pointSet;
  }

  public synchronized int getNumValues() {
    return _pointSet.getNumPoints();
  }

  public synchronized short[] getShorts() {
    short[] results = new short[getNumValues()];
    for (int i = 0; i < results.length; i++) {
      results[i] = getShort(i);
    }
    return results;
  }

  public synchronized int[] getInts() {
    int[] results = new int[getNumValues()];
    for (int i = 0; i < results.length; i++) {
      results[i] = getInt(i);
    }
    return results;
  }

  public synchronized long[] getLongs() {
    long[] results = new long[getNumValues()];
    for (int i = 0; i < results.length; i++) {
      results[i] = getLong(i);
    }
    return results;
  }

  public synchronized float[] getFloats() {
    float[] results = new float[getNumValues()];
    for (int i = 0; i < results.length; i++) {
      results[i] = getFloat(i);
    }
    return results;
  }

  public synchronized double[] getDoubles() {
    double[] results = new double[getNumValues()];
    for (int i = 0; i < results.length; i++) {
      results[i] = getFloat(i);
    }
    return results;
  }

  public synchronized String[] getStrings() {
    String[] results = new String[getNumValues()];
    for (int i = 0; i < results.length; i++) {
      results[i] = getString(i);
    }
    return results;
  }

  public synchronized Timestamp[] getTimestamps() {
    Timestamp[] results = new Timestamp[getNumValues()];
    for (int i = 0; i < results.length; i++) {
      results[i] = getTimestamp(i);
    }
    return results;
  }

  public synchronized void setShorts(final short[] values) {
    for (int i = 0; i < values.length; i++) {
      setShort(i, values[i]);
    }
  }

  public synchronized void setInts(final int[] values) {
    for (int i = 0; i < values.length; i++) {
      setInt(i, values[i]);
    }
  }

  public synchronized void setLongs(final long[] values) {
    for (int i = 0; i < values.length; i++) {
      setLong(i, values[i]);
    }
  }

  public synchronized void setFloats(final float[] values) {
    for (int i = 0; i < values.length; i++) {
      setFloat(i, values[i]);
    }
  }

  public synchronized void setDoubles(final double[] values) {
    for (int i = 0; i < values.length; i++) {
      setDouble(i, values[i]);
    }
  }

  public synchronized void setStrings(final String[] values) {
    for (int i = 0; i < values.length; i++) {
      setString(i, values[i]);
    }
  }

  public synchronized void setTimestamps(final Timestamp[] values) {
    for (int i = 0; i < values.length; i++) {
      setTimestamp(i, values[i]);
    }
  }

  public synchronized short getShort(final int index) {
    try {
      return Short.parseShort(_values.get(index));
    } catch (Exception ex) {
      return Short.MIN_VALUE;
    }
  }

  public synchronized int getInt(final int index) {
    String sval = _values.get(index);
    try {
      return Integer.parseInt(sval);
    } catch (Exception ex) {
      return Integer.MIN_VALUE;
    }
  }

  public synchronized long getLong(final int index) {
    String sval = _values.get(index);
    try {
      return Long.parseLong(sval);
    } catch (Exception ex) {
      return Long.MIN_VALUE;
    }
  }

  public synchronized float getFloat(final int index) {
    String sval = _values.get(index);
    try {
      return Float.parseFloat(sval);
    } catch (Exception ex) {
      return Float.NaN;
    }
  }

  public synchronized double getDouble(final int index) {
    String sval = _values.get(index);
    try {
      return Double.parseDouble(sval);
    } catch (Exception ex) {
      return Float.NaN;
    }
  }

  public synchronized String getString(final int index) {
    String sval = _values.get(index);
    try {
      return sval;
    } catch (Exception ex) {
      return "";
    }
  }

  public synchronized Timestamp getTimestamp(final int index) {
    String sval = _values.get(index);
    try {
      return Timestamp.valueOf(sval);
    } catch (Exception ex) {
      return new Timestamp(0);
    }
  }

  public synchronized void setShort(final int index, final short value) {
    _values.set(index, Short.toString(value));
  }

  public synchronized void setInt(final int index, final int value) {
    _values.set(index, Integer.toString(value));
  }

  public synchronized void setLong(final int index, final long value) {
    _values.set(index, Long.toString(value));
  }

  public synchronized void setFloat(final int index, final float value) {
    _values.set(index, Float.toString(value));
  }

  public synchronized void setDouble(final int index, final double value) {
    _values.set(index, Double.toString(value));
  }

  public synchronized void setString(final int index, final String value) {
    _values.set(index, value);
  }

  public synchronized void setTimestamp(final int index, final Timestamp value) {
    _values.set(index, value.toString());
  }

  public synchronized void removePoint(final int index) {
    _values.remove(index);
  }

  public synchronized void addPoint() {
    _values.add("");
  }
}
