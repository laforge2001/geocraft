/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;


/**
 * A class for indexing SEG-Y index files, as either 2D or 3D.
 * 2D files are indexed by CDP #.
 * 3D files are indexex by Inline,X-line #.
 */
public class SegyTraceIndexer extends BackgroundTask {

  /** The full path of the SEG-Y file. */
  private final String _filePath;

  /** The type of index (POSTSTACK_3D, PRESTACK_3D, etc). */
  private final IndexType _indexType;

  /** The SEG-Y volume mapper model. */
  private final VolumeMapperModel _mapperModel;

  /** The CDP # byte location (for 2D only). */
  private final int _cdpByteLoc;

  /** The inline # byte location (for 3D only). */
  private final int _inlineByteLoc;

  /** The x-line # byte location (for 3D only). */
  private final int _xlineByteLoc;

  /** The offset byte location. */
  private final int _offsetByteLoc;

  /** The SEG-Y sample format code. */
  private int _sampleFormatCode;

  private final List<SegyTraceIndexerListener> _listeners;

  public static SegyTraceIndexer createPostStack2dIndexer(final VolumeMapperModel mapperModel) {
    return new SegyTraceIndexer(mapperModel, IndexType.POSTSTACK_2D);
  }

  public static SegyTraceIndexer createPostStack3dIndexer(final VolumeMapperModel mapperModel) {
    return new SegyTraceIndexer(mapperModel, IndexType.POSTSTACK_3D);
  }

  public static SegyTraceIndexer createPreStack3dIndexer(final VolumeMapperModel mapperModel) {
    return new SegyTraceIndexer(mapperModel, IndexType.PRESTACK_3D);
  }

  /**
   * Constructs a SEG-Y trace indexer.
   */
  public SegyTraceIndexer(final VolumeMapperModel mapperModel, final IndexType type) {
    _mapperModel = mapperModel;
    _filePath = mapperModel.getDirectory() + File.separator + mapperModel.getFileName()
        + mapperModel.getFileExtension();
    _indexType = type;
    _sampleFormatCode = SegyUtil.getSampleFormatCode(_mapperModel.getSampleFormat());
    _cdpByteLoc = mapperModel.getCdpByteLoc();
    _inlineByteLoc = mapperModel.getInlineByteLoc();
    _xlineByteLoc = mapperModel.getXlineByteLoc();
    _offsetByteLoc = mapperModel.getOffsetByteLoc();
    boolean is2D = type.equals(IndexType.POSTSTACK_2D) || type.equals(IndexType.PRESTACK_2D);
    boolean is3D = type.equals(IndexType.POSTSTACK_3D) || type.equals(IndexType.PRESTACK_3D);
    boolean isStacked = type.equals(IndexType.POSTSTACK_2D) || type.equals(IndexType.POSTSTACK_3D);
    if (is2D && (_cdpByteLoc < 1 || _cdpByteLoc > 237)) {
      throw new RuntimeException("Invalid CDP byte location: " + _cdpByteLoc);
    }
    if (is3D && (_inlineByteLoc < 1 || _inlineByteLoc > 237)) {
      throw new RuntimeException("Invalid inline byte location: " + _inlineByteLoc);
    }
    if (is3D && (_xlineByteLoc < 1 || _xlineByteLoc > 237)) {
      throw new RuntimeException("Invalid x-line byte location: " + _xlineByteLoc);
    }
    if (!isStacked && (_offsetByteLoc < 1 || _offsetByteLoc > 237)) {
      throw new RuntimeException("Invalid offset byte location: " + _offsetByteLoc);
    }
    _listeners = new ArrayList<SegyTraceIndexerListener>();
  }

