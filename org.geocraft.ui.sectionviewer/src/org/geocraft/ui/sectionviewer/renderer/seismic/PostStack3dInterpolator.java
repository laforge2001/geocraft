/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.seismic.PostStack3d;


public class PostStack3dInterpolator {

  /**
   * Returns an array of interpolated traces for the given arrays of inlines and xlines.
   * <p>
   * Traces that do not exist in the volume will be interpolated.
   * 
   * @param poststack the 3D volume from which to read.
   * @param inlines the array of inlines.
   * @param xlines the array of xlines (must be same length as array of inlines).
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param xyPoints the array of x,y points that correspond to the requested trace range.
   * @return the array of interpolated traces.
   */
  public static Trace[] getTraces(final PostStack3d poststack, final float[] inlines, final float[] xlines,
      final float zStart, final float zEnd, final Point3d[] xyPoints) {
    if (inlines == null || xlines == null) {
      throw new IllegalArgumentException("The inline and xline arrays cannot be null.");
    }
    if (inlines.length != xlines.length) {
      throw new IllegalArgumentException("The inline and xline arrays must be of equal length.");
    }
    if (xyPoints == null) {
      throw new IllegalArgumentException("The x,y point array cannot be null.");
    }
    if (xyPoints.length != inlines.length) {
      throw new IllegalArgumentException(
          "The length of the x,y point array must match the length of the inline,xline arrays.");
    }
    int zStartIndex = (int) Math.ceil((zStart - poststack.getZStart()) / poststack.getZDelta());
    float zStartAdjusted = poststack.getZStart() + zStartIndex * poststack.getZDelta();
    zStartAdjusted = Math.max(zStartAdjusted, poststack.getZStart());
    int zEndIndex = (int) Math.floor((zEnd - poststack.getZStart()) / poststack.getZDelta());
    float zEndAdjusted = poststack.getZStart() + zEndIndex * poststack.getZDelta();
    zEndAdjusted = Math.min(zEndAdjusted, poststack.getZEnd());

    List<Trace> traceList = Collections.synchronizedList(new ArrayList<Trace>());
    int numTraces = inlines.length;
    int numSamples = 1 + Math.round((zEndAdjusted - zStartAdjusted) / poststack.getZDelta());
    for (int i = 0; i < numTraces; i++) {
      float inline = inlines[i];
      float inlineIndex = (inline - poststack.getInlineStart()) / poststack.getInlineDelta();
      int inlineIndex0 = (int) Math.floor(inlineIndex);
      int inlineIndex1 = (int) Math.ceil(inlineIndex);
      float inlineWeight1 = inlineIndex - inlineIndex0;
      float inlineWeight0 = 1 - inlineWeight1;
      float inline0 = poststack.getInlineStart() + inlineIndex0 * poststack.getInlineDelta();
      float inline1 = poststack.getInlineStart() + inlineIndex1 * poststack.getInlineDelta();

      float xline = xlines[i];
      float xlineIndex = (xline - poststack.getXlineStart()) / poststack.getXlineDelta();
      int xlineIndex0 = (int) Math.floor(xlineIndex);
      int xlineIndex1 = (int) Math.ceil(xlineIndex);
      float xlineWeight1 = xlineIndex - xlineIndex0;
      float xlineWeight0 = 1 - xlineWeight1;
      float xline0 = poststack.getXlineStart() + xlineIndex0 * poststack.getXlineDelta();
      float xline1 = poststack.getXlineStart() + xlineIndex1 * poststack.getXlineDelta();

      float[] inlinesBounds = { inline0, inline0, inline1, inline1 };
      float[] xlinesBounds = { xline0, xline1, xline0, xline1 };
      Trace trace00 = null;
      Trace trace01 = null;
      Trace trace10 = null;
      Trace trace11 = null;
      if (inlineIndex0 >= 0 && inlineIndex0 < poststack.getNumInlines() && inlineIndex1 >= 0
          && inlineIndex1 < poststack.getNumInlines() && xlineIndex0 >= 0 && xlineIndex0 < poststack.getNumXlines()
          && xlineIndex1 >= 0 && xlineIndex1 < poststack.getNumXlines()) {
        TraceData traceData = poststack.getTraces(inlinesBounds, xlinesBounds, zStartAdjusted, zEndAdjusted);
        Trace[] temp = traceData.getTraces();
        trace00 = temp[0];
        trace01 = temp[1];
        trace10 = temp[2];
        trace11 = temp[3];
      }
      double x = xyPoints[i].getX();
      double y = xyPoints[i].getY();
      if (trace00 == null) {
        trace00 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace01 == null) {
        trace01 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace10 == null) {
        trace10 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace11 == null) {
        trace11 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }

      // Combine the bounding traces along the floor inline.
      Trace trace0 = combineTraces(trace00, trace01, xlineWeight0, xlineWeight1);

      // Combine the bounding traces along the ceiling inline.
      Trace trace1 = combineTraces(trace10, trace11, xlineWeight0, xlineWeight1);

      // Combine the results of the previous combinations.
      Trace trace = combineTraces(trace0, trace1, inlineWeight0, inlineWeight1);

      // Update the trace header for the final combine trace.
      HeaderDefinition traceHeaderDef = poststack.getTraceHeaderDefinition();
      Header traceHeader = new Header(traceHeaderDef);
      traceHeader.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
      traceHeader.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
      traceHeader.putDouble(TraceHeaderCatalog.X, x);
      traceHeader.putDouble(TraceHeaderCatalog.Y, y);
      trace.setHeader(traceHeader);
      traceList.add(trace);
    }

    return traceList.toArray(new Trace[0]);
  }

