/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.factory.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.mapper.IPostStack3dMapper.BrickType;
import org.geocraft.core.model.mapper.IPostStack3dMapper.StorageOrganization;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;


/**
 * Factory methods for creating <code>PostStack3d</code> entities.
 */
public class PostStack3dFactory {

  /**
   * Creates a PostStack3d volume backed by the specified mapper. This factory
   * method is used primarily by load/export tasks.
   * 
   * @param name the volume name.
   * @param mapper the volume mapper.
   * @return the created PostStack3d volume.
   */
  public static PostStack3d create(final String name, final IPostStack3dMapper mapper) {
    // Create and return the new volume.
    return new PostStack3d(name, mapper);
  }

  /**
  * Creates a PostStack3d volume backed by the specified mapper. This factory
  * method is used primarily by load/export tasks.
  * 
  * @param name the volume name.
  * @param survey the seismic survey on which the volume is defined.
  * @param mapper the volume mapper.
  * @return the created PostStack3d volume.
  */
  public static PostStack3d create(final String name, final SeismicSurvey3d survey, final IPostStack3dMapper mapper) {
    // Create and return the new volume.
    return new PostStack3d(name, survey, mapper);
  }

  /**
   * Returns a new or existing volume based on a prototype volume. The geometry
   * and other attributes will be taken from the prototype volume. If the entity
   * already exists in the given repository, then that instance will be returned
   * instead. If a new instance if created, it will automatically be added to
   * the repository. The repository is first searched for an existing volume
   * matching the unique ID. If one is found, then the volume is updated and
   * returned. If not, the a new volume is created and returned. If an existing
   * volume is found, a validation check if made to ensure that its z range is
   * compatible with the z range of the requested volume.
   * 
   * @param repository
   *          the repository in which to search for an existing volume.
   * @param prototype
   *          the prototype volume.
   * @param name
   *          the name of the new volume.
   * @return the volume.
   * @throws Exception
   *           thrown on the volume could not be created or is incompatible with
   *           z range of the prototype.
   */
  public static PostStack3d create(final IRepository repository, final PostStack3d prototype, final String name) throws Exception {
    return create(repository, prototype, name, prototype.getZDomain(), prototype.getZStart(), prototype.getZEnd(),
        prototype.getZDelta());
  }

