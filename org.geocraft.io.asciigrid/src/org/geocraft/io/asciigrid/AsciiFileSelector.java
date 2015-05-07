package org.geocraft.io.asciigrid;


import java.io.File;

import org.geocraft.core.common.io.TextFile;
import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.session.MapperParameterStore;
import org.geocraft.ui.io.DatastoreFileSelector;


/**
 * The class for handling the selection of Ascii files on disk.
 */
public class AsciiFileSelector extends DatastoreFileSelector implements AsciiFileConstants {

  /**
  * The default constructor.
  */
  public AsciiFileSelector() {
    super("Ascii Horizon", new String[] { "Ascii Files (.txt)", "Ascii File (*.*)" }, new String[] { "*.txt", "*.*" },
        "LoadModSpecGrid_DIR");
  }

  @Override
  protected MapperModel[] createMapperModelsFromSelectedFiles(final File[] files) {
    int numFiles = files.length;
    AsciiHorizonMapperModel[] mapperModels = new AsciiHorizonMapperModel[numFiles];
    for (int i = 0; i < numFiles; i++) {
      mapperModels[i] = createMapperModel(files[i]);
    }
    return mapperModels;
  }

  private static void parseFileForAttributes(String filePath, AsciiHorizonMapperModel model) {
    TextFile tf = new TextFile(filePath);
    String[] recs = tf.getRecords();
    for (int i = 0; i < recs.length; ++i) {
      if (!recs[i].startsWith("#")) {
        break;
      } else if (recs[i].contains(AbstractAsciiHorizonWriter.X_ORIGIN_ID)) {
        model.setXorigin(Double.parseDouble(recs[i].split("=")[1].trim()));
      } else if (recs[i].contains(AbstractAsciiHorizonWriter.Y_ORIGIN_ID)) {
        model.setYorigin(Double.parseDouble(recs[i].split("=")[1].trim()));
      } else if (recs[i].contains(AbstractAsciiHorizonWriter.COLUMN_SPACING_ID)) {
        model.setColSpacing(Double.parseDouble(recs[i].split("=")[1].trim()));
      } else if (recs[i].contains(AbstractAsciiHorizonWriter.ROW_SPACING_ID)) {
        model.setRowSpacing(Double.parseDouble(recs[i].split("=")[1].trim()));
      } else if (recs[i].contains(AbstractAsciiHorizonWriter.NUM_OF_COLUMNS_ID)) {
        model.setNumOfColumns(Integer.parseInt(recs[i].split("=")[1].trim()));
      } else if (recs[i].contains(AbstractAsciiHorizonWriter.NUM_OF_ROWS_ID)) {
        model.setNumOfRows(Integer.parseInt(recs[i].split("=")[1].trim()));
      } else if (recs[i].contains(AbstractAsciiHorizonWriter.PRIMARY_ANGLE_ID)) {
        model.setPrimaryAngle(Double.parseDouble(recs[i].split("=")[1].trim()));
      } else if (recs[i].contains(AbstractAsciiHorizonWriter.NULL_VALUE_ID)) {
        model.setNullValue(Float.parseFloat(recs[i].split("=")[1].trim()));
      }
    }
  }

  /**
   * Scans the specified ModSpec grid file and returns a mapper model of datastore properties.
   * @param file the ModSpec grid file.
   * @return the mapper model of datastore properties.
   */
  public static AsciiHorizonMapperModel createMapperModel(final File file) {
    // Create a new mapper model.
    AsciiHorizonMapperModel model = new AsciiHorizonMapperModel();
    String filePath = file.getAbsolutePath();
    model.setDirectory(FileUtil.getPathName(filePath));
    model.setFileName(FileUtil.getShortName(filePath));
    model.setOrientation(GridOrientation.X_IS_COLUMN);

    // Restore the previously specified settings.
    MapperParameterStore.restore(model);

    // Override certain previously specified settings with current datastore settings.
    parseFileForAttributes(filePath, model);

    return model;
  }

  /**
   * Scans the specified ModSpec grid file and returns a mapper model of datastore properties.
   * @param file the ModSpec grid file.
   * @return the mapper model of datastore properties.
   */
  public static AsciiHorizonMapperModel createMapperModelNoRestore(final File file) {
    // Create a new mapper model.
    AsciiHorizonMapperModel model = new AsciiHorizonMapperModel();
    String filePath = file.getAbsolutePath();
    model.setDirectory(FileUtil.getPathName(filePath));
    model.setFileName(FileUtil.getShortName(filePath));
    model.setOrientation(GridOrientation.X_IS_COLUMN);

    // Override certain previously specified settings with current datastore settings.
    parseFileForAttributes(filePath, model);

    return model;
  }
}
