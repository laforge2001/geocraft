/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.scatter.well;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.ui.chartviewer.ScatterChartViewer;
import org.geocraft.ui.chartviewer.data.ScatterData;
import org.geocraft.ui.chartviewer.renderer.scatter.ScatterDataRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.axis.Axis;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.viewer.IViewer;


public class WellLogTracesRenderer extends ScatterDataRenderer {

  /** The 1st log trace being rendered. */
  private WellLogTrace _logTrace1;

  /** The 2nd log trace being rendered. */
  private WellLogTrace _logTrace2;

  /** The model of rendering properties for the log traces. */
  private WellLogTracesRendererModel _model = new WellLogTracesRendererModel();

  @Override
  public void setData(final Shell shell, final ScatterChartViewer viewer, final Object[] objects) {
    WellLogTrace logTrace1 = (WellLogTrace) objects[0];
    WellLogTrace logTrace2 = (WellLogTrace) objects[1];
    double[] zs = logTrace1.getZValues(_model.getZDomain());
    _model.setValueObject(WellLogTracesRendererModel.Z_START, zs[0]);
    _model.setValueObject(WellLogTracesRendererModel.Z_END, zs[zs.length - 1]);
    _scatterData = createScatterData(logTrace1, logTrace2, _model);
    super.setData(shell, viewer, objects);
  }

  @Override
  protected void addPopupMenuActions() {
    String name = _logTrace1.getDisplayName() + " vs " + _logTrace2.getDisplayName();
    WellLogTracesRendererDialog dialog = new WellLogTracesRendererDialog(getShell(), name, this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
    addPopupMenuAction(new Action() {

      @Override
      public String getText() {
        return "Swap Axes";
      }

      @Override
      public void run() {
        WellLogTracesRenderer.this.swapAxes();
      }
    });
  }

  /**
   * Swaps the plotting of the logs (e.g. switches from DT vs GR to GR vs DT).
   */
  protected void swapAxes() {
    WellLogTrace temp = _logTrace1;
    _logTrace1 = _logTrace2;
    _logTrace2 = temp;
    _scatterData = createScatterData(_logTrace1, _logTrace2, _model);
    clear();
    IAxis xAxisTemp = new Axis(_modelSpace.getAxisX());
    IAxis yAxisTemp = new Axis(_modelSpace.getAxisY());
    IAxis xAxis = _modelSpace.getAxisX();
    IAxis yAxis = _modelSpace.getAxisY();
    xAxis.setDirection(yAxisTemp.getDirection());
    xAxis.setScale(yAxisTemp.getScale());
    xAxis.setUnit(yAxisTemp.getUnit());
    xAxis.setDefaultRange(yAxisTemp.getDefaultStart(), yAxisTemp.getDefaultEnd());
    xAxis.setViewableRange(yAxisTemp.getViewableStart(), yAxisTemp.getViewableEnd());
    xAxis.getLabel().setText(yAxisTemp.getLabel().getText());
    yAxis.setDirection(xAxisTemp.getDirection());
    yAxis.setScale(xAxisTemp.getScale());
    yAxis.setUnit(xAxisTemp.getUnit());
    yAxis.setDefaultRange(xAxisTemp.getDefaultStart(), xAxisTemp.getDefaultEnd());
    yAxis.setViewableRange(xAxisTemp.getViewableStart(), xAxisTemp.getViewableEnd());
    yAxis.getLabel().setText(xAxisTemp.getLabel().getText());
    addPlotShapes();
    redraw();
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.WELL_FOLDER, autoUpdate);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _logTrace1, _logTrace2 };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _logTrace1 = (WellLogTrace) objects[0];
    _logTrace2 = (WellLogTrace) objects[1];
  }

  @Override
  protected void setNameAndImage() {
    setName(_logTrace1.getDisplayName() + " vs " + _logTrace2.getDisplayName());
    setImage(ModelUI.getSharedImages().getImage(_logTrace1));
  }

  /**
   * Returns the model of rendering properties.
   */
  @Override
  public WellLogTracesRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the rendering properties based on the given model.
   * 
   * @param model the model containing updated rendering properties.
   */
  public void updateSettings(WellLogTracesRendererModel model) {
    _model.updateFrom(model);
    _scatterData = createScatterData(_logTrace1, _logTrace2, _model);
    removeAllShapes();
    addPlotShapes();
    redraw();
    getViewer().getPlot().adjustBounds(_modelSpace, false, true);
  }

  /**
   * Creates a scatter data object from a WellLogTrace entity.
   * The scatter data is created by reading every cell in
   * the logTrace, ignoring those with null values.
   * 
   * @param logTrace1 the 1st log trace from which to extract scatter data.
   * @param logTrace2 the 2nd log trace from which to extract scatter data.
   * @return the histogram data for the logTrace.
   */
  private ScatterData createScatterData(WellLogTrace logTrace1, WellLogTrace logTrace2, WellLogTracesRendererModel model) {
    float[] zs1 = Utilities.copyDoubleArrayToFloatArray(logTrace1.getZValues(model.getZDomain()));
    float[] zs2 = Utilities.copyDoubleArrayToFloatArray(logTrace2.getZValues(model.getZDomain()));
    List<Float> tempx = new ArrayList<Float>();
    List<Float> tempy = new ArrayList<Float>();
    for (int i = 0; i < zs1.length; i++) {
      if (zs1[i] >= model.getZStart() && zs1[i] <= model.getZEnd()) {
        if (!logTrace1.isNull(i)) {
          for (int j = 0; j < zs2.length; j++) {
            if (!logTrace2.isNull(j) && MathUtil.isEqual(zs1[i], zs2[j])) {
              tempx.add(logTrace1.getTraceData()[i]);
              tempy.add(logTrace2.getTraceData()[j]);
              break;
            }
          }
        }
      }
    }
    int numPoints = tempx.size();
    float[] xs = new float[numPoints];
    float[] ys = new float[numPoints];
    for (int i = 0; i < numPoints; i++) {
      xs[i] = tempx.get(i);
      ys[i] = tempy.get(i);
    }
    String name = logTrace1.getDisplayName() + " vs " + logTrace2.getDisplayName();
    return new ScatterData(name, xs, ys, _model.getPointColor(), _model.getPointStyle(), _model.getPointSize());
  }
}
