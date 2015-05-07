/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.ByteOrder;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;
import org.osgi.service.prefs.Preferences;


/**
 * The model bean used by SEG-Y volume mappers.
 * For SEG-Y, the following parameters are potentially editable on input or output.
 * 1) Volume Type : currently this is a string that identifies to the type of volume (e.g. PostStack3d, etc).
 * 2) Directory : the directory in which the SEG-Y file resides.
 * 3) File Name : the base name of the SEG-Y file.
 * 4) File Extension : the extension of the SEG-Y file (e.g. .segy, .sgy).
 * 5) Storage Order : the storage order of the SEG-Y file (e.g. Inline-Xline, Xline-Inline, etc).
 * 6) Sample Rate : the sample rate (needed because of SEG-Y limitations).
 * 7) Unit of Z : the unit of measurement for z (needed because of SEG-Y limitations).
 * 8) Unit of X,Y : the unit of measurement for x,y (needed because of poorly composed SEG-Y files).
 * 9) Data Domain : the domain of the unit of measurement for data (will eventually become not needed).
 * 10) Data Unit : the unit of measurement for the data (needed because of SEG-Y limitations).
 * 11) Byte Order : the byte order (???).
 * 12) Sample Format : the sample format.
 * 13) CDP Byte Loc : the CDP byte location.
 * 14) Inline Byte Loc : the inline byte location.
 * 15) Xline Byte Loc : the xline byte location.
 * 16) X-Coord Byte Loc : the x-coordinate byte location.
 * 17) Y-Coord Byte Loc : the y-coordinate byte location.
 */
public abstract class VolumeMapperModel extends MapperModel implements PropertyChangeListener {

  public static final String VOLUME_TYPE = "Volume Type";

  public static final String DIRECTORY = "Directory";

  public static final String FILE_NAME = "File Name";

  public static final String FILE_EXTN = "FileExtension";

  public static final String STORAGE_ORDER = "Storage Order";

  public static final String SAMPLE_RATE = "Sample Rate";

  public static final String UNIT_OF_Z = "Z Unit";

  public static final String UNIT_OF_XY = "X,Y Unit";

  public static final String DATA_UNIT = "Data Unit";

  public static final String BYTE_ORDER = "Byte Order";

  public static final String SAMPLE_FORMAT = "Sample Format";

  public static final String CDP_BYTE_LOC = "CDP # Byte Loc.";

  public static final String INLINE_BYTE_LOC = "Inline # Byte Loc.";

  public static final String XLINE_BYTE_LOC = "Xline # Byte Loc.";

  public static final String OFFSET_BYTE_LOC = "Offset # Byte Loc.";

  public static final String X_COORD_BYTE_LOC = "X-Coordinate Byte Loc.";

  public static final String Y_COORD_BYTE_LOC = "Y-Coordinate Byte Loc.";

  public static final String INLINE_START = "Inline Start";

  public static final String INLINE_END = "Inline End";

  public static final String INLINE_DELTA = "Inline Delta";

  public static final String XLINE_START = "Xline Start";

  public static final String XLINE_END = "Xline End";

  public static final String XLINE_DELTA = "Xline Delta";

  public static final String AUTO_CALCULATE_GEOMETRY = "Auto Calculate Geometry?";

  private EnumProperty<IndexType> _volumeType;

  private StringProperty _directory;

  private StringProperty _fileName;

  private StringProperty _fileExtension;

  private StringProperty _storageOrder;

  private FloatProperty _sampleRate;

  private EnumProperty<Unit> _unitOfZ;

  private EnumProperty<Unit> _unitOfXY;

  private EnumProperty<Unit> _dataUnit;

  private StringProperty _byteOrder;

  private StringProperty _sampleFormat;

  private IntegerProperty _cdpByteLoc;

  private IntegerProperty _inlineByteLoc; // 181;

  private IntegerProperty _xlineByteLoc; // 185;

  private IntegerProperty _offsetByteLoc;

  private IntegerProperty _xcoordByteLoc; // 81;

  private IntegerProperty _ycoordByteLoc; // 85;

  private BooleanProperty _autoCalculateGeometry;

  private IntegerProperty _inlineStart;

  private IntegerProperty _inlineEnd;

