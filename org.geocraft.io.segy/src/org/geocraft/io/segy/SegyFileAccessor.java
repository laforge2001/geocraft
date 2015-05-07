/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


/**
 * Defines a basic accessor for reading/writing SEG-Y files.
 * It is currently used by SegyMapper, but can be used by itself.
 */
public class SegyFileAccessor {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(SegyFileAccessor.class);

  /** The reference to the unit preferences. */
  private static final UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  /** Enumeration for the open/close action on the SEG-Y file. */
  public static enum OpenClose {
    /** Represents that the SEG-Y file is to be closed. */
    CLOSE,
    /** Represents that the SEG-Y file is to be opened for reading. */
    OPEN_FOR_READ,
    /** Represents that the SEG-Y file is to be opened for writing. */
    OPEN_FOR_WRITE;
  }

  /** The synchronization token for thread safety. */
  public final Object _synchronizeToken = new Object();

  /** The SEG-Y file comments (to be stored in the EBCDIC header). */
  private String _comments;

  /** The (estimated) number of traces in the SEG-Y file. */
  private int _numTraces;

  /** The number of samples per trace in the SEG-Y file. */
  private int _numSamplesPerTrace;

  /** The starting z value. */
  private float _startZ;

  /** The ending z value. */
  private float _endZ;

  /** The delta z value. */
  private float _deltaZ;

  /** The random access file. */
  private RandomAccessFile _raf;

  /** The file channel. */
  private FileChannel _channel;

  /** The SEG-Y EBCDIC header. */
  private SegyEbcdicHeader _ebcdicHeader;

  /** The SEG-Y binary header. */
  private SegyBinaryHeader _binaryHeader;

  /** The full path of the SEG-Y file on disk. */
  private final String _storeFilePath;

  /** The domain (time or depth). */
  private Domain _domain;

  /** The SEG-Y sample format code. */
  private int _sampleFormatCode;

  private final VolumeMapperModel _model;

  private OpenClose _openStatus = OpenClose.CLOSE;

  /**
   * The lone argument is simply a properties instance, which contains the data storage properties used for
   * mapping of the entity to a SEG-Y file on disk. These properties will either come from a data store properties panel when data
   * is loaded by the user, or from a prototype entity if created by an algorithm. In the former case, the properties should contain
   * an element for unique ID, since all the properties must be explicity set during load. In the latter case, the unique ID will
   * not be included and therefore a temporary unique ID will be created. This is done because the properties came from a prototype
   * and do not accurately represent the appropriate data mapping. The temporary unique ID will be updated by the mapper when
   * required and when a reference to the actual entity being mapped is discovered.
   */
  public SegyFileAccessor(final VolumeMapperModel model) {
    _model = model;

    // Create the EBCDIC and binary headers.
    _ebcdicHeader = new SegyEbcdicHeader();
    _binaryHeader = new SegyBinaryHeader();

    _domain = _model.getUnitOfZ().getDomain();
    if (_domain != Domain.TIME && _domain != Domain.DISTANCE) {
      throw new RuntimeException("Invalid z unit (" + _model.getUnitOfZ().getDomain() + "). Must be TIME or DEPTH.");
    }

    // The file path.
    _storeFilePath = _model.getDirectory() + File.separator + _model.getFileName() + _model.getFileExtension();

    // The sample format code (binary header).
    _sampleFormatCode = SegyUtil.getSampleFormatCode(_model.getSampleFormat());
    //SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IBM));

    //_swapBytes = !_storeByteOrder.equals(ByteOrder.nativeOrder());

    // Set the default comments.
    setComments("");

  }

