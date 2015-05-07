/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.nio.ByteOrder;

import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;
import org.geocraft.ui.io.UnitPreferencesValidity;


public class Volume2dMapperModel extends VolumeMapperModel {

  public static final String CDP_START = "CDP Start";

  public static final String CDP_END = "CDP End";

  public static final String CDP_DELTA = "CDP Delta";

  private IntegerProperty _cdpStart;

  private IntegerProperty _cdpEnd;

  private IntegerProperty _cdpDelta;

  public Volume2dMapperModel() {
    super();
    _cdpStart = addIntegerProperty(CDP_START, 0);
    _cdpEnd = addIntegerProperty(CDP_END, 0);
    _cdpDelta = addIntegerProperty(CDP_DELTA, 0);
  }

  public Volume2dMapperModel(Volume2dMapperModel model) {
    this();
    setCdpStart(model.getCdpStart());
    setCdpEnd(model.getCdpEnd());
    setCdpDelta(model.getCdpDelta());
  }

  public int getCdpStart() {
    return _cdpStart.get();
  }

  public void setCdpStart(int cdpStart) {
    _cdpStart.set(cdpStart);
  }

  public int getCdpEnd() {
    return _cdpEnd.get();
  }

  public void setCdpEnd(int cdpEnd) {
    _cdpEnd.set(cdpEnd);
  }

  public int getCdpDelta() {
    return _cdpDelta.get();
  }

  public void setCdpDelta(int cdpDelta) {
    _cdpDelta.set(cdpDelta);
  }

  public void validate(IValidation result) {
    // Check the volume type.
    IndexType volumeType = getVolumeType();
    if (volumeType == null || !volumeType.equals(IndexType.POSTSTACK_2D)) {
      result.error(VolumeMapperModel.VOLUME_TYPE, "Volume Type must be PostStack2d");
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

    // Check the CDP byte location.
    int cdpByteLoc = getCdpByteLoc();
    if (cdpByteLoc < 1 || cdpByteLoc > 237) {
      result.error(VolumeMapperModel.CDP_BYTE_LOC, "CDP Byte Location must be between 1 and 237.");
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
