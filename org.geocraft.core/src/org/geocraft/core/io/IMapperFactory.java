/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.io;


import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.MapperModel;


/**
 * The interface for mapper factories.
 * Mapper factories are responsible for creating instances of mapper objects, which are used to read/write data from a datastore.
 */
public interface IMapperFactory {

  /**
   * Creates a new mapper object based on the properties from another mapper.
   * @param mapper the prototype mapper to replicate, and from which to obtain the properties.
   * @param name the mapper name.
   * @return a new mapper object.
   */
  IMapper createMapper(IMapper mapper, String name);

  /**
   * Creates a new mapper object based on the properties from another mapper model.
   * @param mapper the prototype mapper to replicate.
   * @param model the mapper model from which to obtain the properties.
   * @param name the mapper name.
   * @return a new mapper object.
   */
  IMapper createMapper(IMapper mapper, MapperModel model, String name);
}
