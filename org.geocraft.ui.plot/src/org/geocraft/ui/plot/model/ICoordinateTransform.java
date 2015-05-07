/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.model;


import java.awt.geom.Point2D;

import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.Orientation;


/**
 * The interface for a plot coordinate transform. The transforms allow for
 * conversion between model and pixel coordinates.
 */
public interface ICoordinateTransform {

  /**
   * Converts a model coordinate to a pixel coordinate.
   * 
   * @param model the plot model space to use in the conversion.
   * @param mx the model x coordinate.
   * @param my the model y coordinate.
   * @param p the storage for the computed pixel x,y coordinates.
   */
  void transformModelToPixel(IModelSpace model, double mx, double my, Point2D.Double p);

  /**
   * Converts a pixel coordinate to a model coordinate.
   * 
   * @param model the plot model space to use in the conversion.
   * @param px the pixel x coordinate.
   * @param py the pixel y coordinate.
   * @param m the storage for the computed model x,y coordinates.
   */
  void transformPixelToModel(IModelSpace model, int px, int py, Point2D.Double m);

  /**
   * Converts a pixel coordinate to a model coordinate.
   * 
   * @param axis the model axis to use in the conversion.
   * @param pixelCoord the x-or-y pixel coordinate to convert.
   * @param numPixels the pixel size to use in conversion.
   * @return the converted model coordinate.
   */
  double transformPixelToModel(IAxis axis, double pixelCoord);

  /**
   * Converts a model coordinate to a pixel coordinate.
   * 
   * @param axis the model axis to use in the conversion.
   * @param coord the x-or-y model coordinate to convert.
   * @param numPixels the pixel size to use in conversion. 
   * @return the converted pixel coordinate.
   */
  double transformModelToPixel(IAxis axis, double coord);

  /**
   * Gets the number of pixels in the model canvas for the specified
   * orientation.
   * 
   * @param orientation the orientation (HORIZONTAL or VERTICAL).
   * @return the number of pixels.
   */
  int getNumPixelsInModelCanvas(Orientation orientation);
}