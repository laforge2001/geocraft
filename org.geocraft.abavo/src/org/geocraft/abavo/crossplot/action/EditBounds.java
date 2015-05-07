/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.geocraft.abavo.crossplot.ABavoCrossplot;
import org.geocraft.abavo.crossplot.CrossplotBoundsDialog;
import org.geocraft.abavo.crossplot.CrossplotBoundsModel;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class EditBounds extends Action {

  protected final CrossplotBoundsModel _model;

  protected ABavoCrossplot _crossplot;

  protected CrossplotBoundsDialog _dialog;

  public EditBounds(final ABavoCrossplot crossplot, final CrossplotBoundsModel model) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_PLOT_BOUNDS));
    setToolTipText("Edit the crossplot bounds");
    _model = model;
    _crossplot = crossplot;
  }

  @Override
  public void run() {
    Display.getCurrent().asyncExec(new Runnable() {

      public void run() {
        if (_dialog == null) {
          _dialog = CrossplotBoundsDialog.createEditorDialog(_crossplot, _model);
          _dialog.open();
          _dialog = null;
        }
      }
    });
  }
}
