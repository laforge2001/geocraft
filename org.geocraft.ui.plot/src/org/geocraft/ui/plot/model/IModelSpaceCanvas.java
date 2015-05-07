/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.model;


import java.awt.geom.Point2D;
import java.util.List;

import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.ui.plot.IAxisCanvas;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.listener.ICursorListener;
import org.geocraft.ui.plot.object.IPlotMovableShape;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * The interface for a plot model space canvas.
 */
public interface IModelSpaceCanvas {

  /**
   * Gets the canvas component.
   * 
   * @return the canvas component.
   */
  Composite getComposite();

  /**
   * Gets the canvas size (in pixels).
   *  
   * @return the canvas size (in pixels).
   */
  Point getSize();

  /**
   * Sets the canvas size (in pixels).
   * 
   * @param size the canvas size (in pixels).
   */
  void setSize(Point size);

  /**
   * Sets the canvas size (in pixels).
   * 
   * @param width the canvas width (in pixels).
   * @param width the canvas height (in pixels).
   */
  void setSize(int width, int height);

  /**
   * Gets the preferred canvas size (in pixels).
   * 
   * @return the preferred canvas size (in pixels).
   */
  Point getPreferredSize();

  /**
   * Sets the preferred canvas size (in pixels).
   * 
   * @param size
   *            the preferred canvas size (in pixels).
   */
  void setPreferredSize(Point size);

  /**
   * Adds a plot cursor listener.
   * 
   * @param listener
   *            the plot cursor listener to add.
   */
  void addCursorListener(ICursorListener listener);

  /**
   * Removes a plot cursor listener.
   * 
   * @param listener
   *            the plot cursor listener to remove.
   */
  void removeCursorListener(ICursorListener listener);

  /**
   * Transforms pixel x,y coordinates to model x,y coordinates.
   * 
   * @param model the plot model space.
   * @param px the pixel x coordinate.
   * @param py the pixel y coordinate.
   * @return the model x,y coordinates.
   */
  void transformPixelToModel(IModelSpace model, int px, int py, Point2D.Double m);

  /**
   * Transforms model x,y coordinates to pixel x,y coordinates.
   * 
   * @param model the plot model space.
   * @param mx the model x coordinate.
   * @param my the model y coordinate.
   * @param the object to hold the computed pixel x,y coordinates.
   */
  void transformModelToPixel(IModelSpace model, double mx, double my, Point2D.Double p);

  /**
   * @param topLabelCanvas
   * @param leftLabelCanvas
   * @param rightLabelCanvas
   * @param bottomLabelCanvas
   */
  void setAxisCanvases(IAxisCanvas topLabelCanvas, IAxisCanvas leftLabelCanvas, IAxisCanvas rightLabelCanvas,
      IAxisCanvas bottomLabelCanvas);

  IPlotShape getActiveShape();

  void setActiveShape(IPlotShape shape);

  PointInsertionMode getPointInsertionMode();

  void setPointInsertionMode(PointInsertionMode mode);

  UpdateLevel getUpdateLevel();

  void update(UpdateLevel updateLevel);

  void update(UpdateLevel updateLevel, Rectangle mask);

  void dispose();

  IPlotPoint getNearestPoint(double px, double py);

  boolean getRubberband();

  void setRubberband(boolean rubberband);

  int getSelectionTolerance();

  IPlotPoint getNearestSelectablePoint(double px, double py);

  List<IPlotShape> getSelectedShapes();

  void setShapeInMotion(IPlotMovableShape shape);

  void setPointInMotion(IPlotPoint point);

  IPlotMovableShape getShapeInMotion();

  IPlotPoint getPointInMotion();

  IPlotPoint getNearestSelectablePoint(IPlotShape shape, double px, double py);

  IPlotPoint getNearestPoint(IPlotShape shape, double px, double py);

  void checkAspectRatio();

  IPlotPolygon getZoomRectangle();

  void setZoomRectangle(IPlotPolygon zoomRectangle);

  RGB getBackgroundColor();

  void setBackgroundColor(RGB color);

  void setMouseActions(IPlotMouseAction[] actions);

  void setCursorStyle(int style);

  void addMouseListener(MouseListener listener);

  void addMouseMoveListener(MouseMoveListener listener);

  void addControlListener(ControlListener listener);

  void removeControlListener(ControlListener listener);

  void setToolTipText(String text);

  void setGridLineProperties(LineStyle style, RGB color, int width);

  void setHorizontalAxisGridLineProperties(LineStyle style, RGB color, int width);

  void setVerticalAxisGridLineProperties(LineStyle style, RGB color, int width);

  void setHorizontalAxisGridLineDensity(int density);

  void setVerticalAxisGridLineDensity(int density);

  double computePixelDistance(IModelSpace modelSpace, double x0, double y0, double x1, double y1);
}
