/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.layer;


import org.eclipse.swt.graphics.Image;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.ui.plot.layer.PlotLayer;


public class EllipseRegionsModelLayer extends PlotLayer {

  public EllipseRegionsModelLayer() {
    super("Ellipse Regions Model");
  }

  @Override
  public Image createImage() {
    return Activator.getDefault().createImage(ABavoImages.ELLIPSE_REGIONS_MODEL);
  }

  public void draw() {
    // No action.
  }
}
