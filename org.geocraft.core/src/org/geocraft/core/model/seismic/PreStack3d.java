/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.LoadStatus;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IPreStack3dMapper;


/**
 * Contains the attributes specific to a prestack 3D seismic dataset.
 * A prestack 3D dataset represents a four dimensional array of sample values, the
 * four dimensions being inline, xline, offset and z.<p>
 * <p>
 * Trace data may be retrieved using the following methods:<p>
 *   Read offset traces at an inline,xline: getTracesAtInlineXline().<p>
 *   Read xline traces at an inline,offset: getTracesAtInlineOffset().<p>
 *   Read inline traces at an xline,offset: getTracesAtXlineOffset().<p>
 *   Read an arbitrary set of traces: getTraces().<p>
 * <p>
 * Trace data may be written using the following method:<p>
 *   Write an arbitrary set of traces: putTraces().
 */
public class PreStack3d extends SeismicDataset {

  /**
   * Enumeration for the data storage order for PreStack3d volumes.
   * The data storage order describes the order in which the data was written to the volume file, and therefore
   * represents the preferred direction to use when reading it (for best performance).
   */
  public enum StorageOrder {
    /** The data is stored such that inline varies slowest, then xline, then offset, z varies fastest. */
    INLINE_XLINE_OFFSET_Z("Inline,Xline,Offset"),
    /** The data is stored such that xline varies slowest, then inline, then offset, z varies fastest. */
    XLINE_INLINE_OFFSET_Z("Xline,Inline,Offset"),
    /** The data is stored such that inline varies slowest, then offset, then xline, z varies fastest. */
    INLINE_OFFSET_XLINE_Z("Inline,Offset,Xline"),
    /** The data is stored such that xline varies slowest, then offset, then inline, z varies fastest. */
    XLINE_OFFSET_INLINE_Z("Xline,Offset,Inline"),
    /** The data is stored such that offset varies slowest, then inline, then xline, z varies fastest. */
    OFFSET_INLINE_XLINE_Z("Offset,Inline,Xline"),
    /** The data is stored such that offset varies slowest, then xline, then inline, z varies fastest. */
    OFFSET_XLINE_INLINE_Z("Offset,Xline,Inline"),
    /**
     * The storage order will be auto-calculated upon loading.
     * This is done by reading several inlines, xlines and offsets and recording the access times.
     */
    AUTO_CALCULATED("Auto-Calculated");

    private String _name;

    private StorageOrder(final String name) {
      _name = name;
    }

    /**
     * Returns the name of the storage order.
     * 
     * @return the storage order name.
     */
    public String getName() {
      return _name;
    }

    /**
     * Returns an array of the names of the available storage orders.
     * 
     * @return an array of the storage order names.
     */
    public static String[] valuesAsStrings() {
      StorageOrder[] values = values();
      String[] strings = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        strings[i] = values[i].getName();
      }
      return strings;
    }

    /**
     * Finds a storage order based on its name.
     * 
     * @param title the name of the storage order to find.
     * @return the storage order; or <code>AUTO-CALCULATED</code> if no match found.
     */
    public static StorageOrder lookupByName(final String name) {
      if (name == null) {
        return AUTO_CALCULATED;
      }

      for (StorageOrder order : values()) {
        if (order.getName().equalsIgnoreCase(name)) {
          return order;
        }
      }
      return AUTO_CALCULATED;
    }

