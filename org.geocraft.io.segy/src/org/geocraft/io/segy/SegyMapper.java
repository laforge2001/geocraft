/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.io.IOException;

import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.service.ServiceProvider;


/**
 * Defines the abstract base class for SEG-Y mappers.
 * This class contains all the methods that are common to PostStack2d,
 * PostStack3d, PreStack2d and PreStack3d SEG-Y mappers.
 */
public abstract class SegyMapper<E extends SeismicDataset> extends AbstractMapper<E> {

  /** The logger. */

  /** The model of mapper properties. */
  protected VolumeMapperModel _model;

  /** The SEG-Y file accessor. */
  protected SegyFileAccessor _fileAccessor;

  /** The header definition. */
  protected HeaderDefinition _headerDef;

  public SegyMapper(final VolumeMapperModel model) {
    _model = model;
    _fileAccessor = new SegyFileAccessor(_model);
  }

  /**
   * Gets the store properties of the SEG-Y file.
   * 
   * @return the store properties of the SEG-Y file.
   */
  public VolumeMapperModel getModel() {
    if (_model instanceof Volume2dMapperModel) {
      return new Volume2dMapperModel((Volume2dMapperModel) _model);
    } else if (_model instanceof Volume3dMapperModel) {
      return new Volume3dMapperModel((Volume3dMapperModel) _model);
    }
    throw new IllegalArgumentException("Invalid model: " + _model);
  }

  @Override
  public VolumeMapperModel getInternalModel() {
    return _model;
  }

  /**
   * Create a SEG-Y file on disk.
   * This method will only write the SEG-Y EBCDIC and binary header information.
   * The actual traces are written separately using methods such as putTraces(), etc.
   * 
   * @param entity the mapped volume.
   */
  @Override
  public void createInStore(final E seismicDataset) throws IOException {
    String filePath = getFilePath();
    File file = new File(filePath);
    if (!file.createNewFile()) {
      throw new IOException("The file \'" + filePath + "\' already exists.");
    }
  }

