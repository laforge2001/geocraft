/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.lang.reflect.Array;
import java.util.EnumSet;


public class EnumArrayProperty<T extends Enum> extends ObjectArrayProperty<T> {

  public EnumArrayProperty(String key, Class<T> klass) {
    super(key, klass);
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
  public String pickle() {
    T[] values = get();
    if (values == null || values.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      builder.append(values[i].toString());
      if (i < values.length - 1) {
        builder.append(",");
      }
    }
    return builder.toString();
    //    ArrayConverter arrayConverter = new ArrayConverter(String[].class, new StringConverter());
    //    arrayConverter.setOnlyFirstToString(false);
    //    T[] valueObject = get();
    //    String valueString = (String) arrayConverter.convert(String.class, valueObject);
    //    return valueString;
  }

  @Override
  public void unpickle(String pickledValue) {
    if (pickledValue == null || pickledValue.isEmpty()) {
      T[] values = (T[]) Array.newInstance(getKlazz(), 0);
      set(values);
      return;
    }
    String[] substrings = pickledValue.split(",");
    T[] values = (T[]) Array.newInstance(getKlazz(), substrings.length);
    for (int i = 0; i < values.length; ++i) {
      values[i] = (T) restoreEnum(substrings[i], getKlazz());
    }
    set(values);

    //    String[] names = fromString(pickledValues);
    //    T[] values = (T[]) Array.newInstance(getKlazz(), names.length);
    //    for (int i = 0; i < values.length; ++i) {
    //      values[i] = (T) restoreEnum(names[i], getKlazz());
    //    }
    //    set(values);
  }

}
