/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.util;


import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;


public interface IPostStack3dAlgorithm {

  /**
   * Computes an arbitrary trace in the PostStack3d.
   * @param inline the inline number.
   * @param xline the xline number.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data object.
   */
  Trace computeTrace(PostStack3d ps3d, float inline, float xline, float zStart, float zEnd);

  String getAlgorithmType();

  StorageFormat getStorageFormat();

  StorageOrder getStorageOrder();

  void update(PostStack3d ps3d);

  void close();
}
