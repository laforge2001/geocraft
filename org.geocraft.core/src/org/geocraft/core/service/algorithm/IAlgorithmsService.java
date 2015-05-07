/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved. 
 */
package org.geocraft.core.service.algorithm;


import java.util.Map;


/** Access methods to the registry of active standalone algorithms */
public interface IAlgorithmsService {

  /**
   * Get list of all registered algorithms. key is the unique ID
   * of the algorithm instance (there may be may active instances
   * of the same algorithm); value = algorithm.
   * @return Map of all active algorithms. 
   */
  Map<Integer, Object> getRegisteredAlgorithms();

  /**
   * Get the algorithm's parameters.
   * @param key Unique ID of the algorithm instance
   * @return Parameters of the specified algorithm
   */
  Map<String, String> getAlgorithmParms(int key);

  /**
   * Get the algorithm's name.
   * @param key Unique ID of the algorithm instance
   * @return Name of the specified algorithm
   */
  String getAlgorithmName(int key);

  /**
   * Get the algorithm's class name.
   * @param key Unique ID of the algorithm instance
   * @return Class name of the specified algorithm
   */
  String getAlgorithmClassName(int key);

  /**
   * Get the description of a registered algorithm.
   * @param key Unique ID of the algorithm instance
   * @return Descriptor of the specified algorithm
   */
  Object getDescription(int key);

  /**
   * Get the ID of the workbench window containing the algorithm
   * @param key Unique ID of the algorithm instance
   * @return The ID of the workbench window containing the algorithm
   */
  String getWindowID(int key);

  /**
   * Add (register) an active standalone algorithm to the registry.
   * @param object Algorithm
   * @return Unique key of the algorithm instance
   */
  int add(Object algorithm);

  /**
   * Remove (unregister) an algorithm from the (active algorithms) registry.
   * @param key Unique ID of the algorithm instance
   */
  void remove(int key);

  /**
   * Deactivate a registered algorithm, i.e., close it
   * and close it.
   * @param key Unique ID of the algorithm instance
   */
  void deactivate(int key);

  /**
   * Activate an algorithm and register it
   * @param klass Full class name of algorithm
   * @param parms (key,value) pairs of algorithm parameters
   */
  int activate(String klass, Map<String, String> parms);

  void dumpRegistry();

  void removeAll();
}
