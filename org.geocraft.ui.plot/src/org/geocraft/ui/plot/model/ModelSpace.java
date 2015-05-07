/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.model;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.plot.axis.Axis;
import org.geocraft.ui.plot.axis.AxisRange;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.event.PlotLayerEvent;
import org.geocraft.ui.plot.label.Label;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.listener.IModelSpaceListener;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * Simple implementation of the model space interface.
 */
public class ModelSpace implements IModelSpace, PropertyChangeListener {

  /** The model space name. */
  private String _name;

  /** The x-axis. */
  private final IAxis _xAxis;

  /** The y-axis. */
  private final IAxis _yAxis;

  private final List<IPlotLayer> _layers;

  private int _indexOfActiveLayer;

  private final boolean _aspectRatioFixed;

  private double _aspectRatio;

  private boolean _blockUpdate = false;

  private ModelSpaceBounds _maxBounds = null;

  /** The set of plot model listeners. */
  private final List<IModelSpaceListener> _listeners;

  public ModelSpace(final String name, final String xLabel, final String yLabel) {
    this(name, xLabel, yLabel, false);
  }

  public ModelSpace(final String name, final String xLabel, final String yLabel, final boolean isScaleEditable) {
    this(name, new Axis(new Label(xLabel), Unit.UNDEFINED, new AxisRange(0, 100), Orientation.HORIZONTAL,
        isScaleEditable), new Axis(new Label(yLabel, Orientation.VERTICAL, Alignment.CENTER, true), Unit.UNDEFINED,
        new AxisRange(0, 100), Orientation.VERTICAL, isScaleEditable));
  }

  public ModelSpace(final String name, final String xLabel, final String yLabel, final float aspectRatio) {
    this(name, new Axis(new Label(xLabel), Unit.UNDEFINED, new AxisRange(0, 100), Orientation.HORIZONTAL, false),
        new Axis(new Label(yLabel, Orientation.VERTICAL, Alignment.CENTER, true), Unit.UNDEFINED,
            new AxisRange(0, 100), Orientation.VERTICAL, false), aspectRatio);
  }

  /**
   * Constructs a model space with the given name, x-axis and y-axis.
   * 
   * @param name the model name.
   * @param xAxis the x-axis.
   * @param yAxis the y-axis.
   */
  public ModelSpace(final String name, final IAxis xAxis, final IAxis yAxis) {
    this(name, xAxis, yAxis, Double.NaN);
  }

