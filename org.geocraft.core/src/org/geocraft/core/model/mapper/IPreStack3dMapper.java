/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.mapper;


import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.mapper.IPostStack3dMapper.StorageOrganization;
import org.geocraft.core.model.seismic.PreStack3d;


public interface IPreStack3dMapper extends ISeismicDatasetMapper<PreStack3d> {

  /**
   * Gets the storage order of the data.
   * @return the storage order of the data.
   */
  PreStack3d.StorageOrder getStorageOrder(PreStack3d ps3d);

  /**
   * Gets the storage order of the data.
   */
  void setStorageOrder(PreStack3d.StorageOrder storageOrder);

  /**
   * Get the storage organization of the data
   */
  StorageOrganization getStorageOrganization();

  TraceData getTracesByInlineXline(PreStack3d ps3d, final float inline, final float xline, final float offsetStart,
      final float offsetEnd, final float zStart, final float zEnd);

  TraceData getTracesByInlineOffset(PreStack3d ps3d, final float inline, final float offset, final float xlineStart,
      final float xlineEnd, final float zStart, final float zEnd);

  TraceData getTracesByXlineOffset(PreStack3d ps3d, final float xline, final float offset, final float inlineStart,
      final float inlineEnd, final float zStart, final float zEnd);

  TraceData getTraces(PreStack3d ps3d, final float[] inlines, final float[] xlines, final float[] offsets,
      float zStart, float zEnd);

  void putTraces(PreStack3d ps3d, final TraceData traceData);
}
