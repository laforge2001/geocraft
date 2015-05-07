/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.factory.model;


import java.util.Map;

import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;


/**
 * Factory methods for creating <code>PostStack2dCollection</code> entities.
 */
public class PostStack2dFactory {

  public static PostStack2d createInMemory(final String name, final SeismicSurvey2d survey, final Domain domain) {
    return create(name, new InMemoryMapper(PostStack2d.class), survey, domain);
  }

  /**
   * Creates a <code>PostStack2dCollection</code> entity backed by the specified
   * mapper. This factory method is used primarily by load/export tasks.
   * 
   * @param name
   *          the collection name.
   * @param mapper
   *          the collection mapper.
   * @param survey
   *          the 2D seismic survey.
   * @param domain
   *          the domain (TIME or DEPTH) of the 2D datasets in the collection.
   * @return the created <code>PostStack2dCollection</code> collection.
   */
  public static PostStack2d create(final String name, final IMapper mapper, final SeismicSurvey2d survey,
      final Domain domain) {
    // Create and return the new collection.
    return new PostStack2d(name, mapper, survey, domain);
  }

  /**
   * Returns a new or existing <code>PostStack2dCollection</code> entity based
   * on a prototype. The geometry and other attributes will be taken from the
   * prototype collection. If the entity already exists in the given repository,
   * then that instance will be returned instead. If a new instance if created,
   * it will automatically be added to the repository. The repository is first
   * searched for an existing collection matching the unique ID. If one is
   * found, then the collection is updated and returned. If not, the a new
   * collection is created and returned.
   * 
   * @param repository
   *          the repository in which to search for an existing collection.
   * @param prototype
   *          the prototype collection.
   * @param name
   *          the name of the new collection.
   * @return the <code>PostStack2dCollection</code> entity.
   */
  public static PostStack2d create(final IRepository repository, final PostStack2d prototype, final String name) throws Exception {
    // Make sure the prototype collection is loaded.
    prototype.load();

    // Find the datastore accessor service.
    // This will be used to replicate the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);

    // Get all the 2D poststack collections in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(PostStack2d.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      PostStack2d collection = (PostStack2d) object;
      // Returns the existing collection if its unique ID matches.
      if (collection.getUniqueID().equals(mapper.getUniqueID())) {
        return collection;
      }
    }

    // If no existing collection found, create and return a new collection.
    return create(repository, prototype, mapper, name);
  }

  /**
   * The common factory method for creating a new
   * <code>PostStack2dCollection</code> entity.
   * 
   * @param repository
   *          the repository in which to add the created collection.
   * @param prototype
   *          the prototype collection from which to copy the properties.
   * @param mapper
   *          the collection mapper.
   * @param name
   *          the collection name.
   * @param domain
   *          the collection domain (time or depth).
   * @param startZ
   *          the starting z value.
   * @param endZ
   *          the ending z value.
   * @param deltaZ
   *          the delta z value (sample rate).
   * @return the <code>PostStack2dCollection</code> entity.
   */
  private static PostStack2d create(final IRepository repository, final PostStack2d prototype, final IMapper mapper,
      final String name) throws Exception {
    try {
      // Create a new collection.
      SeismicSurvey2d survey = prototype.getSurvey();
      PostStack2d collection = new PostStack2d(name, mapper, survey, prototype.getZDomain());

      // Add the collection to the repository.
      repository.add(collection);
      return collection;
    } catch (Exception ex) {
      throw new Exception(ex.getMessage());
    }
  }
}
