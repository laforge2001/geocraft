/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.factory;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.PreStack3d.StorageOrder;
import org.geocraft.core.model.seismic.TraceSection.SectionType;


public class PreStack3dSectionFactory {

  /**
   * Creates an inline section and updates the associated section viewer.
   * The inline section is based on the associated prestack volume and the currently selected inline #.
   */
  public static TraceSection createInlineSection(final PreStack3d prestack, final StorageOrder order,
      final float inline, final int xlineDecimation, final int offsetDecimation) {
    SeismicSurvey3d survey = prestack.getSurvey();
    TraceAxisKey[] traceAxisKeys;
    FloatRange[] traceAxisKeyRanges = new FloatRange[3];
    traceAxisKeyRanges[0] = new FloatRange(inline, inline, survey.getInlineDelta());
    if (isBefore(order, TraceAxisKey.XLINE, TraceAxisKey.OFFSET)) {
      traceAxisKeys = new TraceAxisKey[] { TraceAxisKey.INLINE, TraceAxisKey.XLINE, TraceAxisKey.OFFSET };
      traceAxisKeyRanges[1] = decimateRange(survey.getNumXlines(), survey.getXlineStart(), survey.getXlineDelta(),
          xlineDecimation);
      traceAxisKeyRanges[2] = decimateRange(prestack.getNumOffsets(), prestack.getOffsetStart(), prestack
          .getOffsetDelta(), offsetDecimation);
    } else {
      traceAxisKeys = new TraceAxisKey[] { TraceAxisKey.INLINE, TraceAxisKey.OFFSET, TraceAxisKey.XLINE };
      traceAxisKeyRanges[1] = decimateRange(prestack.getNumOffsets(), prestack.getOffsetStart(), prestack
          .getOffsetDelta(), offsetDecimation);
      traceAxisKeyRanges[2] = decimateRange(survey.getNumXlines(), survey.getXlineStart(), survey.getXlineDelta(),
          xlineDecimation);
    }

    return new TraceSection(SectionType.INLINE_SECTION, survey, traceAxisKeys, traceAxisKeyRanges, prestack
        .getZDomain(), prestack.getZStart(), prestack.getZEnd());
  }

  /**
  * Creates an xline section and updates the associated section viewer.
  * The xline section is based on the associated prestack volume and the currently selected xline #.
  */
  public static TraceSection createXlineSection(final PreStack3d prestack, final StorageOrder order, final float xline,
      final int inlineDecimation, final int offsetDecimation) {
    SeismicSurvey3d survey = prestack.getSurvey();
    TraceAxisKey[] traceAxisKeys;
    FloatRange[] traceAxisKeyRanges = new FloatRange[3];
    traceAxisKeyRanges[0] = new FloatRange(xline, xline, survey.getXlineDelta());
    if (isBefore(order, TraceAxisKey.INLINE, TraceAxisKey.OFFSET)) {
      traceAxisKeys = new TraceAxisKey[] { TraceAxisKey.XLINE, TraceAxisKey.INLINE, TraceAxisKey.OFFSET };
      traceAxisKeyRanges[1] = decimateRange(survey.getNumInlines(), survey.getInlineStart(), survey.getInlineDelta(),
          inlineDecimation);
      traceAxisKeyRanges[2] = decimateRange(prestack.getNumOffsets(), prestack.getOffsetStart(), prestack
          .getOffsetDelta(), offsetDecimation);
    } else {
      traceAxisKeys = new TraceAxisKey[] { TraceAxisKey.XLINE, TraceAxisKey.OFFSET, TraceAxisKey.INLINE };
      traceAxisKeyRanges[1] = decimateRange(prestack.getNumOffsets(), prestack.getOffsetStart(), prestack
          .getOffsetDelta(), offsetDecimation);
      traceAxisKeyRanges[2] = decimateRange(survey.getNumInlines(), survey.getInlineStart(), survey.getInlineDelta(),
          inlineDecimation);
    }
    return new TraceSection(SectionType.XLINE_SECTION, survey, traceAxisKeys, traceAxisKeyRanges,
        prestack.getZDomain(), prestack.getZStart(), prestack.getZEnd());
  }

