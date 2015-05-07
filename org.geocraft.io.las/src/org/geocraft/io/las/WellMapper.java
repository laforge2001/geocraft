package org.geocraft.io.las;


import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.core.model.well.WellDomain;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.las.WellMapperModel.DepthType;


public class WellMapper extends AbstractMapper<Well> {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(WellBoreMapper.class);

  private final WellMapperModel _model;

  private final LasReader _reader;

  private final List<WellLogTrace> _hiddenLogTraces = new ArrayList<WellLogTrace>();

  public WellMapper(final WellMapperModel model) {
    _model = model;
    _reader = new LasReader(_model.getDirectory(), _model.getFileName());
  }

  @Override
  protected WellMapperModel getInternalModel() {
    return _model;
  }

  public WellMapperModel getModel() {
    return new WellMapperModel(_model);
  }

  public WellMapper factory(final MapperModel mapperModel) {
    return new WellMapper((WellMapperModel) mapperModel);
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "LAS Well";
  }

  public String getDatastore() {
    return "LAS File";
  }

  @Override
  protected void createInStore(final Well well) throws IOException {
    String filePath = _model.getFilePath();
    File file = new File(filePath);
    if (!file.createNewFile()) {
      LOGGER.info("Overwriting file: " + filePath);
    }
  }

  @Override
  protected void readFromStore(final Well well) throws IOException {
    setWellProperties(_reader, well);
    setWellBoreProperties(_reader, well.getWellBore());
  }

  void readLogTrace(final WellLogTrace logTrace) throws IOException {
    setWellLogTraceProperties(_reader, logTrace);
  }

  @Override
  protected void updateInStore(final Well well) throws IOException {
    WellBore wellBore = well.getWellBore();

    // Check the format to write (default is ASCII).
    File file = new File(_model.getFilePath());

    // Create a backup copy of the original file.
    createBackup(file);

    // Check if the specified file already exists.
    if (file.exists()) {
      // Check if the specified file is actually a directory.
      if (file.isDirectory()) {
        throw new IOException("The path represents a directory, not a LAS file.");
      }
      // Attempt to delete the existing file.
      if (!file.delete()) {
        throw new IOException("Could not delete the existing LAS file.");
      }
    }

    WellMapperModel wellMapperModel = (WellMapperModel) well.getMapper().getModel();

    // Write out all the columns - not just the selected ones, so add back in the hidden
    // log traces to the bore.
    for (WellLogTrace trace : _hiddenLogTraces) {
      well.addWellLogTrace(trace);
    }

    // Write the LAS file.
    // Note: any exception will restore the file and be re-thrown.
    try {
      LasWriter.write(well, wellMapperModel);
    } catch (Exception ex) {
      // An error occurred, so restore the backup copy of the original file.
      LOGGER.error("Error detected writing LAS file. Restoring to previous state...");
      restoreBackup(file);
      throw new IOException(ex);
    }

    // Hide the unselected curves again.
    for (WellLogTrace trace : _hiddenLogTraces) {
      well.removeWellLogTrace(trace);
    }

    //List<WellLogTrace> logTraces = new ArrayList<WellLogTrace>();
    String[] colNames = _model.getSelectedColumnNames();
    List<String> colNamesUpdated = new ArrayList<String>();
    for (WellLogTrace logTrace : well.getWellLogTraces()) {
      boolean match = false;
      for (String colName : colNames) {
        if (logTrace.getDisplayName().equals(colName)) {
          match = true;
          break;
        }
      }
      if (!match) {
        colNamesUpdated.add(logTrace.getDisplayName());
      }
    }

    LasReader reader = new LasReader(_model.getDirectory(), _model.getFileName());
    String[] colNamesWithUnits = WellSelector.processColNames(reader);
    _model.setValueObject(WellMapperModel.COLUMN_NAMES, colNamesWithUnits);

    //WellMapper wellMapper = new WellMapper(wellMapperModel);

    // Create the well entity.
    //Well well = new Well(wellBore.getDisplayName(), wellMapper);

    // Check if the well entity already exists in the repository...
    //ISpecification filter = new TypeSpecification(Well.class);
    //Map<String, Object> map = ServiceProvider.getRepository().get(filter);
    //Collection<Object> objects = map.values();
    //for (Object object : objects) {
    //  Well temp = (Well) object;
    //  if (temp.getUniqueID().equals(well.getUniqueID())) {
    //    well = temp;
    //  }
    //}

    //wellBore.setWell(well);
    //wellBore.setDirty(false);
    //well.removeAllWellBores();
    //well.addWellBore(wellBore);

    //String[] newSelectedColumns = new String[wellBore.getWellLogTraces().length];

    //WellLogTrace[] oldWellLogTraces = wellBore.getWellLogTraces();
    //for (int i = 0; i < oldWellLogTraces.length; ++i) {
    //  WellLogTraceMapperModel traceMapperModel = new WellLogTraceMapperModel();
    //  traceMapperModel.setValueObject(WellLogTraceMapperModel.WELL_MAPPER_MODEL, wellMapperModel);
    //  traceMapperModel.setValueObject(WellLogTraceMapperModel.TRACE_DISPLAY_NAME, oldWellLogTraces[i].getDisplayName());
    //  wellBore.addWellLogTrace(WellLogTraceFactory.create(oldWellLogTraces[i].getDisplayName(), new WellLogTraceMapper(
    //      traceMapperModel), wellBore));
    //  newSelectedColumns[i] = oldWellLogTraces[i].getDisplayName() + " (" + oldWellLogTraces[i].getDataUnit() + ")";
    // }

    //TODO LAS load task will look at the selected logs property to determine which logs to load
    //so the property in the model must be updated - is this the right place to do this?
    //updateSelectedLogs(newSelectedColumns);

    //TODO LAS load task will look at the entire list of logs property to determine which logs to load
    //so the property in the model must be updated - is this the right place to do this?
    //List<String> allColumnNames = new ArrayList<String>();
    //for (WellLogTrace trace : _hiddenLogTraces) {
    //  allColumnNames.add(trace.getDisplayName() + " (" + trace.getDataUnit() + ")");
    //}
    //allColumnNames.addAll(Arrays.asList(newSelectedColumns));
    //updateAllLogs(allColumnNames.toArray(new String[0]));

    //TODO LAS load task uses the well name property in the mapper model to determine the name of the well
    //so the property in the model must be updated - is this the right place to do this?
    //updateWellName(wellBore.getDisplayName());

  }

