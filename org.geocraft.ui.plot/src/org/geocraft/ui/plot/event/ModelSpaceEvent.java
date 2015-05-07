/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.event;


import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * The event fired to listeners when a model is updated.
 */
public class ModelSpaceEvent extends AbstractPlotEvent {

  /** The update level. */
  private UpdateLevel _updateLevel;

  /** The plot model. */
  private final IModelSpace _modelSpace;

  /** The plot group. */
  private IPlotLayer _group;

  /** The plot shape. */
  private IPlotShape _shape;

  /**
   * Constructs a plot model event.
   * @param modelSpace the model space.
   * @param group the plot group.
   * @param type the event type.
   */
  public ModelSpaceEvent(final IModelSpace modelSpace, final IPlotLayer group, final PlotEventType type) {
    this(modelSpace, group, null, type);
  }

  /**
   * Constructs a plot model event.
   * @param modelSpace the model space.
   * @param group the plot group.
   * @param shape the plot shape.
   * @param type the event type.
   */
  public ModelSpaceEvent(final IModelSpace modelSpace, final IPlotLayer group, final IPlotShape shape, final PlotEventType type) {
    super(type);
    _modelSpace = modelSpace;
    _group = group;
    _shape = shape;
  }

  public ModelSpaceEvent(final IModelSpace modelSpace, final UpdateLevel updateLevel, final PlotEventType type) {
    super(type);
    _modelSpace = modelSpace;
    _updateLevel = updateLevel;
  }

  /**
   * Gets the update level (e.g. Redraw, Resize, etc).
   * @return the update level.
   */
  public UpdateLevel getUpdateLevel() {
    return _updateLevel;
  }

  /**
   * Gets the model space.
   * @return the model space.
   */
  public IModelSpace getModelSpace() {
    return _modelSpace;
  }

  /**
   * Gets the plot group.
   * @return the plot group.
   */
  public IPlotLayer getGroup() {
    return _group;
  }

  /**
   * Gets the plot shape.
   * @return the plot shape.
   */
  public IPlotShape getShape() {
    return _shape;
  }
}
