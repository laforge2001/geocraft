/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.algorithm;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.session.AlgorithmParameterStore;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class RestoreParamsAction extends Action {

  private Shell _shell;

  private StandaloneAlgorithm _algorithm;

  public RestoreParamsAction(final Shell shell) {
    setToolTipText("Restore Previously Run Parameters");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_EDIT));
    _shell = shell;
    _algorithm = null;
  }

  public void setAlgorithm(StandaloneAlgorithm algorithm) {
    _algorithm = algorithm;
  }

  @Override
  public void run() {
    try {
      // Restore the algorithm parameters from the preferences store.
      AlgorithmParameterStore.restore(_algorithm);
    } catch (Exception ex) {
      // If an exception occurs, popup an error dialog.
      String message = "Algorithm parameters not restored:\n" + ex.getMessage();
      MessageDialog.openError(_shell, "Restore Algorithm Parameters Error", message);
    }
  }

}
