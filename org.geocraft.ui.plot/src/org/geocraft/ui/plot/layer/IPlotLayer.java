/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.layer;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.listener.IPlotLayerListener;
import org.geocraft.ui.plot.listener.IPlotShapeListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotShape;
import org.geocraft.ui.viewer.ReadoutInfo;


/**
 * The interface for a plot layer (i.e. a collection of plot shapes).
 */
public interface IPlotLayer extends IPlotShapeListener {

  /**
   * Gets the layer name.
   * @return the layer name.
   */
  String getName();

  /**
   * Sets the layer name.
   * @param name the layer name.
   */
  void setName(String name);

  /**
   * Gets the layer icon.
   * @return the layer image.
   */
  Image getImage();

  /**
   * Sets the layer icon.
   * <p>
   * The image is assumed to NOT be disposable.
   * 
   * @param image the layer image.
   */
  void setImage(final Image image);

  /**
   * Sets the layer icon.
   * @param image the layer image.
   * @param flag indicating whether image is disposable.
   */
  void setImage(final Image image, final boolean imageIsDisposable);

  /**
   * Gets the model space to which the layer is associated.
   * @return the model space to which the layer is associated.
   */
  IModelSpace getModelSpace();

  /**
   * Sets the model space to which the layer is associated.
   * @param modelSpace the model space to which the layer is associated.
   */
  void setModelSpace(IModelSpace modelSpace);

  /**
   * Returns the visibility of the layer.
   * @return true if visible; false if not.
   */
  boolean isVisible();

  /**
   * Sets the visibility of the layer.
   * @param visible true for visible; otherwise false.
   */
  void setVisible(boolean visible);

  /**
   * Gets the shapes in the layer.
   * @return the shapes in the layer.
   */
  IPlotShape[] getShapes();

  /**
   * Adds an shape to the layer.
   * @param shape the shape to add.
   */
  void addShape(IPlotShape shape);

  /**
   * Adds an shape to the layer.
   * @param shape the shape to add.
   * @param selected true to auto-select the shape, false otherwise.
   */
  void addShape(IPlotShape shape, boolean selected);

  /**
   * Adds shapes to the layer.
   * @param shapes the shapes to add.
   */
  void addShapes(IPlotShape[] shapes);

  /**
   * Removes a shape from the layer.
   * @param shape the shape to remove.
   */
  void removeShape(IPlotShape shape);

  /**
   * Clears all shapes from the layer.
   */
  void clear();

  /**
   * Adds a layer listener.
   * @param listener the listener to add.
   */
  void addLayerListener(IPlotLayerListener listener);

  /**
   * Removes a layer listener.
   * @param listener the listener to remove.
   */
  void removeLayerListener(IPlotLayerListener listener);

  /**
   * Refreshes the shapes in the plot layer.
   */
  void refresh();

  /**
   * Returns the model space bounds of the layer (i.e. the maximum bounds off all the shapes in the layer).
   * @return the model space bounds of the layer.
   */
  ModelSpaceBounds getBounds();

  /**
   * Adds an action to the tree popup menu.
   * @param action the action to add.
   */
  void addPopupMenuAction(Action action);

  /**
   * Gets the flag for showing readout info. If true this layer is expected
   * to provide additional cursor location information. For example a
   * surface may provide it's depth.
   *
   * @return <i>true</i> to append cursor info, <i>false</i> otherwise.
   */
  boolean showReadoutInfo();

  /**
   * Specify whether this layer should provide additional information
   * to display in the readout panel. The data is appended to details such
   * the x,y location of the cursor.
   *
   * @param show <i>true</i> to show readout info, <i>false</i> otherwise.
   */
  void showReadoutInfo(boolean show);

  /**
   * Dispses of the plot layer resources.
   */
  void dispose();

  /**
   * Removes all the shapes from the layer.
   */
  void removeAllShapes();

  String getToolTipText();

  void updated();

  /**
   * @return
   */
  Action[] getActions();

  void block();

  void unblock();

  void modelSpaceUpdated(ModelSpaceEvent event);

  ReadoutInfo getReadoutInfo(double x, double y);

}
