/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.sectionviewer.IPlotTrace;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PostStack2dRenderer extends SeismicDatasetRenderer {

  private PostStack2d _collection;

  public PostStack2dRenderer() {
    super();
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    // Store a reference to the seismic dataset.
    if (objects[0] instanceof PostStack2dLine) {
      _collection = ((PostStack2dLine) objects[0]).getPostStack();
    } else if (objects[0] instanceof PostStack2d) {
      _collection = (PostStack2d) objects[0];
    }
    _seismicDataset = _collection;
    _canvas = getViewer().getModelSpaceCanvas();
    PostStack2dLine line = _collection.getPostStack2dLines(true)[0];
    _model.setDataUnit(line.getDataUnit());
    _model.setGeometricGainTMax(line.getZEnd());
  }

  @Deprecated
  public Trace[] readTraces(final TraceSection section) {
    int numTraces = section.getNumTraces();
    float[] inlines = new float[numTraces];
    float[] cdps = new float[numTraces];
    int lineNumber = Math.round(section.getTraceAxisKeyValue(0, TraceAxisKey.INLINE));
    if (!_collection.containsPostStack2d(lineNumber)) {
      System.out.println("Skipping " + _collection.getDisplayName());
      return new Trace[0];
    }
    PostStack2dLine poststack = _collection.getPostStack2dLine(lineNumber);
    System.out.println("Rendering " + poststack.getDisplayName());
    float[][] points = section.getTraceKeyValues2d(poststack.getSeismicLine());
    int inlineIndex = section.getTraceAxisKeyIndex(TraceAxisKey.INLINE);
    int cdpIndex = section.getTraceAxisKeyIndex(TraceAxisKey.CDP);
    for (int i = 0; i < section.getNumTraces(); i++) {
      inlines[i] = points[i][inlineIndex];
      cdps[i] = points[i][cdpIndex];
    }
    TraceData traceData = poststack.getTraces(cdps, section.getStartZ(), section.getEndZ());
    return traceData.getTraces();
  }

  @Override
  public ReadoutInfo getReadoutInfo(final int traceNum, final float z) {

    List<String> keys = new ArrayList<String>();
    List<String> values = new ArrayList<String>();

    IPlotTrace[] plotTraces = getPlotTraces();
    if (traceNum >= 1 && traceNum <= plotTraces.length) {
      IPlotTrace plotTrace = plotTraces[traceNum - 1];
      Trace trace = plotTrace.getTrace();
      Header header = trace.getHeader();
      HeaderDefinition headerDef = header.getHeaderDefinition();
      boolean hasLineNo = headerDef.contains(TraceHeaderCatalog.INLINE_NO);
      boolean hasCDP = headerDef.contains(TraceHeaderCatalog.CDP_NO);
      boolean hasOffset = headerDef.contains(TraceHeaderCatalog.OFFSET);
      boolean hasShotpoint = headerDef.contains(TraceHeaderCatalog.SHOTPOINT_NO);
      int zIndex = (int) Math.floor((z - trace.getZStart()) / trace.getZDelta());
      if (zIndex >= 0 && zIndex < trace.getNumSamples()) {
        float value = trace.getDataReference()[zIndex];
        if (hasLineNo) {
          keys.add("Line");
          int lineNumber = Math.round(trace.getInline());
          values.add(_collection.getSurvey().getLineByNumber(lineNumber).getDisplayName());
        } else {
          values.add("" + header.getInteger(TraceHeaderCatalog.INLINE_NO));
        }
        if (hasCDP) {
          keys.add("CDP");
          values.add("" + header.getInteger(TraceHeaderCatalog.CDP_NO));
        }
        if (hasShotpoint) {
          keys.add("Shotpoint");
          values.add("" + header.getFloat(TraceHeaderCatalog.SHOTPOINT_NO));
        }
        if (hasOffset) {
          keys.add("Offset");
          values.add("" + header.getFloat(TraceHeaderCatalog.OFFSET));
        }
        keys.add("Amplitude");
        values.add("" + value);
      }
    }
    return new ReadoutInfo(_seismicDataset.toString(), keys.toArray(new String[0]), values.toArray(new String[0]));
  }

  @Override
  public void setCurrentPosition(final float[] position) {
    // TODO: make the received position the currently displayed trace
    // probably the map viewer will need to be able to broadcast just one trace and not both inline and xline
    // System.out.println("inline=" + position[0] + "  xline=" + position[1]);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.SEISMIC_FOLDER, autoUpdate);
  }

  @Override
  public DataSelection getDataSelection(final double traceNo, final double z) {
    PostStack2d collection = (PostStack2d) _seismicDataset;
    IPlotTrace[] plotTraces = getPlotTraces();
    if (plotTraces == null || plotTraces.length == 0) {
      return null;
    }
    Trace trace = plotTraces[0].getTrace();
    int lineNumber = Math.round(trace.getInline());
    if (!collection.containsPostStack2d(lineNumber)) {
      return null;
    }
    PostStack2dLine poststack = collection.getPostStack2dLine(lineNumber);
    int numTraces = _traces.size();
    if (traceNo >= 1 && traceNo <= numTraces && z >= poststack.getZStart() && z <= poststack.getZEnd()) {
      DataSelection selection = new DataSelection(getClass().getSimpleName());
      selection.setSelectedObjects(new Object[] { _seismicDataset });
      return selection;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.sectionviewer.renderer.seismic.SeismicDatasetRenderer#readTraces(org.eclipse.core.runtime.IProgressMonitor, org.geocraft.core.model.seismic.TraceSection)
   */
  @Override
  public synchronized Trace[] readTraces(final IProgressMonitor monitor, final TraceSection section) {
    if (section == null) {
      return new Trace[0];
    }
    int numTraces = section.getNumTraces();
    float[] inlines = new float[numTraces];
    float[] cdps = new float[numTraces];

    SubMonitor progress = SubMonitor.convert(monitor);

    int lineNumber = Math.round(section.getTraceAxisKeyValue(0, TraceAxisKey.INLINE));
    if (!_collection.containsPostStack2d(lineNumber)) {
      System.out.println("Skipping " + _collection.getDisplayName());
      return new Trace[0];
    }
    PostStack2dLine poststack = _collection.getPostStack2dLine(lineNumber);
    System.out.println("Rendering " + poststack.getDisplayName());
    float[][] points = section.getTraceKeyValues2d(poststack.getSeismicLine());
    int inlineIndex = section.getTraceAxisKeyIndex(TraceAxisKey.INLINE);
    int cdpIndex = section.getTraceAxisKeyIndex(TraceAxisKey.CDP);
    progress.beginTask("Reading traces...", section.getNumTraces());
    for (int i = 0; i < section.getNumTraces(); i++) {
      inlines[i] = points[i][inlineIndex];
      cdps[i] = points[i][cdpIndex];
      progress.worked(1);
    }
    progress.subTask("Reading trace data from datastore...");
    TraceData traceData = poststack.getTraces(cdps, section.getStartZ(), section.getEndZ());
    return traceData.getTraces();
  }
}