  @Override
  protected void deleteFromStore(final Well well) throws IOException {
    String filePath = _model.getFilePath();
    File wellFile = new File(filePath);
    // Check if the LAS file exists.
    if (wellFile.exists()) {
      // Try to delete the LAS file.
      if (!wellFile.delete()) {
        throw new IOException("Could not delete LAS well file \'" + filePath + "\'.");
      }
    } else {
      throw new IOException("LAS well file \'" + filePath + "\' does not exist.");
    }
  }

  private void setWellLogTraceProperties(final LasReader reader, final WellLogTrace trace) {
    //The time or depth index of the measurements

    float[][] rawLogData = reader.getRawData();
    String[] logNames = reader.getColumnNames();

    float[] convertedDepths = convertToWorkspaceUnits(rawLogData[0], parseDepthUnits(reader));

    // Create the log traces
    for (int col = 0; col < logNames.length; col++) {
      ServiceProvider.getLoggingService().getLogger(getClass()).info("Creating log: " + logNames[col]);
      String name = logNames[col];
      if (name.equals(trace.getDisplayName())) {

        //depth column is always first
        if (col == 0) {
          trace.setTraceData(convertedDepths, _model.getDepthUnit(), reader.getNullValue());
        } else {
          trace.setTraceData(rawLogData[col], reader.getUnits(name), reader.getNullValue());
        }

        // TODO: What is the domain in an LAS file? Does the user need to supply this.
        trace.setZValues(Utilities.copyFloatArrayToDoubleArray(convertedDepths),
            DepthType.TVD.equals(_model.getDepthType()) ? WellDomain.TRUE_VERTICAL_DEPTH : WellDomain.MEASURED_DEPTH);
        trace.setTraceType(name);
        trace.setTraceName(name);
        trace.setComment(reader.getCommentsMap().get(name));
        trace.setDirty(false);
        break;
      }
    }

  }

  private void setWellProperties(final LasReader reader, final Well well) {

    well.setCurrentOperator(reader.getCompany());
    well.setDisplayName(reader.getWellName());
    well.setField(reader.getField());
    well.setDataSource(reader.getServiceCompany());

    //set the country attributes
    parseCountry(reader, well);

    String serviceDate = reader.getServiceDate();
    if (serviceDate != null && !serviceDate.isEmpty()) {
      String[] subs = serviceDate.split("/");
      if (subs.length >= 3) {
        Date spudDate = Date.valueOf(subs[2] + "-" + subs[0] + "-" + subs[1]);
        Timestamp spudDate2 = new Timestamp(spudDate.getTime());
        well.setSpudDate(spudDate2);
      }
    }

    well.setLocation(getWellLocation());
    well.setDataSource(reader.getServiceCompany());

    well.setTotalDepth((float) _model.getEndDepth());
  }

