/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.javaseis;


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
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.internal.io.javaseis.ServiceComponent;
import org.javaseis.properties.DataFormat;


public class VolumeAccessorUtil implements IDatastoreAccessorUtil {

  public VolumeAccessorUtil() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public IStatus initialize() {
    return Status.OK_STATUS;
  }

  @Override
  public Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities) {
    Map<Entity, MapperModel> map = new HashMap<Entity, MapperModel>();
    // Loop thru the entities, creating a subset by extracting out only the seismic dataset entities.
    Set<Entity> set = new HashSet<Entity>();
    for (Entity entity : entities) {
      if (entity instanceof SeismicDataset) {
        set.add(entity);
      }
    }

    // Loop thru the subset of seismic datasets, setting default datastore mapper properties for each.
    for (Entity entity : set.toArray(new Entity[0])) {
      SeismicDataset seismicDataset = (SeismicDataset) entity;
      VolumeMapperModel model = new VolumeMapperModel();
      model.setVolumeType(entity.getClass().getSimpleName());
      // Default the directory to the working directory.
      String directory = PreferencesUtil.getPreferencesStore(ServiceComponent.PLUGIN_ID).get("Workspace_DIR",
          Utilities.getWorkingDirectory());
      model.setDirectory(directory);
      // Default the file name to the seismic dataset display name.
      model.setFileName(seismicDataset.getDisplayName());
      // Default the storage order to the preferred order of the seismic dataset.
      if (seismicDataset instanceof PostStack3d) {
        model.setStorageOrder(((PostStack3d) seismicDataset).getPreferredOrder().getTitle());
      } else if (seismicDataset instanceof PreStack3d) {
        model.setStorageOrder(((PreStack3d) seismicDataset).getPreferredOrder().getName());
      }
      // Default the unit of x,y to the application preferences.
      model.setUnitOfXY(UnitPreferences.getInstance().getHorizontalDistanceUnit());
      // Default the unit of z to the seismic dataset unit of z.
      model.setUnitOfZ(seismicDataset.getZUnit());
      // Default the data unit to the seismic dataset data unit.
      Unit dataUnit = seismicDataset.getDataUnit();
      // Default the data domain to the domain of the seismic dataset data unit.
      model.setDataUnit(dataUnit);
      // Default the sample format to FLOAT.
      model.setDataFormat(DataFormat.FLOAT.toString());
      map.put(entity, model);
    }
    return map;
  }

}
