/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product.action;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.views.IViewDescriptor;


public class WindowAction extends Action {

  private final IWorkbenchWindow _window;

  private final String _viewId;

  private final IViewDescriptor _descriptor;

  public WindowAction(final IWorkbenchWindow window, final String id, final IViewDescriptor descriptor) {
    _window = window;
    _viewId = id;
    _descriptor = descriptor;
    if (_descriptor != null) {
      setText(_descriptor.getLabel());
      setImageDescriptor(_descriptor.getImageDescriptor());
    }
  }

  @Override
  public void run() {
    try {
      _window.getActivePage().showView(_viewId);
    } catch (WorkbenchException e) {
      MessageDialog.openError(_window.getShell(), "View error", "Error opening view: " + e.getMessage());
    }
  }

}
