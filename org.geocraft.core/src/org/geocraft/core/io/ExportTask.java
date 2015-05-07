/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.io;


import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.MapperModel;


public abstract class ExportTask extends RepositoryTask {

  public abstract void setEntity(Entity entity);

  public abstract void setMapperModel(MapperModel mapperModel);

}
