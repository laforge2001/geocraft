/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.validation.IValidation;


public class CrossplotBoundsModel extends Model {

  /** Enumeration of the various types of crossplot bounds. */
  public static enum BoundsType {
    AUTOMATIC("Automatic"),
    COMMON_MIN_MAX("Common Min/Max"),
    USER_DEFINED("User-Defined");

    private String _name;

    BoundsType(final String name) {
      _name = name;
    }

    @Override
    public String toString() {
      return _name;
    }
  }

  public static final String BOUNDS_TYPE = "Bounds Type";

  public static final String COMMON_MINMAX = "Common Min/Max";

  public static final String START_A = "Start A";

  public static final String END_A = "End A";

  public static final String START_B = "Start B";

  public static final String END_B = "End B";

  private EnumProperty<BoundsType> _boundsType;

  private DoubleProperty _commonMinMax;

  private DoubleProperty _startA;

  private DoubleProperty _endA;

  private DoubleProperty _startB;

  private DoubleProperty _endB;

  /**
   * Constructs a crossplot bounds model with default values.
   * 
   * @param boundsType the crossplot bounds type.
   * @param commonMinMax the common min/max value (for COMMON_MIN_MAX bounds only).
   * @param startA the starting A value (for USER_DEFINED bounds only).
   * @param endA the ending A value (for USER_DEFINED bounds only).
   * @param startB the starting B value (for USER_DEFINED bounds only).
   * @param endB the ending B value (for USER_DEFINED bounds only).
   */
  public CrossplotBoundsModel(BoundsType boundsType, double commonMinMax, double startA, double endA, double startB, double endB) {
    _boundsType = addEnumProperty(BOUNDS_TYPE, BoundsType.class, boundsType);
    _commonMinMax = addDoubleProperty(COMMON_MINMAX, commonMinMax);
    _startA = addDoubleProperty(START_A, startA);
    _endA = addDoubleProperty(END_A, endA);
    _startB = addDoubleProperty(START_B, startB);
    _endB = addDoubleProperty(END_B, endB);
  }

  /**
   * Constructs a crossplot bounds model that is a copy of another.
   * 
   * @param model the model to copy.
   */
  public CrossplotBoundsModel(final CrossplotBoundsModel model) {
    this(model.getBoundsType(), model.getCommonMinMax(), model.getStartA(), model.getEndA(), model.getStartB(), model
        .getEndB());
  }

  /**
   * Returns the crossplot bounds type.
   */
  public BoundsType getBoundsType() {
    return _boundsType.get();
  }

  /**
   * Returns the common min/max value (for COMMON_MIN_MAX bounds only).
   */
  public double getCommonMinMax() {
    return _commonMinMax.get();
  }

  /**
   * Returns the starting A value (for USER_DEFINED bounds only).
   */
  public double getStartA() {
    return _startA.get();
  }

  /**
   * Returns the ending A value (for USER_DEFINED bounds only).
   */
  public double getEndA() {
    return _endA.get();
  }

  /**
   * Returns the starting B value (for USER_DEFINED bounds only).
   */
  public double getStartB() {
    return _startB.get();
  }

  /**
   * Returns the ending B value (for USER_DEFINED bounds only).
   */
  public double getEndB() {
    return _endB.get();
  }

  public void validate(IValidation results) {
    if (_boundsType.isNull()) {
      results.error(_boundsType, "Bounds type not defined");
    } else {
      switch (_boundsType.get()) {
        case COMMON_MIN_MAX:
          validateDoubleProperty(results, _commonMinMax);
          break;
        case USER_DEFINED:
          validateDoubleProperty(results, _startA);
          validateDoubleProperty(results, _endA);
          validateDoubleProperty(results, _startB);
          validateDoubleProperty(results, _endB);
          break;
        default:
      }
    }
  }

  /**
   * Validates that the value of a double property is neither a NaN nor infinite.
   * 
   * @param results the object in which to put the validation results.
   * @param property the double property to check.
   */
  private void validateDoubleProperty(IValidation results, DoubleProperty property) {
    double value = property.get();
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      results.error(property, "Invalid double value: " + value);
    }
  }
}
