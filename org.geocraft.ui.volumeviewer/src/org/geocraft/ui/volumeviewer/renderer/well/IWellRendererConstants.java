/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.well;


import org.eclipse.swt.graphics.RGB;


public interface IWellRendererConstants {

  /** The key constant for the well bore radius property. */
  public static final String WELL_BORE_RADIUS = "Bore Radius";

  /** The key constant for the well bore color property. */
  public static final String WELL_BORE_COLOR = "Bore Color";

  /** The key constant for the well pick radius property. */
  public static final String WELL_PICK_RADIUS = "Pick Radius";

  /** The key constant for the well pick color property. */
  public static final String WELL_PICK_COLOR = "Pick Color";

  /** The default radius for rendering well bores. */
  public static final int DEFAULT_WELL_BORE_RADIUS = 2;

  /** The default color for rendering well bores (white). */
  public static final RGB DEFAULT_WELL_BORE_COLOR = new RGB(255, 255, 255);

  /** The default radius for rendering well picks. */
  public static final int DEFAULT_WELL_PICK_RADIUS = 100;

  /** The default color for rendering well picks (yellow). */
  public static final RGB DEFAULT_WELL_PICK_COLOR = new RGB(255, 255, 0);
}
