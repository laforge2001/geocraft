/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.geologicfeature;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatMeasurement;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * <code>Horizon</code> is a geologic boundary representing the interface between
 * two strata or an unconformity.
 */
public class GeologicHorizon extends GeologicFeature {

  /** Enumeration for the types of horizon classification. */
  public enum HorizonClassification {
    CONFORMABLE("Conformable"),
    UNCONFORMITY("Unconformity"),
    DISCONFORMITY("Disconformity"),
    FLUIDCONTACT("Fluid Contact"),
    OTHER("Other");

    private String _text;

    private HorizonClassification(final String text) {
      _text = text;
    }

    @Override
    public String toString() {
      return _text;
    }
  }

  /** The user-assigned classification of the horizon. */
  private HorizonClassification _classification;

  /** The geological age of the horizon. */
  private FloatMeasurement _geologicalAge;

  public GeologicHorizon(final String name) {
    this(name, new InMemoryMapper(GeologicHorizon.class));
  }

  /**
   * Parameterized constructor. TODO 360. 
   * @param name of the horizon.
   * @param mapper the datastore mapper.
   */
  public GeologicHorizon(final String name, final IMapper mapper) {
    super(name, FeatureType.HORIZON, mapper);
  }

  /**
   * Returns the user-assigned classification of the horizon.
   * @return the classification of the horizon.
   */
  public HorizonClassification getClassification() {
    return _classification;
  }

  /**
   * Returns the geological age of the horizon.
   * The age is returned as a float measurement, containing a value and unit of measurement.
   * @return the geological age.
   */
  public FloatMeasurement getGeologicalAge() {
    return _geologicalAge;
  }

  /**
   * Sets the user-assigned classification of the horizon.
   * @param classification the horizon classification.
   */
  public void setClassification(final HorizonClassification classification) {
    _classification = classification;
    setDirty(true);
  }

  /**
   * Sets the geological age of the horizon.
   * The age is set as a float measurement, containing a value and unit of measurement.
   * @param geologicalAge the geological age
   */
  public void setGeologicalAge(final FloatMeasurement geologicalAge) {
    Unit unit = geologicalAge.getUnit();
    if (geologicalAge.getUnit().getDomain() != Domain.TIME) {
      throw new IllegalArgumentException("Geologic age in units of \'" + unit.getSymbol()
          + "\' is not in the time domain.");
    }
    _geologicalAge = geologicalAge;
    setDirty(true);
  }

}
