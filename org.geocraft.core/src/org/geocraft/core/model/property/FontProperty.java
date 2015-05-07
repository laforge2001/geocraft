/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import org.eclipse.swt.graphics.FontData;


public class FontProperty extends ObjectArrayProperty<FontData> {

  public FontProperty(String key, FontData[] value) {
    super(key, FontData.class, value);
  }

  @Override
  public void setValueObject(Object value) {
    if (value != null && value instanceof FontData[]) {
      set((FontData[]) value);
      return;
    }
  }

  @Override
  public String toString() {
    return "" + get();
  }

  @Override
  public String pickle() {
    // TODO: Implement the pickle logic.
    return "";
  }

  @Override
  public void unpickle(String value) {
    // TODO: Implement the unpickle logic.
    //  String[] valueArray = fromString(values);
    //    FontData[] value = (FontData[]) Array.newInstance(getKlazz(), valueArray.length);
    //    for (int i = 0; i < valueArray.length; ++i) {
    //      value[i] = new FontData(valueArray[i]);
    //    }
    //    set(value);
  }
}
