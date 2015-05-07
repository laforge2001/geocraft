/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.common.util.validation;


import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;


/**
 * A validator class for the variable names.
 */
public class VariableNameValidator implements IValidator {

  /** The first valid character of a Python variable name. */
  private static final String LETTERS = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /** Valid characters in Python variable names. */
  private static final String LETTERS_NUMBERS = LETTERS + "0123456789";

  /** Python keywords. */
  private static final String[] KEYWORDS = new String[] { "and", "assert", "break", "class", "continue", "def", "del",
      "elif", "else", "except", "exec", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda",
      "not", "or", "pass", "print", "raise", "return", "try", "while" };

  /** The Python keywords list. */
  private static List<String> _keywords = Arrays.asList(KEYWORDS);

  /** The variable name to be validated. */
  private String _varName;

  /**
   * Validates the specified variable name.
   * 
   * @param varName the variable name
   * @return <code>true</code> if the variable has a valid name, <code>false</code> otherwise
   */
  public boolean validate(String varName) {
    _varName = varName;
    if (_varName == null || _varName.length() == 0 || _keywords.contains(_varName)
        || LETTERS.indexOf(_varName.charAt(0)) < 0) {
      return false;
    }
    boolean valid = true;
    for (int i = 1; i < _varName.length() && valid; i++) {
      valid = LETTERS_NUMBERS.indexOf(_varName.charAt(i)) >= 0;
    }
    return valid;
  }

  /**
   * Validates the specified variable name and displays a warning message if the variable name is not valid.
   * 
   * @param parent the parent component
   * @param varName the variable name
   * @return <code>true</code> if the variable has a valid name, <code>false</code> otherwise
   */
  public boolean validate(Component parent, String varName) {
    boolean valid = validate(varName);
    if (!valid) {
      JOptionPane.showMessageDialog(parent, varName + " is not a valid variable name", "Warning",
          JOptionPane.WARNING_MESSAGE);
    }
    return valid;
  }
}
