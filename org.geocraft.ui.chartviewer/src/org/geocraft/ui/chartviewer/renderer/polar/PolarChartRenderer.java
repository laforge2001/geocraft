package org.geocraft.ui.chartviewer.renderer.polar;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.color.ColorMapModel;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.util.Generics;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.ui.chartviewer.AbstractChartViewer;
import org.geocraft.ui.chartviewer.ScatterChartViewer;
import org.geocraft.ui.chartviewer.data.IChartData;
import org.geocraft.ui.chartviewer.data.PolarChartData;
import org.geocraft.ui.chartviewer.renderer.ChartViewRenderer;
import org.geocraft.ui.color.ColorBarEditorListener;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.IPlotShape;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.plot.util.PolygonRegionsUtil;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.ViewLayerEvent;


/**
 * Renders a <code>Grid3d</code> entity in the polar chart
 */
public class PolarChartRenderer extends ChartViewRenderer<ScatterChartViewer> implements ControlListener,
    ColorMapListener, ColorBarEditorListener {

  /** Polar chart data */
  private PolarChartData _polarChartData;

  private ColorMapModel _colorMap;

  /** Color bar */
  private ColorBar _colorBar;

  /** The grid being rendered. */
  protected Grid3d _grid;

  /** The volume being rendered. */
  protected PostStack3d _volume;

  /** Status flag for updating the rendering. */
  protected boolean _updateRendering;

  protected PolarChartRendererModel _model;

  protected IModelSpaceCanvas _canvas;

  public PolarChartRenderer() {
    super("Polar Chart");
    _appendCursorInfo = true;
    _model = new PolarChartRendererModel();
  }

  @Override
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
      _model.setColorBarRange(_polarChartData);
    }
    _updateRendering = true;
    renderImage(_model.getColorBar());
  }

  @Override
  public void refresh() {
    _updateRendering = true;
    renderImage(_model.getColorBar());
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
   * If any of the colors change then is renders the polar plot again
   */
  private void renderImage(ColorMapModel colorMapModel) {

    ScatterChartViewer viewer = getViewer();
    IModelSpaceCanvas canvas = viewer.getPlot().getModelSpaceCanvas();

    double startValue = _model.getColorBar().getStartValue();
    double endValue = _model.getColorBar().getEndValue();

    _colorBar = new ColorBar(colorMapModel, startValue, endValue, (endValue - startValue) / 10);
    RGB[] colorTable = buildColorModel(canvas.getComposite().getBackground().getRGB(), _colorBar);

    IPlotShape[] shapes = getShapes();
    int nAngleVals = _polarChartData.getNumAngleVals();
    int angleIndx = 0;
    int valIndx = 0;
    for (IPlotShape currentShape : shapes) {
      // plot null data
      if (_polarChartData.isNull(valIndx, angleIndx)) {
        PlotPolygon polygon = (PlotPolygon) currentShape;
        Color pointColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        polygon.blockUpdate();
        polygon.setLineColor(pointColor.getRGB());
        polygon.setFillColor(pointColor.getRGB());
        polygon.unblockUpdate();
        // plot data that is not null
      } else {
        // Determine a color Index
        float value = _polarChartData.getValueByCell(valIndx, angleIndx);
        int colorIndex = _colorBar.getColorIndex(value);

        // Make sure the color index is range
        if (colorIndex >= colorTable.length) {
          colorIndex = colorTable.length - 1;
        }
        if (colorIndex < 0) {
          colorIndex = 0;
        }

        // Change color of polygon
        PlotPolygon polygon = (PlotPolygon) currentShape;
        polygon.blockUpdate();
        polygon.setLineColor(colorTable[colorIndex]);
        polygon.setFillColor(colorTable[colorIndex]);
        polygon.setPointColor(colorTable[colorIndex]);
        polygon.unblockUpdate();
      }
      // reset the value and angle index
      valIndx++;
      if (valIndx == nAngleVals) {
        angleIndx++;
        valIndx = 0;
      }
    }

    // save color map
    _colorMap = colorMapModel;
    getViewer().getPlot().getActiveModelSpace().redraw();
  }

  /**
   * Builds the color model for the plot
   * 
   * @param colorBar
   *            the colorbar to use.
   * @return the color model.
   */
  private RGB[] buildColorModel(final RGB backgroundRGB, final ColorBar colorBar) {
    int numColors = colorBar.getNumColors();
    RGB[] rgbs = new RGB[numColors + 1];
    for (int i = 0; i < numColors; i++) {
      rgbs[i] = colorBar.getColor(i);
    }
    rgbs[numColors] = backgroundRGB;
    return rgbs;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {

    List<String> keys = new ArrayList<String>();
    List<String> vals = new ArrayList<String>();

    int nAngleVals = _polarChartData.getNumAngleVals();

    IPlotShape[] shapes = getShapes();
    int angleIndx = 0;
    int valIndx = 0;
    for (IPlotShape currentShape : shapes) {
      IPlotPolygon polygon = (IPlotPolygon) currentShape;
      if (PolygonRegionsUtil.isPointInside(polygon, x, y)) {
        keys.addAll(Generics.asList("Amplitude"));
        float amplitude = _polarChartData.getValueByCell(valIndx, angleIndx);
        vals.addAll(Generics.asList("" + amplitude));
        return new ReadoutInfo(_polarChartData.getDisplayName(), keys, vals);
      }
      // reset the value and angle index
      valIndx++;
      if (valIndx == nAngleVals) {
        angleIndx++;
        valIndx = 0;
      }
    }
    keys.addAll(Generics.asList("Amplitude"));
    vals.addAll(Generics.asList("Undefined"));
    return new ReadoutInfo(_polarChartData.getDisplayName(), keys, vals);
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)) {
      _updateRendering = true;
      renderImage(_model.getColorBar());
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
      renderImage(event.getColorMapModel());
      return;
    }
    _model.getColorBar().setColors(event.getColorMapModel().getColors());
    renderImage(event.getColorMapModel());
  }

  public void updateRendererModel(final PolarChartRendererModel model) {
    if (model.getTransparency() != _model.getTransparency()) {
      _updateRendering = true;
    }
    _model = new PolarChartRendererModel(model);
    _model.setColorBar(new ColorBar(model.getColorBar()));
    redraw(false);
  }

  @Override
  protected void addPopupMenuActions() {
    Shell shell = new Shell(getShell());
    if (_grid != null) {
      PolarChartRendererDialog dialog = new PolarChartRendererDialog(shell, _grid.getDisplayName(), this, _grid,
          _volume);
      addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
    } else {
      if (_volume != null) {
        PolarChartRendererDialog dialog = new PolarChartRendererDialog(shell, _volume.getDisplayName(), this, _grid,
            _volume);
        addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
      }
    }

  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(AbstractChartViewer.DEFAULT_FOLDER, autoUpdate);
  }

  @Override
  public IChartData getRenderedChartData() {
    return _polarChartData;
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _polarChartData };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _polarChartData = (PolarChartData) objects[0];
    _grid = _polarChartData.getGrid();
    if (_grid == null) {
      _volume = _polarChartData.getVolume();
    }
    _model.setPrimaryGrid(_grid);
  }

  @Override
  protected void setNameAndImage() {
    setName(_polarChartData.getDisplayName());
  }

  @Override
  protected void addPlotShapes() {

    ScatterChartViewer viewer = getViewer();
    IModelSpaceCanvas canvas = viewer.getPlot().getModelSpaceCanvas();
    _canvas = canvas;

    // determine the color map
    ColorMapModel colorMapModel = null;
    // Reset color map if new one is available
    if (_colorMap != null) {
      colorMapModel = _colorMap;
      _model.getColorBar().setColors(colorMapModel.getColors());
      // Otherwise default the color map
    } else {
      colorMapModel = new ColorMapModel(64, new SpectrumColorMap());
      colorMapModel.reverseColors();
      _model.getColorBar().setColors(colorMapModel.getColors());
    }

    double startValue = _polarChartData.getAttributeMinimum();
    double endValue = _polarChartData.getAttributeMaximum();
    _model.getColorBar().setStartValue(startValue);
    _model.getColorBar().setEndValue(endValue);

    _colorBar = new ColorBar(colorMapModel, startValue, endValue, (endValue - startValue) / 10);
    RGB[] colorTable = buildColorModel(canvas.getComposite().getBackground().getRGB(), _colorBar);

    float[][] xPnts = _polarChartData.getXPnts();
    float[][] yPnts = _polarChartData.getYPnts();
    int nCellPnts = yPnts.length;
    int numAngleVals = _polarChartData.getNumAngleVals();
    int numAngles = _polarChartData.getNumAngles();

    int cellIndx = 0;
    for (int i1 = 0; i1 < numAngles; i1++) {
      for (int i2 = 0; i2 < numAngleVals; i2++) {
        // Determine the polygon for the current cell
        if (_polarChartData.isNull(i2, i1)) {
          PlotPolygon polygon = new PlotPolygon();
          polygon.setSelectable(true);
          polygon.setRenderLevel(RenderLevel.IMAGE_UNDER_GRID);
          polygon.setName("Polygon #" + (cellIndx + 1));
          Color pointColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
          polygon.setLineColor(pointColor.getRGB());
          polygon.setFillColor(pointColor.getRGB());
          polygon.setPointColor(pointColor.getRGB());
          polygon.setPointStyle(PointStyle.FILLED_SQUARE);
          polygon.setPointSize(3);
          for (int i3 = 0; i3 < nCellPnts; i3++) {
            IPlotPoint point = new PlotPoint(xPnts[i3][cellIndx], yPnts[i3][cellIndx], 0);
            point.setPropertyInheritance(true);
            polygon.addPoint(point);
          }
          addShape(polygon);
        } else {
          // Determine a color Index
          float value = _polarChartData.getValueByCell(i2, i1);
          int colorIndex = _colorBar.getColorIndex(value);

          // Make sure the color index is range
          if (colorIndex >= colorTable.length) {
            colorIndex = colorTable.length - 1;
          }
          if (colorIndex < 0) {
            colorIndex = 0;
          }

          // Set the polygon
          PlotPolygon polygon = new PlotPolygon();
          polygon.setSelectable(true);
          polygon.setRenderLevel(RenderLevel.IMAGE_UNDER_GRID);
          polygon.setName("Polygon #" + (cellIndx + 1));
          polygon.setLineColor(colorTable[colorIndex]);
          polygon.setFillColor(colorTable[colorIndex]);
          polygon.setFillStyle(FillStyle.SOLID);
          polygon.setPointColor(colorTable[colorIndex]);
          polygon.setPointStyle(PointStyle.FILLED_SQUARE);
          polygon.setPointSize(3);
          for (int i3 = 0; i3 < nCellPnts; i3++) {
            IPlotPoint point = new PlotPoint(xPnts[i3][cellIndx], yPnts[i3][cellIndx], 0);
            point.setPropertyInheritance(true);
            polygon.addPoint(point);
          }
          addShape(polygon);
        }
        cellIndx++;
      }
    }
  }

  @Override
  public PolarChartRendererModel getSettingsModel() {
    return _model;
  }

}