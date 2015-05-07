/*
 * Copyright (C) ConocoPhillips 2228 All Rights Reserved. 
 */
package org.geocraft.io.javaseis;


import java.io.File;

import org.geocraft.core.model.datatypes.PolygonUtil;
import org.geocraft.core.model.datatypes.PolygonUtil.PolygonType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.LongProperty;
import org.geocraft.core.model.property.StringArrayProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;


public class VolumeMapperModel extends MapperModel {

  public static final String VOLUME_TYPE = "Volume Type";

  public static final String DIRECTORY = "Directory";

  public static final String FILE_NAME = "File Name";

  public static final String STORAGE_ORDER = "Storage Order";

  public static final String UNIT_OF_XY = "X,Y Unit";

  public static final String UNIT_OF_Z = "Z Unit";

  public static final String DATA_UNIT = "Data Unit";

  public static final String DATA_FORMAT = "Data Format";

  public static final String INLINE_START = "Inline Start";

  public static final String INLINE_END = "Inline End";

  public static final String INLINE_DELTA = "Inline Delta";

  public static final String XLINE_START = "Xline Start";

  public static final String XLINE_END = "Xline End";

  public static final String XLINE_DELTA = "Xline Delta";

  public static final String OFFSET_START = "Offset Start";

  public static final String OFFSET_END = "Offset End";

  public static final String OFFSET_DELTA = "Offset Delta";

  public static final String X0 = "X0";

  public static final String Y0 = "Y0";

  public static final String X1 = "X1";

  public static final String Y1 = "Y1";

  public static final String X2 = "X2";

  public static final String Y2 = "Y2";

  public static final String BIN_GRID_EXISTS = "BinGrid Exists";

  public static final String USE_SECONDARY_STORAGE = "Use Secondary Storage?";

  public static final String SECONDARY_STORAGE_LOCATIONS = "Secondary Storage to Use";

  public static final String NUM_EXTENTS = "# of Extents";

  public static final String CORNER_POINTS_DIRECTION = "Corner Points Direction";

  private StringProperty _volumeType;

  private StringProperty _directory;

  private StringProperty _fileName;

  private StringProperty _storageOrder;

  private EnumProperty<Unit> _unitOfXY;

  private EnumProperty<Unit> _unitOfZ;

  private EnumProperty<Unit> _dataUnit;

  private StringProperty _dataFormat;

  private LongProperty _inlineStart;

  private LongProperty _inlineEnd;

  private LongProperty _inlineDelta;

  private LongProperty _xlineStart;

  private LongProperty _xlineEnd;

  private LongProperty _xlineDelta;

  private FloatProperty _offsetStart;

  private FloatProperty _offsetEnd;

  private FloatProperty _offsetDelta;

  private DoubleProperty _x0;

  private DoubleProperty _y0;

  private DoubleProperty _x1;

  private DoubleProperty _y1;

  private DoubleProperty _x2;

  private DoubleProperty _y2;

  private BooleanProperty _binGridExists;

  private BooleanProperty _secnStorageFlag;

  private StringArrayProperty _secnStorageLocations;

  private IntegerProperty _numExtents;

  private EnumProperty<PolygonUtil.PolygonType> _cornerPointDirection;

  public VolumeMapperModel() {
    _volumeType = addStringProperty(VOLUME_TYPE, "");
    _directory = addStringProperty(DIRECTORY, "");
    _fileName = addStringProperty(FILE_NAME, "");
    _storageOrder = addStringProperty(STORAGE_ORDER, "");
    _unitOfZ = addEnumProperty(UNIT_OF_Z, Unit.class, Unit.UNDEFINED);
    _dataUnit = addEnumProperty(DATA_UNIT, Unit.class, Unit.UNDEFINED);
    _dataFormat = addStringProperty(DATA_FORMAT, "");
    _inlineStart = addLongProperty(INLINE_START, 0);
    _inlineEnd = addLongProperty(INLINE_END, 0);
    _inlineDelta = addLongProperty(INLINE_DELTA, 1);
    _xlineStart = addLongProperty(XLINE_START, 0);
    _xlineEnd = addLongProperty(XLINE_END, 0);
    _xlineDelta = addLongProperty(XLINE_DELTA, 1);
    _offsetStart = addFloatProperty(OFFSET_START, 0);
    _offsetEnd = addFloatProperty(OFFSET_END, 0);
    _offsetDelta = addFloatProperty(OFFSET_DELTA, 1);
    _unitOfXY = addEnumProperty(UNIT_OF_XY, Unit.class, Unit.UNDEFINED);
    _x0 = addDoubleProperty(X0, 0);
    _y0 = addDoubleProperty(Y0, 0);
    _x1 = addDoubleProperty(X1, 0);
    _y1 = addDoubleProperty(Y1, 0);
    _x2 = addDoubleProperty(X2, 0);
    _y2 = addDoubleProperty(Y2, 0);
    _binGridExists = addBooleanProperty(BIN_GRID_EXISTS, false);
    _secnStorageFlag = addBooleanProperty(USE_SECONDARY_STORAGE, true);
    _secnStorageLocations = addStringArrayProperty(SECONDARY_STORAGE_LOCATIONS);
    _numExtents = addIntegerProperty(NUM_EXTENTS, 10);
    _cornerPointDirection = addEnumProperty(CORNER_POINTS_DIRECTION, PolygonType.class, PolygonType.Clockwise);
  }

