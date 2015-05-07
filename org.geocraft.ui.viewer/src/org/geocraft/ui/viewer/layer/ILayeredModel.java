/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import java.util.Map;

import org.geocraft.core.repository.specification.ISpecification;


/**
 * The interface for a layered model (i.e. a model of view layers).
 * View layers are added to the model either at the root level, or under a specified parent layer.
 * When adding a view layer under a parent layer, the parent layer must already exist in the model.
 * If not, then an <i>IllegalArgumentException</i> will be thrown. View layers can also be removed
 * from the model. When doing so, all child layers under the specified layer will also be removed.
 * Each time a layer is added or removed, the model will notify all its listeners that an update
 * has occurred.
 */
public interface ILayeredModel {

  /**
   * Returns the name of the model.
   * @return the name of the model.
   */
  String getName();

  /**
   * Returns an array of the layers in the model.
   * @return an array of the layers in the model.
   */
  IViewLayer[] getLayers();

  /**
   * Returns an IViewLayer based on the result of the specification
   * @param spec the condition to test for
   * @return IViewLayer
   */
  IViewLayer getLayer(ISpecification spec);

  /**
   * Returns the root layers of in the model (i.e. layers with 'null' parent).
   * @return the root layers of in the model.
   */
  IViewLayer[] getRootLayers();

  /**
   * Returns the parent layer of the specified layer.
   * @return the parent layer of the specified layer.
   */
  IViewLayer getParent(IViewLayer child);

  /**
   * Returns an array of the layers under the specified parent.
   * @param parent the parent layer.
   * @return an array of the layers under the specified parent.
   */
  IViewLayer[] getChildren(IViewLayer parent);

  /**
   * Adds a layer to the model at the root level.
   * @param layer the layer to add.
   */
  void addLayer(IViewLayer layer);

  /**
   * Removes a layer from the model.
   * @param layer the layer to remove.
   */
  void removeLayer(IViewLayer layer);

  /**
   * Adds a layer to the model under the specified parent.
   * @param layer the layer to add.
   * @param parent the parent layer.
   */
  void addLayer(IViewLayer layer, IViewLayer parent);

  void addListener(ILayeredModelListener listener);

  void removeListener(ILayeredModelListener listener);

  /**
   * Disposes of the layered model resources.
   */
  void dispose();

  IViewLayer findLayerByName(String name);

  IViewLayer findLayerByObject(final Object object);

  Map<String, Boolean> getCheckedMap();

  void setCheckedMap(Map<String, Boolean> checkedMap);
}
