/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer;


import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.light.LightSourceModel;


/**
 * The interface for the map viewer.
 */
public interface IMapViewer extends IViewer {

  IPlot getPlot();

  LightSourceModel getLightSourceModel();

  ILayeredModel getLayerModel();

  IViewLayer findFolderLayer(String rootName);

  void addObjects(boolean block, Object... objects);
}
