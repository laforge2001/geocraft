/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.io;


import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.MapperModel;


/**
 * Defines the abstract base class used for all tasks that import data into the application.
 */
public abstract class ImportTask extends RepositoryTask {

  /**
   * Sets the mapper model to be used in the import task.
   * 
   * @param mapperModel the mapper model.
   */
  public abstract void setMapperModel(MapperModel mapperModel);

  protected String getAlreadyExistsErrorMessage(final Entity entity) {
    return "The " + entity.getType() + " \'" + entity.getDisplayName() + "\' already exists in the repository.";
  }
}
