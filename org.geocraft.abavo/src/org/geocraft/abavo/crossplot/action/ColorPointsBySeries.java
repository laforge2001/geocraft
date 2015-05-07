/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.abavo.crossplot.layer.CrossplotSeriesLayer;


/**
 * This action causes the point in a crossplot series layer to be colored
 * based on their series, rather than their z values.
 */
public class ColorPointsBySeries extends Action {

  /** The layer containing the crossplot series points. */
  private final CrossplotSeriesLayer _layer;

  public ColorPointsBySeries(final CrossplotSeriesLayer layer) {
    super("Color Points by Series");
    _layer = layer;
  }

  @Override
  public void run() {
    // Switch the layer to color points by their series.
    _layer.colorPointsByZ(false);
  }
}
