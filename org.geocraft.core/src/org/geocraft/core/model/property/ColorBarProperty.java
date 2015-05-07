/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;


/**
 * A property for containing a color value. The color value is an
 * <code>ColorBar</code> object.
 */
public class ColorBarProperty extends Property {

  /** The color bar. */
  private ColorBar _value;

  public ColorBarProperty(final String key, final ColorBar value) {
    super(key);
    _value = value;
  }

  public ColorBar get() {
    return _value;
  }

  public void set(final ColorBar value) {
    // Create a copy of the color bar, so avoid listener problems.
    firePropertyChange(_value, _value = new ColorBar(value));
  }

  @Override
  public Object getValueObject() {
    return _value;
  }

  @Override
  public void setValueObject(final Object valueObject) {
    if (valueObject != null && valueObject instanceof ColorBar) {
      set((ColorBar) valueObject);
      return;
    }
  }

  @Override
  public String pickle() {
    if (_value == null) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    int numColors = _value.getNumColors();
    builder.append("" + numColors);
    for (int i = 0; i < numColors; i++) {
      RGB color = _value.getColor(i);
      builder.append("," + color.red + "," + color.green + "," + color.blue);
    }
    builder.append("," + _value.getStartValue() + "," + _value.getEndValue() + "," + _value.getStepValue() + ","
        + _value.isReversedRange());
    return builder.toString();
  }

  @Override
  public void unpickle(final String value) {
    String[] substrings = value.split(",");
    if (substrings.length >= 4) {
      int numColors = Integer.parseInt(substrings[0]);
      RGB[] colors = new RGB[numColors];
      if (substrings.length == (numColors * 3) + 5) {
        for (int i = 0; i < numColors; i++) {
          int red = Integer.parseInt(substrings[i * 3 + 1]);
          int green = Integer.parseInt(substrings[i * 3 + 2]);
          int blue = Integer.parseInt(substrings[i * 3 + 3]);
          colors[i] = new RGB(red, green, blue);
        }
      } else {
        for (int i = 0; i < numColors; i++) {
          colors[i] = new RGB(0, 0, 0);
        }
      }
      double startValue = Double.parseDouble(substrings[numColors * 3 + 1]);
      double endValue = Double.parseDouble(substrings[numColors * 3 + 2]);
      double stepValue = Double.parseDouble(substrings[numColors * 3 + 3]);
      boolean reversed = Boolean.parseBoolean(substrings[numColors * 3 + 4]);
      ColorBar colorBar = new ColorBar(colors, startValue, endValue, stepValue);
      colorBar.setReversedRange(reversed);
      set(colorBar);
    }
  }
}