  private IntegerProperty _inlineDelta;

  private IntegerProperty _xlineStart;

  private IntegerProperty _xlineEnd;

  private IntegerProperty _xlineDelta;

  public VolumeMapperModel() {
    super();
    _volumeType = addEnumProperty(VOLUME_TYPE, IndexType.class, IndexType.POSTSTACK_3D);
    _directory = addStringProperty(DIRECTORY, "");
    _fileName = addStringProperty(FILE_NAME, "");
    _fileExtension = addStringProperty(FILE_EXTN, "");
    _storageOrder = addStringProperty(STORAGE_ORDER, "");
    _sampleRate = addFloatProperty(SAMPLE_RATE, 0f);
    _unitOfZ = addEnumProperty(UNIT_OF_Z, Unit.class, Unit.UNDEFINED);
    _unitOfXY = addEnumProperty(UNIT_OF_XY, Unit.class, Unit.UNDEFINED);
    _dataUnit = addEnumProperty(DATA_UNIT, Unit.class, Unit.UNDEFINED);
    _byteOrder = addStringProperty(BYTE_ORDER, ByteOrder.nativeOrder().toString());
    _sampleFormat = addStringProperty(SAMPLE_FORMAT, "");
    _cdpByteLoc = addIntegerProperty(CDP_BYTE_LOC, 21);
    _inlineByteLoc = addIntegerProperty(INLINE_BYTE_LOC, 189);
    _xlineByteLoc = addIntegerProperty(XLINE_BYTE_LOC, 193);
    _offsetByteLoc = addIntegerProperty(OFFSET_BYTE_LOC, 37);
    _xcoordByteLoc = addIntegerProperty(X_COORD_BYTE_LOC, 181);
    _ycoordByteLoc = addIntegerProperty(Y_COORD_BYTE_LOC, 185);
    _autoCalculateGeometry = addBooleanProperty(AUTO_CALCULATE_GEOMETRY, true);
    _inlineStart = addIntegerProperty(INLINE_START, 0);
    _inlineEnd = addIntegerProperty(INLINE_END, 0);
    _inlineDelta = addIntegerProperty(INLINE_DELTA, 0);
    _xlineStart = addIntegerProperty(XLINE_START, 0);
    _xlineEnd = addIntegerProperty(XLINE_END, 0);
    _xlineDelta = addIntegerProperty(XLINE_DELTA, 0);
  }

  public VolumeMapperModel(final VolumeMapperModel model) {
    this();
    _volumeType = addEnumProperty(VOLUME_TYPE, IndexType.class, model.getVolumeType());
    _directory = addStringProperty(DIRECTORY, model.getDirectory());
    _fileName = addStringProperty(FILE_NAME, model.getFileName());
    _fileExtension = addStringProperty(FILE_EXTN, model.getFileExtension());
    _storageOrder = addStringProperty(STORAGE_ORDER, model.getStorageOrder());
    _sampleRate = addFloatProperty(SAMPLE_RATE, model.getSampleRate());
    _unitOfZ = addEnumProperty(UNIT_OF_Z, Unit.class, model.getUnitOfZ());
    _unitOfXY = addEnumProperty(UNIT_OF_XY, Unit.class, model.getUnitOfXY());
    _dataUnit = addEnumProperty(DATA_UNIT, Unit.class, model.getDataUnit());
    _byteOrder = addStringProperty(BYTE_ORDER, model.getByteOrder());
    _sampleFormat = addStringProperty(SAMPLE_FORMAT, model.getSampleFormat());
    _cdpByteLoc = addIntegerProperty(CDP_BYTE_LOC, model.getCdpByteLoc());
    _inlineByteLoc = addIntegerProperty(INLINE_BYTE_LOC, model.getInlineByteLoc());
    _xlineByteLoc = addIntegerProperty(XLINE_BYTE_LOC, model.getXlineByteLoc());
    _offsetByteLoc = addIntegerProperty(OFFSET_BYTE_LOC, model.getOffsetByteLoc());
    _xcoordByteLoc = addIntegerProperty(X_COORD_BYTE_LOC, model.getXcoordByteLoc());
    _ycoordByteLoc = addIntegerProperty(Y_COORD_BYTE_LOC, model.getYcoordByteLoc());
    _autoCalculateGeometry = addBooleanProperty(AUTO_CALCULATE_GEOMETRY, model.getAutoCalculateGeometry());
    _inlineStart = addIntegerProperty(INLINE_START, model.getInlineStart());
    _inlineEnd = addIntegerProperty(INLINE_END, model.getInlineEnd());
    _inlineDelta = addIntegerProperty(INLINE_DELTA, model.getInlineDelta());
    _xlineStart = addIntegerProperty(XLINE_START, model.getXlineStart());
    _xlineEnd = addIntegerProperty(XLINE_END, model.getXlineEnd());
    _xlineDelta = addIntegerProperty(XLINE_DELTA, model.getXlineDelta());

  }

