/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.sectionviewer;


import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * The interface for a plot trace shape.
 */
public interface IPlotTrace extends IPlotShape, IDataStats {

  /**
   * Returns the trace number.
   */
  int getTraceNo();

  /**
   * Returns the actual trace value object.
   */
  Trace getTrace();

  /**
   * Returns the original (unmodified) trace object.
   */
  Trace getOriginalTrace();

  void recomputeDataStats();
}