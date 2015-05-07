/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.chartviewer.renderer.ChartViewRenderer;
import org.geocraft.ui.chartviewer.renderer.histogram.HistogramViewRenderer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.PlotScrolling;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.listener.ICursorListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.viewer.RendererSpecification;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;


public class HistogramChartViewer extends AbstractChartViewer implements ICursorListener {

  public static final String Y_AXIS_COUNT = "Count";

  public static final String Y_AXIS_PERCENTAGE = "Percentage";

  protected String _verticalAxis = Y_AXIS_PERCENTAGE;

  public HistogramChartViewer(final Composite parent, final String title, final String xLabel, final String yLabel) {
    super(parent, title, false);
    IPlot plot = getPlot();
    plot.getActiveModelSpace().addLayer(new PlotLayer("Default"));
    plot.getActiveModelSpace().getAxisX().getLabel().setText(xLabel);
    plot.getActiveModelSpace().getAxisY().getLabel().setText(yLabel);
    setLayerTreeVisible(false);
    _verticalAxis = Y_AXIS_PERCENTAGE;
  }

  @Override
  protected IModelSpace initializeModelSpace() {
    return AbstractChartViewer.createModelSpace("", "");
  }

  @Override
  protected PlotScrolling getPlotScrolling() {
    return PlotScrolling.NONE;
  }

  @Override
  public String getChartType() {
    return "HistogramChart";
  }

  @Override
  protected void initializeToolBars() {
    super.initializeToolBars();
    SimpleToolBar histogramToolBar = addCustomToolBar();
    histogramToolBar.addPushButton(new ToggleHistogramYAxis(this));
  }

  public HistogramData addData(final String name, final float[] values, final int numCells, final float minValue,
      final float maxValue, final RGB rgb) {
    return addData(name, values, Float.NaN, numCells, minValue, maxValue, rgb);
  }

  public HistogramData addData(final String name, final float[] values, final float nullValue, final int numCells,
      final float minValue, final float maxValue, final RGB rgb) {
    HistogramData data = new HistogramData(name, values, nullValue, numCells, minValue, maxValue, rgb);
    addObjects(new Object[] { data });
    return data;
  }

  public HistogramData addData(final String name, final float[][] values, final int numCells, final float minValue,
      final float maxValue, final RGB rgb) {
    return addData(name, values, Float.NaN, numCells, minValue, maxValue, rgb);
  }

  public HistogramData addData(final String name, final float[][] values, final float nullValue, final int numCells,
      final float minValue, final float maxValue, final RGB rgb) {
    int numRows = values.length;
    int numCols = values[0].length;
    float[] values1D = new float[numRows * numCols];
    for (int row = 0; row < numRows; row++) {
      System.arraycopy(values[row], 0, values1D, row * numCols, numCols);
    }
    return addData(name, values1D, nullValue, numCells, minValue, maxValue, rgb);
  }

  @Override
  public void cursorSelectionUpdated(final double x, final double y) {
    List<DataSelection> selectionList = new ArrayList<DataSelection>();
    IPlotLayer[] layers = getPlot().getActiveModelSpace().getLayers();
    for (IPlotLayer layer : layers) {
      if (layer instanceof HistogramViewRenderer) {
        HistogramViewRenderer renderer = (HistogramViewRenderer) layer;
        if (renderer.isVisible()) {
          DataSelection selection = renderer.getDataSelection(x, y);
          if (selection != null) {
            selectionList.add(selection);
            renderer.editSettings();
            IViewLayer viewLayer = getLayerModel().getLayer(new RendererSpecification(renderer));
            getLayerViewer().setSelection(new StructuredSelection(viewLayer));
          }
        }
      }
    }
    if (selectionList.size() > 0) {
      ServiceProvider.getMessageService().publish(Topic.DATA_SELECTION, selectionList);
    }
  }

  /**
   * Returns the current vertical histogram axis ("Count" or "Percentage").
   */
  public String getVerticalAxis() {
    return _verticalAxis;
  }

  /**
   * Toggles the vertical histogram axis between "Count" and "Percentage".
   */
  public void toggleVerticalAxis() {
    if (_verticalAxis.equals(Y_AXIS_COUNT)) {
      _verticalAxis = Y_AXIS_PERCENTAGE;
    } else if (_verticalAxis.equals(Y_AXIS_PERCENTAGE)) {
      _verticalAxis = Y_AXIS_COUNT;
    }
    IPlot plot = getPlot();
    plot.getActiveModelSpace().getAxisY().getLabel().setText(_verticalAxis);
    for (ChartViewRenderer renderer : getChartViewRenderers()) {
      renderer.redraw();
    }
    plot.adjustBounds(plot.getActiveModelSpace(), true, true);
  }

  public Model getViewerModel() {
    // TODO Auto-generated method stub
    return null;
  }

  public void updateFromModel() {
    // TODO Auto-generated method stub

  }

  public void addRenderer(String klass, Map<String, String> props, String uniqueId) {
    // TODO Auto-generated method stub

  }

}
