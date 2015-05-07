/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.mapper;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;


/**
 * The base interface for all mappers that map between a seismic dataset entity and a datastore.
 * <p>
 * A seismic dataset entity can be any class that extends the <code>SeismicDataset</code> base class.
 */
public interface ISeismicDatasetMapper<E extends SeismicDataset> extends IMapper<E> {

  /**
   * Sets the z-domain of the seismic dataset.
   * <p>
   * This method must be called after the mapper is instantiated but before the <code>create</code> method is called.
   * 
   * @param zDomain the z domain.
   */
  void setDomain(Domain zDomain);

  /**
   * Gets the format of the seismic dataset in the datastore.
   * <p>
   * Options include 8-bit integer, 16-bit integer, 32-bit floating point, etc.
   * 
   * @return the storage format of the seismic dataset.
   */
  StorageFormat getStorageFormat();

  /**
   * Sets the format of the seismic dataset in the datastore.
   * <p>
   * Options include 8-bit integer, 16-bit integer, 32-bit floating point, etc.
   * <p>
   * This method must be called after the mapper is instantiated but before the <code>create</code> method is called.
   * 
   * @param storageFormat the storage format to set.
   */
  void setStorageFormat(StorageFormat storageFormat);

  /**
   * Closes access to the datastore.
   * <p>
   * This method should be called after all I/O is done.
   */
  void close();

}
