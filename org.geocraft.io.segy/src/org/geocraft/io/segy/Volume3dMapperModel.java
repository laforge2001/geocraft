/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.beans.PropertyChangeEvent;
import java.io.File;
import java.nio.ByteOrder;

import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;
import org.geocraft.ui.io.UnitPreferencesValidity;
import org.osgi.service.prefs.Preferences;


public class Volume3dMapperModel extends VolumeMapperModel {

  public static final String X0 = "X0 Coordinate";

  public static final String Y0 = "Y0 Coordinate";

  public static final String X1 = "X1 Coordinate";

  public static final String Y1 = "Y1 Coordinate";

  public static final String X2 = "X2 Coordinate";

  public static final String Y2 = "Y2 Coordinate";

  public static final String X3 = "X3 Coordinate";

  public static final String Y3 = "Y3 Coordinate";

  private DoubleProperty _x0;

  private DoubleProperty _y0;

  private DoubleProperty _x1;

  private DoubleProperty _y1;

  private DoubleProperty _x2;

  private DoubleProperty _y2;

  private DoubleProperty _x3;

  private DoubleProperty _y3;

  public Volume3dMapperModel() {
    super();
    _x0 = addDoubleProperty(X0, 0.0);
    _y0 = addDoubleProperty(Y0, 0.0);
    _x1 = addDoubleProperty(X1, 0.0);
    _y1 = addDoubleProperty(Y1, 0.0);
    _x2 = addDoubleProperty(X2, 0.0);
    _y2 = addDoubleProperty(Y2, 0.0);
    _x3 = addDoubleProperty(X3, 0.0);
    _y3 = addDoubleProperty(Y3, 0.0);
  }

  public Volume3dMapperModel(final Volume3dMapperModel model) {
    super(model);

    _x0 = addDoubleProperty(X0, model.getX0());
    _y0 = addDoubleProperty(Y0, model.getY0());
    _x1 = addDoubleProperty(X1, model.getX1());
    _y1 = addDoubleProperty(Y1, model.getY1());
    _x2 = addDoubleProperty(X2, model.getX2());
    _y2 = addDoubleProperty(Y2, model.getY2());
    _x3 = addDoubleProperty(X3, model.getX3());
    _y3 = addDoubleProperty(Y3, model.getY3());

  }

  public double getX0() {
    return _x0.get();
  }

  public void setX0(final double x0) {
    _x0.set(x0);
  }

  public double getY0() {
    return _y0.get();
  }

  public void setY0(final double y0) {
    _y0.set(y0);
  }

  public double getX1() {
    return _x1.get();
  }

  public void setX1(final double x1) {
    _x1.set(x1);
  }

  public double getY1() {
    return _y1.get();
  }

  public void setY1(final double y1) {
    _y1.set(y1);
  }

  public double getX2() {
    return _x2.get();
  }

  public void setX2(final double x2) {
    _x2.set(x2);
  }

  public double getY2() {
    return _y2.get();
  }

  public void setY2(final double y2) {
    _y2.set(y2);
  }

  public double getX3() {
    return _x3.get();
  }

  public void setX3(final double x3) {
    _x3.set(x3);
  }

  public double getY3() {
    return _y3.get();
  }

  public void setY3(final double y3) {
    _y3.set(y3);
  }

  @Override
  public String getUniqueId() {
    return getDirectory() + File.separator + getFileName() + getFileExtension();
  }

  @Override
  public void updateUniqueId(final String name) {
    setFileName(name);
  }