  /**
   * Creates an xline section and updates the associated section viewer.
   * The xline section is based on the associated prestack volume and the currently selected xline #.
   */
  public static TraceSection createOffsetSection(final PreStack3d prestack, final StorageOrder order,
      final int inlineDecimation, final int xlineDecimation, final float offset) {
    SeismicSurvey3d survey = prestack.getSurvey();
    TraceAxisKey[] traceAxisKeys;
    FloatRange[] traceAxisKeyRanges = new FloatRange[3];
    traceAxisKeyRanges[0] = new FloatRange(offset, offset, prestack.getOffsetDelta());
    if (isBefore(order, TraceAxisKey.INLINE, TraceAxisKey.XLINE)) {
      traceAxisKeys = new TraceAxisKey[] { TraceAxisKey.OFFSET, TraceAxisKey.INLINE, TraceAxisKey.XLINE };
      traceAxisKeyRanges[1] = decimateRange(survey.getNumInlines(), survey.getInlineStart(), survey.getInlineDelta(),
          inlineDecimation);
      traceAxisKeyRanges[2] = decimateRange(survey.getNumXlines(), survey.getXlineStart(), survey.getXlineDelta(),
          xlineDecimation);
    } else {
      traceAxisKeys = new TraceAxisKey[] { TraceAxisKey.OFFSET, TraceAxisKey.XLINE, TraceAxisKey.INLINE };
      traceAxisKeyRanges[1] = decimateRange(survey.getNumXlines(), survey.getXlineStart(), survey.getXlineDelta(),
          xlineDecimation);
      traceAxisKeyRanges[2] = decimateRange(survey.getNumInlines(), survey.getInlineStart(), survey.getInlineDelta(),
          inlineDecimation);
    }
    return new TraceSection(SectionType.OFFSET_SECTION, survey, traceAxisKeys, traceAxisKeyRanges, prestack
        .getZDomain(), prestack.getZStart(), prestack.getZEnd());
  }

  /**
   * Creates an inline-xline section and updates the associated section viewer.
   * The inline-xline section is based on the associated prestack volume and the currently selected inline,xline #s.
   */
  public static TraceSection createInlineXlineGather(final PreStack3d prestack, final float inline, final float xline,
      final int offsetDecimation) {
    SeismicSurvey3d survey = prestack.getSurvey();
    TraceAxisKey[] traceAxisKeys = { TraceAxisKey.INLINE, TraceAxisKey.XLINE, TraceAxisKey.OFFSET };
    FloatRange[] traceAxisKeyRanges = new FloatRange[3];
    traceAxisKeyRanges[0] = new FloatRange(inline, inline, survey.getInlineDelta());
    traceAxisKeyRanges[1] = new FloatRange(xline, xline, survey.getXlineDelta());
    traceAxisKeyRanges[2] = decimateRange(prestack.getNumOffsets(), prestack.getOffsetStart(), prestack
        .getOffsetDelta(), offsetDecimation);
    return new TraceSection(SectionType.INLINE_XLINE_GATHER, survey, traceAxisKeys, traceAxisKeyRanges, prestack
        .getZDomain(), prestack.getZStart(), prestack.getZEnd());
  }

  /**
   * Creates an inline-offset section and updates the associated section viewer.
   * The inline-offset section is based on the associated prestack volume and the currently selected inline,offset #s.
   */
  public static TraceSection createInlineOffsetSection(final PreStack3d prestack, final float inline,
      final float offset, final int xlineDecimation) {
    SeismicSurvey3d survey = prestack.getSurvey();
    TraceAxisKey[] traceAxisKeys = { TraceAxisKey.INLINE, TraceAxisKey.OFFSET, TraceAxisKey.XLINE };
    FloatRange[] traceAxisKeyRanges = new FloatRange[3];
    traceAxisKeyRanges[0] = new FloatRange(inline, inline, survey.getInlineDelta());
    traceAxisKeyRanges[1] = new FloatRange(offset, offset, prestack.getOffsetDelta());
    traceAxisKeyRanges[2] = decimateRange(survey.getNumXlines(), survey.getXlineStart(), survey.getXlineDelta(),
        xlineDecimation);
    return new TraceSection(SectionType.INLINE_OFFSET_GATHER, survey, traceAxisKeys, traceAxisKeyRanges, prestack
        .getZDomain(), prestack.getZStart(), prestack.getZEnd());
  }