    /**
     * Returns an array of the trace axis keys for a given storage order.
     * 
     * @param order the storage order.
     * @return an array of the trace axis keys.
     */
    public static TraceAxisKey[] getKeys(final StorageOrder order) {
      switch (order) {
        case INLINE_OFFSET_XLINE_Z:
          return new TraceAxisKey[] { TraceAxisKey.INLINE, TraceAxisKey.OFFSET, TraceAxisKey.XLINE };
        case INLINE_XLINE_OFFSET_Z:
          return new TraceAxisKey[] { TraceAxisKey.INLINE, TraceAxisKey.XLINE, TraceAxisKey.OFFSET };
        case OFFSET_INLINE_XLINE_Z:
          return new TraceAxisKey[] { TraceAxisKey.OFFSET, TraceAxisKey.INLINE, TraceAxisKey.XLINE };
        case OFFSET_XLINE_INLINE_Z:
          return new TraceAxisKey[] { TraceAxisKey.OFFSET, TraceAxisKey.XLINE, TraceAxisKey.INLINE };
        case XLINE_INLINE_OFFSET_Z:
          return new TraceAxisKey[] { TraceAxisKey.XLINE, TraceAxisKey.INLINE, TraceAxisKey.OFFSET };
        case XLINE_OFFSET_INLINE_Z:
          return new TraceAxisKey[] { TraceAxisKey.XLINE, TraceAxisKey.OFFSET, TraceAxisKey.INLINE };
        default:
          throw new IllegalArgumentException("Invalid order: " + order);
      }
    }

