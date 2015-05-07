/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.abavo.crossplot.ABDataSeries;


public class ShowSeriesStatistics extends Action {

  private final ABDataSeries _series;

  public ShowSeriesStatistics(final ABDataSeries series) {
    super("Show Statistics...");
    _series = series;
  }

  @Override
  public void run() {
    Shell shell = new Shell(SWT.SHELL_TRIM | SWT.MODELESS);
    MessageDialog.openInformation(shell, "Data Statistics: " + _series.getName(), _series.getRegressionDataStatistics()
        .getInfo());
  }
}
