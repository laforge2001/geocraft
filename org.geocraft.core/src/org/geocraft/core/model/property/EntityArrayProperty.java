/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geocraft.core.model.Entity;
import org.geocraft.core.model.specification.EntityUniqueIdSpecification;
import org.geocraft.core.service.ServiceProvider;


public class EntityArrayProperty<T extends Entity> extends ObjectArrayProperty<T> {

  public EntityArrayProperty(final String key, final Class<T> klass) {
    super(key, klass);
  }

  @Override
  public void setValueObject(final Object valueObject) {
    if (valueObject == null) {
      T[] array = (T[]) Array.newInstance(getKlazz(), 0);
      set(array);
      return;
    }

    if (valueObject.getClass().isArray()) {
      Object[] uniqueIdArray = (Object[]) valueObject;
      List<T> list = new ArrayList<T>();
      for (Object uniqueId : uniqueIdArray) {
        String name = getKlazz().cast(uniqueId).getUniqueID();
        Map<String, Object> x = ServiceProvider.getRepository().get(new EntityUniqueIdSpecification(name));
        for (Entry e : x.entrySet()) {
          try {
            list.add(getKlazz().cast(e.getValue()));
          } catch (ClassCastException ex) {
            //This can be expected for Wells and Well Bores until Well model cleaned up
          }
        }
      }

      set(list.toArray((T[]) Array.newInstance(getKlazz(), 0)));
    } else {
      T[] array = (T[]) Array.newInstance(getKlazz(), 1);
      array[0] = (T) valueObject;
      set(array);
    }

  }

  @Override
  public String pickle() {
    T[] values = get();
    if (values == null || values.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      builder.append(values[i].getUniqueID());
      if (i < values.length - 1) {
        builder.append(",");
      }
    }
    return builder.toString();
  }

  @Override
  public void unpickle(final String pickledValue) {
    // If the pickled value is null or empty, allocate and set a zero-length entity array.
    if (pickledValue == null || pickledValue.isEmpty()) {
      set((T[]) Array.newInstance(getKlazz(), 0));
      return;
    }

    // Otherwise, split the pickled value into an array of unique IDs.
    String[] uniqueIds = pickledValue.split(",");
    List<T> list = new ArrayList<T>();

    // Lookup each of the unique IDs from the repository.
    for (String uniqueId : uniqueIds) {
      Map<String, Object> x = ServiceProvider.getRepository().get(new EntityUniqueIdSpecification(uniqueId));
      for (Entry e : x.entrySet()) {
        list.add(getKlazz().cast(e.getValue()));
      }
    }
    set(list.toArray((T[]) Array.newInstance(getKlazz(), 0)));
  }
}