  /**
   * Returns an array of interpolated traces for the given inline and xline range.
   * <p>
   * Traces that do not exist in the volume will be interpolated.
   * 
   * @param poststack the 3D volume from which to read.
   * @param inline the inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param xlineDelta the xline delta.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param xyPoints the array of x,y points that correspond to the requested trace range.
   * @return the array of interpolated traces.
   */
  public static Trace[] getInline(final PostStack3d poststack, final float inline, final float xlineStart,
      final float xlineEnd, final float xlineDelta, final float zStart, final float zEnd, final Point3d[] xyPoints) {
    int numXlines = 1 + Math.round((xlineEnd - xlineStart) / xlineDelta);
    if (xyPoints == null) {
      throw new IllegalArgumentException("The x,y point array cannot be null.");
    }
    if (xyPoints.length != numXlines) {
      throw new IllegalArgumentException("The length of the x,y point array must match the size of the xline range.");
    }

    int zStartIndex = (int) Math.ceil((zStart - poststack.getZStart()) / poststack.getZDelta());
    float zStartAdjusted = poststack.getZStart() + zStartIndex * poststack.getZDelta();
    zStartAdjusted = Math.max(zStartAdjusted, poststack.getZStart());
    int zEndIndex = (int) Math.floor((zEnd - poststack.getZStart()) / poststack.getZDelta());
    float zEndAdjusted = poststack.getZStart() + zEndIndex * poststack.getZDelta();
    zEndAdjusted = Math.min(zEndAdjusted, poststack.getZEnd());
    float inlineIndex = (inline - poststack.getInlineStart()) / poststack.getInlineDelta();
    int inlineIndex0 = (int) Math.floor(inlineIndex);
    int inlineIndex1 = (int) Math.ceil(inlineIndex);
    float inlineWeight1 = inlineIndex - inlineIndex0;
    float inlineWeight0 = 1 - inlineWeight1;
    float inline0 = poststack.getInlineStart() + inlineIndex0 * poststack.getInlineDelta();
    float inline1 = poststack.getInlineStart() + inlineIndex1 * poststack.getInlineDelta();

    Map<Float, Trace> traceMap0 = new HashMap<Float, Trace>();
    Map<Float, Trace> traceMap1 = new HashMap<Float, Trace>();
    if (inlineIndex0 >= 0 && inlineIndex0 < poststack.getNumInlines() && inlineIndex1 >= 0
        && inlineIndex1 < poststack.getNumInlines()) {
      TraceData traceData0 = poststack.getInline(inline0, poststack.getXlineStart(), poststack.getXlineEnd(),
          zStartAdjusted, zEndAdjusted);
      TraceData traceData1 = poststack.getInline(inline1, poststack.getXlineStart(), poststack.getXlineEnd(),
          zStartAdjusted, zEndAdjusted);
      for (Trace trace : traceData0.getTraces()) {
        traceMap0.put(trace.getXline(), trace);
      }
      for (Trace trace : traceData1.getTraces()) {
        traceMap1.put(trace.getXline(), trace);
      }
    }
    List<Trace> traceList = Collections.synchronizedList(new ArrayList<Trace>());
    int numSamples = 1 + Math.round((zEndAdjusted - zStartAdjusted) / poststack.getZDelta());
    for (int i = 0; i < numXlines; i++) {
      float xline = xlineStart + i * xlineDelta;
      float xlineIndex = (xline - poststack.getXlineStart()) / poststack.getXlineDelta();
      int xlineIndex0 = (int) Math.floor(xlineIndex);
      int xlineIndex1 = (int) Math.ceil(xlineIndex);
      float xlineWeight1 = xlineIndex - xlineIndex0;
      float xlineWeight0 = 1 - xlineWeight1;
      float xline0 = poststack.getXlineStart() + xlineIndex0 * poststack.getXlineDelta();
      float xline1 = poststack.getXlineStart() + xlineIndex1 * poststack.getXlineDelta();
      Trace trace00 = traceMap0.get(xline0);
      Trace trace01 = traceMap0.get(xline1);
      Trace trace10 = traceMap1.get(xline0);
      Trace trace11 = traceMap1.get(xline1);
      double x = xyPoints[i].getX();
      double y = xyPoints[i].getY();
      if (trace00 == null) {
        trace00 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace01 == null) {
        trace01 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace10 == null) {
        trace10 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace11 == null) {
        trace11 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }

      // Combine the bounding traces along the floor inline.
      Trace trace0 = combineTraces(trace00, trace01, xlineWeight0, xlineWeight1);

      // Combine the bounding traces along the ceiling inline.
      Trace trace1 = combineTraces(trace10, trace11, xlineWeight0, xlineWeight1);

      // Combine the results of the previous combinations.
      Trace trace = combineTraces(trace0, trace1, inlineWeight0, inlineWeight1);

      // Update the trace header for the final combine trace.
      HeaderDefinition traceHeaderDef = poststack.getTraceHeaderDefinition();
      Header traceHeader = new Header(traceHeaderDef);
      traceHeader.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
      traceHeader.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
      traceHeader.putDouble(TraceHeaderCatalog.X, x);
      traceHeader.putDouble(TraceHeaderCatalog.Y, y);
      trace.setHeader(traceHeader);
      traceList.add(trace);
    }

    return traceList.toArray(new Trace[0]);
  }

