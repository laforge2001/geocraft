/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.mapper;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.Entity;
import org.geocraft.core.query.IProjectQuery;


/**
 * This class is a mapper for entities that have no associated with
 * a datastore and are considered to be entirely in-memory. As such,
 * all of the datastore methods (create,read,update,delete) are empty.
 * The unique ID for an in-memory mapper is generated from a counter
 * that is simply incremented with each construction of a new mapper.
 */
public class InMemoryMapper<E extends Entity> extends AbstractMapper<E> {

  /** The counter for generating new unique IDs. */
  private static int _currentID = 0;

  public static String createUniqueID() {
    _currentID++;
    return "" + _currentID;
  }

  private final Class _entityClass;

  private final InMemoryMapperModel _model;

  private final String _id;

  public InMemoryMapper(final Class entityClass) {
    _entityClass = entityClass;
    _id = createUniqueID();
    _model = new InMemoryMapperModel(_id);
  }

  public String getName() {
    return _id;
  }

  public InMemoryMapperModel getModel() {
    return _model;
  }

  @Override
  protected InMemoryMapperModel getInternalModel() {
    return _model;
  }

  @Override
  public boolean existsInStore() {
    return false;
  }

  @Override
  public boolean existsInStore(final String name) {
    return existsInStore();
  }

  @Override
  public void create(final E entity) {
    // The entity is in-memory, so there is no creation to be done.
  }

  @Override
  public void read(final E entity, final IProgressMonitor monitor) {
    // The entity is in-memory, so there is no reading to be done.
    monitor.done();
  }

  @Override
  public void update(final E entity) {
    // The entity is in-memory, so there is no updating to be done.
  }

  @Override
  public void delete(final E entity) {
    // The entity is in-memory, so there is no deletion to be done.
  }

  @Override
  protected void createInStore(final E entity) {
    // The entity is in-memory, so there is no creation to be done.
  }

  @Override
  protected void readFromStore(final E entity) {
    // The entity is in-memory, so there is no reading to be done.
  }

  @Override
  protected void updateInStore(final E entity) {
    // The entity is in-memory, so there is no updating to be done.
  }

  @Override
  protected void deleteFromStore(final E entity) {
    // The entity is in-memory, so there is no deletion to be done.
  }

  public InMemoryMapper<E> factory(final MapperModel mapperModel) {
    return new InMemoryMapper<E>(_entityClass);
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "In-Memory " + _entityClass.getName();
  }

  public String getDatastore() {
    return "In-Memory";
  }

  @Override
  public IProjectQuery getProjectQuery() {
    // not a database, so no query support is available
    return null;
  }

}
