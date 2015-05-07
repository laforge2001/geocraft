/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.repository.action;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreEntrySelector;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.ui.io.DatastoreEntitySelectionDialog;
import org.geocraft.ui.model.IModelSharedImages;
import org.geocraft.ui.model.ModelUI;


/**
 * Defines an action to initiate the loading of an entity from a datastore.
 * This is currently used as a context-sensitive addition to the popup menu
 * in the repository view. In other words, if the "Grids" folder is currently
 * selected, then <code>LoadFromDatastore</code> actions would be added to
 * the popup menu for every datastore that supports I/O of grid entities.
 */
public class LoadFromDatastore extends Action {

  /** The datastore from which to load an entity. */
  private IDatastoreAccessor _datastoreAccessor;

  public LoadFromDatastore(IDatastoreAccessor datastoreAccessor) {
    super(datastoreAccessor.getName() + "...");
    _datastoreAccessor = datastoreAccessor;
    String entityName = _datastoreAccessor.getSupportedEntityClassNames()[0];
    IModelSharedImages modelImages = ModelUI.getSharedImages();
    setImageDescriptor(modelImages.getImageDescriptor(entityName));
  }

  @Override
  public void run() {
    if (_datastoreAccessor == null) {
      return;
    }

    // Initialize the datastore access.
    IStatus status = _datastoreAccessor.initialize();
    if (status.getSeverity() == IStatus.ERROR) {
      Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      MessageDialog.openError(shell, "Connection Error: " + _datastoreAccessor.getName(), status.getMessage());
      return;
    } else if (status.getSeverity() == IStatus.CANCEL) {
      return;
    }

    // Create the selection dialog for parameterizing the loading of entities.
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    DatastoreEntitySelectionDialog selectionDialog = new DatastoreEntitySelectionDialog(shell, IOMode.INPUT,
        new Entity[0], _datastoreAccessor);
    selectionDialog.create();
    selectionDialog.getShell().setText("Datastore Entity Selection");
    selectionDialog.getShell().setSize(1000, 500);

    // Create the selector for choosing an entry from the datastore.
    IDatastoreEntrySelector selector = _datastoreAccessor.createInputSelector();
    selector.select(selectionDialog.getDatastoreEntryContainer());

    selectionDialog.open();
  }
}
