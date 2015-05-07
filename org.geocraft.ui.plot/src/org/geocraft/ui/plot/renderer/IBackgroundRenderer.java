/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;


/**
 * The interface for renderers that fill a given
 * rectangle with a background color or image.
 */
public interface IBackgroundRenderer {

  /**
   * Renders the background into the specified rectangle.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   */
  void render(GC gc, Rectangle rectangle);

  /**
   * Gets the background color.
   * This resource is managed by the renderer and
   * should not be disposed.
   * @return the background color.
   */
  RGB getColor();

  /**
   * Set the background color.
   * @param color the background color to set.
   */
  void setColor(RGB color);

  /**
   * Gets the background image, or <i>null</i> if none.
   * This resource is managed by the renderer and
   * should not be disposed.
   * @return the background image, or <i>null</i> if none.
   */
  Image getImage();

  /**
   * Sets the background image.
   * @param image the background image to set.
   */
  void setImage(Image image);

  /**
   * Disposes of the renderer resources.
   */
  void dispose();
}