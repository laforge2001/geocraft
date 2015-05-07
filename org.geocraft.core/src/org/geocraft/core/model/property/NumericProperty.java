/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


public abstract class NumericProperty extends Property {

  public NumericProperty(String key) {
    super(key);
  }

  @Override
  public String pickle() {
    return getValueObject().toString();
  }

  @Override
  public void unpickle(String value) {
    setValueObject(value);
  }

}
