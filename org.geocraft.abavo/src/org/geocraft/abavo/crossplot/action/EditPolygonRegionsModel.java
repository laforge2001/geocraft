/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.polygon.PolygonRegionsModel;
import org.geocraft.internal.abavo.polygon.PolygonRegionsModelEditorDialog;


public class EditPolygonRegionsModel extends Action {

  protected Shell _shell;

  protected final PolygonRegionsModel _model;

  protected PolygonRegionsModelEditorDialog _dialog;

  public EditPolygonRegionsModel(Shell shell, final PolygonRegionsModel model) {
    setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.POLYGON_REGIONS_MODEL));
    setToolTipText("Edit the polygon regions model");
    _shell = shell;
    _model = model;
  }

  @Override
  public void run() {
    Display.getCurrent().asyncExec(new Runnable() {

      public void run() {
        if (_dialog == null) {
          _dialog = PolygonRegionsModelEditorDialog.createEditorDialog(_shell, _model);
          int rtn = _dialog.open();
          _dialog = null;
        }
      }
    });
  }
}
