/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.factory.model;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IPostStack2dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;


/**
 * Factory methods for creating PostStack2d entities.
 */
public class PostStack2dLineFactory {

  /**
   * Creates a PostStack2d volume backed by the specified mapper. This factory
   * method is used primarily by load/export tasks.
   */
  public static PostStack2dLine create(final String name, final IPostStack2dMapper mapper,
      final SeismicSurvey2d survey, final String lineName, final int lineNumber, final PostStack2d poststack) {
    // Create and return the new volume.
    return new PostStack2dLine(name, mapper, survey, lineName, lineNumber, poststack);
  }

  /**
   * Returns a new or existing volume based on a prototype volume. The geometry
   * and other attributes will be taken from the prototype volume. If the entity
   * already exists in the given repository, then that instance will be returned
   * instead. If a new instance if created, it will automatically be added to
   * the repository. The repository is first searched for an existing volume
   * matching the unique ID. If one is found, then the volume is updated and
   * returned. If not, the a new volume is created and returned.
   * 
   * @param repository
   *          the repository in which to search for an existing volume.
   * @param prototype
   *          the prototype volume.
   * @param name
   *          the name of the new volume.
   * @return the volume.
   */
  public static PostStack2dLine create(final IRepository repository, final PostStack2dLine prototype, final String name) throws Exception {
    // Make sure the prototype volume is loaded.
    prototype.load();

    // Find the datastore accessor service.
    // This will be used to replicate the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IPostStack2dMapper mapper = (IPostStack2dMapper) factory.createMapper(prototype.getMapper(), name);

    // Get all the 2D poststack volumes in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(PostStack2dLine.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      PostStack2dLine volume = (PostStack2dLine) object;
      // Returns the existing volume if its unique ID matches.
      if (volume.getUniqueID().equals(mapper.getUniqueID())) {
        validatePostStack2dLine(volume, prototype.getZDomain(), prototype.getZStart(), prototype.getZEnd(), prototype
            .getZDelta());
        return volume;
      }
    }

