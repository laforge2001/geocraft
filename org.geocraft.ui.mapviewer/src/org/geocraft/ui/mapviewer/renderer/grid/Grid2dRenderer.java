/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.grid;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.geometry.LineGeometry;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.ui.color.ColorBarEditorListener;
import org.geocraft.ui.mapviewer.MapViewRenderer;
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
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotImage;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.ViewLayerEvent;


/**
 * A simple implementation for rendering a <code>Grid2d</code> entity in the map viewer.
 */
public class Grid2dRenderer extends MapViewRenderer implements ControlListener, ColorMapListener,
    ColorBarEditorListener {

  /** The grid to render. */
  protected Grid2d _grid;

  /** The plot image for the grid. */
  protected IPlotImage _plotImage;

  /** The byte array of image pixels. */
  protected byte[] _imagePixels;

  /** Status flag for updating the rendering. */
  protected boolean _updateRendering;

  protected IPlotPolygon _outline;

  /** The model of rendering properties. */
  protected Grid2dRendererModel _model;

  /** The model space canvas in which to render the grid. */
  protected IModelSpaceCanvas _canvas;

  /**
   * The no-argument constructor for OSGI.
   */
  public Grid2dRenderer() {
    super("Grid2d Renderer");
    _appendCursorInfo = true;
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
    redraw(true);
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
      _model.getColorBar().setStartValue(_grid.getMinValue());
      _model.getColorBar().setEndValue(_grid.getMaxValue());
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
   * Renders the grid layer image.
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

    // Determine the min/max x,y extents of the grid.
    double xmin = Double.MAX_VALUE;
    double xmax = -Double.MAX_VALUE;
    double ymin = Double.MAX_VALUE;
    double ymax = -Double.MAX_VALUE;
    GridGeometry2d gridGeometry = _grid.getGridGeometry();
    for (LineGeometry lineGeometry : gridGeometry.getLines()) {
      Point3d[] cornerPoints = lineGeometry.getPoints().getPointsDirect();
      for (Point3d cornerPoint : cornerPoints) {
        xmin = Math.min(xmin, cornerPoint.getX());
        xmax = Math.max(xmax, cornerPoint.getX());
        ymin = Math.min(ymin, cornerPoint.getY());
        ymax = Math.max(ymax, cornerPoint.getY());
      }
    }

    // Determine the min/max pixel extents of the grid.
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

    ColorBar colorBar = _model.getColorBar();
    // Create a new image if necessary (e.g. the plot was resized).
    boolean updatePixelArray = false;
    try {
      imageWidth = drawPixelMaxX - drawPixelMinX + 1;
      imageHeight = drawPixelMaxY - drawPixelMinY + 1;
      if (imageWidth < 0 || imageHeight < 0) {
        return;
      }
      updatePixelArray = createImagePixelArray(imageWidth, imageHeight, colorBar);
    } catch (Exception ex) {
      throw new RuntimeException("Problem drawing grid " + ex.toString());
    }
    if (updatePixelArray) {
      Point2D.Double pixelxy = new Point2D.Double();

      // Loop thru each line in the grid.
      for (LineGeometry lineGeometry : gridGeometry.getLines()) {
        int lineNumber = lineGeometry.getNumber();
        float[] values = _grid.getValues(lineNumber);

        int pxPrev = 0;
        int pyPrev = 0;

        // Loop thru each bin in the line.
        int numBins = lineGeometry.getNumBins();
        for (int bin = 0; bin < numBins; bin++) {
          double[] xy = lineGeometry.transformBinToXY(bin);
          double x = xy[0];
          double y = xy[1];
          _canvas.transformModelToPixel(modelSpace, x, y, pixelxy);
          int px = Math.round((float) pixelxy.x);
          int py = Math.round((float) pixelxy.y);
          if (px >= drawPixelMinX && px <= drawPixelMaxX && py >= drawPixelMinY && py <= drawPixelMaxY) {
            float value = values[bin];
            if (!_grid.isNull(value)) {
              int colorIndex = colorBar.getColorIndex(value);
              setColorIndexOfPixel(px - drawPixelMinX, py - drawPixelMinY, imageWidth, colorBar, colorIndex);
            }
          }

          // Interpolate between the bins.
          if (bin > 0) {
            int pxDiff = px - pxPrev;
            int pyDiff = py - pyPrev;
            float pxDelta = 0;
            float pyDelta = 0;
            int maxDiff = 0;
            if (Math.abs(pxDiff) >= Math.abs(pyDiff)) {
              maxDiff = Math.abs(pxDiff);
              pxDelta = pxDiff / (float) maxDiff;
              pyDelta = pyDiff / (float) maxDiff;
            } else {
              maxDiff = Math.abs(pyDiff);
              pyDelta = pyDiff / (float) maxDiff;
              pxDelta = pxDiff / (float) maxDiff;
            }
            for (int i = 0; i < maxDiff; i++) {
              int pxTemp = (int) (pxPrev + i * pxDelta);
              int pyTemp = (int) (pyPrev + i * pyDelta);
              if (pxTemp >= drawPixelMinX && pxTemp <= drawPixelMaxX && pyTemp >= drawPixelMinY
                  && pyTemp <= drawPixelMaxY) {
                float value = _grid.getNullValue();
                if (i < maxDiff / 2) {
                  value = values[bin - 1];
                } else {
                  value = values[bin];
                }
                if (!_grid.isNull(value)) {
                  int colorIndex = colorBar.getColorIndex(value);
                  setColorIndexOfPixel(pxTemp - drawPixelMinX, pyTemp - drawPixelMinY, imageWidth, colorBar, colorIndex);
                }
              }
            }
          }
          pxPrev = px;
          pyPrev = py;
        }
      }
    }

    // Create new actual image for drawing from the pixel byte array.
    PaletteData palette = new PaletteData(buildColorModel(colorBar));
    int depth = 8;
    if (colorBar.getNumColors() >= 256) {
      depth = 16;
    }
    ImageData imageData = new ImageData(imageWidth, imageHeight, depth, palette, 1, _imagePixels);
    imageData.alpha = (int) (255 * .01f * (100 - _model.getTransparency()));
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

    _updateRendering = false;
    super.updated();
  }

  /**
   * Creates the image pixel array (if necessary) of the grid.
   * 
   * @param width the image width.
   * @param height the image height.
   * @return <i>true</i> if a new image was created; <i>false</i> if not.
   * @throws Exception
   */
  private boolean createImagePixelArray(final int width, final int height, final ColorBar colorBar) throws Exception {
    int numPixels = width * height;
    if (_updateRendering || _imagePixels == null || numPixels != _imagePixels.length) {
      _imagePixels = new byte[numPixels];
      for (int p = 0; p < numPixels; p++) {
        _imagePixels[p] = (byte) colorBar.getNumColors();
      }
      return true;
    }
    return false;
  }

  /**
   * Builds the color model for the plot image.
   * 
   * @param colorBar the colorbar to use.
   * @return the color model.
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
   * @param colorIndex the color index.
   */
  private void setColorIndexOfPixel(final int x, final int y, final int width, final ColorBar colorBar,
      final int clrIndex) {
    int numColors = colorBar.getNumColors();
    int colorIndex = Math.max(clrIndex, 0);
    colorIndex = Math.min(colorIndex, numColors - 1);
    if (colorIndex >= 0 && colorIndex < numColors) {
      int pixelIndex = y * width + x;
      _imagePixels[pixelIndex] = (byte) colorIndex;
    }
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {

    //    int nrows = _grid.getNumRows();
    //    int ncols = _grid.getNumColumns();
    //    double[] rc = _geometry.transformXYToRowCol(x, y, true);
    //    Unit units = _grid.getDataUnit();

    List<String> keys = new ArrayList<String>();
    List<String> vals = new ArrayList<String>();

    //    if (rc[0] >= 0 && rc[0] < nrows && rc[1] >= 0 && rc[1] < ncols) {
    //      keys.addAll(Generics.asList("Row", "Column", "Amplitude"));
    //      int row = (int) Math.round(rc[0]);
    //      int col = (int) Math.round(rc[1]);
    //
    //      String zVal = "Null";
    //
    //      if (!_grid.isNull(row, col)) {
    //        zVal = _grid.getValueAtRowCol(row, col) + units.getSymbol();
    //      }
    //
    //      vals.addAll(Generics.asList("" + row, "" + col, "" + zVal));
    //    }

    return new ReadoutInfo(_grid.toString(), keys, vals);
  }

  @Override
  public void dispose() {
    if (_plotImage != null) {
      Image image = _plotImage.getImage();
      if (image != null) {
        image.dispose();
      }
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

  public Grid2dRendererModel getSettingsModel() {
    return _model;
  }

  public void updateSettings(final Grid2dRendererModel model) {
    if (model.getTransparency() != _model.getTransparency()) {
      _updateRendering = true;
    }
    _model.updateFrom(model);
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
    //    double[] rowcol = _grid.getGeometry().transformXYToRowCol(x, y, true);
    //    int row = (int) Math.round(rowcol[0]);
    //    int col = (int) Math.round(rowcol[1]);
    //    if (row >= 0 && row < _grid.getNumRows() && col >= 0 && col < _grid.getNumColumns() && !_grid.isNull(row, col)) {
    //      DataSelection selection = new DataSelection(getClass().getSimpleName());
    //      selection.setSelectedObjects(new Object[] { _grid });
    //      return selection;
    //    }
    return null;
  }

  @Override
  protected void addPopupMenuActions() {
    _model = new Grid2dRendererModel(_grid);
    Grid2dRendererDialog dialog = new Grid2dRendererDialog(getShell(), _grid.toString(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addPlotShapes() {
    _canvas = getViewer().getPlot().getModelSpaceCanvas();

    // Find the minimum and maximum x,y coordinates.
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    GridGeometry2d gridGeometry = _grid.getGridGeometry();
    for (LineGeometry lineGeometry : gridGeometry.getLines()) {
      Point3d[] points = lineGeometry.getPoints().getPointsDirect();
      for (Point3d point : points) {
        minX = Math.min(minX, point.getX());
        maxX = Math.max(maxX, point.getX());
        minY = Math.min(minY, point.getY());
        maxY = Math.max(maxY, point.getY());
      }
    }

    // Create an invisible rectangular outline of the grid bounds.
    // This is done for auto-adjustment of the map view when the renderer is added.
    RGB color = new RGB(255, 255, 255);
    _outline = new PlotPolygon();
    _outline.setName(getName() + " Bounds");
    _outline.clear();
    _outline.setTextColor(color);
    _outline.setPointColor(new RGB(0, 255, 0));
    _outline.setPointSize(0);
    _outline.setPointStyle(PointStyle.NONE);
    _outline.setLineColor(color);
    _outline.setLineWidth(0);
    _outline.setLineStyle(LineStyle.NONE);
    _outline.setFillStyle(FillStyle.NONE);
    _outline.setVisible(false);

    double[] xs = { minX, maxX, maxX, minX };
    double[] ys = { minY, minY, maxY, maxY };
    for (int i = 0; i < 4; i++) {
      IPlotPoint plotPoint = new PlotPoint(xs[i], ys[i], 0);
      plotPoint.setPropertyInheritance(true);
      _outline.addPoint(plotPoint);
    }
    addShape(_outline);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.GRID_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _grid = (Grid2d) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_grid);
    setImage(ModelUI.getSharedImages().getImage(_grid));
  }
}
