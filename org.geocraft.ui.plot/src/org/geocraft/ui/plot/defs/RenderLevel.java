/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for render levels.
 * These are used to specify the drawing order of
 * items in the plot. The background is rendered first,
 * followed by an image, then grid lines, then another
 * image, then the standard shapes, and lastly the
 * selected shapes.
 */
public enum RenderLevel {
  BACKGROUND, IMAGE_UNDER_GRID, GRID, IMAGE_OVER_GRID, STANDARD, SELECTED
}
