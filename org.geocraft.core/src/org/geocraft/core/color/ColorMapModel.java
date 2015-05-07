/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.map.IColorMap;


/**
 * The basic model of a color map.
 */
public class ColorMapModel {

  /** The array of RGBs in the color map model. */
  protected RGB[] _colors;

  /** The color map listeners. */
  protected List<ColorMapListener> _listeners = Collections.synchronizedList(new ArrayList<ColorMapListener>());

  /**
   * Constructs a color map model, copied from another color map model.
   */
  public ColorMapModel(final ColorMapModel model) {
    // Copy the RGB array, so that the model has its own copy.
    _colors = model.getColors();
  }

  /**
   * Construct a color map model.
   * 
   * @param colors
   *          the array of colors to use.
   */
  public ColorMapModel(final RGB[] colors) {
    // Copy the RGB array, so that the model has its own copy.
    _colors = new RGB[colors.length];
    for (int i = 0; i < colors.length; i++) {
      _colors[i] = colors[i];
    }
  }

  /**
   * Constructs a color map model, from one of the pre-defined color maps.
   */
  public ColorMapModel(final int numColors, final IColorMap colormap) {
    // Create the colors, so that the model has its own copy.
    _colors = colormap.getRGBs(numColors);
  }

  /**
   * Reverses the order of the colors in the color array.
   */
  public void reverseColors() {
    RGB[] colors = new RGB[_colors.length];
    for (int i = 0; i < colors.length; i++) {
      colors[i] = _colors[_colors.length - i - 1];
    }
    _colors = colors;
    updated();
  }

  /**
   * Gets the array of colors in the color map model. A copy of the array is
   * returned.
   * 
   * @return a copy of the array of colors.
   */
  public RGB[] getColors() {
    RGB[] colors = new RGB[_colors.length];
    for (int i = 0; i < _colors.length; i++) {
      colors[i] = _colors[i];
    }
    return colors;
  }

  /**
   * Gets the number of colors in the color map model.
   * 
   * @return the number of colors in the color map model.
   */
  public int getNumColors() {
    return _colors.length;
  }

  /**
   * Gets the color at the specified index. This color is managed by the model
   * and should not be disposed.
   * 
   * @param index
   *          the color index.
   * @return a copy of the color.
   */
  public RGB getColor(final int index) {
    if (index < 0 || index >= _colors.length) {
      throw new IllegalArgumentException("Color index out of range: " + index);
    }
    return _colors[index];
  }

  /**
   * Sets the color at the specified index. This color will be clones, so that
   * the model has its own copy.
   * 
   * @param index
   *          the color index.
   * @param color
   *          the color to set.
   */
  public void setColor(final int index, final RGB color) {
    setColor(index, color, true);
  }

  /**
   * Sets the color at the specified index. This color will be cloned, so that
   * the model has its own copy.
   * 
   * @param index
   *          the color index.
   * @param color
   *          the color to set.
   * @param update
   *          true to auto-update; otherwise false.
   */
  public void setColor(final int index, final RGB color, final boolean update) {
    if (index < 0 || index >= _colors.length) {
      throw new IllegalArgumentException("Color index out of range " + index);
    }
    _colors[index] = color;
    if (update) {
      updated();
    }
  }

  /**
   * Sets the array of colors in the color map model. These colors will be
   * cloned, so that the model has its own copy.
   * 
   * @param colors
   *          the array of colors to set.
   */
  public void setColors(final RGB[] colors) {
    _colors = new RGB[colors.length];
    for (int i = 0; i < colors.length; i++) {
      _colors[i] = colors[i];
    }
    updated();
  }

  /**
   * Invoked when the color map model is updated.
   */
  public void updated() {
    ColorMapEvent event = new ColorMapEvent(new ColorMapModel(this));
    fireColorMapChangedEvent(event);
  }

  /**
   * Adds a color map listener to the color map model.
   * 
   * @param listener
   *          the listener to add.
   */
  public void addColorMapListener(final ColorMapListener listener) {
    _listeners.add(listener);
  }

  /**
   * Removes a color map listener from the color bar.
   * 
   * @param listener
   *          the color map listener to remove.
   */
  public void removeColorMapListener(final ColorMapListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Fires a color map event to the listeners.
   * 
   * @param event
   *          the color map event to send to the listeners.
   */
  public void fireColorMapChangedEvent(final ColorMapEvent event) {
    ColorMapListener[] listeners = new ColorMapListener[_listeners.size()];
    for (int i = 0; i < listeners.length; i++) {
      listeners[i] = _listeners.get(i);
    }
    for (ColorMapListener listener : listeners) {
      listener.colorsChanged(event);
    }
  }

  public boolean isSame(final ColorMapModel other) {

    if (getNumColors() != other.getNumColors()) {
      return false;
    }

    for (int i = 0; i < getNumColors(); i++) {
      if (getColor(i) != other.getColor(i)) {
        return false;
      }
    }
    return true;
  }

  public void dispose() {
    _listeners.clear();
  }
}
