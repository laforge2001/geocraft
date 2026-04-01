/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product.action;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroManager;


public class PerspectiveAction extends Action {

  private final IWorkbenchWindow _window;

  private final String _perspectiveId;

  private final IPerspectiveDescriptor _descriptor;

  public PerspectiveAction(final IWorkbenchWindow window, final String id, final IPerspectiveDescriptor descriptor) {
    _window = window;
    _perspectiveId = id;
    _descriptor = descriptor;
    if (_descriptor != null) {
      setText(_descriptor.getLabel());
      setImageDescriptor(_descriptor.getImageDescriptor());
    }
  }

  @Override
  public void run() {
    try {
      IWorkbench workbench = PlatformUI.getWorkbench();
      IIntroManager introManager = workbench.getIntroManager();
      if (introManager.getIntro() != null) {
        introManager.closeIntro(introManager.getIntro());
      }
      workbench.showPerspective(_perspectiveId, _window);
    } catch (WorkbenchException e) {
      MessageDialog.openError(_window.getShell(), "Perspective error", "Error opening perspective: " + e.getMessage());
    }
  }

}
