/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.layer;


import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.ellipse.EllipseModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent;
import org.geocraft.abavo.ellipse.EllipseRegionsModelListener;
import org.geocraft.abavo.ellipse.EllipseUtil;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;


/**
 * The plot layer used for drawing ellipses in ABAVO.
 * This class is used for the Background, Maximum and Selection ellipses.
 */
public class EllipseLayer extends PlotLayer implements EllipseRegionsModelListener {

  /** The model for the active ellipse. */
  private final EllipseModel _activeEllipseModel;

  /** The plot polygon for the active ellipse. */
  private final IPlotPolygon _activeEllipse;

  /** The plot polygon for the static ellipse. */
  private final IPlotPolygon _staticEllipse;

  /** The ellipse point, line and fill color. */
  protected RGB _color;

  public EllipseLayer(final EllipseRegionsModel.EllipseType ellipseType) {
    super(ellipseType.toString() + " Ellipse");

    // Create the active ellipse model.
    _activeEllipseModel = new EllipseModel(ellipseType, 0, 0, 0, 0, 0);

    // Create the image and line color for the layer.
    if (ellipseType.equals(EllipseRegionsModel.EllipseType.Background)) {
      _image = Activator.getDefault().createImage(ABavoImages.BACKGROUND_ELLIPSE);
      _color = new RGB(55, 0, 0);
    } else if (ellipseType.equals(EllipseRegionsModel.EllipseType.Maximum)) {
      _image = Activator.getDefault().createImage(ABavoImages.MAXIMUM_ELLIPSE);
      _color = new RGB(0, 255, 0);
    } else if (ellipseType.equals(EllipseRegionsModel.EllipseType.Selection)) {
      _image = Activator.getDefault().createImage(ABavoImages.SELECTION_ELLIPSE);
      _color = new RGB(0, 0, 255);
    }

    _staticEllipse = new PlotPolygon();
    _staticEllipse.setName("Static");
    _staticEllipse.setLineColor(_color);
    _staticEllipse.setFillColor(_color);
    _staticEllipse.setPointColor(_color);
    _staticEllipse.setPointStyle(PointStyle.NONE);
    for (int i = 0; i < EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS; i++) {
      IPlotPoint point = new PlotPoint();
      point.setPropertyInheritance(true);
      _staticEllipse.addPoint(point);
    }
    _staticEllipse.setSelectable(false);
    addShape(_staticEllipse);

    _activeEllipse = new PlotPolygon();
    _activeEllipse.setName("Active");
    _activeEllipse.setLineColor(_color);
    _activeEllipse.setFillColor(_color);
    _activeEllipse.setPointColor(_color);
    _activeEllipse.setPointStyle(PointStyle.NONE);
    for (int i = 0; i < EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS; i++) {
      IPlotPoint point = new PlotPoint();
      point.setPropertyInheritance(true);
      _activeEllipse.addPoint(point);
    }
    _activeEllipse.setSelectable(false);
    addShape(_activeEllipse);
  }

  @Override
  protected Image createImage() {
    return Activator.getDefault().createImage(ABavoImages.ELLIPSE);
  }

  /**
   * Returns the static plot ellipse.
   * @return the static plot ellipse.
   */
  public IPlotPolygon getStaticEllipse() {
    return _staticEllipse;
  }

  /**
   * Returns the active plot ellipse.
   * @return the active plot ellipse.
   */
  public IPlotPolygon getActiveEllipse() {
    return _activeEllipse;
  }

  /**
   * Updates the active ellipse model.
   * @param m the new slope.
   * @param a the new length.
   * @param b the new width.
   */
  public void updateActiveEllipseModel(final double slope, final double length, final double width,
      final double centerX, final double centerY) {
    _activeEllipseModel.setSlope(slope);
    _activeEllipseModel.setLength(length);
    _activeEllipseModel.setWidth(width);
    _activeEllipseModel.setCenterX(centerX);
    _activeEllipseModel.setCenterY(centerY);
  }

