package org.geocraft.io.las;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.io.IDatastoreAccessorUtil;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellLogTrace;


public class WellAccessorUtil implements IDatastoreAccessorUtil {

  public IStatus initialize() {
    // No initialization needed for LAS files.
    return Status.OK_STATUS;
  }

  @Override
  public Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities) {
    Set<Well> wells = new HashSet<Well>();
    for (Entity entity : entities) {
      if (entity instanceof Well) {
        wells.add((Well) entity);
      }
    }

    Map<Entity, MapperModel> map = new HashMap<Entity, MapperModel>();
    for (Well well : wells) {
      String directory = PreferencesUtil.getPreferencesStore(WellMapperModel.PLUGIN_ID).get("Workspace_DIR",
          Utilities.getWorkingDirectory());
      WellMapperModel mapperModel = new WellMapperModel();
      mapperModel.setValueObject(WellMapperModel.DIRECTORY, directory);
      mapperModel.setValueObject(WellMapperModel.FILE_NAME, well.getDisplayName());
      WellLogTrace[] logTraces = well.getWellLogTraces();
      String[] colNames = new String[logTraces.length];
      for (int i = 0; i < colNames.length; i++) {
        colNames[i] = logTraces[i].getTraceName();
      }
      mapperModel.setColumnNames(colNames);
      mapperModel.setIOMode(IOMode.OUTPUT);
      map.put(well, mapperModel);
    }

    return map;
  }

}
