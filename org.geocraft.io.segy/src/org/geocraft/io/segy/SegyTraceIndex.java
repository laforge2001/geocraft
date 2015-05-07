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
 * A trace index for reading/writing SEG-Y files, which allows for random access into the SEG-Y data file.
 */
public class SegyTraceIndex {

  private static final int VERSION = 101;

  //private static boolean _debug = true;

  public static enum IndexType {
    UNKNOWN("Unknown", 0),
    POSTSTACK_2D("PostStack2d", 1),
    POSTSTACK_3D("PostStack3d", 2),
    PRESTACK_2D("PreStack2d", 3),
    PRESTACK_3D("PreStack3d", 4);

    private final String _name;

    private final int _code;

    private IndexType(final String name, final int code) {
      _name = name;
      _code = code;
    }

    public String getName() {
      return _name;
    }

    public int getCode() {
      return _code;
    }

    @Override
    public String toString() {
      return getName();
    }

    public static IndexType lookupByCode(final int code) {
      IndexType type = UNKNOWN;
      for (IndexType temp : values()) {
        if (temp.getCode() == code) {
          return temp;
        }
      }
      return type;
    }

    public static IndexType lookupByName(final String name) {
      IndexType type = UNKNOWN;
      for (IndexType temp : values()) {
        if (temp.getName().equals(name)) {
          return temp;
        }
      }
      return type;
    }

    /**
     * @return
     */
    public static IndexType[] validValues() {
      IndexType[] types = { POSTSTACK_2D, POSTSTACK_3D, PRESTACK_2D, PRESTACK_3D };
      return types;
    }
  }

  /** The SEG-Y index file path. */
  private final String _path;

  private IndexType _indexType;

  /** Internal byte buffer. */
  private final ByteBuffer _intBuffer;

  /** Internal byte buffer. */
  private final ByteBuffer _longBuffer;

  private int _numTraceKeys;

  private String[] _traceKeyNames;

  private int[] _traceKeyLocs;

  private int[] _traceKeyMins;

  private int[] _traceKeyMaxs;

  private int[] _traceKeyIncs;

  private int[] _traceKeySizes;

  /** The internal byte location of the inline,crossline index. */
  private long _internalMapPosition;

  /** The number of traces in the SEG-Y file. */
  private int _numTraces;

  private RandomAccessFile _raf;

  private FileChannel _channel;

  private boolean _openForRead;

  /**
   * The constructor used when creating a trace index file.
   * @param path the full path to the trace index file.
   * @param indexType the index type (POSTSTACK_2D, POSTSTACK_3D, PRESTACK_3D, etc).
   * @param traceIndexModel the trace index model to write.
   */
  public SegyTraceIndex(final String path, final IndexType indexType, final SegyTraceIndexModel traceIndexModel)
      throws Exception {
    this(path, indexType);
    writeToFile(traceIndexModel);
    read();
  }

  /**
   * The constructor used when reading an existing trace index file.
   * @param path the full path to the trace index file.
   */
  public SegyTraceIndex(final String path) throws Exception {
    this(path, IndexType.UNKNOWN);
    read();
  }

  /**
   * The common private constructor.
   * @param path the full path to the SEG-Y index file.
   * @param indexType the index type (POSTSTACK_2D, POSTSTACK_3D, PRESTACK_3D, etc).
   */
  private SegyTraceIndex(final String path, final IndexType indexType) {
    _path = path;
    _indexType = indexType;
    _intBuffer = ByteBuffer.allocate(4);
    _longBuffer = ByteBuffer.allocate(8);
    _openForRead = false;
  }

  private void read() throws IOException {
    _raf = new RandomAccessFile(_path, "rw");
    _channel = _raf.getChannel();
    int version = readInteger();
    if (version != VERSION) {
      _channel.close();
      throw new IOException("Incompatible version.");
    }
    int code = readInteger();
    _numTraces = readInteger();
    int numKeys = readInteger();
    _indexType = IndexType.lookupByCode(code);
    switch (_indexType) {
      case POSTSTACK_2D:
        if (numKeys != 1) {
          throw new RuntimeException("Incorrect # of trace keys (should be 1 for PostStack2d).");
        }
        _traceKeyNames = new String[] { "CDPs" };
        break;
      case POSTSTACK_3D:
        if (numKeys != 2) {
          throw new RuntimeException("Incorrect # of trace keys (should be 2 for PostStack3d).");
        }
        _traceKeyNames = new String[] { "Inlines", "Xlines" };
        break;
      case PRESTACK_3D:
        if (numKeys != 3) {
          throw new RuntimeException("Incorrect # of trace keys (should be 3 for PreStack3d).");
        }
        _traceKeyNames = new String[] { "Inlines", "Xlines", "Offsets" };
        break;
      default:
        throw new RuntimeException("The index type \'" + _indexType + "\' is not supported.");
    }
    _numTraceKeys = numKeys;
    _traceKeyLocs = new int[numKeys];
    _traceKeyMins = new int[numKeys];
    _traceKeyMaxs = new int[numKeys];
    _traceKeyIncs = new int[numKeys];
    _traceKeySizes = new int[numKeys];
    for (int i = 0; i < numKeys; i++) {
      _traceKeyLocs[i] = readInteger();
      _traceKeyMins[i] = readInteger();
      _traceKeyMaxs[i] = readInteger();
      _traceKeyIncs[i] = readInteger();
      _traceKeySizes[i] = 1 + (_traceKeyMaxs[i] - _traceKeyMins[i]) / _traceKeyIncs[i];
    }
    _internalMapPosition = _channel.position();
    _openForRead = true;
    System.out.println(toString());
  }

