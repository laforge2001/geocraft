/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.seismic;


import java.io.IOException;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.mapper.IPostStack3dMapper.StorageOrganization;
import org.geocraft.core.service.ServiceProvider;


/**
 * Contains the attributes specific to a stacked 3D seismic dataset.
 * A stacked 3D dataset represents a three dimensional array of sample values, the
 * three dimensions being inline, xline and z (time or depth).<p>
 * <p>
 * Trace data may be read using the following methods:<p>
 *   Read an arbitrary rectangle of data getBrick(). <p>
 *   Read a vertical plane of data: getInline(), getXline().<p>
 *   Read an arbitrary set of traces: getTraces().<p>
 *   Read a horizontal plane of data: getSlice().<p>
 *   Read an arbitrary set of samples: getSamples().<p>
 * <p>
 * Trace data may be written using the following methods:<p>
 *   Write an arbitrary set of traces: putTraces().<p>
 *   Write a horizontal plane of data: putSlice().<p>
 *   Write an arbitrary set of samples: putSamples().
 */
public class PostStack3d extends SeismicDataset {

  /**
   * Enumeration for the data storage order for PostStack3d volumes.
   * The data storage order describes the order in which the data was written to the volume file, and therefore
   * represents the preferred direction to use when reading it (for best performance).
   */
  public enum StorageOrder {
    /** The data is stored such that inline varies slowest, then xline, z varies fastest. */
    INLINE_XLINE_Z("Inline,Xline"),
    /** The data is stored such that xline varies slowest, then inline, z varies fastest. */
    XLINE_INLINE_Z("Xline,Inline"),
    /** The data is stored such that z varies slowest, then inline, xline varies fastest. */
    Z_INLINE_XLINE("Z-Inline"),
    /** The data is stored such that z varies slowest, then xline, inline varies fastest. */
    Z_XLINE_INLINE("Z-Xline"),
    /**
     * The storage order will be auto-calculated upon loading.
     * This is done by reading several inlines and xlines and recording the access times.
     */
    AUTO_CALCULATED("Auto-Calculated");

    private final String _title;

    StorageOrder(final String title) {
      _title = title;
    }

    public String getTitle() {
      return _title;
    }

    @Override
    public String toString() {
      return getTitle();
    }

    public static String[] valuesAsStrings() {
      StorageOrder[] values = values();
      String[] strings = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        strings[i] = values[i].getTitle();
      }
      return strings;
    }

