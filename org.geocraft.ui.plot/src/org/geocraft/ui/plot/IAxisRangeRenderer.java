/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.attribute.TextProperties;


/**
 * The interface for axis range renderers, used for rendering the numerical annotation of an axis.
 */
public interface IAxisRangeRenderer {

  /**
   * Renders the range portion of an axis (i.e. the numerical annotation).
   * 
   * @param graphics the graphics objects used for rendering.
   * @param rectangle the rectangle representing the size of the drawing area.
   * @param textProperties the text properties to use for labels, etc.
   */
  void render(final GC graphics, final Rectangle rectangle, final TextProperties textProperties, final int thumb,
      final int minimum, final int maximum, final int selection, final int sizeToSubtrace);
}
