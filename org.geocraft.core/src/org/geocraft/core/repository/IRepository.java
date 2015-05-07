/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.repository;


import java.util.HashMap;
import java.util.Map;

import org.geocraft.core.repository.specification.ISpecification;


/**
 * Defines the methods that a repository should implement.
 */
public interface IRepository {

  /**
   * Used to indicate we couldn't locate an object.
   */
  String UNKNOWN_VARIABLE = "unknown object";

  /**
   * Set the var name map, which maps an entity's unique ID to
   * var name to be used when loading the entity.
   * @param varMap
   */
  void setVarnameMap(HashMap<String, String> varMap);

  /**
   * Add a data object to the repository.
   * 
   * @param key
   * @param object
   */
  void add(String key, Object object);

  /**
   * Add a data object to the repository and assign it a default variable name.
   * 
   * @param object
   * @return the added object name
   */
  String add(Object object);

  /**
   * Bulk addition of new data objects to the repository. Assigns default variable names.
   * 
   * @param object
   * @return the added objects names
   */
  String[] add(Object[] object);

  /**
   * The filtered version of the contents of the repository.
   * 
   * @param filter
   *                to apply
   * @return the filtered variables
   */
  Map<String, Object> get(ISpecification filter);

  /**
   * Return all the contents of the repository.
   * 
   * @return the variables
   */
  Map<String, Object> getAll();

  /**
   * Access a data object in the repository.
   * 
   * @param name
   *                the object name
   * @return the data object.
   */
  Object get(String name);

  /**
   * Checks to see if the repository contains objects that match this specification.
   */
  boolean contains(ISpecification spec);

  /**
   * Deletes the named variable from the name space.
   * 
   * @param name
   *                the variable name
   */
  void remove(String name);

  /**
   * Delete the named variables from the name space.
   * 
   * @param names
   *                the variable names
   */
  void remove(String[] names);

  /**
   * Delete the named variables from the name space.
   * 
   * @param spec
   */
  void remove(ISpecification spec);

  /**
   * Empty the contents of the repository.
   */
  void clear();

  /**
   * Looks up the variable name associated with this java object reference.
   */
  String lookupVariableName(Object obj);

  String getNextVariableName();
}
