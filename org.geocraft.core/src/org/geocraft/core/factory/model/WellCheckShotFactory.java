/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.well.WellCheckShot;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.internal.core.factory.FactoryHelper;


/**
 * The only way to create a WellCheckShot object is via this Factory.
 */

public class WellCheckShotFactory implements IEntityFactory {

  private static WellCheckShotFactory _factory;

  private WellCheckShotFactory() {
    // singleton
  }

  public static synchronized WellCheckShotFactory getInstance() {
    if (_factory == null) {
      _factory = new WellCheckShotFactory();
    }
    return _factory;
  }

  public Entity create(final Entity prototype, final String name) {
    WellCheckShot prototypeWellCheckShot = (WellCheckShot) prototype;
    //IMapper mapper = prototypeWellCheckShot.getMapper().copy();

    // Get a mapper of the same type as the prototype
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);
    WellCheckShot wellCheckShot = new WellCheckShot(name, mapper, prototypeWellCheckShot.getWell());

    // Run the auto-copy
    copyPrototype(wellCheckShot, prototypeWellCheckShot, prototypeWellCheckShot.getProjectName());

    return wellCheckShot;
  }

  public Entity create(final Entity prototype, final IMapper mapper, final String name) {
    WellCheckShot prototypeWellCheckShot = (WellCheckShot) prototype;
    WellCheckShot wellCheckShot = new WellCheckShot(name, mapper, prototypeWellCheckShot.getWell());

    copyPrototype(wellCheckShot, prototypeWellCheckShot, prototypeWellCheckShot.getProjectName());

    return wellCheckShot;
  }

  //TODO Abstract this back up to OpenSpiritFactory
  private WellCheckShot copyPrototype(final WellCheckShot wellCheckShot, final WellCheckShot prototype,
      final String projectName) {

    // Save off the (potentially) new name
    String wellCheckShotName = wellCheckShot.getDisplayName();

    // Copy over all the attributes
    FactoryHelper.copyFromEntity(prototype, wellCheckShot, false);

    // Reset a few
    wellCheckShot.setProjectName(projectName);
    wellCheckShot.setDisplayName(wellCheckShotName);

    wellCheckShot.setDirty(false);
    return wellCheckShot;
  }
}
