/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product.action;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;


public class ResetPerspectiveAction extends Action {

  private IWorkbenchWindow _window;

  public ResetPerspectiveAction(IWorkbenchWindow window) {
    _window = window;
    setText("Reset Default Layout");
  }

  public void run() {
    _window.getActivePage().resetPerspective();
  }

}
