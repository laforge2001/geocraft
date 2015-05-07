///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
// */
//package org.geocraft.ui.mapviewer.renderer.grid;
//
//
//import java.awt.geom.Point2D;
//import java.nio.ByteBuffer;
//
//import org.eclipse.swt.graphics.RGB;
//import org.geocraft.core.model.geometry.GridGeometry3d;
//import org.geocraft.core.model.grid.Grid3d;
//import org.geocraft.core.service.ServiceProvider;
//import org.geocraft.ui.plot.model.IModelSpace;
//import org.geocraft.ui.plot.model.IModelSpaceCanvas;
//
//
//public class Grid3dRgbBlendingRendererStrategy extends AbstractGrid3dRendererStrategy {
//
//  public Grid3dRgbBlendingRendererStrategy(final Grid3dRendererModel model) {
//    super(model);
//  }
//
//  public void render(final IModelSpaceCanvas canvas, final IModelSpace modelSpace, final Grid3d grid,
//      final int drawPixelMinX, final int drawPixelMaxX, final int drawPixelMinY, final int drawPixelMaxY,
//      final int imageWidth, final int numBytesPerPixel, final byte[] imagePixels, final byte[] imageAlphas) {
//    int row = 0;
//    int col = 0;
//    GridGeometry3d geometry = grid.getGeometry();
//    int nrows = geometry.getNumJ();
//    int ncols = geometry.getNumI();
//    Point2D.Double modelxy = new Point2D.Double();
//
//    int alpha = (int) (255 * .01f * (100 - _model.getTransparency()));
//    Grid3d redGrid = _model.getRedChannelGrid();
//    Grid3d greenGrid = _model.getGreenChannelGrid();
//    Grid3d blueGrid = _model.getBlueChannelGrid();
//    float redStart = _model.getRedStartValue();
//    float redEnd = _model.getRedEndValue();
//    float greenStart = _model.getGreenStartValue();
//    float greenEnd = _model.getGreenEndValue();
//    float blueStart = _model.getBlueStartValue();
//    float blueEnd = _model.getBlueEndValue();
//    ByteBuffer buffer = ByteBuffer.allocate(4);
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
//          // if (!smoothImage) {
//          // Not smoothing, so round to the nearest integer row,col.
//          col = (int) Math.round(ij[0]);
//          row = (int) Math.round(ij[1]);
//          // If the row,col coordinates are valid, get the value from the Grid.
//          if (col >= 0 && col < ncols && row >= 0 && row < nrows) {
//            // If the value is not null, then set the color index of the plot pixel.
//            if (!redGrid.isNull(row, col) && !greenGrid.isNull(row, col) && !blueGrid.isNull(row, col)) {
//              boolean showRedChannel = _model.getRedChannelFlag();
//              boolean showGreenChannel = _model.getGreenChannelFlag();
//              boolean showBlueChannel = _model.getBlueChannelFlag();
//              float redValue = redGrid.getValueAtRowCol(row, col);
//              float greenValue = greenGrid.getValueAtRowCol(row, col);
//              float blueValue = blueGrid.getValueAtRowCol(row, col);
//              float redPercent = (redValue - redStart) / (redEnd - redStart);
//              if (showRedChannel) {
//                showRedChannel = redPercent >= 0 && redPercent <= 1;
//              }
//              redPercent = Math.max(0, redPercent);
//              redPercent = Math.min(1, redPercent);
//              float greenPercent = (greenValue - greenStart) / (greenEnd - greenStart);
//              if (showGreenChannel) {
//                showGreenChannel = greenPercent >= 0 && greenPercent <= 1;
//              }
//              greenPercent = Math.max(0, greenPercent);
//              greenPercent = Math.min(1, greenPercent);
//              float bluePercent = (blueValue - blueStart) / (blueEnd - blueStart);
//              if (showBlueChannel) {
//                showBlueChannel = bluePercent >= 0 && bluePercent <= 1;
//              }
//              bluePercent = Math.max(0, bluePercent);
//              bluePercent = Math.min(1, bluePercent);
//              int iredValue = (int) (redPercent * 255);
//              int igreenValue = (int) (greenPercent * 255);
//              int iblueValue = (int) (bluePercent * 255);
//              if (!showRedChannel) {
//                iredValue = 0;
//              }
//              if (!showGreenChannel) {
//                igreenValue = 0;
//              }
//              if (!showBlueChannel) {
//                iblueValue = 0;
//              }
//              RGB rgb = new RGB(iredValue, igreenValue, iblueValue);
//              int color = rgb.red << 16 | rgb.green << 8 | rgb.blue << 0;
//              buffer.position(0);
//              buffer.putInt(color);
//              setColorIndexOfPixel32Bits(px - drawPixelMinX, py - drawPixelMinY, imageWidth, buffer.array(), alpha,
//                  false, numBytesPerPixel, imagePixels, imageAlphas);
//            }
//          }
//        } catch (Exception ex) {
//          ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString());
//        }
//      }
//    }
//  }
//}
