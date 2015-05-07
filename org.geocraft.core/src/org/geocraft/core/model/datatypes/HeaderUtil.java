package org.geocraft.core.model.datatypes;




public class HeaderUtil {

  //  /**
  //   * Gets a string value from the the specified header element.
  //   * 
  //   * @param headerElementId the header element.
  //   * @return the string value.
  //   */
  //  public static String getString(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.String) : "Header element " + headerElement.getID() + " is not of string format.";
  //    return getString(header, header.getDefinition().getByteOffset(headerElement.getID()), headerElement.getCount());
  //  }
  //
  //  /**
  //   * Gets a short value from the the specified header element.
  //   * 
  //   * @param headerElementId the header element.
  //   * @return the short value.
  //   */
  //  public static short getShort(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Short) : "Header element " + headerElement.getID() + " is not of short format.";
  //    return getShort(header, header.getDefinition().getByteOffset(headerElement.getID()));
  //  }
  //
  //  /**
  //   * Gets an integer value from the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the integer value.
  //   */
  //  public static int getInteger(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Integer) : "Header element " + headerElement.getID() + " is not of integer format.";
  //    return getInteger(header, header.getDefinition().getByteOffset(headerElement.getID()));
  //  }
  //
  //  /**
  //   * Gets a long value from the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the long value.
  //   */
  //  public static long getLong(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Long) : "Header element " + headerElement.getID() + " is not of long format.";
  //    return getLong(header, header.getDefinition().getByteOffset(headerElement.getID()));
  //  }
  //
  //  /**
  //   * Gets a float value from the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the float value.
  //   */
  //  public static float getFloat(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Float) : "Header element " + headerElement.getID() + " is not of float format.";
  //    return getFloat(header, header.getDefinition().getByteOffset(headerElement.getID()));
  //  }
  //
  //  /**
  //   * Gets a double value from the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the double value.
  //   */
  //  public static double getDouble(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Double) : "Header element " + headerElement.getID() + " is not of double format.";
  //    return getDouble(header, header.getDefinition().getByteOffset(headerElement.getID()));
  //  }
  //
  //  /**
  //   * Gets a string value from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param numChars the number of characters in the string.
  //   * @return the string value.
  //   */
  //  public static String getString(final Header header, final int byteOffset, final int numChars) {
  //    if (byteOffset < 0) {
  //      return "";
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    char[] cs = new char[numChars];
  //    for (int i = 0; i < numChars; i++) {
  //      cs[i] = buffer.getChar();
  //    }
  //    return String.valueOf(cs);
  //  }
  //
  //  /**
  //   * Gets a short value from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the short value.
  //   */
  //  public static short getShort(final Header header, final int byteOffset) {
  //    if (byteOffset < 0) {
  //      return 0;
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    return buffer.getShort();
  //  }
  //
  //  /**
  //   * Gets an integer value from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the integer value.
  //   */
  //  public static int getInteger(final Header header, final int byteOffset) {
  //    if (byteOffset < 0) {
  //      return 0;
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    return buffer.getInt();
  //  }
  //
  //  /**
  //   * Gets a long value from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the long value.
  //   */
  //  public static long getLong(final Header header, final int byteOffset) {
  //    if (byteOffset < 0) {
  //      return 0;
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    return buffer.getLong();
  //  }
  //
  //  /**
  //   * Gets a float value from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the float value.
  //   */
  //  public static float getFloat(final Header header, final int byteOffset) {
  //    if (byteOffset < 0) {
  //      return 0;
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    return buffer.getFloat();
  //  }
  //
  //  /**
  //   * Gets a double value from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the double value.
  //   */
  //  public static double getDouble(final Header header, final int byteOffset) {
  //    if (byteOffset < 0) {
  //      return 0;
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    return buffer.getDouble();
  //  }
  //
  //  /**
  //   * Puts a string value to the the specified header element.
  //   * 
  //   * @param headerElementId the header element.
  //   * @return the string value.
  //   */
  //  public static void putString(final Header header, final HeaderElement headerElement, final String value) {
  //    assert headerElement.getFormat().equals(Format.String) : "Header element " + headerElement.getID() + " is not of string format.";
  //    putString(header, header.getDefinition().getByteOffset(headerElement.getID()), value);
  //  }
  //
  //  /**
  //   * Puts a short value to the the specified header element.
  //   * 
  //   * @param headerElementId the header element.
  //   * @return the short value.
  //   */
  //  public static void putShort(final Header header, final HeaderElement headerElement, final short value) {
  //    assert headerElement.getFormat().equals(Format.Short) : "Header element " + headerElement.getID() + " is not of short format.";
  //    putShort(header, header.getDefinition().getByteOffset(headerElement.getID()), value);
  //  }
  //
  //  /**
  //   * Puts an integer value to the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the integer value.
  //   */
  //  public static void putInteger(final Header header, final HeaderElement headerElement, final int value) {
  //    assert headerElement.getFormat().equals(Format.Integer) : "Header element " + headerElement.getID() + " is not of integer format.";
  //    putInteger(header, header.getDefinition().getByteOffset(headerElement.getID()), value);
  //  }
  //
  //  /**
  //   * Puts a long value to the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the long value.
  //   */
  //  public static void putLong(final Header header, final HeaderElement headerElement, final long value) {
  //    assert headerElement.getFormat().equals(Format.Long) : "Header element " + headerElement.getID() + " is not of long format.";
  //    putLong(header, header.getDefinition().getByteOffset(headerElement.getID()), value);
  //  }
  //
  //  /**
  //   * Puts a float value to the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the float value.
  //   */
  //  public static void putFloat(final Header header, final HeaderElement headerElement, final float value) {
  //    assert headerElement.getFormat().equals(Format.Float) : "Header element " + headerElement.getID() + " is not of float format.";
  //    putFloat(header, header.getDefinition().getByteOffset(headerElement.getID()), value);
  //  }
  //
  //  /**
  //   * Puts a double value to the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the double value.
  //   */
  //  public static void putDouble(final Header header, final HeaderElement headerElement, final double value) {
  //    assert headerElement.getFormat().equals(Format.Double) : "Header element " + headerElement.getID() + " is not of double format.";
  //    putDouble(header, header.getDefinition().getByteOffset(headerElement.getID()), value);
  //  }
  //
  //  /**
  //   * Puts a string value into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param value the string value.
  //   */
  //  public static void putString(final Header header, final int byteOffset, final String value) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    int numChars = value.length();
  //    for (int i = 0; i < numChars; i++) {
  //      char c = value.charAt(i);
  //      buffer.putChar(c);
  //    }
  //  }
  //
  //  /**
  //   * Puts a short value into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param value the short value.
  //   */
  //  public static void putShort(final Header header, final int byteOffset, final short value) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    buffer.putShort(value);
  //  }
  //
  //  /**
  //   * Puts an integer value into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param value the integer value.
  //   */
  //  public static void putInteger(final Header header, final int byteOffset, final int value) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    buffer.putInt(value);
  //  }
  //
  //  /**
  //   * Puts a long value into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param value the long value.
  //   */
  //  public static void putLong(final Header header, final int byteOffset, final long value) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    buffer.putLong(value);
  //  }
  //
  //  /**
  //   * Puts a float value into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param value the float value.
  //   */
  //  public static void putFloat(final Header header, final int byteOffset, final float value) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    buffer.putFloat(value);
  //  }
  //
  //  /**
  //   * Puts a double value into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param value the double value.
  //   */
  //  public static void putDouble(final Header header, final int byteOffset, final double value) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    buffer.putDouble(value);
  //  }
  //
  //  /**
  //   * Gets a short values from the the specified header element.
  //   * 
  //   * @param headerElementId the header element.
  //   * @return the short values.
  //   */
  //  public static short[] getShorts(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Short) : "Header element " + headerElement.getID() + " is not of short format.";
  //    return getShorts(header, header.getDefinition().getByteOffset(headerElement.getID()), headerElement.getCount());
  //  }
  //
  //  /**
  //   * Gets an integer values from the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the integer values.
  //   */
  //  public static int[] getIntegers(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Integer) : "Header element " + headerElement.getID() + " is not of integer format.";
  //    return getIntegers(header, header.getDefinition().getByteOffset(headerElement.getID()), headerElement.getCount());
  //  }
  //
  //  /**
  //   * Gets a long values from the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the long values.
  //   */
  //  public static long[] getLongs(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Long) : "Header element " + headerElement.getID() + " is not of long format.";
  //    return getLongs(header, header.getDefinition().getByteOffset(headerElement.getID()), headerElement.getCount());
  //  }
  //
  //  /**
  //   * Gets a float values from the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the float values.
  //   */
  //  public static float[] getFloats(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Float) : "Header element " + headerElement.getID() + " is not of float format.";
  //    return getFloats(header, header.getDefinition().getByteOffset(headerElement.getID()), headerElement.getCount());
  //  }
  //
  //  /**
  //   * Gets a double values from the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the double values.
  //   */
  //  public static double[] getDoubles(final Header header, final HeaderElement headerElement) {
  //    assert headerElement.getFormat().equals(Format.Double) : "Header element " + headerElement.getID() + " is not of double format.";
  //    return getDoubles(header, header.getDefinition().getByteOffset(headerElement.getID()), headerElement.getCount());
  //  }
  //
  //  /**
  //   * Gets a short values from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the short values.
  //   */
  //  public static short[] getShorts(final Header header, final int byteOffset, final int count) {
  //    if (byteOffset < 0) {
  //      return new short[0];
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    short[] values = new short[count];
  //    for (int i = 0; i < count; i++) {
  //      values[i] = buffer.getShort();
  //    }
  //    return values;
  //  }
  //
  //  /**
  //   * Gets an integer values from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the integer values.
  //   */
  //  public static int[] getIntegers(final Header header, final int byteOffset, final int count) {
  //    if (byteOffset < 0) {
  //      return new int[0];
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    int[] values = new int[count];
  //    for (int i = 0; i < count; i++) {
  //      values[i] = buffer.getInt();
  //    }
  //    return values;
  //  }
  //
  //  /**
  //   * Gets a long values from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the long values.
  //   */
  //  public static long[] getLongs(final Header header, final int byteOffset, final int count) {
  //    if (byteOffset < 0) {
  //      return new long[0];
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    long[] values = new long[count];
  //    for (int i = 0; i < count; i++) {
  //      values[i] = buffer.getLong();
  //    }
  //    return values;
  //  }
  //
  //  /**
  //   * Gets a float values from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the float values.
  //   */
  //  public static float[] getFloats(final Header header, final int byteOffset, final int count) {
  //    if (byteOffset < 0) {
  //      return new float[0];
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    float[] values = new float[count];
  //    for (int i = 0; i < count; i++) {
  //      values[i] = buffer.getFloat();
  //    }
  //    return values;
  //  }
  //
  //  /**
  //   * Gets a double values from the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @return the double values.
  //   */
  //  public static double[] getDoubles(final Header header, final int byteOffset, final int count) {
  //    if (byteOffset < 0) {
  //      return new double[0];
  //    }
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    double[] values = new double[count];
  //    for (int i = 0; i < count; i++) {
  //      values[i] = buffer.getDouble();
  //    }
  //    return values;
  //  }
  //
  //  /**
  //   * Puts short values to the the specified header element.
  //   * 
  //   * @param headerElementId the header element.
  //   * @param values the short values.
  //   */
  //  public static void putShorts(final Header header, final HeaderElement headerElement, final short[] values) {
  //    assert headerElement.getFormat().equals(Format.Short) : "Header element " + headerElement.getID() + " is not of short format.";
  //    putShorts(header, header.getDefinition().getByteOffset(headerElement.getID()), values);
  //  }
  //
  //  /**
  //   * Puts integer values to the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @param values the integer values.
  //   */
  //  public static void putIntegers(final Header header, final HeaderElement headerElement, final int[] values) {
  //    assert headerElement.getFormat().equals(Format.Integer) : "Header element " + headerElement.getID() + " is not of integer format.";
  //    putIntegers(header, header.getDefinition().getByteOffset(headerElement.getID()), values);
  //  }
  //
  //  /**
  //   * Puts long values to the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @param values the long values.
  //   */
  //  public static void putLongs(final Header header, final HeaderElement headerElement, final long[] values) {
  //    assert headerElement.getFormat().equals(Format.Long) : "Header element " + headerElement.getID() + " is not of long format.";
  //    putLongs(header, header.getDefinition().getByteOffset(headerElement.getID()), values);
  //  }
  //
  //  /**
  //   * Puts float values to the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @param values the float values.
  //   */
  //  public static void putFloats(final Header header, final HeaderElement headerElement, final float[] values) {
  //    assert headerElement.getFormat().equals(Format.Float) : "Header element " + headerElement.getID() + " is not of float format.";
  //    putFloats(header, header.getDefinition().getByteOffset(headerElement.getID()), values);
  //  }
  //
  //  /**
  //   * Puts double values to the the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @param values the double values.
  //   */
  //  public static void putDoubles(final Header header, final HeaderElement headerElement, final double[] values) {
  //    assert headerElement.getFormat().equals(Format.Double) : "Header element " + headerElement.getID() + " is not of double format.";
  //    putDoubles(header, header.getDefinition().getByteOffset(headerElement.getID()), values);
  //  }
  //
  //  /**
  //   * Puts short valu into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param values the short values.
  //   */
  //  public static void putShorts(final Header header, final int byteOffset, final short[] values) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    for (short value : values) {
  //      buffer.putShort(value);
  //    }
  //  }
  //
  //  /**
  //   * Puts integer values into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param values the integer values.
  //   */
  //  public static void putIntegers(final Header header, final int byteOffset, final int[] values) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    for (int value : values) {
  //      buffer.putInt(value);
  //    }
  //  }
  //
  //  /**
  //   * Puts long values into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param values the long values.
  //   */
  //  public static void putLongs(final Header header, final int byteOffset, final long[] values) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    for (long value : values) {
  //      buffer.putLong(value);
  //    }
  //  }
  //
  //  /**
  //   * Puts float values into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param values the float values.
  //   */
  //  public static void putFloats(final Header header, final int byteOffset, final float[] values) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    for (float value : values) {
  //      buffer.putFloat(value);
  //    }
  //  }
  //
  //  /**
  //   * Puts double values into the header at the specified location.
  //   * 
  //   * @param byteOffset the byte offset location.
  //   * @param values the double values.
  //   */
  //  public static void putDoubles(final Header header, final int byteOffset, final double[] values) {
  //    ByteBuffer buffer = header.getBuffer();
  //    buffer.position(byteOffset);
  //    for (double value : values) {
  //      buffer.putDouble(value);
  //    }
  //  }
  //
  //  /**
  //   * Gets a value from the specified header element.
  //   * 
  //   * @param headerElement the header element.
  //   * @return the value (as an object).
  //   */
  //  public static String getValueObject(final Header header, final HeaderElement headerElement) {
  //    Format format = headerElement.getFormat();
  //    if (format.equals(Format.Byte)) {
  //      throw new UnsupportedOperationException("Byte format not yet supported.");
  //    } else if (format.equals(Format.Short)) {
  //      return "" + getShort(header, headerElement);
  //    } else if (format.equals(Format.Integer)) {
  //      return "" + getInteger(header, headerElement);
  //    } else if (format.equals(Format.Long)) {
  //      return "" + getLong(header, headerElement);
  //    } else if (format.equals(Format.Float)) {
  //      return "" + getFloat(header, headerElement);
  //    } else if (format.equals(Format.Double)) {
  //      return "" + getDouble(header, headerElement);
  //    } else if (format.equals(Format.ComplexFloat)) {
  //      throw new UnsupportedOperationException("ComplexFloat format not yet supported.");
  //    } else if (format.equals(Format.ComplexDouble)) {
  //      throw new UnsupportedOperationException("ComplexDouble format not yet supported.");
  //    } else if (format.equals(Format.String)) {
  //      return getString(header, headerElement);
  //    }
  //    return "";
  //  }
}