  /**
   * Creates an xline-offset section and updates the associated section viewer.
   * The xline-offset section is based on the associated prestack volume and the currently selected xline,offset #s.
   */
  public static TraceSection createXlineOffsetSection(final PreStack3d prestack, final float xline, final float offset,
      final int inlineDecimation) {
    SeismicSurvey3d survey = prestack.getSurvey();
    TraceAxisKey[] traceAxisKeys = { TraceAxisKey.XLINE, TraceAxisKey.OFFSET, TraceAxisKey.INLINE };
    FloatRange[] traceAxisKeyRanges = new FloatRange[3];
    traceAxisKeyRanges[0] = new FloatRange(xline, xline, survey.getXlineDelta());
    traceAxisKeyRanges[1] = new FloatRange(offset, offset, prestack.getOffsetDelta());
    traceAxisKeyRanges[2] = decimateRange(survey.getNumInlines(), survey.getInlineStart(), survey.getInlineDelta(),
        inlineDecimation);
    return new TraceSection(SectionType.XLINE_OFFSET_GATHER, survey, traceAxisKeys, traceAxisKeyRanges, prestack
        .getZDomain(), prestack.getZStart(), prestack.getZEnd());
  }

  /**
   * Creates an inline-xline-offset section and updates the associated section viewer.
   * The inline-xline-offset section is based on the associated prestack volume and the currently selected inline,xline,offset #s.
   */
  public static TraceSection createInlineXlineOffsetSection(final PreStack3d prestack, final float inline,
      final float xline, final float offset) {
    SeismicSurvey3d survey = prestack.getSurvey();
    TraceAxisKey[] traceAxisKeys = { TraceAxisKey.INLINE, TraceAxisKey.XLINE, TraceAxisKey.OFFSET };
    FloatRange[] traceAxisKeyRanges = new FloatRange[3];
    traceAxisKeyRanges[0] = new FloatRange(inline, inline, survey.getInlineDelta());
    traceAxisKeyRanges[1] = new FloatRange(xline, xline, survey.getXlineDelta());
    traceAxisKeyRanges[2] = new FloatRange(offset, offset, prestack.getOffsetDelta());
    return new TraceSection(SectionType.INLINE_XLINE_OFFSET_TRACE, survey, traceAxisKeys, traceAxisKeyRanges, prestack
        .getZDomain(), prestack.getZStart(), prestack.getZEnd());
  }

  public static FloatRange decimateRange(final int count, final float start, final float delta, final int decimation) {
    int decimatedCount = 1 + (count - 1) / decimation;
    float decimatedStart = start;
    float decimatedDelta = delta * decimation;
    float decimatedEnd = decimatedStart + (decimatedCount - 1) * decimatedDelta;
    return new FloatRange(decimatedStart, decimatedEnd, decimatedDelta);
  }

  private static boolean isBefore(final StorageOrder order, final TraceAxisKey key1, final TraceAxisKey key2) {
    TraceAxisKey[] keys = StorageOrder.getKeys(order);
    List<TraceAxisKey> list = new ArrayList<TraceAxisKey>();
    for (TraceAxisKey key : keys) {
      list.add(key);
    }
    int index1 = list.indexOf(key1);
    int index2 = list.indexOf(key2);
    System.out.println("KEY1: " + key1 + "=" + index1 + " KEY2: " + key2 + "=" + index2);
    if (index1 == -1) {
      throw new IllegalArgumentException("Invalid key: " + key1);
    }
    if (index2 == -1) {
      throw new IllegalArgumentException("Invalid key: " + key2);
    }
    return index1 < index2;
  }
}
