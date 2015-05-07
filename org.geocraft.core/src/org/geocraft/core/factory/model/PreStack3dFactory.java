/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.factory.model;


import org.geocraft.core.model.mapper.IPreStack3dMapper;
import org.geocraft.core.model.seismic.PreStack3d;


/**
 * Factory methods for creating PreStack3d entities.
 */
public class PreStack3dFactory {

  /**
   * Creates a PreStack3d entity backed by the specified mapper.
   * This factory method is used primarily by load/export tasks.
   * 
   * @param name the entity name.
   * @param mapper the entity mapper.
   * @return the created PreStack3d.
   */
  public static PreStack3d create(final String name, final IPreStack3dMapper mapper) {
    // Create and return the new entity.
    return new PreStack3d(name, mapper);
  }
}
