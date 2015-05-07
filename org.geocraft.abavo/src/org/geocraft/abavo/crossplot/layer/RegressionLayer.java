/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.layer;


import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.geocraft.abavo.crossplot.action.ShowRegressionStatistics;
import org.geocraft.math.regression.RegressionStatistics;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.axis.AxisRange;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;


public class RegressionLayer extends PlotLayer {//implements IModelSpaceListener {

  protected RegressionStatistics _regressionStats;

  protected IPlotPolyline _regressionLine;

  protected LineProperties _lineProps;

  public RegressionLayer(final String name, final String acronym, final RegressionStatistics regressionStats, final LineProperties lineProps) {
    super(name);
    _regressionStats = regressionStats;

    _actions.clear();

    _lineProps = new LineProperties(lineProps);
    //_actions.add(new ViewLayerEditorAction(this, "Edit Properties..."));

    if (acronym.equals("PPD")) {
      URL url = ImageRegistryUtil.createURL("icons/misc/RegressionPPD16.png");
      _image = ImageDescriptor.createFromURL(url).createImage();
    } else if (acronym.equals("LSQ")) {
      URL url = ImageRegistryUtil.createURL("icons/misc/RegressionLSQ16.png");
      _image = ImageDescriptor.createFromURL(url).createImage();
    } else if (acronym.equals("RMA")) {
      URL url = ImageRegistryUtil.createURL("icons/misc/RegressionRMA16.png");
      _image = ImageDescriptor.createFromURL(url).createImage();
    }

    addPopupMenuAction(new ShowRegressionStatistics(_name, _regressionStats));
  }

  @Override
  public void setModelSpace(final IModelSpace modelSpace) {
    if (modelSpace != null) {
      double xmin = 0;
      double xmax = 0;
      double ymin = 0;
      double ymax = 0;
      ModelSpaceBounds bounds = modelSpace.getViewableBounds();
      AxisRange xRange = bounds.getRangeX();
      AxisRange yRange = bounds.getRangeY();
      xmin = xRange.getStart();
      xmax = xRange.getEnd();
      ymin = yRange.getStart();
      ymax = yRange.getEnd();

      double slope = _regressionStats.getSlope();
      double intercept = _regressionStats.getIntercept();
      double yxmin = slope * xmin + intercept;
      double yxmax = slope * xmax + intercept;
      TextProperties textProps = new TextProperties();
      Color pointColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
      PointProperties pointProps = new PointProperties(PointStyle.NONE, pointColor.getRGB(), 1);
      _regressionLine = new PlotPolyline(getName(), textProps, pointProps, _lineProps);

      _regressionLine.setSelectable(false);
      _regressionLine.blockUpdate();
      _regressionLine.setModelSpace(modelSpace);
      _regressionLine.addPoint(new PlotPoint("", xmin, yxmin, 0, pointProps));
      _regressionLine.addPoint(new PlotPoint("", xmax, yxmax, 0, pointProps));
      _regressionLine.unblockUpdate();
      addShape(_regressionLine);
    }
    super.setModelSpace(modelSpace);
    if (_modelSpace != null) {
      //_modelSpace.addListener(this);
    }
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.DEFAULT_BOUNDS_UPDATED)
        || eventType.equals(PlotEventType.MODEL_SPACE_UPDATED)) {
      if (_regressionLine != null) {
        IModelSpace modelSpace = event.getModelSpace();
        ModelSpaceBounds bounds = modelSpace.getDefaultBounds();
        AxisRange xRange = bounds.getRangeX();
        double xmin = xRange.getStart();
        double xmax = xRange.getEnd();

        double slope = _regressionStats.getSlope();
        double intercept = _regressionStats.getIntercept();
        double yxmin = slope * xmin + intercept;
        double yxmax = slope * xmax + intercept;
        _regressionLine.getPoint(0).moveTo(xmin, yxmin);
        _regressionLine.getPoint(1).moveTo(xmax, yxmax);
        refresh();
      }
    }
  }

  public RegressionStatistics getRegressionStatistics() {
    return _regressionStats;
  }

  @Override
  public String getToolTipText() {
    return _regressionStats.getInfo();
  }

  public void moveToFront() {
    getModelSpace().moveToTop(this);
    updated();
  }
}
