/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.model.mapper;


import java.util.Arrays;

import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.SliceBufferOrder;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;


/**
 * The interface for all mappers that map between a <code>PostStack3d</code> entity and a datastore.
 */
public interface IPostStack3dMapper extends ISeismicDatasetMapper<PostStack3d> {

  /**
   * Enumeration for the data storage organization.
   * Storage organization describes how volume data is
   * stored (i.e. as vertical traces, horizontal slices, etc).
   */
  public enum StorageOrganization {
    /** Data is written as vertical traces. */
    TRACE("Trace", "3dv", "trc"),
    /** Data is written as bricks. */
    BRICK("Bricked", "bri", "ibri", "xbri", "vbri", "hbri"),
    /** Data is written as compressed bricks/traces. */
    COMPRESSED("Compressed", "cmp");

    private final String _text;

    private final String[] _codes;

    StorageOrganization(final String text, final String... codes) {
      _text = text;
      _codes = codes;
    }

    public static StorageOrganization lookupByCode(final String code) {
      for (StorageOrganization storageOrganization : StorageOrganization.values()) {
        for (String c : storageOrganization.getCodes()) {
          if (c.equalsIgnoreCase(code)) {
            return storageOrganization;
          }
        }
      }
      return StorageOrganization.TRACE;
    }

    public String[] getCodes() {
      return Arrays.copyOf(_codes, _codes.length);
    }

    @Override
    public String toString() {
      return _text;
    }
  }

  public enum BrickType {
    DEFAULT("Default Bricks", "bri"),
    INLINE("Inline Bricks", "ibri"),
    XLINE("Xline Bricks", "xbri"),
    VERTICAL("Vertical Bricks", "vbri"),
    HORIZONTAL("Horizontal Bricks", "hbri");

    private final String _text;

    private final String _code;

    BrickType(final String text, final String code) {
      _text = text;
      _code = code;
    }

    public static BrickType lookupByCode(final String code) {
      for (BrickType type : BrickType.values()) {
        if (type.getCode().equalsIgnoreCase(code)) {
          return type;
        }
      }
      return BrickType.DEFAULT;
    }

    public String getCode() {
      return _code;
    }

    @Override
    public String toString() {
      return _text;
    }
  }

  /**
   * Gets a rectangular area of trace data from the datastore.
   * 
   * @param inlineStart the starting inline number.
   * @param inlineEnd the ending inline number.
   * @param xlineStart the starting crossline number.
   * @param xlineEnd the ending crossline number.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data object.
   */
  TraceData getBrick(PostStack3d postStack3d, float inlineStart, float inlineEnd, float xlineStart, float xlineEnd,
      float zStart, float zEnd);

  /**
   * Gets an inline of traces from the datastore.
   * 
   * @param inline the inline number.
   * @param xlineStart the starting crossline number.
   * @param xlineEnd the ending crossline number.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data object.
   */
  TraceData getInline(PostStack3d ps3d, float inline, float xlineStart, float xlineEnd, float zStart, float zEnd);

  /**
   * Gets a crossline of traces from the datastore.
   * 
   * @param xline the crossline number.
   * @param inlineStart the starting inline number.
   * @param inlineEnd the ending inline number.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data object.
   */
  TraceData getXline(PostStack3d ps3d, float xline, float inlineStart, float inlineEnd, float zStart, float zEnd);

  /**
   * Gets a collection of arbitrary traces from the datastore.
   * 
   * @param inlines the array of inline numbers.
   * @param xlines the array of crossline numbers.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data object.
   */
  TraceData getTraces(PostStack3d ps3d, float[] inlines, float[] xlines, float zStart, float zEnd);

  /**
   * Gets an array of slice data from the datastore.
   * 
   * @param z the slice z value (time or depth).
   * @param inlineStart the inline start value.
   * @param inlineEnd the inline end value.
   * @param xlineStart the xline start value.
   * @param xlineEnd the xline end value.
   * @param order the slice buffer order.
   * @return the slice data array.
   */
  float[] getSlice(PostStack3d ps3d, float z, float inlineStart, float inlineEnd, float xlineStart, float xlineEnd,
      SliceBufferOrder order);

  /**
   * Gets an array of slice data from the datastore.
   * 
   * @param z the slice z value (time or depth).
   * @param inlineStart the inline start value.
   * @param inlineEnd the inline end value.
   * @param xlineStart the xline start value.
   * @param xlineEnd the xline end value.
   * @param order the slice buffer order.
   * @return the slice data array.
   */
  float[] getSlice(PostStack3d ps3d, float z, float inlineStart, float inlineEnd, float xlineStart, float xlineEnd,
      SliceBufferOrder order, float missingValue);

  /**
   * Gets an array of samples from the datastore.
   * 
   * @param inline An array of inline values for the desired samples.
   * @param xline An array of xline values for the desired samples.
   * @param z An array of z values (time or depth) for the desired samples.
   * @return the array of seismic sample values for the specified sample locations.
   */
  float[] getSamples(PostStack3d ps3d, float[] inline, float[] xline, float[] z);

  /**
   * Puts a collection of arbitrary traces to the datastore.
   * 
   * @param traceData the trace data object.
   */
  void putTraces(PostStack3d ps3d, TraceData traceData);

  /**
   * Puts an array of slice data to the datastore.
   * 
   * @param z the slice z value (time or depth).
   * @param inlineStart the inline start value.
   * @param inlineEnd the inline end value.
   * @param xlineStart the xline start value.
   * @param xlineEnd the xline end value.
   * @param order the slice buffer order.
   * @param samples the slice data array.
   */
  void putSlice(PostStack3d ps3d, float z, float inlineStart, float inlineEnd, float xlineStart, float xlineEnd,
      SliceBufferOrder order, float[] samples);

  /**
   * Puts an array of samples to the datastore.
   * 
   * @param inline An array of inline values for the desired samples.
   * @param xline An array of xline values for the desired samples.
   * @param z An array of z values (time or depth) for the desired samples.
   * @param samples the array of seismic sample values for the specified sample locations.
   */
  void putSamples(PostStack3d ps3d, float[] inline, float[] xline, float[] z, float[] samples);

  /**
   * Gets the storage order of the dataset.
   * 
   * @param ps3d the PostStack3d volume.
   * @return the storage order of the dataset.
   */
  StorageOrder getStorageOrder(PostStack3d ps3d);

  /**
   * Sets the storage order of the dataset.
   * 
   * @param storageOrder the storage order to set.
   */
  void setStorageOrder(StorageOrder storageOrder);

  /**
   * Get the storage organization of the dataset.
   * 
   * @return the storage organization of the dataset.
   */
  StorageOrganization getStorageOrganization();

  /**
   * Checks if a trace index exists.
   * 
   * @param createIndex true to create a necessary trace index, otherwise false.
   */
  void checkTraceIndex();

  /**
   * Sets the storage organization and format of the dataset.
   * 
   * @param storageOrganization the storage organization of the dataset.
   * @param storageFormat the storage format of the dataset.
   */
  void setStorageOrganizationAndFormat(StorageOrganization storageOrganization, StorageFormat storageFormat,
      BrickType brickType, float fidelity);

  /**
   * Checks if a seismic dataset with the given storage organization and storage format can be created.
   * 
   * @param storageOrganization the storage organization.
   * @param storageFormat the storage format.
   * @return a string containing error messages; if no errors, the string will be empty.
   */
  String canCreate(StorageOrganization storageOrganization, StorageFormat storageFormat);

}
