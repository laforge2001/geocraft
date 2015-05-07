/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.geologicfeature.GeologicHorizon;
import org.geocraft.core.repository.IRepository;


/**
 * This class generates synthetic, in-memory geologic horizons.
 */
public final class GeologicHorizonGenerator {

  /** The repository in which to add geologic horizons. */
  private IRepository _repository;

  /** The list of geologic horizons that have been generated. */
  private List<GeologicHorizon> _horizons = new ArrayList<GeologicHorizon>();

  /**
   * Constructs a synthetic geologic horizon generator.
   * 
   * @param repository the repository in which to add generated geologic horizons.
   */
  public GeologicHorizonGenerator(final IRepository repository) {
    _repository = repository;
  }

  /**
   * Creates an in-memory geologic horizon and adds it to the repository.
   * 
   * @param index the index of the horizon to generate.
   */
  public synchronized void addHorizon(final int index) {
    // Create an in-memory geologic horizon.
    GeologicHorizon horizon = new GeologicHorizon("Horizon" + index);

    // Add the horizon to the repository.
    horizon.setDirty(false);
    _repository.add(horizon);

    // Store the horizon in the internal list.
    _horizons.add(horizon);
  }

  /**
   * Returns an array of the geologic horizons that have been generated.
   * 
   * @return an array of the generated horizons.
   */
  public synchronized GeologicHorizon[] getHorizons() {
    return _horizons.toArray(new GeologicHorizon[0]);
  }
}
