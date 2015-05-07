/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.defs.ImageAnchor;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


/**
 * The interface for a plot image.
 */
public interface IPlotImage extends IPlotShape {

  /**
   * Gets the plot image anchor.
   * @return the plot image anchor.
   */
  ImageAnchor getAnchorType();

  /**
   * Sets the plot image anchor.
   * @param anchor the plot image anchor.
   */
  void setAnchorType(ImageAnchor anchor);

  /**
   * Returns the fixed size status of the plot image.
   * @return true if image is fixed size; false if not.
   */
  boolean isFixedSize();

  /**
   * Sets the fixed size status of the plot image.
   * @param fixedSize true to set the image to be fixed size; otherwise false.
   */
  void setFixedSize(boolean fixedSize);

  /**
   * Gets the actual image.
   * @return the actual image.
   */
  Image getImage();

  /**
   * Sets the actual image.
   * @param image the actual image to set.
   */
  void setImage(Image image);

  /**
   * Gets the bounds of the image.
   * @param canvas the model canvas.
   */
  Rectangle getRectangle(IModelSpaceCanvas canvas);

}