  /**
   * Does the background indexing task.
   * @return the generated trace map.
   */
  @Override
  public Object compute(final ILogger logger, final IProgressMonitor monitor) {
    String shortName = FileUtil.getShortName(_filePath);
    monitor.beginTask("Indexing SEG-Y file: " + shortName, 100);
    SegyTraceIndexModel traceIndexModel = null;
    try {
      // Map the traces, to allow access by row-column, inline-xline, etc.
      HeaderDefinition traceHeaderDef = SegyTraceHeader.PRESTACK3D_HEADER_DEF;

      // Open the random-access-file.
      RandomAccessFile randomAccessFile = new RandomAccessFile(_filePath, "r");
      FileChannel fileChannel = randomAccessFile.getChannel();
      long fileLength = fileChannel.size();

      long position = 0;

      // Read the EBCDIC and binary headers.
      SegyEbcdicHeader ebcdicHeader = new SegyEbcdicHeader();
      SegyBinaryHeader binaryHeader = new SegyBinaryHeader();
      ebcdicHeader.getBuffer().position(0);
      binaryHeader.getBuffer().position(0);

      monitor.subTask("Reading EBCDIC header...");
      monitor.worked(2);
      fileChannel.read(ebcdicHeader.getBuffer(), position);
      position += ebcdicHeader.getSize();
      monitor.subTask("Reading binary header...");
      monitor.worked(2);
      fileChannel.read(binaryHeader.getBuffer(), position);
      binaryHeader.updateHeaderFromBuffer();

      // Skip over the Binary Header and any extended Textual File Headers.
      int numExtendedHeaders = binaryHeader.getShort(SegyBinaryHeaderCatalog.NUMBER_OF_EXTENDED_HEADERS);
      if (numExtendedHeaders == 0) {
        position += SegyBinaryHeader.SEGY_BINARY_HEADER_SIZE;
      } else if (numExtendedHeaders > 0) {
        position += SegyBinaryHeader.SEGY_BINARY_HEADER_SIZE + numExtendedHeaders * 3200;
      } else {
        throw new UnsupportedOperationException("Variable # of extended textual headers not yet supported.");
      }
      fileChannel.position(position);
      long fullHeaderSize = position;

      int sampleCountGlobal = 0;
      int numBytesPerSample = 4;
      try {
        // Gets the sample format, sample count and sample rate from the binary header.
        if (_sampleFormatCode == 0) {
          _sampleFormatCode = binaryHeader.getShort(SegyBinaryHeaderCatalog.SAMPLE_FORMAT_CODE);
        }
        sampleCountGlobal = binaryHeader.getShort(SegyBinaryHeaderCatalog.SAMPLES_PER_TRACE);

        if (_sampleFormatCode == 3) {
          numBytesPerSample = 2;
        } else if (_sampleFormatCode == 8) {
          numBytesPerSample = 1;
        }

        long dataLength = fileLength - fullHeaderSize;
        if (dataLength > 0) {
          SegyTraceHeader traceHeader = new SegyTraceHeader();
          traceHeader.getBuffer().position(0);
          fileChannel.read(traceHeader.getBuffer(), position);
          traceHeader.getBuffer().position(0);
          traceHeader.updateHeaderFromBuffer();
        }

      } catch (Exception ex) {
        randomAccessFile.close();
        throw new IOException("Error opening volume: " + _filePath + "\n" + ex.toString());
      }
      fileChannel.position(fullHeaderSize);

      monitor.subTask("Initializing trace map...");
      monitor.worked(1);

      // Initialize a trace map for indexing by CDP #.
      traceIndexModel = getTraceIndexModel(shortName);
      if (traceIndexModel == null) {
        randomAccessFile.close();
        return null;
      }

      // Scan and index the traces.
      boolean hasTraces = fileLength > fullHeaderSize;
      int[] traceKeyVals = new int[3];
      if (hasTraces) {
        int count = 0;
        SegyTraceHeader traceHeader = new SegyTraceHeader(traceHeaderDef);
        monitor.subTask("Scanning traces...");
        monitor.worked(5);
        int percentComplete = 10;
        while (position < fileLength && (monitor == null || !monitor.isCanceled())) {
          int oldPercentComplete = percentComplete;
          percentComplete = 10 + (int) (50 * (float) position / fileLength);
          int work = percentComplete - oldPercentComplete;
          monitor.worked(work);
          traceHeader.getBuffer().position(0);
          fileChannel.read(traceHeader.getBuffer(), position);
          traceHeader.getBuffer().position(0);
          traceHeader.updateHeaderFromBuffer();
          int sampleCountLocal = traceHeader.getShort(SegyTraceHeaderCatalog.NUM_SAMPLES);
          int cdp = traceHeader.getFromBufferAsInt(_cdpByteLoc - 1);
          int inline = traceHeader.getFromBufferAsInt(_inlineByteLoc - 1);
          int xline = traceHeader.getFromBufferAsInt(_xlineByteLoc - 1);
          int offset = traceHeader.getFromBufferAsInt(_offsetByteLoc - 1);

          // If local sample count == 0, then assume the trace header has not been filled properly
          // and default to the sample count from the binary header.
          if (sampleCountLocal == 0) {
            sampleCountLocal = sampleCountGlobal;
          }
          if (sampleCountLocal > 0) {
            switch (_indexType) {
              case POSTSTACK_2D:
                // Add the CDP number to the trace header definition.
                traceKeyVals[0] = cdp;
                if (traceKeyVals[0] != 0) {
                  System.out.println("Storing " + traceKeyVals[0]);
                  traceIndexModel.storeTrace(traceKeyVals, position);
                }
                break;
              case POSTSTACK_3D:
                // Add the inline and crossline numbers to the trace header definition.
                traceKeyVals[0] = inline;
                traceKeyVals[1] = xline;
                if (traceKeyVals[0] != 0 && traceKeyVals[1] != 0) {
                  traceIndexModel.storeTrace(traceKeyVals, position);
                }
                break;
              case PRESTACK_3D:
                // Add the inline, crossline and offset numbers to the trace header definition.
                traceKeyVals[0] = inline;
                traceKeyVals[1] = xline;
                traceKeyVals[2] = offset;
                if (traceKeyVals[0] != 0 && traceKeyVals[1] != 0) {
                  traceIndexModel.storeTrace(traceKeyVals, position);
                }
                break;
              default:
                throw new RuntimeException("The index type \'" + _indexType + "\' is not supported.");
            }
          }
          position += SegyTraceHeader.SEGY_TRACE_HEADER_SIZE;
          position += sampleCountLocal * numBytesPerSample;
          fileChannel.position(position);
          count++;
        }

        // Map the traces.
        monitor.subTask("Mapping traces for " + traceIndexModel.getName() + "...");
        monitor.worked(20);
        traceIndexModel.mapTraces();
      }
      randomAccessFile.close();

      // Check if the progress is canceled.
      if (monitor.isCanceled()) {
        logger.warn("Warning: The task to create the trace index file was cancelled.");
        return null;
      }

      // Write the trace index file.
      monitor.subTask("Writing trace index file...");
      monitor.worked(15);
      buildTraceIndex(traceIndexModel);
    } catch (Exception ex) {
      logger.error("Error: " + ex.toString(), ex);
    }
    monitor.worked(5);
    monitor.done();
    notifyListeners(traceIndexModel);
    return traceIndexModel;
  }