  /**
   * Returns the slope of the active ellipse.
   * @return the slope of the active ellipse.
   */
  public double getActiveEllipseSlope() {
    return _activeEllipseModel.getSlope();
  }

  /**
   * Returns the length of the active ellipse.
   * @return the length of the active ellipse.
   */
  public double getActiveEllipseLength() {
    return _activeEllipseModel.getLength();
  }

  /**
   * Returns the width of the active ellipse.
   * @return the width of the active ellipse.
   */
  public double getActiveEllipseWidth() {
    return _activeEllipseModel.getWidth();
  }

  /**
   * Returns the center x-coordinate of the active ellipse.
   * @return the center x-coordinate of the active ellipse.
   */
  public double getActiveEllipseCenterX() {
    return _activeEllipseModel.getCenterX();
  }

  /**
   * Returns the center y-coordinate of the active ellipse.
   * @return the center y-coordinate of the active ellipse.
   */
  public double getActiveEllipseCenterY() {
    return _activeEllipseModel.getCenterY();
  }

  public void ellipseModelUpdated(final EllipseRegionsModelEvent event) {
    if (event.getType().equals(EllipseRegionsModelEvent.Type.EllipsesUpdated)) {
      EllipseRegionsModel model = event.getEllipseRegionsModel();
      EllipseModel ellipseModel = model.getEllipseModel(_activeEllipseModel.getType());
      double[] ex = new double[EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS];
      double[] ey = new double[EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS];
      double mterm = ellipseModel.getSlope();
      double aterm = ellipseModel.getLength();
      double bterm = ellipseModel.getWidth();
      double xctr = ellipseModel.getCenterX();
      double yctr = ellipseModel.getCenterY();
      EllipseUtil.computeEllipse(mterm, aterm, bterm, xctr, yctr, ex, ey);
      _staticEllipse.blockUpdate();
      for (int i = 0; i < EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS; i++) {
        _staticEllipse.getPoint(i).moveTo(ex[i], ey[i]);
      }
      _staticEllipse.unblockUpdate();
    }
    //setVisible(true);
    updated();
  }

  /**
   * Creates a plot polygon from an ellipse model containing slope, length and width.
   * @param ellipseModel the ellipse model.
   * @return the plot polygon.
   */
  public static IPlotPolygon createEllipsePolygon(final EllipseModel ellipseModel) {
    IPlotPolygon ellipse = new PlotPolygon();
    double[] xs = new double[EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS];
    double[] ys = new double[EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS];
    EllipseUtil.computeEllipse(ellipseModel.getSlope(), ellipseModel.getLength(), ellipseModel.getWidth(), ellipseModel
        .getCenterX(), ellipseModel.getCenterY(), xs, ys);
    for (int i = 0; i < EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS; i++) {
      ellipse.addPoint(new PlotPoint(xs[i], ys[i], 0));
    }
    xs = null;
    ys = null;
    return ellipse;
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  public void setLineProperties(final RGB rgb, final int lineWidth) {
    _staticEllipse.setPointColor(rgb);
    _staticEllipse.setLineColor(rgb);
    _staticEllipse.setLineWidth(lineWidth);
    _activeEllipse.setPointColor(rgb);
    _activeEllipse.setLineColor(rgb);
    _activeEllipse.setLineWidth(lineWidth);

    // Update the ellipse color in the layer image.
    ImageData imageData = getImage().getImageData();
    RGB[] rgbs = { rgb };
    int width = imageData.width;
    int height = imageData.height;
    int depth = 8;
    PaletteData palette = new PaletteData(rgbs);
    int scanlinePad = 1;
    byte[] data = new byte[width * height];
    byte[] alphas = imageData.alphaData;
    for (int i = 0; i < imageData.width * imageData.height; i++) {
      data[i] = (byte) 0;
    }
    imageData = new ImageData(width, height, depth, palette, scanlinePad, data);
    imageData.alphaData = alphas;
    setImage(new Image(null, imageData));

    updated();
  }
}
