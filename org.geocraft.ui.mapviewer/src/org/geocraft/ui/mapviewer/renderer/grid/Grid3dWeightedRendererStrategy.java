///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
// */
//package org.geocraft.ui.mapviewer.renderer.grid;
//
//
//import java.awt.geom.Point2D;
//
//import org.geocraft.core.color.ColorBar;
//import org.geocraft.core.model.geometry.GridGeometry3d;
//import org.geocraft.core.model.grid.Grid3d;
//import org.geocraft.core.service.ServiceProvider;
//import org.geocraft.ui.plot.model.IModelSpace;
//import org.geocraft.ui.plot.model.IModelSpaceCanvas;
//
//
//public class Grid3dWeightedRendererStrategy extends AbstractGrid3dRendererStrategy {
//
//  public Grid3dWeightedRendererStrategy(final Grid3dRendererModel model) {
//    super(model);
//  }
//
//  public void render(final IModelSpaceCanvas canvas, final IModelSpace modelSpace, final Grid3d grid,
//      final int drawPixelMinX, final int drawPixelMaxX, final int drawPixelMinY, final int drawPixelMaxY,
//      final int imageWidth, final int numBytesPerPixel, final byte[] imagePixels, final byte[] imageAlphas) {
//    int row = 0;
//    int col = 0;
//    int nrows = grid.getNumJ();
//    int ncols = grid.getNumI();
//    GridGeometry3d geometry = grid.getGeometry();
//    Point2D.Double modelxy = new Point2D.Double();
//
//    Grid3d grid2 = _model.getSecondaryGrid();
//    int weighting = _model.getSimpleBlendWeighting();
//    int alpha = (int) (255 * .01f * (100 - _model.getTransparency()));
//    boolean smoothImage = _model.getSmoothImage();
//    ColorBar colorBar = _model.getColorBar(grid);
//
//    // Loop over each pixel.
//    for (int px = drawPixelMinX; px <= drawPixelMaxX; px++) {
//      for (int py = drawPixelMinY; py <= drawPixelMaxY; py++) {
//        // Convert the pixel x,y coordinates to a model x,y coordinates.
//        canvas.transformPixelToModel(modelSpace, px, py, modelxy);
//        try {
//          double gridx = modelxy.getX();
//          double gridy = modelxy.getY();
//          // Convert the model x,y coordinates to row,col coordinates.
//          double[] ij = geometry.transformXYToIJ(gridx, gridy, false);
//          if (!smoothImage) {
//            // Not smoothing, so round to the nearest integer row,col.
//            col = (int) Math.round(ij[0]);
//            row = (int) Math.round(ij[1]);
//            // If the row,col coordinates are valid, get the value from the Grid.
//            if (col >= 0 && col < ncols && row >= 0 && row < nrows) {
//              // If the value is not null, then set the color index of the plot pixel.
//              if (!grid.isNull(row, col) && !grid2.isNull(row, col)) {
//                float value1 = grid.getValueAtRowCol(row, col);
//                float value2 = grid2.getValueAtRowCol(row, col);
//                float weight1 = (100f - weighting) / 100f;
//                float weight2 = 1f - weight1;
//                float value = value1 * weight1 + value2 * weight2;
//                int colorIndex = colorBar.getColorIndex(value);
//                setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, colorBar, colorIndex, alpha,
//                    imagePixels, imageAlphas);
//              }
//            }
//          } else {
//            // Smoothing, so get the row,col coordinates of the 4 surrounding grid cells.
//            int row0 = (int) ij[1];
//            int row1 = row0 + 1;
//            int col0 = (int) ij[0];
//            int col1 = col0 + 1;
//            double row1wt = ij[1] - row0;
//            double row0wt = 1 - row1wt;
//            double col1wt = ij[0] - col0;
//            double col0wt = 1 - col1wt;
//
//            int[] rowArray = new int[4];
//            int[] colArray = new int[4];
//            rowArray[0] = row0;
//            colArray[0] = col0;
//            rowArray[1] = row1;
//            colArray[1] = col0;
//            rowArray[2] = row0;
//            colArray[2] = col1;
//            rowArray[3] = row1;
//            colArray[3] = col1;
//
//            // Compute the weighting factors for each of the 4 cells.
//            double[] sclArray = new double[4];
//            sclArray[0] = row0wt * col0wt;
//            sclArray[1] = row1wt * col0wt;
//            sclArray[2] = row0wt * col1wt;
//            sclArray[3] = row1wt * col1wt;
//
//            float gridValue = 0;
//            double count = 0;
//
//            // If all the row,col coordinates are value, get the values from the Grid.
//            if (ij[1] >= 0 && ij[1] < nrows - 1 && ij[0] >= 0 && ij[0] < ncols - 1) {
//              // Add the value from each cell, excluding any null values.
//              for (int m = 0; m < 4; m++) {
//                row = rowArray[m];
//                col = colArray[m];
//                // If the value is not null, weight the contribution of each cell value by its scalar.
//                if (!grid.isNull(row, col) && !grid2.isNull(row, col)) {
//                  float value1 = grid.getValueAtRowCol(row, col);
//                  float value2 = grid2.getValueAtRowCol(row, col);
//                  float weight1 = (100f - weighting) / 100f;
//                  float weight2 = 1f - weight1;
//                  float value = value1 * weight1 + value2 * weight2;
//                  gridValue += value * sclArray[m];
//                  count += sclArray[m];
//                }
//              }
//              if (count >= 0.5) {
//                gridValue /= count;
//              } else {
//                gridValue = grid.getNullValue();
//              }
//              // If the value is not null, then set the color index of the plot pixel.
//              if (!grid.isNull(gridValue)) {
//                int index = colorBar.getColorIndex(gridValue);
//                setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, index, alpha, imagePixels,
//                    imageAlphas);
//              }
//            }
//          }
//        } catch (Exception ex) {
//          ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString());
//        }
//      }
//    }
//  }
//
//}
