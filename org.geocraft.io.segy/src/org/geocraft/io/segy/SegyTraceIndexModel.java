/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.service.ServiceProvider;


/**
 * A class for indexing traces within a file (e.g. SEG-Y) based on
 * trace keys (e.g. INLINE,CROSSLINE,CDP).
 */
public class SegyTraceIndexModel {

  private final String _name;

  /** The number of trace keys. */
  private final int _numTraceKeys;

  /** The number of traces. */
  private int _numTraces;

  /** The list of trace key names. */
  private final String[] _traceKeyNames;

  /** The list of trace key byte locations. */
  private final int[] _traceKeyByteLocs;

  /** The list of trace byte positions. */
  private final List<Long> _tracePosition;

  private int[][] _traceKeyValues;

  /** The list of trace key minimums. */
  private final int[] _traceKeyMinVals;

  /** The list of trace key maximums. */
  private final int[] _traceKeyMaxVals;

  /** The list of trace key increments. */
  private final int[] _traceKeyIncVals;

  /** The list of trace key range sizes. */
  private final int[] _traceKeySizes;

  /** The list of temporary files. */
  private RandomAccessFile[] _tempFiles;

  /** The list of temporary file channels. */
  private FileChannel[] _tempChannels;

  /**
   * Constructs a trace index with 1 key.
   * @param name the trace index name.
   * @param traceKeyName the trace key name.
   * @param traceKeyByteLoc the trace key byte location.
   */
  public SegyTraceIndexModel(final String name, final String traceKeyName, final int traceKeyByteLoc) {
    this(name, new String[] { traceKeyName }, new int[] { traceKeyByteLoc });
  }

  public SegyTraceIndexModel(final String name, final String[] traceKeyNames, final int[] traceKeyByteLocs, final int[] traceKeyMinVals, final int[] traceKeyMaxVals, int[] traceKeyIncVals) {
    _name = name;
    _numTraces = 0;
    _tracePosition = new ArrayList<Long>();
    _numTraceKeys = traceKeyNames.length;
    _traceKeyNames = new String[_numTraceKeys];
    _traceKeyByteLocs = new int[_numTraceKeys];
    _traceKeyMinVals = new int[_numTraceKeys];
    _traceKeyMaxVals = new int[_numTraceKeys];
    _traceKeyIncVals = new int[_numTraceKeys];
    _traceKeySizes = new int[_numTraceKeys];
    System.arraycopy(traceKeyNames, 0, _traceKeyNames, 0, _numTraceKeys);
    System.arraycopy(traceKeyByteLocs, 0, _traceKeyByteLocs, 0, _numTraceKeys);
    System.arraycopy(traceKeyMinVals, 0, _traceKeyMinVals, 0, _numTraceKeys);
    System.arraycopy(traceKeyMaxVals, 0, _traceKeyMaxVals, 0, _numTraceKeys);
    System.arraycopy(traceKeyIncVals, 0, _traceKeyIncVals, 0, _numTraceKeys);
    for (int i = 0; i < _numTraceKeys; i++) {
      _traceKeySizes[i] = 1 + (_traceKeyMaxVals[i] - _traceKeyMinVals[i]) / _traceKeyIncVals[i];
    }
  }

  /**
   * Constructs a trace index with 1 key.
   * @param name the trace index name.
   * @param traceKeyNames the list of trace key names.
   * @param traceKeyByteLocs the list of trace byte locations.
   */
  public SegyTraceIndexModel(final String name, final String[] traceKeyNames, final int[] traceKeyByteLocs) {
    _name = name;

    _tracePosition = new ArrayList<Long>();
    _numTraceKeys = traceKeyNames.length;
    _traceKeyNames = new String[_numTraceKeys];
    _traceKeyByteLocs = new int[_numTraceKeys];
    _traceKeyMinVals = new int[_numTraceKeys];
    _traceKeyMaxVals = new int[_numTraceKeys];
    _traceKeyIncVals = new int[_numTraceKeys];
    _traceKeySizes = new int[_numTraceKeys];
    for (int i = 0; i < _numTraceKeys; i++) {
      _traceKeyNames[i] = traceKeyNames[i];
      _traceKeyByteLocs[i] = traceKeyByteLocs[i];
      _traceKeyMinVals[i] = 0;
      _traceKeyMaxVals[i] = 0;
      _traceKeyIncVals[i] = 1;
      _traceKeySizes[i] = 0;
    }
    _tempFiles = new RandomAccessFile[_numTraceKeys];
    _tempChannels = new FileChannel[_numTraceKeys];
    for (int i = 0; i < _numTraceKeys; i++) {
      String traceKeyName = _traceKeyNames[i];
      File file = new File(_name + "." + traceKeyName + ".trace.map");
      file.deleteOnExit();
      try {
        _tempFiles[i] = new RandomAccessFile(file, "rw");
        _tempChannels[i] = _tempFiles[i].getChannel();
      } catch (Exception ex) {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
        throw new RuntimeException(ex.toString());
      }
    }
  }

  /**
   * Gets the trace index name.
   * @return the trace index name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the number of trace keys.
   * @return the number of trace keys.
   */
  public int getNumKeys() {
    return _numTraceKeys;
  }

  /**
   * Gets the trace key names.
   * @return the trace key names.
   */
  public String[] getKeyNames() {
    return _traceKeyNames;
  }

  /**
   * Gets the trace key byte locations.
   * @return the trace key byte locations.
   */
  public int[] getKeyByteLocs() {
    return _traceKeyByteLocs;
  }

  /**
   * Gets the trace key minimum values.
   * @return the trace key minimum values.
   */
  public int[] getKeyMinValues() {
    return _traceKeyMinVals;
  }