  public IndexType getVolumeType() {
    return _volumeType.get();
  }

  public void setVolumeType(final IndexType volumeType) {
    _volumeType.set(volumeType);
  }

  public String getDirectory() {
    return _directory.get();
  }

  public void setDirectory(final String directory) {
    _directory.set(directory);
  }

  public String getFileName() {
    return _fileName.get();
  }

  public void setFileName(final String fileName) {
    _fileName.set(fileName);
  }

  public String getFileExtension() {
    return _fileExtension.get();
  }

  public void setFileExtension(final String fileExtension) {
    _fileExtension.set(fileExtension);
  }

  public String getStorageOrder() {
    return _storageOrder.get();
  }

  public void setStorageOrder(final String storageOrder) {
    _storageOrder.set(storageOrder);
  }

  public float getSampleRate() {
    return _sampleRate.get();
  }

  public void setSampleRate(final float sampleRate) {
    _sampleRate.set(sampleRate);
  }

  public Unit getUnitOfZ() {
    return _unitOfZ.get();
  }

  public void setUnitOfZ(final Unit unitOfZ) {
    _unitOfZ.set(unitOfZ);
  }

  public Unit getUnitOfXY() {
    return _unitOfXY.get();
  }

  public void setUnitOfXY(final Unit unitOfXY) {
    _unitOfXY.set(unitOfXY);
  }

  public Unit getDataUnit() {
    return _dataUnit.get();
  }

  public void setDataUnit(final Unit dataUnit) {
    _dataUnit.set(dataUnit);
  }

  public String getByteOrder() {
    return _byteOrder.get();
  }

  public void setByteOrder(final String byteOrder) {
    _byteOrder.set(byteOrder);
  }

  public String getSampleFormat() {
    return _sampleFormat.get();
  }

  public void setSampleFormat(final String sampleFormat) {
    _sampleFormat.set(sampleFormat);
  }

  public int getCdpByteLoc() {
    return _cdpByteLoc.get();
  }

  public void setCdpByteLoc(final int cdpByteLoc) {
    _cdpByteLoc.set(cdpByteLoc);
  }

  public int getInlineByteLoc() {
    return _inlineByteLoc.get();
  }

  public void setInlineByteLoc(final int inlineByteLoc) {
    _inlineByteLoc.set(inlineByteLoc);
  }

  public int getXlineByteLoc() {
    return _xlineByteLoc.get();
  }

  public void setXlineByteLoc(final int xlineByteLoc) {
    _xlineByteLoc.set(xlineByteLoc);
  }

  public int getOffsetByteLoc() {
    return _offsetByteLoc.get();
  }

  public void setOffsetByteLoc(final int offsetByteLoc) {
    _offsetByteLoc.set(offsetByteLoc);
  }

  public int getXcoordByteLoc() {
    return _xcoordByteLoc.get();
  }

  public void setXcoordByteLoc(final int xcoordByteLoc) {
    _xcoordByteLoc.set(xcoordByteLoc);
  }

  public int getYcoordByteLoc() {
    return _ycoordByteLoc.get();
  }

  public void setYcoordByteLoc(final int ycoordByteLoc) {
    _ycoordByteLoc.set(ycoordByteLoc);
  }

  public int getInlineStart() {
    return _inlineStart.get();
  }

  public void setInlineStart(final int inlineStart) {
    _inlineStart.set(inlineStart);
  }

  public int getInlineEnd() {
    return _inlineEnd.get();
  }

