/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.factory.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IGrid3dMapper;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;


/**
 * Factory methods for creating <code>Grid3d</code> entities.
 */
public class Grid3dFactory {

  public static Grid3d create(final IRepository repository, final PostStack3d prototype, final String name,
      final Unit dataUnit, final float[][] dataValues, final float nullValue, final OnsetType onsetType) {
    String datastoreName = prototype.getDatastore();
    SeismicSurvey3d survey = prototype.getSurvey();
    Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    if (datastoreName == null || datastoreName.length() == 0) {
      Grid3d grid = createInMemory(name, survey, dataUnit, dataValues, nullValue);
      repository.add(grid);
      return grid;
    }

    IDatastoreAccessorService service = ServiceProvider.getDatastoreAccessorService();
    IDatastoreAccessor[] accessors = service.getDatastoreAccessors(new Class[] { Grid3d.class }, IOMode.OUTPUT);
    for (IDatastoreAccessor accessor : accessors) {
      if (accessor.getName().startsWith(datastoreName)) {

        // Create a new mapper model based on the prototype volume.
        MapperModel model;
        try {
          model = accessor.mapEntitiesToModels(new Entity[] { prototype }).values().iterator().next();
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("Could not create " + datastoreName + " " + Grid3d.class + " mapper");
        }
        if (model == null) {
          throw new RuntimeException("Could not create " + datastoreName + " " + Grid3d.class + " mapper");
        }
        model.updateUniqueId(name);

        // Create a new mapper based on the mapper model.
        IGrid3dMapper mapper = (IGrid3dMapper) accessor.createMapper(IOMode.OUTPUT, model);
        if (mapper == null) {
          throw new RuntimeException("Could not create " + datastoreName + " " + Grid3d.class + " mapper");
        }

        // Update the necessary properties on the mapper.
        mapper.setFileName(name);
        mapper.setStorageDirectory(prototype.getStorageDirectory());
        mapper.setDataUnit(dataUnit);
        mapper.setOnsetType(onsetType);
        mapper.setXYUnit(xyUnit);

        // Get all the grids in the repository.
        Map<String, Object> map = repository.get(new TypeSpecification(Grid3d.class));

        try {

          // Loop thru each one comparing its unique ID to the one to be created.
          for (Object object : map.values()) {
            Grid3d grid = (Grid3d) object;
            // Returns the existing grid if its unique ID matches.
            if (grid.getUniqueID().equals(mapper.getUniqueID())) {
              validateGrid3d(grid, dataUnit);
              updateGrid(grid, survey, dataUnit, dataValues, nullValue);
              return grid;
            }
          }

          return create(repository, name, mapper, survey, dataUnit, nullValue, dataValues);

        } catch (Exception ex) {
          ex.printStackTrace();
          throw new RuntimeException(ex);
        }
      }
    }
    Grid3d grid = createInMemory(name, survey, dataUnit, dataValues, nullValue);
    repository.add(grid);
    return grid;

    //throw new RuntimeException("Grid3d datastore not found: " + datastoreName);
  }

  /**
   * Returns a new or existing grid based on a prototype grid. The geometry,
   * data values, null value and unit of measurement will be taken from the
   * prototype grid. If the entity already exists in the given repository, then
   * that instance will be returned instead. If a new instance if created, it
   * will automatically be added to the repository. The repository is first
   * searched for an existing grid matching the unique ID. If one is found, then
   * the grid is updated and returned. If not, the a new grid is created and
   * returned.
   * 
   * @param repository
   *          the repository in which to search for an existing grid.
   * @param prototype
   *          the prototype grid.
   * @param name
   *          the name of the new grid.
   * @return the grid.
   */
  public static Grid3d create(final IRepository repository, final Grid3d prototype, final String name) {
    // Find the datastore accessor service. This will be used to replicate
    // the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IGrid3dMapper mapper = (IGrid3dMapper) factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the grids in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(Grid3d.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      Grid3d grid = (Grid3d) object;
      // Returns the existing grid if its unique ID matches.
      if (grid.getUniqueID().equals(mapper.getUniqueID())) {
        updateGrid(grid, prototype.getGeometry(), prototype.getDataUnit(), prototype.getValues(),
            prototype.getNullValue());
        return grid;
      }
    }

