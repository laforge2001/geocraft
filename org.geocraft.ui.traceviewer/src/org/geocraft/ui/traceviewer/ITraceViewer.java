/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.traceviewer;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;


public interface ITraceViewer extends IViewer {

  IPlot getPlot();

  ILayeredModel getLayerModel();

  IViewLayer findFolderLayer(String rootName);

  void addObjects(boolean block, Object... objects);

  IModelSpaceCanvas getModelSpaceCanvas();

  void addTraces(final Trace[] traces);

  void addTraces(final TraceData traceData);

  void addPicks(final float[] picks, final String name, final RGB color);
}
