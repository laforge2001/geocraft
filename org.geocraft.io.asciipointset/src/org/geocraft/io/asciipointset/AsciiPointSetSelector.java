/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciipointset;


import java.io.File;

import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.session.MapperParameterStore;
import org.geocraft.ui.io.DatastoreFileSelector;


public class AsciiPointSetSelector extends DatastoreFileSelector {

  /**
   * The default constructor.
   */
  public AsciiPointSetSelector() {
    super("Ascii PointSet", new String[] { "Ascii PointSet (.txt)" }, new String[] { "*.txt" }, "LoadModSpecGrid_DIR");
  }

  @Override
  protected MapperModel[] createMapperModelsFromSelectedFiles(final File[] files) {
    int numFiles = files.length;
    AsciiPointSetMapperModel[] mapperModels = new AsciiPointSetMapperModel[numFiles];
    for (int i = 0; i < numFiles; i++) {
      mapperModels[i] = createMapperModel(files[i]);
    }
    return mapperModels;
  }

  public AsciiPointSetMapperModel createMapperModel(final File file) {
    AsciiPointSetMapperModel mapperModel = new AsciiPointSetMapperModel();
    mapperModel.setValueObject(AsciiPointSetMapperModel.DIRECTORY, file.getParentFile().getAbsolutePath());
    mapperModel.setValueObject(AsciiPointSetMapperModel.FILE_NAME, file.getName());

    // Restore the previously specified settings.
    MapperParameterStore.restore(mapperModel);

    return mapperModel;
  }

}
