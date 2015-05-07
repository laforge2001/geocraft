package org.geocraft.io.las;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocraft.core.common.io.TextFile;
import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.io.DatastoreFileSelector;


public class WellSelector extends DatastoreFileSelector {

  public WellSelector() {
    super("LAS Well", new String[] { "LAS Well (.las)", "LAS Well (.LAS)" }, new String[] { "*.las", "*.LAS" },
        "LoadLasWell_DIR");
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.io.DatastoreFileSelector#createMapperModelsFromSelectedFiles(java.io.File[])
   */
  @Override
  protected MapperModel[] createMapperModelsFromSelectedFiles(final File[] files) {
    int numFiles = files.length;

    List<WellMapperModel> mapperModels = new ArrayList<WellMapperModel>();
    for (int i = 0; i < numFiles; i++) {
      mapperModels.add(createMapperModel(files[i]));
    }

    WellMapperModel[] returnMe = mapperModels.toArray(new WellMapperModel[0]);
    return returnMe;
  }

  /**
   * @param file
   * @return
   */
  public WellMapperModel createMapperModel(final File file) {
    WellMapperModel mapperModel = new WellMapperModel();
    String filePath = file.getAbsolutePath();

    mapperModel.setValueObject(WellMapperModel.DIRECTORY, FileUtil.getPathName(filePath));
    mapperModel.setValueObject(WellMapperModel.FILE_NAME, FileUtil.getShortName(filePath).split("\\.")[0]);

    TextFile tf = createTextFile(mapperModel.getDirectory(), mapperModel.getFileName());
    LasReader reader = new LasReader(mapperModel.getDirectory(), mapperModel.getFileName());
    float[] dataRange = reader.getDataRange();

    // Restore the previously specified settings.
    //    MapperParameterStore.restore(mapperModel);

    mapperModel.setValueObject(WellMapperModel.DEPTH_UNITS, parseDepthUnits(reader));
    mapperModel.setValueObject(WellMapperModel.BEG_DEPTH_DEFAULT, dataRange[0]);
    mapperModel.setValueObject(WellMapperModel.BEG_DEPTH, dataRange[0]);
    mapperModel.setValueObject(WellMapperModel.END_DEPTH_DEFAULT, dataRange[1]);
    mapperModel.setValueObject(WellMapperModel.END_DEPTH, dataRange[1]);
    mapperModel.setValueObject(WellMapperModel.STEP_DEFAULT, dataRange[2]);
    mapperModel.setValueObject(WellMapperModel.STEP, dataRange[2]);
    //    lmm.setValueObject(LasMapperModel.DESCRIPTION_LIST, createDescriptionList(reader));

    mapperModel.setValueObject(WellMapperModel.XCOORD, reader.getXCoordinate());
    mapperModel.setValueObject(WellMapperModel.YCOORD, reader.getYCoordinate());
    mapperModel.setValueObject(WellMapperModel.HZCS, reader.getHorizontalCoordinateSystem());
    mapperModel.setValueObject(WellMapperModel.DATUM, reader.getGeodeticDatum());
    mapperModel.setValueObject(WellMapperModel.WELL_NAME, reader.getWellName());

    //    String[] colNamesWithUnits = processColNames(reader);
    mapperModel.setValueObject(WellMapperModel.COLUMN_NAMES, reader.getColumnNames());
    mapperModel.setValueObject(WellMapperModel.SELECTED_COLUMN_NAMES, reader.getColumnNames());
    //    mapperModel.setValueObject(WellMapperModel.COLUMN_NAMES, colNamesWithUnits);
    return mapperModel;
  }

  /**
   * basically parses the units and tries to determine which units to set
   * for depth. Basically if the string contains an "f", the units are feet
   * otherwise meters
   *
   * @param reader
   * @return
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

  /**
   * @param reader
   * @return
   */
  public static String[] processColNames(final LasReader reader) {
    String[] colNames = reader.getColumnNames();
    Map<String, String> unitList = reader.getUnitList();
    String[] colNamesWithUnits = new String[colNames.length];

    for (int i = 0; i < colNames.length; ++i) {
      colNamesWithUnits[i] = colNames[i] + " (" + unitList.get(colNames[i]) + ")";

    }
    // TODO Auto-generated method stub
    return colNamesWithUnits;
  }

  /**
   * @param directory
   * @param fileName
   * @return
   */
  private TextFile createTextFile(final String directory, final String fileName) {
    final String filePath = directory + File.separator + fileName;
    File file = new File(filePath);
    if (!file.exists() || !file.canRead() || file.isDirectory()) {
      throw new RuntimeException("Cannot acces the LAS file: " + filePath);
    }
    return new TextFile(filePath);
  }

  /**
   * @param reader
   * @return
   */
  private List<LasMnemonicDescriptionModel> createDescriptionList(final LasReader reader) {
    String[] mnemonic = reader.getColumnNames();
    Map<String, String> comments = reader.getCommentsMap();
    Map<String, String> unitList = reader.getUnitList();
    String name = reader.getWellName();
    List<LasMnemonicDescriptionModel> list = new ArrayList<LasMnemonicDescriptionModel>();

    for (String mne : mnemonic) {
      LasMnemonicDescriptionModel model = new LasMnemonicDescriptionModel();
      model.setValueObject(LasMnemonicDescriptionModel.CAN_LOAD, true);
      model.setValueObject(LasMnemonicDescriptionModel.MNEMONIC, mne);
      model.setValueObject(LasMnemonicDescriptionModel.DESCRIPTION, comments.get(mne));
      model.setValueObject(LasMnemonicDescriptionModel.FILE_UNITS, unitList.get(mne));
      try {
        model.setValueObject(LasMnemonicDescriptionModel.INTERP_UNITS, Unit.lookupBySymbol(unitList.get(mne)));
      } catch (IllegalArgumentException e) {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(e.getMessage());
        model.setValueObject(LasMnemonicDescriptionModel.INTERP_UNITS, Unit.UNDEFINED);
      }
      model.setValueObject(LasMnemonicDescriptionModel.NAME, name);
      list.add(model);
    }
    return list;
  }
}
