/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.aoi;


import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.MapperModel;


public class AOITestMapper extends AbstractMapper {

  private final AOITestMapperModel _model;

  public AOITestMapper() {
    _model = new AOITestMapperModel();
  }

  @Override
  protected void createInStore(final Entity entity) {
    // Nothing to do.
  }

  @Override
  protected void deleteFromStore(final Entity entity) {
    // Nothing to do.
  }

  @Override
  protected void readFromStore(final Entity entity) {
    // Nothing to do.
  }

  @Override
  protected void updateInStore(final Entity entity) {
    // Nothing to do.
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "In Memory";
  }

  public String getDatastore() {
    return "In-Memory";
  }

  @Override
  protected AOITestMapperModel getInternalModel() {
    return _model;
  }

  public AOITestMapper factory(final MapperModel mapperModel) {
    return null;
  }

  public AOITestMapperModel getModel() {
    AOITestMapperModel model = new AOITestMapperModel();
    model.updateFrom(_model);
    return model;
  }

}
