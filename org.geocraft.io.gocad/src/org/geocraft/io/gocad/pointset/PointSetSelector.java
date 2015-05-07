/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.gocad.pointset;


import java.io.File;

import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.session.MapperParameterStore;
import org.geocraft.ui.io.DatastoreFileSelector;


public class PointSetSelector extends DatastoreFileSelector {

  /**
   * The default constructor.
   */
  public PointSetSelector() {
    super("GOCAD PointSet", new String[] { "GOCAD PointSet (.vs)" }, new String[] { "*.vs" }, "LoadGOCADPointSet_DIR");
  }

  @Override
  protected MapperModel[] createMapperModelsFromSelectedFiles(final File[] files) {
    int numFiles = files.length;
    PointSetMapperModel[] mapperModels = new PointSetMapperModel[numFiles];
    for (int i = 0; i < numFiles; i++) {
      mapperModels[i] = createMapperModel(files[i]);
    }
    return mapperModels;
  }

  public PointSetMapperModel createMapperModel(final File file) {
    PointSetMapperModel mapperModel = new PointSetMapperModel();
    mapperModel.setValueObject(PointSetMapperModel.DIRECTORY, file.getParentFile().getAbsolutePath());
    mapperModel.setValueObject(PointSetMapperModel.FILE_NAME, file.getName());

    // Restore the previously specified settings.
    MapperParameterStore.restore(mapperModel);

    return mapperModel;
  }

}
