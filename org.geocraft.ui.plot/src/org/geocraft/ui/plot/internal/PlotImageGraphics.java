/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.internal;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;


/**
 * The graphics object used for drawing an image, with an off-screen buffer.
 */
public class PlotImageGraphics {

  /** The buffered image. */
  protected Image _image;

  /** The graphics. */
  protected GC _graphics;

  /**
   * Constructs a plot image graphics component.
   * @param composite the composite.
   * @param width the width.
   * @param height the height.
   */
  public PlotImageGraphics(final Composite composite, final int width, final int height) {
    // Allocate a new image.
    _image = new Image(composite.getDisplay(), width, height);
    if (_image != null) {
      // Allocate a new GC.
      _graphics = new GC(_image);
    }
  }

  /**
   * Gets the current image.
   * @return the current image.
   */
  public Image getImage() {
    return _image;
  }

  /**
   * Gets the current graphics.
   * @return the current graphics.
   */
  public GC getGraphics() {
    return _graphics;
  }

  /**
   * Disposes of the resource associated with the graphics object.
   */
  public void dispose() {
    if (_graphics != null) {
      _graphics.dispose();
    }
    if (_image != null) {
      _image.dispose();
    }
  }
}
