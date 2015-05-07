/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.ui.property;


import org.geocraft.algorithm.CreateModelessDialog;
import org.geocraft.algorithm.StandaloneAlgorithm;


public class RegisterPropertyEditing {

  static CreateModelessDialog _dialog;

  static StandaloneAlgorithm _calculator;

  public RegisterPropertyEditing() {
    // Blank for now
  }

  public static void setDialog(CreateModelessDialog dialog) {
    _dialog = dialog;
  }

  public static CreateModelessDialog getDialog() {
    return _dialog;
  }

  public static void setCalculator(StandaloneAlgorithm calculator) {
    _calculator = calculator;
  }

  public static StandaloneAlgorithm getCalculator() {
    return _calculator;
  }

  public static boolean isDialogOpen() {
    boolean dialogOpen = false;
    if (_dialog != null && _dialog.getShell() != null) {
      dialogOpen = true;
    }
    return dialogOpen;
  }

}
