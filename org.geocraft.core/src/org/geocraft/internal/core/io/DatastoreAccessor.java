/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.core.io;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.io.DatastoreEntrySelector;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorUtil;
import org.geocraft.core.io.IDatastoreLocationSelector;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.service.ServiceProvider;


/**
 * The common implementation of a datastore accessor.
 */
public class DatastoreAccessor implements IDatastoreAccessor {

  /** The configuration element for the datastore access. */
  private final IConfigurationElement _configElement;

  /**
   * The default constructor.
   * @param configurationElement the configuration element for the datastore access.
   */
  public DatastoreAccessor(final IConfigurationElement configurationElement) {
    _configElement = configurationElement;
  }

  public String getId() {
    return getName();
  }

  public String getName() {
    return _configElement.getAttribute("name");
  }

  public String getCategory() {
    return _configElement.getAttribute("category");
  }

  public boolean isVisible() {
    String temp = _configElement.getAttribute("visible");
    if (temp == null) {
      return true;
    }
    return Boolean.parseBoolean(temp);
  }

  public String[] getMapperClassNames() {
    List<String> mapperClassNames = new ArrayList<String>();
    IConfigurationElement[] configurationElements = _configElement.getChildren("mapper");
    for (IConfigurationElement configurationElement : configurationElements) {
      String mapperClassName = configurationElement.getAttribute("class");
      mapperClassNames.add(mapperClassName);
    }
    return mapperClassNames.toArray(new String[0]);
  }

  public String[] getSupportedEntityClassNames() {
    Set<String> entityClasses = new HashSet<String>();
    IConfigurationElement[] mappers = _configElement.getChildren("mapper");
    for (IConfigurationElement mapper : mappers) {
      String supportedEntity = mapper.getAttribute("supportedEntityClassName");
      entityClasses.add(supportedEntity);
    }
    return entityClasses.toArray(new String[0]);
  }

  public IStatus initialize() {
    return getAccessorUtil().initialize();
  }

  public Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities) {
    return getAccessorUtil().mapEntitiesToModels(entities);
  }

  public IDatastoreAccessorUtil getAccessorUtil() {
    try {
      return (IDatastoreAccessorUtil) _configElement.createExecutableExtension("utilClass");
    } catch (CoreException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
    }
    return null;
  }

  /***********************************************************************************/

  public boolean canInput() {
    return _configElement.getChildren("input").length == 1;
  }

  public boolean canOutput() {
    return _configElement.getChildren("output").length == 1;
  }

  public DatastoreEntrySelector createInputSelector() {
    IConfigurationElement ioElement = getInputElement();
    try {
      return (DatastoreEntrySelector) ioElement.createExecutableExtension("selector");
    } catch (CoreException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
      return null;
    }
  }

  public IDatastoreLocationSelector createOutputSelector() {
    IConfigurationElement ioElement = getOutputElement();
    if (ioElement.getAttribute("selector") == null) {
      return null;
    }
    try {
      return (IDatastoreLocationSelector) ioElement.createExecutableExtension("selector");
    } catch (CoreException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
      return null;
    }
  }

  public String getMapperModelClassName(final IOMode ioMode) {
    if (ioMode.equals(IOMode.INPUT)) {
      if (canInput()) {
        IConfigurationElement ioElement = getIOElement(ioMode);
        String modelName = ioElement.getAttribute("mapperModel");
        return modelName;
      }
    } else if (ioMode.equals(IOMode.OUTPUT)) {
      if (canOutput()) {
        IConfigurationElement ioElement = getIOElement(ioMode);
        String modelName = ioElement.getAttribute("mapperModel");
        return modelName;
      }
    }
    // return null if the user cannot input this data store
    return null;
  }

  public IMapper createMapper(final IOMode ioMode, MapperModel model) {
    try {
      IMapper mapper = (IMapper) _configElement.createExecutableExtension("mapper");
      return mapper.factory(model);
    } catch (CoreException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
      return null;
    }
  }

  public MapperModel createMapperModel(final IOMode ioMode) {
    IConfigurationElement ioElement = getIOElement(ioMode);
    try {
      MapperModel model = (MapperModel) ioElement.createExecutableExtension("mapperModel");
      return model;
    } catch (CoreException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
      return null;
    }
  }

  public ImportTask createImportTask() {
    IConfigurationElement ioElement = getIOElement(IOMode.INPUT);
    try {
      return (ImportTask) ioElement.createExecutableExtension("task");
    } catch (CoreException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
      return null;
    }
  }

  public ExportTask createExportTask() {
    IConfigurationElement ioElement = getIOElement(IOMode.OUTPUT);
    try {
      return (ExportTask) ioElement.createExecutableExtension("task");
    } catch (CoreException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
      return null;
    }
  }

  /**
   * Returns the internal OSGI configuration element for the specified I/O mode.
   * 
   * @param ioMode the I/O mode (Input or Output).
   * @return the configuration element.
   */
  private IConfigurationElement getIOElement(final IOMode ioMode) {
    if (ioMode.equals(IOMode.INPUT)) {
      return getInputElement();
    } else if (ioMode.equals(IOMode.OUTPUT)) {
      return getOutputElement();
    }
    throw new RuntimeException("Invalid I/O mode.");
  }

  /**
   * Returns the internal OSGI configuration element for Input mode.
   *
   * @return the configuration element for Input mode.
   */
  private IConfigurationElement getInputElement() {
    if (canInput()) {
      IConfigurationElement[] inputs = _configElement.getChildren("input");
      if (inputs.length == 0) {
        throw new RuntimeException("The " + getName() + " datastore is not supported for input.");
      } else if (inputs.length > 1) {
        throw new RuntimeException("The " + getName() + " datastore has multiple input definitions.");
      }
      return inputs[0];
    }
    throw new RuntimeException("This " + getName() + " datastore is not supported for intput.");
  }

  /**
   * Returns the internal OSGI configuration element for Output mode.
   *
   * @return the configuration element for Output mode.
   */
  private IConfigurationElement getOutputElement() {
    if (canOutput()) {
      IConfigurationElement[] outputs = _configElement.getChildren("output");
      if (outputs.length == 0) {
        throw new RuntimeException("The " + getName() + " datastore is not supported for output.");
      } else if (outputs.length > 1) {
        throw new RuntimeException("The " + getName() + " datastore has multiple output definitions.");
      }
      return outputs[0];
    }
    throw new RuntimeException("The " + getName() + " datastore is not supported for output.");
  }

  @Override
  public String toString() {
    return getName();
  }
}