  /**
   * Returns an array of interpolated traces for the given xline and inline range.
   * <p>
   * Traces that do not exist in the volume will be interpolated.
   * 
   * @param poststack the 3D volume from which to read.
   * @param xline the xline.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param inlineDelta the inline delta.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param xyPoints the array of x,y points that correspond to the requested trace range.
   * @return the array of interpolated traces.
   */
  public static Trace[] getXline(final PostStack3d poststack, final float xline, final float inlineStart,
      final float inlineEnd, final float inlineDelta, final float zStart, final float zEnd, final Point3d[] xyPoints) {
    int numInlines = 1 + Math.round((inlineEnd - inlineStart) / inlineDelta);
    if (xyPoints == null) {
      throw new IllegalArgumentException("The x,y point array cannot be null.");
    }
    if (xyPoints.length != numInlines) {
      throw new IllegalArgumentException("The length of the x,y point array must match the size of the inline range.");
    }

    int zStartIndex = (int) Math.ceil((zStart - poststack.getZStart()) / poststack.getZDelta());
    float zStartAdjusted = poststack.getZStart() + zStartIndex * poststack.getZDelta();
    zStartAdjusted = Math.max(zStartAdjusted, poststack.getZStart());
    int zEndIndex = (int) Math.floor((zEnd - poststack.getZStart()) / poststack.getZDelta());
    float zEndAdjusted = poststack.getZStart() + zEndIndex * poststack.getZDelta();
    zEndAdjusted = Math.min(zEndAdjusted, poststack.getZEnd());
    float xlineIndex = (xline - poststack.getXlineStart()) / poststack.getXlineDelta();
    int xlineIndex0 = (int) Math.floor(xlineIndex);
    int xlineIndex1 = (int) Math.ceil(xlineIndex);
    float xlineWeight1 = xlineIndex - xlineIndex0;
    float xlineWeight0 = 1 - xlineWeight1;
    float xline0 = poststack.getXlineStart() + xlineIndex0 * poststack.getXlineDelta();
    float xline1 = poststack.getXlineStart() + xlineIndex1 * poststack.getXlineDelta();

    Map<Float, Trace> traceMap0 = new HashMap<Float, Trace>();
    Map<Float, Trace> traceMap1 = new HashMap<Float, Trace>();
    if (xlineIndex0 >= 0 && xlineIndex0 < poststack.getNumXlines() && xlineIndex1 >= 0
        && xlineIndex1 < poststack.getNumXlines()) {
      TraceData traceData0 = poststack.getXline(xline0, poststack.getInlineStart(), poststack.getInlineEnd(),
          zStartAdjusted, zEndAdjusted);
      TraceData traceData1 = poststack.getXline(xline1, poststack.getInlineStart(), poststack.getInlineEnd(),
          zStartAdjusted, zEndAdjusted);
      for (Trace trace : traceData0.getTraces()) {
        traceMap0.put(trace.getInline(), trace);
      }
      for (Trace trace : traceData1.getTraces()) {
        traceMap1.put(trace.getInline(), trace);
      }
    }
    List<Trace> traceList = Collections.synchronizedList(new ArrayList<Trace>());
    int numSamples = 1 + Math.round((zEndAdjusted - zStartAdjusted) / poststack.getZDelta());
    for (int i = 0; i < numInlines; i++) {
      float inline = inlineStart + i * inlineDelta;
      float inlineIndex = (inline - poststack.getInlineStart()) / poststack.getInlineDelta();
      int inlineIndex0 = (int) Math.floor(inlineIndex);
      int inlineIndex1 = (int) Math.ceil(inlineIndex);
      float inlineWeight1 = inlineIndex - inlineIndex0;
      float inlineWeight0 = 1 - inlineWeight1;
      float inline0 = poststack.getInlineStart() + inlineIndex0 * poststack.getInlineDelta();
      float inline1 = poststack.getInlineStart() + inlineIndex1 * poststack.getInlineDelta();
      Trace trace00 = traceMap0.get(inline0);
      Trace trace01 = traceMap0.get(inline1);
      Trace trace10 = traceMap1.get(inline0);
      Trace trace11 = traceMap1.get(inline1);
      double x = xyPoints[i].getX();
      double y = xyPoints[i].getY();
      if (trace00 == null) {
        trace00 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace01 == null) {
        trace01 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace10 == null) {
        trace10 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }
      if (trace11 == null) {
        trace11 = new Trace(zStartAdjusted, poststack.getZDelta(), poststack.getZUnit(), x, y, new float[numSamples],
            Status.Live);
      }

      // Combine the bounding traces along the floor xline.
      Trace trace0 = combineTraces(trace00, trace01, inlineWeight0, inlineWeight1);

      // Combine the bounding traces along the floor xline.
      Trace trace1 = combineTraces(trace10, trace11, inlineWeight0, inlineWeight1);

      // Combine the results of the previous combinations.
      Trace trace = combineTraces(trace0, trace1, xlineWeight0, xlineWeight1);

      // Update the trace header for the final combine trace.
      HeaderDefinition traceHeaderDef = poststack.getTraceHeaderDefinition();
      Header traceHeader = new Header(traceHeaderDef);
      traceHeader.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
      traceHeader.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
      traceHeader.putDouble(TraceHeaderCatalog.X, x);
      traceHeader.putDouble(TraceHeaderCatalog.Y, y);
      trace.setHeader(traceHeader);

      traceList.add(trace);
    }

    return traceList.toArray(new Trace[0]);
  }

  /**
   * Combines 2 traces, based on given weights.
   * <p>
   * The trace header will simply be copied from the 1st trace, and will need to be modified accordingly outside this method.
   * 
   * @param trace1 the 1st trace.
   * @param trace2 the 2nd trace.
   * @param weight1 the weighting for the 1st trace.
   * @param weight2 the weighting for the 2nd trace.
   * @return the combine trace.
   */
  public static Trace combineTraces(final Trace trace1, final Trace trace2, final float weight1, final float weight2) {
    int numSamples = trace1.getNumSamples();
    float[] data = new float[numSamples];

    // Combine the 2 traces only if neither of them is 'missing'.
    if (!trace1.isMissing() && !trace2.isMissing()) {
      float weightSum = weight1 + weight2;
      float[] data0 = trace1.getDataReference();
      float[] data1 = trace2.getDataReference();
      for (int k = 0; k < numSamples; k++) {
        data[k] = (data0[k] * weight1 + data1[k] * weight2) / weightSum;
      }
    }
    // Return the combined trace.
    return new Trace(trace1, data);
  }
}
