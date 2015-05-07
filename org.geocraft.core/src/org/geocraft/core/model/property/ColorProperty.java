/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import org.eclipse.swt.graphics.RGB;


/**
 * A property for containing a color value.
 * The color value is an <code>RGB</code> object.
 */
public class ColorProperty extends Property {

  /** The color value. */
  private RGB _value;

  /**
   * Constructs a color property with an initial value.
   * 
   * @param key the property key.
   * @param value the initial property value.
   */
  public ColorProperty(String key, RGB value) {
    super(key);
    _value = value;
  }

  /**
   * Gets the color value.
   * 
   * @return the color value.
   */
  public RGB get() {
    return _value;
  }

  /**
   * Sets the color value.
   * 
   * @param value the color value to set.
   */
  public void set(RGB value) {
    firePropertyChange(_value, _value = value);
  }

  @Override
  public Object getValueObject() {
    return _value;
  }

  @Override
  public void setValueObject(Object value) {
    if (value != null && value instanceof RGB) {
      set((RGB) value);
      return;
    }
  }

  public boolean isNull() {
    return _value == null;
  }

  @Override
  public String toString() {
    return "" + get();
  }

  @Override
  public String pickle() {
    if (_value == null) {
      return "";
    }
    return _value.red + "," + _value.green + "," + _value.blue;
  }

  @Override
  public void unpickle(String value) {
    String[] rgbs = value.split(",");
    // Restore the RGB if the pickled value contains 3 substrings (red,green,blue).
    if (rgbs.length == 3) {
      int red = Integer.parseInt(rgbs[0]);
      int green = Integer.parseInt(rgbs[1]);
      int blue = Integer.parseInt(rgbs[2]);
      set(new RGB(red, green, blue));
    }
  }
}