  /**
   * Opens the SEG-Y file on disk for I/O access.
   */
  public void openForRead() {
    if (_openStatus.equals(OpenClose.OPEN_FOR_WRITE)) {
      close();
    }
    if (_channel == null) {
      try {
        _raf = new RandomAccessFile(_storeFilePath, "r");
        _channel = _raf.getChannel();
        _openStatus = OpenClose.OPEN_FOR_READ;
      } catch (FileNotFoundException ex) {
        LOGGER.error(ex.toString());

        /**
         * This is a temporary hack, to at least notify the user of the file error.
         *
         * We should really be checking the SEG-Y header to make sure
         * it is a valid SEG-Y (ensure its not a word document or something)
         *
         * And it should not cancel the file read operation for multiple files
         * when one of the files cannot be successfully read. Currently, it throws
         * an Exception and the flow of execution (for loop) is canceled.
         */
        LOGGER
            .error(_storeFilePath
                + " cannot be loaded.\n\nThis file cannot be found or you do not have the proper permissions to write to it.\n\n"
                + "If you were loading more than one file, it is possible they have not loaded.\n\n"
                + "Please check your settings and try again.");
      }
    }
  }

  /**
   * Opens the SEG-Y file on disk for I/O access.
   */
  public void openForWrite() {
    openForWrite(false);
  }

  protected void openForWrite(boolean reset) {
    if (_openStatus.equals(OpenClose.OPEN_FOR_READ)) {
      close();
    }
    if (_channel == null) {
      try {
        _raf = new RandomAccessFile(_storeFilePath, "rw");
        if (reset) {
          if (_raf.length() >= 3600) {
            _raf.setLength(3600);
          } else {
            _raf.setLength(0);
          }
        }
        _channel = _raf.getChannel();
        _openStatus = OpenClose.OPEN_FOR_WRITE;
      } catch (IOException ex) {
        LOGGER.error(ex.toString());

        /**
         * This is a temporary hack, to at least notify the user of the file error.
         *
         * We should really be checking the SEG-Y header to make sure
         * it is a valid SEG-Y (ensure its not a word document or something)
         *
         * And it should not cancel the file read operation for multiple files
         * when one of the files cannot be successfully read. Currently, it throws
         * an Exception and the flow of execution (for loop) is canceled.
         */
        LOGGER
            .error(_storeFilePath
                + " cannot be loaded.\n\nThis file cannot be found or you do not have the proper permissions to write to it.\n\n"
                + "If you were loading more than one file, it is possible they have not loaded.\n\n"
                + "Please check your settings and try again.");
      }
    }
  }

  /**
   * Closes the SEG-Y file on disk from I/O access.
   */
  public void close() {
    if (_channel != null) {
      synchronized (_synchronizeToken) {
        try {
          _raf.close();
        } catch (IOException e) {
          LOGGER.error(e.toString());
        }
        _channel = null;
      }
    }
    _raf = null;
    _openStatus = OpenClose.CLOSE;
  }

  /**
   * Closes the SEG-Y file access.
   */
  public boolean shutdown() {
    close();
    return true;
  }

  /**
   * Returns <i>true</i>, as SEG-Y mappers support writing of data.
   * @return <i>true</i> always.
   */
  public boolean supportsWrite() {
    return true;
  }

  /**
   * Deletes the SEG-Y file on disk.
   */
  public void delete() {
    // Delete the SEG-Y file.
    File file = new File(_storeFilePath);
    if (file.exists()) {
      if (!file.isDirectory()) {
        if (file.delete()) {
          LOGGER.info("The SEG-Y file was successfully deleted.");
        } else {
          LOGGER.error("The SEG-Y file could not be deleted.");
        }
      } else {
        LOGGER.error("The path represents a directory, not a SEG-Y file.");
      }
    } else {
      LOGGER.error("The SEG-Y file does not exist.");
    }
  }

