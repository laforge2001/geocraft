/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import java.io.IOException;

import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.EarthModel;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.service.ServiceProvider;


/**
 * Factory methods for creating EarthModel entities.
 */
public class EarthModelFactory {

  /**
   * Creates an <code>EarthModel</code> entity backed by the specified mapper.
   * This factory method is used primarily by load/export tasks.
   * 
   * @param name
   *          the entity name.
   * @param mapper
   *          the entity mapper.
   * @return the created EarthModel.
   * @throws IOException
   *           thrown if the earth model cannot be created.
   */
  public static EarthModel create(final EarthModel prototype, final String name) throws IOException {
    // Make sure the prototype is loaded.
    prototype.load();

    // Create a new mapper based on the prototype's mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);

    // Create a new entity and copy properties from the prototype.
    EarthModel earthModel = new EarthModel(name, prototype.getPrimaryZDomain(), mapper);
    copyPrototype(prototype, earthModel);

    // Return the new entity.
    return earthModel;
  }

  /**
   * Copies the important properties from a prototype EarthModel entity to
   * another.
   * 
   * @param prototype
   *          the prototype EarthModel entity.
   * @param earthModel
   *          the destination EarthModel entity.
   */
  private static void copyPrototype(final EarthModel prototype, final EarthModel earthModel) {
    // Copy the properties from the prototype entity.
    earthModel.setDescription(prototype.getDescription());
    earthModel.setProjectName(prototype.getProjectName());
    earthModel.setDirty(false);
  }
}