  /**
   * Constructs a fixed-aspect-ratio model space with the given name, x-axis and y-axis.
   * 
   * @param name the model name.
   * @param xAxis the x-axis.
   * @param yAxis the y-axis.
   * @param aspectRatio the desired aspect ratio.
   */
  public ModelSpace(final String name, final IAxis xAxis, final IAxis yAxis, final double aspectRatio) {
    _name = name;

    if (!xAxis.getOrientation().equals(Orientation.HORIZONTAL)) {
      throw new IllegalArgumentException("X-Axis is not horizontal.");
    }

    if (!yAxis.getOrientation().equals(Orientation.VERTICAL)) {
      throw new IllegalArgumentException("Y-Axis is not vertical.");
    }

    _aspectRatioFixed = !Double.isNaN(aspectRatio);
    _aspectRatio = aspectRatio;
    _xAxis = xAxis;
    _yAxis = yAxis;
    _xAxis.addPropertyChangeListener(this);
    _yAxis.addPropertyChangeListener(this);
    _xAxis.getLabel().addPropertyChangeListener(this);
    _yAxis.getLabel().addPropertyChangeListener(this);
    _layers = Collections.synchronizedList(new ArrayList<IPlotLayer>());
    _listeners = Collections.synchronizedList(new ArrayList<IModelSpaceListener>());
    //addLayer(new PlotLayer("Default"), true);
  }

  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    _name = name;
  }

  public IAxis getAxisX() {
    return _xAxis;
  }

  public IAxis getAxisY() {
    return _yAxis;
  }

  public ModelSpaceBounds getViewableBounds() {
    return new ModelSpaceBounds(_xAxis.getViewableStart(), _xAxis.getViewableEnd(), _yAxis.getViewableStart(), _yAxis
        .getViewableEnd());
  }

  public ModelSpaceBounds getDefaultBounds() {
    return new ModelSpaceBounds(_xAxis.getDefaultStart(), _xAxis.getDefaultEnd(), _yAxis.getDefaultStart(), _yAxis
        .getDefaultEnd());
  }

  public void propertyChange(final PropertyChangeEvent event) {
    if (_blockUpdate) {
      return;
    }
    String propertyName = event.getPropertyName();
    if (propertyName.equals(Axis.DEFAULT_START) || propertyName.equals(Axis.DEFAULT_END)) {
      return;
    }
    IPlotLayer layer = null;
    ModelSpaceEvent modelEvent = new ModelSpaceEvent(this, layer, PlotEventType.AXIS_UPDATED);
    fireModelEvent(modelEvent);
  }

  public void addListener(final IModelSpaceListener listener) {
    addListener(listener, false);
  }

  public void addListener(final IModelSpaceListener listener, final boolean insertFirst) {
    if (_listeners.contains(listener)) {
      return;
    }
    if (insertFirst) {
      _listeners.add(0, listener);
    } else {
      _listeners.add(listener);
    }
  }

  public void removeListener(final IModelSpaceListener listener) {
    _listeners.remove(listener);
  }

  public IPlotLayer[] getLayers() {
    IPlotLayer[] layers = new IPlotLayer[_layers.size()];
    for (int i = 0; i < layers.length; i++) {
      layers[i] = _layers.get(i);
    }
    return layers;
  }

  public void moveToTop(IPlotLayer layer) {
    if (_layers.contains(layer)) {
      _layers.remove(layer);
      _layers.add(layer);
    }
  }

  public void addLayer(final IPlotLayer layer) {
    addLayer(layer, false);
  }

  public void addLayer(final IPlotLayer layer, final boolean makeActive) {
    if (_layers.contains(layer)) {
      return;
    }
    _layers.add(layer);
    for (IPlotShape shape : layer.getShapes()) {
      shape.setModelSpace(this);
    }
    layer.setModelSpace(this);
    layer.addLayerListener(this);
    if (makeActive) {
      _indexOfActiveLayer = _layers.size() - 1;
    }
    updated(PlotEventType.LAYER_ADDED, layer, null);
  }

  public void removeLayer(final IPlotLayer layer) {
    IPlotLayer activeLayer = _layers.get(_indexOfActiveLayer);
    if (layer.equals(activeLayer)) {
      _indexOfActiveLayer = 0;
    }
    _layers.remove(layer);
    layer.removeLayerListener(this);
    layer.setModelSpace(null);
    updated(PlotEventType.LAYER_REMOVED, layer, null);
  }

  public IPlotLayer getActiveLayer() {
    return _layers.get(_indexOfActiveLayer);
  }

  public boolean containsLayer(final IPlotLayer layer) {
    return _layers.contains(layer);
  }

  public void addShape(final IPlotShape shape) {
    addShape(shape, getActiveLayer());
  }

  public void addShape(final IPlotShape shape, final IPlotLayer layer) {
    if (!containsLayer(layer)) {
      throw new IllegalArgumentException("The layer \'" + layer.getName() + "\' does not exist in the model.");
    }
    layer.addShape(shape);
    shape.setModelSpace(this);
  }

  public void addShapes(final IPlotShape[] shapes) {
    addShapes(shapes, getActiveLayer());
  }

  public void addShapes(final IPlotShape[] shapes, final IPlotLayer layer) {
    if (!containsLayer(layer)) {
      throw new IllegalArgumentException("The layer \'" + layer.getName() + "\' does not exist in the model.");
    }
    layer.addShapes(shapes);
    for (IPlotShape shape : shapes) {
      shape.setModelSpace(this);
    }
  }

  public void layerUpdated(final PlotLayerEvent event) {
    updated(event.getEventType(), event.getLayer(), event.getShape());
  }

  public void redraw() {
    fireModelEvent(new ModelSpaceEvent(this, UpdateLevel.REDRAW, PlotEventType.MODEL_REDRAW));
  }

  public void updated() {
    updated(PlotEventType.MODEL_SPACE_UPDATED);
  }

  /**
   * Fires a model updated event to the listeners.
   * @param type the type of model updated event.
   */
  protected void updated(final PlotEventType type) {
    fireModelEvent(new ModelSpaceEvent(this, null, null, type));
  }

  /**
   * Fires a model updated event to the listeners.
   * @param type the type of model updated event.
   * @param layer the layer of the model updated event.
   * @param object the object of the model updated event.
   */
  protected void updated(final PlotEventType type, final IPlotLayer layer, final IPlotShape object) {
    fireModelEvent(new ModelSpaceEvent(this, layer, object, type));
  }

  /**
   * Fires a model event to the listeners.
   * @param event the model event to fire.
   */
  protected void fireModelEvent(final ModelSpaceEvent event) {
    IModelSpaceListener[] listeners = _listeners.toArray(new IModelSpaceListener[0]);
    for (IModelSpaceListener listener : listeners) {
      listener.modelSpaceUpdated(event);
    }
  }

  public boolean isFixedAspectRatio() {
    return _aspectRatioFixed;
  }

  public double aspectRatio() {
    return _aspectRatio;
  }

  public void setAspectRatio(final double aspectRatio) {
    _aspectRatio = aspectRatio;
    IPlotLayer layer = null;
    fireModelEvent(new ModelSpaceEvent(this, layer, PlotEventType.MODEL_SPACE_UPDATED));
  }

  public void dispose() {
    _xAxis.dispose();
    _yAxis.dispose();
    _listeners.clear();
  }

  public void setDefaultBounds(final double xStart, final double xEnd, final double yStart, final double yEnd) {
    _blockUpdate = true;
    _xAxis.setDefaultRange(xStart, xEnd);
    _yAxis.setDefaultRange(yStart, yEnd);
    _blockUpdate = false;
    updated(PlotEventType.DEFAULT_BOUNDS_UPDATED);
  }

  public void setViewableBounds(final double xStart, final double xEnd, final double yStart, final double yEnd) {
    _blockUpdate = true;
    _xAxis.setViewableRange(xStart, xEnd);
    _yAxis.setViewableRange(yStart, yEnd);
    _blockUpdate = false;
    updated(PlotEventType.VIEWABLE_BOUNDS_UPDATED);
  }

  public void setDefaultAndViewableBounds(final double xStart, final double xEnd, final double yStart,
      final double yEnd, Unit yUnit, String yLabel) {
    _blockUpdate = true;
    _xAxis.setDefaultRange(xStart, xEnd);
    _yAxis.setDefaultRange(yStart, yEnd);
    _xAxis.setViewableRange(xStart, xEnd);
    _yAxis.setViewableRange(yStart, yEnd);
    _yAxis.setUnit(yUnit);
    _yAxis.getLabel().setText(yLabel);
    _blockUpdate = false;
    updated(PlotEventType.VIEWABLE_BOUNDS_UPDATED);
  }

  public void setDefaultAndViewableBounds(double xStartDefault, double xEndDefault, double yStartDefault,
      double yEndDefault, double xStartViewable, double xEndViewable, double yStartViewable, double yEndViewable) {
    _blockUpdate = true;
    _xAxis.setDefaultRange(xStartDefault, xEndDefault);
    _yAxis.setDefaultRange(yStartDefault, yEndDefault);
    _xAxis.setViewableRange(xStartViewable, xEndViewable);
    _yAxis.setViewableRange(yStartViewable, yEndViewable);
    _blockUpdate = false;
    updated(PlotEventType.VIEWABLE_BOUNDS_UPDATED);
  }

  public ModelSpaceBounds getMaximumBounds() {
    return _maxBounds;
  }

  public boolean hasMaximumBounds() {
    return _maxBounds != null;
  }

  public void setMaximumBounds(ModelSpaceBounds maxBounds) {
    _maxBounds = maxBounds;
  }

}
