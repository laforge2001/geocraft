package org.geocraft.ui.chartviewer.renderer.histogram;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.util.Generics;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.chartviewer.HistogramChartViewer;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.chartviewer.data.IChartData;
import org.geocraft.ui.chartviewer.renderer.ChartViewRenderer;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.viewer.ReadoutInfo;


public abstract class HistogramViewRenderer extends ChartViewRenderer<HistogramChartViewer> {

  protected HistogramData _histogramData;

  protected IPlotPolygon _histogramPolygon;

  protected String _verticalAxis;

  public HistogramViewRenderer() {
    super("Histogram Renderer");
  }

  @Override
  public IChartData getRenderedChartData() {
    return _histogramData;
  }

  @Override
  public void redraw() {
    String verticalAxis = getViewer().getVerticalAxis();
    if (!verticalAxis.equals(_verticalAxis)) {
      _verticalAxis = verticalAxis;
      removeAllShapes();
      addPlotShapes();
    }
    super.redraw();
  }

  @Override
  protected void setViewer(HistogramChartViewer viewer) {
    super.setViewer(viewer);
    _verticalAxis = viewer.getVerticalAxis();
  }

  @Override
  public ReadoutInfo getReadoutInfo(double x, double y) {

    List<String> keys = new ArrayList<String>();
    List<String> vals = new ArrayList<String>();

    keys.addAll(Generics.asList("Value", "Count", "Percentage"));
    float value = (float) x;
    int count = _histogramData.getCount(value);
    float percentage = _histogramData.getPercentage(value);
    vals.add("" + value);
    vals.add("" + count);
    vals.add("" + percentage);

    return new ReadoutInfo(getName(), keys, vals);
  }

  @Override
  public DataSelection getDataSelection(double x, double y) {
    // If the layer is not visible, the histogram polygon does not exist, or the point is outside, then return null.
    if (!isVisible() || _histogramPolygon == null || !_histogramPolygon.isVisible()
        || !_histogramPolygon.isPointInside(x, y)) {
      return null;
    }
    // Otherwise return the a valid data selection object.
    DataSelection selection = new DataSelection(getClass().getSimpleName());
    selection.setSelectedObjects(getRenderedObjects());
    return selection;
  }

  @Override
  protected void addPlotShapes() {
    _histogramData = createHistogramData();
    if (getViewer() == null || _histogramData == null) {
      return;
    }

    double xStart = _histogramData.getStartX();
    double xEnd = _histogramData.getEndX();
    double xDelta = (xEnd - xStart) / _histogramData.getNumCells();

    RGB color = _histogramData.getRGB();
    _histogramPolygon = new PlotPolygon();
    _histogramPolygon.setName(_histogramData.getDisplayName());
    _histogramPolygon.setSelectable(false);
    _histogramPolygon.setEditable(false);
    _histogramPolygon.setTransparency(50);
    _histogramPolygon.setLineColor(new RGB(0, 0, 0));
    _histogramPolygon.setLineStyle(LineStyle.SOLID);
    _histogramPolygon.setLineWidth(1);
    _histogramPolygon.setFillColor(color);
    _histogramPolygon.setFillStyle(FillStyle.SOLID);
    _histogramPolygon.setPointStyle(PointStyle.NONE);
    _histogramPolygon.addPoint(createPoint(xStart, 0));
    for (int i = 0; i < _histogramData.getNumCells(); i++) {
      double x0 = xStart + i * xDelta;
      double x1 = x0 + xDelta;
      double y = 0;
      if (_verticalAxis.equals(HistogramChartViewer.Y_AXIS_PERCENTAGE)) {
        y = _histogramData.getPercentageByCell(i);
      } else if (_verticalAxis.equals(HistogramChartViewer.Y_AXIS_COUNT)) {
        y = _histogramData.getCountByCell(i);
      } else {
        throw new RuntimeException("Invalid vertical axis: " + _verticalAxis);
      }
      _histogramPolygon.addPoint(createPoint(x0, y));
      _histogramPolygon.addPoint(createPoint(x1, y));
    }
    _histogramPolygon.addPoint(createPoint(xEnd, 0));
    addShape(_histogramPolygon);
  }

  private IPlotPoint createPoint(final double x, final double y) {
    IPlotPoint point = new PlotPoint(x, y, 0);
    point.setPropertyInheritance(true);
    return point;
  }

  protected abstract HistogramData createHistogramData();
}
