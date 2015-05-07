/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.datatypes.DataType;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;


/**
 * A set of utility methods for SEG-Y.
 */
public class SegyUtil {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(SegyUtil.class);

  /**
   * Gets the number of bytes per trace sample.
   * 
   * @param sampleFormatCode the SEG-Y sample format code.
   * @return the number of bytes per sample.
   */
  public static int getNumBytesPerSample(final int sampleFormatCode) {
    int numBytesPerSample = 0;
    switch (sampleFormatCode) {
      case 1:
      case 2:
      case 4:
        numBytesPerSample = 4;
        break;
      case 3:
        numBytesPerSample = 2;
        break;
      case 8:
        numBytesPerSample = 1;
        break;
      default:
        throw new IllegalArgumentException("Invalid SEG-Y sample format code: " + sampleFormatCode);
    }
    return numBytesPerSample;
  }

  /**
   * Gets the horizontal distance unit based on the SEG-Y measurement system code.
   * @param measureSys the SEG-Y measurement system code.
   * @return the horizontal distance unit.
   */
  public static Unit getHorizontalDistanceUnit(final int measureSys) {
    Unit unit = Unit.UNDEFINED;
    if (measureSys == 1) {
      unit = Unit.METER;
    } else if (measureSys == 2) {
      unit = Unit.FOOT;
    }
    return unit;
  }

  /**
   * Gets the SEG-Y measurement system code based on the specified units.
   * @param horizontalDistanceUnit the horizontal distance unit.
   * @return the SEG-Y measurement system code.
   */
  public static int getMeasurementSystemCode(final Unit horizontalDistanceUnit) {
    int measurementSys = 0;
    if (horizontalDistanceUnit.equals(Unit.METER)) {
      measurementSys = 1;
    } else if (horizontalDistanceUnit.equals(Unit.FOOT)) {
      measurementSys = 2;
    } else {
      LOGGER.warn("Invalid units of x,y...must be meters or feet.");
    }
    return measurementSys;
  }

  /**
   * Gets the storage data type.
   * @param sampleFormatCode the SEG-Y sample format code.
   * @return the storage data type.
   */
  public static DataType getStorageDataType(final int sampleFormatCode) {
    DataType storageDataType = null;
    switch (sampleFormatCode) {
      case 1:
      case 2:
      case 4:
        storageDataType = DataType.FLOAT;
        break;
      case 3:
        storageDataType = DataType.SHORT;
        break;
      case 8:
        storageDataType = DataType.BYTE;
        break;
      default:
        throw new IllegalArgumentException("Invalid SEG-Y sample format code: " + sampleFormatCode);
    }
    return storageDataType;
  }

  /**
   * Reads the SEG-Y EBCDIC header.
   * @param path the SEG-Y file path.
   * @return the SEG-Y EBCDIC header.
   * @throws IOException
   */
  public static SegyEbcdicHeader readEbcdicHeader(final String path) throws IOException {
    RandomAccessFile file = new RandomAccessFile(path, "r");
    FileChannel channel = file.getChannel();
    SegyEbcdicHeader ebcdicHeader = new SegyEbcdicHeader();
    channel.position(0);
    ebcdicHeader.getBuffer().position(0);
    channel.read(ebcdicHeader.getBuffer());
    file.close();
    return ebcdicHeader;
  }

  /**
   * Reads the SEG-Y binary header.
   * @param path the SEG-Y file path.
   * @return the SEG-Y binary header.
   * @throws IOException
   */
  public static SegyBinaryHeader readBinaryHeader(final String path) throws IOException {
    RandomAccessFile file = new RandomAccessFile(path, "r");
    FileChannel channel = file.getChannel();
    SegyBinaryHeader binaryHeader = new SegyBinaryHeader();
    channel.position(3200);
    binaryHeader.getBuffer().position(0);
    channel.read(binaryHeader.getBuffer());
    file.close();
    binaryHeader.updateHeaderFromBuffer();
    return binaryHeader;
  }

