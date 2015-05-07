/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.geologicfeature;


import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IMapper;


/**
 * The abstract base class for all geologic features.
 * 
 * A geologic feature identifies a geologic concept (e.g. a specific geologic
 * fault, horizon, or stratigraphic interval) but does not contain the geometric
 * representation of the geologic feature. In other words, a geologic feature
 * can be represented in different ways (grids, points, polylines, etc).
 */
public abstract class GeologicFeature extends Entity {

  public enum FeatureType {
    HORIZON("Horizon"),
    FAULT("Fault"),
    INTERVAL("Interval"),
    OTHER("Other");

    private String _name;

    FeatureType(final String name) {
      _name = name;
    }

    @Override
    public String toString() {
      return _name;
    }

    public static FeatureType lookupByName(final String name) {
      for (FeatureType type : FeatureType.values()) {
        if (type.toString().equals(name)) {
          return type;
        }
      }
      return null;
    }
  }

  /** Identifies the feature type - HORIZON, FAULT, INTERVAL, OTHER. */
  private final FeatureType _featureType;

  /**
   * Constructs a geologic feature with the given name and type.
   * @param name the name of the geologic feature.
   * @param type the type of the geologic feature.
   * @param mapper the entity mapper
   */
  protected GeologicFeature(final String name, final FeatureType featureType, final IMapper mapper) {
    super(name, mapper);
    _featureType = featureType;
  }

  /**
   * Returns the feature type - HORIZON, FAULT, INTERVAL, OTHER.
   * @return the feature type.
   */
  public FeatureType getFeatureType() {
    return _featureType;
  }

}
