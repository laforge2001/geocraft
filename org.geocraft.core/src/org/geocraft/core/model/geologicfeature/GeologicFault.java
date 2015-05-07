/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.geologicfeature;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatMeasurement;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.fault.FaultType;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


public class GeologicFault extends GeologicFeature {

  /** The user-assigned type of  fault. */
  private FaultType _faultType;

  /** The age of the geologic feature. */
  private FloatMeasurement _geologicalAge;

  public GeologicFault(final String name) {
    this(name, new InMemoryMapper(GeologicFault.class));
  }

  /**
   * Parameterized constructor.
   * @param name Name of the entity.
   * @param mapper the entity mapper
   */
  public GeologicFault(final String name, final IMapper mapper) {
    super(name, FeatureType.FAULT, mapper);
  }

  /**
   * The user-assigned type of  fault
   *
   * @return faultType
   */
  public FaultType getFaultType() {
    return _faultType;
  }

  /**
   * The age of the geologic feature.
   * @return geologicalAge
   */
  public FloatMeasurement getGeologicalAge() {
    return _geologicalAge;
  }

  /**
   * The user-assigned type of  fault
   * @param type
   */
  public void setFaultType(final FaultType type) {
    _faultType = type;
    setDirty(true);
  }

  /**
   * The age of the geologic feature.
   * @param geologicalAge
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
