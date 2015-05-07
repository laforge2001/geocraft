package org.geocraft.ui.chartviewer.renderer.scatter;


import org.geocraft.ui.chartviewer.AbstractChartViewer;
import org.geocraft.ui.chartviewer.ScatterChartViewer;
import org.geocraft.ui.chartviewer.data.IChartData;
import org.geocraft.ui.chartviewer.data.ScatterData;
import org.geocraft.ui.chartviewer.renderer.ChartViewRenderer;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.viewer.ReadoutInfo;


public class ScatterDataRenderer extends ChartViewRenderer<ScatterChartViewer> {

  protected ScatterData _scatterData;

  protected IPlotPolyline _scatterPoints;

  public ScatterDataRenderer() {
    super("Scatter Data Renderer");
    showReadoutInfo(false);
  }

  @Override
  public IChartData getRenderedChartData() {
    return _scatterData;
  }

  @Override
  protected void addPlotShapes() {
    _scatterPoints = new PlotPolyline();
    _scatterPoints.setEditable(false);
    _scatterPoints.setSelectable(false);
    if (_scatterData.getConnectPoints()) {
      _scatterPoints.setLineStyle(LineStyle.SOLID);
    } else {
      _scatterPoints.setLineStyle(LineStyle.NONE);
    }
    _scatterPoints.setPointSize(_scatterData.getPointSize());
    _scatterPoints.setPointColor(_scatterData.getPointRGB());
    _scatterPoints.setPointStyle(_scatterData.getPointStyle());

    // Display labels if the scatter data has them
    if (_scatterData.hasLabels()) {
      _scatterPoints.setTextFont(_scatterData.getLabelFont());
      _scatterPoints.setTextColor(_scatterData.getPointRGB());
      for (int i = 0; i < _scatterData.getNumPoints(); i++) {
        IPlotPoint point = new PlotPoint(_scatterData.getLabel(i), _scatterData.getX(i), _scatterData.getY(i), 0);
        point.setPropertyInheritance(false);
        _scatterPoints.addPoint(point);
      }
      // otherwise display points
    } else {
      for (int i = 0; i < _scatterData.getNumPoints(); i++) {
        IPlotPoint point = new PlotPoint(_scatterData.getX(i), _scatterData.getY(i), 0);
        point.setPropertyInheritance(true);
        _scatterPoints.addPoint(point);
      }
    }
    addShape(_scatterPoints);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(AbstractChartViewer.DEFAULT_FOLDER, autoUpdate);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _scatterData };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _scatterData = (ScatterData) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_scatterData.getDisplayName());
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#getSettingsModel()
   */
  @Override
  public ScatterDataRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReadoutInfo getReadoutInfo(double x, double y) {
    return null;
  }
}
