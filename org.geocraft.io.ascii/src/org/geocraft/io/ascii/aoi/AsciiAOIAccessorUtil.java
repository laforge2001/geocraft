package org.geocraft.io.ascii.aoi;


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
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.io.ascii.ServiceComponent;


/**
 * The datastore accessor implementation for ASCII AOIs.
 */
public class AsciiAOIAccessorUtil implements IDatastoreAccessorUtil {

  public Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities) {
    Map<Entity, MapperModel> map = new HashMap<Entity, MapperModel>();
    // Loop thru the entities, creating a subset by extracting out only the AOI properties.
    Set<Entity> set = new HashSet<Entity>();
    for (Entity entity : entities) {
      if (entity instanceof MapPolygonAOI) {
        set.add(entity);
      }
    }

    // Loop thru the subset of AOI properties, setting default datastore mapper properties for each.
    for (Entity entity : set.toArray(new Entity[0])) {
      AreaOfInterest aoi = (AreaOfInterest) entity;
      AsciiAOIMapperModel model = new AsciiAOIMapperModel(); 
      // Default the directory to the working directory if the Preferences key has not been set yet.
      String directory = PreferencesUtil.getPreferencesStore(ServiceComponent.PLUGIN_ID).get("Workspace_DIR",
          Utilities.getWorkingDirectory());
      model.setDirectory(directory);
      // Default the file name to the AOI display name.
      model.setFileName(aoi.getDisplayName());
      model.setAOIType(getAOIType(aoi));
      map.put(entity, model);
    }
    return map;
  }

  private String getAOIType(AreaOfInterest aoi) {
    if (MapPolygonAOI.class.isAssignableFrom(aoi.getClass())) {
      return AsciiAOIConstants.MAP_POLYGON_AOI;
    } else if (SeismicSurvey3dAOI.class.isAssignableFrom(aoi.getClass())) {
      return AsciiAOIConstants.INLINE_XLINE_AOI;
    } else if (SeismicSurvey2dAOI.class.isAssignableFrom(aoi.getClass())) {
      return AsciiAOIConstants.LINE_SHOTPOINT_AOI;
    }
    return "";
  }

  @Override
  public IStatus initialize() {
    // No initialization necessary.
    return Status.OK_STATUS;
  }

}
