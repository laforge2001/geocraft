/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import org.geocraft.core.model.EarthModel;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.repository.IRepository;


/**
 * This class generates synthetic, in-memory earth models.
 */
public class EarthModelGenerator {

  /** The repository in which to add earth models. */
  private IRepository _repository;

  /**
   * Constructs a synthetic earth model generator.
   * 
   * @param repository the repository in which to add generated earth models.
   */
  public EarthModelGenerator(final IRepository repository) {
    _repository = repository;
  }

  /**
   * Creates an in-memory earth model and adds it to the given repository.
   * 
   * @param name the name of the earth model.
   * @param domain the domain of the earth model.
   */
  public void addEarthModel(String name, Domain domain) {
    // Create an time-domain earth model and add it to the repository.
    EarthModel earthModel = new EarthModel(name, domain);
    _repository.add(earthModel);
  }

}