  /**
   * @param event  
   */

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    if (event.getPropertyName().equals(VolumeMapperModel.UNIT_OF_XY)) {
      Unit oldUnitOfXY = (Unit) event.getOldValue();
      Unit newUnitOfXY = getUnitOfXY();
      if (newUnitOfXY == null || !newUnitOfXY.equals(Unit.FOOT) && !newUnitOfXY.equals(Unit.METER)
          && !newUnitOfXY.equals(Unit.US_SURVEY_FOOT)) {
        return;
      }
      if (oldUnitOfXY == null || oldUnitOfXY == Unit.UNDEFINED) {
        oldUnitOfXY = newUnitOfXY;
      }
      double x0 = Unit.convert(getX0(), oldUnitOfXY, newUnitOfXY);
      double y0 = Unit.convert(getY0(), oldUnitOfXY, newUnitOfXY);
      double x1 = Unit.convert(getX1(), oldUnitOfXY, newUnitOfXY);
      double y1 = Unit.convert(getY1(), oldUnitOfXY, newUnitOfXY);
      double x2 = Unit.convert(getX2(), oldUnitOfXY, newUnitOfXY);
      double y2 = Unit.convert(getY2(), oldUnitOfXY, newUnitOfXY);
      double x3 = Unit.convert(getX3(), oldUnitOfXY, newUnitOfXY);
      double y3 = Unit.convert(getY3(), oldUnitOfXY, newUnitOfXY);
      setX0(x0);
      setY0(y0);
      setX1(x1);
      setY1(y1);
      setX2(x2);
      setY2(y2);
      setX3(x3);
      setY3(y3);
    }
  }

  @Override
  public boolean isRestoreable(final Preferences prefs) {
    if (new File(prefs.get(UNIQUE_ID, "")).canRead()) {
      return true;
    }
    ServiceProvider.getLoggingService().getLogger(getClass())
        .warn(prefs.name() + "'s uniqueId was inconsistent with the saved file location. Skipping...");
    return false;
  }

  public void validate(IValidation result) {
    // Check the volume type.
    IndexType volumeType = getVolumeType();
    if (volumeType == null || !(volumeType.equals(IndexType.POSTSTACK_3D) || volumeType.equals(IndexType.PRESTACK_3D))) {
      result.error(VolumeMapperModel.VOLUME_TYPE, "Volume Type must be PostStack3d or PreStack3d");
    }

    // Check the directory.
    String directory = getDirectory();
    if (directory == null || directory.length() == 0) {
      result.error(VolumeMapperModel.DIRECTORY, "Directory must be specified.");
    } else {
      File dir = new File(directory);
      if (!dir.exists()) {
        result.error(VolumeMapperModel.DIRECTORY, "Directory does not exist.");
      }
      if (!dir.isDirectory()) {
        result.error(VolumeMapperModel.DIRECTORY, "Directory is not a directory.");
      }
      if (!dir.canRead()) {
        result.error(VolumeMapperModel.DIRECTORY, "Directory is not readable.");
      }
      if (!dir.canWrite()) {
        result.error(VolumeMapperModel.DIRECTORY, "Directory is not writable.");
      }
    }

    // Check the file name.
    String fileName = getFileName();
    if (fileName == null || fileName.length() == 0) {
      result.error(VolumeMapperModel.FILE_NAME, "File name must be defined.");
    }

    // Check the file extension.
    String fileExtn = getFileExtension();
    if (fileExtn == null || fileExtn.length() == 0) {
      result.error(VolumeMapperModel.FILE_EXTN, "File exension must be defined.");
    }

    IOMode ioMode = getIOMode();
    if (ioMode != null && ioMode.equals(IOMode.OUTPUT)) {
      String fullPath = directory + File.separator + fileName + fileExtn;
      if (new File(fullPath).exists()) {
        result.warning(VolumeMapperModel.FILE_NAME, "File named '" + fullPath + "' exists and will be overwritten");
      }
    }

    // Check the sample rate.
    float sampleRate = getSampleRate();
    if (sampleRate <= 0) {
      result.error(VolumeMapperModel.SAMPLE_RATE, "Sample rate must be positive.");
    }

    // Check the sample format.
    String sampleFormat = getSampleFormat();
    if (sampleFormat != null) {
      int sampleFormatCode = SegyUtil.getSampleFormatCode(sampleFormat);
      if (sampleFormatCode == 0) {
        result.error(VolumeMapperModel.SAMPLE_FORMAT, "Invalid sample format.");
      }
    } else {
      result.error(VolumeMapperModel.SAMPLE_FORMAT, "Sample format must be defined.");
    }

    // Check the byte order.
    String byteOrderStr = getByteOrder();
    if (byteOrderStr == null || !byteOrderStr.equals(ByteOrder.nativeOrder().toString())) {
      result.error(VolumeMapperModel.BYTE_ORDER, "Byte order must be the native order");
    }

    // TODO: Check the storage order.

    // Check the unit of z.
    Unit zUnit = getUnitOfZ();
    if (zUnit == null || zUnit.equals(Unit.UNDEFINED)) {
      result.error(VolumeMapperModel.UNIT_OF_Z, "Z unit must be specified.");
    }

    UnitPreferencesValidity.checkZUnitOfMeasurement(result, VolumeMapperModel.UNIT_OF_Z, zUnit);

    // Check the unit of x,y.
    Unit xyUnit = getUnitOfXY();
    if (xyUnit == null || xyUnit.equals(Unit.UNDEFINED)) {
      result.error(VolumeMapperModel.UNIT_OF_XY, "X,Y unit must be specified.");
    } else {
      if (!xyUnit.equals(Unit.METER) && !xyUnit.equals(Unit.FOOT) && !xyUnit.equals(Unit.US_SURVEY_FOOT)) {
        result.error(VolumeMapperModel.UNIT_OF_XY, "X,Y unit must be foot or meter.");
      }
    }

    UnitPreferencesValidity.checkXYUnitOfMeasurement(result, VolumeMapperModel.UNIT_OF_XY, xyUnit);

    // Check the data unit.
    Unit dataUnit = getDataUnit();
    if (dataUnit == null || dataUnit.equals(Unit.UNDEFINED)) {
      result.error(VolumeMapperModel.DATA_UNIT, "Data unit must be specified.");
    }

    // Check the inline byte location.
    int inlineByteLoc = getInlineByteLoc();
    if (inlineByteLoc < 1 || inlineByteLoc > 237) {
      result.error(VolumeMapperModel.INLINE_BYTE_LOC, "Inline Byte Location must be between 1 and 237.");
    }

    // Check the xline byte location.
    int xlineByteLoc = getXlineByteLoc();
    if (xlineByteLoc < 1 || xlineByteLoc > 237) {
      result.error(VolumeMapperModel.XLINE_BYTE_LOC, "Xline Byte Location must be between 1 and 237.");
    }

    // Check the x-coordinate byte location.
    int xcoordByteLoc = getXcoordByteLoc();
    if (xcoordByteLoc < 1 || xcoordByteLoc > 237) {
      result.error(VolumeMapperModel.X_COORD_BYTE_LOC, "X-Coord. Byte Location must be between 1 and 237.");
    }

    // Check the y-coordinate byte location.
    int ycoordByteLoc = getYcoordByteLoc();
    if (ycoordByteLoc < 1 || ycoordByteLoc > 237) {
      result.error(VolumeMapperModel.Y_COORD_BYTE_LOC, "Y-Coord. Byte Location must be between 1 and 237.");
    }
  }
}
