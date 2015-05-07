/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.util.Map;
import java.util.Map.Entry;

import org.geocraft.core.model.Entity;
import org.geocraft.core.model.specification.EntityUniqueIdSpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;


public class EntityProperty<T extends Entity> extends ObjectProperty<T> {

  public EntityProperty(final String key, final Class<T> klazz) {
    super(key, klazz);
  }

  public EntityProperty(final String key, final Class<T> klazz, final T value) {
    super(key, klazz);
    set(value);
  }

  @Override
  public void setValueObject(final Object valueObject) {
    // Value object can be null for optional parameters such as AOI
    if (valueObject != null && getKlazz().isAssignableFrom(valueObject.getClass())) {
      set(getKlazz().cast(valueObject));
    } else if (valueObject == null) {
      T value = null;
      set(value);
    }
  }

  @Override
  public String pickle() {
    T entity = get();
    if (entity == null) {
      return "null";
    }
    return entity.getUniqueID();
  }

  @Override
  public void unpickle(final String uniqueId) {
    if (uniqueId == null || uniqueId.equalsIgnoreCase("null")) {
      return;
    }
    Map<String, Object> results = ServiceProvider.getRepository().get(new EntityUniqueIdSpecification(uniqueId));
    switch (results.size()) {
      case 0:
        // No results found.
        break;
      case 1:
        for (Entry entry : results.entrySet()) {
          set(getKlazz().cast(entry.getValue()));
        }
        break;
      default:
        throw new IllegalArgumentException("Error restoring " + getKey()
            + "...multiple entities found with unique ID: " + uniqueId);
    }
  }

  public void autoDefault() {
    Map<String, Object> results = ServiceProvider.getRepository().get(new TypeSpecification(getKlazz()));
    if (results.size() == 1) {
      setValueObject(results.values().toArray()[0]);
    }
  }
}
