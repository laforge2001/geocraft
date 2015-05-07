/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import org.geocraft.core.model.Entity;


/**
 * An interface that all entity factories have to implement.
 */
public interface IEntityFactory {

  // TODO: better names for these methods

  /**
   * Create new entity from an old one with a new name.
   */
  Entity create(Entity prototype, String name) throws Exception;

}