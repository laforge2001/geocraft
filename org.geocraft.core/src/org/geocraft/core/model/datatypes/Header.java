package org.geocraft.core.model.datatypes;


import java.io.Serializable;

import org.geocraft.core.model.datatypes.HeaderEntry.Format;


public class Header implements Serializable {

  /** The header definition containing the header entries. */
  private final HeaderDefinition _headerDef;

  /** The array of string values. */
  private final String[] _stringValues;

  /** The array of byte values. */
  private final byte[] _byteValues;

  /** The array of short values. */
  private final short[] _shortValues;

  /** The array of integer values. */
  private final int[] _intValues;

  /** The array of long values. */
  private final long[] _longValues;

  /** The array of float values. */
  private final float[] _floatValues;

  /** The array of double values. */
  private final double[] _doubleValues;

  /**
   * Constructs a header with the given definition.
   * <p>
   * The definition will be stored and a byte buffer of sufficient size will be
   * allocated to store the actual header values.
   * 
   * @param headerDef the header definition.
   */
  public Header(final HeaderDefinition headerDef) {
    _headerDef = headerDef;
    _stringValues = new String[headerDef.getStringArraySize()];
    _byteValues = new byte[headerDef.getByteArraySize()];
    _shortValues = new short[headerDef.getShortArraySize()];
    _intValues = new int[headerDef.getIntegerArraySize()];
    _longValues = new long[headerDef.getLongArraySize()];
    _floatValues = new float[headerDef.getFloatArraySize()];
    _doubleValues = new double[headerDef.getDoubleArraySize()];
  }

  public Header(final Header header) {
    this(header.getHeaderDefinition());
    copyFrom(header);
  }

  public void copyFrom(final Header header) {
    for (HeaderEntry entry : header.getHeaderDefinition().getEntries()) {
      switch (entry.getFormat()) {
        case STRING:
          putStrings(entry, header.getStrings(entry));
          break;
        case BYTE:
          putBytes(entry, header.getBytes(entry));
          break;
        case SHORT:
          putShorts(entry, header.getShorts(entry));
          break;
        case INTEGER:
          putIntegers(entry, header.getIntegers(entry));
          break;
        case LONG:
          putLongs(entry, header.getLongs(entry));
          break;
        case FLOAT:
          putFloats(entry, header.getFloats(entry));
          break;
        case DOUBLE:
          putDoubles(entry, header.getDoubles(entry));
          break;
      }
    }
  }

  public HeaderDefinition getHeaderDefinition() {
    return _headerDef;
  }

  public String getString(final HeaderEntry headerEntry) {
    int index = _headerDef.getStringIndex(headerEntry.getKey());
    return _stringValues[index];
  }

  public byte getByte(final HeaderEntry headerEntry) {
    int index = _headerDef.getByteIndex(headerEntry.getKey());
    return _byteValues[index];
  }

  public short getShort(final HeaderEntry headerEntry) {
    int index = _headerDef.getShortIndex(headerEntry.getKey());
    return _shortValues[index];
  }

  public int getInteger(final HeaderEntry headerEntry) {
    int index = _headerDef.getIntegerIndex(headerEntry.getKey());
    return _intValues[index];
  }

  public long getLong(final HeaderEntry headerEntry) {
    int index = _headerDef.getLongIndex(headerEntry.getKey());
    return _longValues[index];
  }

  public float getFloat(final HeaderEntry headerEntry) {
    int index = _headerDef.getFloatIndex(headerEntry.getKey());
    return _floatValues[index];
  }

  public double getDouble(final HeaderEntry headerEntry) {
    int index = _headerDef.getDoubleIndex(headerEntry.getKey());
    return _doubleValues[index];
  }

  public String getString(final String key) {
    int index = _headerDef.getStringIndex(key);
    return _stringValues[index];
  }

  public byte getByte(final String key) {
    int index = _headerDef.getByteIndex(key);
    return _byteValues[index];
  }

  public short getShort(final String key) {
    int index = _headerDef.getShortIndex(key);
    return _shortValues[index];
  }

  public int getInteger(final String key) {
    int index = _headerDef.getIntegerIndex(key);
    return _intValues[index];
  }

  public long getLong(final String key) {
    int index = _headerDef.getLongIndex(key);
    return _longValues[index];
  }

  public float getFloat(final String key) {
    int index = _headerDef.getFloatIndex(key);
    return _floatValues[index];
  }

