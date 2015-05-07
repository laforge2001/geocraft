/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.sectionviewer;


public interface IDataStats {

  /**
   * Gets the trace data minimum.
   * @return the trace data minimum.
   */
  float getDataMinimum();

  /**
   * Gets the trace data maximum.
   * @return the trace data maximum.
   */
  float getDataMaximum();

  /**
   * Gets the trace data average.
   * @return the trace data average.
   */
  float getDataAverage();
}
