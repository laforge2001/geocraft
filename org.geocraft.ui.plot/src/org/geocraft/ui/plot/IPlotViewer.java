/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;


/**
 * The base interface for layered viewers that are built on top of the
 * <code>org.geocraft.ui.plot</code> bundle.
 */
public interface IPlotViewer extends IPlot, IViewer {

  /**
   * Returns the underlying plot.
   */
  IPlot getPlot();

  /**
   * Returns the layer model associated with the viewer.
   */
  ILayeredModel getLayerModel();

  /**
   * Adds a layer to the root level of the layer model.
   * 
   * @param layer the layer to add.
   */
  void addLayerToRoot(final IViewLayer layer);

  /**
   * Removes a layer from the viewer.
   * 
   * If the layer does not exist in the viewer, no action will occur.
   * @param layer the layer to remove.
   */
  void removeLayer(final IViewLayer layer);

  /**
   * Returns an array of the current viewer layers.
   */
  IViewLayer[] getLayers();

}
