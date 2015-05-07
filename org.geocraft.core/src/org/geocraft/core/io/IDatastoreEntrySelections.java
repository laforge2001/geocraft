/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.io;


import org.geocraft.core.model.mapper.MapperModel;


public interface IDatastoreEntrySelections {

  /**
   * Adds selected datastore entries to the selection list
   * for additional parameterization.
   * 
   * @param descriptions the array of entry names or descriptions.
   * @param models the array of mapper models.
   */
  void add(String[] descriptions, MapperModel[] models);
}