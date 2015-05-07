package org.geocraft.io.las;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringArrayProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;


public class WellMapperModel extends MapperModel {

  public static final String LAS_EXTENSION = ".las";

  //TODO change this to use WellLogDomain eventually ?
  public enum DepthType {
    TVD("True Vertical Depth"),
    MD("Measured Depth");

    private String _label;

    DepthType(final String label) {
      _label = label;
    }

    @Override
    public String toString() {
      return _label;
    }

  }

  public static final String PLUGIN_ID = "org.geocraft.io.las";

  public static final String DIRECTORY = "Directory";

  public static final String FILE_NAME = "File Name";

  public static final String XY_UNIT = "X,Y Unit";

  public static final String Z_UNIT = "Z Unit";

  public static final String COLUMN_NAMES = "Column Names";

  public static final String SELECTED_COLUMN_NAMES = "Selected Column Names";

  public static final String DEPTH_UNITS = "Depth Units";

  public static final String BEG_DEPTH_DEFAULT = "Actual First Depth";

  public static final String BEG_DEPTH = "First Depth";

  public static final String END_DEPTH_DEFAULT = "Actual Last Depth";

  public static final String END_DEPTH = "Last Depth";

  public static final String STEP_DEFAULT = "Actual Depth Step";

  public static final String STEP = "Depth Step";

  public static final String DESCRIPTION_LIST = "description list";

  public static final String XCOORD = "X-Coordinate";

  public static final String YCOORD = "Y-Coordinate";

  public static final String HZCS = "Horizontal Coordinate System";

  public static final String DATUM = "Geodetic Datum";

  public static final String DEPTH_TYPE = "Depth Type";

  public static final String WELL_NAME = "Well Name";

  private final StringProperty _directory;

  private final StringProperty _fileName;

  private final StringArrayProperty _columnNames;

  private final StringArrayProperty _selectedColumnNames;

  private final EnumProperty<Unit> _depthUnits;

  private final DoubleProperty _begDepthDef;

  private final DoubleProperty _endDepthDef;

  private final DoubleProperty _stepDef;

  private final DoubleProperty _begDepth;

  private final DoubleProperty _endDepth;

  private final DoubleProperty _step;

  private final DoubleProperty _xcoord;

  private final DoubleProperty _ycoord;

  private final StringProperty _hzcs;

  private final StringProperty _geodeticDatum;

  private final EnumProperty<DepthType> _depthType;

  /** User selected units for each log trace. */
  Map<String, Unit> _actualUnits = new HashMap<String, Unit>();

  private final StringProperty _wellName;

  public WellMapperModel() {
    _directory = addStringProperty(DIRECTORY, "");
    _fileName = addStringProperty(FILE_NAME, "");
    _columnNames = addStringArrayProperty(COLUMN_NAMES);
    _selectedColumnNames = addStringArrayProperty(SELECTED_COLUMN_NAMES);
    _begDepthDef = addDoubleProperty(BEG_DEPTH_DEFAULT, 0.0);
    _endDepthDef = addDoubleProperty(END_DEPTH_DEFAULT, 0.0);
    _stepDef = addDoubleProperty(STEP_DEFAULT, 0.0);
    _begDepth = addDoubleProperty(BEG_DEPTH, 0.0);
    _endDepth = addDoubleProperty(END_DEPTH, 0.0);
    _step = addDoubleProperty(STEP, 0.0);
    _xcoord = addDoubleProperty(XCOORD, Double.NaN);
    _ycoord = addDoubleProperty(YCOORD, Double.NaN);
    _hzcs = addStringProperty(HZCS, "");
    _geodeticDatum = addStringProperty(DATUM, "");
    _depthType = addEnumProperty(WellMapperModel.DEPTH_TYPE, DepthType.class, DepthType.MD);
    _depthUnits = addEnumProperty(WellMapperModel.DEPTH_UNITS, Unit.class, Unit.FOOT);
    _wellName = addStringProperty(WellMapperModel.WELL_NAME, "");
  }

