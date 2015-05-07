/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.ui.viewer.IViewer;


/**
 * Renders a PostStack3d entity in the section viewer.
 */
public class PreStack3dRenderer extends SeismicDatasetRenderer {

  public PreStack3dRenderer() {
    super();
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    // Store a reference to the seismic dataset.
    _seismicDataset = (SeismicDataset) objects[0];
    _canvas = getViewer().getModelSpaceCanvas();
    _model.setDataUnit(((SeismicDataset) _seismicDataset).getDataUnit());
    _model.setGeometricGainTMax(((SeismicDataset) _seismicDataset).getZEnd());
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
    PreStack3d prestack = (PreStack3d) _seismicDataset;
    int numTraces = _traces.size();
    if (traceNo >= 1 && traceNo <= numTraces && z >= prestack.getZStart() && z <= prestack.getZEnd()) {
      DataSelection selection = new DataSelection(getClass().getSimpleName());
      selection.setSelectedObjects(new Object[] { _seismicDataset });
      return selection;
    }
    return null;
  }

  @Override
  public synchronized Trace[] readTraces(final IProgressMonitor monitor, final TraceSection section) {
    PreStack3d prestack = (PreStack3d) _seismicDataset;
    int numTraces = section.getNumTraces();
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];
    float[] offsets = new float[numTraces];
    float[][] points = section.getTraceKeyValues3d();
    int inlineIndex = section.getTraceAxisKeyIndex(TraceAxisKey.INLINE);
    int xlineIndex = section.getTraceAxisKeyIndex(TraceAxisKey.XLINE);
    int offsetIndex = section.getTraceAxisKeyIndex(TraceAxisKey.OFFSET);
    SubMonitor progress = SubMonitor.convert(monitor);

    // Allocate the collection in which to store the traces read.
    List<Trace> traceList = new ArrayList<Trace>();

    float zStart = section.getStartZ();
    float zEnd = section.getEndZ();
    SectionType sectionType = section.getSectionType();
    FloatRange inlineRange = null;
    FloatRange xlineRange = null;
    FloatRange offsetRange = null;
    if (!sectionType.equals(SectionType.IRREGULAR)) {
      inlineRange = section.getTraceAxisKeyRanges(TraceAxisKey.INLINE);
      xlineRange = section.getTraceAxisKeyRanges(TraceAxisKey.XLINE);
      offsetRange = section.getTraceAxisKeyRanges(TraceAxisKey.OFFSET);
    }

