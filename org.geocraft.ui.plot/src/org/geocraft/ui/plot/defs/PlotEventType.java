/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for plot event types.
 * Options include cursor motions, layer updates, axis updates, etc.
 * These are used internally to communicate between different
 * parts of the plot.
 */
public enum PlotEventType {
  CURSOR, MOUSE, MOUSE_MOVE, LAYOUT_UPDATED, DEFAULT_BOUNDS_UPDATED, VIEWABLE_BOUNDS_UPDATED, PLOT_UPDATED, MODEL_SPACE_UPDATED, AXIS_UPDATED, LAYER_UPDATED, LAYER_ADDED, LAYER_REMOVED, LAYER_SELECTED, LAYER_DESELECTED, SHAPE_UPDATED, SHAPE_ADDED, SHAPE_REMOVED, SHAPE_SELECTED, SHAPE_DESELECTED, SHAPE_START_MOTION, SHAPE_MOTION, SHAPE_END_MOTION, POINT_UPDATED, POINT_ADDED, POINT_REMOVED, POINT_SELECTED, POINT_DESELECTED, POINT_START_MOTION, POINT_MOTION, POINT_END_MOTION, MODEL_REDRAW
}
