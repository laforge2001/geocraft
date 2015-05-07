/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model;


import java.util.HashSet;
import java.util.Set;

import org.geocraft.core.model.mapper.IMapper;


/**
 * A set of horizon fault boundaries (e.g. fault polygons) representing
 * the intersection of a horizon Grid  with a set of faults.
 *
 * TODO 360 is add remove legit or should it be done in constructor?
 */

public class HorizonFaultBoundarySet extends Entity {

  /** The HorizonFaultBoundaries associated with this boundary set. */
  private final Set<HorizonFaultBoundary> _horizonFaultBoundaries = new HashSet<HorizonFaultBoundary>();

  /** Identifies the associated horizon representation if it exists. */
  //private Grid _horizonGrid;
  /**
   * Create an empty HorizonFaultBoundarySet.
   *
   * @param name of the HorizonFaultBoundarySet
   */
  public HorizonFaultBoundarySet(final String name, final IMapper mapper) {
    super(name, mapper);
  }

  /**
   * The HorizonFaultBoundaries associated with this boundary set.
   *
   * @return horizonFaultBoundaries
   */
  public HorizonFaultBoundary[] getHorizonFaultBoundaries() {

    HorizonFaultBoundary[] horizonFaultBoundariesArray = new HorizonFaultBoundary[_horizonFaultBoundaries.size()];

    _horizonFaultBoundaries.toArray(horizonFaultBoundariesArray);
    return horizonFaultBoundariesArray;
  }

  /**
   * Identifies the associated horizon Grid.
   *
   * @return horizonGrid
   */
  //  public Grid getHorizonGrid() {
  //    return _horizonGrid;
  //  }
  /**
   * The number of boundary polygons contained in this set.
   *
   * @return numBoundaries
   */
  public int getNumBoundaries() {
    return _horizonFaultBoundaries.size();
  }

  /**
   * Add an array of HorizonFaultBoundary, ignoring duplicates.
   *
   * @param horizonFaultBoundaries associated with this boundary set.
   */
  public void addToHorizonFaultBoundaries(final HorizonFaultBoundary[] horizonFaultBoundaries) {

    for (HorizonFaultBoundary horizonFaultBoundarie : horizonFaultBoundaries) {
      _horizonFaultBoundaries.add(horizonFaultBoundarie);
    }
  }

  /**
   * Remove elements specified in the parameter.
   *
   * @param horizonFaultBoundaries associated with this boundary set.
   */
  public void removeFromHorizonFaultBoundaries(final HorizonFaultBoundary[] horizonFaultBoundaries) {

    for (HorizonFaultBoundary horizonFaultBoundarie : horizonFaultBoundaries) {
      _horizonFaultBoundaries.remove(horizonFaultBoundarie);
    }
  }

  /**
   * Removes all horizonFaultBoundaries.
   */
  public void removeAllHorizonFaultBoundaries() {
    _horizonFaultBoundaries.clear();
  }
}
