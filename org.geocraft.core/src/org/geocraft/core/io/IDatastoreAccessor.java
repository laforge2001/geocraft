/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.io;


import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;


/**
 * The basic interface for a datastore accessor.
 * There will be an implementation of this for each data store/entity combination
 * (e.g. ModSpec Grids, SEG-Y Volumes).
 */
public interface IDatastoreAccessor {

  /**
   * Returns the ID of the datastore accessor.
   */
  String getId();

  /**
   * Returns the name of the datastore accessor (e.g. ModSpec Grid, SEG-Y Volume).
   */
  String getName();

  /**
   * Returns the category of the datastore accessor (e.g. Grids, 3D Seismic).
   */
  String getCategory();

  /**
   * Returns a flag indicating if this datastore accessor is visible.
   * <p>
   * Setting the visibility flag in the extension point is a way of
   * enabling/disabling a particular datastore connection without having
   * to remove the extension itself.
   * 
   * @return <i>true</i> if visible; <i>false</i> if not.
   */
  boolean isVisible();

  /**
   * Calls the initialization method in the datastore accessor util class.
   * For some datastore initialization might be necessary (e.g. to open a project, etc).
   * 
   * @return the status of the initialization.
   */
  IStatus initialize();

  /**
   * Returns <i>true</i> if the datastore accessor supports input, <i>false</i> if not.
   */
  boolean canInput();

  /**
   * Returns <i>true</i> if the datastore accessor supports output, <i>false</i> if not.
   */
  boolean canOutput();

  /**
   * Returns an array of the mapper class names supported by the datastore accessor.
   */
  String[] getMapperClassNames();

  /**
   * Returns the mapper model class names supported by the datastore accessor.
   */
  String getMapperModelClassName(IOMode ioMode);

  /**
   * Returns an array of the entity class names supported by the datastore accessor.
   */
  String[] getSupportedEntityClassNames();

  /**
   * Creates a mapping for the specified entities to mapper models.
   * 
   * @param entities the entities.
   * @return the mapping of entities to mapper models.
   */
  Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities);

  /**
   * Creates an input selector for entries in the datastore.
   * 
   * @return the datastore entry selector.
   */
  DatastoreEntrySelector createInputSelector();

  /**
   * Creates an output selector for locations in the datastore.
   * 
   * @return the datastore location selection.
   */
  IDatastoreLocationSelector createOutputSelector();

  /**
   * Creates a model of mapper properties.
   * 
   * @param ioMode the I/O mode (Input or Output).
   * @return the mapper model.
   */
  MapperModel createMapperModel(IOMode ioMode);

  /**
   * Creates the task for importing one or more entities from the datastore.
   * 
   * @return the import task.
   */
  ImportTask createImportTask();

  /**
   * Creates the task for exporting an entity to the datastore.
   * 
   * @return the export task.
   */
  ExportTask createExportTask();

  /**
   * Creates a mapper.
   * 
   * @param ioMode the I/O mode (Input or Output).
   * @param model the model of mapper properties.
   * @return the mapper;
   */
  IMapper createMapper(IOMode ioMode, MapperModel model);

}