  public void buildTraceIndex(final SegyTraceIndexModel traceIndexModel) throws Exception {
    switch (_indexType) {
      case POSTSTACK_2D:
        new SegyTraceIndex(_filePath + ".ndx", IndexType.POSTSTACK_2D, traceIndexModel).close();
        break;
      case POSTSTACK_3D:
        new SegyTraceIndex(_filePath + ".ndx", IndexType.POSTSTACK_3D, traceIndexModel).close();
        break;
      case PRESTACK_3D:
        new SegyTraceIndex(_filePath + ".ndx", IndexType.PRESTACK_3D, traceIndexModel).close();
        break;
      default:
        throw new RuntimeException("The index type \'" + _indexType + "\' is not supported.");
    }
  }

  private SegyTraceIndexModel getTraceIndexModel(final String shortName) {
    SegyTraceIndexModel traceIndexModel = null;

    switch (_indexType) {
      case POSTSTACK_2D:
        // Initialize a trace map for indexing by cdp #.
        traceIndexModel = new SegyTraceIndexModel(shortName + ".2D", "CDP", _cdpByteLoc);
        break;
      case POSTSTACK_3D:
        // Initialize a trace map for indexing by inline,crossline.
        traceIndexModel = new SegyTraceIndexModel(shortName + ".3D", new String[] { "INLINE", "XLINE" }, new int[] {
            _inlineByteLoc, _xlineByteLoc });
        break;
      case PRESTACK_3D:
        // Initialize a trace map for indexing by inline,crossline,offset.
        traceIndexModel = new SegyTraceIndexModel(shortName + ".3D", new String[] { "INLINE", "XLINE", "OFFSET" },
            new int[] { _inlineByteLoc, _xlineByteLoc, _offsetByteLoc });
        break;
      default:
        throw new RuntimeException("The index type \'" + _indexType + "\' is not supported.");
    }

    return traceIndexModel;
  }

  public void addListener(final SegyTraceIndexerListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(final SegyTraceIndexerListener listener) {
    _listeners.remove(listener);
  }

  protected void notifyListeners(final SegyTraceIndexModel traceIndexModel) {
    SegyTraceIndexerListener[] listeners = new SegyTraceIndexerListener[_listeners.size()];
    for (int i = 0; i < listeners.length; i++) {
      listeners[i] = _listeners.get(i);
    }
    for (SegyTraceIndexerListener listener : listeners) {
      listener.tracesIndexed(_mapperModel, traceIndexModel);
    }
  }
}