  /**
   * Returns a new or existing volume, based on a prototype volume. This method
   * allows the user to specify the domain and z range of the new volume. The
   * repository is first searched for an existing volume matching the unique ID.
   * If one is found, then the volume is updated and returned. If not, the a new
   * volume is created and returned. If an existing volume is found, a
   * validation check if made to ensure that its z range is compatible with the
   * requested z range.
   * 
   * @param repository
   *          the repository in which to search for an existing volume.
   * @param prototype
   *          the prototype volume.
   * @param name
   *          the name of the new volume.
   * @param domain
   *          the domain of the new volume.
   * @param zStart
   *          the starting z of the new volume.
   * @param zEnd
   *          the ending z of the new volume.
   * @param zDelta
   *          the delta z of the new volume.
   * @return the volume.
   * @throws Exception
   *           thrown on the volume could not be created or is incompatible with
   *           the requested z range.
   */
  public static PostStack3d create(final IRepository repository, final PostStack3d prototype, final String name,
      final Domain domain, final float zStart, final float zEnd, final float zDelta) throws Exception {
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
    IPostStack3dMapper mapper = (IPostStack3dMapper) factory.createMapper(prototypeMapper, model, name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the volumes in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(PostStack3d.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      PostStack3d volume = (PostStack3d) object;
      // Returns the existing volume if its unique ID matches.
      if (volume.getUniqueID().equals(mapper.getUniqueID())) {
        // Validate the requested z range of the existing volume.
        validatePostStack3d(volume, domain, zStart, zEnd, zDelta);
        return volume;
      }
    }

    // If no existing volume found, create and return a new volume backed by the new mapper.
    return create(repository, prototype, mapper, name, domain, zStart, zEnd, zDelta);
  }

  /**
   * Returns a new or existing volume, based on a prototype volume. This method
   * allows the user to specify the storage organization, storage format and z
   * range of the new volume. The repository is first searched for an existing
   * volume matching the unique ID. If one is found, then the volume is updated
   * and returned. If not, the a new volume is created and returned. If an
   * existing volume is found, a validation check if made to ensure that its z
   * range is compatible with the requested z range.
   * 
   * @param repository
   *          the repository in which to search for an existing volume.
   * @param prototype
   *          the prototype volume.
   * @param name
   *          the name of the new volume.
   * @param storageOrganizationCode
   *          the storage organization code (e.g. "ibri", "xbri", "cmp", etc) of
   *          the new volume.
   * @param storageFormatCode
   *          the storage format (8-bit integer, 16-bit integer, etc) of the new
   *          volume.
   * @param fidelity
   *          the fidelity for compressed volumes (0-100).
   * @param zStart
   *          the starting z of the new volume.
   * @param zEnd
   *          the ending z of the new volume.
   * @param zDelta
   *          the delta z of the new volume.
   * @return the volume.
   * @throws Exception
   *           thrown on the volume could not be created or is incompatible with
   *           the requested z range.
   */
  public static PostStack3d create(final IRepository repository, final PostStack3d prototype, final String name,
      final String storageOrganizationCode, final String storageFormatCode, final float fidelity, final float zStart,
      final float zEnd, final float zDelta) throws Exception {

    // Lookup the storage organization from the code, or take it from the prototype if the code is empty.
    StorageOrganization storageOrganization = prototype.getStorageOrganization();
    if (!storageOrganizationCode.isEmpty()) {
      storageOrganization = StorageOrganization.lookupByCode(storageOrganizationCode);
    }

    // Lookup the storage format from the code, or take it from the prototype if the code is empty.
    StorageFormat storageFormat = prototype.getStorageFormat();
    if (!storageFormatCode.isEmpty()) {
      storageFormat = StorageFormat.lookupByCode(storageFormatCode);
    }
    BrickType brickType = BrickType.lookupByCode(storageOrganizationCode);

    // Make sure the prototype volume is loaded.
    prototype.load();
    Domain zDomain = prototype.getZDomain();

    // Find the datastore accessor service.
    // This will be used to replicate the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IMapper prototypeMapper = prototype.getMapper();
    MapperModel model = prototypeMapper.getModel();

    IPostStack3dMapper mapper = (IPostStack3dMapper) factory.createMapper(prototypeMapper, model, name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Set the desired storage organization (TRACES, BRICKS, etc) and format (INT08, INT16, etc).
    mapper.setStorageOrganizationAndFormat(storageOrganization, storageFormat, brickType, fidelity);

    // Get all the volumes in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(PostStack3d.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      PostStack3d volume = (PostStack3d) object;
      // Returns the existing volume if its unique ID matches.
      if (volume.getUniqueID().equals(mapper.getUniqueID())) {
        // Validate the requested z range of the existing volume.
        validatePostStack3d(volume, zDomain, zStart, zEnd, zDelta);
        return volume;
      }
    }

    // If no existing volume found, create and return a new volume backed by the new mapper.
    return create(repository, prototype, mapper, name, zDomain, zStart, zEnd, zDelta);
  }

  /**
   * Creates a PostStack3d with the specified name, storage format and sample
   * rate. All other properties are copied from a prototype volume.If an
   * existing volume is found, a validation check if made to ensure that its z
   * range is compatible with the requested z range.
   * 
   * @param repository
   *          the repository in which to add the created volume.
   * @param prototype
   *          the prototype volume from which to copy the mapper.
   * @param name
   *          the volume name.
   * @param storageFormat
   *          the volume storage format.
   * @param zDelta
   *          the delta z value (sample rate).
   * @return the created PostStack3d.
   * @throws Exception
   *           thrown on the volume could not be created or is incompatible with
   *           the requested z range.
   */
  public static PostStack3d create(final IRepository repository, final PostStack3d prototype, final String name,
      final StorageFormat storageFormat, final float zDelta) throws Exception {
    // Make sure the prototype volume is loaded.
    prototype.load();

    // Find the datastore accessor service.
    // This will be used to replicate the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IPostStack3dMapper mapper = (IPostStack3dMapper) factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the volumes in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(PostStack3d.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      PostStack3d volume = (PostStack3d) object;
      // Returns the existing volume if its unique ID matches.
      if (volume.getUniqueID().equals(mapper.getUniqueID())) {
        // Validate the requested z range of the existing volume.
        validatePostStack3d(volume, volume.getZDomain(), prototype.getZMaxStart(), prototype.getZMaxEnd(), zDelta);
        return volume;
      }
    }

    // Set the desired storage format.
    if (!storageFormat.equals(mapper.getStorageFormat())) {
      mapper.setStorageFormat(storageFormat);
    }

    // Create and return the new volume.
    return create(repository, prototype, mapper, name, prototype.getZDomain(), prototype.getZStart(),
        prototype.getZEnd(), zDelta);
  }

