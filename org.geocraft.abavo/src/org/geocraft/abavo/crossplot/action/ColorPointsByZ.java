/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.abavo.crossplot.layer.CrossplotSeriesLayer;


/**
 * This action causes the points in a crossplot series layer to be colored
 * based on their z values, rather than their series.
 */
public class ColorPointsByZ extends Action {

  /** The layer containing the crossplot series points. */
  private final CrossplotSeriesLayer _layer;

  public ColorPointsByZ(final CrossplotSeriesLayer layer) {
    super("Color Points by Z-Value");
    _layer = layer;
  }

  @Override
  public void run() {
    // Switch the layer to color points by their z value.
    _layer.colorPointsByZ(true);
  }
}
