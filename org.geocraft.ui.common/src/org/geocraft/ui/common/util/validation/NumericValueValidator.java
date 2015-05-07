/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.common.util.validation;


import java.awt.Component;

import javax.swing.JOptionPane;


/**
 * A validator class for the numerical values.
 */
public class NumericValueValidator implements IValidator {

  /** The value to be validated. */
  private String _value;

  /** If the value should parse to an integer. */
  private boolean _integer;

  /** If the value needs to be in a range. */
  private boolean _range;

  /** The minimum value. */
  private float _minimum;

  /** The maximum value. */
  private float _maximum;

  /**
   * The constructor.
   * 
   * @param integer if the values should parse to an integer
   */
  public NumericValueValidator(boolean integer) {
    _integer = integer;
  }

  /**
   * Set the value range.
   * 
   * @param minimum the minimum value
   * @param maximum the maximum value
   */
  public void setRange(float minimum, float maximum) {
    _range = true;
    _minimum = Math.min(minimum, maximum);
    _maximum = Math.max(minimum, maximum);
  }

  /**
   * Validates the specified value.
   * 
   * @param value the string to be validated
   * @return <code>true</code> if the value is a float value, <code>false</code> otherwise
   */
  public boolean validate(String value) {
    _value = value;
    if (_value == null || _value.length() == 0) {
      return false;
    }
    try {
      float val = Float.parseFloat(_value);
      if (_integer) {
        val = Integer.parseInt(_value);
      }
      if (_range) {
        return val >= _minimum && val <= _maximum;
      }
      return true;
    } catch (NumberFormatException nE) {
      return false;
    }
  }

  /**
   * Validates the specified value and displays a warning message if the value is not a float.
   * 
   * @param parent the parent component
   * @param value the string to be validated
   * @return <code>true</code> if it is a numeric value, <code>false</code> otherwise
   */
  public boolean validate(Component parent, String value) {
    boolean valid = validate(value);
    if (!valid) {
      StringBuffer message = new StringBuffer(value);
      message.append(" is not a valid numeric value");
      if (_range) {
        message.append("\n or is not in the [" + _minimum + ".." + _maximum + "] range");
      }
      JOptionPane.showMessageDialog(parent, message.toString(), "Warning", JOptionPane.WARNING_MESSAGE);
    }
    return valid;
  }
}
