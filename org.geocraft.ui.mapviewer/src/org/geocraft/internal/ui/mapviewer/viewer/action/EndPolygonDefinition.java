/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.mapviewer.viewer.action;


import java.awt.geom.Point2D;
import java.sql.Timestamp;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.io.ascii.aoi.AsciiAOIExportTask;
import org.geocraft.io.ascii.aoi.AsciiAOIMapper;
import org.geocraft.io.ascii.aoi.AsciiAOIMapperModel;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseActionList;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;


/**
 * The mouse action for ending a AOI polygon definition.
 */
public class EndPolygonDefinition extends AbstractPlotMouseAction {

  private final IMapViewer _viewer;

  private final IPlotPolygon _polygon;

  private static int _counter;

  public EndPolygonDefinition(final IMapViewer viewer, final IPlotPolygon polygon) {
    super(new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, 0), "Polygon End", "End the Polygon definition.");

    _viewer = viewer;
    _polygon = polygon;
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IPlot plot = _viewer.getPlot();

    _polygon.deselect();
    _polygon.rubberbandOff();
    plot.setMouseActions(PlotMouseActionList.getDefaultObjectActions().getActions(), SWT.CURSOR_ARROW);

    // Need to re-add the last point, as it is lost when rubber-banding is turned off.
    Point2D.Double modelCoord = event.getModelCoord();
    if (modelCoord != null) {
      double x = modelCoord.getX();
      double y = modelCoord.getY();
      IPlotPoint point = new PlotPoint(x, y, 0);
      point.setPropertyInheritance(true);
      _polygon.addPoint(point, PointInsertionMode.LAST);
    }

    _counter++;
    String defaultName = "MapViewerAOI" + _counter;

    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

    AsciiAOIExportDialog dialog = new AsciiAOIExportDialog(shell, defaultName);
    int result = dialog.open();
    if (result == IDialogConstants.OK_ID) {

      AsciiAOIMapperModel mapperModel = dialog.getModel();
      String aoiName = mapperModel.getFileName();

      MapPolygonAOI aoi = new MapPolygonAOI(aoiName);
      // TODO get the units from the map view. _mapView.getPlotModel().getAxisX().getUnits());
      int numPoints = _polygon.getPointCount();
      double[] x = new double[numPoints];
      double[] y = new double[numPoints];
      for (int i = 0; i < numPoints; i++) {
        x[i] = _polygon.getPoint(i).getX();
        y[i] = _polygon.getPoint(i).getY();
      }
      aoi.addInclusionPolygon(x, y);
      aoi.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
      if (mapperModel.getZRangeFlag()) {
        float zStart = mapperModel.getZStart();
        float zEnd = mapperModel.getZEnd();
        Unit zUnit = mapperModel.getZUnit();
        aoi.setZRange(zStart, zEnd, zUnit);
      }

      AsciiAOIExportTask task = new AsciiAOIExportTask();
      task.setMapperModel(mapperModel);
      task.setEntity(aoi);
      try {
        TaskRunner.runTask(task, "Writing ASCII AOI", TaskRunner.JOIN);
        aoi = new MapPolygonAOI(aoiName, new AsciiAOIMapper(mapperModel));
        aoi.load();
        ServiceProvider.getRepository().add(aoi);
        _viewer.getPlot().getActiveModelSpace().removeLayer(_polygon.getLayer());
        _viewer.addObjects(false, new Object[] { aoi });
      } catch (Exception ex) {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.getMessage());
      }
    } else {
      // If canceled, them simply remove the plot polygon layer.
      _viewer.getPlot().getActiveModelSpace().removeLayer(_polygon.getLayer());
    }
  }
}
