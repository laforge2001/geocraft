/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer;


import org.eclipse.jface.action.Action;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;


/**
 * The interface for the section viewer.
 */
public interface ISectionViewer extends IViewer {

  /**
   * Returns the layered model of the viewer.
   */
  ILayeredModel getLayerModel();

  /**
   * Looks up one of the default folder layers based on its name.
   * @param name the name to search for.
   * @return the folder layer matching the name.
   */
  IViewLayer findFolderLayer(String name);

  /**
   * Adds objects to the section viewer.
   * @param objects the array of objects to add.
   * @param block <i>true</i> to block the UI; otherwise <i>false</i>.
   */
  void addObjects(Object[] objects, boolean block);

  int getSeismicLineNumber2d();

  SeismicSurvey2d getSeismicSurvey2d();

  SeismicSurvey3d getReferenceSurvey3d();

  SeismicDataset getReferenceDataset();

  void enableToggleGroups(boolean enable);

  void incrementSection();

  void decrementSection();

  void redrawAllRenderers();

  Action getNavigationAction();

  TraceSection getTraceSection();

  void setTraceSection(TraceSection traceSection);

  void setZStartAndEnd(float zStart, float zEnd);

  IPlot getPlot();

  IModelSpaceCanvas getModelSpaceCanvas();

  FloatRange getOffsetRange();

  TraceSection getTraceSection(Domain zDomain);

  float getHorizontalDisplayScale();

  float getVerticalDisplayScale();

  void setScales(float tpiScale, float ipsScale);
}
