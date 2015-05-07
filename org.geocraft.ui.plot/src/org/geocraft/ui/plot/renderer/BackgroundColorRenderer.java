/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;


/**
 * This class renders a solid color into a given rectangle.
 */
public class BackgroundColorRenderer implements IBackgroundRenderer {

  /** The background color to render. */
  private Color _color;

  /**
   * Constructs a background renderer that fills with a color.
   * @param color the background color to fill with.
   */
  public BackgroundColorRenderer(final RGB color) {
    setColor(color);
  }

  public void render(final GC gc, final Rectangle rectangle) {
    // Fill the specified rectangle with the background color.
    gc.setBackground(_color);
    gc.fillRectangle(rectangle);
  }

  public void setColor(final RGB color) {
    // Create an internal copy of the color and dispose of the old one.
    Color colorOld = _color;
    _color = new Color(null, color);
    if (colorOld != null) {
      colorOld.dispose();
    }
  }

  public RGB getColor() {
    return _color.getRGB();
  }

  public Image getImage() {
    // This color renderer does not contain an image.
    return null;
  }

  @SuppressWarnings("unused")
  public void setImage(final Image image) {
    // This color renderer does not contain an image.
    // No action required.
  }

  public void dispose() {
    // Dispose of the internal color resource.
    _color.dispose();
  }
}
