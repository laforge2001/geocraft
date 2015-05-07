/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.viewer.layer.IViewLayer;


public interface IPlotViewLayer extends IViewLayer {

  IPlotLayer getPlotLayer();
}