  public double getDouble(final String key) {
    int index = _headerDef.getDoubleIndex(key);
    return _doubleValues[index];
  }

  public void putString(final HeaderEntry headerEntry, final String value) {
    int index = _headerDef.getStringIndex(headerEntry.getKey());
    _stringValues[index] = value;
  }

  public void putByte(final HeaderEntry headerEntry, final byte value) {
    int index = _headerDef.getByteIndex(headerEntry.getKey());
    _byteValues[index] = value;
  }

  public void putShort(final HeaderEntry headerEntry, final short value) {
    int index = _headerDef.getShortIndex(headerEntry.getKey());
    _shortValues[index] = value;
  }

  public void putInteger(final HeaderEntry headerEntry, final int value) {
    int index = _headerDef.getIntegerIndex(headerEntry.getKey());
    _intValues[index] = value;
  }

  public void putLong(final HeaderEntry headerEntry, final long value) {
    int index = _headerDef.getLongIndex(headerEntry.getKey());
    _longValues[index] = value;
  }

  public void putFloat(final HeaderEntry headerEntry, final float value) {
    int index = _headerDef.getFloatIndex(headerEntry.getKey());
    _floatValues[index] = value;
  }

  public void putDouble(final HeaderEntry headerEntry, final double value) {
    int index = _headerDef.getDoubleIndex(headerEntry.getKey());
    _doubleValues[index] = value;
  }

  public void putString(final String key, final String value) {
    int index = _headerDef.getStringIndex(key);
    _stringValues[index] = value;
  }

  public void putByte(final String key, final byte value) {
    int index = _headerDef.getByteIndex(key);
    _byteValues[index] = value;
  }

  public void putShort(final String key, final short value) {
    int index = _headerDef.getShortIndex(key);
    _shortValues[index] = value;
  }

  public void putInteger(final String key, final int value) {
    int index = _headerDef.getIntegerIndex(key);
    _intValues[index] = value;
  }

  public void putLong(final String key, final long value) {
    int index = _headerDef.getLongIndex(key);
    _longValues[index] = value;
  }

  public void putFloat(final String key, final float value) {
    int index = _headerDef.getFloatIndex(key);
    _floatValues[index] = value;
  }

  public void putDouble(final String key, final double value) {
    int index = _headerDef.getDoubleIndex(key);
    _doubleValues[index] = value;
  }

  public Object getValue(final String key) {
    HeaderEntry headerEntry = _headerDef.getHeaderEntry(key);
    Format format = headerEntry.getFormat();
    switch (format) {
      case STRING:
        return "" + getString(key);
      case BYTE:
        return "" + getByte(key);
      case SHORT:
        return "" + getShort(key);
      case INTEGER:
        return "" + getInteger(key);
      case LONG:
        return "" + getLong(key);
      case FLOAT:
        return "" + getFloat(key);
      case DOUBLE:
        return "" + getDouble(key);
    }
    throw new IllegalArgumentException("Invalid header entry format: " + format);
  }

  public String[] getStrings(final HeaderEntry headerEntry) {
    int index = _headerDef.getStringIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    String[] values = new String[numElements];
    for (int i = 0; i < numElements; i++) {
      values[i] = _stringValues[index + i];
    }
    return values;
  }

  public void putStrings(final HeaderEntry headerEntry, final String... values) {
    int index = _headerDef.getStringIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    for (int i = 0; i < numElements; i++) {
      _stringValues[index + i] = values[i];
    }
  }

  public byte[] getBytes(final HeaderEntry headerEntry) {
    int index = _headerDef.getByteIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    byte[] values = new byte[numElements];
    for (int i = 0; i < numElements; i++) {
      values[i] = _byteValues[index + i];
    }
    return values;
  }

  public void putBytes(final HeaderEntry headerEntry, final byte... values) {
    int index = _headerDef.getByteIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    for (int i = 0; i < numElements; i++) {
      _byteValues[index + i] = values[i];
    }
  }

  public short[] getShorts(final HeaderEntry headerEntry) {
    int index = _headerDef.getShortIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    short[] values = new short[numElements];
    for (int i = 0; i < numElements; i++) {
      values[i] = _shortValues[index + i];
    }
    return values;
  }

  public void putShorts(final HeaderEntry headerEntry, final short... values) {
    int index = _headerDef.getShortIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    for (int i = 0; i < numElements; i++) {
      _shortValues[index + i] = values[i];
    }
  }

