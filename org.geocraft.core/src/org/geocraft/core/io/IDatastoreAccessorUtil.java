/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.io;


import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.MapperModel;


/**
 * The basic interface for a datastore accessor.
 * There will be an implementation of this for each data store/entity combination
 * (e.g. ModSpec Grids, SEG-Y Volumes).
 */
public interface IDatastoreAccessorUtil {

  /**
   * Initializes the accessor connection to the datastore. This could be 
   * used to prompt a user to select a 'project' prior to importing data. 
   * 
   * This is not needed in the simple case where the datastore saves it's 
   * data to disk. 
   
   * @return the connection status.
   */
  IStatus initialize();

  /**
   * Creates a map of mapper parameters model with default values from the specified entity.
   * When doing an "export..." on selected entities to a particular datastore, it is necessary
   * to create a mapper for each entity, which requires a model of mapper parameters. A model
   * can be created and then filled in with as much default information as possible from the entity.
   * That is part of the logic of this method. The other part is to resolve the entities selected
   * for output into entities that can actually be output.
   * @param entities the entities for which to get default values.
   * @return a map of mapper parameters model.
   */
  Map<Entity, MapperModel> mapEntitiesToModels(Entity[] entities);
}
