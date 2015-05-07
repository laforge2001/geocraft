/*
 * Copyright (C) ConocoPhillips 2007 - 2008 All Rights Reserved.
 */
package org.geocraft.core.model.mapper;


import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.Entity;
import org.geocraft.core.query.IProjectQuery;


/**
 * Maps the domain specific entity into the datastore. Defines the
 * methods that all mapper's must provide to the entity for reading
 * and writing data.
 */
public interface IMapper<E extends Entity> {

  /**
   * Constructs a new mapper with the given mapper model.
   * This is like a copy constructor.
   *
   * @param mapperModel the model of mapper properties.
   * @return the new mapper.
   */
  IMapper factory(final MapperModel mapperModel);

  /**
   * Return a project query interface for the datastore / project associated with this mapper
   * @throws SQLException
   */
  IProjectQuery getProjectQuery() throws SQLException;

  /**
   * Gets the datastore entry description.
   */
  String getDatastoreEntryDescription();

  /**
   * Gets the name of the underlying datastore to which this mapper connects.
   *
   * @return the name of the underlying datastore.
   */
  String getDatastore();

  /**
   * Gets the storage directory (optional) of the underlying datastore.
   *
   * @return the storage directory; or an empty string if not applicable.
   */
  String getStorageDirectory();

  /**
   * Returns a unique identifier string for the entity mapper.
   * <p>
   * By default this is a combination of the datastore name and the id of the entry in the datastore.
   *
   * @return a unique identifier string for the entity mapper.
   */
  String getUniqueID();

  /**
   * Returns a copy of the model used for mapping the associated entity to the datastore.
   * <p>
   * This method is used by the entity factories when creating mappers for a new entity based on a prototype entity,
   * and therefore the unique identifier field will be cleared out.
   *
   * @return the model of the mapper properties.
   */
  MapperModel getModel();

  /**
   * Creates an entry for the entity in the datastore.
   * <p>
   * The entity must be in the <i>LOADED</i> state.
   *
   * @param entity the entity.
   * @throws IOException thrown on I/O error.
   */
  void create(final E entity) throws IOException;

  /**
   * Reads the properties of an entity from the datastore.
   * <p>
   * The entity must be in the <i>GHOST</i> state.
   *
   * @param entity the entity.
   * @param the progress monitor for long-running tasks.
   * @throws IOException thrown on I/O error.
   */
  void read(final E entity, final IProgressMonitor monitor) throws IOException;

  /**
   * Updates the entry for an entity in the datastore.
   * <p>
   * The entity must be in the <i>LOADED</i> state.
   *
   * @param entity the entity.
   * @throws IOException thrown on I/O error.
   */
  void update(final E entity) throws IOException;

  /**
   * Deletes the entry for an entity from the datastore.
   * <p>
   * The entity must be in the <i>LOADED</i> state.
   *
   * @param entity the entity.
   * @throws IOException thrown on I/O error.
   */
  void delete(final E entity) throws IOException;

  /**
   * Checks if the current mapper reflects an existing entry in the underlying datastore.
   *
   * @return <i>true</i> if the entry already exists in the data store, <i>false</i> if not.
   */
  boolean existsInStore();

  /**
   * Checks if an entry with the given name already exists in the underlying datastore.
   * <p>
   * This method can be used to determine if it is safe to write an entity to a store
   * without overwriting some other data.
   *
   * @param proposedName the name to check.
   */
  boolean existsInStore(final String proposedName);

  /**
   * Validate the proposed name for a new entry in the underlying datastore.
   * Datastores often have naming restrictions (length, special characters, etc)
   * and this method allows for implementation of the naming logic.
   *
   * @param proposedName the name of the proposed entry.
   * @return the validation status of the proposed name.
   */
  IStatus validateName(final String proposedName);

  /**
   * Used to reinitialize a datastore entry (e.g. for SEG-Y, this means clearing
   * out existing traces so the file can be re-written).
   *
   * @throws IOException thrown on initialization error.
   */
  void reinitialize() throws IOException;

  /**
   * Used to create an output display name in situations where the input display name
   * consists of multiple fields.  The nameSuffix is appended to the correct field
   * in the inputDisplayName.  For R5K Mappers, this is the place to set the interpreter
   * field correctly.
   */
  String createOutputDisplayName(String inputDisplayName, String nameSuffix);

}
