/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.layer;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PolygonRegionsModelLayer extends PlotLayer {

  private PolygonLayer[] _polygonLayers;

  public PolygonRegionsModelLayer() {
    super("Polygon Regions Model");
    _polygonLayers = new PolygonLayer[0];
    showReadoutInfo(true);
  }

  @Override
  public Image createImage() {
    return Activator.getDefault().createImage(ABavoImages.POLYGON_REGIONS_MODEL);
  }

  public void draw() {
    // No action.
  }

  public void setPolygonLayers(PolygonLayer[] polygonLayers) {
    _polygonLayers = polygonLayers;
  }

  @Override
  public ReadoutInfo getReadoutInfo(double x, double y) {
    List<String> keys = new ArrayList<String>();
    List<String> values = new ArrayList<String>();
    for (PolygonLayer layer : _polygonLayers) {
      ReadoutInfo subInfo = layer.getReadoutInfo(x, y);
      for (int i = 0; i < subInfo.getNumRecords(); i++) {
        keys.add(subInfo.getKey(i));
        values.add(subInfo.getValue(i));
      }
    }
    return new ReadoutInfo(getName(), keys, values);
  }
}
