/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.factory.model;


import java.util.Map;

import org.geocraft.core.io.IMapperFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;


/**
 * Factory methods for creating <code>Wavelet</code> entities.
 */
public class WaveletFactory {

  /** The logger. */

  public static Wavelet create(final IRepository repository, final String name, final IMapper mapper,
      final float startTime, final float timeInterval, final float[] values, final String dataType, final String phase) {

    // Get all the wavelets in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(Wavelet.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      Wavelet wavelet = (Wavelet) object;
      // Returns the existing wavelet if its unique ID matches.
      if (wavelet.getUniqueID().equals(mapper.getUniqueID())) {
        updateWavelet(wavelet, startTime, timeInterval, values, dataType, phase);
        return wavelet;
      }
    }

    // If no existing wavelet found, create and return a new wavelet backed by the new mapper.
    return createInternal(repository, name, mapper, startTime, timeInterval, values, dataType, phase);
  }

  /**
   * Returns a new or existing wavelet based on a prototype wavelet. The time
   * range, data values, data type and phase will be taken from the prototype
   * wavelet. If the entity already exists in the given repository, then that
   * instance will be returned instead. If a new instance if created, it will
   * automatically be added to the repository. The repository is first searched
   * for an existing wavelet matching the unique ID. If one is found, then the
   * wavelet is updated and returned. If not, the a new wavelet is created and
   * returned.
   * 
   * @param repository
   *          the repository in which to search for an existing wavelet.
   * @param prototype
   *          the prototype wavelet.
   * @param name
   *          the name of the new wavelet.
   * @return the wavelet.
   */
  public static Wavelet create(final IRepository repository, final Wavelet prototype, final String name) {

    // Find the datastore accessor service. This will be used to replicate
    // the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the wavelets in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(Wavelet.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      Wavelet wavelet = (Wavelet) object;
      // Returns the existing wavelet if its unique ID matches.
      if (wavelet.getUniqueID().equals(mapper.getUniqueID())) {
        updateWavelet(wavelet, prototype.getTimeStart(), prototype.getTimeInterval(), prototype.getValues(), prototype
            .getDataType(), prototype.getPhase());
        return wavelet;
      }
    }

    // If no existing wavelet found, create and return a new wavelet backed by the new mapper.
    return createInternal(repository, name, mapper, prototype.getTimeStart(), prototype.getTimeInterval(), prototype
        .getValues(), prototype.getDataType(), prototype.getPhase());
  }

  /**
   * Returns a new or existing wavelet, based on a prototype wavelet. This
   * method allows the user to specify the time range, data values, data type
   * and phase of the new wavelet. The repository is first searched for an
   * existing wavelet matching the unique ID. If one is found, then the wavelet
   * is updated and returned. If not, the a new wavelet is created and returned.
   * 
   * @param repository
   *          the repository in which to search for an existing wavelet.
   * @param prototype
   *          the prototype wavelet.
   * @param name
   *          the name of the new wavelet.
   * @param timeStart
   *          the starting time of the wavelet.
   * @param timeInterval
   *          the time interval between wavelet samples.
   * @param dataValues
   *          the array of wavelet data values.
   * @param dataType
   *          the wavelet data type descriptor.
   * @param phase
   *          the wavelet phase descriptor.
   * @return the wavelet.
   */
  public static Wavelet create(final IRepository repository, final Wavelet prototype, final String name,
      final float timeStart, final float timeInterval, final float[] dataValues, final String dataType,
      final String phase) {

    // Find the datastore accessor service. This will be used to replicate
    // the prototype mapper.
    IMapperFactory factory = ServiceProvider.getDatastoreAccessorService();
    if (factory == null) {
      throw new RuntimeException("Datastore service not available.");
    }

    // Create a new mapper based on the prototype mapper.
    IMapper mapper = factory.createMapper(prototype.getMapper(), name);
    if (mapper == null) {
      throw new RuntimeException("Could not create mapper: " + prototype.getClass());
    }

    // Get all the wavelets in the repository.
    Map<String, Object> map = repository.get(new TypeSpecification(Wavelet.class));

    // Loop thru each one comparing its unique ID to the one to be created.
    for (Object object : map.values()) {
      Wavelet wavelet = (Wavelet) object;
      // Returns the existing wavelet if its unique ID matches.
      if (wavelet.getUniqueID().equals(mapper.getUniqueID())) {
        updateWavelet(wavelet, timeStart, timeInterval, dataValues, dataType, phase);
        return wavelet;
      }
    }

    // If no existing wavelet found, create and return a new wavelet backed by the new mapper.
    return createInternal(repository, name, mapper, timeStart, timeInterval, dataValues, dataType, phase);
  }

  /**
   * The common factory method for creating a new wavelet entity. This allows
   * for specifying the wavelet geometry, data unit and data values.
   * 
   * @param repository
   *          the repository in which to add the created wavelet.
   * @param name
   *          the name of the new wavelet.
   * @param mapper
   *          the mapper to the underlying datastore.
   * @param timeStart
   *          the starting time of the wavelet.
   * @param timeInterval
   *          the time interval between wavelet samples.
   * @param dataValues
   *          the array of wavelet data values.
   * @param dataType
   *          the wavelet data type descriptor.
   * @param phase
   *          the wavelet phase descriptor.
   * @return the created wavelet.
   */
  private static Wavelet createInternal(final IRepository repository, final String name, final IMapper mapper,
      final float timeStart, final float timeInterval, final float[] dataValues, final String dataType,
      final String phase) {

    try {
      // Create the new wavelet.
      Wavelet wavelet = new Wavelet(name, mapper);

      // Update the wavelet properties.
      updateWavelet(wavelet, timeStart, timeInterval, dataValues, dataType, phase);

      // Check if the wavelet exists in the datastore.
      if (!wavelet.getMapper().existsInStore()) {
        // If not, create it first.
        wavelet.getMapper().create(wavelet);
      }
      wavelet.update();

      // Add the wavelet to the repository.
      repository.add(wavelet);
      return wavelet;
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }

  /**
   * Updates various properties of a wavelet. Afterwards, the wavelet is marked
   * as loaded.
   * 
   * @param wavelet
   *          the wavelet to update.
   * @param timeStart
   *          the starting time of the wavelet.
   * @param timeInterval
   *          the time interval between wavelet samples.
   * @param dataValues
   *          the array of wavelet data values.
   * @param dataType
   *          the wavelet data type descriptor.
   * @param phase
   *          the wavelet phase descriptor.
   */
  private static void updateWavelet(final Wavelet wavelet, final float timeStart, final float timeInterval,
      final float[] dataValues, final String dataType, final String phase) {
    // Update the wavelet properties.
    wavelet.setValues(dataValues);
    float timeEnd = timeStart + (dataValues.length - 1) * timeInterval;
    wavelet.setTimeRange(timeStart, timeEnd, timeInterval);
    wavelet.setDataType(dataType);
    wavelet.setPhase(phase);

    // Mark the wavelet as loaded and return it.
    wavelet.markGhost();
    wavelet.markLoaded();
  }

  /**
   * Creates a wavelet with an in-memory mapper. This allows for specifying the
   * wavelet time range, data values and some descriptive information.
   * 
   * @param name
   *          the name of the new wavelet.
   * @param timeStart
   *          the starting time of the wavelet.
   * @param timeInterval
   *          the time interval between wavelet samples.
   * @param dataValues
   *          the array of wavelet data values.
   * @param dataType
   *          the wavelet data type descriptor.
   * @param phase
   *          the wavelet phase descriptor.
   * @return the created wavelet.
   */
  public static Wavelet createInMemory(final String name, final float timeStart, final float timeInterval,
      final float[] dataValues, final String dataType, final String phase) {

    // Create a new wavelet.
    Wavelet wavelet = new Wavelet(name, new InMemoryMapper(Wavelet.class));

    // Update the wavelet properties.
    updateWavelet(wavelet, timeStart, timeInterval, dataValues, dataType, phase);
    return wavelet;
  }

  /**
   * Checks if an entry with the proposed name exists in the underlying
   * datastore of a specified wavelet.
   * 
   * @param wavelet
   *          the wavelet whose underlying datastore to check.
   * @param proposedName
   *          the name of the entry to search for.
   * @return <i>true</i> if an entry already exists; <i>false</i> if not.
   */
  public static boolean existsInStore(final Wavelet wavelet, final String proposedName) {
    return DataSource.existsInStore(wavelet, proposedName);
  }
}
