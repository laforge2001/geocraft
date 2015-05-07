/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.util.EnumSet;


public class EnumProperty<T extends Enum> extends ObjectProperty<T> {

  public EnumProperty(String key, Class<T> klass) {
    super(key, klass);
  }

  public EnumProperty(String key, Class<T> klass, T value) {
    super(key, klass);
    set(value);
  }

  private static Enum restoreEnum(final String name, final Class klazz) {
    for (Object enumType : EnumSet.allOf(klazz)) {
      if (enumType.toString().equals(name)) {
        return (Enum) enumType;
      }
    }
    return null;
  }

  @Override
  public void setValueObject(Object value) {
    if (value.getClass().isAssignableFrom(getKlazz())) {
      set(getKlazz().cast(value/*restoreEnum(value.toString(), getKlazz())*/));
    }
  }

  @Override
  public String toString() {
    return "" + get();
  }

  @Override
  public void unpickle(String pickledValue) {
    set(getKlazz().cast(restoreEnum(pickledValue, getKlazz())));
  }
}
