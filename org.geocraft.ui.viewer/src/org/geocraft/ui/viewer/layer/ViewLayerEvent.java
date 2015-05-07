/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


/**
 * Defines the ViewLayerEvent class.
 */
public class ViewLayerEvent {

  /** The Id code for a "traces read" event. */
  public static enum EventType {
    LAYER_ADDED, LAYER_REMOVED, LAYER_RENAMED, LAYER_UPDATED, TRACES_READ
  }

  protected IViewLayer _viewLayer;

  protected EventType _eventType;

  /**
   * Constructs an instance of ViewLayerEvent.
   * @param viewLayer the event view layer.
   * @param type the event type.
   */
  public ViewLayerEvent(final IViewLayer viewLayer, final EventType type) {
    setViewLayer(viewLayer);
    setEventType(type);
  }

  /**
   * Gets the event view layer.
   * @return the event view layer.
   */
  public IViewLayer getViewLayer() {
    return _viewLayer;
  }

  /**
   * Gets the event type.
   * @return the event type.
   */
  public EventType getEventType() {
    return _eventType;
  }

  /**
   * Sets the event view layer.
   * @param viewLayer the event view layer.
   */
  public void setViewLayer(final IViewLayer viewLayer) {
    _viewLayer = viewLayer;
  }

  /**
   * Sets the event type.
   * @param id the event type.
   */
  public void setEventType(final EventType type) {
    _eventType = type;
  }
}
