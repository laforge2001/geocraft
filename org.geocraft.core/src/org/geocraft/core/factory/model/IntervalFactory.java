/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.geologicfeature.GeologicInterval;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.service.ServiceProvider;


public class IntervalFactory {

  /**
   * Constructs a geologic interval given a name and a mapper.
   * 
   * @param name
   *          the name of the geologic feature.
   * @param mapper
   *          the mapper.
   * @return the constructed geologic interval.
   */
  public static GeologicInterval create(final String name, final IMapper mapper) {
    return new GeologicInterval(name, mapper);
  }

  /**
   * Constructs a geologic interval given a prototype interval, name and a
   * mapper.
   * 
   * @param prototype
   *          the prototype interval.
   * @param name
   *          the name of the geologic interval.
   * @param mapper
   *          the mapper.
   * @return the constructed geologic interval.
   */
  public static GeologicInterval create(final GeologicInterval prototype, final String name, final IMapper mapper) {
    GeologicInterval prototypeInterval = prototype;
    GeologicInterval interval = new GeologicInterval(name, mapper);

    copyInterval(interval, prototypeInterval);
    return interval;
  }

  /**
   * Constructs a geologic interval given a prototype interval and a name.
   * 
   * @param prototype
   *          the prototype interval.
   * @param name
   *          the name of the geologic interval.
   * @return the constructed geologic interval.
   */
  public static GeologicInterval create(final GeologicInterval prototype, final String name) {
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);

    GeologicInterval interval = new GeologicInterval(name, mapper);

    copyInterval(interval, prototype);
    return interval;
  }

  /**
   * Copies the properties from one interval to another.
   * 
   * @param interval
   *          to interval on which to set the properties.
   * @param prototype
   *          the prototype interval from which to get properties.
   */
  private static void copyInterval(final GeologicInterval interval, final GeologicInterval prototype) {
    interval.setParent(prototype.getParent());
    interval.setBaseBoundary(prototype.getBaseBoundary());
    interval.setTopBoundary(prototype.getTopBoundary());
    interval.setInfillType(prototype.getInfillType());
    interval.setComment(prototype.getComment());
    interval.setNumLayers(prototype.getNumLayers());
    interval.setProjectName(prototype.getProjectName());
    interval.setDirty(false);
  }

}
