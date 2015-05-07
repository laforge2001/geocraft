/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;


/**
 * This class renders an image into a given rectangle.
 */
public class BackgroundImageRenderer implements IBackgroundRenderer {

  /** The background image to render. */
  private Image _image;

  /** Flag indicating if the image is textured. */
  private final boolean _isTextured;

  /**
   * Constructs a background renderer that fills with an untextured image.
   * @param image the background image to fill with.
   */
  public BackgroundImageRenderer(final Image image) {
    this(image, false);
  }

  /**
   * Constructs a background renderer that fills with an image.
   * @param image the background image.
   * @param textured <i>true</i> to generate a textured image, otherwise <i>false</i> for a single image.
   */
  public BackgroundImageRenderer(final Image image, final boolean textured) {
    // Construct an internal copy of the image
    setImage(image);
    _isTextured = textured;
  }

  public void render(final GC gc, final Rectangle rectangle) {
    // Draw the background image into the specified rectangle.
    if (!_isTextured) {
      gc.drawImage(_image, rectangle.x, rectangle.y);
    } else {
      gc.drawImage(_image, rectangle.x, rectangle.y);
      // TODO:      
      //      TexturePaint texturePaint = new TexturePaint(_backgroundImage, new Rectangle(0, 0, _backgroundImage.getWidth(), _backgroundImage.getHeight()));
      //      g2.setPaint(texturePaint);
      //      g2.fill(new Rectangle(0, 0, bounds.width, bounds.height));
      //      g2.setPaint(null);
      //      graphics.setBackgroundPattern(pattern);
      //      graphics.fillRectangle(bounds);
      //      graphics.setBackgroundPattern(pattern);
    }
  }

  public void setColor(final RGB color) {
    // This image renderer does not contain a color.
    // No action required.
  }

  public RGB getColor() {
    // This image renderer does not contain a color.
    return null;
  }

  public Image getImage() {
    return _image;
  }

  public void setImage(final Image image) {
    // Create an internal copy of the image and dispose of the old one.
    Image imageOld = _image;
    if (image != null) {
      _image = new Image(image.getDevice(), image.getImageData());
    } else {
      _image = null;
    }
    if (imageOld != null) {
      imageOld.dispose();
    }
  }

  public void dispose() {
    // Dispose of the internal image resource.
    _image.dispose();
  }
}