  public VolumeMapperModel(final VolumeMapperModel model) {
    this();
    updateFrom(model);
  }

  public void setCornerPointDirection(PolygonType direction) {
    _cornerPointDirection.set(direction);
  }

  public PolygonType getCornerPointDirection() {
    return _cornerPointDirection.get();
  }

  public String getVolumeType() {
    return _volumeType.get();
  }

  public void setVolumeType(final String volumeType) {
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

  public String getStorageOrder() {
    return _storageOrder.get();
  }

  public void setStorageOrder(final String storageOrder) {
    _storageOrder.set(storageOrder);
  }

  public Unit getUnitOfXY() {
    return _unitOfXY.get();
  }

  public void setUnitOfXY(final Unit unitOfXY) {
    _unitOfXY.set(unitOfXY);
  }

  public Unit getUnitOfZ() {
    return _unitOfZ.get();
  }

  public void setUnitOfZ(final Unit unitOfZ) {
    _unitOfZ.set(unitOfZ);
  }

  public Unit getDataUnit() {
    return _dataUnit.get();
  }

  public void setDataUnit(final Unit dataUnit) {
    _dataUnit.set(dataUnit);
  }

  public String getDataFormat() {
    return _dataFormat.get();
  }

  public void setDataFormat(final String dataFormat) {
    _dataFormat.set(dataFormat);
  }

  public long getInlineStart() {
    return _inlineStart.get();
  }

  public void setInlineStart(final long inlineStart) {
    _inlineStart.set(inlineStart);
  }

  public long getInlineEnd() {
    return _inlineEnd.get();
  }

  public void setInlineEnd(final long inlineEnd) {
    _inlineEnd.set(inlineEnd);
  }

  public long getInlineDelta() {
    return _inlineDelta.get();
  }

  public void setInlineDelta(final long inlineDelta) {
    _inlineDelta.set(inlineDelta);
  }

  public long getXlineStart() {
    return _xlineStart.get();
  }

  public void setXlineStart(final long xlineStart) {
    _xlineStart.set(xlineStart);
  }

  public long getXlineEnd() {
    return _xlineEnd.get();
  }

  public void setXlineEnd(final long xlineEnd) {
    _xlineEnd.set(xlineEnd);
  }

  public long getXlineDelta() {
    return _xlineDelta.get();
  }

  public void setXlineDelta(final long xlineDelta) {
    _xlineDelta.set(xlineDelta);
  }

  public float getOffsetStart() {
    return _offsetStart.get();
  }

  public void setOffsetStart(final float offsetStart) {
    _offsetStart.set(offsetStart);
  }

  public float getOffsetEnd() {
    return _offsetEnd.get();
  }

  public void setOffsetEnd(final float offsetEnd) {
    _offsetEnd.set(offsetEnd);
  }

  public float getOffsetDelta() {
    return _offsetDelta.get();
  }

  public void setOffsetDelta(final float offsetDelta) {
    _offsetDelta.set(offsetDelta);
  }

  public double getX0() {
    return _x0.get();
  }

  public void setX0(final double x) {
    _x0.set(x);
  }

  public double getY0() {
    return _y0.get();
  }

  public void setY0(final double y) {
    _y0.set(y);
  }

  public double getX1() {
    return _x1.get();
  }

  public void setX1(final double x) {
    _x1.set(x);
  }

  public double getY1() {
    return _y1.get();
  }

  public void setY1(final double y) {
    _y1.set(y);
  }

  public double getX2() {
    return _x2.get();
  }

  public void setX2(final double x) {
    _x2.set(x);
  }

  public double getY2() {
    return _y2.get();
  }

  public void setY2(final double y) {
    _y2.set(y);
  }

  public boolean getBinGridExists() {
    return _binGridExists.get();
  }

  public void setBinGridExists(final boolean binGridExists) {
    _binGridExists.set(binGridExists);
  }

  public boolean getSecondaryStorageFlag() {
    return _secnStorageFlag.get();
  }

  public void setSecondaryStorageFlag(boolean useSecondaryStorage) {
    _secnStorageFlag.set(useSecondaryStorage);
  }

  public String[] getVirtualFoldersLoc() {
    return _secnStorageLocations.get();
  }

  public void setVirtualFoldersLoc(String[] virtualFolders) {
    _secnStorageLocations.set(virtualFolders);
  }

  public int getNumExtents() {
    return _numExtents.get();
  }

  public void setNumExtents(int numExtents) {
    _numExtents.set(numExtents);
  }

  @Override
  public String getUniqueId() {
    return getDirectory() + File.separator + getFileName();
  }

  @Override
  public void updateUniqueId(final String name) {
    setFileName(name);
  }

  @Override
  public boolean existsInStore() {
    return existsInStore(getFileName());
  }

  @Override
  public boolean existsInStore(final String name) {
    File file = new File(getDirectory() + File.separator + name);
    return file.exists() && file.canRead();
  }

  public void validate(IValidation results) {
    // Check the volume type.
    String volumeType = getVolumeType();
    if (volumeType == null || !volumeType.equals("PostStack3d") && !volumeType.equals("PreStack3d")) {
      results.error(VOLUME_TYPE, "Volume Type must be PostStack3d or PreStack3d");
    }

    String directory = getDirectory();
    if (directory == null || directory.length() == 0) {
      results.error(DIRECTORY, "Directory must be specified.");
    } else {
      File dir = new File(directory);
      if (!dir.exists()) {
        results.error(DIRECTORY, "Directory does not exist.");
      }
      if (!dir.isDirectory()) {
        results.error(DIRECTORY, "Directory is not a directory.");
      }
      if (!dir.canRead()) {
        results.error(DIRECTORY, "Directory is not readable.");
      }
      //      if (!dir.canWrite()) {
      //        validation.add(createError(JavaSeisDIRECTORY, "Directory is not writable."));
      //      }
    }

    String fileName = getFileName();
    if (fileName == null || fileName.length() == 0) {
      results.error(FILE_NAME, "File name must be defined.");
    }

    IOMode ioMode = getIOMode();
    if (ioMode != null && ioMode.equals(IOMode.OUTPUT)) {
      String fullPath = directory + File.separator + fileName;
      if (new File(fullPath).exists()) {
        results.warning(FILE_NAME, "Volume named '" + fullPath + "' exists and will be overwritten");
      }
    }

    // Check the data unit.
    Unit dataUnit = getDataUnit();
    if (dataUnit == null || dataUnit.equals(Unit.UNDEFINED)) {
      results.error(DATA_UNIT, "Data unit must be specified.");
    }

    String dataFormat = getDataFormat();
    if (dataFormat == null || dataFormat.length() == 0) {
      results.error(DATA_FORMAT, "Data format must be specified.");
    }

    // Check that at least 1 virtual folder is specified.
    if (_secnStorageFlag.get()) {
      if (_secnStorageLocations.isEmpty()) {
        results.error(SECONDARY_STORAGE_LOCATIONS, "No virtual folders location specified.");
      } else {
        for (String path : _secnStorageLocations.get()) {
          File dir = new File(path);
          if (!dir.isDirectory()) {
            results.error(SECONDARY_STORAGE_LOCATIONS, "Virtual folder location is not a directory: " + path);
          } else if (!dir.canWrite()) {
            results.error(SECONDARY_STORAGE_LOCATIONS, "Virtual folder location is not writable: " + path);
          }
        }
      }

      // Check that at least 1 extent is specified.
      if (_numExtents.get() < 1) {
        results.error(NUM_EXTENTS, "No extents specified.");
      }
    } else {
      results.warning(USE_SECONDARY_STORAGE,
          "The use of secondary storage is recommended to prevent filling up local directories with large datasets!");
    }
  }
}