    public static StorageOrder lookupByName(final String title) {
      if (title == null) {
        return AUTO_CALCULATED;
      }

      for (StorageOrder order : values()) {
        if (order.getTitle().equalsIgnoreCase(title)) {
          return order;
        }
      }
      return AUTO_CALCULATED;
    }
  }

  /**
   * Enumeration of the available data ordering when reading z-slices.
   */
  public enum SliceBufferOrder {
    /** Inline varies slowest; Xline varies fastest. */
    INLINE_XLINE,
    /** Xline varies slowest; Inline varies fastest. */
    XLINE_INLINE
  }

  /**
   * The volume's inline range (start, end, delta).
   * If the starting inline is less than the ending inline, then the increment will be positive;
   * otherwise, negative. The volume's inline increment must be a multiple of the
   * seismic survey's inline delta. If the inline increment is larger than the survey's this
   * indicates the volume is decimated (de-populated) along this axis.
   */
  private FloatRange _inlineRange;

  /**
   * The volume's xline range (start, end, delta).
   * If the starting xline is less than the ending xline, then the increment will be positive;
   * otherwise, negative. The volume's xline delta must be a multiple of the
   * seismic survey's xline delta. If the xline delta is larger than the survey's this
   * indicates the volume is decimated (de-populated) along this axis.
   */
  private FloatRange _xlineRange;

  /** The seismic geometry associated with the volume. */
  private SeismicSurvey3d _survey;

  /** The null value. */
  public static final float NULL_VALUE = 0f;

  /**
   * Constructs a poststack 3D seismic dataset with the given name.
   * <p>
   * The dataset will be lazy-loaded from its underlying datastore, as defined by the given mapper.
   * 
   * @param name the display name of the seismic dataset.
   * @param mapper the mapper to the underlying datastore.
   */
  public PostStack3d(final String name, final IPostStack3dMapper mapper) {
    super(name, mapper);
  }

  /**
   * Constructs a poststack 3D seismic dataset with the given name.
   * <p>
   * The dataset will be lazy-loaded from its underlying datastore, as defined by the given mapper.
   * 
   * @param name the display name of the seismic dataset.
   * @param survey the seismic survey on which the volume is defined.
   * @param mapper the mapper to the underlying datastore.
   */
  public PostStack3d(final String name, final SeismicSurvey3d survey, final IPostStack3dMapper mapper) {
    super(name, mapper);
    _survey = survey;
  }

  /**
   * Gets the 3D seismic survey on which the dataset is defined.
   * 
   * @return the 3D seismic survey.
   */
  public SeismicSurvey3d getSurvey() {
    if (_survey == null) {
      load();
    }
    return _survey;
  }

  /**
   * Sets the 3D seismic survey on which the dataset is defined.
   * 
   * @param survey the 3D seismic survey.
   */
  public void setSurvey(final SeismicSurvey3d survey) {
    _survey = survey;
    setDirty(true);
  }

  /**
   * Sets the inline range (start,end,delta) of the 3D seismic dataset.
   * 
   * @param start the starting inline.
   * @param end the ending inline.
   * @param delta the inline delta.
   */
  public void setInlineRangeAndDelta(final float start, final float end, final float delta) {
    validateZRange(start, end, delta);
    _inlineRange = new FloatRange(start, end, delta);
  }

  /**
   * Sets the xline range (start,end,delta) of the 3D seismic dataset.
   * 
   * @param start the starting xline.
   * @param end the ending xline.
   * @param delta the xline delta.
   */
  public void setXlineRangeAndDelta(final float start, final float end, final float delta) {
    validateZRange(start, end, delta);
    _xlineRange = new FloatRange(start, end, delta);
  }

  /**
  * Returns the number of inlines contained in the 3D seismic dataset.
  * 
  * @return the number of inlines.
  */
  public int getNumInlines() {
    load();
    return _inlineRange.getNumSteps();
  }

  /**
   * Returns number of xlines contained in the 3D seismic dataset.
   * 
   * @return the number of xlines.
   */
  public int getNumXlines() {
    load();
    return _xlineRange.getNumSteps();
  }

  /**
   * Returns the inline range (start,end,delta) of the 3D seismic dataset.
   * 
   * @return the inline range.
   */
  public FloatRange getInlineRange() {
    load();
    return _inlineRange;
  }

  /**
   * Returns the xline range (start,end,delta) of the 3D seismic dataset.
   * 
   * @return the xline range.
   */
  public FloatRange getXlineRange() {
    load();
    return _xlineRange;
  }

  /**
   * Returns the starting inline of the 3D seismic dataset.
   * 
   * @return the starting inline.
   */
  public float getInlineStart() {
    load();
    return _inlineRange.getStart();
  }

  /**
   * Returns the ending inline of the 3D seismic dataset.
   * 
   * @return the ending inline.
   */
  public float getInlineEnd() {
    load();
    return _inlineRange.getEnd();
  }

  /**
   * Returns the inline delta of the 3D seismic dataset.
   * 
   * @return the inline delta.
   */
  public float getInlineDelta() {
    load();
    return _inlineRange.getDelta();
  }

  /**
   * Returns the starting xline of the 3D seismic dataset.
   * 
   * @return the starting xline.
   */
  public float getXlineStart() {
    load();
    return _xlineRange.getStart();
  }

  /**
   * Returns the ending xline of the 3D seismic dataset.
   * 
   * @return the ending xline.
   */
  public float getXlineEnd() {
    load();
    return _xlineRange.getEnd();
  }

  /**
   * Returns the xline delta of the 3D seismic dataset.
   * 
   * @return the xline delta.
   */
  public float getXlineDelta() {
    load();
    return _xlineRange.getDelta();
  }

  /**
   * Returns the storage order of the data contained in the 3D seismic dataset.
   * This can be used to determine the preferred order in which data from the volume should be accessed.
   * 
   * @param order the preferred order.
   */
  public StorageOrder getPreferredOrder() {
    load();
    if (_mapper != null) {
      return ((IPostStack3dMapper) _mapper).getStorageOrder(this);
    }
    return StorageOrder.INLINE_XLINE_Z;
  }

  @Override
  public StorageFormat getStorageFormat() {
    load();
    return ((IPostStack3dMapper) _mapper).getStorageFormat();
  }

  public StorageOrganization getStorageOrganization() {
    load();
    return ((IPostStack3dMapper) _mapper).getStorageOrganization();
  }

  @Override
  public void close() {
    if (_mapper != null) {
      ((IPostStack3dMapper) _mapper).close();
    }
    setDirty(false);
    markGhost();
  }

  @Override
  public void load() {
    super.load();
    //calculateExtent();
  }

  /**
   * Loads the entity from the underlying datastore if it is not already loaded.
   * The persistence mapper will handle the actual reading, and will set all the
   * entity attributes it can.
   */
  @Override
  public void load(final IJobChangeListener listener) {
    // If the entity is not a ghost (i.e. already loaded or loading), then simply return.
    if (!isGhost()) {
      if (listener != null) {
        listener.done(new IJobChangeEvent() {

          @Override
          public long getDelay() {
            return 0;
          }

          @Override
          public Job getJob() {
            return null;
          }

          @Override
          public IStatus getResult() {
            return ValidationStatus.ok();
          }

        });
      }
      return;
    }
    try {
      markLoaded();
      if (listener == null) {
        read((IJobChangeListener[]) null);
        calculateExtent();
      } else {
        IJobChangeListener listener2 = new JobChangeAdapter() {

          @Override
          public void done(final IJobChangeEvent event) {
            PostStack3d.this.calculateExtent();
          }

        };
        read(listener, listener2);
      }
    } catch (IOException ex) {
      markGhost();
      // Log the exception.
      String message = "Error loading " + getType() + " \'" + toString() + "\' from datastore: " + ex.getMessage();
      ServiceProvider.getLoggingService().getLogger(getClass()).error(message);
      // Throw a runtime exception.
      throw new RuntimeException(ex.getMessage());
    }
  }

  /**
   * Calculates the inline,xline,z extents of the 3D seismic datasets.
   * <p>
   * Note: This is a private method that is called internally after load().
   */
  private void calculateExtent() {
    if (_inlineRange == null || _xlineRange == null || _zRange == null || _survey == null) {
      return;
    }
    float inlineStart = _inlineRange.getStart();
    float inlineEnd = _inlineRange.getEnd();
    float xlineStart = _xlineRange.getStart();
    float xlineEnd = _xlineRange.getEnd();
    float[] inlines = { inlineStart, inlineEnd, inlineEnd, inlineStart, inlineStart, inlineEnd, inlineEnd, inlineStart };
    float[] xlines = { xlineStart, xlineStart, xlineEnd, xlineEnd, xlineStart, xlineStart, xlineEnd, xlineEnd };

    Point3d[] points = _survey.transformInlineXlineToXY(inlines, xlines).getCopyOfPoints();

    float zStart = _zRange.getStart();
    float zEnd = _zRange.getEnd();

    points[0] = new Point3d(points[0].getX(), points[0].getY(), zStart);
    points[1] = new Point3d(points[1].getX(), points[1].getY(), zStart);
    points[2] = new Point3d(points[2].getX(), points[2].getY(), zStart);
    points[3] = new Point3d(points[3].getX(), points[3].getY(), zStart);
    points[4] = new Point3d(points[4].getX(), points[4].getY(), zEnd);
    points[5] = new Point3d(points[5].getX(), points[5].getY(), zEnd);
    points[6] = new Point3d(points[6].getX(), points[6].getY(), zEnd);
    points[7] = new Point3d(points[7].getX(), points[7].getY(), zEnd);

    CoordinateSeries ps3dExtent = CoordinateSeries
        .createDirect(points, _survey.getCornerPoints().getCoordinateSystem());

    setExtent(ps3dExtent);
    setActualExtent(ps3dExtent);
    setDirty(false);
  }

  /**
   * Gets traces from the 3D seismic dataset for the given brick.
   * 
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data collection object.
   */
  public TraceData getBrick(final float inlineStart, final float inlineEnd, final float xlineStart,
      final float xlineEnd, final float zStart, final float zEnd) {
    // Validate the inline, xline and z values.
    validateInline(inlineStart, true);
    validateInline(inlineEnd, true);
    validateXline(xlineStart, true);
    validateXline(xlineEnd, true);
    validateZ(zStart);
    validateZ(zEnd);

    // Read the traces.
    return ((IPostStack3dMapper) _mapper).getBrick(this, inlineStart, inlineEnd, xlineStart, xlineEnd, zStart, zEnd);
  }

  /**
   * Gets traces from the 3D seismic dataset for the given inline.
   * 
   * @param inline the inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data collection object.
   */
  public TraceData getInline(final float inline, final float xlineStart, final float xlineEnd, final float zStart,
      final float zEnd) {
    // Validate the inline, xline and z values.
    validateInline(inline, true);
    validateXline(xlineStart, true);
    validateXline(xlineEnd, true);
    validateZ(zStart);
    validateZ(zEnd);

    // Read the traces.
    return ((IPostStack3dMapper) _mapper).getInline(this, inline, xlineStart, xlineEnd, zStart, zEnd);
  }

  /**
   * Gets traces from the 3D seismic dataset for the given xline.
   * 
   * @param xline the xline.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data collection object.
   */
  public TraceData getXline(final float xline, final float inlineStart, final float inlineEnd, final float zStart,
      final float zEnd) {
    // Validate the inline, xline and z values.
    validateXline(xline, true);
    validateInline(inlineStart, true);
    validateInline(inlineEnd, true);
    validateZ(zStart);
    validateZ(zEnd);

    // Read the traces.
    return ((IPostStack3dMapper) _mapper).getXline(this, xline, inlineStart, inlineEnd, zStart, zEnd);
  }

  /**
   * Gets traces from this 3D seismic dataset for the given inline,xline values.
   * <p>
   * The inline and xline arrays represent location pairs, and therefore the arrays must be of equal length.
   * 
   * @param inlines the array of inline values.
   * @param xlines the array of xline values.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data collection object.
   */
  public TraceData getTraces(final float[] inlines, final float[] xlines, final float zStart, final float zEnd) {
    // Validate the inline, xline and z values.
    validateInlineXlineArrays(inlines, xlines, true);
    validateZ(zStart);
    validateZ(zEnd);

    // Read the traces.
    return ((IPostStack3dMapper) _mapper).getTraces(this, inlines, xlines, zStart, zEnd);
  }

  /**
   * Puts the given traces into this 3D seismic dataset.
   * <p>
   * The traces are required to have headers that contain the inline,xline
   * coordinates of the trace, otherwise an exception will be thrown.
   * @param traceData the collection of traces to put.
   */
  public void putTraces(final TraceData traceData) {
    // Validate the inline and xline coordinates of the traces.
    int numTraces = traceData.getNumTraces();
    for (int i = 0; i < numTraces; i++) {
      Trace trace = traceData.getTrace(i);
      if (!trace.isMissing()) {
        validateInline(trace.getInline(), false);
        validateXline(trace.getXline(), false);
      }
    }
    // Validate the z range of the traces.
    validateZMax(traceData.getStartZ());
    validateZMax(traceData.getEndZ());

    // Write the traces.
    ((IPostStack3dMapper) _mapper).putTraces(this, traceData);
  }

  /**
   * Gets z-slice data from this 3D seismic dataset for the given inline,xline ranges.
   * <p>
   * The data is returned in a single-dimension array in the requested buffer order.
   * 
   * @param inlineStart the starting inline value.
   * @param inlineEnd the ending inline value.
   * @param xlineStart the starting xline value.
   * @param xlineEnd the ending xline value.
   * @param z the z-slice value.
   * @param order the buffer order.
   * @return the slice data array.
   */
  public float[] getSlice(final float inlineStart, final float inlineEnd, final float xlineStart, final float xlineEnd,
      final float z, final SliceBufferOrder order) {
    validateInline(inlineStart, true);
    validateInline(inlineEnd, true);
    validateXline(xlineStart, true);
    validateXline(xlineEnd, true);
    validateZ(z);
    return ((IPostStack3dMapper) _mapper).getSlice(this, z, inlineStart, inlineEnd, xlineStart, xlineEnd, order);
  }

  /**
   * Gets z-slice data from this 3D seismic dataset for the given inline,xline ranges.
   * <p>
   * The data is returned in a single-dimension array in the requested buffer order.
   * 
   * @param inlineStart the starting inline value.
   * @param inlineEnd the ending inline value.
   * @param xlineStart the starting xline value.
   * @param xlineEnd the ending xline value.
   * @param z the z-slice value.
   * @param order the buffer order.
   * @param missingValue the value to return for "missing" traces.
   * @return the slice data array.
   */
  public float[] getSlice(final float inlineStart, final float inlineEnd, final float xlineStart, final float xlineEnd,
      final float z, final SliceBufferOrder order, final float missingValue) {
    validateInline(inlineStart, true);
    validateInline(inlineEnd, true);
    validateXline(xlineStart, true);
    validateXline(xlineEnd, true);
    validateZ(z);
    return ((IPostStack3dMapper) _mapper).getSlice(this, z, inlineStart, inlineEnd, xlineStart, xlineEnd, order,
        missingValue);
  }

  public void putSlice(final float inlineStart, final float inlineEnd, final float xlineStart, final float xlineEnd,
      final float z, final SliceBufferOrder order, final float[] samples) throws Exception {
    validateInline(inlineStart, false);
    validateInline(inlineEnd, false);
    validateXline(xlineStart, false);
    validateXline(xlineEnd, false);
    validateZ(z);
    ((IPostStack3dMapper) _mapper).putSlice(this, z, inlineStart, inlineEnd, xlineStart, xlineEnd, order, samples);
  }

  /**
   * Gets arbitrary sample data from this 3D seismic dataset for the given inline, xline and z locations.
   * <p>
   * The inline, xline and z arrays represent location pairs, and therefore the arrays must be of equal length.
   * The data is returned in a single-dimension array, in the same order as the inline, xline and z coordinates.
   * 
   * @param inlines the array of inline values.
   * @param xlines the array of xline values.
   * @param zs the array of z values.
   * @return the array of sample data.
   */
  public float[] getSamples(final float[] inlines, final float[] xlines, final float[] zs) throws Exception {
    validateInlineXlineZArrays(inlines, xlines, zs, true);
    return ((IPostStack3dMapper) _mapper).getSamples(this, inlines, xlines, zs);
  }

  /**
   * Puts arbitrary sample data into this 3D seismic dataset for the given inline, xline and z locations.
   * <p>
   * The inline, xline and z arrays represent location pairs, and therefore the arrays must be of equal length.<p>
   * The sample array must be the same length as the location arrays.
   * 
   * @param inlines the array of inline values.
   * @param xlines the array of xline values.
   * @param zs the array of z values.
   * @param samples the array of sample data to put.
   */
  public void putSamples(final float[] inlines, final float[] xlines, final float[] zs, final float[] samples) throws Exception {
    validateInlineXlineZArrays(inlines, xlines, zs, false);
    ((IPostStack3dMapper) _mapper).putSamples(this, inlines, xlines, zs, samples);
  }

  /**
   * Checks that the given inline value is valid.
   * The value is checked to make sure it is a multiple of the inline delta.
   * If <i>checkBounds</i> is <i>true<i>, it is also be checked to make sure
   * it falls within the start and end inline bounds.
   * 
   * @param inline the inline to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   * @throws IllegalArgumentException if the inline is invalid.
   */
  private void validateInline(final float inline, final boolean checkBounds) {
    validateRange("inline", inline, _survey.getInlineStart(), _survey.getInlineEnd(), _survey.getInlineDelta(),
        checkBounds);
  }

  /**
   * Checks that the given xline value is valid.
   * The value is checked to make sure it is a multiple of the xline delta.
   * If <i>checkBounds</i> is <i>true<i>, it is also be checked to make sure
   * it falls within the start and end xline bounds.
   * 
   * @param xline the xline to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   * @throws IllegalArgumentException if the xline is invalid.
   */
  private void validateXline(final float xline, final boolean checkBounds) {
    validateRange("xline", xline, _survey.getXlineStart(), _survey.getXlineEnd(), _survey.getXlineDelta(), checkBounds);
  }

  /**
   * Checks that the given arrays of inline, xline and z values are valid.
   * <p>
   * The arrays are considered valid if they are both non-null and of the same length.
   * The values are checked to make sure they are multiples of the inline, xline and z deltas.
   * If <i>checkBounds</i> is <i>true<i>, they are also be checked to make sure they fall
   * within the start and end inline, xline and z bounds.
   * 
   * @param inlines the array of inline values to validate.
   * @param xlines the array of xline values to validate.
   * @param zs the array of z values to validate.
   * @param checkBounds <i>true</i> to check the array values against the bounds of the volume; otherwise <i>false</i>.
   * @throws IllegalArgumentException if any of the array values is invalid.
   */
  private void validateInlineXlineZArrays(final float[] inlines, final float[] xlines, final float[] zs,
      final boolean checkBounds) {
    // Validate the inline and xline arrays.
    validateInlineXlineArrays(inlines, xlines, checkBounds);

    // Validate the z array is non-null and the same length as the inline and xline arrays.
    if (zs == null) {
      throw new IllegalArgumentException("Invalid z array: null.");
    }
    if (inlines.length != zs.length) {
      throw new IllegalArgumentException("Invalid array length: # z values does not equals # inlines,xlines.");
    }

    // Validate the z array values fall on an increment of the z delta value.
    for (float element : zs) {
      validateZ(element);
    }
  }

  /**
   * Checks that the given arrays of inline and xline values are valid.
   * <p>
   * The values are checked to make sure they are multiples of the inline and xline deltas.
   * If <i>checkBounds</i> is <i>true<i>, they are also be checked to make sure they fall
   * within the start and end inline and xline bounds.
   * 
   * @param inlines the array of inline values to validate.
   * @param xlines the array of xline values to validate.
   * @param checkBounds <i>true</i> to check the array values against the bounds of the volume; otherwise <i>false</i>.
   * @throws IllegalArgumentException if any of the array values is invalid.
   */
  private void validateInlineXlineArrays(final float[] inlines, final float[] xlines, final boolean checkBounds) {
    // Validate the inline and xline arrays are non-null and of equal length.
    if (inlines == null) {
      throw new IllegalArgumentException("Invalid inline array: null.");
    }
    if (xlines == null) {
      throw new IllegalArgumentException("Invalid xline array: null");
    }
    if (inlines.length != xlines.length) {
      throw new IllegalArgumentException("Invalid array length: # of inlines does not equals # of xlines.");
    }

    // Validate the inline and xline array values fall on increments of their delta values,
    // and are (optionally) within bounds.
    for (int i = 0; i < inlines.length; i++) {
      validateInline(inlines[i], checkBounds);
      validateXline(xlines[i], checkBounds);
    }
  }
}
