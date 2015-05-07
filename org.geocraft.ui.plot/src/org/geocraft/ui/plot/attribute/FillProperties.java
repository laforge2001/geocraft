/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.attribute;


import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.model.AbstractBean;
import org.geocraft.ui.plot.defs.FillStyle;


/**
 * The model of properties used for filling plot shapes.
 * These include the fill style, color, texture image and opacity.
 */
public class FillProperties extends AbstractBean {

  /** The fill style: NONE, SOLID, TEXTURE. */
  private FillStyle _fillStyle;

  /** The fill color. */
  private RGB _fillRGB;

  /** The image used for a texture fill style. */
  private BufferedImage _fillImage;

  /**
   * The empty constructor.
   */
  public FillProperties() {
    _fillStyle = FillStyle.NONE;
    _fillRGB = new RGB(0, 0, 0);
    _fillImage = null;
  }

  /**
   * The default constructor.
   * @param textStyle the text style.
   * @param textRGB the text color.
   */
  public FillProperties(final FillStyle style, final RGB color, final BufferedImage image) {
    setStyle(style);
    setRGB(color);
    setImage(image);
  }

  /**
   * The copy constructor.
   * @param fillProps the fill properties to copy.
   */
  public FillProperties(final FillProperties fillProps) {
    this(fillProps.getStyle(), fillProps.getRGB(), fillProps.getImage());
  }

  /**
   * Returns the fill style (NONE, SOLID or TEXTURE).
   */
  public FillStyle getStyle() {
    return _fillStyle;
  }

  /**
   * Returns the color used for a solid fill.
   */
  public RGB getRGB() {
    return _fillRGB;
  }

  /**
   * Returns the image used for a texture fill.
   */
  public BufferedImage getImage() {
    return _fillImage;
  }

  /**
   * Sets the style of fill (NONE, SOLID or TEXTURE).
   * 
   * @param style the fill style to set.
   */
  public void setStyle(final FillStyle style) {
    firePropertyChange("fillStyle", _fillStyle, _fillStyle = style);
  }

  /**
   * Sets the color to use for a solid fill.
   * 
   * @param rgb the color to set.
   */
  public void setRGB(final RGB rgb) {
    firePropertyChange("fillRGB", _fillRGB, _fillRGB = rgb);
  }

  /**
   * Sets the image to use for a texture fill.
   * 
   * @param fillImage the texture image.
   */
  public void setImage(final BufferedImage fillImage) {
    firePropertyChange("fillImage", _fillImage, _fillImage = fillImage);
  }

  public void dispose() {
    // No resources to dispose.
  }

}
