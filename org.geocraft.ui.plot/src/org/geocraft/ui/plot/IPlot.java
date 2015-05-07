/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import java.text.NumberFormat;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;
import org.geocraft.ui.plot.listener.ICursorListener;
import org.geocraft.ui.plot.listener.IModelSpaceListener;
import org.geocraft.ui.plot.listener.IPlotListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


/**
 * The interface for a plot.
 * A plot consists of a title canvas, axis canvases and optional scrollbars, as well
 * as a drawing area for rendering one or more model spaces.
 * @see IModelSpace
 */
public interface IPlot extends IModelSpaceListener, ICursorListener {

  /** The maximum image size, in square inches. */
  public final int MAX_IMAGE_SIZE = 800;

  /**
   * Returns the type of plot scrolling.
   * 
   * @return the type of plot scrolling (horizontal, vertical, both, etc).
   */
  PlotScrolling getScrolling();

  /**
   * Returns the plot composite.
   * NOTE: The plot composite should not need to be accessed directory.
   */
  PlotComposite getPlotComposite();

  /**
   * Returns the plot title.
   */
  String getTitle();

  /**
   * Sets the plot title.
   */
  void setTitle(String title);

  /**
   * Returns an array of the model spaces contained in the plot.
   */
  IModelSpace[] getModelSpaces();

  /**
   * Returns the model space that is currently active.
   */
  IModelSpace getActiveModelSpace();

  /**
   * Sets the model space to make active.
   */
  void setActiveModelSpace(IModelSpace modelSpace);

  /**
   * Adds a model space to the plot.
   */
  void addModelSpace(IModelSpace model);

  /**
   * Removes a model space from the plot.
   */
  void removeModelSpace(IModelSpace model);

  /**
   * Adds a listener for plot events.
   */
  void addListener(IPlotListener listener);

  /**
   * Removes a listener for plot events.
   */
  void removeListener(IPlotListener listener);

  /**
   * Disposes of the plot resources.
   */
  void dispose();

  /**
   * Adds a layer to the plot.
   * 
   * @param layer the layer to add.
   */
  void addLayer(IPlotLayer layer);

  /**
   * Adds a layer to the plot.
   * The plot can be triggered to auto-adjust its viewable and default bounds
   * based on the added layer.
   * 
   * @param layer the layer to add.
   * @param adjustViewableBounds flag to auto-adjust the viewable bounds.
   * @param adjustDefaultBounds flag to auto-adjust the default bounds.
   */
  void addLayer(IPlotLayer layer, boolean adjustViewableBounds, boolean adjustDefaultBounds);

  void adjustBounds(IModelSpace modelSpace, boolean adjustViewableBounds, boolean adjustDefaultBounds);

  /**
   * Unzooms (resets) all the model spaces to their default bounds.
   */
  void unzoom();

  /**
   * Unzooms (resets) the specified model space to its default bounds.
   * 
   * @param modelSpace the model space to unzoom.
   */
  void unzoom(IModelSpace modelSpace);

  /**
   * Zooms the plot by the specified zoom factor.
   * 
   * @param zoomFactor the zoom factor.
   */
  void zoom(double zoomFactor);

  /**
   * Zooms each model contained in the plot by the specified zoom factor,
   * at the specified x,y pixel coordinates.
   * 
   * @param zoomFactor the zoom factor.
   * @param xPixel the x pixel coordinate.
   * @param yPixel the y pixel coordinate.
   */
  void zoom(final double zoomFactor, final int xPixel, final int yPixel);

  /**
   * Zooms the plot by the specified zoom factors.
   * 
   * @param horizontalZoomFactor the horizontal zoom factor.
   * @param verticalZoomFactor the vertical zoom factor.
   */
  void zoom(final double horizontalZoomFactor, double verticalZoomFactor);

  /**
   * Zooms each model contained in the plot by the specified zoom factor,
   * at the specified x,y pixel coordinates.
   * 
   * @param horizontalZoomFactor the horizontal zoom factor.
   * @param verticalZoomFactor the vertical zoom factor.
   * @param xPixel the x pixel coordinate.
   * @param yPixel the y pixel coordinate.
   */
  void zoom(final double horizontalZoomFactor, double verticalZoomFactor, final int xPixel, final int yPixel);

  /**
   * Returns the  zoom-in/zoom-out factor for the plot.
   */
  double getZoomFactor();

  /**
   * Sets the zoom-in/zoom-out factor for the plot.
   */
  void setZoomFactor(double zoomFactor);

  /**
   * Return the status of the cursor broadcast; <i>true</i> if enabled, <i>false</i> if not.
   */
  boolean getCursorBroadcast();

  /**
   * Sets the status of the cursor broadcast.
   * 
   * @param broadcast <i>true</i> to broadcast, else <i>false</i>.
   */
  void setCursorBroadcast(boolean broadcast);

