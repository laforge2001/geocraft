/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.ui.viewer.IViewer;


public class PostStack3dRenderer extends SeismicDatasetRenderer {

  public PostStack3dRenderer() {
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
    PostStack3d poststack = (PostStack3d) _seismicDataset;
    int numTraces = _traces.size();
    if (traceNo >= 1 && traceNo <= numTraces && z >= poststack.getZStart() && z <= poststack.getZEnd()) {
      DataSelection selection = new DataSelection(getClass().getSimpleName());
      selection.setSelectedObjects(new Object[] { _seismicDataset });
      return selection;
    }
    return null;
  }

  @Override
  public synchronized Trace[] readTraces(final IProgressMonitor monitor, final TraceSection section) {
    PostStack3d poststack = (PostStack3d) _seismicDataset;
    int numTraces = section.getNumTraces();

    SubMonitor progress = SubMonitor.convert(monitor);
    progress.beginTask("Reading Traces", 100);

    float[][] points = section.getTraceKeyValues3d();
    int inlineIndex = section.getTraceAxisKeyIndex(TraceAxisKey.INLINE);
    int xlineIndex = section.getTraceAxisKeyIndex(TraceAxisKey.XLINE);

    // Allocate the collection in which to store the traces read.
    List<Trace> traceList = new ArrayList<Trace>();

    float zStart = section.getStartZ();
    float zEnd = section.getEndZ();
    SectionType sectionType = section.getSectionType();
    FloatRange inlineRange = null;
    FloatRange xlineRange = null;
    int numOffsets = 1;
    if (!sectionType.equals(SectionType.IRREGULAR)) {
      inlineRange = section.getTraceAxisKeyRanges(TraceAxisKey.INLINE);
      xlineRange = section.getTraceAxisKeyRanges(TraceAxisKey.XLINE);
      if (section.containsTraceAxisKey(TraceAxisKey.OFFSET)) {
        FloatRange offsetRange = section.getTraceAxisKeyRanges(TraceAxisKey.OFFSET);
        numOffsets = offsetRange.getNumSteps();
      }
    }
    Point3d[] controlPoints = section.getPointsXY();
    switch (sectionType) {
      case INLINE_SECTION:
        if (numOffsets == 1) {
          monitor.subTask("Reading inline=" + inlineRange.getStart() + ", xlines=" + xlineRange.getStart() + "-"
              + xlineRange.getEnd());
          Trace[] traces = PostStack3dInterpolator.getInline(poststack, inlineRange.getStart(), xlineRange.getStart(),
              xlineRange.getEnd(), xlineRange.getDelta(), zStart, zEnd, controlPoints);
          progress.setWorkRemaining(traces.length);
          for (Trace trace : traces) {
            traceList.add(trace);
            progress.worked(1);
          }
        } else {
          for (int i = 0; i < numTraces; i++) {
            float inline = points[i][inlineIndex];
            float xline = points[i][xlineIndex];
            // Read the traces that need to be read from the dataset.
            Trace[] traces0 = PostStack3dInterpolator.getTraces(poststack, new float[] { inline },
                new float[] { xline }, zStart, zEnd, controlPoints);
            for (Trace trace : traces0) {
              traceList.add(trace);
            }
            int test = (int) (i / (numTraces * 1.000) * 100);
            if (test % 10 == 0) {
              monitor.subTask("Reading inline=" + inline + ", xline=" + xline);
              progress.worked(test);
            }
          }
        }
        break;
      case XLINE_SECTION:
        if (numOffsets == 1) {
          monitor.subTask("Reading xline=" + xlineRange.getStart() + ", inlines=" + inlineRange.getStart() + "-"
              + inlineRange.getEnd());
          Trace[] traces = PostStack3dInterpolator.getXline(poststack, xlineRange.getStart(), inlineRange.getStart(),
              inlineRange.getEnd(), inlineRange.getDelta(), zStart, zEnd, controlPoints);
          progress.setWorkRemaining(traces.length);
          for (Trace trace : traces) {
            traceList.add(trace);
            progress.worked(1);
          }
        } else {
          for (int i = 0; i < numTraces; i++) {
            float inline = points[i][inlineIndex];
            float xline = points[i][xlineIndex];
            // Read the traces that need to be read from the dataset.
            Trace[] traces1 = PostStack3dInterpolator.getTraces(poststack, new float[] { inline },
                new float[] { xline }, zStart, zEnd, controlPoints);
            for (Trace trace : traces1) {
              traceList.add(trace);
            }
            int test = (int) (i / (numTraces * 1.000) * 100);
            if (test % 10 == 0) {
              monitor.subTask("Reading inline=" + inline + ", xline=" + xline);
              progress.worked(test);
            }
          }
        }
        break;
      case OFFSET_SECTION:
        for (int i = 0; i < numTraces; i++) {
          float inline = points[i][inlineIndex];
          float xline = points[i][xlineIndex];
          // Read the traces that need to be read from the dataset.
          Trace[] traces2 = PostStack3dInterpolator.getTraces(poststack, new float[] { inline }, new float[] { xline },
              zStart, zEnd, controlPoints);
          for (Trace trace : traces2) {
            traceList.add(trace);
          }
          int test = (int) (i / (numTraces * 1.000) * 100);
          if (test % 10 == 0) {
            monitor.subTask("Reading inline=" + inline + ", xline=" + xline);
            progress.worked(test);
          }
        }
        break;
      case INLINE_OFFSET_GATHER:
        monitor.subTask("Reading inline=" + inlineRange.getStart() + ", xlines=" + xlineRange.getStart() + "-"
            + xlineRange.getEnd());
        Trace[] traces3 = PostStack3dInterpolator.getInline(poststack, inlineRange.getStart(), xlineRange.getStart(),
            xlineRange.getEnd(), xlineRange.getDelta(), zStart, zEnd, controlPoints);
        progress.setWorkRemaining(traces3.length);
        for (Trace trace : traces3) {
          traceList.add(trace);
          progress.worked(1);
        }
        break;
      case XLINE_OFFSET_GATHER:
        monitor.subTask("Reading xline=" + xlineRange.getStart() + ", inlines=" + inlineRange.getStart() + "-"
            + inlineRange.getEnd());
        Trace[] traces4 = PostStack3dInterpolator.getXline(poststack, xlineRange.getStart(), inlineRange.getStart(),
            inlineRange.getEnd(), inlineRange.getDelta(), zStart, zEnd, controlPoints);
        progress.setWorkRemaining(traces4.length);
        for (Trace trace : traces4) {
          traceList.add(trace);
          progress.worked(1);
        }
        break;
      case INLINE_XLINE_GATHER:
      case INLINE_XLINE_OFFSET_TRACE:
        monitor.subTask("Reading inline=" + inlineRange.getStart() + ", xline=" + xlineRange.getStart());
        Trace[] traces5 = PostStack3dInterpolator.getTraces(poststack, new float[] { inlineRange.getStart() },
            new float[] { xlineRange.getStart() }, zStart, zEnd, controlPoints);
        progress.setWorkRemaining(traces5.length);
        for (Trace trace : traces5) {
          traceList.add(trace);
          progress.worked(1);
        }
        break;
      case IRREGULAR:
        for (int i = 0; i < numTraces; i++) {
          float inline = points[i][inlineIndex];
          float xline = points[i][xlineIndex];
          Point3d[] controlPoint = new Point3d[1];
          controlPoint[0] = controlPoints[i];
          // Read the traces that need to be read from the dataset.
          Trace[] traces7 = PostStack3dInterpolator.getTraces(poststack, new float[] { inline }, new float[] { xline },
              zStart, zEnd, controlPoint);
          for (Trace trace : traces7) {
            traceList.add(trace);
          }
          int test = (int) (i / (numTraces * 1.000) * 100);
          if (test % 10 == 0) {
            monitor.subTask("Reading inline=" + inline + ", xline=" + xline);
            progress.worked(test);
          }
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid section type: " + sectionType);
    }

    return traceList.toArray(new Trace[0]);
  }
}
