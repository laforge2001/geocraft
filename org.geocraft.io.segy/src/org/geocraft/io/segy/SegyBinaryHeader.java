/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.nio.ByteBuffer;

import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderEntry;


/**
 * Defines the SegyBinaryHeader class.
 */
public class SegyBinaryHeader extends Header {

  public static final int SEGY_BINARY_HEADER_SIZE = 400;

  private ByteBuffer _buffer;

  /**
   * Constructs an instance of SegyBinaryHeader.
   */
  public SegyBinaryHeader() {
    super(new SegyBinaryHeaderDefinition());
    _buffer = ByteBuffer.allocate(SEGY_BINARY_HEADER_SIZE);
  }

  public ByteBuffer getBuffer() {
    return _buffer;
  }

  public void updateBufferFromHeader() {
    for (HeaderEntry headerEntry : getHeaderDefinition().getEntries()) {
      int byteOffset = SegyBinaryHeaderCatalog.getByteOffset(headerEntry);
      switch (headerEntry.getFormat()) {
        case SHORT:
          putToBufferAsShort(byteOffset, getShort(headerEntry));
          break;
        case INTEGER:
          putToBufferAsInt(byteOffset, getInteger(headerEntry));
          break;
        case FLOAT:
          putToBufferAsFloat(byteOffset, getFloat(headerEntry));
          break;
        case DOUBLE:
          putToBufferAsDouble(byteOffset, getDouble(headerEntry));
          break;
        default:
          throw new RuntimeException("Invalid entry for SEG-Y binary header: " + headerEntry);
      }
    }
  }

  public void updateHeaderFromBuffer() {
    for (HeaderEntry headerEntry : getHeaderDefinition().getEntries()) {
      int byteOffset = SegyBinaryHeaderCatalog.getByteOffset(headerEntry);
      switch (headerEntry.getFormat()) {
        case SHORT:
          putShort(headerEntry, getFromBufferAsShort(byteOffset));
          break;
        case INTEGER:
          putInteger(headerEntry, getFromBufferAsInt(byteOffset));
          break;
        case FLOAT:
          putFloat(headerEntry, getFromBufferAsFloat(byteOffset));
          break;
        case DOUBLE:
          putDouble(headerEntry, getFromBufferAsDouble(byteOffset));
          break;
        default:
          throw new RuntimeException("Invalid entry for SEG-Y binary header: " + headerEntry);
      }
    }
  }

  /**
   * Gets a short (2-byte) value from the header buffer.
   * 
   * @param byteOffset the byte offset in the header.
   * @return the short value at the byte offset.
   */
  public short getFromBufferAsShort(final int byteOffset) {
    short value = 0;
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 2) {
      value = _buffer.getShort();
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
    return value;
  }

  /**
   * Gets an integer (4-byte) value from the header buffer.
   * 
   * @param byteOffset the byte offset in the header.
   * @return the integer value at the byte offset.
   */
  public int getFromBufferAsInt(final int byteOffset) {
    int value = 0;
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 4) {
      value = _buffer.getInt();
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
    return value;
  }

  /**
   * Gets a long (8-byte) value from the header buffer.
   * 
   * @param byteOffset the byte offset in the header.
   * @return the integer value at the byte offset.
   */
  public long getFromBufferAsLong(final int byteOffset) {
    long value = 0;
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 8) {
      value = _buffer.getLong();
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
    return value;
  }

  /**
   * Gets a float (4-byte) value from the header buffer.
   * 
   * @param byteOffset the byte offset in the header.
   * @return the float value at the byte offset.
   */
  public float getFromBufferAsFloat(final int byteOffset) {
    float value = 0;
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 4) {
      value = _buffer.getFloat();
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
    return value;
  }

  /**
   * Gets a double (8-byte) value from the header buffer.
   * 
   * @param byteOffset the byte offset in the header.
   * @return the double value at the byte offset.
   */
  public double getFromBufferAsDouble(final int byteOffset) {
    double value = 0;
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 8) {
      value = _buffer.getDouble();
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
    return value;
  }

  /**
   * Puts a short (2-byte) value into the unassigned portion of the header.
   * 
   * @param byteOffset the byte offset in the header.
   * @param value the short value.
   */
  public void putToBufferAsShort(final int byteOffset, final short value) {
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 2) {
      _buffer.putShort(value);
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
  }

  /**
   * Puts an integer (4-byte) value into the unassigned portion of the header.
   * 
   * @param byteOffset the byte offset in the header.
   * @param value the integer value.
   */
  public void putToBufferAsInt(final int byteOffset, final int value) {
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 4) {
      _buffer.putInt(value);
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
  }

  /**
   * Puts a long (8-byte) value into the unassigned portion of the header.
   * 
   * @param byteOffset the byte offset in the header.
   * @param value the integer value.
   */
  public void putToBufferAsLong(final int byteOffset, final long value) {
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 8) {
      _buffer.putLong(value);
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
  }

  /**
   * Puts a float (4-byte) value into the unassigned portion of the header.
   * 
   * @param byteOffset the byte offset in the header.
   * @param value the float value.
   */
  public void putToBufferAsFloat(final int byteOffset, final float value) {
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 4) {
      _buffer.putFloat(value);
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
  }

  /**
   * Puts a double (8-byte) value into the unassigned portion of the header.
   * 
   * @param byteOffset the byte offset in the header.
   * @param value the double value.
   */
  public void putToBufferAsDouble(final int byteOffset, final double value) {
    _buffer.position(byteOffset);
    if (byteOffset >= 0 && byteOffset <= SEGY_BINARY_HEADER_SIZE - 8) {
      _buffer.putDouble(value);
    } else {
      throw new RuntimeException("Invalid binary header byte location (" + byteOffset + ").");
    }
  }

  /**
   * Returns true if the specified header is identical; false if not.
   * 
   * @param binaryHeader the header to test against.
   * @return true if the specified header is identical; false if not.
   */
  public boolean matches(final SegyBinaryHeader binaryHeader) {
    ByteBuffer buffer = binaryHeader.getBuffer();
    buffer.position(0);
    _buffer.position(0);
    int numBytes = buffer.capacity();
    // Check the byte buffer sizes.
    if (buffer.capacity() != _buffer.capacity()) {
      return false;
    }
    // Check the individual bytes.
    for (int i = 0; i < numBytes; i++) {
      if (buffer.array()[i] != _buffer.array()[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (HeaderEntry headerEntry : getHeaderDefinition().getEntries()) {
      builder.append(headerEntry.getName() + ": " + getValueObject(headerEntry) + "\n");
    }
    return builder.toString();
  }
}