  public void setInlineEnd(final int inlineEnd) {
    _inlineEnd.set(inlineEnd);
  }

  public int getInlineDelta() {
    return _inlineDelta.get();
  }

  public void setInlineDelta(final int inlineDelta) {
    _inlineDelta.set(inlineDelta);
  }

  public int getXlineStart() {
    return _xlineStart.get();
  }

  public void setXlineStart(final int xlineStart) {
    _xlineStart.set(xlineStart);
  }

  public int getXlineEnd() {
    return _xlineEnd.get();
  }

  public void setXlineEnd(final int xlineEnd) {
    _xlineEnd.set(xlineEnd);
  }

  public int getXlineDelta() {
    return _xlineDelta.get();
  }

  public void setXlineDelta(final int xlineDelta) {
    _xlineDelta.set(xlineDelta);
  }

  public boolean getAutoCalculateGeometry() {
    return _autoCalculateGeometry.get();
  }

  public void setAutoCalculateGeometry(final boolean autoCalculateGeometry) {
    _autoCalculateGeometry.set(autoCalculateGeometry);
  }

  @Override
  public String getUniqueId() {
    return getDirectory() + File.separator + getFileName() + getFileExtension();
  }

  @Override
  public void updateUniqueId(final String name) {
    setFileName(name);
  }

  public void setDomain(final Domain domain) {
    if (domain.equals(Domain.TIME)) {
      setUnitOfZ(Unit.MILLISECONDS);
    } else if (domain.equals(Domain.DISTANCE)) {
      setUnitOfZ(UnitPreferences.getInstance().getVerticalDistanceUnit());
    } else {
      throw new IllegalArgumentException("Invalid domain: " + domain + ".");
    }
  }

  @Override
  public boolean isRestoreable(final Preferences prefs) {
    if (new File(prefs.get(UNIQUE_ID, "")).canRead()) {
      return true;
    }
    ServiceProvider.getLoggingService().getLogger(getClass()).warn(
        prefs.name() + "'s uniqueId was inconsistent with the saved file location. Skipping...");
    return false;
  }

  @Override
  public boolean existsInStore() {
    return existsOnDisk(getFilePath());
  }

  @Override
  public boolean existsInStore(String name) {
    return existsOnDisk(_directory.get() + File.separator + name + _fileExtension.get());
  }

  public String getFilePath() {
    return _directory.get() + File.separator + _fileName.get() + _fileExtension.get();
  }

  /**
   * Returns <i>true</i> if the SEG-Y file exists on disk; <i>false</i> if not.
   * 
   * @param filePath the full file path of the SEG-Y file to check.
   * @return <i>true</i> if the SEG-Y file exists on disk; <i>false</i> if not.
   */
  private boolean existsOnDisk(final String filePath) {
    File file = new File(filePath);
    return file.exists() && file.canRead();
  }

  //  /** updates the field without triggering the property change event
  //   * @param inlineStart
  //   */
  //  public void updateInlineStart(final int inlineStart) {
  //    _inlineStart = inlineStart;
  //  }
  //
  //  /** updates the field without triggering the property change event
  //   * @param inlineEnd
  //   */
  //  public void updateInlineEnd(final int inlineEnd) {
  //    _inlineEnd = inlineEnd;
  //  }
  //
  //  /** updates the field without triggering the property change event
  //   * @param inlineDelta
  //   */
  //  public void updateInlineDelta(final int inlineDelta) {
  //    _inlineDelta = inlineDelta;
  //  }
  //
  //  /** updates the field without triggering the property change event
  //   * @param xlineStart
  //   */
  //
  //  public void updateXlineStart(final int xlineStart) {
  //    _xlineStart = xlineStart;
  //  }
  //
  //  /** updates the field without triggering the property change event
  //   * @param xlineEnd
  //   */
  //
  //  public void updateXlineEnd(final int xlineEnd) {
  //    _xlineEnd = xlineEnd;
  //  }
  //
  //  /** updates the field without triggering the property change event
  //   * @param xlineDelta
  //   */
  //
  //  public void updateXlineDelta(final int xlineDelta) {
  //    _xlineDelta = xlineDelta;
  //  }

}
