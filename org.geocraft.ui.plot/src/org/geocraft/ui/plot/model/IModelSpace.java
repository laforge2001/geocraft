/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.model;


import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.listener.IModelSpaceListener;
import org.geocraft.ui.plot.listener.IPlotLayerListener;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * Interface for a plot model space.
 */
public interface IModelSpace extends IPlotLayerListener {

  /**
   * Gets the model space name.
   * 
   * @return the model space name. 
   */
  String getName();

  /**
   * Sets the model space name.
   * 
   * @param name the model space name to set.
   */
  void setName(String name);

  /**
   * Gets the x-axis of the model.
   * 
   * @return the x-axis of the model.
   */
  IAxis getAxisX();

  /**
   * Gets the y-axis of the model.
   * 
   * @return the y-axis of the model.
   */
  IAxis getAxisY();

  ModelSpaceBounds getViewableBounds();

  ModelSpaceBounds getDefaultBounds();

  ModelSpaceBounds getMaximumBounds();

  boolean hasMaximumBounds();

  void setMaximumBounds(ModelSpaceBounds maxBounds);

  boolean isFixedAspectRatio();

  double aspectRatio();

  void setAspectRatio(double aspectRatio);

  IPlotLayer[] getLayers();

  void addLayer(IPlotLayer group);

  void addLayer(IPlotLayer group, boolean makeActive);

  void removeLayer(IPlotLayer group);

  boolean containsLayer(IPlotLayer group);

  IPlotLayer getActiveLayer();

  void addShape(final IPlotShape shape);

  void addShape(final IPlotShape shape, final IPlotLayer group);

  void addShapes(final IPlotShape[] shapes);

  void addShapes(final IPlotShape[] shapes, final IPlotLayer group);

  void redraw();

  void updated();

  /**
   * Adds a model space listener.
   * 
   * @param listener the model space listener to add.
   */
  void addListener(IModelSpaceListener listener);

  void addListener(IModelSpaceListener listener, boolean insertFirst);

  /**
   * Removes a model space listener.
   * 
   * @param listener the model space listener to remove.
   */
  void removeListener(IModelSpaceListener listener);

  void setDefaultBounds(double xStart, double xEnd, double yStart, double yEnd);

  void setViewableBounds(double xStart, double xEnd, double yStart, double yEnd);

  void setDefaultAndViewableBounds(final double xStart, final double xEnd, final double yStart, final double yEnd,
      Unit yUnit, String yLabel);

  void setDefaultAndViewableBounds(double xStartDefault, double xEndDefault, double yStartDefault, double yEndDefault,
      double xStartViewable, double xEndViewable, double yStartViewable, double yEndViewable);

  void dispose();

  void moveToTop(IPlotLayer layer);
}
