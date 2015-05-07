/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.grid;


import java.awt.geom.Point2D;

import org.geocraft.core.color.ColorBar;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


public class Grid3dRendererRunnable implements Runnable {

  protected static final byte FULLY_TRANSPARENT = (byte) 0;

  protected static final byte FULLY_OPAQUE = (byte) 255;

  private final IModelSpaceCanvas _canvas;

  private final IModelSpace _modelSpace;

  private final Grid3d _grid;

  private final float[][] _values;

  private final boolean[][] _nulls;

  private final int _xPixelStart;

  private final int _xPixelEnd;

  private final int _drawPixelMinX;

  //private final int _drawPixelMaxX;

  private final int _drawPixelMinY;

  private final int _drawPixelMaxY;

  private final int _imageWidth;

  private final ColorBar _colorBar;

  private final int _alpha;

  private final boolean _smoothImage;

  private final byte[] _imagePixels;

  private final byte[] _imageAlphas;

  public Grid3dRendererRunnable(final IModelSpaceCanvas canvas, final IModelSpace modelSpace, final Grid3d grid, final float[][] values, final boolean[][] nulls, final int xPixelStart, final int xPixelEnd, final int drawPixelMinX, final int drawPixelMaxX, final int drawPixelMinY, final int drawPixelMaxY, final int imageWidth, final ColorBar colorBar, final int alpha, final boolean smoothImage, final byte[] imagePixels, final byte[] imageAlphas) {
    _canvas = canvas;
    _modelSpace = modelSpace;
    _grid = grid;
    _values = values;
    _nulls = nulls;
    _imageWidth = imageWidth;
    _xPixelStart = xPixelStart;
    _xPixelEnd = xPixelEnd;
    _drawPixelMinX = drawPixelMinX;
    //_drawPixelMaxX = drawPixelMaxX;
    _drawPixelMinY = drawPixelMinY;
    _drawPixelMaxY = drawPixelMaxY;
    _colorBar = colorBar;
    _alpha = alpha;
    _smoothImage = smoothImage;
    _imagePixels = imagePixels;
    _imageAlphas = imageAlphas;
  }

  public void run() {
    int row = 0;
    int col = 0;
    GridGeometry3d geometry = _grid.getGeometry();
    int nrows = _grid.getNumRows();
    int ncols = _grid.getNumColumns();
    Point2D.Double modelxy = new Point2D.Double();

    // Loop over each pixel.
    for (int px = _xPixelStart; px <= _xPixelEnd; px++) {
      for (int py = _drawPixelMinY; py <= _drawPixelMaxY; py++) {
        // Convert the pixel x,y coordinates to a model x,y coordinates.
        _canvas.transformPixelToModel(_modelSpace, px, py, modelxy);
        try {
          double gridx = modelxy.getX();
          double gridy = modelxy.getY();
          // Convert the model x,y coordinates to row,col coordinates.
          double[] rc = geometry.transformXYToRowCol(gridx, gridy, false);
          //double[] rc = { 0, 0 };
          if (!_smoothImage) {
            // Not smoothing, so round to the nearest integer row,col.
            row = (int) Math.round(rc[0]);
            col = (int) Math.round(rc[1]);
            // If the row,col coordinates are valid, get the value from the Grid.
            if (col >= 0 && col < ncols && row >= 0 && row < nrows) {
              // If the value is not null, then set the color index of the plot pixel.
              float value = _values[row][col];
              if (!_nulls[row][col]) {
                int colorIndex = _colorBar.getColorIndex(value);
                setColorIndexOfPixel(px - _drawPixelMinX, py - _drawPixelMinY, _imageWidth, _colorBar, colorIndex,
                    _alpha);
              }
            }
          } else {
            // Smoothing, so get the row,col coordinates of the 4 surrounding grid cells.
            int row0 = (int) rc[0];
            int row1 = row0 + 1;
            int col0 = (int) rc[1];
            int col1 = col0 + 1;
            double row1wt = rc[0] - row0;
            double row0wt = 1 - row1wt;
            double col1wt = rc[1] - col0;
            double col0wt = 1 - col1wt;

            int[] rowArray = new int[4];
            int[] colArray = new int[4];
            rowArray[0] = row0;
            colArray[0] = col0;
            rowArray[1] = row1;
            colArray[1] = col0;
            rowArray[2] = row0;
            colArray[2] = col1;
            rowArray[3] = row1;
            colArray[3] = col1;

            // Compute the weighting factors for each of the 4 cells.
            double[] sclArray = new double[4];
            sclArray[0] = row0wt * col0wt;
            sclArray[1] = row1wt * col0wt;
            sclArray[2] = row0wt * col1wt;
            sclArray[3] = row1wt * col1wt;

            float gridValue = 0;
            double count = 0;

            // If all the row,col coordinates are value, get the values from the Grid.
            if (rc[1] >= 0 && rc[1] < ncols - 1 && rc[0] >= 0 && rc[0] < nrows - 1) {
              // Add the value from each cell, excluding any null values.
              for (int m = 0; m < 4; m++) {
                row = rowArray[m];
                col = colArray[m];
                // If the value is not null, weight the contribution of each cell value by its scalar.
                if (!_grid.isNull(row, col)) {
                  gridValue += _values[row][col] * sclArray[m];
                  count += sclArray[m];
                }
              }
              if (count >= 0.5) {
                gridValue /= count;
              } else {
                gridValue = _grid.getNullValue();
              }
              // If the value is not null, then set the color index of the plot pixel.
              if (!_grid.isNull(gridValue)) {
                int index = _colorBar.getColorIndex(gridValue);
                setColorIndexOfPixel(px - _drawPixelMinX, py - _drawPixelMinY, _imageWidth, _colorBar, index, _alpha);
              }
            }
          }
        } catch (Exception ex) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString());
        }
      }
    }
  }

  /**
   * Sets the color index of the specified image pixel.
   * @param x the x pixel.
   * @param y the y pixel.
   * @param width the image width in pixels.
   * @param colorIndex the color index.
   */
  private void setColorIndexOfPixel(final int x, final int y, final int width, final ColorBar colorBar,
      final int clrIndex, final int alpha) {
    int numColors = colorBar.getNumColors();
    int colorIndex = Math.max(clrIndex, 0);
    colorIndex = Math.min(colorIndex, numColors - 1);
    int pixelIndex = y * width + x;
    if (colorIndex >= 0 && colorIndex < numColors) {
      _imagePixels[pixelIndex] = (byte) colorIndex;
      _imageAlphas[pixelIndex] = (byte) alpha;
    } else {
      _imageAlphas[pixelIndex] = FULLY_TRANSPARENT;
    }
  }

  public static void main(final String[] args) {
    float[] values = new float[735 * 1671];
    for (int i = 0; i < values.length; i++) {
      values[i] = i % 100;
    }
    long t1 = System.currentTimeMillis();
    for (float value : values) {
      MathUtil.isEqual(value, 20f);
    }
    long t2 = System.currentTimeMillis();
    System.out.println("ELAPSED TIME: " + (t2 - t1));
  }
}
