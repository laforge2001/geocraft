/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.CursorLocation;
import org.geocraft.core.model.event.CursorLocation.TimeOrDepth;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.CanvasType;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.event.PlotEvent;
import org.geocraft.ui.plot.internal.AxisRangeCanvas;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;
import org.geocraft.ui.plot.listener.IPlotListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotShape;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPointGroup;


/**
 * The base implementation of the plot interface.
 */
public class Plot implements IPlot {

  /** The scrolled flag. */
  private final PlotScrolling _scrolling;

  /** The set of model spaces. */
  private final Set<IModelSpace> _modelSpaces;

  /** The active model space. */
  private IModelSpace _modelSpaceActive;

  private final PlotComposite _plotComposite;

  /** The set of plot listeners. */
  private final Set<IPlotListener> _listeners;

  private double _zoomFactor;

  /** A flag for the cursor broadcast status. */
  private boolean _broadcastCursor;

  private boolean _showCursorToolTip;

  private IPlotPointGroup _cursorTracker;

  private boolean _broadcastCursorReceive;

  private boolean _renderActiveModelOnly;

  private int _screenResolution;

  /**
   * Constructs a simple plot with the specified title and default model.
   */
  public Plot(final Composite parent, final IModelSpace modelSpace, final PlotScrolling scrolled) {
    _scrolling = scrolled;
    _modelSpaces = Collections.synchronizedSet(new HashSet<IModelSpace>());
    _modelSpaces.add(modelSpace);
    modelSpace.addListener(this);
    _modelSpaceActive = modelSpace;
    _listeners = Collections.synchronizedSet(new HashSet<IPlotListener>());
    _zoomFactor = 2;
    _broadcastCursor = false;
    _showCursorToolTip = false;
    _renderActiveModelOnly = false;
    _plotComposite = new PlotComposite(parent, this);

    // Determine the screen resolution.
    _screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();

  }

  public PlotComposite getPlotComposite() {
    return _plotComposite;
  }

  public PlotScrolling getScrolling() {
    return _scrolling;
  }

  public String getTitle() {
    return _plotComposite.getTitleCanvas().getLabel().getText();
  }

  public void setTitle(final String title) {
    _plotComposite.getTitleCanvas().getLabel().setText(title);
    _plotComposite.getTitleCanvas().redraw();
  }

  public IModelSpace[] getModelSpaces() {
    return _modelSpaces.toArray(new IModelSpace[0]);
  }

  public void addModelSpace(final IModelSpace modelSpace) {
    if (!_modelSpaces.contains(modelSpace)) {
      _modelSpaces.add(modelSpace);
      modelSpace.addListener(this);
      _plotComposite.update();
    }
  }

  public void removeModelSpace(final IModelSpace model) {
    _modelSpaces.remove(model);
    _plotComposite.update();
  }

  public void addListener(final IPlotListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(final IPlotListener listener) {
    _listeners.remove(listener);
  }

  public synchronized void modelSpaceUpdated(final ModelSpaceEvent event) {
    if (_plotComposite == null) {
      return;
    }
    PlotEventType eventType = event.getEventType();
    IModelSpaceCanvas canvas = _plotComposite.getModelSpaceCanvas();
    IPlotLayer group = event.getGroup();
    IPlotShape shape = event.getShape();

    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)) {
      for (IModelSpace modelSpace : getModelSpaces()) {
        for (IPlotLayer plotLayer : modelSpace.getLayers()) {
          plotLayer.block();
          plotLayer.modelSpaceUpdated(event);
          plotLayer.unblock();
        }
      }
    }

