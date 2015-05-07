/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.io;


import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;


/**
 * The interface for the datastore accessor services.
 * The service allows other bundles to query for all the datastore accessors
 * currently registered for I/O. These are returned as datastore accessors.
 */
public interface IDatastoreAccessorService extends IMapperFactory {

  /**
   * Returns all the available datastore accessors registered with with service.
   * 
   * @return the array of datastore accessors.
   */
  IDatastoreAccessor[] getDatastoreAccessors();

  /**
   * Restores an entity from a mapper model.
   * This is used in the session restore.
   * 
   * @param model the mapper model used to restore the entity.
   */
  void restoreEntityFromMapperModel(final MapperModel model);

  /**
   * Creates a mapper model based on its class name.
   * This method loops thru the registered datastore accessors, comparing the
   * model names in each with the given class name. When a match is found, an
   * instance of the model is instantiated. If none is found, then null is
   * returned.
   * 
   * @param modelClassName the class name of the mapper model to search for.
   * @return the created mapper model; or <i>null</i> if none found.
   */
  MapperModel createMapperModelFromClassName(String modelClassName);

  IDatastoreAccessor[] getDatastoreAccessors(Class[] klasses, IOMode ioMode);
}