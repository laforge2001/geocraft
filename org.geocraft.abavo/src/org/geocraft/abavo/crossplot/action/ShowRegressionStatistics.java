/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.math.regression.RegressionStatistics;


public class ShowRegressionStatistics extends Action {

  private final String _name;

  private final RegressionStatistics _stats;

  public ShowRegressionStatistics(final String name, final RegressionStatistics stats) {
    super("Show Statistics...");
    _name = name;
    _stats = stats;
  }

  @Override
  public void run() {
    Shell shell = new Shell(SWT.SHELL_TRIM | SWT.MODELESS);
    MessageDialog.openInformation(shell, "Data Statistics: " + _name, _stats.getInfo());
  }
}
