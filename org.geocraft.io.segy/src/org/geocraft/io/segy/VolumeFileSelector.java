/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;

import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.session.MapperParameterStore;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;
import org.geocraft.ui.io.DatastoreFileSelector;


/**
 * The class for handling the selection of SEG-Y volume files on disk.
 */
public class VolumeFileSelector extends DatastoreFileSelector {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(VolumeFileSelector.class);

  /** The default volume type. */
  private IndexType _volumeType;

  /**
   * Constructs a file selector for choosing 2D or 3D SEG-Y files on disk.
   * @param volumeType the default volume type.
   */
  public VolumeFileSelector(IndexType volumeType) {
    super("SEG-Y Volume", new String[] { "SEG-Y Files (.segy)", "SEG-Y Files (.sgy)" }, new String[] { "*.segy",
        "*.sgy" }, "LoadSegyVolume_DIR");
    _volumeType = volumeType;
  }

  @Override
  protected MapperModel[] createMapperModelsFromSelectedFiles(final File[] files) {
    int numFiles = files.length;
    VolumeMapperModel[] mapperModels = new VolumeMapperModel[numFiles];
    for (int i = 0; i < numFiles; i++) {
      mapperModels[i] = createMapperModel(files[i], _volumeType);
    }
    return mapperModels;
  }

  /**
   * Scans the specified SEG-Y file and returns a PostStack3d mapper model of datastore properties.
   * 
   * @param file the SEG-Y file.
   * @return the PostStack3d mapper model of datastore properties.
   */
  public static Volume3dMapperModel createPostStack3dMapperModel(final File file) {
    return (Volume3dMapperModel) createMapperModel(file, IndexType.POSTSTACK_3D);
  }

  /**
   * Scans the specified SEG-Y file and returns a mapper model of datastore properties.
   * 
   * @param file the SEG-Y file.
   * @return the mapper model of datastore properties.
   */
  public static VolumeMapperModel createMapperModel(final File file, IndexType volumeType) {
    // Create a new mapper model.
    VolumeMapperModel model = null;
    if (volumeType.equals(IndexType.POSTSTACK_2D) || volumeType.equals(IndexType.PRESTACK_2D)) {
      model = new Volume2dMapperModel();
    } else if (volumeType.equals(IndexType.POSTSTACK_3D) || volumeType.equals(IndexType.PRESTACK_3D)) {
      model = new Volume3dMapperModel();
    } else {
      throw new IllegalArgumentException("Unsupported volume type: " + volumeType);
    }
    String filePath = file.getAbsolutePath();
    model.setDirectory(FileUtil.getPathName(filePath));
    model.setFileName(FileUtil.getBaseName(filePath));
    model.setFileExtension(FileUtil.getFileExtension(filePath));

    // Update the mapper model from SEG-Y binary header.
    float sampleRate = 1;
    ByteOrder byteOrder = ByteOrder.nativeOrder();
    int sampleFormatCode = 0;
    int measurementSysCode = 0;
    Unit dataUnit = Unit.SEISMIC_AMPLITUDE;
    Unit xyUnit = Unit.UNDEFINED;
    StorageOrder storageOrder = StorageOrder.AUTO_CALCULATED;
    int numExtendedHeaders = 0;
    // Read the binary header for sample rate and sample format code.
    try {
      SegyBinaryHeader binaryHeader = SegyUtil.readBinaryHeader(filePath);
      numExtendedHeaders = binaryHeader.getShort(SegyBinaryHeaderCatalog.NUMBER_OF_EXTENDED_HEADERS);
      sampleRate = binaryHeader.getShort(SegyBinaryHeaderCatalog.SAMPLE_INTERVAL);
      sampleRate /= 1000;
      sampleFormatCode = binaryHeader.getShort(SegyBinaryHeaderCatalog.SAMPLE_FORMAT_CODE);
      measurementSysCode = binaryHeader.getShort(SegyBinaryHeaderCatalog.MEASUREMENT_SYSTEM);
      xyUnit = SegyUtil.getHorizontalDistanceUnit(measurementSysCode);
      // Swap the bytes and check result.
      int ibitsOrig = sampleFormatCode;
      int ibitsSwap = ibitsOrig << 24 | ibitsOrig >> 24 & 0xff | (ibitsOrig & 0xff00) << 8
          | (ibitsOrig & 0xff0000) >> 8;
      boolean origResultOk = ibitsOrig >= 0 && ibitsOrig <= 8;
      boolean swapResultOk = ibitsSwap >= 0 && ibitsSwap <= 8;
      // If the swapped result is ok but the origin is not, then the file byte order
      // does not match the native order and byte-swapping is needed.
      if (swapResultOk && !origResultOk) {
        if (byteOrder.equals(ByteOrder.LITTLE_ENDIAN)) {
          byteOrder = ByteOrder.BIG_ENDIAN;
        } else if (byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
          byteOrder = ByteOrder.LITTLE_ENDIAN;
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error scanning binary header for SEG-Y file: " + filePath, e);
    }
    // Read the 1st trace header for x,y coordinates.
    try {
      SegyTraceHeader traceHeader = SegyUtil.readFirstTraceHeader(filePath, numExtendedHeaders);
      int xyUnitsCode = traceHeader.getShort(SegyTraceHeaderCatalog.COORDINATE_UNITS);
      if (xyUnitsCode == 2) {
        xyUnit = Unit.SECONDS_ANGULAR;
        LOGGER.error("Unsupported coordinate units: seconds of arc.");
      } else if (xyUnitsCode != 1) {
        LOGGER.warn("Undefined coordinate units. Assuming length (meters or feet).");
      }
    } catch (Exception e) {
      LOGGER.error("Error scanning 1st trace header for SEG-Y file: " + filePath);
    }

    model.setSampleRate(sampleRate);
    model.setDataUnit(dataUnit);
    model.setUnitOfXY(xyUnit);
    model.setUnitOfZ(Unit.MILLISECONDS);
    model.setStorageOrder(storageOrder.getTitle());
    model.setCdpByteLoc(21);

    // Restore the previously specified settings.
    MapperParameterStore.restore(model);

    model.setByteOrder(ByteOrder.nativeOrder().toString());
    model.setSampleFormat(SegyUtil.getSampleFormatString(sampleFormatCode));
    model.setVolumeType(volumeType);
    model.setAutoCalculateGeometry(true);

    return model;
  }

}