  /**
   * @param model
   */
  public WellMapperModel(final WellMapperModel model) {
    this();
    updateFrom(model);
  }

  public Unit getDepthUnit() {
    return _depthUnits.get();
  }

  public DepthType getDepthType() {
    return _depthType.get();
  }

  public double getXCoord() {
    return _xcoord.get();
  }

  public double getYCoord() {
    return _ycoord.get();
  }

  public String getCoordinateSystem() {
    return _hzcs.get();
  }

  public String getGeodeticDatum() {
    return _geodeticDatum.get();
  }

  public String getDirectory() {
    return _directory.get();
  }

  public String getFileName() {
    String name = _fileName.get();
    if (name.endsWith(LAS_EXTENSION)) {
      return name;
    }
    return name + LAS_EXTENSION;
  }

  public Unit getUnit(final String columnName) {
    return _actualUnits.get(columnName);
  }

  public String[] getColumnNames() {
    return _columnNames.get();
  }

  public String[] getSelectedColumnNames() {
    return _selectedColumnNames.get();
  }

  public double getBegDepthDef() {
    return _begDepthDef.get();
  }

  public double getEndDepthDef() {
    return _endDepthDef.get();
  }

  public double getStepDef() {
    return _stepDef.get();
  }

  public double getBegDepth() {
    return _begDepth.get();
  }

  public double getEndDepth() {
    return _endDepth.get();
  }

  public double getStep() {
    return _step.get();
  }

  public String getWellName() {
    return _wellName.get();
  }

  public String getFilePath() {
    return getDirectory() + File.separatorChar + getFileName();
  }

  @Override
  public boolean existsInStore() {
    return existsInStore(_fileName.get());
  }

  @Override
  public boolean existsInStore(final String name) {
    File file = new File(_directory.get(), name);
    if (!file.exists()) {
      file = new File(_directory.get(), name + ".las");
      if (!file.exists()) {
        file = new File(_directory.get(), name + ".LAS");
      }
    }
    return file.exists() && file.canRead();
  }

  @Override
  public String getUniqueId() {
    return _directory.get() + File.separator + getFileName();
  }

  public void setColumnNames(final String[] names) {
    _columnNames.set(names);
  }

  @Override
  public void updateUniqueId(final String name) {
    _fileName.set(name + LAS_EXTENSION);
  }

  @Override
  public void validate(final IValidation results) {
    if (!new File((String) _directory.getValueObject()).exists()) {
      results.error(WellMapperModel.DIRECTORY, "Directory does not exist");
    }
    if (_xcoord.getValueObject().equals("NaN")) {
      results.warning(WellMapperModel.XCOORD, "X-coordinate is required. Latitude/Longitude not currently supported");
    }
    if (_ycoord.getValueObject().equals("NaN")) {
      results.warning(WellMapperModel.YCOORD, "Y-coordinate is required. Latitude/Longitude not currently supported");
    }
    if (_hzcs.get().equals("")) {
      results.warning(WellMapperModel.HZCS, "Horizontal coordinate system required");
    }
    if ("".equals(_geodeticDatum.get())) {
      results.warning(WellMapperModel.DATUM, "Geodetic Datum required");
    }

    if (_selectedColumnNames.get().length == 0) {
      results.error(WellMapperModel.SELECTED_COLUMN_NAMES, "At least one well log curve must be selected");
    }

    if (!checkIfDepthColumnSelected()) {
      results.warning(WellMapperModel.SELECTED_COLUMN_NAMES,
          "A Depth Curve has not been selected and may not be compatible outside of Geocraft");
    }
  }

  /**
   * @return
   */
  private boolean checkIfDepthColumnSelected() {
    Pattern p = Pattern.compile("^[Dd][Ee][Pp][Tt]");
    String[] selectedColNames = _selectedColumnNames.get();
    for (String s : selectedColNames) {
      Matcher m = p.matcher(s);
      if (m.find()) {
        return true;
      }
    }
    return false;
  }

}
