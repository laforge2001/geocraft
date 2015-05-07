/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color;


import java.util.EventListener;


/**
 * The listener interface for receiving color map model events.
 */
public interface ColorMapListener extends EventListener {

  /**
   * Invoked when the color map model is changed.
   * @param event the color map event.
   */
  void colorsChanged(ColorMapEvent event);
}