  /**
   * Creates a PostStack3d with the specified name, storage format and sample
   * rate. All other properties are copied from a prototype volume.If an
   * existing volume is found, a validation check if made to ensure that its z
   * range is compatible with the requested z range.
   * 
   * @param repository
   *          the repository in which to add the created volume.
   * @param prototype
   *          the prototype volume from which to copy the mapper.
   * @param name
   *          the volume name.
   * @param storageFormat
   *          the volume storage format.
   * @param zDelta
   *          the delta z value (sample rate).
   * @return the created PostStack3d.
   * @throws Exception
   *           thrown on the volume could not be created or is incompatible with
   *           the requested z range.
   */
  public static PostStack3d create(final IRepository repository, final PostStack3d prototype, final String name,
      final StorageFormat storageFormat, final StorageOrganization storageOrganization, final float zDelta) throws Exception {
    // Make sure the prototype volume is loaded.
    prototype.load();

    // Find the datastore accessor service.
    // This will be used to replicate the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IPostStack3dMapper mapper = (IPostStack3dMapper) factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the volumes in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(PostStack3d.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      PostStack3d volume = (PostStack3d) object;
      // Returns the existing volume if its unique ID matches.
      if (volume.getUniqueID().equals(mapper.getUniqueID())) {
        // Validate the requested z range of the existing volume.
        validatePostStack3d(volume, volume.getZDomain(), prototype.getZMaxStart(), prototype.getZMaxEnd(), zDelta);
        return volume;
      }
    }

    // Set the desired storage format.
    if (!storageFormat.equals(mapper.getStorageFormat())) {
      mapper.setStorageFormat(storageFormat);
    }

    // Set the desired storage organization.
    if (!storageOrganization.equals(mapper.getStorageOrganization())) {
      mapper.setStorageOrganizationAndFormat(storageOrganization, storageFormat, BrickType.VERTICAL, 0.9f);
    }

