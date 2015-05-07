/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color;


/**
 * The color map event class.
 * Passed to listeners when a color map is updated.
 */
public class ColorMapEvent {

  /** The color map model. */
  protected ColorMapModel _colorMapModel;

  /**
   * Constructs a color map event.
   * @param colorMapModel the color map model.
   */
  public ColorMapEvent(ColorMapModel colorMapModel) {
    _colorMapModel = colorMapModel;
  }

  /**
   * Gets the color map model of the event.
   * @return the color map model of the event.
   */
  public ColorMapModel getColorMapModel() {
    return _colorMapModel;
  }

  /**
   * Returns the string representation of the color map event.
   * @return the string representation of the color map event.
   */
  public String toString() {
    return "ColorMapEvent: # colors:" + _colorMapModel.getNumColors() + " first color: " + _colorMapModel.getColor(0)
        + " last color: " + _colorMapModel.getColor(_colorMapModel.getNumColors() - 1);

  }

}
