/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.mapper.InMemoryMapperModel;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.ObjectProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.model.validation.Validation;
import org.geocraft.core.service.ServiceProvider;


public class DatastoreProperty extends ObjectProperty<DatastorePropertyData> {

  private Class _entityClass;

  public DatastoreProperty(final String key, Class entityClass) {
    super(key, DatastorePropertyData.class);
    _entityClass = entityClass;
  }

  public DatastoreProperty(final String key, Class entityClass, final DatastorePropertyData value) {
    super(key, DatastorePropertyData.class);
    _entityClass = entityClass;
    set(value);
  }

  @Override
  public void setValueObject(final Object valueObject) {
    if (valueObject instanceof DatastorePropertyData) {
      set((DatastorePropertyData) valueObject);
    } else {
      DatastorePropertyData value = null;
      IDatastoreAccessor accessor = null;
      if (valueObject != null) {
        accessor = findDatastoreAccessor(valueObject.toString());
        if (accessor == null) {
          value = new DatastorePropertyData(accessor, new InMemoryMapperModel(), new InMemoryMapper(_entityClass));
        } else {
          value = new DatastorePropertyData(accessor);
        }
      }
      set(value);
    }
  }

  @Override
  public String pickle() {
    DatastorePropertyData value = get();
    IDatastoreAccessor accessor = value._accessor;
    if (accessor == null) {
      return "null";
    }
    return accessor.toString();
  }

  @Override
  public void unpickle(final String uniqueId) {
    if (uniqueId == null || uniqueId.equalsIgnoreCase("null")) {
      return;
    }
    IDatastoreAccessor accessor = findDatastoreAccessor(uniqueId);
    if (accessor == null) {
      set(new DatastorePropertyData(accessor, new InMemoryMapperModel(), new InMemoryMapper(_entityClass)));
    } else {
      set(new DatastorePropertyData(accessor));
    }
  }

  private IDatastoreAccessor findDatastoreAccessor(String uniqueId) {
    IDatastoreAccessorService service = ServiceProvider.getDatastoreAccessorService();
    IDatastoreAccessor[] accessors = service.getDatastoreAccessors(new Class[] { _entityClass }, IOMode.OUTPUT);
    for (IDatastoreAccessor accessor : accessors) {
      if (accessor.getName().startsWith(uniqueId)) {
        return accessor;
      }
    }
    return null;
  }

  public void validate(IValidation results) {
    Validation v = new Validation();
    DatastorePropertyData data = get();
    if (data == null) {
      v.error(this, "No datastore selected");
      return;
    }
    if (data._accessor != null && data._model != null) {
      data._model.validate(v);
    }
    int maxSeverity = v.getMaxSeverity();
    switch (maxSeverity) {
      case IStatus.ERROR:
        results.error(this, v.getStatusMessages(maxSeverity));
        break;
      case IStatus.WARNING:
        results.warning(this, v.getStatusMessages(maxSeverity));
        break;
      case IStatus.INFO:
        results.info(this, v.getStatusMessages(maxSeverity));
        break;
    }
  }

  public void setDatastore(String datastoreName) {
    System.out.println("Setting datastore: " + datastoreName);
    setValueObject(datastoreName);
  }

  public void updateFromPrototype(Entity prototype) {
    setDatastore(prototype.getDatastore());
    DatastorePropertyData data = get();
    if (data == null) {
      return;
    }
    if (data._accessor != null && data._model != null && data._mapper != null) {
      MapperModel model = data._accessor.mapEntitiesToModels(new Entity[] { prototype }).values().iterator().next();
      data._model = model;
      data._mapper = data._accessor.createMapper(IOMode.OUTPUT, data._model);
      System.out.println("updating data: " + data + " model: " + data._model);
      firePropertyChange(getKey(), null, data);
    }
  }
}
