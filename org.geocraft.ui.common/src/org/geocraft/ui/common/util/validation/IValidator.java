/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.common.util.validation;


import java.awt.Component;


/**
 * An interface to define validator methods.
 */
public interface IValidator {

  /**
   * The validate method.
   * 
   * @param value the value to be validated
   * @return <code>true</code> if the value is valid, <code>false</code> otherwise
   */
  boolean validate(String value);

  /**
   * The validate method that displays a warning if the value is not valid.
   * 
   * @param parent the parent component
   * @param value the value to be validated
   * @return <code>true</code> if the value is valid, <code>false</code> otherwise
   */
  boolean validate(Component parent, String value);
}
