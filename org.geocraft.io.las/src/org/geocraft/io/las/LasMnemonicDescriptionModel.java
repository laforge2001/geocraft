/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;


public class LasMnemonicDescriptionModel extends Model {

  private BooleanProperty _canLoad;

  private StringProperty _mnemonic;

  private StringProperty _description;

  private EnumProperty<Unit> _interpretedUnit;

  private StringProperty _lasFileUnit;

  private StringProperty _name;

  public final static String CAN_LOAD = "Load";

  public final static String MNEMONIC = "Mnemonic";

  public final static String DESCRIPTION = "Description";

  public final static String FILE_UNITS = "Units in File";

  public final static String INTERP_UNITS = "Interpreted Units";

  public final static String NAME = "Name";

  public LasMnemonicDescriptionModel() {
    _canLoad = addBooleanProperty(CAN_LOAD, true);
    _mnemonic = addStringProperty(MNEMONIC, " ");
    _description = addStringProperty(DESCRIPTION, " ");
    _lasFileUnit = addStringProperty(FILE_UNITS, " ");
    _interpretedUnit = addEnumProperty(INTERP_UNITS, Unit.class, Unit.UNDEFINED);
    _name = addStringProperty(NAME, " ");
  }

  public boolean getCanLoad() {
    return _canLoad.get();
  }

  public String getMnemonic() {
    return _mnemonic.get();
  }

  public String getDescription() {
    return _description.get();
  }

  public String getName() {
    return _name.get();
  }

  /**
   * retrieves the unit symbol from the las file
   * @return
   */
  public String getFileUnit() {
    return _lasFileUnit.get();
  }

  /**
   * retrieves what Geocraft thinks the las unit symbol maps to
   * 
   * @return
   */
  public Unit getInterpUnit() {
    return _interpretedUnit.get();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.IModel#validate(org.geocraft.core.model.validation.IValidation)
   */
  @Override
  public void validate(IValidation results) {
    if (getInterpUnit().equals(Unit.UNDEFINED)) {
      results.error(INTERP_UNITS, "Units could not be interpreted");
    }

  }

  @Override
  public boolean equals(Object model) {
    if (model instanceof LasMnemonicDescriptionModel) {
      LasMnemonicDescriptionModel testModel = (LasMnemonicDescriptionModel) model;
      return getCanLoad() == testModel.getCanLoad() && getMnemonic().equals(testModel.getMnemonic())
          && getDescription().equals(testModel.getDescription()) && getInterpUnit().equals(testModel.getInterpUnit())
          && getName().equals(testModel.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getMnemonic().hashCode() + getDescription().hashCode() + getInterpUnit().hashCode() + getName().hashCode();
  }

  @Override
  public String toString() {
    return getMnemonic() + " " + getDescription() + " " + getFileUnit() + " " + getInterpUnit() + " " + getName();
  }
}