  private Coordinate getWellLocation() {
    Point3d wellLoc = new Point3d(_model.getXCoord(), _model.getYCoord(), 0.0);
    CoordinateSystem wellCs = new CoordinateSystem(_model.getCoordinateSystem(), _model.getGeodeticDatum(),
        Domain.DISTANCE);
    return new Coordinate(wellLoc, wellCs);
  }

  private void parseCountry(final LasReader reader, final Well well) {

    final String CANADA = "ca";
    final String USA = "us";

    if (reader.getCountry() != null) {
      well.setCountry(reader.getCountry().getCode());
    }

    String[] cols = reader.getColumnNames();
    Arrays.sort(cols);

    if (!reader.getApi().equals("") || !reader.getState().equals("") || !reader.getCounty().equals("")) {
      well.setCountry(USA);
    } else if (!reader.getUwi().equals("") || !reader.getProvince().equals("") || !reader.getLicense().equals("")) {
      well.setCountry(CANADA);
    }

    if (well.getCountry() != null) {
      if (well.getCountry().equals(CANADA)) {
        well.setIdentifierAndType(reader.getUwi(), "UWI");
        well.setStateOrProvince(reader.getProvince());
        //TODO supposed to set a "LIC" attribute - using UWI in writer for now
      } else {
        well.setIdentifierAndType(reader.getApi(), "API");
        well.setStateOrProvince(reader.getState());
        well.setCounty(reader.getCounty());
      }
    }

  }

  /**
   * This method parses the units and tries to determine which units to set
   * for depth. Basically if the string contains an "f", the units are feet
   * otherwise meters.
   *
   * @param reader the LAS reader.
   * @return the unit.
   */
  private Unit parseDepthUnits(final LasReader reader) {
    String lasUnit = reader.getUnitList().get(reader.getColumnNames()[0]);
    Pattern p = Pattern.compile("[fF]");
    Matcher m = p.matcher(lasUnit);
    if (m.find()) {
      return Unit.FOOT;
    }
    return Unit.METER;
  }

  private void setWellBoreProperties(final LasReader reader, final WellBore bore) {

    //The time or depth index of the measurements

    float[][] rawLogData = reader.getRawData();

    float[] depths = convertToWorkspaceUnits(rawLogData[0], parseDepthUnits(reader));

    bore.setDepthsAndTimes(depths, depths, new float[0]);

    double[] xOffsets = new double[depths.length];
    double[] yOffsets = new double[depths.length];
    bore.setXYOffsets(xOffsets, yOffsets);
  }

  /**
   * @param data
   */
  private float[] convertToWorkspaceUnits(final float[] data, final Unit origUnit) {
    float[] oldVals = new float[data.length];
    System.arraycopy(data, 0, oldVals, 0, data.length);
    for (int i = 0; i < oldVals.length; ++i) {
      oldVals[i] = Unit
          .convert(oldVals[i], origUnit, Unit.FOOT/*UnitPreferences.getInstance().getVerticalDistanceUnit()*/);
    }
    _model
        .setValueObject(WellMapperModel.DEPTH_UNITS, Unit.FOOT/*UnitPreferences.getInstance().getVerticalDistanceUnit()*/);
    return oldVals;
  }

  public void hideUnselectedLog(final WellLogTrace trace) {
    _hiddenLogTraces.add(trace);
    trace.getWell().removeWellLogTrace(trace);
  }

  /**
   * Restores a backup copy of the LAS file.
   *
   * @param file the LAS file.
   * @throws IOException thrown on copy error.
   */
  private void restoreBackup(final File file) throws IOException {
    FileUtil.copy(file.getAbsolutePath() + "_bak", file.getAbsolutePath());
  }

  /**
   * Creates a backup copy of the LAS file.
   *
   * @param file the LAS file.
   * @throws IOException thrown on copy error.
   */
  private void createBackup(final File file) throws IOException {
    FileUtil.copy(file.getAbsolutePath(), file.getAbsolutePath() + "_bak");
  }

  private void updateWellName(final String name) {
    _model.setValueObject(WellMapperModel.WELL_NAME, name);
  }

  private void updateSelectedLogs(final String[] selectedLogs) {
    _model.setValueObject(WellMapperModel.SELECTED_COLUMN_NAMES, selectedLogs);
  }

  private void updateAllLogs(final String[] selectedLogs) {
    _model.setValueObject(WellMapperModel.COLUMN_NAMES, selectedLogs);
  }

}