  /**
   * Gets the path of the index file.
   * @return the path of the index file.
   */
  public String getPath() {
    return _path;
  }

  public IndexType getType() {
    return _indexType;
  }

  public int getTraceKeyMin(final int index) {
    return _traceKeyMins[index];
  }

  public int getTraceKeyMax(final int index) {
    return _traceKeyMaxs[index];
  }

  public int getTraceKeyInc(final int index) {
    return _traceKeyIncs[index];
  }

  public int getTraceKeyLoc(final int index) {
    return _traceKeyLocs[index];
  }

  /**
   * Gets the internal location of the inline,crossline map.
   * @return the internal location of the inline,crossline map.
   */
  private long getInternalMapPosition() {
    return _internalMapPosition;
  }

  private void writeToFile(final SegyTraceIndexModel traceIndexModel) throws Exception {
    File f = new File(_path);
    if (f.exists()) {
      f.delete();
    }
    if (_raf != null) {
      _raf.close();
    }
    _raf = new RandomAccessFile(_path, "rw");
    _channel = _raf.getChannel();
    long position = 0;
    // Store the trace map code.
    IndexType traceMapCode = IndexType.UNKNOWN;
    if (_indexType.equals(IndexType.POSTSTACK_2D)) {
      traceMapCode = IndexType.POSTSTACK_2D;
    } else if (_indexType.equals(IndexType.POSTSTACK_3D)) {
      traceMapCode = IndexType.POSTSTACK_3D;
    } else if (_indexType.equals(IndexType.PRESTACK_3D)) {
      traceMapCode = IndexType.PRESTACK_3D;
    } else {
      throw new RuntimeException("Invalid trace index type: " + _indexType + ".");
    }
    int numTraces = traceIndexModel.getNumTraces();
    int numKeys = traceIndexModel.getNumKeys();
    writeInteger(VERSION);
    writeInteger(traceMapCode.getCode());
    writeInteger(numTraces);
    // Store the # of header elements.
    writeInteger(numKeys);
    long numBins = 1;
    long[] numBinsForKey = new long[numKeys];
    for (int j = 0; j < numKeys; j++) {
      // Store the minimum,maximum,step for each header element.
      writeInteger(traceIndexModel.getKeyByteLocs()[j]);
      writeInteger(traceIndexModel.getKeyMinValues()[j]);
      writeInteger(traceIndexModel.getKeyMaxValues()[j]);
      writeInteger(traceIndexModel.getIncValues()[j]);
      numBinsForKey[j] = 1 + (traceIndexModel.getKeyMaxValues()[j] - traceIndexModel.getKeyMinValues()[j])
          / traceIndexModel.getIncValues()[j];
      numBins *= numBinsForKey[j];
    }
    position = _channel.position();
    for (int i = 0; i < numBins; i++) {
      writeLong(0);
    }
    _channel.position(position);
    for (int traceIndex = 0; traceIndex < numTraces; traceIndex++) {
      long tracePosition = traceIndexModel.getTracePosition(traceIndex);
      int[] traceKeyValues = traceIndexModel.getTraceKeyValues(traceIndex);
      if (traceKeyValues.length == numKeys) {
        long traceOffset = 0;
        for (int i = 0; i < numKeys; i++) {
          long localOffset = (traceKeyValues[i] - traceIndexModel.getKeyMinValues()[i])
              / traceIndexModel.getIncValues()[i];
          for (int j = i + 1; j < numKeys; j++) {
            localOffset *= numBinsForKey[j];
          }
          traceOffset += localOffset;
        }
        _channel.position(position + traceOffset * 8);
        writeLong(tracePosition);
        //        System.out.println("traceKeyVals: " + traceKeyValues[0] + " " + traceKeyValues[1] + " pos=" + tracePosition
        //            + " offset=" + traceOffset);
      }
    }
    close();
  }