  /**
   * Reads the 1st SEG-Y trace header.
   * 
   * @param path the SEG-Y file path.
   * @param numExtendedHeaders the number of extended textual headers records.
   * @return the SEG-Y trace header.
   * @throws IOException thrown on I/O error.
   */
  public static SegyTraceHeader readFirstTraceHeader(final String path, final int numExtendedHeaders) throws IOException {
    RandomAccessFile file = new RandomAccessFile(path, "r");
    FileChannel channel = file.getChannel();
    SegyTraceHeader traceHeader = new SegyTraceHeader();
    if (numExtendedHeaders == 0) {
      channel.position(3600);
    } else if (numExtendedHeaders > 0) {
      channel.position(3600 + numExtendedHeaders * 3200);
    } else {
      throw new UnsupportedOperationException("Variable # of extended textual headers not yet supported.");
    }
    traceHeader.getBuffer().position(0);
    channel.read(traceHeader.getBuffer());
    file.close();
    return traceHeader;
  }

  /**
   * Gets the SEG-Y trace index.
   * @param path the SEG-Y file path.
   * @param ilineByteLoc the inline byte location.
   * @param xlineByteLoc the x-line byte location.
   * @return the SEG-Y trace index.
   * @throws Exception thrown on error.
   */
  public static SegyTraceIndex getTraceIndex(final VolumeMapperModel model, final IndexType type) throws Exception {
    SegyTraceIndex traceIndex = null;
    String filePath = model.getDirectory() + File.separator + model.getFileName() + model.getFileExtension();
    File file = new File(filePath + ".ndx");
    boolean needsIndexing = true;
    // Check if SEG-Y index file exists.
    if (file.exists()) {
      traceIndex = new SegyTraceIndex(filePath + ".ndx");
      needsIndexing = traceIndex.getType().equals(IndexType.UNKNOWN);
    }
    // If SEG-Y index file does not exist, generate one.
    if (needsIndexing) {
      SegyTraceIndexer indexer = new SegyTraceIndexer(model, type);
      SegyTraceIndexModel traceIndexModel = (SegyTraceIndexModel) TaskRunner.runTask(indexer, "SEG-Y Trace Indexer");
      if (traceIndexModel != null) {
        traceIndex = new SegyTraceIndex(filePath + ".ndx");
      }
    }
    return traceIndex;
  }

  /**
   * Writes the SEG-Y EBCDIC header.
   * @param path the SEG-Y file path.
   * @param ebcdicHeader the SEG-Y EBCDIC header.
   * @throws IOException
   */
  public static void writeEbcdicHeader(final String path, final SegyEbcdicHeader ebcdicHeader) throws IOException {
    RandomAccessFile file = new RandomAccessFile(path, "rw");
    FileChannel channel = file.getChannel();
    channel.position(0);
    ebcdicHeader.getBuffer().position(0);
    channel.write(ebcdicHeader.getBuffer());
    file.close();
  }

  /**
   * Writes the SEG-Y binary header.
   * @param path the SEG-Y file path.
   * @param binaryHeader the SEG-Y binary header.
   * @throws IOException
   */
  public static void writeBinaryHeader(final String path, final SegyBinaryHeader binaryHeader) throws IOException {
    binaryHeader.updateBufferFromHeader();
    RandomAccessFile file = new RandomAccessFile(path, "rw");
    FileChannel channel = file.getChannel();
    channel.position(3200);
    binaryHeader.getBuffer().position(0);
    channel.write(binaryHeader.getBuffer());
    file.close();
  }

