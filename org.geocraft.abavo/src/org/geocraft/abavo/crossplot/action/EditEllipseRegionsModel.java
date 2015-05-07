/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.internal.abavo.ellipse.EllipseRegionsModelEditorDialog;


public class EditEllipseRegionsModel extends Action {

  protected Shell _shell;

  protected final EllipseRegionsModel _model;

  protected EllipseRegionsModelEditorDialog _dialog;

  public EditEllipseRegionsModel(Shell shell, final EllipseRegionsModel model) {
    setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.ELLIPSE_REGIONS_MODEL));
    setToolTipText("Edit the ellipse regions model");
    _shell = shell;
    _model = model;
  }

  @Override
  public void run() {
    Display.getCurrent().asyncExec(new Runnable() {

      public void run() {
        if (_dialog == null) {
          _dialog = EllipseRegionsModelEditorDialog.createEditorDialog(_shell, _model);
          _dialog.open();
          _dialog = null;
        }
      }
    });
  }
}
