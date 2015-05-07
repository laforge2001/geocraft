/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.abavo.crossplot.layer.CrossplotSeriesLayer;
import org.geocraft.abavo.crossplot.layer.RegressionLayer;


public class MoveSeriesToFront extends Action {

  private final CrossplotSeriesLayer _seriesLayer;

  private final RegressionLayer[] _regressionLayers;

  public MoveSeriesToFront(final CrossplotSeriesLayer seriesLayer, final RegressionLayer[] regressionLayers) {
    super("Move to Front...");
    _seriesLayer = seriesLayer;
    _regressionLayers = new RegressionLayer[regressionLayers.length];
    System.arraycopy(regressionLayers, 0, _regressionLayers, 0, regressionLayers.length);
  }

  @Override
  public void run() {
    _seriesLayer.moveToFront();
    for (RegressionLayer regressionLayer : _regressionLayers) {
      regressionLayer.moveToFront();
    }
  }
}