  /**
   * Return the status of the cursor reception; <i>true</i> if enabled, <i>false</i> if not.
   */
  boolean getCursorReception();

  /**
   * Sets the status of the cursor reception.
   * 
   * @param reception <i>true</i> to receive, else <i>false</i>.
   */
  void setCursorReception(boolean reception);

  /**
   * Returns the composite container in which the shared and any custom toolbars will reside.
   */
  Composite getToolBarContainer();

  /**
   * Sets the mouse actions to apply for the plot, along with a cursor.
   * Current mouse actions will be discarded or overridden.
   * 
   * @param actions the array of mouse actions to set.
   * @param cursorStyle the predefined SWT cursor style.
   */
  void setMouseActions(IPlotMouseAction[] actions, int cursorStyle);

  /**
   * Sets the mouse actions to apply for the plot, along with a cursor.
   * Current mouse actions will be discarded or overridden.
   * 
   * @param actions the array of mouse actions to set.
   * @param cursorStyle a custom cursor.
   */
  void setMouseActions(IPlotMouseAction[] actions, Cursor cursor);

  /**
   * Returns the background color of the plot.
   * This is the background color for the model space only.
   */
  RGB getBackgroundPlotColor();

  /**
   * Sets the background color of the plot.
   * This is the background color for the model space only.
   * @param color the background color to set.
   */
  void setBackgroundPlotColor(RGB color);

  /**
   * Returns the visibility of the cursor tooltip.
   * 
   * @return <i>true</i> if shown; <i>false</i> if not.
   */
  boolean showCursorToolTip();

  /**
   * Sets the visibility of the cursor tooltip.
   * 
   * @param showToolTip <i>true</i> to show the cursor tooltip; <i>false</i> to hide it.
   */
  void showCursorToolTip(boolean showToolTip);

  /**
   * Returns the flag for rendering only the active model space.
   * 
   * @return <i>true</i> if only the active model space if to be rendered; otherwise <i>false</i>.
   */
  boolean renderActiveModelOnly();

  /**
   * Sets the flag for rendering only the active model space.
   * 
   * @param renderActiveModelOnly <i>true</i> to render only the active model space; <i>false</i> to render all model spaces.
   */
  void renderActiveModelOnly(boolean renderActiveModelOnly);

  /**
   * Sets the density of the horizontal (x) axis grid lines.
   * The density is a relative value, as the actual number of grid lines
   * will be computed automatically based on the density value.
   * 
   * @param density the horizontal density.
   */
  void setHorizontalAxisGridLineDensity(int density);

  /**
   * Sets the density of the vertical (y) axis grid lines.
   * The density is a relative value, as the actual number of grid lines
   * will be computed automatically based on the density value.
   * 
   * @param density the vertical density.
   */
  void setVerticalAxisGridLineDensity(int density);

  /**
   * Sets the density of the horizontal (x) axis annotations.
   * The density is a relative value, as the actual number of annotations
   * will be computed automatically based on the density value.
   * 
   * @param density the horizontal density.
   */
  void setHorizontalAxisAnnotationDensity(int density);

  /**
   * Sets the density of the vertical (y) axis annotations.
   * The density is a relative value, as the actual number of annotations
   * will be computed automatically based on the density value.
   * 
   * @param density the vertical density.
   */
  void setVerticalAxisAnnotationDensity(int density);

  /**
   * Moves the tracking cursor in the plot to the specified model x,y.
   */
  void cursorTracked(final double x, final double y);

  /**
   * Returns the plot canvas in which the contents of the model spaces will
   * actually be rendered. This canvas is the central component of the plot.
   * 
   * @return the model space canvas.
   */
  IModelSpaceCanvas getModelSpaceCanvas();

  /**
   * Returns the canvas layout model of the plot.
   */
  CanvasLayoutModel getCanvasLayoutModel();

  /**
   * Sets the canvas layout model of the plot.
   */
  void setCanvasLayoutModel(CanvasLayoutModel model);

  /**
   * Updates the canvas layout model of the plot.
   * @param model
   */
  void updateCanvasLayout(CanvasLayoutModel model);

  /**
   * Brings up a dialog for editing the layout (margins and widths)
   * of the various plot components (model space canvas, title canvas,
   * axis canvases, etc).
   */
  void editCanvasLayout();

  /**
   * Sets a custom number formatter for rendering annotation values
   * for the horizontal (x) axis.
   */
  void setCursorFormatterX(NumberFormat formatter);

  /**
   * Sets a custom number formatter for rendering annotation values
   * for the vertical (y) axis.
   */
  void setCursorFormatterY(NumberFormat formatter);

  /**
   * Triggers all portions of the plot to update.
   */
  void updateAll();

  void moveToTop(IPlotLayer layer);

}
