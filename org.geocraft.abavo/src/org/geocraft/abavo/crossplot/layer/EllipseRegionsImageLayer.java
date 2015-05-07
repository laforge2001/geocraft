/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.layer;


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
import org.eclipse.swt.widgets.Widget;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.ClassBackgroundColorMap;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.ellipse.EllipseRegionsClassifier;
import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent;
import org.geocraft.abavo.ellipse.EllipseRegionsModelListener;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.color.ColorMapModel;
import org.geocraft.core.color.map.IColorMap;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.ui.plot.defs.ImageAnchor;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotImage;
import org.geocraft.ui.plot.object.PlotImage;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.ViewLayerEvent;
import org.geocraft.ui.viewer.layer.ViewLayerListener;


public class EllipseRegionsImageLayer extends PlotLayer implements ViewLayerListener, EllipseRegionsModelListener,
    ControlListener, ColorMapListener {

  /** The array of class names (used for cursor info). */
  protected String[] _classNames = { "I", "II", "III", "IV" };

  /** The associated crossplot. */
  protected IABavoCrossplot _crossplot;

  /** The class background process. */
  protected EllipseRegionsClassifier _classBkg;

  //protected EllipseRegionsClassBackgroundLayerEditor _editor;

  /** The image of class regions. */
  protected Image _image;

  /** The plot image. */
  protected IPlotImage _plotImage;

  /** The color map used in display of the image. */
  protected IColorMap _colorMap;

  /** The color bar used in display of the image. */
  protected ColorBar _colorBar;

  /** The array of image pixels (stored for performance). */
  protected byte[] _imagePixels;

  /** The flag indicating the image rendering needs updating. */
  protected boolean _updateRendering;

  public EllipseRegionsImageLayer(final IABavoCrossplot crossplot) {
    super("Regions Image");
    _crossplot = crossplot;
    _updateRendering = true;
    _colorMap = new ClassBackgroundColorMap();
    _colorBar = new ColorBar(new ColorMapModel(64, _colorMap), -128, 128, 4);
    _colorBar.setReversedRange(true);
    _crossplot.getEllipseRegionsModel().addEllipseModelListener(this);
    _crossplot.getModelSpaceCanvas().addControlListener(this);

    //_appendCursorInfo = true;

    //_actions.clear();
    //_actions.add(new ViewLayerEditorAction(this, "Edit Color Bar..."));
  }

  @Override
  public Image createImage() {
    return Activator.getDefault().createImage(ABavoImages.REGIONS_IMAGE);
  }

  public void redraw() {
    _updateRendering = true;
    renderImage();
    super.updated();
  }

  @Override
  public void refresh() {
    renderImage();
  }

  private synchronized void renderImage() {
    if (_modelSpace == null || _crossplot.getModelSpaceCanvas().getComposite().isDisposed()) {
      return;
    }
    _crossplot.setCursorStyle(SWT.CURSOR_WAIT);

    ModelSpaceBounds bounds = _modelSpace.getViewableBounds();
    double xStart = bounds.getStartX();
    double xEnd = bounds.getEndX();
    double yStart = bounds.getStartY();
    double yEnd = bounds.getEndY();
    if (Double.isNaN(xStart) || Double.isNaN(xEnd) || Double.isNaN(yStart) || Double.isNaN(yEnd)) {
      return;
    }

    try {
      _classBkg = new EllipseRegionsClassifier(_crossplot.getEllipseRegionsModel(), 128, xStart, xEnd, yStart, yEnd);
      //_appendCursorInfo = true;
    } catch (Exception ex) {
      //_appendCursorInfo = false;
      _crossplot.setCursorStyle(SWT.CURSOR_ARROW);
      return;
    }
    IModelSpaceCanvas canvas = _crossplot.getModelSpaceCanvas();

    // Get pixel width and height of map.
    Point2D.Double pxy0 = new Point2D.Double();
    canvas.transformModelToPixel(_modelSpace, xStart, yStart, pxy0);
    Point2D.Double pxy1 = new Point2D.Double();
    canvas.transformModelToPixel(_modelSpace, xEnd, yEnd, pxy1);

    double px0 = Math.min(pxy0.x, pxy1.x);
    double py0 = Math.min(pxy0.y, pxy1.y);
    double px1 = Math.max(pxy0.x, pxy1.x);
    double py1 = Math.max(pxy0.y, pxy1.y);
    int width = (int) (px1 - px0);
    int height = (int) (py1 - py0);

    if (width <= 0 || height <= 0) {
      throw new RuntimeException("Invalid image size.");
    }

    boolean newImage = false;
    try {
      newImage = createImage(width, height);
    } catch (Exception ex) {
      throw new RuntimeException("Error drawing class background: " + ex.toString());
    }

    //double classNull = Double.NaN;
    Point2D.Double anchorPoint = new Point2D.Double();
    canvas.transformPixelToModel(_modelSpace, 0, 0, anchorPoint);

    if (newImage) {
      for (int i = 0; i < width; i++) {

        int px = (int) px0 + i;
        for (int j = 0; j < height; j++) {

          int py = (int) py0 + j;
          Point2D.Double modelCoord = new Point2D.Double();
          canvas.transformPixelToModel(_modelSpace, px, py, modelCoord);
          double classValue = _classBkg.processAB(modelCoord.getX(), modelCoord.getY());

          if (!Double.isNaN(classValue)) {
            int index = _colorBar.getColorIndex(classValue);
            colorImagePoint(i, j, width, index);
          }
        }
      }
    }
    PaletteData palette = new PaletteData(buildColorModel(_colorBar));
    int depth = 8;
    if (_colorBar.getNumColors() >= 256) {
      depth = 16;
    }
    ImageData imageData = new ImageData(width, height, depth, palette, 1, _imagePixels);
    Image image = new Image(canvas.getComposite().getDisplay(), imageData);

    if (_plotImage == null) {
      _plotImage = new PlotImage(image, "", ImageAnchor.UpperLeft, anchorPoint);
      _plotImage.setVisible(_isVisible);
      _plotImage.setSelectable(false);
      _plotImage.setRenderLevel(RenderLevel.IMAGE_UNDER_GRID);
      addShape(_plotImage);
    } else {
      Image imageOld = _plotImage.getImage();
      _plotImage.getPoint(0).moveTo(anchorPoint.getX(), anchorPoint.getY());
      _plotImage.setImage(image);
      if (imageOld != null) {
        imageOld.dispose();
      }
    }
    _updateRendering = false;
    _crossplot.setCursorStyle(SWT.CURSOR_ARROW);
  }

  @Override
  public void setModelSpace(final IModelSpace modelSpace) {
    super.setModelSpace(modelSpace);
    if (_modelSpace != null) {
      //_modelSpace.addListener(this);
    }
  }

  private RGB[] buildColorModel(final ColorBar colorBar) {
    int numColors = colorBar.getNumColors();
    RGB[] rgbs = new RGB[numColors + 1];
    for (int i = 0; i < numColors; i++) {
      rgbs[i] = colorBar.getColor(i);
    }
    rgbs[numColors] = _crossplot.getModelSpaceCanvas().getComposite().getBackground().getRGB();
    return rgbs;
  }

  protected boolean createImage(final int w, final int h) throws Exception {

    int numPixels = w * h;
    if (_updateRendering || _imagePixels == null || numPixels != _imagePixels.length) {
      _imagePixels = new byte[numPixels];
      for (int p = 0; p < numPixels; p++) {
        _imagePixels[p] = (byte) _colorBar.getNumColors();
      }

      return true;
    }
    return false;
  }

  protected void colorImagePoint(final int x, final int y, final int width, final int index) {
    int pixelIndex = y * width + x;
    _imagePixels[pixelIndex] = (byte) index;
  }

  public void setColors(final RGB[] colors) {
    if (colors.length != _colorBar.getNumColors()) {
      _updateRendering = true;
    }
    _colorBar.setColors(colors);
    refresh();
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {

    List<String> keys = new ArrayList<String>();
    keys.add("Class");
    List<String> values = new ArrayList<String>();
    values.add("Undefined");

    if (isVisible() && _classBkg != null) {
      String value = "Undefined";
      double classValue = _classBkg.processAB(x, y);
      if (!Double.isNaN(classValue) && !MathUtil.isEqual(classValue, 0.0)) {
        int index = (int) (Math.abs(classValue) / 32);
        value = _classNames[index];
        if (classValue > 0) {
          value += " Top";
        } else {
          value += " Base";
        }
        value += " (" + classValue + ")";
      }
      values.remove(0);
      values.add(value);
    }

    return new ReadoutInfo(getName(), keys, values);
  }

  public void ellipseModelUpdated(final EllipseRegionsModelEvent event) {
    redraw();
  }

  public void viewLayerUpdated(final ViewLayerEvent event) {
    redraw();
  }

  public void controlMoved(final ControlEvent event) {
    // No action required.
  }

  public void controlResized(final ControlEvent event) {
    redraw();
  }

  @Override
  public void dispose() {
    if (_plotImage != null) {
      Image image = _plotImage.getImage();
      if (image != null) {
        image.dispose();
      }
    }
    Widget widget = _crossplot.getModelSpaceCanvas().getComposite();
    if (!widget.isDisposed()) {
      _crossplot.getModelSpaceCanvas().removeControlListener(this);
    }
    super.dispose();
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)
        || eventType.equals(PlotEventType.MODEL_SPACE_UPDATED) || eventType.equals(PlotEventType.MODEL_REDRAW)) {
      redraw();
    }
  }

  public void colorsChanged(final ColorMapEvent event) {
    setColors(event.getColorMapModel().getColors());
  }
}
