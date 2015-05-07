/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.culture.Layer;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.internal.core.factory.FactoryHelper;


/**
 * TODO cleanup this factory and change the Layer constructor to the standard
 * signature of (name, mapper).
 */
public class LayerFactory {

  public static Layer create(final Layer prototype, final IMapper mapper, final String name) {

    Layer newLayer = new Layer(name, mapper, prototype.getLayerType(), prototype.getAttributeNames(), prototype
        .getAttributeTypes());

    FactoryHelper.copyFromEntity(prototype, newLayer, false);

    return newLayer;
  }

  public static Layer create(final Layer prototype, final String name) {
    // Copy the mapper
    //IMapper mapper = prototype.getMapper().copy();
    // Get a mapper of the same type as the prototype
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);

    return create(prototype, mapper, name);
  }
}
