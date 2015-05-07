/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.attribute.FillProperties;
import org.geocraft.ui.plot.defs.FillStyle;


public interface IPlotFilledShape extends IPlotLinedShape {

  /**
   * Gets the polygon fill properties.
   * @return the polygon fill properties.
   */
  FillProperties getFillProperties();

  FillStyle getFillStyle();

  RGB getFillColor();

  BufferedImage getFillImage();

  void setFillStyle(final FillStyle fillStyle);

  void setFillColor(final RGB color);

  void setFillImage(final BufferedImage fillImage);
}