  @Override
  public void updateInStore(final E seismicDataset) {
    Domain storeDomain = getFileAccessor().getDomain();
    if (storeDomain == null || !storeDomain.equals(seismicDataset.getZDomain())) {
      // Need to switch the store domain type to match the seismic dataset.
      getFileAccessor().setDomain(seismicDataset.getZDomain());
      if (storeDomain.equals(Domain.TIME)) {
        _model.setUnitOfZ(Unit.MILLISECONDS);
      } else if (storeDomain.equals(Domain.DISTANCE)) {
        _model.setUnitOfZ(UnitPreferences.getInstance().getVerticalDistanceUnit());
        if (_model.getUnitOfZ().equals(Unit.UNDEFINED)) {
          _model.setUnitOfZ(Unit.METER);
        }
      }
    }
    float deltaZ = seismicDataset.getZDelta();
    try {
      _model.setSampleRate(Unit.convert(deltaZ, seismicDataset.getZUnit(), _model.getUnitOfZ()));
    } catch (Exception ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error("Error converting z-units", ex);
    }
    // Check that the directory exists.
    File dir = new File(_model.getDirectory());
    if (!dir.exists()) {
      throw new IllegalArgumentException("The directory does not exist.");
    }
    // Check the file path.
    File file = new File(getFilePath());
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IllegalArgumentException("The path represents a directory, not a SEG-Y file.");
      }
    }
    // Update the EBCDIC and binary headers.
    SegyEbcdicHeader ebcdicHeader = getFileAccessor().getEbcdicHeader();
    SegyBinaryHeader binaryHeader = getFileAccessor().getBinaryHeader();
    ebcdicHeader.set(seismicDataset.getComment());
    binaryHeader.putShort(SegyBinaryHeaderCatalog.SAMPLE_FORMAT_CODE, (short) getFileAccessor().getSampleFormatCode());
    binaryHeader.putShort(SegyBinaryHeaderCatalog.MEASUREMENT_SYSTEM,
        (short) SegyUtil.getMeasurementSystemCode(_model.getUnitOfXY()));
    binaryHeader.putShort(SegyBinaryHeaderCatalog.SAMPLE_INTERVAL, (short) (_model.getSampleRate() * 1000));
    binaryHeader.putShort(SegyBinaryHeaderCatalog.SAMPLES_PER_TRACE, (short) seismicDataset.getNumSamplesPerTrace());

    // If volume is 2D, put the line number in the binary header.
    if (seismicDataset instanceof PostStack2dLine) {
      binaryHeader.putInteger(SegyBinaryHeaderCatalog.LINE_NUM, _model.getInlineStart());
    }

    SeismicSurvey3d survey = null;
    if (seismicDataset instanceof PostStack3d) {
      survey = ((PostStack3d) seismicDataset).getSurvey();
    } else if (seismicDataset instanceof PreStack3d) {
      survey = ((PreStack3d) seismicDataset).getSurvey();
    }
    if (seismicDataset instanceof PostStack3d || seismicDataset instanceof PreStack3d) {
      BinGridDefinitionStanza binGrid = new BinGridDefinitionStanza();
      binGrid.put(BinGridStanzaEntry.BinGridName, seismicDataset.getDisplayName() + " geometry");
      binGrid.put(BinGridStanzaEntry.AlternateIAxisDescription, TraceHeaderCatalog.INLINE_NO.getName());
      binGrid.put(BinGridStanzaEntry.AlternateJAxisDescription, TraceHeaderCatalog.XLINE_NO.getName());
      binGrid.put(BinGridStanzaEntry.BinGridOriginICoordinate, "" + survey.getInlineStart());
      binGrid.put(BinGridStanzaEntry.BinGridOriginJCoordinate, "" + survey.getXlineStart());
      int numRows = survey.getNumRows();
      int numCols = survey.getNumColumns();
      double[] xy00 = survey.transformRowColToXY(0, 0);
      double[] xy01 = survey.transformRowColToXY(0, numCols - 1);
      double[] xy10 = survey.transformRowColToXY(numRows - 1, 0);
      double distance1 = Math.sqrt(Math.pow(xy00[0] - xy01[0], 2) + Math.pow(xy00[1] - xy01[1], 2));
      double distance2 = Math.sqrt(Math.pow(xy00[0] - xy10[0], 2) + Math.pow(xy00[1] - xy10[1], 2));
      double inlineSpacing = distance1 / (numCols - 1);
      double xlineSpacing = distance2 / (numRows - 1);
      binGrid.put(BinGridStanzaEntry.BinGridOriginEasting, "" + xy00[0]);
      binGrid.put(BinGridStanzaEntry.BinGridOriginNorthing, "" + xy00[1]);
      binGrid.put(BinGridStanzaEntry.ScaleFactorOfBinGrid, "1.0");
      binGrid.put(BinGridStanzaEntry.ScaleFactorNodeICoordinate, "1.0");
      binGrid.put(BinGridStanzaEntry.ScaleFactorNodeJCoordinate, "1.0");
      binGrid.put(BinGridStanzaEntry.NormalBinWidthOnIAxis, "" + inlineSpacing);
      binGrid.put(BinGridStanzaEntry.NormalBinWidthOnJAxis, "" + xlineSpacing);
      binGrid.put(BinGridStanzaEntry.GridBearingOfBinGridJAxis, "20");
      binGrid.put(BinGridStanzaEntry.GridBearingUnitName, Unit.DEGREE_OF_AN_ANGLE.getName());
      binGrid.put(BinGridStanzaEntry.BinNodeIncrementOnIAxis, "" + survey.getInlineDelta());
      binGrid.put(BinGridStanzaEntry.BinNodeIncrementOnJAxis, "" + survey.getXlineDelta());
      //      binGrid.put(BinGridStanzaEntry.FirstCheckNodeICoordinate, "334.0");
      //      binGrid.put(BinGridStanzaEntry.FirstCheckNodeJCoordinate, "235.0");
      //      binGrid.put(BinGridStanzaEntry.FirstCheckNodeEasting, "465602.94");
      //      binGrid.put(BinGridStanzaEntry.FirstCheckNodeNorthing, "5836624.30");
      //      binGrid.put(BinGridStanzaEntry.SecondCheckNodeICoordinate, "1352.0");
      //      binGrid.put(BinGridStanzaEntry.SecondCheckNodeJCoordinate, "955.0");
      //      binGrid.put(BinGridStanzaEntry.SecondCheckNodeEasting, "492591.98");
      //      binGrid.put(BinGridStanzaEntry.SecondCheckNodeNorthing, "5836377.16");
      //      binGrid.put(BinGridStanzaEntry.ThirdCheckNodeICoordinate, "605.0");
      //      binGrid.put(BinGridStanzaEntry.ThirdCheckNodeJCoordinate, "955.0");
      //      binGrid.put(BinGridStanzaEntry.ThirdCheckNodeEasting, "475046.03");
      //      binGrid.put(BinGridStanzaEntry.ThirdCheckNodeNorthing, "5842763.36");
    }

    try {
      // Write the EBCDIC and binary headers.
      SegyUtil.writeEbcdicHeader(getFilePath(), ebcdicHeader);
      SegyUtil.writeBinaryHeader(getFilePath(), binaryHeader);
    } catch (IOException e) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(e.toString());
      return;
    }
    ServiceProvider.getLoggingService().getLogger(getClass()).info("SEG-Y file \'" + getFilePath() + "\' updated.");
  }

  /**
   * Returns the reference to the SEG-Y file accessor.
   */
  protected SegyFileAccessor getFileAccessor() {
    if (_fileAccessor == null) {
      _fileAccessor = new SegyFileAccessor(_model);
    }
    return _fileAccessor;
  }

  //  /**
  //   * Forces a synchronization of the open/close.
  //   * Upon closing, it will also create the trace index file if none exists.
  //   * 
  //   * @param operation the I/O operation (Open or Close).
  //   */
  //  protected void openclose(final SegyFileAccessor.OpenClose operation) {
  //    switch (operation) {
  //      case CLOSE:
  //        getFileAccessor().close();
  //        break;
  //      case OPEN_FOR_READ:
  //        getFileAccessor().openForRead();
  //        break;
  //      case OPEN_FOR_WRITE:
  //        getFileAccessor().openForWrite();
  //        break;
  //    }
  //  }

  /**
   * Closes the SEG-Y file from I/O access.
   * 
   * @param seismicDataset the seismicDataset.
   */
  public void close() {
    getFileAccessor().close();
  }

  /**
   * Returns the full path of the SEG-Y file on disk.
   */
  protected String getFilePath() {
    return _model.getFilePath();
  }

  public StorageFormat getStorageFormat() {
    String sampleFormat = _model.getSampleFormat();
    int sampleFormatCode = SegyUtil.getSampleFormatCode(sampleFormat);
    switch (sampleFormatCode) {
      case SegyBytes.SAMPLE_FORMAT_CODE_FIXED_1BYTE:
        return StorageFormat.INTEGER_08;
      case SegyBytes.SAMPLE_FORMAT_CODE_FIXED_2BYTE:
        return StorageFormat.INTEGER_16;
      case SegyBytes.SAMPLE_FORMAT_CODE_FIXED_4BYTE:
        return StorageFormat.INTEGER_32;
      case SegyBytes.SAMPLE_FORMAT_CODE_FIXED_4BYTE_WITH_GAIN:
        return StorageFormat.INTEGER_32;
      case SegyBytes.SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IBM:
        return StorageFormat.FLOAT_32;
      case SegyBytes.SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IEEE:
        return StorageFormat.FLOAT_32;
      default:
        throw new RuntimeException("Invalid SEG-Y sample format: " + sampleFormat);
    }
  }

  public void setStorageFormat(final StorageFormat storageFormat) {
    int sampleFormatCode = SegyBytes.SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IEEE;
    if (storageFormat.equals(StorageFormat.INTEGER_08)) {
      sampleFormatCode = SegyBytes.SAMPLE_FORMAT_CODE_FIXED_1BYTE;
    } else if (storageFormat.equals(StorageFormat.INTEGER_16)) {
      sampleFormatCode = SegyBytes.SAMPLE_FORMAT_CODE_FIXED_2BYTE;
    } else if (storageFormat.equals(StorageFormat.INTEGER_32)) {
      sampleFormatCode = SegyBytes.SAMPLE_FORMAT_CODE_FIXED_4BYTE;
    } else if (storageFormat.equals(StorageFormat.FLOAT_32)) {
      sampleFormatCode = SegyBytes.SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IEEE;
    }
    _model.setSampleFormat(SegyUtil.getSampleFormatString(sampleFormatCode));
    getFileAccessor().setSampleFormatCode();
  }

  /**
   * Returns the synchronization token used to block.
   */
  protected Object getSynchronizeToken() {
    return getFileAccessor().getSynchronizeToken();
  }

  protected void deleteFromStore() {
    // Delete the file, if it exists.
    if (_fileAccessor != null) {
      _fileAccessor.close();
      _fileAccessor.delete();
      _fileAccessor = null;
    }
  }

  public void setDomain(Domain domain) {
    _model.setDomain(domain);
    getFileAccessor().setDomain(domain);
  }

  @Override
  public String getStorageDirectory() {
    return _model.getDirectory();
  }

  /**
   * @throws IOException  
   */
  @Override
  public void reinitialize() throws IOException {
    getFileAccessor().reinitialize();
  }
}