  public int[] getIntegers(final HeaderEntry headerEntry) {
    int index = _headerDef.getIntegerIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    int[] values = new int[numElements];
    for (int i = 0; i < numElements; i++) {
      values[i] = _intValues[index + i];
    }
    return values;
  }

  public void putIntegers(final HeaderEntry headerEntry, final int... values) {
    int index = _headerDef.getIntegerIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    for (int i = 0; i < numElements; i++) {
      _intValues[index + i] = values[i];
    }
  }

  public long[] getLongs(final HeaderEntry headerEntry) {
    int index = _headerDef.getLongIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    long[] values = new long[numElements];
    for (int i = 0; i < numElements; i++) {
      values[i] = _longValues[index + i];
    }
    return values;
  }

  public void putLongs(final HeaderEntry headerEntry, final long... values) {
    int index = _headerDef.getLongIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    for (int i = 0; i < numElements; i++) {
      _longValues[index + i] = values[i];
    }
  }

  public float[] getFloats(final HeaderEntry headerEntry) {
    int index = _headerDef.getFloatIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    float[] values = new float[numElements];
    for (int i = 0; i < numElements; i++) {
      values[i] = _floatValues[index + i];
    }
    return values;
  }

  public void putFloats(final HeaderEntry headerEntry, final float... values) {
    int index = _headerDef.getFloatIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    for (int i = 0; i < numElements; i++) {
      _floatValues[index + i] = values[i];
    }
  }

  public double[] getDoubles(final HeaderEntry headerEntry) {
    int index = _headerDef.getDoubleIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    double[] values = new double[numElements];
    for (int i = 0; i < numElements; i++) {
      values[i] = _doubleValues[index + i];
    }
    return values;
  }

  public void putDoubles(final HeaderEntry headerEntry, final double... values) {
    int index = _headerDef.getDoubleIndex(headerEntry.getKey());
    int numElements = headerEntry.getNumElements();
    for (int i = 0; i < numElements; i++) {
      _doubleValues[index + i] = values[i];
    }
  }

  public Object getValueObject(final HeaderEntry headerEntry) {
    switch (headerEntry.getFormat()) {
      case STRING:
        return "" + getString(headerEntry);
      case BYTE:
        return "" + getByte(headerEntry);
      case SHORT:
        return "" + getShort(headerEntry);
      case INTEGER:
        return "" + getInteger(headerEntry);
      case LONG:
        return "" + getLong(headerEntry);
      case FLOAT:
        return "" + getFloat(headerEntry);
      case DOUBLE:
        return "" + getDouble(headerEntry);
    }
    return "";
  }

  public static void main(final String[] args) {
    tester();
  }

  public static void tester() {
    long startTime = System.currentTimeMillis();
    int numInlines = 847;
    int numXlines = 1200;
    HeaderEntry[] headerEntries = new HeaderEntry[4];
    headerEntries[0] = TraceHeaderCatalog.INLINE_NO;
    headerEntries[1] = TraceHeaderCatalog.XLINE_NO;
    headerEntries[2] = TraceHeaderCatalog.X;
    headerEntries[3] = TraceHeaderCatalog.Y;
    // HeaderEntry complex = new HeaderEntry("COMPLEX", "Complex", "complex",
    // Format.DOUBLE, 2);
    // headerEntries[4] = complex;
    HeaderDefinition headerDef = new HeaderDefinition(headerEntries);
    for (int i = 0; i < numInlines; i++) {
      for (int j = 0; j < numXlines; j++) {
        Header header = new Header(headerDef);
        header.putInteger(TraceHeaderCatalog.INLINE_NO, (i + 1));
        header.putInteger(TraceHeaderCatalog.XLINE_NO, (j + 1));
        header.putDouble(TraceHeaderCatalog.X, Math.random());
        header.putDouble(TraceHeaderCatalog.Y, Math.random());
        // header.putDoubles(complex, 1.5, -4.7);
        int inline = header.getInteger(TraceHeaderCatalog.INLINE_NO);
        int xline = header.getInteger(TraceHeaderCatalog.XLINE_NO);
        double x = header.getDouble(TraceHeaderCatalog.X);
        double y = header.getDouble(TraceHeaderCatalog.Y);
        // double[] cmplx = header.getDoubles(complex);
      }
    }
    long endTime = System.currentTimeMillis();
    int numHeaders = numInlines * numXlines;
    System.out.println("ELAPSED TIME: " + (endTime - startTime) + " " + numHeaders);
  }
}
