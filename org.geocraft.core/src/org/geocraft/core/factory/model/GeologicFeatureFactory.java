/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.factory.model;


import org.geocraft.core.model.geologicfeature.GeologicFault;
import org.geocraft.core.model.geologicfeature.GeologicFeature;
import org.geocraft.core.model.geologicfeature.GeologicHorizon;
import org.geocraft.core.model.geologicfeature.GeologicInterval;
import org.geocraft.core.model.geologicfeature.GeologicFeature.FeatureType;
import org.geocraft.core.model.mapper.IMapper;


public class GeologicFeatureFactory {

  /**
   * Constructs a geologic feature given a feature type, name and a mapper.
   * @param type the feature type (e.g. HORIZON, FAULT, INTERVAL).
   * @param name the name of the geologic feature.
   * @param mapper the mapper.
   * @return the constructed geologic feature.
   */
  public static GeologicFeature create(final FeatureType type, final String name, final IMapper mapper) {
    if (type.equals(FeatureType.HORIZON)) {
      return new GeologicHorizon(name, mapper);
    } else if (type.equals(FeatureType.FAULT)) {
      return new GeologicFault(name, mapper);
    } else if (type.equals(FeatureType.INTERVAL)) {
      return new GeologicInterval(name, mapper);
    }

    return null;
  }

  /**
   * Constructs a geologic feature given a prototype feature and a name.
   * @param prototype the prototype feature.
   * @param name the name of the geologic feature.
   * @return the constructed geologic feature.
   */
  public static GeologicFeature create(final GeologicFeature prototype, final String name) throws Exception {
    FeatureType type = prototype.getFeatureType();
    if (type.equals(FeatureType.HORIZON)) {
      return HorizonFactory.create((GeologicHorizon) prototype, name);
    } else if (type.equals(FeatureType.FAULT)) {
      return GeologicFaultFactory.create((GeologicFault) prototype, name);
    } else if (type.equals(FeatureType.INTERVAL)) {
      return IntervalFactory.create((GeologicInterval) prototype, name);
    }
    return null;
  }
}