    try {
      // If no existing grid found, create and return a new grid backed by the new mapper.
      return create(repository, name, mapper, prototype.getGeometry(), prototype.getDataUnit(),
          prototype.getNullValue(), prototype.getValues());
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Returns a new or existing grid, based on a prototype grid. This method
   * allows the user to specify the geometry and data values and unit of
   * measurement for the new grid. The null value will be taken from the
   * prototype grid. The repository is first searched for an existing grid
   * matching the unique ID. If one is found, then the grid is updated and
   * returned. If not, the a new grid is created and returned.
   * 
   * @param repository
   *          the repository in which to search for an existing grid.
   * @param prototype
   *          the prototype grid.
   * @param dataValues
   *          the grid data values for the new grid.
   * @param name
   *          the name of the new grid.
   * @param geometry
   *          the geometry of the new grid.
   * @param dataUnit
   *          the unit of measurement for the data values.
   * @return the grid.
   */
  public static Grid3d create(final IRepository repository, final Grid3d prototype, final float[][] dataValues,
      final String name, final GridGeometry3d geometry, final Unit dataUnit) {
    // Find the datastore accessor service. This will be used to replicate
    // the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IGrid3dMapper mapper = (IGrid3dMapper) factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the grids in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(Grid3d.class));

    try {
      // Loop thru each one comparing its unique ID to the one to be created.
      for (Object object : map.values()) {
        Grid3d grid = (Grid3d) object;
        // Returns the existing grid if its unique ID matches.
        if (grid.getUniqueID().equals(mapper.getUniqueID())) {
          validateGrid3d(grid, dataUnit);
          updateGrid(grid, geometry, dataUnit, dataValues, prototype.getNullValue());
          return grid;
        }
      }

      // If no existing grid found, create and return a new grid backed by the new mapper.
      return create(repository, name, mapper, geometry, dataUnit, prototype.getNullValue(), dataValues);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Returns a new or existing grid, based on a prototype grid. This method
   * allows the user to specify the data values for the new grid. The geometry,
   * unit of measurement and null value will be taken from the prototype grid.
   * The repository is first searched for an existing grid matching the unique
   * ID. If one is found, then the grid is updated and returned. If not, the a
   * new grid is created and returned.
   * 
   * @param repository
   *          the repository in which to search for an existing grid.
   * @param prototype
   *          the prototype grid.
   * @param dataValues
   *          the grid data values for the new grid.
   * @param name
   *          the name of the new grid.
   * @return the grid.
   */
  public static Grid3d create(final IRepository repository, final Grid3d prototype, final float[][] dataValues,
      final String name) {
    return create(repository, prototype, dataValues, name, prototype.getGeometry(), prototype.getDataUnit());
  }

  /**
   * Returns a new or existing grid, based on a prototype grid. This method
   * allows the user to specify the geometry and data values for the new grid.
   * The unit of measurement and null value will be taken from the prototype
   * grid. The repository is first searched for an existing grid matching the
   * unique ID. If one is found, then the grid is updated and returned. If not,
   * the a new grid is created and returned.
   * 
   * @param repository
   *          the repository in which to search for an existing grid.
   * @param prototype
   *          the prototype grid.
   * @param dataValues
   *          the grid data values for the new grid.
   * @param name
   *          the name of the new grid.
   * @param geometry
   *          the geometry of the new grid.
   * @return the grid.
   */
  public static Grid3d create(final IRepository repository, final Grid3d prototype, final float[][] dataValues,
      final String name, final GridGeometry3d geometry) {
    return create(repository, prototype, dataValues, name, geometry, prototype.getDataUnit());
  }

  /**
   * The common factory method for creating a new grid entity. This allows for
   * specifying the grid geometry, data unit and data values.
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
  private static Grid3d create(final IRepository repository, final String name, final IGrid3dMapper mapper,
      final GridGeometry3d geometry, final Unit dataUnit, final float nullValue, final float[][] dataValues) throws Exception {

    // Create the new grid.
    Grid3d grid = new Grid3d(name, mapper);

    updateGrid(grid, geometry, dataUnit, dataValues, nullValue);

    // Check if the grid exists in the datastore.
    if (!DataSource.existsInStore(grid)) {
      // If not, create it first.
      mapper.setDataUnit(dataUnit);
      grid.getMapper().create(grid);
    } else {
      grid.update();
    }
    grid.markGhost();
    grid.load();

    validateGrid3d(grid, dataUnit);

    // Add the grid to the repository.
    repository.add(grid);
    return grid;
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
  private static void updateGrid(final Grid3d grid, final GridGeometry3d geometry, final Unit dataUnit,
      final float[][] dataValues, final float nullValue) {
    // Set the geometry, data unit and values.
    if (DataSource.existsInStore(grid)) {
      GridGeometry3d existingGeometry = grid.getGeometry();
      if (existingGeometry != null) {
        // Check that the geometry matches.
        if (!existingGeometry.matchesGeometry(geometry)) {
          throw new IllegalArgumentException("Cannot change the existing grid geometry.");
        }
      }
    }
    grid.setGeometry(geometry);
    grid.setValues(dataValues, nullValue, dataUnit);
    Domain domain = dataUnit.getDomain();
    if (domain.equals(Domain.TIME) || domain.equals(Domain.DISTANCE)) {
      grid.setZDomain(domain);
    } else {
      grid.setZDomain(Domain.TIME);
    }

    // Mark the grid as loaded and return it.
    grid.markGhost();
    grid.markLoaded();
  }

  /**
   * Validates that the data unit of an existing grid is ok for the
   * requested data unit.
   * 
   * @param grid the existing grid.
   * @param dataUnit the requested data unit.
   * @throws Exception thrown if the grid does not meet one or more of the requirements.
   */
  private static void validateGrid3d(final Grid3d grid, final Unit dataUnit) throws Exception {
    List<String> errors = new ArrayList<String>();
    String gridName = grid.getDisplayName();
    if (!grid.getDataUnit().equals(dataUnit)) {
      errors.add("The requested data unit (" + dataUnit + ") does not match the data unit (" + grid.getDataUnit()
          + ") of existing grid (" + gridName + ").");
    }
    if (errors.size() > 0) {
      StringBuilder builder = new StringBuilder();
      for (String error : errors) {
        builder.append(error + "\n");
      }
      throw new RuntimeException(builder.toString());
    }
  }

  /**
   * Creates a grid with an in-memory mapper. This allows for specifying the
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
  public static Grid3d createInMemory(final String name, final GridGeometry3d geometry, final Unit dataUnit,
      final float[][] dataValues, final float nullValue) {
    try {
      // Create the new grid.
      Grid3d grid = new Grid3d(name, geometry);

      updateGrid(grid, geometry, dataUnit, dataValues, nullValue);

      grid.markGhost();
      grid.markLoaded();
      return grid;
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
  public static boolean existsInStore(final Grid3d grid, final String proposedName) {
    return DataSource.existsInStore(grid, proposedName);
  }
}
