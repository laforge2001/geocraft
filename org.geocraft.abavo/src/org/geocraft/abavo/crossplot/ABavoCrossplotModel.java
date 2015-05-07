/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.geocraft.abavo.defs.CommunicationStatus;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.ObjectProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.math.regression.RegressionMethodDescription;


public class ABavoCrossplotModel extends Model {

  public static final String REGRESSION_METHOD = "Regression Method";

  public static final String ANCHORED_TO_ORIGIN = "Anchored To Origin";

  public static final String COMMUNICATION_STATUS = "Communication Status";

  private ObjectProperty<RegressionMethodDescription> _regressionMethod;

  private BooleanProperty _anchoredToOrigin;

  private EnumProperty<CommunicationStatus> _communicationStatus;

  public ABavoCrossplotModel() {
    _regressionMethod = addObjectProperty(REGRESSION_METHOD, RegressionMethodDescription.class);
    _anchoredToOrigin = addBooleanProperty(ANCHORED_TO_ORIGIN, true);
    _communicationStatus = addEnumProperty(COMMUNICATION_STATUS, CommunicationStatus.class,
        CommunicationStatus.COMMUNICATION_DISABLED);
  }

  public RegressionMethodDescription getRegressionMethod() {
    return _regressionMethod.get();
  }

  public void setRegressionMethod(final RegressionMethodDescription regressionMethod) {
    _regressionMethod.set(regressionMethod);
  }

  public boolean getAnchoredToOrigin() {
    return _anchoredToOrigin.get();
  }

  public void setAnchoredToOrigin(final boolean anchoredToOrigin) {
    _anchoredToOrigin.set(anchoredToOrigin);
  }

  public CommunicationStatus getCommunicationStatus() {
    return _communicationStatus.get();
  }

  public void setCommunicationStatus(final CommunicationStatus communicationStatus) {
    _communicationStatus.set(communicationStatus);
  }

  public void validate(IValidation results) {
    if (_regressionMethod.isNull()) {
      results.error(_regressionMethod, "Regression method not defined");
    }

    if (_communicationStatus.isNull()) {
      results.error(_communicationStatus, "Communication status not defined");
    }
  }
}
