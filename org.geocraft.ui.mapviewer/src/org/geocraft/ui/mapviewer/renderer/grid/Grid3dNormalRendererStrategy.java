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
//public class Grid3dNormalRendererStrategy extends AbstractGrid3dRendererStrategy {
//
//  public Grid3dNormalRendererStrategy(final Grid3dRendererModel model) {
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
//              if (!grid.isNull(row, col)) {
//                int colorIndex = colorBar.getColorIndex(grid.getValueAtRowCol(row, col));
//                setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, colorBar, colorIndex, alpha,
//                    imagePixels, imageAlphas);
//              }
//            }
//          } else {
//            float gridValue = grid.getValueAtXY(gridx, gridy, false);
//            // If the value is not null, then set the color index of the plot pixel.
//            if (!grid.isNull(gridValue)) {
//              int index = colorBar.getColorIndex(gridValue);
//              setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, colorBar, index, alpha,
//                  imagePixels, imageAlphas);
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