  /**
   * Gets the trace key maximum values.
   * @return the trace key maximum values.
   */
  public int[] getKeyMaxValues() {
    return _traceKeyMaxVals;
  }

  /**
   * Gets the trace key increment values.
   * @return the trace key increment values.
   */
  public int[] getIncValues() {
    return _traceKeyIncVals;
  }

  /**
   * Stores the trace position for the specified trace key values.
   * @param traceKeyVals the trace key values.
   * @param tracePosition the trace byte position.
   */
  public void storeTrace(final int[] traceKeyVals, final long tracePosition) throws Exception {

    _numTraces++;
    _tracePosition.add(tracePosition);

    ByteBuffer buffer4 = ByteBuffer.allocate(4);

    for (int i = 0; i < _numTraceKeys; i++) {

      int traceKeyMinVal = _traceKeyMinVals[i];
      int traceKeyMaxVal = _traceKeyMaxVals[i];
      int traceKeyIncVal = _traceKeyIncVals[i];
      int traceKeyVal = traceKeyVals[i];
      //System.out.println("STORING " + _traceKeyNames[i] + " = " + traceKeyVal);

      // Compute new min,max,size.
      if (_numTraces == 1) {
        traceKeyMinVal = traceKeyVal;
        traceKeyMaxVal = traceKeyVal;
        traceKeyIncVal = 0;
      } else {

        if (_numTraces == 2) {
          traceKeyIncVal = Math.abs(traceKeyVal - traceKeyMinVal);
        } else {

          if (traceKeyIncVal != 0) {

            int abs = Math.abs(traceKeyVal - traceKeyMinVal);
            int rem = abs % traceKeyIncVal;

            if (rem != 0) {
              if (traceKeyIncVal % rem == 0) {
                traceKeyIncVal = rem;
              } else {
                traceKeyIncVal = 1;
              }
            }
          } else {
            traceKeyIncVal = Math.abs(traceKeyVal - traceKeyMinVal);
          }
        }
        if (traceKeyVal < traceKeyMinVal || traceKeyVal > traceKeyMaxVal) {
          traceKeyMinVal = Math.min(traceKeyVal, traceKeyMinVal);
          traceKeyMaxVal = Math.max(traceKeyVal, traceKeyMaxVal);
        }
      }
      _traceKeyMinVals[i] = traceKeyMinVal;
      _traceKeyMaxVals[i] = traceKeyMaxVal;

      int traceKeySize = 1;

      if (traceKeyIncVal != 0) {
        traceKeySize += (traceKeyMaxVal - traceKeyMinVal) / traceKeyIncVal;
      }

      _traceKeyIncVals[i] = Math.abs(traceKeyIncVal);
      _traceKeySizes[i] = traceKeySize;

      buffer4.position(0);
      buffer4.putInt(traceKeyVal);
      buffer4.position(0);
      _tempChannels[i].write(buffer4);
    }

    buffer4.clear();
  }

  /**
   * Maps the stored traces into gathers.
   */
  public void mapTraces() throws Exception {

    ByteBuffer buffer = ByteBuffer.allocate(4);

    _traceKeyValues = new int[_numTraces][_numTraceKeys];
    for (int traceIndex = 0; traceIndex < _numTraces; traceIndex++) {
      for (int traceKeyIndex = 0; traceKeyIndex < _numTraceKeys; traceKeyIndex++) {
        int traceKeyMinVal = _traceKeyMinVals[traceKeyIndex];
        int traceKeyMaxVal = _traceKeyMaxVals[traceKeyIndex];

        long position = traceIndex * 4;

        int traceKeyVal = -1;
        buffer.position(0);
        try {
          _tempChannels[traceKeyIndex].read(buffer, position);
          buffer.position(0);
          traceKeyVal = buffer.getInt();
        } catch (IOException e) {
          _traceKeyValues[traceIndex] = new int[0];
          break;
        }
        if (traceKeyVal < traceKeyMinVal || traceKeyVal > traceKeyMaxVal) {
          _traceKeyValues[traceIndex] = new int[0];
          break;
        }
        _traceKeyValues[traceIndex][traceKeyIndex] = traceKeyVal;
      }
    }

    for (int i = 0; i < _numTraceKeys; i++) {
      _tempChannels[i].close();
      _tempFiles[i].close();
    }
    _tempFiles = null;
    _tempChannels = null;

  }

  /**
   * Gets the byte position of the specified trace.
   * @param traceIndex the trace index for which to get the byte position.
   * @return the byte position of the trace.
   */
  public long getTracePosition(final int traceIndex) throws Exception {
    return _tracePosition.get(traceIndex).longValue();
  }

  /**
   * Gets the number of traces in the index.
   * @return the number of traces in the index.
   */
  public int getNumTraces() {
    return _numTraces;
  }

  public void print() {
    ServiceProvider.getLoggingService().getLogger(getClass()).info(toString());
  }

  /**
   * Prints the trace index info to the logger.
   */
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    ServiceProvider.getLoggingService().getLogger(getClass()).info("TraceIndex: " + _name);
    for (int i = 0; i < _numTraceKeys; i++) {
      ServiceProvider.getLoggingService().getLogger(getClass()).info(
          "  Header Key #" + (i + 1) + " => " + _traceKeyNames[i] + " : Min. = " + _traceKeyMinVals[i] + " : Max. = "
              + _traceKeyMaxVals[i] + " : Inc. = " + _traceKeyIncVals[i]);
    }
    return buffer.toString();
  }

  public int[] getTraceKeyValues(int traceIndex) {
    return _traceKeyValues[traceIndex];
  }
}