  /**
   * Gets the SEG-Y sample format code based on the sample format string.
   * @param sampleFormatStr the sample format string.
   * @return the SEG-Y sample format code.
   */
  public static int getSampleFormatCode(final String sampleFormatStr) {
    if (sampleFormatStr.equals(SegyBytes.SAMPLE_FORMAT_FIXED_1BYTE)) {
      return SegyBytes.SAMPLE_FORMAT_CODE_FIXED_1BYTE;
    } else if (sampleFormatStr.equals(SegyBytes.SAMPLE_FORMAT_FIXED_2BYTE)) {
      return SegyBytes.SAMPLE_FORMAT_CODE_FIXED_2BYTE;
    } else if (sampleFormatStr.equals(SegyBytes.SAMPLE_FORMAT_FIXED_4BYTE)) {
      return SegyBytes.SAMPLE_FORMAT_CODE_FIXED_4BYTE;
    } else if (sampleFormatStr.equals(SegyBytes.SAMPLE_FORMAT_FIXED_4BYTE_WITH_GAIN)) {
      return SegyBytes.SAMPLE_FORMAT_CODE_FIXED_4BYTE_WITH_GAIN;
    } else if (sampleFormatStr.equals(SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IBM)) {
      return SegyBytes.SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IBM;
    } else if (sampleFormatStr.equals(SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IEEE)) {
      return SegyBytes.SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IEEE;
    }
    return 0;
  }

  /**
   * Gets the sample format string based on the SEG-Y sample format code.
   * @param sampleFormatCode the SEG-Y sample format code.
   * @return the sample format string.
   */
  public static String getSampleFormatString(final int sampleFormatCode) {
    switch (sampleFormatCode) {
      case SegyBytes.SAMPLE_FORMAT_CODE_FIXED_1BYTE:
        return SegyBytes.SAMPLE_FORMAT_FIXED_1BYTE;
      case SegyBytes.SAMPLE_FORMAT_CODE_FIXED_2BYTE:
        return SegyBytes.SAMPLE_FORMAT_FIXED_2BYTE;
      case SegyBytes.SAMPLE_FORMAT_CODE_FIXED_4BYTE:
        return SegyBytes.SAMPLE_FORMAT_FIXED_4BYTE;
      case SegyBytes.SAMPLE_FORMAT_CODE_FIXED_4BYTE_WITH_GAIN:
        return SegyBytes.SAMPLE_FORMAT_FIXED_4BYTE_WITH_GAIN;
      case SegyBytes.SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IBM:
        return SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IBM;
      case SegyBytes.SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IEEE:
        return SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IEEE;
      default:
        LOGGER.warn("Invalid SEG-Y sample format code : ");
    }
    return "Unknown";
  }

  public static final String[] getSampleFormats() {
    String[] formats = new String[] { SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IBM, SegyBytes.SAMPLE_FORMAT_FIXED_4BYTE,
        SegyBytes.SAMPLE_FORMAT_FIXED_2BYTE, SegyBytes.SAMPLE_FORMAT_FIXED_4BYTE_WITH_GAIN,
        SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IEEE, SegyBytes.SAMPLE_FORMAT_FIXED_1BYTE };
    return formats;
  }

  public static boolean needIndexing(final String filePath) {
    String ndxFilePath = filePath + ".ndx";
    File datFile = new File(filePath);
    File ndxFile = new File(ndxFilePath);
    // Check if the index file exists.
    if (ndxFile.exists()) {
      // Check if the index file is older than the data file.
      if (ndxFile.lastModified() < datFile.lastModified()) {
        return true;
      }
    } else {
      return true;
    }
    return false;
  }

  public static ByteOrder getByteOrder(final String value) {
    if (value.equalsIgnoreCase(ByteOrder.LITTLE_ENDIAN.toString())) {
      return ByteOrder.LITTLE_ENDIAN;
    } else if (value.equalsIgnoreCase(ByteOrder.BIG_ENDIAN.toString())) {
      return ByteOrder.BIG_ENDIAN;
    }
    return ByteOrder.BIG_ENDIAN;
  }

  public static Domain getDomainType(final String value) {
    if (value.equalsIgnoreCase(Domain.TIME.getTitle())) {
      return Domain.TIME;
    } else if (value.equalsIgnoreCase(Domain.DISTANCE.getTitle())) {
      return Domain.DISTANCE;
    }
    return Domain.DISTANCE;
  }

}
