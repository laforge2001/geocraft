/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.geologicfeature.GeologicHorizon;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.service.ServiceProvider;


public class HorizonFactory {

  /**
   * Constructs a geologic horizon given a name and a mapper.
   * 
   * @param name
   *          the name of the geologic feature.
   * @param mapper
   *          the mapper.
   * @return the constructed geologic horizon.
   */
  public static GeologicHorizon create(final String name, final IMapper mapper) {
    return new GeologicHorizon(name, mapper);
  }

  /**
   * Constructs a geologic horizon given a prototype horizon, name and a mapper.
   * 
   * @param prototype
   *          the prototype horizon.
   * @param name
   *          the name of the geologic horizon.
   * @param mapper
   *          the mapper.
   * @return the constructed geologic horizon.
   */
  public static GeologicHorizon create(final GeologicHorizon prototype, final String name, final IMapper mapper) {
    GeologicHorizon prototypeHorizon = prototype;
    GeologicHorizon horizon = new GeologicHorizon(name, mapper);

    copyHorizon(horizon, prototypeHorizon);
    return horizon;
  }

  /**
   * Constructs a geologic horizon given a prototype horizon and a name.
   * 
   * @param prototype
   *          the prototype horizon.
   * @param name
   *          the name of the geologic horizon.
   * @return the constructed geologic horizon.
   */
  public static GeologicHorizon create(final GeologicHorizon prototype, final String name) {
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);

    GeologicHorizon horizon = new GeologicHorizon(name, mapper);

    copyHorizon(horizon, prototype);
    return horizon;
  }

  /**
   * Copies the properties from one horizon to another.
   * 
   * @param horizon
   *          to horizon on which to set the properties.
   * @param prototype
   *          the prototype horizon from which to get properties.
   */
  private static void copyHorizon(final GeologicHorizon horizon, final GeologicHorizon prototype) {

    horizon.setClassification(prototype.getClassification());
    horizon.setComment(prototype.getComment());
    horizon.setGeologicalAge(prototype.getGeologicalAge());
    horizon.setProjectName(prototype.getProjectName());
    horizon.setDirty(false);
  }
}