    // Create and return the new volume.
    return create(repository, prototype, mapper, name, prototype.getZDomain(), prototype.getZStart(),
        prototype.getZEnd(), zDelta);
  }

  /**
   * Validates that the z range and domain of an existing volume is ok for the
   * requested z range.
   * 
   * @param volume the existing volume.
   * @param zDomain the requested z domain.
   * @param zStart the requested z start.
   * @param zEnd the requested z end.
   * @param zDelta the requested z delta.
   * @throws Exception  thrown if the volume does not meet one or more of the requirements.
   */
  private static void validatePostStack3d(final PostStack3d volume, final Domain zDomain, final float zStart,
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
    if (zStart < volume.getZMaxStart()) {
      errors.add("The requested start-z (" + zStart + ") is less than the start-z (" + volume.getZStart()
          + ") of the existing volume (" + volumeName + ").");
    }
    if (zEnd > volume.getZMaxEnd()) {
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
   * The common factory method for creating a new 3D poststack entity.
   * 
   * @param repository
   *          the repository in which to add the created volume.
   * @param prototype
   *          the prototype volume from which to copy the properties.
   * @param mapper
   *          the volume mapper.
   * @param name
   *          the volume name.
   * @param zDomain
   *          the volume z domain (time or depth).
   * @param zStart
   *          the starting z value.
   * @param zEnd
   *          the ending z value.
   * @param zDelta
   *          the delta z value (sample rate).
   * @return the created PostStack3d.
   */
  private static PostStack3d create(final IRepository repository, final PostStack3d prototype,
      final IPostStack3dMapper mapper, final String name, final Domain zDomain, final float zStart, final float zEnd,
      final float zDelta) throws Exception {

    // Create a new volume.
    PostStack3d ps3d = new PostStack3d(name, mapper);

    if (!mapper.existsInStore()) {
      // If it does not exist in the datastore, then create it.
      mapper.setDomain(zDomain);
      ps3d.setZDomain(zDomain);
      ps3d.setSurvey(prototype.getSurvey());
      ps3d.setInlineRangeAndDelta(prototype.getInlineStart(), prototype.getInlineEnd(), prototype.getInlineDelta());
      ps3d.setXlineRangeAndDelta(prototype.getXlineStart(), prototype.getXlineEnd(), prototype.getXlineDelta());
      ps3d.setZRangeAndDelta(zStart, zEnd, zDelta);
      ps3d.setZMaxRangeAndDelta(zStart, zEnd, zDelta);
      ps3d.setComment(prototype.getComment());
      ps3d.setDataUnit(prototype.getDataUnit());
      ps3d.setElevationDatum(prototype.getElevationDatum());
      ps3d.setElevationReferences(prototype.getElevationReference());
      ps3d.setTraceHeaderDefinition(prototype.getTraceHeaderDefinition());
      ps3d.markGhost();
      ps3d.markLoaded();
      mapper.create(ps3d);
    } else {
      // If it does exist in the datastore, then load it.
      ps3d.load();
    }

    // Now validate it is acceptable.
    validatePostStack3d(ps3d, zDomain, zStart, zEnd, zDelta);

    // Add the volume to the repository.
    repository.add(ps3d);
    return ps3d;
  }

  /**
   * The common factory method for creating a new 3D poststack entity.
   * 
   * @param repository
   *          the repository in which to add the created volume.
   * @param prototype
   *          the prototype volume from which to copy the properties.
   * @param mapper
   *          the volume mapper.
   * @param name
   *          the volume name.
   * @param zDomain
   *          the volume z domain (time or depth).
   * @param zStart
   *          the starting z value.
   * @param zEnd
   *          the ending z value.
   * @param zDelta
   *          the delta z value (sample rate).
   * @return the created PostStack3d.
   */
  public static PostStack3d create(final IRepository repository, final SeismicSurvey3d survey, final float inlineStart,
      final float inlineEnd, final float inlineDelta, final float xlineStart, final float xlineEnd,
      final float xlineDelta, final Unit dataUnit, final HeaderDefinition traceHeaderDef,
      final IPostStack3dMapper mapper, final String name, final Domain zDomain, final float zStart, final float zEnd,
      final float zDelta) throws Exception {

    // Create a new volume.
    PostStack3d ps3d = new PostStack3d(name, mapper);

    if (!mapper.existsInStore()) {
      // If it does not exist in the datastore, then create it.
      mapper.setDomain(zDomain);
      ps3d.setZDomain(zDomain);
      ps3d.setSurvey(survey);
      ps3d.setInlineRangeAndDelta(inlineStart, inlineEnd, inlineDelta);
      ps3d.setXlineRangeAndDelta(xlineStart, xlineEnd, xlineDelta);
      ps3d.setZRangeAndDelta(zStart, zEnd, zDelta);
      ps3d.setZMaxRangeAndDelta(zStart, zEnd, zDelta);
      //ps3d.setComment(prototype.getComment());
      ps3d.setDataUnit(dataUnit);
      //ps3d.setElevationDatum(prototype.getElevationDatum());
      //ps3d.setElevationReferences(prototype.getElevationReference());
      ps3d.setTraceHeaderDefinition(traceHeaderDef);
      ps3d.markGhost();
      ps3d.markLoaded();
      mapper.create(ps3d);
    } else {
      // If it does exist in the datastore, then load it.
      ps3d.load();
    }

    // Now validate it is acceptable.
    validatePostStack3d(ps3d, zDomain, zStart, zEnd, zDelta);

    // Add the volume to the repository.
    repository.add(ps3d);
    return ps3d;
  }

  /**
   * Checks if an entry with the proposed name exists in the underlying
   * datastore of a specified 3D poststack volume.
   * 
   * @param poststack
   *          the volume whose underlying datastore to check.
   * @param proposedName
   *          the name of the entry to search for.
   * @return <i>true</i> if an entry already exists; <i>false</i> if not.
   */
  public static boolean existsInStore(final PostStack3d poststack, final String proposedName) {
    return DataSource.existsInStore(poststack, proposedName);
  }

  public static String canCreate(final PostStack3d prototype, final String storageOrganizationCode,
      final String storageFormatCode) {

    // Lookup the storage organization from the code, or take it from the prototype if the code is empty.
    StorageOrganization storageOrganization = prototype.getStorageOrganization();
    if (!storageOrganizationCode.isEmpty()) {
      storageOrganization = StorageOrganization.lookupByCode(storageOrganizationCode);
    }

    // Lookup the storage format from the code, or take it from the prototype if the code is empty.
    StorageFormat storageFormat = prototype.getStorageFormat();
    if (!storageFormatCode.isEmpty()) {
      storageFormat = StorageFormat.lookupByCode(storageFormatCode);
    }

    IPostStack3dMapper mapper = (IPostStack3dMapper) prototype.getMapper();
    return mapper.canCreate(storageOrganization, storageFormat);
  }
}
