/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.label;


import java.beans.PropertyChangeListener;

import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.defs.Orientation;


/**
 * The interface for a plot label.
 */
public interface ILabel {

  String getText();

  void setText(String text);

  Orientation getOrientation();

  void setOrientation(Orientation orientation);

  Alignment getAlignment();

  void setAlignment(Alignment alignment);

  boolean isVisible();

  void setVisible(boolean visible);

  void dispose();

  void addPropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

}