  /**
   * Reads the SEG-Y EBCDIC and binary header from the file on disk in order
   * to establish certain properties of the data.
   */
  public void read() throws IOException {
    synchronized (_synchronizeToken) {

      File file = new File(_storeFilePath);

      if (file.exists()) {

        if (!file.isDirectory()) {

          if (file.length() >= 3600) {

            // Open the random access file for I/O.
            openForRead();
            try {

              // Read the EBCDIC and binary headers.
              _ebcdicHeader = SegyUtil.readEbcdicHeader(_storeFilePath);
              _binaryHeader = SegyUtil.readBinaryHeader(_storeFilePath);

              setComments(_ebcdicHeader.toString());

              long fileLength = _channel.size();
              long position = 3600;

              setFilePosition(position);

              // Get the x,y units of measurement (binary header).
              //int measureSys = _binaryHeader.get(SegyBinaryHeaderCatalog.MEASUREMENT_SYSTEM.getID());
              //_model.setUnitOfXY(SegyUtil.getHorizontalDistanceUnit(measureSys));

              // Get the application horizontal distance units.
              Unit xyUnits = UNIT_PREFS.getHorizontalDistanceUnit();

              if (_model.getUnitOfXY().equals(Unit.UNDEFINED)) {

                if (xyUnits.equals(Unit.UNDEFINED)) {

                  // If SEG-Y units are undefined and app units are undefined,
                  // then default to meters for both.
                  _model.setUnitOfXY(Unit.METER);
                  xyUnits = _model.getUnitOfXY();
                  UNIT_PREFS.setHorizontalDistanceUnit(xyUnits);
                  LOGGER.info("SEG-Y and application x,y units undefined. Defaulting to meters.");
                } else {

                  // If SEG-Y units are undefined and app units are defined,
                  // then set the SEG-Y units to the app units.
                  _model.setUnitOfXY(xyUnits);
                  LOGGER.info("SEG-Y x,y units undefined. Defaulting to application x,y units of " + xyUnits.getName());
                }
              } else {

                // If SEG-Y units are defined and app units are undefined,
                // then set the app units to the SEG-Y units.
                if (xyUnits.equals(Unit.UNDEFINED)) {
                  xyUnits = _model.getUnitOfXY();
                  UNIT_PREFS.setHorizontalDistanceUnit(xyUnits);
                  LOGGER.info("Application x,y units undefined. Defaulting to SEG-Y x,y units of " + xyUnits.getName());
                }
              }
              if (_model.getUnitOfXY().equals(Unit.UNDEFINED)) {
                throw new RuntimeException("SEG-Y x,y unit of measurement undefined.");
              }

              // Get the number of samples per trace (binary header).
              int numSamplesPerTrace = _binaryHeader.getShort(SegyBinaryHeaderCatalog.SAMPLES_PER_TRACE);

              // Gets the sample format code (binary header).
              if (_sampleFormatCode == 0) {
                _sampleFormatCode = _binaryHeader.getShort(SegyBinaryHeaderCatalog.SAMPLE_FORMAT_CODE);
              }

              // Get the sample interval (binary header).
              int sampleRate = _binaryHeader.getShort(SegyBinaryHeaderCatalog.SAMPLE_INTERVAL);
              float deltaZ = sampleRate;

              if (!Float.isNaN(_model.getSampleRate())) {
                deltaZ = _model.getSampleRate();
              } else {

                if (_domain.equals(Domain.TIME)) {

                  // Note: actual storage units are microseconds, but convert them to milliseconds.
                  deltaZ = Unit.convert(deltaZ, Unit.MICROSECOND, _model.getUnitOfZ());
                } else if (_domain.equals(Domain.DISTANCE)) {
                  deltaZ /= 1000;
                }
                _model.setSampleRate(deltaZ);
              }

              Unit zUnit = Unit.UNDEFINED;

              if (_domain.equals(Domain.TIME)) {
                zUnit = UNIT_PREFS.getTimeUnit();
                if (zUnit.equals(Unit.UNDEFINED)) {
                  zUnit = _model.getUnitOfZ();
                  UNIT_PREFS.setTimeUnit(zUnit);
                }
              } else if (_domain.equals(Domain.DISTANCE)) {
                zUnit = UNIT_PREFS.getVerticalDistanceUnit();
                if (zUnit.equals(Unit.UNDEFINED)) {
                  zUnit = _model.getUnitOfZ();
                  UNIT_PREFS.setVerticalDistanceUnit(zUnit);
                }
              }
              if (zUnit.equals(Unit.UNDEFINED)) {
                throw new RuntimeException("Invalid z units (" + zUnit + ").");
              }
              deltaZ = Unit.convert(deltaZ, _model.getUnitOfZ(), zUnit);

              float startZ = 0;
              float endZ = 0;

              long dataLength = fileLength - 3600;
              int numSamples = numSamplesPerTrace;
              int numTraces = 0;

              if (dataLength > 0) {

                SegyTraceHeader traceHeader = new SegyTraceHeader();

                traceHeader.getBuffer().position(0);
                readByteBuffer(traceHeader.getBuffer(), position);
                traceHeader.getBuffer().position(0);
                traceHeader.updateHeaderFromBuffer();

                startZ = traceHeader.getShort(SegyTraceHeaderCatalog.DELAY_RECORDING_TIME);
                startZ = Unit.convert(startZ, _model.getUnitOfZ(), zUnit);
                endZ = startZ + (numSamplesPerTrace - 1) * deltaZ;
                numSamples = 1 + Math.round((endZ - startZ) / deltaZ);

                int numBytesPerSample = 4;

                if (_sampleFormatCode == SegyBytes.SAMPLE_FORMAT_CODE_FIXED_2BYTE) {
                  numBytesPerSample = 2;
                } else if (_sampleFormatCode == SegyBytes.SAMPLE_FORMAT_CODE_FIXED_1BYTE) {
                  numBytesPerSample = 1;
                }
                numTraces = (int) (dataLength / (240 + numSamples * numBytesPerSample));
              }

              _numTraces = numTraces;
              _numSamplesPerTrace = numSamples;
              _startZ = startZ;
              _endZ = endZ;
              _deltaZ = deltaZ;
            } catch (Exception e) {
              LOGGER.error(e.toString());
            }

            // Close the file.
            close();
          } else {
            throw new IOException("The SEG-Y file \'" + file.getAbsolutePath()
                + "\' does not contain the required EBCDIC and/or binary headers.");
          }
        } else {
          throw new IOException("The path \'" + _storeFilePath + "\' represents a directory, not a SEG-Y file.");
        }
      } else {
        throw new IOException("The SEG-Y file does not exist. " + _storeFilePath);
      }
    }
  }