    if (eventType.equals(PlotEventType.SHAPE_SELECTED)) {
      canvas.update(UpdateLevel.REDRAW);
      canvas.getComposite().update();
    } else if (eventType.equals(PlotEventType.SHAPE_DESELECTED)) {
      if (shape != null && shape.equals(canvas.getActiveShape())) {
        canvas.setActiveShape(null);
      }
      canvas.update(UpdateLevel.REDRAW);
      canvas.getComposite().update();
    }
    if (group != null && !eventType.equals(PlotEventType.AXIS_UPDATED)
        && !eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)) {
      if (shape != null) {
        if (shape.isSelected()) {
          canvas.update(UpdateLevel.REFRESH);
        } else {
          Rectangle mask = shape.getRectangle(canvas);
          if (!shape.isSelected() && mask != null) {
            canvas.update(UpdateLevel.REDRAW, mask);
          } else {
            canvas.update(UpdateLevel.REDRAW);
          }
        }
      } else {
        canvas.update(UpdateLevel.REDRAW);
      }
    } else {
      _plotComposite.updateAll();
    }
    //    if (eventType.equals(EventType.ModelRedraw)) {
    //      UpdateLevel updateLevel = event.getUpdateLevel();
    //      if (updateLevel != null) {
    //        IModelSpaceCanvas canvas = _plotComposite.getModelSpaceCanvas();
    //        canvas.update(updateLevel);
    //      }
    //    } else if (eventType.equals(EventType.AxisUpdated) || eventType.equals(EventType.BoundsUpdated)) {
    //      IModelSpaceCanvas canvas = _plotComposite.getModelSpaceCanvas();
    //      canvas.update(UpdateLevel.Resize);
    //    }
    PlotEvent plotEvent = new PlotEvent(event.getEventType(), this);
    notifyListeners(plotEvent);
  }

  /**
   * Notifies listeners that the plot has been updated.
   * @param plotEvent the event to send to the plot listeners.
   */
  private void notifyListeners(final PlotEvent plotEvent) {
    IPlotListener[] listeners = _listeners.toArray(new IPlotListener[0]);
    for (IPlotListener listener : listeners) {
      listener.plotUpdated(plotEvent);
    }
  }

  public void dispose() {
    _modelSpaces.clear();
    _listeners.clear();
    _plotComposite.dispose();
  }

  public IModelSpace getActiveModelSpace() {
    return _modelSpaceActive;
  }

  public void setActiveModelSpace(final IModelSpace modelSpace) {
    if (_modelSpaces.contains(modelSpace)) {
      _modelSpaceActive = modelSpace;
      _plotComposite.setAxes(modelSpace);
      _plotComposite.updateAll();
    } else {
      throw new IllegalArgumentException("The specified model space does not exist in the plot.");
    }
  }

  public void addLayer(final IPlotLayer layer) {
    addLayer(layer, false, false);
  }

  public void addLayer(final IPlotLayer layer, final boolean adjustViewableBounds, final boolean adjustDefaultBounds) {
    IModelSpace modelSpace = getActiveModelSpace();
    modelSpace.addLayer(layer);
    adjustBounds(modelSpace, adjustViewableBounds, adjustDefaultBounds);
  }

  public void moveToTop(final IPlotLayer layer) {
    for (IModelSpace modelSpace : getModelSpaces()) {
      modelSpace.moveToTop(layer);
    }
  }

  public void adjustBounds(IModelSpace modelSpace, final boolean adjustViewableBounds, final boolean adjustDefaultBounds) {

    IAxis xAxis = modelSpace.getAxisX();
    IAxis yAxis = modelSpace.getAxisY();

    double xStartViewable = Double.MAX_VALUE;
    double xEndViewable = -Double.MAX_VALUE;
    double yStartViewable = Double.MAX_VALUE;
    double yEndViewable = -Double.MAX_VALUE;
    double xStartDefault = Double.MAX_VALUE;
    double xEndDefault = -Double.MAX_VALUE;
    double yStartDefault = Double.MAX_VALUE;
    double yEndDefault = -Double.MAX_VALUE;
    if (xAxis.getViewableStart() > xAxis.getViewableEnd()) {
      xStartViewable = -Double.MAX_VALUE;
      xEndViewable = Double.MAX_VALUE;
    }
    if (yAxis.getViewableStart() > yAxis.getViewableEnd()) {
      yStartViewable = -Double.MAX_VALUE;
      yEndViewable = Double.MAX_VALUE;
    }
    if (xAxis.getDefaultStart() > xAxis.getDefaultEnd()) {
      xStartDefault = -Double.MAX_VALUE;
      xEndDefault = Double.MAX_VALUE;
    }
    if (yAxis.getDefaultStart() > yAxis.getDefaultEnd()) {
      yStartDefault = -Double.MAX_VALUE;
      yEndDefault = Double.MAX_VALUE;
    }
    for (IPlotLayer plotLayer : modelSpace.getLayers()) {
      ModelSpaceBounds bounds = plotLayer.getBounds();
      if (bounds.isValid()) {
        double xmin = Math.min(bounds.getStartX(), bounds.getEndX());
        double xmax = Math.max(bounds.getStartX(), bounds.getEndX());
        double ymin = Math.min(bounds.getStartY(), bounds.getEndY());
        double ymax = Math.max(bounds.getStartY(), bounds.getEndY());
        if (adjustViewableBounds) {
          if (xAxis.getViewableStart() <= xAxis.getViewableEnd()) {
            xStartViewable = Math.min(xStartViewable, xmin);
            xEndViewable = Math.max(xEndViewable, xmax);
          } else {
            xStartViewable = Math.max(xStartViewable, xmax);
            xEndViewable = Math.min(xEndViewable, xmin);
          }
          if (yAxis.getViewableStart() <= yAxis.getViewableEnd()) {
            yStartViewable = Math.min(yStartViewable, ymin);
            yEndViewable = Math.max(yEndViewable, ymax);
          } else {
            yStartViewable = Math.max(yStartViewable, ymax);
            yEndViewable = Math.min(yEndViewable, ymin);
          }
        }
        if (adjustDefaultBounds) {
          if (xAxis.getDefaultStart() <= xAxis.getDefaultEnd()) {
            xStartDefault = Math.min(xStartDefault, xmin);
            xEndDefault = Math.max(xEndDefault, xmax);
          } else {
            xStartDefault = Math.max(xStartDefault, xmax);
            xEndDefault = Math.min(xEndDefault, xmin);
          }
          if (yAxis.getDefaultStart() <= yAxis.getDefaultEnd()) {
            yStartDefault = Math.min(yStartDefault, ymin);
            yEndDefault = Math.max(yEndDefault, ymax);
          } else {
            yStartDefault = Math.max(yStartDefault, ymax);
            yEndDefault = Math.min(yEndDefault, ymin);
          }
        }
      }
    }
    if (adjustDefaultBounds && adjustViewableBounds) {
      modelSpace.setDefaultAndViewableBounds(xStartDefault, xEndDefault, yStartDefault, yEndDefault, xStartViewable,
          xEndViewable, yStartViewable, yEndViewable);
    } else {
      if (adjustViewableBounds) {
        modelSpace.setViewableBounds(xStartViewable, xEndViewable, yStartViewable, yEndViewable);
      }
      if (adjustDefaultBounds) {
        modelSpace.setDefaultBounds(xStartDefault, xEndDefault, yStartDefault, yEndDefault);
      }
    }

    _plotComposite.redraw();
  }

  /**
   * Zooms each model contained in the plot by the specified zoom factor,
   * at the center of the plot window.
   * @param zoomFactor the zoom factor.
   */
  public void zoom(final double zoomFactor) {
    Point size = _plotComposite.getModelSpaceCanvas().getSize();
    zoom(zoomFactor, size.x / 2, size.y / 2);
  }

  /**
   * Zooms each model contained in the plot by the specified zoom factors,
   * at the center of the plot window.
   * @param horizontal zoomFactor the horizontal zoom factor.
   * @param vertical zoomFactor the vertical zoom factor.
   */
  public void zoom(final double horizontalZoomFactor, final double verticalZoomFactor) {
    Point size = _plotComposite.getModelSpaceCanvas().getSize();
    zoom(horizontalZoomFactor, verticalZoomFactor, size.x / 2, size.y / 2);
  }

  /**
   * Zooms each model contained in the plot by the specified zoom factor,
   * at the x,y pixel coordinates in the specified mouse event.
   * @param event the mouse event containing the x,y pixel coordinates.
   * @param zoomFactor the zoom factor.
   */
  public void zoom(final MouseEvent event, final double zoomFactor) {
    zoom(zoomFactor, event.getX(), event.getY());
  }

  /**
   * Zooms each model contained in the plot by the specified zoom factor,
   * at the specified x,y pixel coordinates.
   * @param zoomFactor the zoom factor.
   * @param xPixel the x pixel coordinate.
   * @param yPixel the y pixel coordinate.
   */
  public void zoom(final double zoomFactor, final int xPixel, final int yPixel) {
    zoom(zoomFactor, zoomFactor, xPixel, yPixel);
  }

  /**
   * Zooms each model contained in the plot by the specified zoom factor,
   * at the specified x,y pixel coordinates.
   * @param horizontalZoomFactor the horizontal zoom factor.
   * @param verticalZoomFactor the vertical zoom factor.
   * @param xPixel the x pixel coordinate.
   * @param yPixel the y pixel coordinate.
   */
  public void zoom(final double horizontalZoomFactor, final double verticalZoomFactor, final int xPixel,
      final int yPixel) {
    IModelSpaceCanvas canvas = _plotComposite.getModelSpaceCanvas();
    Point size = canvas.getSize();

    // If the plot allows for scrolling, then the handling of zooming
    // needs to be done differently than without scrolling.
    if (_scrolling != PlotScrolling.NONE) {
      //      // Compare the proposed new area to the old area...
      //      int oldArea = size.x * size.y;
      //      int newWidthInPixels = Math.round(size.x * (float) horizontalZoomFactor);
      //      int newHeightInPixels = Math.round(size.y * (float) verticalZoomFactor);
      //      int newArea = newWidthInPixels * newHeightInPixels;
      //
      //      // Compute the width and height of the drawing area (in pixels).
      //      float newWidthInInches = (float) newWidthInPixels / _screenResolution;
      //      float newHeightInInches = (float) newHeightInPixels / _screenResolution;
      //      float newAreaInSqInches = newWidthInInches * newHeightInInches;
      //
      //      // Simply change the size of the canvas if either of the
      //      // following conditions are met:
      //      // 1) The new area is smaller than the old area.
      //      // 2) The new area is smaller than the maximum area.
      //      if (newArea > oldArea && newAreaInSqInches < MAX_IMAGE_SIZE) {
      //        size = new Point(newWidthInPixels, newHeightInPixels);
      //        canvas.setSize(size);
      //      }
    }
    for (IModelSpace modelSpace : getModelSpaces()) {
      Point2D.Double modelCoord = new Point2D.Double();
      canvas.transformPixelToModel(modelSpace, xPixel, yPixel, modelCoord);

      // Only zoom the axes if the scale is LINEAR.
      // Zooming of LOG scales is not supported.
      IAxis xAxis = modelSpace.getAxisX();
      double xStart = xAxis.getViewableStart();
      double xEnd = xAxis.getViewableEnd();
      if (xAxis.getScale().equals(AxisScale.LINEAR)) {
        double xModel = modelCoord.x;
        double xToStart = xAxis.getViewableStart() - xModel;
        double xToEnd = xAxis.getViewableEnd() - xModel;
        xToStart /= horizontalZoomFactor;
        xToEnd /= horizontalZoomFactor;
        xStart = xModel + xToStart;
        xEnd = xModel + xToEnd;
      }
      IAxis yAxis = modelSpace.getAxisY();
      double yStart = yAxis.getViewableStart();
      double yEnd = yAxis.getViewableEnd();
      if (yAxis.getScale().equals(AxisScale.LINEAR)) {
        double yModel = modelCoord.y;
        double yToStart = yAxis.getViewableStart() - yModel;
        double yToEnd = yAxis.getViewableEnd() - yModel;
        yToStart /= verticalZoomFactor;
        yToEnd /= verticalZoomFactor;
        yStart = yModel + yToStart;
        yEnd = yModel + yToEnd;
      }
      if (modelSpace.hasMaximumBounds()) {
        ModelSpaceBounds maxBounds = modelSpace.getMaximumBounds();
        if (xStart <= xEnd) {
          xStart = Math.max(xStart, maxBounds.getStartX());
          xEnd = Math.min(xEnd, maxBounds.getEndX());
        } else {
          xStart = Math.min(xStart, maxBounds.getStartX());
          xEnd = Math.max(xEnd, maxBounds.getEndX());
        }
        if (yStart <= yEnd) {
          yStart = Math.max(yStart, maxBounds.getStartY());
          yEnd = Math.min(yEnd, maxBounds.getEndY());
        } else {
          yStart = Math.min(yStart, maxBounds.getStartY());
          yEnd = Math.max(yEnd, maxBounds.getEndY());
        }
      }
      ModelSpaceBounds viewableBounds = checkAspectRatio(modelSpace, modelSpace.getDefaultBounds(),
          new ModelSpaceBounds(xStart, xEnd, yStart, yEnd), size);
      modelSpace.setViewableBounds(viewableBounds.getStartX(), viewableBounds.getEndX(), viewableBounds.getStartY(),
          viewableBounds.getEndY());
      canvas.transformPixelToModel(modelSpace, xPixel, yPixel, modelCoord);

    }
  }

  private ModelSpaceBounds checkAspectRatio(final IModelSpace plotSpace, final ModelSpaceBounds defaultBounds,
      final ModelSpaceBounds viewableBounds, final Point size) {
    if (!plotSpace.isFixedAspectRatio()) {
      return viewableBounds;
    }
    double aspectRatio = plotSpace.aspectRatio();
    double currentPixelAR = (double) size.y / size.x;
    double dxPref = defaultBounds.getEndX() - defaultBounds.getStartX();
    double dyPref = defaultBounds.getEndY() - defaultBounds.getStartY();
    double dxModel = viewableBounds.getEndX() - viewableBounds.getStartX();
    double dyModel = viewableBounds.getEndY() - viewableBounds.getStartY();
    double xCtr = (viewableBounds.getStartX() + viewableBounds.getEndX()) / 2;
    double yCtr = (viewableBounds.getStartY() + viewableBounds.getEndY()) / 2;
    double preferredModelAR = dyPref / dxPref;
    // double currentModelAR = dyModel / dxModel;
    // double currentAR = currentPixelAR / currentModelAR;
    double preferredAR = currentPixelAR / preferredModelAR;
    double updatedModelAR = currentPixelAR / aspectRatio;
    double dx = dxModel;
    double dy = dyModel;
    if (preferredAR < aspectRatio) {
      double dy1 = Math.abs(defaultBounds.getEndY() - defaultBounds.getStartY());
      double dy2 = Math.abs(viewableBounds.getEndY() - viewableBounds.getStartY());
      dy = dy2;// Math.min(dy1, dy2);
      dx = dy / updatedModelAR;
    } else if (preferredAR > aspectRatio) {
      double dx1 = Math.abs(defaultBounds.getEndX() - defaultBounds.getStartX());
      double dx2 = Math.abs(viewableBounds.getEndX() - viewableBounds.getStartX());
      dx = dx2;// Math.min(dx1, dx2);
      dy = dx * updatedModelAR;
    }
    int xSign = 1;
    if (viewableBounds.getStartX() > viewableBounds.getEndX()) {
      xSign = -1;
    }
    double xStart = xCtr - xSign * dx / 2;
    double xEnd = xCtr + xSign * dx / 2;
    int ySign = 1;
    if (viewableBounds.getStartY() > viewableBounds.getEndY()) {
      ySign = -1;
    }
    double yStart = yCtr - ySign * dy / 2;
    double yEnd = yCtr + ySign * dy / 2;
    return new ModelSpaceBounds(xStart, xEnd, yStart, yEnd);
  }

  public double getZoomFactor() {
    return _zoomFactor;
  }

  public void setZoomFactor(final double zoomFactor) {
    _zoomFactor = zoomFactor;
  }

  public boolean getCursorBroadcast() {
    return _broadcastCursor;
  }

  public void setCursorBroadcast(final boolean broadcast) {
    _broadcastCursor = broadcast;
    if (_cursorTracker != null) {
      _cursorTracker.setVisible(broadcast);
    }
  }

  public boolean getCursorReception() {
    return _broadcastCursorReceive;
  }

  public void setCursorReception(final boolean receive) {
    _broadcastCursorReceive = receive;
    if (_cursorTracker != null) {
      _cursorTracker.setVisible(receive);
    }
  }

  public void unzoom() {
    for (IModelSpace modelSpace : getModelSpaces()) {
      unzoom(modelSpace);
    }
  }

  public void unzoom(final IModelSpace modelSpace) {
    ModelSpaceBounds defaultBounds = modelSpace.getDefaultBounds();
    double xStart = defaultBounds.getStartX();
    double xEnd = defaultBounds.getEndX();
    double yStart = defaultBounds.getStartY();
    double yEnd = defaultBounds.getEndY();
    modelSpace.setViewableBounds(xStart, xEnd, yStart, yEnd);
    _plotComposite.getModelSpaceCanvas().checkAspectRatio();
  }

  public Composite getToolBarContainer() {
    return _plotComposite.getToolBarContainer();
  }

  public void setMouseActions(final IPlotMouseAction[] actions, final int cursorStyle) {
    _plotComposite.getModelSpaceCanvas().setMouseActions(actions);
    _plotComposite.getModelSpaceCanvas().setCursorStyle(cursorStyle);
  }

  public void setMouseActions(final IPlotMouseAction[] actions, final Cursor cursor) {
    _plotComposite.getModelSpaceCanvas().setMouseActions(actions);
    _plotComposite.getModelSpaceCanvas().getComposite().setCursor(cursor);
  }

  public void cursorTracked(final double x, final double y) {
    if (_cursorTracker == null) {
      RGB color = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW).getRGB();
      _cursorTracker = new PlotPointGroup("Cursor", new TextProperties(), new PointProperties(PointStyle.CROSS, color,
          20));
      IPlotPoint point = new PlotPoint(0, 0, 0);
      point.setPropertyInheritance(true);
      _cursorTracker.addPoint(point);
      IPlotLayer cursorLayer = new PlotLayer("Cursor", false);
      cursorLayer.addShape(_cursorTracker);
      _cursorTracker.select();
      addLayer(cursorLayer, false, false);
    }
    _cursorTracker.getPoint(0).moveTo(x, y);
  }

  public void cursorUpdated(final double x, final double y, final boolean broadcast) {
    _plotComposite.cursorUpdated(x, y, broadcast);
  }

  public void cursorSelectionUpdated(final double x, final double y) {
    if (_broadcastCursor) {
      ServiceProvider.getMessageService().publish(
          Topic.CURSOR_SELECTION_LOCATION,
          new CursorLocation(new Coordinate(new Point3d(x, y, 0), ApplicationPreferences.getInstance()
              .getTimeCoordinateSystem()), TimeOrDepth.NONE, "" + hashCode()));
    }
  }

  public RGB getBackgroundPlotColor() {
    return _plotComposite.getModelSpaceCanvas().getBackgroundColor();
  }

  public void setBackgroundPlotColor(final RGB color) {
    _plotComposite.getModelSpaceCanvas().setBackgroundColor(color);
    _plotComposite.updateAll();
  }

  public boolean showCursorToolTip() {
    return _showCursorToolTip;
  }

  public void showCursorToolTip(final boolean showToolTip) {
    _showCursorToolTip = showToolTip;
  }

  public boolean renderActiveModelOnly() {
    return _renderActiveModelOnly;
  }

  public void renderActiveModelOnly(final boolean renderActiveModelOnly) {
    _renderActiveModelOnly = renderActiveModelOnly;
    _plotComposite.updateAll();
  }

  public void setHorizontalAxisGridLineDensity(final int density) {
    _plotComposite.getModelSpaceCanvas().setHorizontalAxisGridLineDensity(density);
  }

  public void setVerticalAxisGridLineDensity(final int density) {
    _plotComposite.getModelSpaceCanvas().setVerticalAxisGridLineDensity(density);
  }

  public void setHorizontalAxisAnnotationDensity(final int density) {
    AxisRangeCanvas canvas1 = (AxisRangeCanvas) _plotComposite.getCanvasMap().get(CanvasType.TOP_AXIS_RANGE);
    AxisRangeCanvas canvas2 = (AxisRangeCanvas) _plotComposite.getCanvasMap().get(CanvasType.BOTTOM_AXIS_RANGE);
    canvas1.setLabelDensity(density);
    canvas2.setLabelDensity(density);
  }

  public void setVerticalAxisAnnotationDensity(final int density) {
    AxisRangeCanvas canvas1 = (AxisRangeCanvas) _plotComposite.getCanvasMap().get(CanvasType.LEFT_AXIS_RANGE);
    AxisRangeCanvas canvas2 = (AxisRangeCanvas) _plotComposite.getCanvasMap().get(CanvasType.RIGHT_AXIS_RANGE);
    canvas1.setLabelDensity(density);
    canvas2.setLabelDensity(density);
  }

  public IModelSpaceCanvas getModelSpaceCanvas() {
    return _plotComposite.getModelSpaceCanvas();
  }

  public CanvasLayoutModel getCanvasLayoutModel() {
    return _plotComposite.getCanvasLayoutModel();
  }

  public void setCanvasLayoutModel(final CanvasLayoutModel model) {
    _plotComposite.setCanvasLayoutModel(model);
  }

  public void setCursorFormatterX(final NumberFormat formatter) {
    _plotComposite.setCursorFormatterX(formatter);
  }

  public void setCursorFormatterY(final NumberFormat formatter) {
    _plotComposite.setCursorFormatterY(formatter);
  }

  public void updateCanvasLayout(final CanvasLayoutModel model) {
    _plotComposite.updateCanvasLayout(model);
  }

  public void editCanvasLayout() {
    _plotComposite.editCanvasLayout();
  }

  public void updateAll() {
    _plotComposite.updateAll();
  }
}
