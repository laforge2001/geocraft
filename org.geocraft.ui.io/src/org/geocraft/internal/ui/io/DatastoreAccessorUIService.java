/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.io;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.IDatastoreAccessorUIService;


/**
 * Support for accessing the user interface for input and output of registered entities. 
 */
public class DatastoreAccessorUIService implements IDatastoreAccessorUIService {

  public DatastoreAccessorUIService() {
    ServiceProvider.getLoggingService().getLogger(getClass()).debug("Datastore accessor UI service started.");
  }

  public AbstractModelView getModelView(final IDatastoreAccessor datastoreAccessor, final IOMode ioMode) {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] extensions = registry.getConfigurationElementsFor("org.geocraft.ui.io.datastoreAccessorUI");
    for (IConfigurationElement extension : extensions) {
      String datastoreAccessorId = extension.getAttribute("datastoreAccessorId");
      String id = datastoreAccessor.getId();
      if (datastoreAccessorId.equals(id)) {
        try {
          if (ioMode.equals(IOMode.INPUT)) {
            return (AbstractModelView) extension.createExecutableExtension("inputModelView");
          } else if (ioMode.equals(IOMode.OUTPUT)) {
            return (AbstractModelView) extension.createExecutableExtension("outputModelView");
          }
        } catch (CoreException ex) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
        }
        throw new IllegalArgumentException("Unrecognized I/O mode: " + ioMode);
      }
    }
    return null;
  }
}
