/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.preferences;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;


/**
 * The coordinate system service. Not implemented yet. 
 * 
 * TODO if this is a 'service' it needs to do something like coordinate transformations otherwise rename it. 
 */
public class CoordinateSystemService {

  /** The coordinate systems. */
  // TODO perhaps this should be a set with a unique mapper for each system? 
  private final List<CoordinateSystem> _coordinateSystems;

  /** The singleton instance. */
  private static CoordinateSystemService _instance;

  /**
   * The singleton constructor.
   */
  private CoordinateSystemService() {
    _coordinateSystems = new ArrayList<CoordinateSystem>();

    _coordinateSystems.add(new CoordinateSystem("not implemented", Domain.DISTANCE));
    _coordinateSystems.add(new CoordinateSystem("not implemented", Domain.TIME));
  }

  /**
   * Return the singleton instance.
   * @return the singleton instance
   */
  public static CoordinateSystemService getInstance() {
    if (_instance == null) {
      _instance = new CoordinateSystemService();
    }
    return _instance;
  }

  /**
   * Add a new system to the service. 
   * 
   * @param system to add. 
   */
  public void registerSystem(final CoordinateSystem system) {
    _coordinateSystems.add(system);
  }

  /**
   * Return the coordinate system having the specified name and domain. 
   * 
   * @param name the coordinate system name
   * @param domain the coordinate system domain
   * @return the coordinate system
   */
  public CoordinateSystem getSystemForName(final String name, final Domain domain) {
    CoordinateSystem result = null;

    for (CoordinateSystem system : _coordinateSystems) {
      if (system.getName().equals(name) && system.getDomain() == domain) {
        result = system;
      }
    }

    return result;
  }

  /**
   * Return a list with all the coordinate systems for a specified domain.
   * 
   * @param domain the coordinate system domain
   * @return a list with the coordinate systems for a specified domain
   */
  public List<CoordinateSystem> getCoordinateSystems(final Domain domain) {
    List<CoordinateSystem> result = new ArrayList<CoordinateSystem>();

    for (CoordinateSystem system : _coordinateSystems) {
      if (system.getDomain() == domain) {
        result.add(system);
      }
    }

    return result;
  }

}
