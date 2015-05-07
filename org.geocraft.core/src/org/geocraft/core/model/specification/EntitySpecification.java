/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.Entity;
import org.geocraft.core.repository.specification.AbstractSpecification;


/**
 * A filter that will find any matching Entities in the object manager. 
 */

public class EntitySpecification extends AbstractSpecification {

  private Entity _entity;

  /**
   * Look for the specified Entity. 
   * 
   * @param entity the Entity to match to. 
   */
  public EntitySpecification(Entity entity) {
    _entity = entity;
  }

  /**
   * Compares the Entity's mapper contents to see if they refer
   * to the same Entity. 
   */
  public boolean isSatisfiedBy(Object obj) {
    if (obj.getClass().isAssignableFrom(_entity.getClass())) {
      Entity target = (Entity) obj;
      if (_entity.getUniqueID().equals(target.getUniqueID())) {
        return true;
      }
    }
    return false;
  }

}
