/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.geologicfeature.GeologicFault;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.service.ServiceProvider;


public class GeologicFaultFactory {

  /**
   * Constructs a geologic fault given a name and a mapper.
   * 
   * @param name
   *          the name of the geologic feature.
   * @param mapper
   *          the mapper.
   * @return the constructed geologic fault.
   */
  public static GeologicFault create(final String name, final IMapper mapper) {
    return new GeologicFault(name, mapper);
  }

  /**
   * Constructs a geologic fault given a prototype fault, name and a mapper.
   * 
   * @param prototype
   *          the prototype fault.
   * @param name
   *          the name of the geologic fault.
   * @param mapper
   *          the mapper.
   * @return the constructed geologic fault.
   */
  public static GeologicFault create(final GeologicFault prototype, final String name, final IMapper mapper) {
    GeologicFault prototypeGeologicFault = prototype;
    GeologicFault fault = new GeologicFault(name, mapper);

    copyFault(fault, prototypeGeologicFault);
    return fault;
  }

  /**
   * Constructs a geologic fault given a prototype fault and a name.
   * 
   * @param prototype
   *          the prototype fault.
   * @param name
   *          the name of the geologic fault.
   * @return the constructed geologic fault.
   */
  public static GeologicFault create(final GeologicFault prototype, final String name) {
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);

    GeologicFault fault = new GeologicFault(name, mapper);

    copyFault(fault, prototype);
    return fault;
  }

  /**
   * Copies the properties from one fault to another.
   * 
   * @param fault
   *          to fault on which to set the properties.
   * @param prototype
   *          the prototype fault from which to get properties.
   */
  private static void copyFault(final GeologicFault geologicFault, final GeologicFault prototype) {
    geologicFault.setComment(prototype.getComment());
    geologicFault.setFaultType(prototype.getFaultType());
    geologicFault.setGeologicalAge(prototype.getGeologicalAge());
    geologicFault.setProjectName(prototype.getProjectName());
    geologicFault.setDirty(false);
  }
}
