/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import org.geocraft.core.service.ServiceProvider;


/**
 * Defines the class for storing the SEG-Y EBCDIC header information.
 */
public final class SegyEbcdicHeader {

  /** The byte buffer. */
  private final ByteBuffer _buffer;

  /** The character buffer. */
  private char[] _charBuffer;

  /** The byte array input stream. */
  private ByteArrayInputStream _inputStream;

  /** The input stream reader. */
  private InputStreamReader _inputStreamReader;

  /** The byte array output stream. */
  private ByteArrayOutputStream _outputStream;

  /** The output stream writer. */
  private OutputStreamWriter _outputStreamWriter;

  /**
   * Constructs an instance of SegyEbcdicHeader.
   */
  public SegyEbcdicHeader() {

    _buffer = ByteBuffer.allocate(3200);
    try {
      _charBuffer = new char[3200];
      _inputStream = new ByteArrayInputStream(_buffer.array());
      _inputStreamReader = new InputStreamReader(_inputStream, "CP037");
      _outputStream = new ByteArrayOutputStream(3200);
      _outputStreamWriter = new OutputStreamWriter(_outputStream, "CP037");
    } catch (Exception ex) {
      throw new RuntimeException("Unable to construct SEG-Y EBCDIC header: " + ex.toString());
    }
  }

  /**
   * Gets the EBCDIC header size (in bytes).
   * @return the EBCDIC header size (in bytes).
   */
  public int getSize() {
    return _buffer.capacity();
  }

  /**
   * Gets the EBCDIC header byte buffer.
   * @return the EBCDIC header byte buffer.
   */
  public ByteBuffer getBuffer() {
    return _buffer;
  }

  /**
   * Gets the EBCDIC header as a string.
   * @return the EBCDIC header.
   */
  @Override
  public String toString() {

    String text;

    try {
      _inputStreamReader.read(_charBuffer, 0, 3200);
      text = String.valueOf(_charBuffer);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return text.trim();
  }

  /**
   * Returns the EBCDIC header as a multi-line string.
   * @return the EBCDIC header.
   */
  public String asMultiLineString() {
    String text = String.valueOf(_charBuffer);
    StringBuilder builder = new StringBuilder();
    int count = (text.length() - 1) / 80;
    for (int i = 0; i < count; i++) {
      int beginIndex = i * 80;
      int endIndex = beginIndex + 80;
      builder.append(text.substring(beginIndex, endIndex) + "\n");
    }
    return builder.toString();
  }

  /**
   * Sets the EBCDIC header string.
   * @param text the EBCDIC header string.
   */
  public void set(final String text) {

    int len = Math.min(text.length(), 3200);

    for (int i = 0; i < len; i++) {
      _charBuffer[i] = text.charAt(i);
    }
    for (int i = len; i < 3200; i++) {
      _charBuffer[i] = ' ';
    }

    try {
      _buffer.clear();
      _outputStreamWriter.write(_charBuffer, 0, 3200);
      _outputStreamWriter.flush();
      //_outputStreamWriter.close();
      System.arraycopy(_outputStream.toByteArray(), 0, _buffer.array(), 0, 3200);
    } catch (IOException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
    }
  }

  /**
   * Returns true if the specified EBCDIC header matches; false if not.
   * @param ebcdicHeader the header to compare.
   * @return true if the specified EBCDIC header matches; false if not.
   */
  public boolean matches(final SegyEbcdicHeader ebcdicHeader) {
    ByteBuffer buffer = ebcdicHeader.getBuffer();
    buffer.position(0);
    _buffer.position(0);
    int numBytes = buffer.capacity();
    if (buffer.capacity() != _buffer.capacity()) {
      return false;
    }
    for (int i = 0; i < numBytes; i++) {
      if (buffer.array()[i] != _buffer.array()[i]) {
        return false;
      }
    }
    return true;
  }

}
