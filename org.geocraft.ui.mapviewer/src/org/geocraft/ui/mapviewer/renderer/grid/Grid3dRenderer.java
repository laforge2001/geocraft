package org.geocraft.ui.mapviewer.renderer.grid;


import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.color.map.GrayscaleColorMap;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.Generics;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.color.ColorBarEditorListener;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.mapviewer.renderer.BlendingType;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.ImageAnchor;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotImage;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.PlotImage;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.ViewLayerEvent;
import org.geocraft.ui.viewer.light.LightSourceModel;


/**
 * Renders a <code>Grid3d</code> entity in the map viewer.
 */
public class Grid3dRenderer extends MapViewRenderer implements ControlListener, ColorMapListener,
    ColorBarEditorListener {

  /** The grid being rendered. */
  protected Grid3d _grid;

  /** The geometry of the rendered grid. */
  protected GridGeometry3d _geometry;

  /** The plot image for the grid. */
  protected IPlotImage _plotImage;

  /** The byte array of image pixels. */
  protected byte[] _imagePixels;

  protected int _numBytesPerPixel;

  /** The byte array of image alphas. */
  protected byte[] _imageAlphas;

  /** Status flag for updating the rendering. */
  protected boolean _updateRendering;

  /** Initially used for the x gradient, then reused to store the normal. */
  protected float[][] _dx;

  /** Initially used for the y gradient, then reused to store the normal. */
  protected float[][] _dy;

  /** Used to store the z component of the surface normal. */
  protected float[][] _dz;

  /** An array of the surface values. */
  protected float[][] _z;

  /** An array of the cosines of the angle between surface normals and sun. */
  protected float[][] _cosines;

  protected IPlotImage _shadedImage;

  protected byte[] _shadedPixels;

  protected static final byte GRAY_NULL = 0;

  protected static final byte FULLY_TRANSPARENT = (byte) 0;

  protected static final byte FULLY_OPAQUE = (byte) 255;

  protected PaletteData _grayscalePalette;

  protected PlotPolygon _outline;

  protected Grid3dRendererModel _model;

  protected IModelSpaceCanvas _canvas;

  protected IModelListener _lightSourceListener = null;

  public Grid3dRenderer() {
    super("Grid3d Renderer");
    _appendCursorInfo = true;
    _model = new Grid3dRendererModel();
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _grid };
  }

  public void colorBarChanged(final ColorBar colorBar) {
    _model.setColorBar(colorBar);
    redraw(false);
  }

  @Override
  public void redraw() {
    redraw(false);
  }

  /**
   * Redraws the grid in the map view, with an option for
   * resetting the color bar range to that of the grid
   * being rendered. This is useful in the event that an
   * algorithm (or other mechanism) updates the grid values
   * and the grid needs to be redrawn in the map view.
   * 
   * @param resetColorRange <i>true</i> to reset the color bar range to that of the grid; otherwise <i>false</i>.
   */
  protected void redraw(final boolean resetColorRange) {
    if (resetColorRange) {
      _model.setColorBarRange(_grid);
    }
    _updateRendering = true;
    renderImage();
  }

  @Override
  public void refresh() {
    _updateRendering = true;
    renderImage();
  }

  public void viewLayerUpdated(final ViewLayerEvent event) {
    redraw(false);
  }

  public void controlMoved(final ControlEvent event) {
    // No action required.
  }

  public void controlResized(final ControlEvent event) {
    redraw(false);
  }

  /**
   * Renders the Grid layer image.
   */
  private synchronized void renderImage() {
    IModelSpace modelSpace = getModelSpace();
    if (modelSpace == null || _grid == null) {
      return;
    }

    // Gets the plot x,y bounds.
    ModelSpaceBounds bounds = modelSpace.getViewableBounds();
    double xStart = bounds.getStartX();
    double xEnd = bounds.getEndX();
    double yStart = bounds.getStartY();
    double yEnd = bounds.getEndY();

    // Get pixel width and height of the plot.
    Point2D.Double pxy0 = new Point2D.Double();
    _canvas.transformModelToPixel(modelSpace, xStart, yStart, pxy0);
    Point2D.Double pxy1 = new Point2D.Double();
    _canvas.transformModelToPixel(modelSpace, xEnd, yEnd, pxy1);
    double px0 = Math.min(pxy0.x, pxy1.x);
    double py0 = Math.min(pxy0.y, pxy1.y);
    double px1 = Math.max(pxy0.x, pxy1.x);
    double py1 = Math.max(pxy0.y, pxy1.y);

    int imageWidth = (int) (px1 - px0);
    int imageHeight = (int) (py1 - py0);

    if (_grid == null) {
      throw new RuntimeException("The grid property should not be null.");
    }

    // Get the number of rows,cols of the parent Grid.
    int nrows = _grid.getNumRows();
    int ncols = _grid.getNumColumns();
    Point3d[] cornerPoints = _grid.getCornerPoints().getPointsDirect();
    double xmin = Double.MAX_VALUE;
    double xmax = -Double.MAX_VALUE;
    double ymin = Double.MAX_VALUE;
    double ymax = -Double.MAX_VALUE;
    for (Point3d cornerPoint : cornerPoints) {
      xmin = Math.min(xmin, cornerPoint.getX());
      xmax = Math.max(xmax, cornerPoint.getX());
      ymin = Math.min(ymin, cornerPoint.getY());
      ymax = Math.max(ymax, cornerPoint.getY());
    }

    Point2D.Double gridPixelMin = new Point2D.Double();
    _canvas.transformModelToPixel(modelSpace, xmin, ymin, gridPixelMin);
    Point2D.Double gridPixelMax = new Point2D.Double();
    _canvas.transformModelToPixel(modelSpace, xmax, ymax, gridPixelMax);
    int gridPixelMinX = (int) Math.floor(Math.min(gridPixelMin.getX(), gridPixelMax.getX()));
    int gridPixelMaxX = (int) Math.ceil(Math.max(gridPixelMin.getX(), gridPixelMax.getX()));
    int gridPixelMinY = (int) Math.floor(Math.min(gridPixelMin.getY(), gridPixelMax.getY()));
    int gridPixelMaxY = (int) Math.ceil(Math.max(gridPixelMin.getY(), gridPixelMax.getY()));
    int drawPixelMinX = (int) Math.max(gridPixelMinX, px0);
    int drawPixelMaxX = (int) Math.min(gridPixelMaxX, px1);
    int drawPixelMinY = (int) Math.max(gridPixelMinY, py0);
    int drawPixelMaxY = (int) Math.min(gridPixelMaxY, py1);
    Point2D.Double temp1 = new Point2D.Double();
    _canvas.transformPixelToModel(modelSpace, drawPixelMinX, drawPixelMinY, temp1);
    Point2D.Double temp2 = new Point2D.Double();
    _canvas.transformPixelToModel(modelSpace, drawPixelMaxX, drawPixelMaxY, temp2);
    Point2D.Double cornerPoint1 = new Point2D.Double(Math.min(temp1.x, temp2.x), Math.max(temp1.y, temp2.y));
    //Point2D.Double cornerPoint2 = new Point2D.Double(Math.max(temp1.x, temp2.x), Math.min(temp1.y, temp2.y));

    BlendingType blendingType = _model.getBlendingType();
    ColorBar colorBar = _model.getColorBar(_grid);
    // Create a new image if necessary (e.g. the plot was resized).
    boolean updatePixelArray = false;
    try {
      imageWidth = drawPixelMaxX - drawPixelMinX + 1;
      imageHeight = drawPixelMaxY - drawPixelMinY + 1;
      if (imageWidth < 0 || imageHeight < 0) {
        return;
      }
      int colorDepth = 8;
      int numColors = colorBar.getNumColors();
      if (numColors > 256) {
        colorDepth = 16;
      }
      if (_model.getBlendingType() == BlendingType.RGB_BLENDING) {
        colorDepth = 32;
        numColors = 32 * 32 * 32;
      }
      updatePixelArray = createImagePixelArray(imageWidth, imageHeight, colorDepth, numColors);
    } catch (Exception ex) {
      throw new RuntimeException("Problem drawing grid " + ex.toString());
    }

    int alpha = (int) (255 * .01f * (100 - _model.getTransparency()));
    boolean smoothImage = _model.getSmoothImage();

    //long t1 = System.currentTimeMillis();
    float nullValue = _grid.getNullValue();
    float[][] values = _grid.getValues();
    boolean[][] nulls = new boolean[nrows][ncols];
    for (int i = 0; i < nrows; i++) {
      for (int j = 0; j < ncols; j++) {
        nulls[i][j] = MathUtil.isEqual(values[i][j], nullValue);
      }
    }
    //    if (updatePixelArray) {
    //      // Limit the # of processors to be in the range 1-3.
    //      int numProcessors = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
    //      numProcessors = Math.min(3, numProcessors);
    //
    //      int numPixelsPerThread = 1 + imageWidth / numProcessors;
    //      int numPixels = 0;
    //      int numPixelsTemp = 0;
    //      List<Thread> threads = new ArrayList<Thread>();
    //      int xPixelStart = drawPixelMinX;
    //      int xPixelEnd = drawPixelMinX;
    //      while (numPixelsTemp < imageWidth) {
    //        xPixelEnd = xPixelStart + numPixelsPerThread - 1;
    //        if (xPixelEnd > drawPixelMaxX) {
    //          xPixelEnd = drawPixelMaxX;
    //        }
    //        numPixelsPerThread = xPixelEnd - xPixelStart + 1;
    //        Grid3dRendererRunnable runnable = new Grid3dRendererRunnable(_canvas, getModelSpace(), _grid, values, nulls,
    //            xPixelStart, xPixelEnd, drawPixelMinX, drawPixelMaxX, drawPixelMinY, drawPixelMaxY, imageWidth, colorBar,
    //            alpha, smoothImage, _imagePixels, _imageAlphas);
    //        Thread t = new Thread(runnable);
    //        threads.add(t);
    //        xPixelStart = xPixelEnd + 1;
    //        numPixelsTemp += numPixelsPerThread;
    //        numPixels++;
    //      }
    //      for (Thread t : threads.toArray(new Thread[0])) {
    //        //System.out.println("START: " + System.currentTimeMillis());
    //        t.start();
    //      }
    //      for (Thread t : threads.toArray(new Thread[0])) {
    //        try {
    //          t.join();
    //        } catch (InterruptedException ex) {
    //          // TODO: How to handle this exception.
    //          // For now, log an error and print the stack trace.
    //          ServiceProvider.getLoggingService().getLogger(Grid3dRenderer.class).error("Thread Error: " + ex.getMessage(),
    //              ex);
    //          ex.printStackTrace();
    //        }
    //      }
    //      //long t2 = System.currentTimeMillis();
    //      //System.out.println("METHOD1: " + (t2 - t1) + " " + threads.size());
    //    }

    if (updatePixelArray) {
      Point2D.Double modelxy = new Point2D.Double();
      int row, col;

      if (blendingType == BlendingType.NONE) {

        // Loop over each pixel.
        for (int px = drawPixelMinX; px <= drawPixelMaxX; px++) {
          for (int py = drawPixelMinY; py <= drawPixelMaxY; py++) {
            // Convert the pixel x,y coordinates to a model x,y coordinates.
            _canvas.transformPixelToModel(modelSpace, px, py, modelxy);
            try {
              double gridx = modelxy.getX();
              double gridy = modelxy.getY();
              // Convert the model x,y coordinates to row,col coordinates.
              double[] rc = _geometry.transformXYToRowCol(gridx, gridy, false);
              if (!smoothImage) {
                // Not smoothing, so round to the nearest integer row,col.
                row = (int) Math.round(rc[0]);
                col = (int) Math.round(rc[1]);
                // If the row,col coordinates are valid, get the value from the Grid.
                if (col >= 0 && col < ncols && row >= 0 && row < nrows) {
                  // If the value is not null, then set the color index of the plot pixel.
                  if (!_grid.isNull(row, col)) {
                    int colorIndex = colorBar.getColorIndex(_grid.getValueAtRowCol(row, col));
                    setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, colorBar, colorIndex,
                        alpha);
                  }
                }
              } else {
                float gridValue = _grid.getValueAtXY(gridx, gridy, false);
                // If the value is not null, then set the color index of the plot pixel.
                if (!_grid.isNull(gridValue)) {
                  int index = colorBar.getColorIndex(gridValue);
                  setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, colorBar, index, alpha);
                }
              }
            } catch (Exception ex) {
              ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString());
            }
          }
        }
      } else if (blendingType == BlendingType.WEIGHTED) {
        Grid3d grid2 = _model.getSecondaryGrid();
        int weighting = _model.getSimpleBlendWeighting();

        // Loop over each pixel.
        for (int px = drawPixelMinX; px <= drawPixelMaxX; px++) {
          for (int py = drawPixelMinY; py <= drawPixelMaxY; py++) {
            // Convert the pixel x,y coordinates to a model x,y coordinates.
            _canvas.transformPixelToModel(modelSpace, px, py, modelxy);
            try {
              double gridx = modelxy.getX();
              double gridy = modelxy.getY();
              // Convert the model x,y coordinates to row,col coordinates.
              double[] rc = _geometry.transformXYToRowCol(gridx, gridy, false);
              if (!smoothImage) {
                // Not smoothing, so round to the nearest integer row,col.
                row = (int) Math.round(rc[0]);
                col = (int) Math.round(rc[1]);
                // If the row,col coordinates are valid, get the value from the Grid.
                if (col >= 0 && col < ncols && row >= 0 && row < nrows) {
                  // If the value is not null, then set the color index of the plot pixel.
                  if (!_grid.isNull(row, col) && !grid2.isNull(row, col)) {
                    float value1 = _grid.getValueAtRowCol(row, col);
                    float value2 = grid2.getValueAtRowCol(row, col);
                    float weight1 = (100f - weighting) / 100f;
                    float weight2 = 1f - weight1;
                    float value = value1 * weight1 + value2 * weight2;
                    int colorIndex = colorBar.getColorIndex(value);
                    setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, colorBar, colorIndex,
                        alpha);
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
                    if (!_grid.isNull(row, col) && !grid2.isNull(row, col)) {
                      float value1 = _grid.getValueAtRowCol(row, col);
                      float value2 = grid2.getValueAtRowCol(row, col);
                      float weight1 = (100f - weighting) / 100f;
                      float weight2 = 1f - weight1;
                      float value = value1 * weight1 + value2 * weight2;
                      gridValue += value * sclArray[m];
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
                    int index = colorBar.getColorIndex(gridValue);
                    setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, index, alpha);
                  }
                }
              }
            } catch (Exception ex) {
              ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString());
            }
          }
        }
      } else if (blendingType == BlendingType.RGB_BLENDING) {
        Grid3d redGrid = _model.getRedChannelGrid();
        Grid3d greenGrid = _model.getGreenChannelGrid();
        Grid3d blueGrid = _model.getBlueChannelGrid();
        float redStart = _model.getRedStartValue();
        float redEnd = _model.getRedEndValue();
        float greenStart = _model.getGreenStartValue();
        float greenEnd = _model.getGreenEndValue();
        float blueStart = _model.getBlueStartValue();
        float blueEnd = _model.getBlueEndValue();
        ByteBuffer buffer = ByteBuffer.allocate(4);

        // Loop over each pixel.
        for (int px = drawPixelMinX; px <= drawPixelMaxX; px++) {
          for (int py = drawPixelMinY; py <= drawPixelMaxY; py++) {
            // Convert the pixel x,y coordinates to a model x,y coordinates.
            _canvas.transformPixelToModel(modelSpace, px, py, modelxy);
            try {
              double gridx = modelxy.getX();
              double gridy = modelxy.getY();
              // Convert the model x,y coordinates to row,col coordinates.
              double[] rc = _geometry.transformXYToRowCol(gridx, gridy, false);
              // if (!smoothImage) {
              // Not smoothing, so round to the nearest integer row,col.
              row = (int) Math.round(rc[0]);
              col = (int) Math.round(rc[1]);
              // If the row,col coordinates are valid, get the value from the Grid.
              if (col >= 0 && col < ncols && row >= 0 && row < nrows) {
                // If the value is not null, then set the color index of the plot pixel.
                if (!redGrid.isNull(row, col) && !greenGrid.isNull(row, col) && !blueGrid.isNull(row, col)) {
                  boolean showRedChannel = _model.getRedChannelFlag();
                  boolean showGreenChannel = _model.getGreenChannelFlag();
                  boolean showBlueChannel = _model.getBlueChannelFlag();
                  float redValue = redGrid.getValueAtRowCol(row, col);
                  float greenValue = greenGrid.getValueAtRowCol(row, col);
                  float blueValue = blueGrid.getValueAtRowCol(row, col);
                  float redPercent = (redValue - redStart) / (redEnd - redStart);
                  if (showRedChannel) {
                    showRedChannel = redPercent >= 0 && redPercent <= 1;
                  }
                  redPercent = Math.max(0, redPercent);
                  redPercent = Math.min(1, redPercent);
                  float greenPercent = (greenValue - greenStart) / (greenEnd - greenStart);
                  if (showGreenChannel) {
                    showGreenChannel = greenPercent >= 0 && greenPercent <= 1;
                  }
                  greenPercent = Math.max(0, greenPercent);
                  greenPercent = Math.min(1, greenPercent);
                  float bluePercent = (blueValue - blueStart) / (blueEnd - blueStart);
                  if (showBlueChannel) {
                    showBlueChannel = bluePercent >= 0 && bluePercent <= 1;
                  }
                  bluePercent = Math.max(0, bluePercent);
                  bluePercent = Math.min(1, bluePercent);
                  int iredValue = (int) (redPercent * 255);
                  int igreenValue = (int) (greenPercent * 255);
                  int iblueValue = (int) (bluePercent * 255);
                  if (!showRedChannel) {
                    iredValue = 0;
                  }
                  if (!showGreenChannel) {
                    igreenValue = 0;
                  }
                  if (!showBlueChannel) {
                    iblueValue = 0;
                  }
                  RGB rgb = new RGB(iredValue, igreenValue, iblueValue);
                  int color = rgb.red << 16 | rgb.green << 8 | rgb.blue << 0;
                  buffer.position(0);
                  buffer.putInt(color);
                  setColorIndexOfPixel32Bits(px - drawPixelMinX, py - drawPixelMinY, imageWidth, buffer.array(), alpha,
                      false);
                }
              }
            } catch (Exception ex) {
              ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString());
            }
          }
        }
      }
      //long t2 = System.currentTimeMillis();
      //System.out.println("METHOD2: " + (t2 - t1));
    }

    // Create new actual image for drawing from the pixel byte array.
    int colorDepth = 8;
    if (colorBar.getNumColors() >= 256) {
      colorDepth = 16;
    }
    ImageData imageData = null;
    if (blendingType == BlendingType.RGB_BLENDING) {
      colorDepth = 32;
    }
    if (colorDepth == 8) {
      PaletteData palette = new PaletteData(buildColorModel(colorBar));
      imageData = new ImageData(imageWidth, imageHeight, colorDepth, palette, 1, _imagePixels);
    } else {
      PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
      imageData = new ImageData(imageWidth, imageHeight, colorDepth, palette, _numBytesPerPixel, _imagePixels);
    }
    imageData.alphaData = _imageAlphas;
    Image image = new Image(_canvas.getComposite().getDisplay(), imageData);
    if (_plotImage == null) {
      _plotImage = new PlotImage(image, "", ImageAnchor.UpperLeft, cornerPoint1);
      _plotImage.setRenderLevel(RenderLevel.IMAGE_OVER_GRID);
      _plotImage.setVisible(_isVisible);
      addShape(_plotImage);
    } else {
      Image oldImage = _plotImage.getImage();
      _plotImage.blockUpdate();
      _plotImage.setImage(image);
      _plotImage.getPoint(0).moveTo(cornerPoint1.getX(), cornerPoint1.getY());
      _plotImage.unblockUpdate();
      //_plotImage.getPoint(1).moveTo(cornerPoint2.getX(), cornerPoint2.getY());
      if (oldImage != null) {
        oldImage.dispose();
      }
    }

    if (_model.getShadedRelief()) {
      Image shadedImage = recomputeShadedImage(imageWidth, imageHeight, drawPixelMinX, drawPixelMaxX, drawPixelMinY,
          drawPixelMaxY);
      if (_shadedImage == null) {
        _shadedImage = new PlotImage(shadedImage, "", ImageAnchor.UpperLeft, cornerPoint1);
        _shadedImage.setRenderLevel(RenderLevel.IMAGE_OVER_GRID);
        _shadedImage.setVisible(_isVisible);
        addShape(_shadedImage);
      } else {
        Image oldImage = _shadedImage.getImage();
        _shadedImage.blockUpdate();
        _shadedImage.getPoint(0).moveTo(cornerPoint1.getX(), cornerPoint1.getY());
        //_shadedImage.getPoint(1).moveTo(cornerPoint2.getX(), cornerPoint2.getY());
        _shadedImage.setImage(shadedImage);
        _shadedImage.unblockUpdate();
        if (oldImage != null) {
          oldImage.dispose();
        }
      }
      _shadedImage.setVisible(_isVisible);

    } else {
      if (_shadedImage != null) {
        _shadedImage.setVisible(false);
      }
    }

    _updateRendering = false;
    super.updated();
  }

  private float[] computeSunVector(final double elevation, final double azimuth) {
    double x = Math.sin(Math.toRadians(azimuth));
    double y = Math.cos(Math.toRadians(azimuth));
    double z = Math.tan(Math.sqrt(x * x + y * y) * Math.toRadians(elevation));
    return normalize(new float[] { (float) x, (float) y, (float) z });
  }

  private float[] normalize(final float[] vector) {
    float u = vector[0];
    float v = vector[1];
    float w = vector[2];
    float norm = (float) Math.sqrt(u * u + v * v + w * w);
    return new float[] { u / norm, v / norm, w / norm };
  }

  public Image recomputeShadedImage(final int imageWidth, final int imageHeight, final int px0, final int px1,
      final int py0, final int py1) {

    // lazily initialize the shading data structures. 
    if (_dx == null) {
      _z = _grid.getValues();
      _dx = computeDzPerDx(_z);
      _dy = computeDzPerDy(_z);
      computeNormals();
      _cosines = new float[_z.length][_z[0].length];
    }

    // convert from elevation and azumuth into vector
    LightSourceModel lightSource = getViewer().getLightSourceModel();
    float[] sun = computeSunVector(lightSource.getElevation(), lightSource.getAzimuth());

    // compute the angles between the sun and the precomputed surface normals. 
    computeCosines(_dx, _dy, _dz, sun);

    IModelSpace modelSpace = getModelSpace();
    Point2D.Double modelPoint = new Point2D.Double();
    for (int px = px0; px <= px1; px++) {
      for (int py = py0; py <= py1; py++) {
        _canvas.transformPixelToModel(modelSpace, px, py, modelPoint);
        double[] rc = _geometry.transformXYToRowCol(modelPoint.getX(), modelPoint.getY(), true);
        int row = (int) Math.round(rc[0]);
        int col = (int) Math.round(rc[1]);
        int pndx = (py - py0) * imageWidth + px - px0;
        if (row >= 0 && row < _grid.getNumRows() && col >= 0 && col < _grid.getNumColumns()) {
          if (MathUtil.isEqual(_cosines[row][col], _grid.getNullValue())) {
            _shadedPixels[pndx] = GRAY_NULL;
            //_shadedPixels[pndx] = (byte)_shadedColorBar.getNumColors();
          } else {
            assert _cosines[row][col] >= -1.1 && _cosines[row][col] <= 1.1 : row + " " + col + " " + _cosines[row][col]
                + " " + _dx[row][col] + " " + _dy[row][col];
            // black is 0 white is 128
            _shadedPixels[pndx] = (byte) (38 + Math.round(90 * (1.0f + _cosines[row][col])) / 2.0f);
            //_shadedPixels[pndx] = (byte)(Math.round(_shadedColorBar.getNumColors() * (1.0f + _cosines[row][col])) / 2.0f);
          }
        } else {
          _shadedPixels[pndx] = GRAY_NULL;
          //_shadedPixels[pndx] = (byte)_shadedColorBar.getNumColors();
        }
      }
    }

    // Create new actual image for drawing from the pixel byte array.
    if (_grayscalePalette == null) {
      GrayscaleColorMap grayscale = new GrayscaleColorMap();
      _grayscalePalette = new PaletteData(grayscale.getRGBs(129));
    }
    int depth = 8;
    ImageData imageData = new ImageData(imageWidth, imageHeight, depth, _grayscalePalette, 1, _shadedPixels);
    imageData.alpha = 128;
    Image shadedImageBuffer = new Image(_canvas.getComposite().getDisplay(), imageData);

    //Image shadedImage = Toolkit.getDefaultToolkit().createImage(
    //    new MemoryImageSource(pw, ph, buildColorModel(_shadedColorBar), _shadedPixels, 0, pw));
    //WritableRaster raster = _shadedImageBuffer.
    //raster.setDataElements(0, 0, pw, ph, _shadedPixels);

    return shadedImageBuffer;
  }

  /**
   * Creates the image pixel array (if necessary) of the Grid.
   * @param width the image width.
   * @param height the image height.
   * @return <i>true</i> if a new image was created; <i>false</i> if not.
   * @throws Exception
   */
  private boolean createImagePixelArray(final int width, final int height, final int colorDepth, final int numColors) throws Exception {
    int numPixels = width * height;
    _numBytesPerPixel = colorDepth / 8;
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.putInt(numColors);
    byte[] bytes = buffer.array();
    if (_updateRendering || _imagePixels == null || numPixels * _numBytesPerPixel != _imagePixels.length) {
      _imagePixels = new byte[numPixels * _numBytesPerPixel];
      _imageAlphas = new byte[numPixels];
      _shadedPixels = new byte[numPixels];
      for (int p = 0; p < numPixels; p++) {
        for (int j = 0; j < _numBytesPerPixel; j++) {
          _imagePixels[p] = (byte) numColors;
        }
        _imageAlphas[p] = FULLY_TRANSPARENT;
        _shadedPixels[p] = GRAY_NULL;
      }

      return true;
    }
    return false;
  }

  private float[][] computeDzPerDx(final float[][] grid) {
    float[][] dx = new float[_grid.getNumRows()][_grid.getNumColumns()];

    // Null out around the edges where there is not enough data to compute gradient.
    for (int row = 0; row < _grid.getNumRows(); row++) {
      for (int col = 0; col < _grid.getNumColumns(); col++) {
        dx[row][col] = _grid.getNullValue();
      }
    }

    // Compute.
    for (int row = 1; row < _grid.getNumRows() - 1; row++) {
      for (int col = 1; col < _grid.getNumColumns() - 1; col++) {

        if (MathUtil.isEqual(grid[row][col + 1], _grid.getNullValue())
            || MathUtil.isEqual(grid[row][col], _grid.getNullValue())
            || MathUtil.isEqual(grid[row][col - 1], _grid.getNullValue())) {
          dx[row][col] = _grid.getNullValue();
        } else {
          dx[row][col] = (grid[row][col + 1] - grid[row][col - 1]) / 2.0f;
        }
      }
    }

    return dx;
  }

  protected float[][] computeDzPerDy(final float[][] grid) {
    float[][] dy = new float[_grid.getNumRows()][_grid.getNumColumns()];

    // Null out around the edges where there is not enough data to compute gradient.
    for (int row = 0; row < _grid.getNumRows(); row++) {
      for (int col = 0; col < _grid.getNumColumns(); col++) {
        dy[row][col] = _grid.getNullValue();
      }
    }

    // Compute.
    for (int row = 1; row < _grid.getNumRows() - 1; row++) {
      for (int col = 1; col < _grid.getNumColumns() - 1; col++) {
        if (MathUtil.isEqual(grid[row + 1][col], _grid.getNullValue())
            || MathUtil.isEqual(grid[row][col], _grid.getNullValue())
            || MathUtil.isEqual(grid[row - 1][col], _grid.getNullValue())) {
          dy[row][col] = _grid.getNullValue();
        } else {
          dy[row][col] = (grid[row + 1][col] - grid[row - 1][col]) / 2.0f;
        }
      }
    }

    return dy;
  }

  /**
   * The vector for the normal to the surface is [dx, dy, 1] and we
   * want avoid having to compute ||u|| later on. So we normalize
   * now so that when we move the sun we know that ||u|| = 1.
   *
   * compute vector norm to the surface using the cross product
   * along x axis we have A = 0,0,0 --> 1, 0, dx
   * along y axis we have B = 0,0,0 --> 0, 1, dy
   * cross product is A x B = Ay*Bz - Az*By, Az*Bx - Ax*Bz, Ax*By - Ay*Bx
   *
   * We compute the normalized z vector here also so we can compute
   * the direction cosine faster when the sun is moved. This trades
   * off memory for speed.
   *
   */
  private void computeNormals() {

    if (_dz != null) {
      throw new IllegalStateException("Should only compute this once.");
    }

    _dz = new float[_grid.getNumRows()][_grid.getNumColumns()];

    for (int row = 1; row < _grid.getNumRows() - 1; row++) {
      for (int col = 1; col < _grid.getNumColumns() - 1; col++) {

        if (MathUtil.isEqual(_dx[row][col], _grid.getNullValue())
            || MathUtil.isEqual(_dy[row][col], _grid.getNullValue())) {
          _dx[row][col] = _grid.getNullValue();
          _dy[row][col] = _grid.getNullValue();
          _dz[row][col] = _grid.getNullValue();
        } else {
          float length = (float) Math.sqrt(_dx[row][col] * _dx[row][col] + _dy[row][col] * _dy[row][col] + 1);
          _dx[row][col] = _dx[row][col] / length;
          _dy[row][col] = _dy[row][col] / length;
          _dz[row][col] = 1 / length;
        }
      }
    }
  }

  /**
   * Use the dot product to determine the angle between the normal to each
   * grid cell and the sun.
   *
   * @param u1 x component of the normal (no longer gradient)
   * @param u2 y component of the normal
   * @param u3 z component of the normal
   * @param sun vector for the sun ray
   */
  private void computeCosines(final float[][] u1, final float[][] u2, final float[][] u3, final float[] sun) {

    // normalized vector for the sun's location
    float v1 = sun[0];
    float v2 = sun[1];
    float v3 = sun[2];

    for (int row = 0; row < _grid.getNumRows(); row++) {
      for (int col = 0; col < _grid.getNumColumns(); col++) {

        if (MathUtil.isEqual(u1[row][col], _grid.getNullValue())
            || MathUtil.isEqual(u2[row][col], _grid.getNullValue())) {
          _cosines[row][col] = _grid.getNullValue();
        } else {
          // use dot product to find the angle between sun and the normal
          // u.v = u1v1 + u2v2 + u3v3
          // cos thi = u.v / ||u|| ||v||
          _cosines[row][col] = u1[row][col] * v1 + u2[row][col] * v2 + u3[row][col] * v3;
        }
      }
    }
  }

  /**
   * Builds the color model for the plot image.
   * @param colorBar the colorbar to use.
   * @return the color model.0
   */
  private RGB[] buildColorModel(final ColorBar colorBar) {
    int numColors = colorBar.getNumColors();
    RGB[] rgbs = new RGB[numColors + 1];
    for (int i = 0; i < numColors; i++) {
      rgbs[i] = colorBar.getColor(i);
    }
    rgbs[numColors] = _canvas.getComposite().getBackground().getRGB();
    return rgbs;
  }

  /**
   * Sets the color index of the specified image pixel.
   * @param x the x pixel.
   * @param y the y pixel.
   * @param width the image width in pixels.
   * @param clrIndex the color index.
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

  /**
   * Sets the color index of the specified image pixel.
   * @param x the x pixel.
   * @param y the y pixel.
   * @param width the image width in pixels.
   * @param clrIndex the color index.
   */
  private void setColorIndexOfPixel(final int x, final int y, final int width, final int clrIndex, final int alpha) {
    int numColors = 32 * 32 * 32;
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

  /**
   * Sets the color index of the specified image pixel.
   * @param x the x pixel.
   * @param y the y pixel.
   * @param width the image width in pixels.
   * @param clrIndex the color index.
   */
  private void setColorIndexOfPixel32Bits(final int x, final int y, final int width, final byte[] colorBytes,
      final int alpha, final boolean isTransparent) {
    int pixelIndex = y * width + x;
    if (!isTransparent) {
      for (int j = 0; j < _numBytesPerPixel; j++) {
        _imagePixels[pixelIndex * _numBytesPerPixel + j] = colorBytes[j];
      }
      _imageAlphas[pixelIndex] = (byte) alpha;
    } else {
      _imageAlphas[pixelIndex] = FULLY_TRANSPARENT;
    }
  }

  private Image createImage(final Device device, final int width, final int height) {
    return new Image(device, width, height);
    // return new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {

    int nrows = _grid.getNumRows();
    int ncols = _grid.getNumColumns();
    double[] rc = _geometry.transformXYToRowCol(x, y, true);
    Unit units = _grid.getDataUnit();

    List<String> keys = new ArrayList<String>();
    List<String> vals = new ArrayList<String>();

    if (rc[0] >= 0 && rc[0] < nrows && rc[1] >= 0 && rc[1] < ncols) {
      keys.addAll(Generics.asList("Row", "Column", "Amplitude"));
      int row = (int) Math.round(rc[0]);
      int col = (int) Math.round(rc[1]);

      String zVal = "Null";

      if (!_grid.isNull(row, col)) {
        zVal = _grid.getValueAtRowCol(row, col) + units.getSymbol();
      }

      vals.addAll(Generics.asList("" + row, "" + col, "" + zVal));
    }

    return new ReadoutInfo(_grid.toString(), keys, vals);
  }

  @Override
  public void dispose() {
    if (_plotImage != null) {
      Image image = _plotImage.getImage();
      if (image != null && !image.isDisposed()) {
        image.dispose();
      }
    }
    if (_shadedImage != null) {
      Image image = _shadedImage.getImage();
      if (image != null && !image.isDisposed()) {
        image.dispose();
      }
    }
    if (_lightSourceListener != null) {
      getViewer().getLightSourceModel().removeListener(_lightSourceListener);
    }
    //Widget widget = _canvas.getComposite();
    //if (!widget.isDisposed()) {
    //  _canvas.removeControlListener(this);
    //  ((ModelSpaceCanvas) _canvas).removeMouseMoveListener(this);
    //}
    super.dispose();
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)) {
      _updateRendering = true;
      renderImage();
    }
  }

  @Override
  public void colorsChanged(final ColorMapEvent event) {
    ColorBar colorBarOld = _model.getColorBar();
    if (event.getColorMapModel().getNumColors() != colorBarOld.getNumColors()) {
      _updateRendering = true;
      ColorBar colorBar = new ColorBar(event.getColorMapModel(), colorBarOld.getStartValue(),
          colorBarOld.getEndValue(), colorBarOld.getStepValue());
      _model.setColorBar(colorBar);
      colorBarOld.dispose();
      renderImage();
      return;
    }
    _model.getColorBar().setColors(event.getColorMapModel().getColors());
    renderImage();
  }

  public void updateRendererModel(final Grid3dRendererModel model) {
    if (model.getTransparency() != _model.getTransparency()) {
      _updateRendering = true;
    }
    _model = new Grid3dRendererModel(model);
    _model.setColorBar(new ColorBar(model.getColorBar()));
    redraw(false);
  }

  //  public void mouseMove(final MouseEvent event) {
  //    if (!_model.getShadedRelief()) {
  //      return;
  //    }
  //
  //    int midX = _canvas.getSize().x / 2;
  //    int midY = _canvas.getSize().y / 2;
  //
  //    float maxDist = Math.min(midX, midY);
  //
  //    float u = event.x - midX;
  //    float v = midY - event.y;
  //
  //    float distance = (float) Math.sqrt(u * u + v * v);
  //
  //    float ratio = Math.min(1, distance / maxDist);
  //
  //    float elev = (float) (3.14159 / 2 * (1 - ratio));
  //
  //    float w = (float) (Math.atan(elev) * Math.sqrt(u * u + v * v));
  //
  //    setSunAzimuth(new float[] { u, v, w });
  //
  //    // System.out.println(midX + " " + midY + " [" + e.getX() + " " + e.getY() + "] " + elev + " [" + u + " " + v + " " + w + "]");
  //
  //    renderImage();
  //  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    double[] rowcol = _grid.getGeometry().transformXYToRowCol(x, y, true);
    int row = (int) Math.round(rowcol[0]);
    int col = (int) Math.round(rowcol[1]);
    if (row >= 0 && row < _grid.getNumRows() && col >= 0 && col < _grid.getNumColumns() && !_grid.isNull(row, col)) {
      DataSelection selection = new DataSelection(getClass().getSimpleName());
      selection.setSelectedObjects(new Object[] { _grid });
      return selection;
    }
    return null;
  }

  @Override
  protected void addPopupMenuActions() {
    Shell shell = new Shell(getShell());
    Grid3dRendererDialog dialog = new Grid3dRendererDialog(shell, _grid.getDisplayName(), this, _grid);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addPlotShapes() {
    _canvas = getViewer().getPlot().getModelSpaceCanvas();

    // Draw the grid outline.
    RGB color = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE).getRGB();
    _outline = new PlotPolygon();
    _outline.setName(getName() + " Bounds");
    _outline.clear();
    _outline.setTextColor(color);
    _outline.setPointColor(new RGB(255, 255, 255));
    _outline.setPointSize(1);
    _outline.setPointStyle(PointStyle.NONE);
    _outline.setLineColor(color);
    _outline.setLineWidth(1);
    _outline.setLineStyle(LineStyle.SOLID);
    _outline.setFillStyle(FillStyle.NONE);
    Point3d[] points = _grid.getCornerPoints().getPointsDirect();
    for (Point3d point : points) {
      IPlotPoint plotPoint = new PlotPoint(point.getX(), point.getY(), point.getZ());
      plotPoint.setPropertyInheritance(true);
      _outline.addPoint(plotPoint);
    }
    addShape(_outline);
    //    PlotViewLayer viewLayer = new PlotViewLayer(GridRenderer.this, true, false, true);
    //    viewer.addLayer(viewLayer.getPlotLayer(), true, true);
    //    viewer.getLayerModel().addLayer(viewLayer, viewer.getDefaultLayerForName("Grids"));

    //    Display.getDefault().syncExec(new Runnable() {
    //
    //      public void run() {
    //        _canvas.getComposite().addControlListener(GridRenderer.this);
    //        ((ModelSpaceCanvas) _canvas).addMouseMoveListener(GridRenderer.this);
    //      }
    //    });
    //IModelSpace modelSpace = _viewer.getActiveModelSpace();
    //modelSpace.addListener(this, true);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.GRID_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _grid = (Grid3d) objects[0];
    _geometry = _grid.getGeometry();
    _model.setColorBarRange(_grid);
    _model.setPrimaryGrid(_grid);

    // Create a listener for the light source model shared across all map viewers.
    _lightSourceListener = new IModelListener() {

      @Override
      public void propertyChanged(final String key) {
        Display.getDefault().asyncExec(new Runnable() {

          public void run() {
            // If the light source model has changed, and the renderer is
            // using shaded relief, then trigger a redraw.
            if (_model.getShadedRelief()) {
              _updateRendering = true;
              renderImage();
            }
          }
        });
      }

    };
    getViewer().getLightSourceModel().addListener(_lightSourceListener);
  }

  @Override
  protected void setNameAndImage() {
    IRepository r = ServiceProvider.getRepository();
    String varName = r.lookupVariableName(_grid);
    if (varName.equals(IRepository.UNKNOWN_VARIABLE)) {
      setName(_grid.getDisplayName());
    } else {
      setName(varName + "=" + _grid.getDisplayName());
    }
    setImage(ModelUI.getSharedImages().getImage(_grid));
  }

  @Override
  public Grid3dRendererModel getSettingsModel() {
    return _model;
  }

}