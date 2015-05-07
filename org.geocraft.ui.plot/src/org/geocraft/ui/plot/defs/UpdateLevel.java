/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for update levels.
 * Options include resize, redraw, refresh.
 * These are used internally by the plot code to
 * specify the type of display updating that is
 * required as a result of an action.
 */
public enum UpdateLevel {
  /** Plot has been resized, everything needs redrawing. */
  RESIZE,
  /** Plot model space needs redrawing. */
  REDRAW,
  /** Only selected objects in model space needs redrawing. */
  REFRESH
}
