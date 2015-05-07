/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Composite;
import org.geocraft.ui.plot.listener.ICanvasListener;


/**
 * The interface for a plot canvas.
 */
public interface ICanvas extends PropertyChangeListener {

  /**
   * Gets the plot canvas component.
   * 
   * @return the plot canvas component.
   */
  Composite getComposite();

  /**
   * Completely redraw the plot canvas.
   */
  void redraw();

  void addCanvasListener(ICanvasListener listener);

  void removeCanvasListener(ICanvasListener listener);

  void dispose();

}