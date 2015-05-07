/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciipointset;


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
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.mapper.MapperModel;


public class AsciiPointSetAccessorUtil implements IDatastoreAccessorUtil {

  @Override
  public IStatus initialize() {
    return Status.OK_STATUS;
  }

  @Override
  public Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities) {

    Set<PointSet> pointSets = new HashSet<PointSet>();
    for (Entity entity : entities) {
      if (entity instanceof PointSet) {
        pointSets.add((PointSet) entity);
      }
    }

    Map<Entity, MapperModel> map = new HashMap<Entity, MapperModel>();

    for (PointSet pointSet : pointSets) {
      AsciiPointSetMapperModel mapperModel = new AsciiPointSetMapperModel();
      String directory = PreferencesUtil.getPreferencesStore(AsciiPointSetMapperModel.PLUGIN_ID).get(
          "Workspace_DIR", Utilities.getWorkingDirectory());
      mapperModel.setValueObject(AsciiPointSetMapperModel.DIRECTORY, directory);
      mapperModel.setValueObject(AsciiPointSetMapperModel.FILE_NAME, pointSet.getDisplayName());
      mapperModel.setValueObject(AsciiPointSetMapperModel.Z_UNIT, pointSet.getZUnit());
      map.put(pointSet, mapperModel);
    }
    return map;
  }

}
