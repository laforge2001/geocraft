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
//public class Grid3dGlyphsRendererStrategy extends AbstractGrid3dRendererStrategy {
//
//  public Grid3dGlyphsRendererStrategy(final Grid3dRendererModel model) {
//    super(model);
//  }
//
//  public void render(final IModelSpaceCanvas canvas, final IModelSpace modelSpace, final Grid3d grid,
//      final int drawPixelMinX, final int drawPixelMaxX, final int drawPixelMinY, final int drawPixelMaxY,
//      final int imageWidth, final int numBytesPerPixel, final byte[] imagePixels, final byte[] imageAlphas) {
//    Grid3d magnitudeGrid = _model.getGlyphMagnitudeGrid();
//    Grid3d directionGrid = _model.getGlyphDirectionGrid();
//
//    int row = 0;
//    int col = 0;
//    int nrows = grid.getNumJ();
//    int ncols = grid.getNumI();
//    GridGeometry3d geometry = grid.getGeometry();
//    Point2D.Double modelxy = new Point2D.Double();
//    Point2D.Double pixelxy = new Point2D.Double();
//
//    int alpha = (int) (255 * .01f * (100 - _model.getTransparency()));
//    ColorBar colorBar = _model.getColorBar(grid);
//
//    float[][] directionValues = directionGrid.getValues();
//    float[][] magnitudeValues = magnitudeGrid.getValues();
//    double rowSpacing = geometry.getJSpacing();
//    double colSpacing = geometry.getISpacing();
//    double[] xy00 = geometry.transformIJToXY(0, 0);
//    canvas.transformModelToPixel(modelSpace, xy00[0], xy00[1], pixelxy);
//    double px00 = pixelxy.x;
//    double py00 = pixelxy.y;
//    double[] xy10 = geometry.transformIJToXY(0, 1);
//    canvas.transformModelToPixel(modelSpace, xy10[0], xy10[1], pixelxy);
//    double px10 = pixelxy.x;
//    double py10 = pixelxy.y;
//    double[] xy01 = geometry.transformIJToXY(1, 0);
//    canvas.transformModelToPixel(modelSpace, xy01[0], xy01[1], pixelxy);
//    double px01 = pixelxy.x;
//    double py01 = pixelxy.y;
//    double dpx10 = px10 - px00;
//    double dpy10 = py10 - py00;
//    double dpx01 = px01 - px00;
//    double dpy01 = py01 - py00;
//    double dp10 = Math.sqrt(dpx10 * dpx10 + dpy10 * dpy10);
//    double dp01 = Math.sqrt(dpx01 * dpx01 + dpy01 * dpy01);
//    double dpmax = Math.max(dp10, dp01);
//
//    // Loop over each pixel.
//    for (row = 0; row < nrows; row++) {
//      for (col = 0; col < ncols; col++) {
//        // Convert the pixel x,y coordinates to a model x,y coordinates.
//        //canvas.transformPixelToModel(modelSpace, px, py, modelxy);
//        try {
//          //double gridx = modelxy.getX();
//          //double gridy = modelxy.getY();
//          // Convert the model x,y coordinates to row,col coordinates.
//          //double[] rc = geometry.transformXYToRowCol(gridx, gridy, false);
//
//          // Not smoothing, so round to the nearest integer row,col.
//          //row = (int) Math.round(rc[0]);
//          //col = (int) Math.round(rc[1]);
//          // If the row,col coordinates are valid, get the value from the Grid.
//          if (col >= 0 && col < ncols && row >= 0 && row < nrows) {
//            // If the value is not null, then set the color index of the plot pixel.
//            if (!grid.isNull(row, col) && !directionGrid.isNull(row, col) && !magnitudeGrid.isNull(row, col)) {
//              double[] xy = geometry.transformIJToXY(row, col);
//              canvas.transformModelToPixel(modelSpace, xy[0], xy[1], pixelxy);
//              float direction = directionValues[row][col];
//              float magnitude = directionValues[row][col];
//              double directionInRad = Math.toRadians(90 - direction);
//              double cosTerm = Math.cos(directionInRad);
//              double sinTerm = Math.sin(directionInRad);
//              int colorIndex = colorBar.getColorIndex(grid.getValueAtRowCol(row, col));
//              double fdx1 = cosTerm * dpmax / 2;
//              double fdy1 = sinTerm * dpmax / 2;
//              double fdx0 = -fdx1;
//              double fdy0 = -fdy1;
//              int idx = 1 + (int) Math.abs(fdx1);
//              int idy = 1 + (int) Math.abs(fdy1);
//              int numSteps = 0;
//              double fdx = 0;
//              double fdy = 0;
//              if (idx > idy) {
//                numSteps = 2 * idx + 1;
//              } else {
//                numSteps = 2 * idy + 1;
//              }
//              int numStepsHalf = numSteps / 2;
//              if (numStepsHalf > 0) {
//                fdx = fdx1 / numStepsHalf;
//                fdy = fdy1 / numStepsHalf;
//              } else {
//                fdx = 0;
//                fdy = 0;
//              }
//              for (int ii = -numStepsHalf; ii <= numStepsHalf; ii++) {
//                int px = (int) (pixelxy.x + ii * fdx);
//                int py = (int) (pixelxy.y - ii * fdy);
//                if (px >= drawPixelMinX && px <= drawPixelMaxX && py >= drawPixelMinY && py < drawPixelMaxY) {
//                  setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, colorBar, colorIndex, alpha,
//                      imagePixels, imageAlphas);
//                }
//              }
//            }
//          }
//
//        } catch (Exception ex) {
//          ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString());
//        }
//      }
//    }
//  }
//
//}
