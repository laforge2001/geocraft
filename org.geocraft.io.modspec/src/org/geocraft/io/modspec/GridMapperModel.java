package org.geocraft.io.modspec;


import java.io.File;
import java.io.IOException;

import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;
import org.geocraft.ui.io.UnitPreferencesValidity;
import org.osgi.service.prefs.Preferences;


/**
 * The model of mapper properties for ModSpec grids.
 * The model extends the abstract mapper model class and is a bean.
 */
public class GridMapperModel extends MapperModel implements ModSpecGridConstants {

  public static final String DIRECTORY = "Directory";

  public static final String FILE_NAME = "File Name";

  public static final String XY_UNIT = "X,Y Unit";

  public static final String DATA_UNIT = "Data Unit";

  public static final String ONSET_TYPE = "Onset Type";

  public static final String FILE_FORMAT = "File Format";

  public static final String ORIENTATION = "Orientation";

  /** The directory in which the ModSpec grid file is located. */
  private final StringProperty _directory;

  /** The name of the ModSpec grid file. */
  private final StringProperty _fileName;

  /** The unit of measurement of the x,y coordinates in the ModSpec grid file. */
  private final EnumProperty<Unit> _xyUnit;

  /** The unit of measurement of the data in the ModSpec grid file. */
  private final EnumProperty<Unit> _dataUnit;

  /** The onset type of the data in the ModSpec grid file. */
  private final EnumProperty<OnsetType> _onsetType;

  /** The format of the ModSpec grid file (ASCII or Binary). */
  private final EnumProperty<GridFileFormat> _fileFormat;

  /** The row,col <-> x,y orientation of the ModSpec grid file. */
  //private EnumProperty<GridOrientation> _orientation;

  /**
   * Create a model with default values.
   */
  public GridMapperModel() {
    _directory = addStringProperty(DIRECTORY, "");
    _fileName = addStringProperty(FILE_NAME, "");
    _xyUnit = addEnumProperty(XY_UNIT, Unit.class, Unit.UNDEFINED);
    _dataUnit = addEnumProperty(DATA_UNIT, Unit.class, Unit.UNDEFINED);
    _onsetType = addEnumProperty(ONSET_TYPE, OnsetType.class, OnsetType.MINIMUM);
    _fileFormat = addEnumProperty(FILE_FORMAT, GridFileFormat.class, null);
    //_orientation = addEnumProperty(ORIENTATION, GridOrientation.class, null);
  }