    FloatRange inlineRangeVol = prestack.getInlineRange();
    FloatRange xlineRangeVol = prestack.getXlineRange();
    FloatRange offsetRangeVol = prestack.getOffsetRange();
    int numInlines = inlineRange.getNumSteps();
    int numXlines = xlineRange.getNumSteps();
    int numOffsets = offsetRange.getNumSteps();
    float inline;
    float xline;
    float offset;
    Map<String, Trace> traceMap = new HashMap<String, Trace>();
    int totalWork = 0;
    switch (sectionType) {
      case INLINE_SECTION:
        totalWork = numTraces + numXlines * numOffsets;
        progress.beginTask("Reading " + sectionType.toString(), totalWork);
        inline = inlineRange.getStart();
        for (int i = 0; i < numXlines; i++) {
          xline = xlineRange.getStart() + i * xlineRange.getDelta();
          progress.subTask("Reading xline: " + xline);
          TraceData traceData = prestack.getTracesByInlineXline(inline, xline, offsetRange.getStart(),
              offsetRange.getEnd(), zStart, zEnd);
          Trace[] traces = traceData.getTraces();
          int jinc = Math.round(offsetRange.getDelta() / offsetRangeVol.getDelta());
          for (int j = 0; j < numOffsets; j++) {
            offset = offsetRange.getStart() + j * offsetRange.getDelta();
            Trace trace = traces[j * jinc];
            String key = inline + "," + xline + "," + offset;
            traceMap.put(key, trace);
            progress.worked(1);
          }
        }
        break;
      case XLINE_SECTION:
        totalWork = numTraces + numInlines * numOffsets;
        progress.beginTask("Reading " + sectionType.toString(), totalWork);
        xline = xlineRange.getStart();
        for (int i = 0; i < numInlines; i++) {
          inline = inlineRange.getStart() + i * inlineRange.getDelta();
          TraceData traceData = prestack.getTracesByInlineXline(inline, xline, offsetRange.getStart(),
              offsetRange.getEnd(), zStart, zEnd);
          Trace[] traces = traceData.getTraces();
          int jinc = Math.round(offsetRange.getDelta() / offsetRangeVol.getDelta());
          for (int j = 0; j < offsetRange.getNumSteps(); j++) {
            offset = offsetRange.getStart() + j * offsetRange.getDelta();
            Trace trace = traces[j * jinc];
            String key = inline + "," + xline + "," + offset;
            traceMap.put(key, trace);
            progress.worked(1);
          }
        }
        break;
      case OFFSET_SECTION:
        totalWork = numTraces + numInlines * numXlines;
        progress.beginTask("Reading " + sectionType.toString(), totalWork);
        offset = offsetRange.getStart();
        for (int i = 0; i < numInlines; i++) {
          inline = xlineRange.getStart() + i * xlineRange.getDelta();
          TraceData traceData = prestack.getTracesByInlineOffset(inline, offset, xlineRange.getStart(),
              xlineRange.getEnd(), zStart, zEnd);
          Trace[] traces = traceData.getTraces();
          int jinc = Math.round(xlineRange.getDelta() / xlineRangeVol.getDelta());
          for (int j = 0; j < numXlines; j++) {
            xline = xlineRange.getStart() + j * xlineRange.getDelta();
            Trace trace = traces[j * jinc];
            String key = inline + "," + xline + "," + offset;
            traceMap.put(key, trace);
            progress.worked(1);
          }
        }
        break;
      case INLINE_OFFSET_GATHER:
        totalWork = numTraces + numXlines;
        progress.beginTask("Reading " + sectionType.toString(), totalWork);
        inline = inlineRange.getStart();
        offset = offsetRange.getStart();
        TraceData traceData3 = prestack.getTracesByInlineOffset(inline, offset, xlineRange.getStart(),
            xlineRange.getEnd(), zStart, zEnd);
        Trace[] traces3 = traceData3.getTraces();
        int jinc3 = Math.round(xlineRange.getDelta() / xlineRangeVol.getDelta());
        for (int j = 0; j < numXlines; j++) {
          xline = xlineRange.getStart() + j * xlineRange.getDelta();
          Trace trace = traces3[j * jinc3];
          String key = inline + "," + xline + "," + offset;
          traceMap.put(key, trace);
          progress.worked(1);
        }
        break;
      case XLINE_OFFSET_GATHER:
        totalWork = numTraces + numInlines;
        progress.beginTask("Reading " + sectionType.toString(), totalWork);
        xline = xlineRange.getStart();
        offset = offsetRange.getStart();
        TraceData traceData4 = prestack.getTracesByXlineOffset(xline, offset, inlineRange.getStart(),
            inlineRange.getEnd(), zStart, zEnd);
        Trace[] traces4 = traceData4.getTraces();
        int jinc4 = Math.round(inlineRange.getDelta() / inlineRangeVol.getDelta());
        for (int j = 0; j < numInlines; j++) {
          inline = inlineRange.getStart() + j * inlineRange.getDelta();
          Trace trace = traces4[j * jinc4];
          String key = inline + "," + xline + "," + offset;
          traceMap.put(key, trace);
          progress.worked(1);
        }
        break;
      case INLINE_XLINE_GATHER:
        totalWork = numTraces + numOffsets;
        progress.beginTask("Reading " + sectionType.toString(), totalWork);
        inline = inlineRange.getStart();
        xline = xlineRange.getStart();
        TraceData traceData5 = prestack.getTracesByInlineXline(inline, xline, offsetRange.getStart(),
            offsetRange.getEnd(), zStart, zEnd);
        Trace[] traces5 = traceData5.getTraces();
        int jinc5 = Math.round(offsetRange.getDelta() / offsetRangeVol.getDelta());
        for (int j = 0; j < numOffsets; j++) {
          offset = offsetRange.getStart() + j * offsetRange.getDelta();
          Trace trace = traces5[j * jinc5];
          String key = inline + "," + xline + "," + offset;
          traceMap.put(key, trace);
          progress.worked(1);
        }
        break;
      case INLINE_XLINE_OFFSET_TRACE:
        totalWork = numTraces + 1;
        progress.beginTask("Reading " + sectionType.toString(), totalWork);
        inline = inlineRange.getStart();
        xline = xlineRange.getStart();
        offset = offsetRange.getStart();
        TraceData traceData6 = prestack.getTracesByInlineXline(inline, xline, offset, offset, zStart, zEnd);
        progress.worked(1);
        Trace trace = traceData6.getTrace(0);
        String key = inline + "," + xline + "," + offset;
        traceMap.put(key, trace);
        break;
      case IRREGULAR:
        totalWork = numTraces + numTraces;
        progress.beginTask("Reading " + sectionType.toString(), totalWork);
        for (int i = 0; i < numTraces; i++) {
          inlines[i] = points[i][inlineIndex];
          xlines[i] = points[i][xlineIndex];
          offsets[i] = points[i][offsetIndex];
          progress.worked(1);
        }
        // Read the traces that need to be read from the dataset.
        TraceData traceData7 = prestack.getTraces(inlines, xlines, offsets, section.getStartZ(), section.getEndZ());
        progress.worked(numTraces);
        Trace[] traces7 = traceData7.getTraces();
        for (int j = 0; j < numTraces; j++) {
          String key7 = inlines[j] + "," + xlines[j] + "," + offsets[j];
          traceMap.put(key7, traces7[j]);
          progress.worked(1);
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid section type: " + sectionType);
    }
    progress.subTask("Mapping traces...");
    for (int i = 0; i < numTraces; i++) {
      inline = points[i][inlineIndex];
      xline = points[i][xlineIndex];
      offset = points[i][offsetIndex];
      String key = inline + "," + xline + "," + offset;
      traceList.add(traceMap.get(key));
      progress.worked(1);
    }
    progress.done();

    return traceList.toArray(new Trace[0]);
  }
}
