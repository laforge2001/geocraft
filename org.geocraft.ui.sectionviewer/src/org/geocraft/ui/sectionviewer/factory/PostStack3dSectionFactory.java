/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.factory;


import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;


public class PostStack3dSectionFactory {

  /**
   * Creates an inline section and updates the associated section viewer.
   * The inline section is based on the associated poststack volume and the currently selected inline #.
   */
  public static TraceSection createInlineSection(final PostStack3d poststack, final float inline,
      final int xlineDecimation) {
    SeismicSurvey3d survey = poststack.getSurvey();
    TraceAxisKey[] traceAxisKeys = { TraceAxisKey.INLINE, TraceAxisKey.XLINE };
    FloatRange[] traceAxisKeyRanges = new FloatRange[2];
    traceAxisKeyRanges[0] = new FloatRange(inline, inline, survey.getInlineDelta());
    traceAxisKeyRanges[1] = decimateRange(survey.getNumXlines(), survey.getXlineStart(), survey.getXlineDelta(),
        xlineDecimation);
    return new TraceSection(SectionType.INLINE_SECTION, survey, traceAxisKeys, traceAxisKeyRanges, poststack
        .getZDomain(), poststack.getZStart(), poststack.getZEnd());
  }

  /**
  * Creates an xline section and updates the associated section viewer.
  * The xline section is based on the associated poststack volume and the currently selected xline #.
  */
  public static TraceSection createXlineSection(final PostStack3d poststack, final float xline,
      final int inlineDecimation) {
    SeismicSurvey3d survey = poststack.getSurvey();
    TraceAxisKey[] traceAxisKeys = { TraceAxisKey.XLINE, TraceAxisKey.INLINE };
    FloatRange[] traceAxisKeyRanges = new FloatRange[2];
    traceAxisKeyRanges[0] = new FloatRange(xline, xline, survey.getXlineDelta());
    traceAxisKeyRanges[1] = decimateRange(survey.getNumInlines(), survey.getInlineStart(), survey.getInlineDelta(),
        inlineDecimation);
    return new TraceSection(SectionType.XLINE_SECTION, survey, traceAxisKeys, traceAxisKeyRanges, poststack
        .getZDomain(), poststack.getZStart(), poststack.getZEnd());
  }

  public static FloatRange decimateRange(final int count, final float start, final float delta, final int decimation) {
    int decimatedCount = 1 + (count - 1) / decimation;
    float decimatedStart = start;
    float decimatedDelta = delta * decimation;
    float decimatedEnd = decimatedStart + (decimatedCount - 1) * decimatedDelta;
    return new FloatRange(decimatedStart, decimatedEnd, decimatedDelta);
  }
}