    // If no existing volume found, create and return a new volume.
    return create(repository, prototype, mapper, name);
  }

  /**
   * Validates that the z range and domain of an existing volume is ok for the
   * requested z range.
   * 
   * @param volume
   *          the existing volume.
   * @param zDomain
   *          the requested z domain.
   * @param zStart
   *          the requested z start.
   * @param zEnd
   *          the requested z end.
   * @param zDelta
   *          the requested z delta.
   * @throws Exception
   *           thrown if the volume does not meet one or more of the
   *           requirements.
   */
  private static void validatePostStack2dLine(final PostStack2dLine volume, final Domain zDomain, final float zStart,
      final float zEnd, final float zDelta) throws Exception {
    List<String> errors = new ArrayList<String>();
    String volumeName = volume.getDisplayName();
    if (!volume.getZDomain().equals(zDomain)) {
      errors.add("The requested z-domain (" + zDomain + ") does not match the z-domain (" + volume.getZDomain()
          + ") of existing volume (" + volumeName + ").");
    }
    if (Float.compare(volume.getZDelta(), zDelta) != 0) {
      errors.add("The requested delta-z (" + zDelta + ") does not match the delta-z (" + volume.getZDelta()
          + ") of existing volume (" + volumeName + ").");
    }
    if (zStart < volume.getZStart()) {
      errors.add("The requested start-z (" + zStart + ") is less than the start-z (" + volume.getZStart()
          + ") of the existing volume (" + volumeName + ").");
    }
    if (zEnd > volume.getZEnd()) {
      errors.add("The requested end-z (" + zEnd + ") is greater than the end-z (" + volume.getZEnd()
          + ") of the existing volume (" + volumeName + ").");
    }
    if (errors.size() > 0) {
      StringBuilder builder = new StringBuilder();
      for (String error : errors) {
        builder.append(error + "\n");
      }
      throw new Exception(builder.toString());
    }
  }

  /**
   * Returns a new or existing volume, based on a prototype volume. This method
   * allows the user to specify the domain and z range of the new volume. The
   * repository is first searched for an existing volume matching the unique ID.
   * If one is found, then the volume is updated and returned. If not, the a new
   * volume is created and returned.
   * 
   * @param repository
   *          the repository in which to search for an existing volume.
   * @param prototype
   *          the prototype volume.
   * @param name
   *          the name of the new volume.
   * @param domain
   *          the domain of the new volume.
   * @param startZ
   *          the starting z of the new volume.
   * @param endZ
   *          the ending z of the new volume.
   * @param deltaZ
   *          the delta z of the new volume.
   * @return the volume.
   */
  public static PostStack2dLine create(final IRepository repository, final PostStack2dLine prototype,
      final String name, final Domain domain, final float startZ, final float endZ, final float deltaZ) throws Exception {
    // Make sure the prototype volume is loaded.
    prototype.load();

    // Find the datastore accessor service.
    // This will be used to replicate the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IMapper prototypeMapper = prototype.getMapper();
    MapperModel model = prototypeMapper.getModel();
    IPostStack2dMapper mapper = (IPostStack2dMapper) factory.createMapper(prototypeMapper, model, name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the volumes in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(PostStack2dLine.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      PostStack2dLine volume = (PostStack2dLine) object;
      // Returns the existing volume if its unique ID matches.
      if (volume.getUniqueID().equals(mapper.getUniqueID())) {
        validatePostStack2dLine(volume, domain, startZ, endZ, deltaZ);
        return volume;
      }
    }

    // If no existing volume found, create and return a new volume backed by the new mapper.
    return create(repository, prototype, mapper, name, domain, startZ, endZ, deltaZ);
  }

  /**
   * Creates a PostStack2d with the specified name, storage format and sample
   * rate. All other properties are copied from a prototype volume.
   * 
   * @param repository
   *          the repository in which to add the created volume.
   * @param prototype
   *          the prototype volume from which to copy the mapper.
   * @param name
   *          the volume name.
   * @param storageFormat
   *          the volume storage format.
   * @param deltaZ
   *          the delta z value (sample rate).
   * @return the created PostStack2d.
   */
  public static PostStack2dLine create(final IRepository repository, final PostStack2dLine prototype,
      final String name, final StorageFormat storageFormat, final float deltaZ) throws Exception {
    // Make sure the prototype volume is loaded.
    prototype.load();

    // Find the datastore accessor service.
    // This will be used to replicate the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IPostStack2dMapper mapper = (IPostStack2dMapper) factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the volumes in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(PostStack2dLine.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      PostStack2dLine volume = (PostStack2dLine) object;
      // Returns the existing volume if its unique ID matches.
      if (volume.getUniqueID().equals(mapper.getUniqueID())) {
        validatePostStack2dLine(volume, prototype.getZDomain(), prototype.getZStart(), prototype.getZEnd(), deltaZ);
        return volume;
      }
    }

    // Set the desired storage format.
    mapper.setStorageFormat(storageFormat);

    // Create and return the new volume.
    return create(repository, prototype, mapper, name, deltaZ);
  }

  /**
   * Creates a PostStack2d with the specified name and mapper. The remainder of
   * the properties will be taken from the prototype volume.
   * 
   * @param repository
   *          the repository in which to add the created volume.
   * @param prototype
   *          the prototype volume from which to copy the properties.
   * @param mapper
   *          the volume mapper.
   * @param name
   *          the volume name.
   * @return the created PostStack2d.
   */
  private static PostStack2dLine create(final IRepository repository, final PostStack2dLine prototype,
      final IPostStack2dMapper mapper, final String name) throws Exception {
    return create(repository, prototype, mapper, name, prototype.getZDomain(), prototype.getZStart(), prototype
        .getZEnd(), prototype.getZDelta());
  }

  /**
   * Creates a PostStack2d with the specified name and mapper. The remainder of
   * the properties will be taken from the prototype volume.
   * 
   * @param repository
   *          the repository in which to add the created volume.
   * @param prototype
   *          the prototype volume from which to copy the properties.
   * @param mapper
   *          the volume mapper.
   * @param name
   *          the volume name.
   * @param deltaZ
   *          the delta z value (sample rate).
   * @return the created PostStack2d volume.
   */
  private static PostStack2dLine create(final IRepository repository, final PostStack2dLine prototype,
      final IPostStack2dMapper mapper, final String name, final float deltaZ) throws Exception {
    return create(repository, prototype, mapper, name, prototype.getZDomain(), prototype.getZStart(), prototype
        .getZEnd(), deltaZ);
  }

  /**
   * The common factory method for creating a new 2D poststack entity.
   * 
   * @param repository
   *          the repository in which to add the created volume.
   * @param prototype
   *          the prototype volume from which to copy the properties.
   * @param mapper
   *          the volume mapper.
   * @param name
   *          the volume name.
   * @param domain
   *          the volume domain (time or depth).
   * @param startZ
   *          the starting z value.
   * @param endZ
   *          the ending z value.
   * @param deltaZ
   *          the delta z value (sample rate).
   * @return the created PostStack2d.
   */
  private static PostStack2dLine create(final IRepository repository, final PostStack2dLine prototype,
      final IPostStack2dMapper mapper, final String name, final Domain domain, final float startZ, final float endZ,
      final float deltaZ) throws Exception {
    PostStack2dLine ps2dLine = null;
    try {
      // Create a new volume.
      SeismicSurvey2d survey = prototype.getSurvey();
      PostStack2d poststack = PostStack2dFactory.create(repository, prototype.getPostStack(), name);
      ps2dLine = new PostStack2dLine(prototype.getLineName() + "_" + name, mapper, survey, prototype.getLineName(),
          prototype.getLineNumber(), poststack);

      if (!mapper.existsInStore()) {
        ps2dLine.setZDomain(domain);
        ps2dLine.setCdpRange(prototype.getCdpStart(), prototype.getCdpEnd(), prototype.getCdpDelta());
        ps2dLine.setZRangeAndDelta(startZ, endZ, deltaZ);

        // Copy important attributes over that aren't specified in constructor
        ps2dLine.setComment(prototype.getComment());
        // ps2dLine.setNumSamplesPerTrace(prototype.getNumSamplesPerTrace());
        //ps2dLine.setSampleMax(prototype.getSampleMax());
        //ps2dLine.setSampleMin(prototype.getSampleMin());
        ps2dLine.setDataUnit(prototype.getDataUnit());
        ps2dLine.setElevationDatum(prototype.getElevationDatum());
        ps2dLine.setElevationReferences(prototype.getElevationReference());
        ps2dLine.setTraceHeaderDefinition(prototype.getTraceHeaderDefinition());
        ps2dLine.markGhost();
        ps2dLine.markLoaded();
        mapper.create(ps2dLine);
      } else {
        ps2dLine.load();
      }

      validatePostStack2dLine(ps2dLine, domain, startZ, endZ, deltaZ);

      poststack.addPostStack2dLine(ps2dLine.getLineNumber(), ps2dLine);

      // Add the volume to the repository.
      repository.add(ps2dLine);
      return ps2dLine;
    } catch (IOException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new Exception(ex.getMessage());
    }
  }

  /**
   * Checks if an entry with the proposed name exists in the underlying
   * datastore of a specified 2D poststack volume.
   * 
   * @param poststack
   *          the volume whose underlying datastore to check.
   * @param proposedName
   *          the name of the entry to search for.
   * @return <i>true</i> if an entry already exists; <i>false</i> if not.
   */
  public static boolean existsInStore(final PostStack2dLine poststack, final String proposedName) {
    return DataSource.existsInStore(poststack, proposedName);
  }
}
