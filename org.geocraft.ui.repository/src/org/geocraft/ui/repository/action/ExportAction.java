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
import org.geocraft.core.model.Entity;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.io.DatastoreSelectionDialog;


public class ExportAction extends Action {

  /** The repository navigator. */
  private final CommonNavigator _navigator;

  /** The entities to export. */
  private final Entity[] _entities;

  /**
   * The default constructor.
   * 
   * @param navigator the repository navigator.
   */
  public ExportAction(final CommonNavigator navigator, final Entity entity) {
    this(navigator, new Entity[] { entity });
  }

  /**
   * The default constructor.
   * 
   * @param navigator the repository navigator.
   */
  public ExportAction(final CommonNavigator navigator, final Entity[] entities) {
    setText("Export...");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_EXPORT));
    _navigator = navigator;
    _entities = entities;
  }

  @Override
  public void run() {
    IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
    if (datastoreAccessorService == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error("No datastore accessor service found.");
      return;
    }

    Display display = Display.getDefault();
    Shell shell = new Shell(display);
    Point location = _navigator.getSite().getShell().getLocation();
    shell.setLocation(location);

    final DatastoreSelectionDialog datastoreDialog = DatastoreSelectionDialog.createOutputDialog(shell, _entities);
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
