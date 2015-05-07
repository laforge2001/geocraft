package org.geocraft.io.modspec;


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
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;
import org.geocraft.internal.io.modspec.ServiceComponent;


/**
 * The datastore accessor implementation for ModSpec grids.
 */
public class GridAccessorUtil implements IDatastoreAccessorUtil, ModSpecGridConstants {

  public Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities) {
    Map<Entity, MapperModel> map = new HashMap<Entity, MapperModel>();
    // Loop thru the entities, creating a subset by extracting out only the grid properties.
    Set<Entity> set = new HashSet<Entity>();
    for (Entity entity : entities) {
      if (entity instanceof Grid3d) {
        set.add(entity);
      }
    }

    // Loop thru the subset of grid properties, setting default datastore mapper properties for each.
    for (Entity entity : set.toArray(new Entity[0])) {
      Grid3d grid = (Grid3d) entity;
      GridMapperModel model = new GridMapperModel();
      // Default the directory to the working directory if the Preferences key has not been set yet.
      String directory = PreferencesUtil.getPreferencesStore(ServiceComponent.PLUGIN_ID).get("SaveModSpecGrid_DIR",
          Utilities.getWorkingDirectory());
      model.setDirectory(directory);
      // Default the file name to the grid display name.
      model.setFileName(grid.getDisplayName());
      // Default the x,y units to the application x,y units (this is persisted in the meta file).
      model.setXyUnit(UnitPreferences.getInstance().getHorizontalDistanceUnit());
      // Default the data domain to the domain of the grid data unit (this is persisted in the meta file).
      Unit dataUnit = grid.getDataUnit();
      // Default the data unit to the grid data unit (this is persisted in the meta file).
      model.setDataUnit(dataUnit);
      // Default the data format to ASCII.
      model.setFileFormat(GridFileFormat.ASCII);
      // Default the orientation to the grid orientation (this is persisted in the meta file).
      //model.setOrientation(computeGridOrientation(grid));
      map.put(entity, model);
    }
    return map;
  }

  @Override
  public IStatus initialize() {
    // No initialization necessary.
    return Status.OK_STATUS;
  }

}