  /**
   * Gets the channel file position.
   * 
   * @return the channel file position.
   * @throws IOException thrown on I/O error.
   */
  public long getFilePosition() throws IOException {
    return _channel.position();
  }

  /**
   * Sets the channel file position.
   * 
   * @param newPosition the file position to set.
   * @throws IOException thrown on I/O error.
   */
  public void setFilePosition(final long newPosition) throws IOException {
    _channel.position(newPosition);
  }

  /**
   * Sets the channel file position to the end of the file.
   * 
   * @throws IOException thrown on I/O error.
   */
  public void setFilePositionEOF() throws IOException {
    _channel.position(_raf.length());
  }

  /**
   * Reads a sequence of bytes from the channel into the byte buffer.
   * 
   * @param buffer the buffer into which to put the bytes.
   * @throws IOException thrown on I/O error.
   */
  public void readByteBuffer(final ByteBuffer buffer) throws IOException {
    _channel.read(buffer);
  }

  /**
   * Reads a sequence of bytes from the channel into the byte buffer.
   * 
   * @param buffer the buffer into which to put the bytes from reading.
   * @param position the file position at which to read.
   * @throws IOException thrown on I/O error.
   */
  public void readByteBuffer(final ByteBuffer buffer, final long position) throws IOException {
    _channel.read(buffer, position);
  }

  /**
   * Writes a sequence of bytes to the channel from the byte buffer.
   * 
   * @param buffer the buffer from which to get the bytes for writing.
   * @throws IOException thrown on I/O error.
   */
  public void writeByteBuffer(final ByteBuffer buffer) throws IOException {
    _channel.write(buffer);
  }

  /**
   * Gets the size of the SEG-Y file on disk (in bytes).
   * @return the size of the SEG-Y file on disk.
   */
  public long getFileSize() throws IOException {
    return _raf.length();
  }