    @Override
    public String toString() {
      return getName();
    }
  }

  /** The seismic survey associated with the prestack 3D. */
  private SeismicSurvey3d _survey;

  /** The inline range (start, end and delta). */
  private FloatRange _inlineRange;

  /** The xline range (start, end and delta). */
  private FloatRange _xlineRange;

  /** The offset range (start, end and delta). */
  private FloatRange _offsetRange;

  public PreStack3d(final String name, final IMapper mapper) {
    super(name, mapper);
  }

  /**
   * Returns the number of inlines in the prestack dataset.
   */
  public int getNumInlines() {
    return _inlineRange.getNumSteps();
  }

  /**
   * Returns the number of xlines in the prestack dataset.
   */
  public int getNumXlines() {
    return _xlineRange.getNumSteps();
  }

  /**
   * Returns the number of offsets in the prestack dataset.
   */
  public int getNumOffsets() {
    return _offsetRange.getNumSteps();
  }

  /**
   * Returns the inline range (start,end,delta) of the prestack dataset.
   */
  public FloatRange getInlineRange() {
    load();
    return _inlineRange;
  }

  /**
   * Returns the xline range (start,end,delta) of the prestack dataset.
   */
  public FloatRange getXlineRange() {
    load();
    return _xlineRange;
  }

  /**
   * Returns the offset range (start,end,delta) of the prestack dataset.
   */
  public FloatRange getOffsetRange() {
    load();
    return _offsetRange;
  }

  /**
   * Returns the starting inline number.
   */
  public float getInlineStart() {
    load();
    return _inlineRange.getStart();
  }

  /**
   * Returns the ending inline number.
   */
  public float getInlineEnd() {
    load();
    return _inlineRange.getEnd();
  }

  /**
   * Returns the inline increment.
   */
  public float getInlineDelta() {
    load();
    return _inlineRange.getDelta();
  }

  /**
   * Returns the starting xline number.
   */
  public float getXlineStart() {
    load();
    return _xlineRange.getStart();
  }

  /**
   * Returns the ending xline number.
   */
  public float getXlineEnd() {
    load();
    return _xlineRange.getEnd();
  }

  /**
   * Returns the xline increment.
   */
  public float getXlineDelta() {
    load();
    return _xlineRange.getDelta();
  }

  /**
   * Returns the starting offset number.
   */
  public float getOffsetStart() {
    load();
    return _offsetRange.getStart();
  }

  /**
   * Returns the ending offset number.
   */
  public float getOffsetEnd() {
    load();
    return _offsetRange.getEnd();
  }

  /**
   * Returns the offset increment.
   */
  public float getOffsetDelta() {
    load();
    return _offsetRange.getDelta();
  }

  /**
   * Sets the inline range of the prestack dataset.
   * @param start the starting inline number.
   * @param end the ending inline number.
   * @param delta the inline increment.
   */
  public void setInlineRange(final float start, final float end, final float delta) {
    _inlineRange = new FloatRange(start, end, delta);
  }

  /**
   * Sets the xline range of the prestack dataset.
   * @param start the starting xline number.
   * @param end the ending xline number.
   * @param delta the xline increment.
   */
  public void setXlineRange(final float start, final float end, final float delta) {
    _xlineRange = new FloatRange(start, end, delta);
  }

  /**
   * Sets the offset range of the prestack dataset.
   * @param start the starting offset number.
   * @param end the ending offset number.
   * @param delta the offset increment.
   */
  public void setOffsetRange(final float start, final float end, final float delta) {
    _offsetRange = new FloatRange(start, end, delta);
  }

  /**
   * Returns the seismic geometry associated with the prestack dataset.
   */
  public SeismicSurvey3d getSurvey() {
    load();
    return _survey;
  }

  public void setSurvey(final SeismicSurvey3d survey) {
    _survey = survey;
    setDirty(true);
  }

  /**
   * Returns the storage order of the volume on disk.
   * This is used to determine the preferred order in which the volume should be accessed.
   * @param order the preferred order.
   */
  public StorageOrder getPreferredOrder() {
    load();
    if (_mapper != null) {
      return ((IPreStack3dMapper) _mapper).getStorageOrder(this);
    }
    return StorageOrder.INLINE_OFFSET_XLINE_Z;
  }

  /**
   * Returns the traces located at the specified inline,xline location.
   * Traces are read between the starting and ending offset values, and between the
   * starting and ending z value.
   * 
   * @param inline the inline number.
   * @param xline the xline number.
   * @param offsetStart the starting offset.
   * @param offsetEnd the ending offset.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data.
   */
  public TraceData getTracesByInlineXline(final float inline, final float xline, final float offsetStart,
      final float offsetEnd, final float zStart, final float zEnd) {
    validateInline(inline, true);
    validateXline(xline, true);
    validateZ(zStart);
    validateZ(zEnd);
    return ((IPreStack3dMapper) _mapper).getTracesByInlineXline(this, inline, xline, offsetStart, offsetEnd, zStart,
        zEnd);
  }

  /**
   * Returns the traces located at the specified inline,offset location.
   * Traces are read between the starting and ending xline values, and between the
   * starting and ending z value.
   * 
   * @param inline the inline number.
   * @param offset the offset number.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data.
   */
  public TraceData getTracesByInlineOffset(final float inline, final float offset, final float xlineStart,
      final float xlineEnd, final float zStart, final float zEnd) {
    validateInline(inline, true);
    validateOffset(offset, true);
    validateZ(zStart);
    validateZ(zEnd);
    return ((IPreStack3dMapper) _mapper).getTracesByInlineOffset(this, inline, offset, xlineStart, xlineEnd, zStart,
        zEnd);
  }

  /**
   * Returns the traces located at the specified xline,offset location.
   * Traces are read between the starting and ending inline values, and between the
   * starting and ending z value.
   * 
   * @param xline the xline number.
   * @param offset the offset number.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data.
   */
  public TraceData getTracesByXlineOffset(final float xline, final float offset, final float inlineStart,
      final float inlineEnd, final float zStart, final float zEnd) {
    validateXline(xline, true);
    validateOffset(offset, true);
    validateZ(zStart);
    validateZ(zEnd);
    return ((IPreStack3dMapper) _mapper).getTracesByXlineOffset(this, xline, offset, inlineStart, inlineEnd, zStart,
        zEnd);
  }

  /**
   * Returns the traces located at the specified inline,xline,offset locations.
   * Traces are read between the starting and ending z value.
   * 
   * @param inlines the array of inline numbers.
   * @param xlines the array of xline numbers.
   * @param offsets the array of offset numbers.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the trace data.
   */
  public TraceData getTraces(final float[] inlines, final float[] xlines, final float[] offsets, final float zStart,
      final float zEnd) {
    validateInlineXlineOffsetArrays(inlines, xlines, offsets, true);
    validateZ(zStart);
    validateZ(zEnd);
    return ((IPreStack3dMapper) _mapper).getTraces(this, inlines, xlines, offsets, zStart, zEnd);
  }

  /**
   * Puts traces into the prestack dataset.
   * The location at which to put each trace is obtained from the trace header, which must contain
   * vaalues for inline, xline and offset.
   * 
   * @param traceData the trace data.
   */
  public void putTraces(final TraceData traceData) {
    int numTraces = traceData.getNumTraces();
    for (int i = 0; i < numTraces; i++) {
      Trace trace = traceData.getTrace(i);
      validateInline(trace.getInline(), false);
      validateXline(trace.getXline(), false);
      // TODO: validate the offset
    }
    validateZ(traceData.getStartZ());
    validateZ(traceData.getEndZ());
    ((IPreStack3dMapper) _mapper).putTraces(this, traceData);
  }

  @Override
  public void close() {
    if (_mapper != null) {
      ((IPreStack3dMapper) _mapper).close();
    }
    _status = LoadStatus.GHOST;
  }

  @Override
  public void load() {
    super.load();
    calculateExtent();
  }

  /**
   * Validates the specified inline number.
   * The value is checked to make sure it is a multiple of the inline delta.
   * If <i>checkBounds</i> is <i>true<i>, it is also be checked to make sure
   * it falls within the start and end inline bounds.
   * 
   * @param inline the inline to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   */
  private void validateInline(final float inline, final boolean checkBounds) {
    validateRange("inline", inline, _survey.getInlineStart(), _survey.getInlineEnd(), _survey.getInlineDelta(),
        checkBounds);
  }

  /**
   * Validates the specified xline number.
   * The value is checked to make sure it is a multiple of the xline delta.
   * If <i>checkBounds</i> is <i>true<i>, it is also be checked to make sure
   * it falls within the start and end xline bounds.
   * 
   * @param xline the xline to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   */
  private void validateXline(final float xline, final boolean checkBounds) {
    validateRange("xline", xline, _survey.getXlineStart(), _survey.getXlineEnd(), _survey.getXlineDelta(), checkBounds);
  }

  /**
   * Validates the specified offset number.
   * The value is checked to make sure it is a multiple of the offset delta.
   * If <i>checkBounds</i> is <i>true<i>, it is also be checked to make sure
   * it falls within the start and end offset bounds.
   * 
   * @param offset the offset to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   */
  private void validateOffset(final float offset, final boolean checkBounds) {
    validateRange("offset", offset, _offsetRange.getStart(), _offsetRange.getEnd(), _offsetRange.getDelta(),
        checkBounds);
  }

  /**
   * Validates the specified inline, xline and offset arrays.
   * @param inlines the array of inline numbers.
   * @param xlines the array of xline numbers.
   * @param offsets the array of offset numbers.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   */
  private void validateInlineXlineOffsetArrays(final float[] inlines, final float[] xlines, final float[] offsets,
      final boolean checkBounds) {
    if (inlines == null) {
      throw new IllegalArgumentException("Invalid inline array: null.");
    }
    if (xlines == null) {
      throw new IllegalArgumentException("Invalid xline array: null");
    }
    if (offsets == null) {
      throw new IllegalArgumentException("Invalid offset array: null");
    }
    if (inlines.length != xlines.length) {
      throw new IllegalArgumentException("Invalid array length: # xlines does not equal # inlines.");
    }
    if (inlines.length != offsets.length) {
      throw new IllegalArgumentException("Invalid array length: # offsets does not equal # inlines.");
    }
    for (int i = 0; i < inlines.length; i++) {
      validateInline(inlines[i], checkBounds);
      validateXline(xlines[i], checkBounds);
      validateOffset(offsets[i], checkBounds);
    }
  }

  /**
   * Calculates the inline,xline,z extent of the volume.
   * This is a private method that is called internally after load().
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

    SeismicSurvey3d geometry = _survey;
    Point3d[] points = geometry.transformInlineXlineToXY(inlines, xlines).getCopyOfPoints();

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

    CoordinateSeries ps3dExtent = CoordinateSeries.createDirect(points, geometry.getCornerPoints()
        .getCoordinateSystem());

    setExtent(ps3dExtent);
    setActualExtent(ps3dExtent);
    setDirty(false);
  }
}
