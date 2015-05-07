/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.factory;


import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;


public class PostStack2dSectionFactory {

  /**
  * Creates an inline section and updates the associated section viewer.
  * The inline section is based on the associated poststack volume and the currently selected inline #.
  */
  public static TraceSection createInlineSection(final PostStack2dLine poststack, final int xlineDecimation) {
    float inline = poststack.getLineNumber();
    TraceAxisKey[] traceAxisKeys = { TraceAxisKey.INLINE, TraceAxisKey.CDP };
    FloatRange[] traceAxisKeyRanges = new FloatRange[2];
    traceAxisKeyRanges[0] = new FloatRange(inline, inline, 1);
    traceAxisKeyRanges[1] = decimateRange(poststack.getNumCdps(), poststack.getCdpStart(), poststack.getCdpDelta(),
        xlineDecimation);
    return new TraceSection(SectionType.INLINE_SECTION, poststack.getSeismicLine(), traceAxisKeys, traceAxisKeyRanges,
        poststack.getZDomain(), poststack.getZStart(), poststack.getZEnd());
  }

  public static FloatRange decimateRange(final int count, final float start, final float delta, final int decimation) {
    int decimatedCount = 1 + (count - 1) / decimation;
    float decimatedStart = start;
    float decimatedDelta = delta * decimation;
    float decimatedEnd = decimatedStart + (decimatedCount - 1) * decimatedDelta;
    return new FloatRange(decimatedStart, decimatedEnd, decimatedDelta);
  }
}