  /**
   * Transfers an array of byte data to an array of float data. This is used when reading
   * trace data from the SEG-Y file on disk.
   * 
   * @param numSamples the number of samples.
   * @param bytes the array of byte values.
   * @param floats the array of float values.
   */
  public void getFloatsFromBytes(final int numSamples, final byte[] bytes, final float[] floats) {
    ByteOrder byteOrder = ByteOrder.nativeOrder();
    if (_model.getByteOrder().equals(ByteOrder.BIG_ENDIAN.toString())) {
      byteOrder = ByteOrder.BIG_ENDIAN;
    } else if (_model.getByteOrder().equals(ByteOrder.LITTLE_ENDIAN.toString())) {
      byteOrder = ByteOrder.LITTLE_ENDIAN;
    } else {
      throw new RuntimeException("Invalid byte order (" + byteOrder + ").");
    }
    SegyBytes.getFloatsFromBytes(_sampleFormatCode, numSamples, bytes, floats, byteOrder);
  }

  /**
   * Transfers an array of float data to an array of byte data. This is used when writing
   * trace data to the SEG-Y file on disk.
   * 
   * @param numSamples the number of samples.
   * @param floats the array of float values.
   * @param bytes the array of byte values.
   */
  public void putFloatsToBytes(final int numSamples, final float[] floats, final byte[] bytes) {
    ByteOrder byteOrder = ByteOrder.nativeOrder();
    if (_model.getByteOrder().equals(ByteOrder.BIG_ENDIAN.toString())) {
      byteOrder = ByteOrder.BIG_ENDIAN;
    } else if (_model.getByteOrder().equals(ByteOrder.LITTLE_ENDIAN.toString())) {
      byteOrder = ByteOrder.LITTLE_ENDIAN;
    } else {
      throw new RuntimeException("Invalid byte order (" + byteOrder + ").");
    }
    SegyBytes.putFloatsToBytes(_sampleFormatCode, numSamples, floats, bytes, byteOrder);
  }

  /**
   * Sets the comments to be put into the EBCDIC header.
   * @param comments the comments to be set.
   */
  public void setComments(final String comments) {
    _comments = comments;
  }

  /**
   * Appends to the comments to be put into the EBCDIC header.
   * @param comments the comments to be appended.
   */
  public void appendComments(final String comments) {
    _comments += comments;
  }

  /**
   * Returns the comments to be put into the EBCDIC header.
   */
  public String getComments() {
    return _comments;
  }

  /**
   * Returns the EBCDIC header of the SEG-Y file.
   */
  public SegyEbcdicHeader getEbcdicHeader() {
    return _ebcdicHeader;
  }

  /**
   * Returns the binary header of the SEG-Y file.
   */
  public SegyBinaryHeader getBinaryHeader() {
    return _binaryHeader;
  }

  /**
   * Returns the SEG-Y sample format code.
   * @return
   */
  public int getSampleFormatCode() {
    return _sampleFormatCode;
  }

  /**
   * Sets the SEG-Y sample format code.
   */
  public void setSampleFormatCode() {
    _sampleFormatCode = SegyUtil.getSampleFormatCode(_model.getSampleFormat());
  }

  /**
   * Returns the domain of the SEG-Y file.
   */
  public Domain getDomain() {
    return _domain;
  }

  /**
   * Sets the domain of the SEG-Y file.
   * 
   * @param domain the domain (Time or Depth).
   */
  public void setDomain(final Domain domain) {
    _domain = domain;
  }

  /**
   * Returns the synchronization token, used to block when reading/writing traces.
   * @return the synchronization token.
   */
  public Object getSynchronizeToken() {
    return _synchronizeToken;
  }

  /**
   * Returns the number of samples per trace in the SEG-Y file.
   */
  public int getNumSamplesPerTrace() {
    return _numSamplesPerTrace;
  }

  /**
   * Returns the number of traces in the SEG-Y file.
   */
  public int getNumTraces() {
    return _numTraces;
  }

  /**
   * Returns the delta-z value of the SEG-Y file.
   */
  public float getStartZ() {
    return _startZ;
  }

  /**
   * Returns the delta-z value of the SEG-Y file.
   */
  public float getEndZ() {
    return _endZ;
  }

  /**
   * Returns the delta-z value of the SEG-Y file.
   */
  public float getDeltaZ() {
    return _deltaZ;
  }

  public void reinitialize() {
    close();
    openForWrite(true);
    close();
  }

}
