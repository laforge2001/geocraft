/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonNavigator;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.io.DatastoreSelectionDialog;


public class LoadAction extends Action {

  /** The logger. */

  /** The repository navigator. */
  private final CommonNavigator _navigator;

  /**
   * The default constructor.
   * 
   * @param navigator the repository navigator.
   */
  public LoadAction(final CommonNavigator navigator) {
    _navigator = navigator;
    try {
      getClass().getClassLoader().loadClass(DatastoreSelectionDialog.class.getName());
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
    if (datastoreAccessorService == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error("No datastore accessor service found.");
      return;
    }
    Shell shell = _navigator.getSite().getShell();
    final DatastoreSelectionDialog datastoreDialog = DatastoreSelectionDialog.createInputDialog(shell);
    datastoreDialog.create();
    datastoreDialog.getShell().pack();
    Point size = datastoreDialog.getShell().computeSize(500, 400);
    datastoreDialog.getShell().setSize(size);

    // We are already on the SWT thread, but we ensure
    // this way that the setActive() will be called after the open().
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        datastoreDialog.getShell().setActive();
      }
    });
    datastoreDialog.open();
  }
}
