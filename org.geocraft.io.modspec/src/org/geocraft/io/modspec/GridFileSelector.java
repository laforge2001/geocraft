package org.geocraft.io.modspec;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.session.MapperParameterStore;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;
import org.geocraft.ui.io.DatastoreFileSelector;


/**
 * The class for handling the selection of ModSpec grid files on disk.
 */
public class GridFileSelector extends DatastoreFileSelector implements ModSpecGridConstants {

  /**
  * The default constructor.
  */
  public GridFileSelector() {
    super("ModSpec Grid", new String[] { "ModSpec Grid Files (.grid)" }, new String[] { "*.grid" },
        "LoadModSpecGrid_DIR");
  }

  @Override
  protected MapperModel[] createMapperModelsFromSelectedFiles(final File[] files) {
    int numFiles = files.length;
    GridMapperModel[] mapperModels = new GridMapperModel[numFiles];
    for (int i = 0; i < numFiles; i++) {
      mapperModels[i] = createMapperModel(files[i]);
    }
    return mapperModels;
  }

  /**
   * Scans the specified ModSpec grid file and returns a mapper model of datastore properties.
   * @param file the ModSpec grid file.
   * @return the mapper model of datastore properties.
   */
  public static GridMapperModel createMapperModel(final File file) {
    // Create a new mapper model.
    GridMapperModel model = new GridMapperModel();
    String filePath = file.getAbsolutePath();
    model.setDirectory(FileUtil.getPathName(filePath));
    model.setFileName(FileUtil.getBaseName(filePath));

    //    GridOrientation orientation = GridOrientation.X_IS_COLUMN;
    //    try {
    //      orientation = getOrientation(filePath);
    //    } catch (Exception ex) {
    //      ServiceProvider.getLoggingService().getLogger(GridFileSelector.class).warn(
    //          "Error trying to determine ModSpec grid orientation...defaulting to X->Column", ex);
    //    }
    //    model.setOrientation(orientation);

    // Restore the previously specified settings.
    MapperParameterStore.restore(model);

    // Override certain previously specified settings with current datastore settings.
    GridFileFormat fileFormat = GridFileFormat.ASCII;
    try {
      fileFormat = getFileFormat(filePath);
    } catch (IOException ex) {
      ServiceProvider.getLoggingService().getLogger(GridFileSelector.class).warn(
          "Error trying to determine ModSpec grid data format...assuming ASCII", ex);
    }
    model.setFileFormat(fileFormat);

    return model;
  }

  /**
   * Returns <i>true</i> if the specified path appears to be a ModSpec grid file in binary format.
   * @param path the path of the ModSpec grid file.
   * @return true if binary; false if not (ASCII).
   * @throws IOException
   */
  public static GridFileFormat getFileFormat(final String path) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
    String record = reader.readLine().trim();
    if (!record.equals("#<CPS_v1 TYPE=MODSPEC_GRID/>")) {
      reader.close();
      return GridFileFormat.ASCII;
    }
    reader.close();
    return GridFileFormat.BINARY;
  }

}
