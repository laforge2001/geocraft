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
import org.geocraft.abavo.crossplot.ABDataSeries;
import org.geocraft.abavo.crossplot.action.ColorPointsBySeries;
import org.geocraft.abavo.crossplot.action.ColorPointsByZ;
import org.geocraft.abavo.crossplot.action.ShowSeriesStatistics;
import org.geocraft.abavo.crossplot.action.WriteSeriesToFile;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPointGroup;


public class CrossplotSeriesLayer extends PlotLayer implements ColorMapListener {

  private final ABDataSeries _series;

  private final ColorBar _colorBar;

  private final IPlotPointGroup _pointGroup;

  public CrossplotSeriesLayer(final ABDataSeries series, final PointProperties pointProps, final ColorBar colorBar) {
    super(series.getName() + " Points");
    _series = series;
    _colorBar = colorBar;
    _colorBar.addColorMapListener(this);
    _pointGroup = new PlotPointGroup();
    float[] a = series.getA();
    float[] b = series.getB();
    Point3d[] pts = series.getPoints();
    for (int i = 0; i < _series.getNumPoints(); i++) {
      IPlotPoint point = new PlotPoint(a[i], b[i], pts[i].getZ());
      point.setPointStyle(pointProps.getStyle());
      point.setPointColor(pointProps.getColor());
      point.setPointSize(pointProps.getSize());
      point.setPropertyInheritance(true);
      _pointGroup.addPoint(point);
      _pointGroup.setPointStyle(pointProps.getStyle());
      _pointGroup.setPointColor(pointProps.getColor());
      _pointGroup.setPointSize(pointProps.getSize());
    }
    _pointGroup.setSelectable(false);
    addShape(_pointGroup);

    addPopupMenuAction(new ColorPointsByZ(this));
    addPopupMenuAction(new ColorPointsBySeries(this));
    addPopupMenuAction(new ShowSeriesStatistics(_series));
    addPopupMenuAction(new WriteSeriesToFile(_series));
  }

  @Override
  public Image createImage() {
    return Activator.getDefault().createImage(ABavoImages.DATA_SERIES_POINTS);
  }

  @Override
  public String getToolTipText() {
    return _series.getRegressionDataStatistics().getInfo();
  }

  public void draw() {
    addShape(_pointGroup);
  }

  public void colorPointsByZ(final boolean colorByZ) {
    _pointGroup.blockUpdate();
    for (IPlotPoint point : _pointGroup.getPoints()) {
      point.setPropertyInheritance(!colorByZ);
    }
    double startValue = _colorBar.getStartValue();
    double endValue = _colorBar.getEndValue();
    if (Double.isNaN(startValue) || Double.isNaN(endValue)) {
      startValue = _series.getMinimumZ();
      endValue = _series.getMaximumZ();
      _colorBar.setStartValue(startValue);
      _colorBar.setEndValue(endValue);
      Labels labels = new Labels(startValue, endValue, 10);
      _colorBar.setStepValue(labels.getIncrement());
    }
    colorsChanged(null);
    _pointGroup.unblockUpdate();
    updated();
  }

  public void colorsChanged(final ColorMapEvent event) {
    _pointGroup.blockUpdate();
    for (IPlotPoint point : _pointGroup.getPoints()) {
      double z = point.getZ();
      point.setPointColor(_colorBar.getColor(z, false));
    }
    _pointGroup.unblockUpdate();
    updated();
  }

  public void setPointProperties(final PointProperties pointProperties) {
    _pointGroup.blockUpdate();
    _pointGroup.setPointColor(pointProperties.getColor());
    _pointGroup.setPointSize(pointProperties.getSize());
    _pointGroup.setPointStyle(pointProperties.getStyle());
    _pointGroup.unblockUpdate();

    // Update the ellipse color in the layer image.
    ImageData imageData = getImage().getImageData();
    RGB[] rgbs = { pointProperties.getColor() };
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

  @Override
  public void dispose() {
    _colorBar.removeColorMapListener(this);
    super.dispose();
  }

  public void moveToFront() {
    getModelSpace().moveToTop(this);
    updated();
  }
}
