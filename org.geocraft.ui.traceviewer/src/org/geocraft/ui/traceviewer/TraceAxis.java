/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.traceviewer;


import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.plot.axis.Axis;
import org.geocraft.ui.plot.axis.AxisRange;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.label.Label;


public class TraceAxis extends Axis {

  public TraceAxis(final AxisRange range) {
    super(new Label("Trace", Orientation.HORIZONTAL, Alignment.CENTER, true), Unit.TRACES, range, Orientation.HORIZONTAL, AxisDirection.LEFT_TO_RIGHT);
  }
}
