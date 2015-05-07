/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.ui.viewer.layer.ViewLayerEvent.EventType;


/**
 * The default implementation of a layered model.
 */
public class LayeredModel implements ILayeredModel, ViewLayerListener {

  /** The model name. */
  private final String _name;

  /** The list of registered model listeners. */
  private final List<ILayeredModelListener> _listeners;

  /** The array of root-level view layers. */
  private final List<IViewLayer> _rootKeys;

  /** The full map of all view layer entries in the model, */
  private final Map<IViewLayer, LayeredModelEntry> _map;

  /** The collection of checked-state flags, mapped by unique ID of the view layers. */
  private Map<String, Boolean> _checkedMap;

  public LayeredModel(final String name) {
    _name = name;
    _rootKeys = Collections.synchronizedList(new ArrayList<IViewLayer>());
    _map = Collections.synchronizedMap(new HashMap<IViewLayer, LayeredModelEntry>());
    _listeners = Collections.synchronizedList(new ArrayList<ILayeredModelListener>());
    _checkedMap = Collections.synchronizedMap(new HashMap<String, Boolean>());
  }

  public String getName() {
    return _name;
  }

  public IViewLayer[] getRootLayers() {
    return _rootKeys.toArray(new IViewLayer[0]);
  }

  public IViewLayer[] getLayers() {
    return _map.keySet().toArray(new IViewLayer[0]);
  }

  public synchronized IViewLayer getParent(final IViewLayer child) {
    LayeredModelEntry entry = _map.get(child);
    if (entry == null) {
      return null;
    }
    return entry.getParent();
  }

  public synchronized IViewLayer[] getChildren(final IViewLayer parent) {
    LayeredModelEntry entry = _map.get(parent);
    if (entry == null) {
      return new IViewLayer[0];
    }
    return entry.getChildren();
  }

  public synchronized void addLayer(final IViewLayer layer) {
    if (_map.containsKey(layer)) {
      throw new IllegalArgumentException("The layer already exists in the model.");
    }
    LayeredModelEntry entry = new LayeredModelEntry(layer);
    _map.put(layer, entry);
    _rootKeys.add(layer);
    layer.addListener(this);
    layeredModelUpdated(new ViewLayerEvent(layer, ViewLayerEvent.EventType.LAYER_ADDED));
  }

  public synchronized void addLayer(final IViewLayer layer, final IViewLayer parent) {
    if (parent == null) {
      addLayer(layer);
      return;
    }
    if (_map.containsKey(layer)) {
      throw new IllegalArgumentException("The layer already exists in the model.");
    }
    if (!_map.containsKey(parent)) {
      throw new IllegalArgumentException("The parent layer does not exist in the model.");
    }
    if (_map.containsKey(parent)) {
      LayeredModelEntry parentEntry = _map.get(parent);
      if (parentEntry != null) {
        parentEntry.addChild(layer);
        layeredModelUpdated(new ViewLayerEvent(parent, ViewLayerEvent.EventType.LAYER_UPDATED));
      }
      LayeredModelEntry childEntry = new LayeredModelEntry(layer);
      childEntry.setParent(parent);
      _map.put(layer, childEntry);
      layer.addListener(this);
      layeredModelUpdated(new ViewLayerEvent(layer, ViewLayerEvent.EventType.LAYER_ADDED));
    }
  }

  public synchronized void removeLayer(final IViewLayer layer) {
    removeLayer(layer, true);
  }

  private synchronized void removeLayer(final IViewLayer layer, boolean trigger) {
    // Find the layer entry in the model.
    LayeredModelEntry layerEntry = _map.get(layer);
    if (layerEntry != null) {
      // Find the parent layer.
      IViewLayer parentLayer = layerEntry.getParent();
      if (parentLayer != null) {
        // Find the parent layer entry in the model.
        LayeredModelEntry parentEntry = _map.get(parentLayer);
        if (parentEntry != null) {
          // Remove the layer from the parent layer entry.
          parentEntry.removeChild(layer);
          layeredModelUpdated(new ViewLayerEvent(parentLayer, ViewLayerEvent.EventType.LAYER_UPDATED));
        }
      }
      // Set the parent layer to 'null'.
      layerEntry.setParent(null);
      _map.remove(layer);
      _rootKeys.remove(layer);
      layer.removeListener(this);

      if (trigger) {
        layer.remove();
        layeredModelUpdated(new ViewLayerEvent(layer, ViewLayerEvent.EventType.LAYER_REMOVED));
      }

      // Loop thru each of the child layers and remove them from the model.
      for (IViewLayer childLayer : layerEntry.getChildren()) {
        if (childLayer != null) {
          removeLayer(childLayer);
        }
      }
      System.out.println("Disposing of layer=" + layer.getName());
      layer.dispose();
    }
  }

  public synchronized void addListener(final ILayeredModelListener listener) {
    if (!_listeners.contains(listener)) {
      _listeners.add(listener);
    }
  }

  public synchronized void removeListener(final ILayeredModelListener listener) {
    _listeners.remove(listener);
  }

  public synchronized void dispose() {
    for (LayeredModelEntry entry : _map.values()) {
      entry.dispose();
    }
    _map.clear();
    _listeners.clear();
  }

  protected synchronized void layeredModelUpdated(final ViewLayerEvent event) {
    for (ILayeredModelListener listener : _listeners.toArray(new ILayeredModelListener[0])) {
      listener.layeredModelUpdated(event);
    }
  }

  public synchronized void viewLayerUpdated(final ViewLayerEvent event) {
    EventType eventType = event.getEventType();
    if (eventType.equals(ViewLayerEvent.EventType.LAYER_REMOVED)) {
      removeLayer(event.getViewLayer(), false);
    }
    layeredModelUpdated(event);
  }

  public synchronized IViewLayer findLayerByName(final String name) {
    for (IViewLayer layer : getLayers()) {
      if (layer.getName().equals(name)) {
        return layer;
      }
    }
    return null;
  }

  public synchronized IViewLayer findLayerByObject(final Object object) {
    if (object == null) {
      return null;
    }
    for (IViewLayer layer : getLayers()) {
      Object userObject = layer.getUserObject();
      if (userObject.equals(object)) {
        return layer;
      }
    }
    return null;
  }

  public synchronized IViewLayer getLayer(final ISpecification specification) {
    Set keys = _map.keySet();
    for (Iterator it = keys.iterator(); it.hasNext();) {
      IViewLayer model = (IViewLayer) it.next();
      if (specification.isSatisfiedBy(model)) {
        return model;
      }
    }
    return null;
  }

  public final Map<String, Boolean> getCheckedMap() {
    return _checkedMap;
  }

  public final void setCheckedMap(Map<String, Boolean> checkedMap) {
    _checkedMap.clear();
    for (String uniqueID : checkedMap.keySet()) {
      Boolean checked = checkedMap.get(uniqueID);
      _checkedMap.put(uniqueID, checked);
    }
  }
}
