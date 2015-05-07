/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.query.IProjectQuery;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


/**
 * This is the abstract base class for all mappers.
 * A mapper is responsible for mapping between the
 * domain entities and datastore entries. Entities
 * that are loaded from a datastore will contain a
 * reference to a mapper instance to that datastore.
 * Entities that are created by the user also require
 * a mapper reference passed into their constructor.
 * Factory methods are available to assist in auto-
 * generating a mapper from a prototype. Optionally,
 * an <code>InMemoryMapper</code> can be created for
 * entities that are small enough to be kept in memory
 * and do not require automatic persistence.
 */
public abstract class AbstractMapper<E extends Entity> implements IMapper<E> {

  /** Synchronizer token object */
  protected static final Object TOKEN = new Object();

  /**
   * Returns a flag indicating if another object is "equal" to this one.
   * <p>
   * Two mappers are equal if they have the same unique ID.
   * 
   * @return <i>true</i> if the other object is a mapper with the same unique ID; otherwise <i>false</i>.
   */
  @Override
  public boolean equals(final Object object) {
    if (!(object instanceof IMapper)) {
      return false;
    }
    IMapper other = (IMapper) object;

    return getUniqueID().equals(other.getUniqueID());
  }

  /**
   * The <code>equals</code> method was overridden, so the <code>hashCode</code> must also be overridden.
   */
  @Override
  public int hashCode() {
    HashCode hashCode = new HashCode();
    hashCode.add(getUniqueID());
    return hashCode.getHashCode();
  }

  public void create(final E entity) throws IOException {
    // If the entity is not in the loaded state, then simply return.
    if (!entity.isLoaded()) {
      getLogger().warn("Loading entity " + entity + " in create(entity) method");
      entity.load();
      //      getLogger().warn("Could not create entity " + entity + " in datastore. It is not yet loaded.");
      //      return;
    }
    // Call the datastore-specific logic to create the entity in the underlying datastore.
    createInStore(entity);
    update(entity);
  }

  public void read(final E entity, final IProgressMonitor monitor) throws IOException {
    // Resolve the mapper unique ID, if it was a temporary one.
    MapperModel model = getInternalModel();
    if (model == null) {
      throw new IllegalStateException(
          "Mapper was null! This used to be silently ignored but I don't think it should ever happen?");
    }
    // Call the datastore-specific logic to read the entity from the underlying datastore.
    readFromStore(entity, monitor);
  }

  public void update(final E entity) throws IOException {
    // If the entity is not in the loaded state, then simply return.
    if (!entity.isLoaded()) {
      getLogger().warn("Could not update entity " + entity + " in datastore. It is not yet loaded.");
      return;
    }
    // Call the datastore-specific logic to update the entity in the underlying datastore.
    updateInStore(entity);
  }

  public void delete(final E entity) throws IOException {
    if (!entity.isLoaded()) {
      getLogger().warn("Could not delete entity " + entity + " in datastore. It is not yet loaded.");
      return;
    }
    deleteFromStore(entity);
  }

  public String getUniqueID() {
    return getDatastoreEntryDescription() + " : " + getInternalModel().getUniqueId();
  }

  /**
   * Gets the model of mapper parameters.
   * <p>
   * This method return the actual model used by the mapper, as opposed
   * to the <code>getModel</code> method, which returns a copy.
   * 
   * @param model the model of mapper parameters.
   */
  protected abstract MapperModel getInternalModel();

  /**
   * Creates an entity in the underlying datastore.
   * 
   * @param entity the entity.
   * @throws IOException thrown on I/O error.
   */
  protected abstract void createInStore(final E entity) throws IOException;

  /**
   * Reads an entity from the underlying datastore.
   * 
   * @param entity the entity to read from the datastore.
   * @throws IOException thrown on I/O error.
   */
  protected void readFromStore(final E entity, final IProgressMonitor monitor) throws IOException {
    readFromStore(entity);
  }

  /**
   * Reads an entity from the underlying datastore.
   * 
   * @param entity the entity to read from the datastore.
   * @throws IOException thrown on I/O error.
   */
  protected abstract void readFromStore(final E entity) throws IOException;

  /**
   * Updates an entity in the underlying datastore.
   * 
   * @param entity the entity to update in the datastore.
   * @throws IOException thrown on I/O error.
   */
  protected abstract void updateInStore(final E entity) throws IOException;

  /**
   * Deletes an entity from the underlying datastore.
   * 
   * @param entity the entity to delete from the datastore.
   * @throws IOException thrown on I/O error.
   */
  protected abstract void deleteFromStore(final E entity) throws IOException;

  /**
   * Returns the logger to use for logging messages.
   * 
   * @return the logger to use.
   */
  protected ILogger getLogger() {
    return ServiceProvider.getLoggingService().getLogger(getClass());
  }

  public abstract String getDatastoreEntryDescription();

  public String getStorageDirectory() {
    return "";
  }

  public boolean existsInStore() {
    return getInternalModel().existsInStore();
  }

  public boolean existsInStore(final String name) {
    return getInternalModel().existsInStore(name);
  }

  public IStatus validateName(final String proposedName) {
    return getInternalModel().validateName(proposedName);
  }

  /**
   * @throws IOException  
   */
  public void reinitialize() throws IOException {
    // This is datastore-dependent, so datastores should override this method as necessary.
  }

  public IProjectQuery getProjectQuery() throws SQLException {
    return null;
  }

  public String createOutputDisplayName(final String inputDisplayName, final String nameSuffix) {
    return inputDisplayName + nameSuffix;
  }

}
