/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.geomath.algorithm.velocity;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.repository.specification.AbstractSpecification;


/**
 * Defines a filter for <code>PostStack3d</code> entities with data in the velocity domain.
 */
public class VelocityVolumeSpecification extends AbstractSpecification {

  @Override
  public boolean isSatisfiedBy(Object obj) {
    if (obj != null && PostStack3d.class.isAssignableFrom(obj.getClass())) {
      PostStack3d volume = (PostStack3d) obj;
      // Check that the domain of the volume data unit is velocity.
      return volume.getDataDomain() == Domain.VELOCITY;
    }
    return false;
  }
}
