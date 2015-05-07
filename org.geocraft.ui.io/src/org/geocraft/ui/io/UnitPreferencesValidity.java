package org.geocraft.ui.io;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.validation.IValidation;


public class UnitPreferencesValidity {

  public static void checkXYUnitOfMeasurement(final IValidation results, final String key, final Unit unit) {
    if (unit != null) {
      Unit preferenceUnits;
      preferenceUnits = UnitPreferences.getInstance().getHorizontalDistanceUnit();
      if (!preferenceUnits.equals(unit)) {
        setInfoMessage(results, key, unit, preferenceUnits);
      }
    }
  }

  public static void checkZUnitOfMeasurement(final IValidation results, final String key, final Unit unit) {
    if (unit != null) {
      Domain domain = unit.getDomain();
      Unit preferenceUnits;
      if (domain == Domain.DISTANCE) {
        preferenceUnits = UnitPreferences.getInstance().getVerticalDistanceUnit();
        if (!preferenceUnits.equals(unit)) {
          setInfoMessage(results, key, unit, preferenceUnits);
        }
      }
      if (domain == Domain.TIME) {
        preferenceUnits = UnitPreferences.getInstance().getTimeUnit();
        if (!preferenceUnits.equals(unit)) {
          setInfoMessage(results, key, unit, preferenceUnits);
        }
      }
    }
  }

  private static void setInfoMessage(final IValidation results, final String key, final Unit unit,
      final Unit preferenceUnit) {
    String message = "The datastore unit (" + unit.toString() + ") doesn't match the application preferences unit ("
        + preferenceUnit.toString() + ").\nThe necessary conversions will be handled automatically.";
    results.info(key, message);
  }
}
