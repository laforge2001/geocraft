/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


/**
 * A property for containing an array of string values.
 */
public class StringArrayProperty extends ObjectArrayProperty<String> {

  /**
   * Constructs a string property.
   * 
   * @param key the property key.
   */
  public StringArrayProperty(String key, Class<String> klass) {
    super(key, klass);
  }

  public boolean contains(String value) {
    if (get() != null) {
      for (String val : get()) {
        if (val.equals(value)) {
          return true;
        }
      }
    }
    return false;
  }

  public void add(String value) {
    if (get() == null) {
      set(new String[] { value });
      return;
    }
    int oldLength = get().length;
    String[] values = new String[oldLength + 1];
    System.arraycopy(get(), 0, values, 0, oldLength);
    values[oldLength] = value;
    set(values);
  }

  @Override
  public String pickle() {
    String[] values = get();
    if (values == null || values.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      builder.append(values[i]);
      if (i < values.length - 1) {
        builder.append(",");
      }
    }
    return builder.toString();
  }

  @Override
  public void unpickle(String pickledValue) {
    if (pickledValue == null || pickledValue.isEmpty()) {
      set(new String[0]);
      return;
    }
    String[] values = pickledValue.split(",");
    set(values);
  }
}