  /**
   * Internal method for reading a long value.
   * @return the long value.
   * @throws IOException thrown on IO error.
   */
  private long readLong() throws IOException {
    _longBuffer.position(0);
    _channel.read(_longBuffer);
    return _longBuffer.getLong(0);
  }

  /**
   * Internal method for reading an integer value.
   * @return the integer value.
   * @throws IOException thrown on IO error.
   */
  private int readInteger() throws IOException {
    _intBuffer.position(0);
    _channel.read(_intBuffer);
    return _intBuffer.getInt(0);
  }

  private void writeInteger(final int value) throws IOException {
    _intBuffer.putInt(0, value);
    _intBuffer.position(0);
    _channel.write(_intBuffer);
  }

  private void writeLong(final long value) throws IOException {
    _longBuffer.putLong(0, value);
    _longBuffer.position(0);
    _channel.write(_longBuffer);
  }

  /**
   * Returns a brief description of the trace index.
   */
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Type: " + _indexType.getName() + " " + _path + "\n");
    for (int i = 0; i < _numTraceKeys; i++) {
      buffer.append(_traceKeyNames[i] + ": " + _traceKeyMins[i] + "," + _traceKeyMaxs[i] + " [" + _traceKeyIncs[i]
          + "]\n");
    }
    return buffer.toString();
  }

  /**
   * Gets the number of indexed traces.
   * @return the number of indexed traces.
   */
  public int getNumTraces() {
    return _numTraces;
  }

  /**
   * Gets the byte positions of the traces of the gather at the specified trace
   * key values.
   * @param traceKeyVals the trace key values of the gather for which to get
   *           trace byte positions.
   * @return the byte positions of the traces.
   */
  public long getTracePosition(final int[] traceKeyVals) throws IOException {
    if (traceKeyVals.length != _numTraceKeys) {
      return 0;
    }
    long traceOffset = 0;
    for (int i = 0; i < _numTraceKeys; i++) {
      long localOffset = (traceKeyVals[i] - _traceKeyMins[i]) / _traceKeyIncs[i];
      for (int j = i + 1; j < _numTraceKeys; j++) {
        localOffset *= _traceKeySizes[j];
      }
      traceOffset += localOffset;
    }
    long newPos = _internalMapPosition + traceOffset * 8;
    if (newPos < 0) {
      return 0;
    }
    _channel.position(_internalMapPosition + traceOffset * 8);
    long tracePosition = readLong();
    return tracePosition;
  }

  private int[] getEntryIndices(final int traceKey0) {
    int[] traceKeys = new int[_numTraceKeys];
    for (int i = 0; i < _numTraceKeys; i++) {
      traceKeys[i] = -999;
    }

    List<Integer> indices = new ArrayList<Integer>();

    int traceKeyIndex0 = (traceKey0 - _traceKeyMins[0]) / _traceKeyIncs[0];

    int[] traceKeyIndices0 = { traceKeyIndex0 };
    if (traceKeyIndex0 < 0) {
      traceKeyIndices0 = new int[_traceKeySizes[0]];
      for (int i = 0; i < _traceKeySizes[0]; i++) {
        traceKeyIndices0[i] = i;
      }
    }

    for (int index0 : traceKeyIndices0) {
      int entryIndex = index0;
      indices.add(new Integer(entryIndex));
    }

    int[] entryIndices = new int[indices.size()];
    for (int i = 0; i < entryIndices.length; i++) {
      entryIndices[i] = indices.get(i).intValue();
    }
    return entryIndices;
  }

  private int[] getEntryIndices(final int traceKey0, final int traceKey1) {
    int[] traceKeys = new int[_numTraceKeys];
    for (int i = 0; i < _numTraceKeys; i++) {
      traceKeys[i] = -999;
    }

    List<Integer> indices = new ArrayList<Integer>();

    int traceKeyIndex0 = (traceKey0 - _traceKeyMins[0]) / _traceKeyIncs[0];
    int traceKeyIndex1 = (traceKey1 - _traceKeyMins[1]) / _traceKeyIncs[1];

    int[] traceKeyIndices0 = { traceKeyIndex0 };
    if (traceKeyIndex0 < 0) {
      traceKeyIndices0 = new int[_traceKeySizes[0]];
      for (int i = 0; i < _traceKeySizes[0]; i++) {
        traceKeyIndices0[i] = i;
      }
    }
    int[] traceKeyIndices1 = { traceKeyIndex1 };
    if (traceKeyIndex1 < 0) {
      traceKeyIndices1 = new int[_traceKeySizes[1]];
      for (int i = 0; i < _traceKeySizes[1]; i++) {
        traceKeyIndices1[i] = i;
      }
    }

    for (int index0 : traceKeyIndices0) {
      int temp0 = index0 * _traceKeySizes[1];
      for (int index1 : traceKeyIndices1) {
        int entryIndex = temp0 + index1;
        indices.add(new Integer(entryIndex));
      }
    }

    int[] entryIndices = new int[indices.size()];
    for (int i = 0; i < entryIndices.length; i++) {
      entryIndices[i] = indices.get(i).intValue();
    }
    return entryIndices;
  }

  private int[] getEntryIndices(final int traceKey0, final int traceKey1, final int traceKey2) {
    int[] traceKeys = new int[_numTraceKeys];
    for (int i = 0; i < _numTraceKeys; i++) {
      traceKeys[i] = -999;
    }

    List<Integer> indices = new ArrayList<Integer>();

    int traceKeyIndex0 = (traceKey0 - _traceKeyMins[0]) / _traceKeyIncs[0];
    int traceKeyIndex1 = (traceKey1 - _traceKeyMins[1]) / _traceKeyIncs[1];
    int traceKeyIndex2 = (traceKey2 - _traceKeyMins[2]) / _traceKeyIncs[2];

    int[] traceKeyIndices0 = { traceKeyIndex0 };
    if (traceKeyIndex0 < 0) {
      traceKeyIndices0 = new int[_traceKeySizes[0]];
      for (int i = 0; i < _traceKeySizes[0]; i++) {
        traceKeyIndices0[i] = i;
      }
    }
    int[] traceKeyIndices1 = { traceKeyIndex1 };
    if (traceKeyIndex1 < 0) {
      traceKeyIndices1 = new int[_traceKeySizes[1]];
      for (int i = 0; i < _traceKeySizes[1]; i++) {
        traceKeyIndices1[i] = i;
      }
    }
    int[] traceKeyIndices2 = { traceKeyIndex2 };
    if (traceKeyIndex2 < 0) {
      traceKeyIndices2 = new int[_traceKeySizes[2]];
      for (int i = 0; i < _traceKeySizes[2]; i++) {
        traceKeyIndices2[i] = i;
      }
    }

    for (int index0 : traceKeyIndices0) {
      int temp0 = index0 * _traceKeySizes[1] * _traceKeySizes[2];
      for (int index1 : traceKeyIndices1) {
        int temp1 = temp0 + index1 * _traceKeySizes[2];
        for (int index2 : traceKeyIndices2) {
          int entryIndex = temp1 + index2;
          indices.add(new Integer(entryIndex));
        }
      }
    }

    int[] entryIndices = new int[indices.size()];
    for (int i = 0; i < entryIndices.length; i++) {
      entryIndices[i] = indices.get(i).intValue();
    }
    return entryIndices;
  }

  public void close() throws IOException {
    if (_raf != null) {
      _raf.close();
    }
  }

  public void deleteFromStore() throws IOException {
    // First close the channel.
    close();
    _raf = null;
    _channel = null;

    // Also delete the SEG-Y index file.
    File ndxFile = new File(_path);
    if (ndxFile.exists()) {
      if (!ndxFile.isDirectory()) {
        if (ndxFile.delete()) {
          ServiceProvider.getLoggingService().getLogger(getClass()).info(
              "The SEG-Y index file was successfully deleted.");
        } else {
          ServiceProvider.getLoggingService().getLogger(getClass()).error("The SEG-Y index file could not be deleted.");
        }
      } else {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(
            "The path represents a directory, not a SEG-Y index file.");
      }
    } else {
      ServiceProvider.getLoggingService().getLogger(getClass()).error("The SEG-Y index file does not exist.");
    }
  }

  /**
   * Store the position of a SEG-Y trace in the SEG-Y trace index.
   * <p>
   * The location at which to store the trace position is computed from the array of trace key values.
   * 
   * @param traceKeyVals the array of trace key values.
   * @param tracePosition the position of the trace in the SEG-Y file.
   * @throws IOException thrown on I/O error.
   */
  public void mapTrace(int[] traceKeyVals, long tracePosition) throws IOException {
    long traceOffset = 0;
    for (int i = 0; i < _numTraceKeys; i++) {
      long localOffset = (traceKeyVals[i] - _traceKeyMins[i]) / _traceKeyIncs[i];
      for (int j = i + 1; j < _numTraceKeys; j++) {
        int numBinsForKey = 1 + (_traceKeyMaxs[j] - _traceKeyMins[j]) / _traceKeyIncs[j];
        localOffset *= numBinsForKey;
      }
      traceOffset += localOffset;
    }
    _channel.position(_internalMapPosition + traceOffset * 8);
    writeLong(tracePosition);
  }
}
