/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.core.io;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.common.util.Sorting;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.service.ServiceProvider;


/**
 * This class defines a simple implementation of the datastore
 * accessor service interface. The datastore accessor services
 * provides a method to get all the datastore accessors currently
 * registered, and it also acts as a factory to create mappers
 * for entities.
 */
public class DatastoreAccessorService implements IDatastoreAccessorService {

  /**
   * The default no-argument constructor.
   */
  public DatastoreAccessorService() {
    ServiceProvider.getLoggingService().getLogger(getClass()).debug("Datastore accessor service started.");
  }

  public IDatastoreAccessor[] getDatastoreAccessors() {
    List<String> datastoreNames = new ArrayList<String>();
    Map<String, IDatastoreAccessor> datastoreAccessorMap = new HashMap<String, IDatastoreAccessor>();
    List<IDatastoreAccessor> datastoreAccessorList = new ArrayList<IDatastoreAccessor>();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] configElements = registry
        .getConfigurationElementsFor("org.geocraft.core.io.datastoreAccessor");
    for (IConfigurationElement configElement : configElements) {
      IDatastoreAccessor datastoreAccessor = new DatastoreAccessor(configElement);
      if (datastoreAccessor.isVisible()) {
        datastoreAccessorMap.put(datastoreAccessor.getName(), datastoreAccessor);
        datastoreNames.add(datastoreAccessor.getName());
      }
    }
    Collections.sort(datastoreNames, Sorting.ALPHANUMERIC_COMPARATOR);
    for (String datastoreName : datastoreNames) {
      datastoreAccessorList.add(datastoreAccessorMap.get(datastoreName));
    }
    return datastoreAccessorList.toArray(new IDatastoreAccessor[0]);
  }

  public IDatastoreAccessor[] getDatastoreAccessors(Class[] klasses, IOMode ioMode) {
    List<String> datastoreNames = new ArrayList<String>();
    Map<String, IDatastoreAccessor> datastoreAccessorMap = new HashMap<String, IDatastoreAccessor>();
    List<IDatastoreAccessor> datastoreAccessorList = new ArrayList<IDatastoreAccessor>();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] configElements = registry
        .getConfigurationElementsFor("org.geocraft.core.io.datastoreAccessor");
    for (IConfigurationElement configElement : configElements) {
      boolean flag = false;
      IDatastoreAccessor datastoreAccessor = new DatastoreAccessor(configElement);
      if (datastoreAccessor.isVisible()) {
        if (ioMode.equals(IOMode.INPUT)) {
          if (!datastoreAccessor.canInput()) {
            continue;
          }
        } else if (ioMode.equals(IOMode.OUTPUT)) {
          if (!datastoreAccessor.canOutput()) {
            continue;
          }
        }
        String[] classNames = datastoreAccessor.getSupportedEntityClassNames();
        for (String className : classNames) {
          for (Class klass : klasses) {
            if (klass.getSimpleName().equals(className)) {
              flag = true;
              break;
            }
          }
        }
        if (flag) {
          datastoreAccessorMap.put(datastoreAccessor.getName(), datastoreAccessor);
          datastoreNames.add(datastoreAccessor.getName());
        }
      }
    }
    Collections.sort(datastoreNames, Sorting.ALPHANUMERIC_COMPARATOR);
    for (String datastoreName : datastoreNames) {
      datastoreAccessorList.add(datastoreAccessorMap.get(datastoreName));
    }
    return datastoreAccessorList.toArray(new IDatastoreAccessor[0]);
  }

  public IMapper createMapper(final IMapper mapper, final String name) {
    return createMapper(mapper, mapper.getModel(), name);
  }

  public synchronized void restoreEntityFromMapperModel(final MapperModel model) {
    for (IDatastoreAccessor dataAccessor : getDatastoreAccessors()) {
      if (model.getClass().getName().equals(dataAccessor.getMapperModelClassName(IOMode.INPUT))) {
        ImportTask task = dataAccessor.createImportTask();
        task.setMapperModel(model);
        TaskRunner.runTask(task, "Restoring " + model.getUniqueId(), TaskRunner.JOIN);
      }
    }
  }

  public IMapper createMapper(final IMapper mapper, final MapperModel model, final String name) {
    model.updateUniqueId(name);
    return mapper.factory(model);
  }

  public MapperModel createMapperModelFromClassName(final String modelClassName) {
    // Loop thru all the datastore accessors currently registered.
    IDatastoreAccessor[] datastoreAccessors = getDatastoreAccessors();
    for (IDatastoreAccessor datastoreAccessor : datastoreAccessors) {
      // Loop thru the mapper class names for each datastore accessor.
      String className = datastoreAccessor.getMapperModelClassName(IOMode.INPUT);
      // If a mapper class name matches the input mapper class name,
      // then create and return a new instance of the mapper.
      if (className != null && modelClassName.equals(className)) {
        return datastoreAccessor.createMapperModel(IOMode.INPUT);
      }
    }
    return null;
  }

}
