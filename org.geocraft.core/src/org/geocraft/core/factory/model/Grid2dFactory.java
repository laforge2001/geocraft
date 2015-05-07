/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.factory.model;


import java.util.Map;

import org.geocraft.core.io.Grid2dInMemoryMapper;
import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.mapper.IGrid2dMapper;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;


/**
 * Contains the preferred factory methods for creating new 2D grid entities.
 */
public class Grid2dFactory {

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
  public static Grid2d create(final IRepository repository, final Grid2d prototype, final String name) {

    // Find the datastore accessor service. This will be used to replicate
    // the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IGrid2dMapper mapper = (IGrid2dMapper) factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the grids in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(Grid2d.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      Grid2d grid = (Grid2d) object;
      // Returns the existing grid if its unique ID matches.
      if (grid.getUniqueID().equals(mapper.getUniqueID())) {
        updateGrid(grid, prototype.getDataUnit(), prototype.getNullValue());
        return grid;
      }
    }

    // If no existing grid found, create and return a new grid backed by the new mapper.
    return create(repository, name, mapper, prototype.getGridGeometry(), prototype.getDataUnit(), prototype
        .getNullValue());
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
  public static Grid2d create(final IRepository repository, final Grid2d prototype, final String name,
      final Unit dataUnit) {

    // Find the datastore accessor service. This will be used to replicate
    // the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IGrid2dMapper mapper = (IGrid2dMapper) factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the grids in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(Grid2d.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      Grid2d grid = (Grid2d) object;
      // Returns the existing grid if its unique ID matches.
      if (grid.getUniqueID().equals(mapper.getUniqueID())) {
        updateGrid(grid, dataUnit, prototype.getNullValue());
        return grid;
      }
    }

    // If no existing grid found, create and return a new grid backed by the new mapper.
    return create(repository, name, mapper, prototype.getGridGeometry(), dataUnit, prototype.getNullValue());
  }

  /**
   * The common factory method for creating a new grid entity. This allows for
   * specifying the grid geometry, data unit and data values.
   * 
   * @param repository
   *          the repository in which to add the created grid.
   * @param name
   *          the name of the new grid.
   * @param geometry
   *          the geometry of the new grid.
   * @param mapper
   *          the mapper of the new grid.
   * @param dataUnit
   *          the unit of measurement for the data values.
   * @return the created grid.
   */
  private static Grid2d create(final IRepository repository, final String name, final IGrid2dMapper mapper,
      final GridGeometry2d gridGeometry, final Unit dataUnit, final float nullValue) {

    try {
      // Create the new grid.
      Grid2d grid = new Grid2d(name, mapper, gridGeometry);

      updateGrid(grid, dataUnit, nullValue);

      // Check if the grid exists in the datastore.
      if (!DataSource.existsInStore(grid)) {
        // If not, create it first.
        grid.getMapper().create(grid);
      }
      grid.update();

      // Add the entity to the repository.
      repository.add(grid);
      return grid;
    } catch (Exception ex) {
      ServiceProvider.getLoggingService().getLogger(Grid2dFactory.class).error(ex.toString(), ex);
    }
    return null;
  }

  private static void updateGrid(final Grid2d grid, final Unit dataUnit, final float nullValue) {
    // Set the geometry, data unit and values.
    grid.setDataUnit(dataUnit);
    grid.setNullValue(nullValue);
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
   * @return the created grid.
   */
  public static Grid2d createInMemory(final String name, final GridGeometry2d gridGeometry, final Unit dataUnit,
      final float nullValue) {
    try {
      // Create the new grid.
      Grid2d grid = new Grid2d(name, new Grid2dInMemoryMapper(), gridGeometry);

      updateGrid(grid, dataUnit, nullValue);
      return grid;
    } catch (Exception ex) {
      ServiceProvider.getLoggingService().getLogger(Grid2dFactory.class).error(ex.toString(), ex);
    }
    return null;
  }

}
