package org.geocraft.io.asciigrid;


import java.io.File;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.io.UnitPreferencesValidity;
import org.osgi.service.prefs.Preferences;


/**
 * The model of mapper properties for Ascii horizon grids. The model extends the abstract mapper model class and is a bean.
 */
public class AsciiHorizonMapperModel extends MapperModel implements AsciiFileConstants {

  // Register the exporting of horizon ascii data
  static {
    ExportHorizonAsciiData.register();
  }

  /** Enumeration for the Index type. */
  public enum IndexType {
    /* Depth data */
    DEPTH("Depth"),
    /* Time data */
    TIME("Time");

    private final String _displayName;

    IndexType(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  public static final String DIRECTORY = "Directory";

  public static final String FILE_NAME = "File Name";

  public static final String INDEX_TYPE = "Index Type";

  public static final String XY_UNITS = "X,Y Units";

  public static final String DATA_UNITS = "Data Units";

  public static final String ONSET_TYPE = "Onset Type";

  public static final String X_ORIGIN = "X Origin";

  public static final String Y_ORIGIN = "Y Origin";

  public static final String COL_SPACING = "Column Spacing";

  public static final String ROW_SPACING = "Row Spacing";

  public static final String NUM_OF_COLUMNS = "Number Of Columns";

  public static final String NUM_OF_ROWS = "Number Of Rows";

  public static final String PRIMARY_ANGLE = "Primary Angle";

  public static final String NULL_VALUE = "Null Value";

  public static final String STARTING_LINE_NUM = "Starting Line Number";

  public static final String NUM_OF_HORIZONS = "Number of Horizons";

  public static final String X_COLUMN_NUM = "X Column Number";

  public static final String Y_COLUMN_NUM = "Y Column Number";

  public static final String H1_COLUMN_NUM = "H1 Column Number";

  public static final String H2_COLUMN_NUM = "H2 Column Number";

  public static final String H3_COLUMN_NUM = "H3 Column Number";

  public static final String H4_COLUMN_NUM = "H4 Column Number";

  public static final String H5_COLUMN_NUM = "H5 Column Number";

  public static final String H1_NAME = "H1 Name";

  public static final String H2_NAME = "H2 Name";

  public static final String H3_NAME = "H3 Name";

  public static final String H4_NAME = "H4 Name";

  public static final String H5_NAME = "H5 Name";

  public static final String HORIZON1 = "Horizon1";

  public static final String HORIZON2 = "Horizon2";

  public static final String HORIZON3 = "Horizon3";

  public static final String HORIZON4 = "Horizon4";

  public static final String HORIZON5 = "Horizon5";

  public static final String AREA_OF_INTEREST = "Area of Interest";

  public static final String USE_AREA_OF_INTEREST = "Use Area Of Interest";

  public static final String ORIENTATION = "Orientation";

  /** The directory in which the Ascii horizon grid file is located. */
  private StringProperty _directory;

  /** The name of the Ascii Horizon file. */
  private StringProperty _fileName;

  /** The index Type */
  public final EnumProperty<IndexType> _indexType;

  /** The unit of measurement of the x,y coordinates in the Ascii horizon file. */
  private EnumProperty<Unit> _xyUnits;

  /** The Data Units (Units vary depending on whether the index type is depth or time */
  private EnumProperty<Unit> _dataUnits;

  /** The onset type of the data in the ModSpec grid file. */
  private EnumProperty<OnsetType> _onsetType;

  /** X Origin of the data in the Ascii horizon file */
  private DoubleProperty _xOrigin;

  /** Y Origin of the data in the Ascii horizon file */
  protected DoubleProperty _yOrigin;

  /** Column spacing of the data in the Ascii horizon file */
  protected DoubleProperty _colSpacing;

  /** Row spacing of the data in the Ascii horizon file */
  protected final DoubleProperty _rowSpacing;

  /** # of columns of the data in the Ascii horizon file */
  protected final IntegerProperty _numOfColumns;

  /** # of rows of the data in the Ascii horizon file */
  protected final IntegerProperty _numOfRows;

  /** Primary angle of the data in the Ascii horizon file */
  protected final DoubleProperty _primaryAngle;

  /** Null value of the data in the Ascii horizon file */
  protected final FloatProperty _nullValue;

  /** Starting line number. */
  private IntegerProperty _startingLineNum;

  /** Number of horizons. */
  public IntegerProperty _numOfHorizons;

  /** X Column number. */
  private IntegerProperty _xColumnNum;

  /** Y Column number. */
  private IntegerProperty _yColumnNum;

  /** H1 Column number. */
  private IntegerProperty _h1ColumnNum;

  /** H2 Column number. */
  private IntegerProperty _h2ColumnNum;

  /** H3 Column number. */
  private IntegerProperty _h3ColumnNum;

  /** H4 Column number. */
  private IntegerProperty _h4ColumnNum;

  /** H5 Column number. */
  private IntegerProperty _h5ColumnNum;

  /** H1 name. */
  private StringProperty _h1Name;

  /** H2 name. */
  private StringProperty _h2Name;

  /** H3 name. */
  private StringProperty _h3Name;

  /** H4 name. */
  private StringProperty _h4Name;

  /** H5 name. */
  private StringProperty _h5Name;

  /** Horizons to export */
  protected final EntityProperty<Grid3d> _horizon1;

  protected final EntityProperty<Grid3d> _horizon2;

  protected final EntityProperty<Grid3d> _horizon3;

  protected final EntityProperty<Grid3d> _horizon4;

  protected final EntityProperty<Grid3d> _horizon5;

  /** The use-area-of-interest property. */
  protected final BooleanProperty _useAreaOfInterest;

  /** The area-of-interest property. */
  protected final EntityProperty<AreaOfInterest> _areaOfInterest;

  /** The row,col <-> x,y orientation of the Ascii horizon file. */
  private EnumProperty<GridOrientation> _orientation;

  public AsciiHorizonMapperModel() {
    _directory = addStringProperty(DIRECTORY, "");
    _fileName = addStringProperty(FILE_NAME, "");
    _indexType = addEnumProperty(INDEX_TYPE, IndexType.class, IndexType.DEPTH);
    _xyUnits = addEnumProperty(XY_UNITS, Unit.class, Unit.METER);
    _dataUnits = addEnumProperty(DATA_UNITS, Unit.class, Unit.METER);
    _onsetType = addEnumProperty(ONSET_TYPE, OnsetType.class, OnsetType.MINIMUM);
    _xOrigin = addDoubleProperty(X_ORIGIN, 0.0);
    _yOrigin = addDoubleProperty(Y_ORIGIN, 0.0);
    _colSpacing = addDoubleProperty(COL_SPACING, 0.0);
    _rowSpacing = addDoubleProperty(ROW_SPACING, 0.0);
    _numOfColumns = addIntegerProperty(NUM_OF_COLUMNS, 0);
    _numOfRows = addIntegerProperty(NUM_OF_ROWS, 0);
    _primaryAngle = addDoubleProperty(PRIMARY_ANGLE, 0.0);
    _nullValue = addFloatProperty(NULL_VALUE, 0.0f);
    _startingLineNum = addIntegerProperty(STARTING_LINE_NUM, 1);
    _numOfHorizons = addIntegerProperty(NUM_OF_HORIZONS, 1);
    _xColumnNum = addIntegerProperty(X_COLUMN_NUM, 1);
    _yColumnNum = addIntegerProperty(Y_COLUMN_NUM, 2);
    _h1ColumnNum = addIntegerProperty(H1_COLUMN_NUM, 3);
    _h2ColumnNum = addIntegerProperty(H2_COLUMN_NUM, 4);
    _h3ColumnNum = addIntegerProperty(H3_COLUMN_NUM, 5);
    _h4ColumnNum = addIntegerProperty(H4_COLUMN_NUM, 6);
    _h5ColumnNum = addIntegerProperty(H5_COLUMN_NUM, 7);
    _orientation = addEnumProperty(ORIENTATION, GridOrientation.class, null);
    _h1Name = addStringProperty(H1_NAME, "horName1");
    _h2Name = addStringProperty(H2_NAME, "horName2");
    _h3Name = addStringProperty(H3_NAME, "horName3");
    _h4Name = addStringProperty(H4_NAME, "horName4");
    _h5Name = addStringProperty(H5_NAME, "horName5");
    _horizon1 = addEntityProperty(HORIZON1, Grid3d.class);
    _horizon2 = addEntityProperty(HORIZON2, Grid3d.class);
    _horizon3 = addEntityProperty(HORIZON3, Grid3d.class);
    _horizon4 = addEntityProperty(HORIZON4, Grid3d.class);
    _horizon5 = addEntityProperty(HORIZON5, Grid3d.class);
    _useAreaOfInterest = addBooleanProperty(USE_AREA_OF_INTEREST, false);
    _areaOfInterest = addEntityProperty(AREA_OF_INTEREST, AreaOfInterest.class);
  }

  public AsciiHorizonMapperModel(final AsciiHorizonMapperModel model) {
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

  public IndexType getIndexType() {
    return _indexType.get();
  }

  public void setIndexType(final IndexType indexType) {
    _indexType.set(indexType);
  }

  public Unit getXyUnits() {
    return _xyUnits.get();
  }

  public void setXyUnits(final Unit xyUnits) {
    _xyUnits.set(xyUnits);
  }

  public Unit getDataUnits() {
    return _dataUnits.get();
  }

  public void setDataUnits(final Unit dataUnits) {
    _dataUnits.set(dataUnits);
  }

  public OnsetType getOnsetType() {
    return _onsetType.get();
  }

  public void setOnsetType(final OnsetType onsetType) {
    _onsetType.set(onsetType);
  }

  /**
    * @return the X Origin
    */
  public double getXorigin() {
    return _xOrigin.get();
  }

  /**
   * @set the X Origin
   */
  public void setXorigin(final double xOrigin) {
    _xOrigin.set(xOrigin);
  }

  /**
   * @return the Y Origin
   */
  public double getYorigin() {
    return _yOrigin.get();
  }

  /**
   * @set the Y Origin
   */
  public void setYorigin(final double yOrigin) {
    _yOrigin.set(yOrigin);
  }

  /**
   * @return the Column spacing
   */
  public double getColSpacing() {
    return _colSpacing.get();
  }

  /**
   * @set the Column spacing
   */
  public void setColSpacing(final double colSpacing) {
    _colSpacing.set(colSpacing);
  }

  /**
   * @return the Row spacing
   */
  public double getRowSpacing() {
    return _rowSpacing.get();
  }

  /**
   * @set the Row spacing
   */
  public void setRowSpacing(final double rowSpacing) {
    _rowSpacing.set(rowSpacing);
  }

  /**
   * @return the number of columns
   */
  public int getNumOfColumns() {
    return _numOfColumns.get();
  }

  /**
   * @set the number of columns
   */
  public void setNumOfColumns(final int numOfColumns) {
    _numOfColumns.set(numOfColumns);
  }

  /**
   * @return the number of rows
   */
  public int getNumOfRows() {
    return _numOfRows.get();
  }

  /**
   * @set the number of rows
   */
  public void setNumOfRows(final int numOfRows) {
    _numOfRows.set(numOfRows);
  }

  /**
   * @return the Primary Angle
   */
  public double getPrimaryAngle() {
    return _primaryAngle.get();
  }

  /**
   * @set the Primary Angle
   */
  public void setPrimaryAngle(final double angle) {
    _primaryAngle.set(angle);
  }

  /**
   * @return the Null Value
   */
  public float getNullValue() {
    return _nullValue.get();
  }

  /**
   * @set the Null Value
   */
  public void setNullValue(final float nullValue) {
    _nullValue.set(nullValue);
  }

  /**
   * @return the starting line number
   */
  public int getStartingLineNum() {
    return _startingLineNum.get();
  }

  /**
   * @set the starting line number
   */
  public void setStartingLineNum(final int lineNum) {
    _startingLineNum.set(lineNum);
  }

  /**
   * @return the number of horizons
   */
  public int getNumOfHorizons() {
    return _numOfHorizons.get();
  }

  /**
   * @set the number of horizon
   */
  public void setNumOfHorizons(final int numOfHorizons) {
    _numOfHorizons.set(numOfHorizons);
  }

  /**
   * @return the X column number
   */
  public int getXcolumnNum() {
    return _xColumnNum.get();
  }

  /**
   * @set the X column number
   */
  public void setXcolumnNum(final int colNumber) {
    _xColumnNum.set(colNumber);
  }

  /**
   * @return the Y column number
   */
  public int getYcolumnNum() {
    return _yColumnNum.get();
  }

  /**
   * @set the Y column number
   */
  public void setYcolumnNum(final int colNumber) {
    _yColumnNum.set(colNumber);
  }

  /**
   * @return the H1 column number
   */
  public int getH1ColumnNum() {
    return _h1ColumnNum.get();
  }

  /**
   * @set the H1 column number
   */
  public void setH1ColumnNum(final int colNumber) {
    _h1ColumnNum.set(colNumber);
  }

  /**
   * @return the H2 column number
   */
  public int getH2ColumnNum() {
    return _h2ColumnNum.get();
  }

  /**
   * @set the H2 column number
   */
  public void setH2ColumnNum(final int colNumber) {
    _h2ColumnNum.set(colNumber);
  }

  /**
   * @return the H3 column number
   */
  public int getH3ColumnNum() {
    return _h3ColumnNum.get();
  }

  /**
   * @set the H3 column number
   */
  public void setH3ColumnNum(final int colNumber) {
    _h3ColumnNum.set(colNumber);
  }

  /**
   * @return the H4 column number
   */
  public int getH4ColumnNum() {
    return _h4ColumnNum.get();
  }

  /**
   * @set the H4 column number
   */
  public void setH4ColumnNum(final int colNumber) {
    _h4ColumnNum.set(colNumber);
  }

  /**
   * @return the H5 column number
   */
  public int getH5ColumnNum() {
    return _h5ColumnNum.get();
  }

  /**
   * @set the H5 column number
   */
  public void setH5ColumnNum(final int colNumber) {
    _h5ColumnNum.set(colNumber);
  }

  /**
   * @return the H1 name
   */
  public String getH1Name() {
    return _h1Name.get();
  }

  /**
   * @set the H1 name
   */
  public void setH1Name(final String name) {
    _h1Name.set(name);
  }

  /**
   * @return the H2 name
   */
  public String getH2Name() {
    return _h2Name.get();
  }

  /**
   * @set the H2 name
   */
  public void setH2Name(final String name) {
    _h2Name.set(name);
  }

  /**
   * @return the H3 name
   */
  public String getH3Name() {
    return _h3Name.get();
  }

  /**
   * @set the H3 name
   */
  public void setH3Name(final String name) {
    _h3Name.set(name);
  }

  /**
   * @return the H4 name
   */
  public String getH4Name() {
    return _h4Name.get();
  }

  /**
   * @set the H4 name
   */
  public void setH4Name(final String name) {
    _h4Name.set(name);
  }

  /**
   * @return the H5 name
   */
  public String getH5Name() {
    return _h5Name.get();
  }

  /**
   * @set the H5 name
   */
  public void setH5Name(final String name) {
    _h5Name.set(name);
  }

  /**
   * @return Horizon1 to export
   */
  public Grid3d getHorizon1() {
    return _horizon1.get();
  }

  /**
   * @set Horizon1 to export
   */
  public void setHorizon1(final Grid3d horizon) {
    _horizon1.set(horizon);
  }

  /**
   * @return Horizon2 to export
   */
  public Grid3d getHorizon2() {
    return _horizon2.get();
  }

  /**
   * @set Horizon2 to export
   */
  public void setHorizon2(final Grid3d horizon) {
    _horizon2.set(horizon);
  }

  /**
   * @return Horizon3 to export
   */
  public Grid3d getHorizon3() {
    return _horizon3.get();
  }

  /**
   * @set Horizon3 to export
   */
  public void setHorizon3(final Grid3d horizon) {
    _horizon3.set(horizon);
  }

  /**
   * @return Horizon4 to export
   */
  public Grid3d getHorizon4() {
    return _horizon4.get();
  }

  /**
   * @set Horizon4 to export
   */
  public void setHorizon4(final Grid3d horizon) {
    _horizon4.set(horizon);
  }

  /**
   * @return Horizon5 to export
   */
  public Grid3d getHorizon5() {
    return _horizon5.get();
  }

  /**
   * @set Horizon5 to export
   */
  public void setHorizon5(final Grid3d horizon) {
    _horizon5.set(horizon);
  }

  /**
   * @return Area of Interest
   */
  public AreaOfInterest getAreaOfInterest() {
    return _areaOfInterest.get();
  }

  /**
   * @set Area of Interest
   */
  public void setAreaOfInterest(final AreaOfInterest aoi) {
    _areaOfInterest.set(aoi);
  }

  /**
   * @return whether to use Area of Interest
   */
  public boolean getUseAreaOfInterest() {
    return _useAreaOfInterest.get();
  }

  /**
   * @set whether to use Area of Interest
   */
  public void setUseAreaOfInterest(final boolean useField) {
    _useAreaOfInterest.set(useField);
  }

  @Override
  public String getUniqueId() {
    return getDirectory() + File.separator + getFileName() + AsciiFileConstants.TEXT_FILE_EXTN;
  }

  @Override
  public void updateUniqueId(final String name) {
    setFileName(name);
  }

  public String getFilePath() {
    return _directory + File.separator + _fileName + AsciiFileConstants.TEXT_FILE_EXTN;
  }

  @Override
  public boolean isRestoreable(final Preferences prefs) {
    if (!new File(prefs.get(UNIQUE_ID, "")).canRead()) {
      ServiceProvider.getLoggingService().getLogger(getClass())
          .warn(prefs.name() + "'s uniqueId was inconsistent with the saved file location. Skipping...");
      return false;
    }
    return true;
  }

  public GridOrientation getOrientation() {
    return _orientation.get();
  }

  public void setOrientation(final GridOrientation orientation) {
    _orientation.set(orientation);
  }

  @Override
  public boolean existsInStore() {
    return existsInStore(getFileName());
  }

  @Override
  public boolean existsInStore(final String name) {
    return existsOnDisk(getDirectory() + File.separator + name + AsciiFileConstants.TEXT_FILE_EXTN);
  }

  /**
   * Returns <i>true</i> if the ascii file exists on disk; <i>false</i> if
   * not.
   * 
   * @param filePath
   *            the full file path of the Ascii horizon grid file to check.
   * @return <i>true</i> if the Ascii horizon grid file exists on disk;
   *         <i>false</i> if not.
   */
  private boolean existsOnDisk(final String filePath) {
    File file = new File(filePath);
    return file.exists();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.common.model2.IModel#validate(org.geocraft.core.common.model2.validation.IValidation)
   */
  @Override
  public void validate(IValidation results) {
    if (_directory.isEmpty()) {
      results.error(AsciiHorizonMapperModel.DIRECTORY, "Directory must be specified.");
    } else {
      File dir = new File(_directory.get());
      if (!dir.exists()) {
        results.error(AsciiHorizonMapperModel.DIRECTORY, "Directory does not exist.");
      }
      if (!dir.isDirectory()) {
        results.error(AsciiHorizonMapperModel.DIRECTORY, "Directory is not a directory.");
      }
      if (!dir.canRead()) {
        results.error(AsciiHorizonMapperModel.DIRECTORY, "Directory is not readable.");
      }
      if (!dir.canWrite()) {
        results.error(AsciiHorizonMapperModel.DIRECTORY, "Directory is not writable.");
      }

      if (_fileName.isEmpty()) {
        results.error(AsciiHorizonMapperModel.FILE_NAME, "File name must be defined.");
      }

      Unit xyUnits = _xyUnits.get();
      if (xyUnits == null || xyUnits.equals(Unit.UNDEFINED)) {
        results.error(AsciiHorizonMapperModel.XY_UNITS, "X,Y unit must be specified.");
      } else {
        if (!xyUnits.equals(Unit.FOOT) && !xyUnits.equals(Unit.METER) && !xyUnits.equals(Unit.US_SURVEY_FOOT)) {
          results.error(AsciiHorizonMapperModel.XY_UNITS, "X,Y unit must be foot or meter.");
        }
      }

      UnitPreferencesValidity.checkXYUnitOfMeasurement(results, AsciiHorizonMapperModel.XY_UNITS, xyUnits);

      Unit dataUnits = _dataUnits.get();
      if (dataUnits == null || dataUnits.equals(Unit.UNDEFINED)) {
        results.error(AsciiHorizonMapperModel.DATA_UNITS, "Data unit must be specified.");
      }

      UnitPreferencesValidity.checkZUnitOfMeasurement(results, AsciiHorizonMapperModel.DATA_UNITS, dataUnits);

      if (_onsetType.isNull()) {
        results.error(AsciiHorizonMapperModel.ONSET_TYPE, "Onset type must be specified.");
      }
    }
    // TODO Auto-generated method stub

  }

}