  public GridMapperModel(final GridMapperModel model) {
    this();
    updateFrom(model);
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

  public Unit getXyUnit() {
    return _xyUnit.get();
  }

  public void setXyUnit(final Unit xyUnit) {
    _xyUnit.set(xyUnit);
  }

  public Unit getDataUnit() {
    return _dataUnit.get();
  }

  public void setDataUnit(final Unit dataUnit) {
    _dataUnit.set(dataUnit);
  }

  public OnsetType getOnsetType() {
    return _onsetType.get();
  }

  public void setOnsetType(final OnsetType onsetType) {
    _onsetType.set(onsetType);
  }

  public GridFileFormat getFileFormat() {
    return _fileFormat.get();
  }

  public void setFileFormat(final GridFileFormat fileFormat) {
    _fileFormat.set(fileFormat);
  }

  //public GridOrientation getOrientation() {
  //  return _orientation.get();
  //}

  //public void setOrientation(final GridOrientation orientation) {
  //  _orientation.set(orientation);
  //}

  @Override
  public String getUniqueId() {
    return getDirectory() + File.separator + getFileName() + GRID_FILE_EXTN;
  }

  @Override
  public void updateUniqueId(final String name) {
    setFileName(name);
  }

  @Override
  public boolean isRestoreable(final Preferences prefs) {
    if (!new File(prefs.get(UNIQUE_ID, "")).canRead()) {
      ServiceProvider.getLoggingService().getLogger(getClass())
          .warn(prefs.name() + "'s uniqueId was inconsistent with the saved file location. Skipping...");
      return false;
    }

    try {
      GridFileFormat fileFormat = GridFileSelector.getFileFormat(prefs.get(UNIQUE_ID, ""));
      if (!fileFormat.toString().equals(prefs.get(FILE_FORMAT, ""))) {
        ServiceProvider.getLoggingService().getLogger(getClass())
            .warn("File format does not equal what is stored in meta-data.");
        return false;
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  public void validate(final IValidation results) {
    if (_directory.isEmpty()) {
      results.error(GridMapperModel.DIRECTORY, "Directory must be specified.");
    } else {
      File dir = new File(_directory.get());
      if (!dir.exists()) {
        results.error(GridMapperModel.DIRECTORY, "Directory does not exist.");
      }
      if (!dir.isDirectory()) {
        results.error(GridMapperModel.DIRECTORY, "Directory is not a directory.");
      }
      if (!dir.canRead()) {
        results.error(GridMapperModel.DIRECTORY, "Directory is not readable.");
      }
      if (!dir.canWrite()) {
        results.error(GridMapperModel.DIRECTORY, "Directory is not writable.");
      }
    }

    if (_fileName.isEmpty()) {
      results.error(GridMapperModel.FILE_NAME, "File name must be defined.");
    }

    Unit xyUnit = _xyUnit.get();
    if (xyUnit == null || xyUnit.equals(Unit.UNDEFINED)) {
      results.error(GridMapperModel.XY_UNIT, "X,Y unit must be specified.");
    } else {
      if (!xyUnit.equals(Unit.FOOT) && !xyUnit.equals(Unit.METER) && !xyUnit.equals(Unit.US_SURVEY_FOOT)) {
        results.error(GridMapperModel.XY_UNIT, "X,Y unit must be foot or meter.");
      }
    }

    UnitPreferencesValidity.checkXYUnitOfMeasurement(results, GridMapperModel.XY_UNIT, xyUnit);

    Unit dataUnit = _dataUnit.get();
    if (dataUnit == null || dataUnit.equals(Unit.UNDEFINED)) {
      results.error(GridMapperModel.DATA_UNIT, "Data unit must be specified.");
    }

    if (_onsetType.isNull()) {
      results.error(GridMapperModel.ONSET_TYPE, "Onset type must be specified.");
    }

    UnitPreferencesValidity.checkZUnitOfMeasurement(results, GridMapperModel.DATA_UNIT, dataUnit);

    if (_fileFormat.isNull()) {
      results.error(GridMapperModel.FILE_FORMAT, "File format must be specified.");
    }

    //if (_orientation.isNull()) {
    //  results.error(GridMapperModel.ORIENTATION, "Orientation must be specified.");
    //}

    IOMode ioMode = getIOMode();
    if (ioMode.equals(IOMode.INPUT)) {
      try {
        GridFileFormat fileFormatTest = GridFileSelector.getFileFormat(getUniqueId());
        if (!fileFormatTest.equals(getFileFormat())) {
          results.warning(GridMapperModel.FILE_FORMAT, "File format meta data doesn't match what's in the file.");
        }
      } catch (IOException e) {
        // file may not exist yet because we are exporting
        //e.printStackTrace();
      }
    }

    if (ioMode.equals(IOMode.OUTPUT)) {
      String fullPath = _directory.get() + File.separator + _fileName.get() + ModSpecGridConstants.GRID_FILE_EXTN;
      if (new File(fullPath).exists()) {
        results.warning(GridMapperModel.FILE_NAME, "Grid named '" + fullPath + "' exists and will be overwritten");
      }
    }
  }

  @Override
  public boolean existsInStore() {
    return existsOnDisk(getFilePath());
  }

  @Override
  public boolean existsInStore(final String name) {
    return existsOnDisk(_directory.get() + File.separator + name + GRID_FILE_EXTN);
  }

  public String getFilePath() {
    return _directory.get() + File.separator + _fileName.get() + GRID_FILE_EXTN;
  }

  /**
   * Returns <i>true</i> if the ModSpec grid file exists on disk; <i>false</i> if not.
   * 
   * @param filePath the full file path of the ModSpec grid file to check.
   * @return <i>true</i> if the ModSpec grid file exists on disk; <i>false</i> if not.
   */
  private boolean existsOnDisk(final String filePath) {
    File file = new File(filePath);
    return file.exists() && file.canRead();
  }
}
