/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.abavo.crossplot.ABDataSeries;
import org.geocraft.core.common.io.TextFile;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.model.validation.Validation;


public class WriteSeriesToFile extends Action {

  private final ABDataSeries _series;

  public WriteSeriesToFile(final ABDataSeries series) {
    super("Write points to file...");
    _series = series;
  }

  @Override
  public void run() {
    Shell shell = new Shell(SWT.SHELL_TRIM | SWT.MODELESS);
    WriteSeriesToFileModelDialog dialog = new WriteSeriesToFileModelDialog(shell, _series);
    int result = dialog.open();
    if (result == IDialogConstants.OK_ID) {
      WriteSeriesToFileModel model = dialog.getModel();
      IValidation validation = new Validation();
      model.validate(validation);
      if (validation.containsError()) {
        MessageDialog.openError(shell, "Error", validation.getStatusMessages(0));
        return;
      }
      String directory = model.getDirectory();
      String fileName = model.getFileName();
      boolean columnA = model.getColumnA();
      boolean columnB = model.getColumnB();
      boolean columnX = model.getColumnX();
      boolean columnY = model.getColumnY();
      boolean columnZ = model.getColumnZ();
      float[] a = _series.getA();
      float[] b = _series.getB();
      Point3d[] points = _series.getPoints();
      TextFile tf = new TextFile();
      int numPoints = _series.getNumPoints();
      for (int i = 0; i < numPoints; i++) {
        String line = "";
        if (columnA) {
          line += a[i] + "  ";
        }
        if (columnB) {
          line += b[i] + "  ";
        }
        if (columnX) {
          line += points[i].getX() + "  ";
        }
        if (columnY) {
          line += points[i].getY() + "  ";
        }
        if (columnZ) {
          line += points[i].getZ() + "  ";
        }
        tf.add(line);
      }
      tf.write(directory + File.separator + fileName + ".txt");
    }
  }
}
