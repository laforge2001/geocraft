/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import java.util.Map;

import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.FloatMeasurementSeries;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.IWellMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.core.model.well.WellDomain;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.internal.core.factory.FactoryHelper;


/**
 * The only way to create a Well object is via this Factory.
 */

public class WellFactory {

  // Create a new well like the prototype, but only restrict the wellbore from mdStart to mdEnd
  public static Well create(final IRepository repository, final Well prototype, final String name,
      final String identifier, final String identifierType, final float mdStart, final float mdEnd, final String comment) {
    String datastoreName = prototype.getDatastore();
    if (datastoreName == null || datastoreName.length() == 0) {
      Well well = createInMemory(name, identifier, identifierType);
      repository.add(well);
      return well;
    }

    IDatastoreAccessorService service = ServiceProvider.getDatastoreAccessorService();
    IDatastoreAccessor[] accessors = service.getDatastoreAccessors(new Class[] { Well.class }, IOMode.OUTPUT);
    for (IDatastoreAccessor accessor : accessors) {
      if (accessor.getName().startsWith(datastoreName)) {

        // Create a new mapper model based on the prototype volume.
        MapperModel model;
        try {
          model = accessor.mapEntitiesToModels(new Entity[] { prototype }).values().iterator().next();
        } catch (Exception e) {
          throw new RuntimeException("Could not create " + datastoreName + " " + Well.class + " mapper");
        }
        if (model == null) {
          throw new RuntimeException("Could not create " + datastoreName + " " + Well.class + " mapper");
        }
        model.updateUniqueId(name);

        // Create a new mapper based on the mapper model.
        IWellMapper mapper = (IWellMapper) accessor.createMapper(IOMode.OUTPUT, model);
        if (mapper == null) {
          throw new RuntimeException("Could not create " + datastoreName + " " + Well.class + " mapper");
        }

        // Update the necessary properties on the mapper.
        mapper.setWellName(name);
        mapper.setWellIdentifier(identifier);
        mapper.setWellIdentifierType(identifierType);

        // Get all the wells in the repository.
        Map<String, Object> map = repository.get(new TypeSpecification(Well.class));

        try {

          // Loop thru each one comparing its unique ID to the one to be created.
          for (Object object : map.values()) {
            Well well = (Well) object;
            // Returns the existing grid if its unique ID matches.
            if (well.getUniqueID().equals(mapper.getUniqueID())) {
              validateWell(well);
              throw new RuntimeException("Well already exists in repository, create failed.");
            }
          }

          return create(repository, prototype, name, mapper, identifier, identifierType, mdStart, mdEnd, comment);

        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    }
    Well well = createInMemory(name, identifier, identifierType);
    repository.add(well);
    return well;

  }

  public static Well create(final IRepository repository, final Well prototype, final String name,
      final String identifier, final String identifierType) {
    String datastoreName = prototype.getDatastore();
    if (datastoreName == null || datastoreName.length() == 0) {
      Well well = createInMemory(name, identifier, identifierType);
      repository.add(well);
      return well;
    }

    IDatastoreAccessorService service = ServiceProvider.getDatastoreAccessorService();
    IDatastoreAccessor[] accessors = service.getDatastoreAccessors(new Class[] { Well.class }, IOMode.OUTPUT);
    for (IDatastoreAccessor accessor : accessors) {
      if (accessor.getName().startsWith(datastoreName)) {

        // Create a new mapper model based on the prototype volume.
        MapperModel model;
        try {
          model = accessor.mapEntitiesToModels(new Entity[] { prototype }).values().iterator().next();
        } catch (Exception e) {
          throw new RuntimeException("Could not create " + datastoreName + " " + Well.class + " mapper");
        }
        if (model == null) {
          throw new RuntimeException("Could not create " + datastoreName + " " + Well.class + " mapper");
        }
        model.updateUniqueId(name);

        // Create a new mapper based on the mapper model.
        IWellMapper mapper = (IWellMapper) accessor.createMapper(IOMode.OUTPUT, model);
        if (mapper == null) {
          throw new RuntimeException("Could not create " + datastoreName + " " + Well.class + " mapper");
        }

        // Update the necessary properties on the mapper.
        mapper.setWellName(name);
        mapper.setWellIdentifier(identifier);
        mapper.setWellIdentifierType(identifierType);

        // Get all the wells in the repository.
        Map<String, Object> map = repository.get(new TypeSpecification(Well.class));

        try {

          // Loop thru each one comparing its unique ID to the one to be created.
          for (Object object : map.values()) {
            Well well = (Well) object;
            // Returns the existing grid if its unique ID matches.
            if (well.getUniqueID().equals(mapper.getUniqueID())) {
              validateWell(well);
              updateWell(well, identifier, identifierType);
              return well;
            }
          }

          return create(repository, prototype, name, mapper, identifier, identifierType);

        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    }
    Well well = createInMemory(name, identifier, identifierType);
    repository.add(well);
    return well;

  }

  /**
   * The common factory method for creating a new well entity. This allows for
   * specifying the well geometry, data unit and data values.
   * 
   * @param repository
   *          the repository in which to add the created grid.
   * @param name
   *          the name of the new grid.
   * @param mapper
   *          the mapper to the underlying datastore.
   * @param geometry
   *          the geometry of the new grid.
   * @param mapper
   *          the mapper of the new grid.
   * @param dataUnit
   *          the unit of measurement for the data values.
   * @param nullValue
   *          the null value for the grid.
   * @param dataValues
   *          the array of data values.
   * @return the created grid.
   */
  private static Well create(final IRepository repository, final Well prototype, final String name,
      final IWellMapper mapper, final String identifier, final String identifierType) throws Exception {

    // Create the new grid.
    Well well = new Well(name, mapper);

    // Copy over all the well attributes from the prototype
    FactoryHelper.copyFromEntity(prototype, well, false);

    // set information from name
    // Overwrite 
    well.setDisplayName(name);
    well.setLeaseName(name);
    well.setIdentifierAndType(identifier, identifierType);
    well.setDirty(false);

    // copy the wellbore and properties
    WellBore prototypeBore = prototype.getWellBore();
    WellBore wellBore = well.getWellBore();

    copyWellBore(prototypeBore, wellBore);

    updateWell(well, identifier, identifierType);

    // Check if the grid exists in the datastore.
    if (!DataSource.existsInStore(well)) {
      // If not, create it first.
      well.getMapper().create(well);
    } else {
      well.update();
    }
    well.markGhost();
    well.load();

    validateWell(well);

    // Add the grid to the repository.
    repository.add(well);
    return well;
  }

  private static Well create(final IRepository repository, final Well prototype, final String name,
      final IWellMapper mapper, final String identifier, final String identifierType, final float mdStart,
      final float mdEnd, final String comment) throws Exception {

    // Create the new grid.
    Well well = new Well(name, mapper);

    // Copy over all the well attributes from the prototype
    FactoryHelper.copyFromEntity(prototype, well, false);

    // set information from name
    // Overwrite 
    well.setDisplayName(name);
    well.setLeaseName(name);
    well.setIdentifierAndType(identifier, identifierType);
    well.setComment(comment);
    well.setDirty(false);

    // copy the wellbore and properties
    WellBore prototypeBore = prototype.getWellBore();
    WellBore wellBore = well.getWellBore();

    copyWellBore(prototypeBore, wellBore, mdStart, mdEnd);

    updateWell(well, identifier, identifierType);

    // Check if the grid exists in the datastore.
    if (!DataSource.existsInStore(well)) {
      // If not, create it first.
      well.getMapper().create(well);
    } else {
      well.update();
    }
    well.markGhost();
    well.load();

    validateWell(well);

    // Add the grid to the repository.
    repository.add(well);
    return well;
  }

  /**
   * Updates various properties of a grid. Afterwards, the grid is marked as
   * loaded.
   * 
   * @param grid
   *          the grid to update.
   * @param geometry
   *          the geometry of the grid.
   * @param dataUnit
   *          the unit of measurement for the data values.
   * @param dataValues
   *          the array of data values.
   * @param nullValue
   *          the null value for the grid.
   */
  public static void updateWell(final Well well, final String identifier, final String identifierType) {

    well.setIdentifier(identifier);
    well.setIdentifierType(identifierType);

    // Mark the grid as loaded and return it.
    well.markGhost();
    well.markLoaded();
  }

  /**
   * Validates that the data unit of an existing grid is ok for the
   * requested data unit.
   * 
   * @param grid the existing grid.
   * @param dataUnit the requested data unit.
   * @throws Exception thrown if the grid does not meet one or more of the requirements.
   */
  public static void validateWell(final Well well) throws Exception {
    // Nothing to do, I think
  }

  /**
   * Creates a well with an in-memory mapper. This allows for specifying the
   * grid geometry, data unit and data values.
   * 
   * @param name
   *          the name of the new grid.
   * @param geometry
   *          the geometry of the new grid.
   * @param dataUnit
   *          the unit of measurement for the data values.
   * @param dataValues
   *          the array of data values.
   * @param nullValue
   *          the null value for the grid.
   * @return the created grid.
   */
  public static Well createInMemory(final String name, final String identifier, final String identifierType) {
    try {
      // Create the new grid.
      Well well = new Well(name);
      updateWell(well, identifier, identifierType);

      well.markGhost();
      well.markLoaded();
      return well;
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }

  /**
   * Checks if an entry with the proposed name exists in the underlying
   * datastore of a specified grid.
   * 
   * @param grid
   *          the grid whose underlying datastore to check.
   * @param proposedName
   *          the name of the entry to search for.
   * @return <i>true</i> if an entry already exists; <i>false</i> if not.
   */
  public static boolean existsInStore(final Well well, final String proposedName) {
    return DataSource.existsInStore(well, proposedName);
  }

  public static void copyWellBore(final WellBore prototypeBore, final WellBore targetBore) {
    targetBore.setAzimuthNorthType(prototypeBore.getAzimuthNorthType());
    targetBore.setBoreAliases(prototypeBore.getBoreAliases(), prototypeBore.getBoreAliasTypes());
    targetBore.setBoreStatus(prototypeBore.getBoreStatus());
    targetBore.setBottomLocation(prototypeBore.getBottomLocation());
    targetBore.setCalcMethod(prototypeBore.getCalcMethod());
    targetBore.setCompletionDate(prototypeBore.getCompletionDate());
    targetBore.setDataSource(prototypeBore.getDataSource());
    targetBore.setDefaultCheckShot(prototypeBore.getDefaultCheckShot());
    targetBore.setElevation(prototypeBore.getElevation());
    targetBore.setElevationDatum(prototypeBore.getElevationDatum());
    targetBore.setFlowDirection(prototypeBore.getFlowDirection());
    targetBore.setFluidType(prototypeBore.getFluidType());
    targetBore.setFormationAtTD(prototypeBore.getFormationAtTD());
    //targetBore.setIdentifier(prototypeBore.getIdentifier());
    //targetBore.setIdentifierType(prototypeBore.getIdentifierType());
    targetBore.setPathAzimuth(prototypeBore.getPathAzimuth());
    targetBore.setPathDip(prototypeBore.getPathDip());

    if (prototypeBore.getOriginalWellDomain() == WellDomain.TRUE_VERTICAL_DEPTH) {
      targetBore.setDepthsAndTimes(prototypeBore.getMeasuredDepths(), prototypeBore.getTrueVerticalDepths(),
          new float[0]);
    } else {
      targetBore.setDepthsAndTimes(prototypeBore.getMeasuredDepths(), new float[0], prototypeBore.getTwoWayTimes());
    }

    targetBore.setXYOffsets(prototypeBore.getXOffsets(), prototypeBore.getYOffsets());
    targetBore.setPlugBackTotalDepth(prototypeBore.getPlugBackTotalDepth());
    targetBore.setShowType(prototypeBore.getShowType());
    targetBore.setSpudDate(prototypeBore.getSpudDate());
  }

  public static void copyWellBore(final WellBore prototypeBore, final WellBore targetBore, final float mdStart,
      final float mdEnd) {

    // Rip through the prototypeBore position log and find indices of first and last mds that satisfy the range
    float[] sourceMds = prototypeBore.getMeasuredDepths();

    int startIndex = -1;
    int n = 0;
    for (int i = 0; i < sourceMds.length; i++) {
      if (sourceMds[i] >= mdStart && startIndex == -1) {
        startIndex = i;
      }
      if (sourceMds[i] >= mdStart && sourceMds[i] <= mdEnd) {
        n++;
      }
    }

    // extract MDS, Tvds, Twts
    float[] targetMds = new float[n];
    System.arraycopy(sourceMds, startIndex, targetMds, 0, n);

    if (prototypeBore.getOriginalWellDomain() == WellDomain.TRUE_VERTICAL_DEPTH) {
      // copy & set tvd
      float[] targetTvds = new float[n];
      System.arraycopy(prototypeBore.getTrueVerticalDepths(), startIndex, targetTvds, 0, n);
      targetBore.setDepthsAndTimes(targetMds, targetTvds, new float[0]);
    } else {
      float[] targetTwts = new float[n];
      System.arraycopy(prototypeBore.getTwoWayTimes(), startIndex, targetTwts, 0, n);
      targetBore.setDepthsAndTimes(targetMds, new float[0], targetTwts);
    }

    // extract x,y Offsets
    double[] targetXOffsets = new double[n];
    double[] targetYOffsets = new double[n];
    System.arraycopy(prototypeBore.getXOffsets(), startIndex, targetXOffsets, 0, n);
    System.arraycopy(prototypeBore.getYOffsets(), startIndex, targetYOffsets, 0, n);
    targetBore.setXYOffsets(targetXOffsets, targetYOffsets);

    // set bottom location? no mappers actually set this
    //targetBore.setBottomLocation(targetBore.getLocationFromMeasuredDepth(targetMds[n - 1], WellDomain.MEASURED_DEPTH));

    FloatMeasurementSeries sourcePathAzimuth = prototypeBore.getPathAzimuth();
    if (sourcePathAzimuth != null) {
      // TODO - extract path azimuths     
    }
    FloatMeasurementSeries sourcePathDip = prototypeBore.getPathDip();
    if (sourcePathDip != null) {
      // TODO - extract path dip     
    }

    targetBore.setAzimuthNorthType(prototypeBore.getAzimuthNorthType());
    targetBore.setBoreAliases(prototypeBore.getBoreAliases(), prototypeBore.getBoreAliasTypes());
    targetBore.setBoreStatus(prototypeBore.getBoreStatus());

    //targetBore.setBottomLocation(prototypeBore.getBottomLocation());

    targetBore.setCalcMethod(prototypeBore.getCalcMethod());
    targetBore.setCompletionDate(prototypeBore.getCompletionDate());
    targetBore.setDataSource(prototypeBore.getDataSource());
    targetBore.setDefaultCheckShot(prototypeBore.getDefaultCheckShot());
    targetBore.setElevation(prototypeBore.getElevation());
    targetBore.setElevationDatum(prototypeBore.getElevationDatum());
    targetBore.setFlowDirection(prototypeBore.getFlowDirection());
    targetBore.setFluidType(prototypeBore.getFluidType());
    targetBore.setFormationAtTD(prototypeBore.getFormationAtTD());
    //targetBore.setIdentifier(prototypeBore.getIdentifier());
    //targetBore.setIdentifierType(prototypeBore.getIdentifierType());

    targetBore.setPlugBackTotalDepth(prototypeBore.getPlugBackTotalDepth());
    targetBore.setShowType(prototypeBore.getShowType());
    targetBore.setSpudDate(prototypeBore.getSpudDate());
  }
}

///////////////////////////////////////////////
//
//  // Force the name over the identifier since they HAVE to be unique
//  public Entity create(final Entity prototype, final String name, final String identifier, final String identifierType) {
//    Well prototypeWell = (Well) prototype;
//
//    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
//    IMapper mapper = factory.createMapper(prototype.getMapper(), name);
//    Well well = new Well(name, mapper);
//    well.setIdentifierAndType(identifier, identifierType);
//
//    copyPrototype(well, prototypeWell, prototypeWell.getProjectName());
//    return well;
//  }
//
//  // Force the name over the identifier since they HAVE to be unique
//  public Entity create(final Entity prototype, final String name) {
//    Well prototypeWell = (Well) prototype;
//
//    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
//    IMapper mapper = factory.createMapper(prototype.getMapper(), name);
//    Well well = new Well(name, mapper);
//    well.setIdentifierAndType(prototypeWell.getIdentifier(), prototypeWell.getIdentifierType());
//
//    copyPrototype(well, prototypeWell, prototypeWell.getProjectName());
//    return well;
//  }
//
//  public Entity create(final Entity prototype, final IMapper mapper, final String name) {
//    Well prototypeWell = (Well) prototype;
//    Well well = new Well(name, mapper);
//    well.setIdentifierAndType(prototypeWell.getIdentifier(), prototypeWell.getIdentifierType());
//
//    copyPrototype(well, prototypeWell, prototypeWell.getProjectName());
//    return well;
//  }
//
//  private static Well copyPrototype(final Well well, final Well prototype, final String projectName) {
//
//    // Save off the (potentially) new name
//    String wellName = well.getDisplayName();
//
//    // Copy over all the attributes
//    FactoryHelper.copyFromEntity(prototype, well, false);
//
//    // Reset a few
//    well.setProjectName(projectName);
//    well.setDisplayName(wellName);
//    well.setIdentifierAndType(well.getIdentifier(), well.getIdentifierType());
//
//    well.setDirty(false);
//    return well;
//  }
