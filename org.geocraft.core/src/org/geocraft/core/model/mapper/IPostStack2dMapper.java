/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.model.mapper;


import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.seismic.PostStack2dLine;


public interface IPostStack2dMapper extends ISeismicDatasetMapper<PostStack2dLine> {

  /**
   * Gets a collection of arbitrary traces (for a single line) from the datastore.
   * The trace data object returned will contain data for the traces
   * located at the specified CDP numbers, and between the starting
   * and ending z values.
   * @param ps2d the PostStack2d entity.
   * @param cdps the array of CDP numbers.
   * @param zStart The starting z value.
   * @param zEnd The ending z value.
   * @return the trace data object.
   */
  TraceData getTraces(PostStack2dLine ps2d, float[] cdps, float zStart, float zEnd);

  /**
   * Put a collection of arbitrary trace (for a single line) to the datastore.
   * The trace data object passed in must contain data for the traces
   * located at the specified CDP numbers, and between the starting
   * and ending z values.
   * @param ps2d the PostStack2d entity.
   * @param the trace data object.
   */
  void putTraces(PostStack2dLine ps2d, TraceData traceData);

  /**
   * Gets an array of samples from the datastore.
   * The data array returned will contain data for the trace samples
   * located at the specified CDP numbers, and at the specified
   * z values. The number of values in the z series must equals the
   * size of the CDP array.
   * @param ps2d the PostStack2d entity.
   * @param cdps the array of CDP numbers.
   * @param z the array of z-values.
   * @return the array of seismic sample values for the specified sample locations.
   */
  float[] getSamples(PostStack2dLine ps2d, float[] cdps, float[] z);

  /**
   * Puts an array of samples to the datastore.
   * The data array object passed in must contain data for the trace samples
   * located at the specified CDP numbers, and at the specified z values.
   * The CDP and z arrays must be of equals size.
   * @param ps2d the PostStack2d entity.
   * @param cdps the array of CDP numbers.
   * @param z the array of z values.
   * @param samples the array of seismic sample values for the specified sample locations.
   */
  void putSamples(PostStack2dLine ps2d, float[] cdp, float[] z, Unit zUnit, float[] samples);

  /**
   * Closes the mapper access to the datastore.
   * This should be done after all I/O is done.
   */
  void close();
}
