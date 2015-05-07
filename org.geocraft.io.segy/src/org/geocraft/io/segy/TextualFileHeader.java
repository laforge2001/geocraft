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


public class TextualFileHeader {

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
   * Constructs an instance of TextualFileHeader.
   */
  public TextualFileHeader() {
    _buffer = ByteBuffer.allocate(3200);
    try {
      _charBuffer = new char[3200];
      _inputStream = new ByteArrayInputStream(_buffer.array());
      _inputStreamReader = new InputStreamReader(_inputStream);
      _outputStream = new ByteArrayOutputStream(3200);
      _outputStreamWriter = new OutputStreamWriter(_outputStream);
    } catch (Exception ex) {
      throw new RuntimeException("Unable to construct SEG-Y Textual File Header header: " + ex.toString());
    }
  }

  /**
   * Gets the Textual File Header header size (in bytes).
   * 
   * @return the Textual File Header header size (in bytes).
   */
  public int getSize() {
    return _buffer.capacity();
  }

  /**
   * Gets the Textual File Header header byte buffer.
   * 
   * @return the Textual File Header header byte buffer.
   */
  public ByteBuffer getBuffer() {
    return _buffer;
  }

  /**
   * Gets the Textual File Header header as a string.
   * @return the Textual File Header header.
   */
  @Override
  public String toString() {
    String text = "";
    try {
      _inputStreamReader.read(_charBuffer, 0, 3200);
      text = String.valueOf(_charBuffer);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return text.trim();
  }

  /**
   * Returns the Textual File Header header as a multi-line string.
   * 
   * @return the Textual File Header header.
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

  public void set(final String text, int lineIndex) {
    char[] charBuffer = new char[80];
    int len = Math.min(text.length(), 80);
    for (int i = 0; i < len; i++) {
      charBuffer[i] = text.charAt(i);
    }
    for (int i = len; i < 3200; i++) {
      charBuffer[i] = ' ';
    }
    try {
      int index = lineIndex * 80;
      _outputStreamWriter.write(charBuffer, 0, 80);
      _outputStreamWriter.flush();
      //_outputStreamWriter.close();
      System.arraycopy(_outputStream.toByteArray(), 0, _buffer.array(), index, 80);
    } catch (IOException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
    }
  }

  /**
   * Sets the Textual File Header header string.
   * 
   * @param text the Textual File Header header string.
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
   * Returns <i>true</i> if the specified Textual File Header header matches; <i>false</i> if not.
   * 
   * @param textualFileHeader the Textual File Header to compare.
   * @return <i>true</i> if the specified Textual File Header header matches; <i>false</i> if not.
   */
  public boolean matches(final TextualFileHeader textualFileHeader) {
    ByteBuffer buffer = textualFileHeader.getBuffer();
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
