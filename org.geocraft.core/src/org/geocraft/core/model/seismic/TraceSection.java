/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import org.geocraft.core.model.base.ValueObject;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.geometry.LineGeometry;


/**
 * This class defines a section of traces for display in the section viewer.
 * The section consists of a series of control points in x,y space that define the shape,
 * as well as starting and end z values.
 * 
 * TODO consider splitting this up into separate classes with an interface? 
 */
public class TraceSection extends ValueObject {

  //Display properties
  static public final String TYPE = "TraceSectionType";

  static public final String DOMAIN = "Domain";

  static public final String START_Z = "StartZ";

  static public final String END_Z = "EndZ";

  static public final String TRACE_AXIS_KEYS = "TraceAxisKeys";

  static public final String TRACE_AXIS_KEY_RANGES = "TraceAxisKeyRanges";

  static public final String IS_2D = "is2D";

  static public final String SEISMIC_LINE = "SeismicLine";

  /** Enumeration for the various section types. */
  public static enum SectionType {
    INLINE_SECTION("Inline Section"),
    XLINE_SECTION("Crossline Section"),
    OFFSET_SECTION("Offset Section"),
    INLINE_XLINE_GATHER("Inline-Xline Gather"),
    INLINE_OFFSET_GATHER("Inline-Offset Gather"),
    XLINE_OFFSET_GATHER("Xline-Offset Gather"),
    INLINE_XLINE_OFFSET_TRACE("Trace"),
    IRREGULAR("Irregular Section");

    private String _name;

    private SectionType(final String name) {
      _name = name;
    }

    public static SectionType fromString(final String name) {
      if (name != null) {
        for (SectionType type : SectionType.values()) {
          if (name.equals(type._name)) {
            return type;
          }
        }
      }
      throw new IllegalArgumentException("No SectionType \'" + name + "\'");
    }

    @Override
    public String toString() {
      return _name;
    }
  }

  /** The type of trace section (INLINE, XLINE, INLINE_XLINE_GATHER, etc). */
  private final SectionType _type;

  /** The array of trace axis keys used to define the traces in the section. */
  private final TraceAxisKey[] _traceAxisKeys;

  /** The array of trace axis key values that define the traces in the section. */
  private final float[][] _traceAxisKeyValues;

  private FloatRange[] _traceAxisKeyRanges;

  /** The number of trace locations in the section. */
  private int _numTraces;

  /** The domain of the section (TIME or DEPTH). */
  private final Domain _domain;

  /** The starting z of the section. */
  private float _startZ;

  /** The ending z of the section. */
  private float _endZ;

  /** The array of x,y points for the trace locations in the section. */
  private Point3d[] _xyPoints;

  private Point3d[] _controlPoints = new Point3d[0];

  private int _numPanels = 1;

  private int[] _panelIndices = new int[0];

  private boolean _is2D;

  private SeismicLine2d _seismicLine;

  /**
   * Constructs an irregular trace section from the specified trace axis keys and values, as well as the domain (TIME or DEPTH)
   * and the z range.

   * @param controlPoints the array of points representing the actual defined section.
   * @param tracePoints the array of points resampled to a geometry and shifted to trace locations.
   * @param panelIndices the array of panel index values.
   * @param traceAxisKeys the array of trace axis keys used to define the section.
   * @param traceAxisKeyRanges the array of trace axis key values used to define the section.
   * @param domain the domain of the section.
   * @param startZ the starting value of the z range.
   * @param endZ the ending value of the z range.
   */
  public TraceSection(final Point3d[] controlPoints, final Point3d[] tracePoints, final int[] panelIndices, final TraceAxisKey[] traceAxisKeys, final float[][] traceAxisKeyValues, final Domain domain, final float startZ, final float endZ) {
    super(SectionType.IRREGULAR.toString());

    _controlPoints = new Point3d[controlPoints.length];
    System.arraycopy(controlPoints, 0, _controlPoints, 0, controlPoints.length);

    _type = SectionType.IRREGULAR;

    _numTraces = tracePoints.length;

    _panelIndices = new int[_numTraces];
    System.arraycopy(panelIndices, 0, _panelIndices, 0, _numTraces);
    _numPanels = 1 + _panelIndices[_numTraces - 1];

    // Store the x,y coordinates.
    _xyPoints = new Point3d[_numTraces];
    for (int i = 0; i < _numTraces; i++) {
      System.arraycopy(tracePoints, 0, _xyPoints, 0, _numTraces);
    }

    // Store the trace axis keys and values.
    _traceAxisKeys = new TraceAxisKey[traceAxisKeys.length];
    System.arraycopy(traceAxisKeys, 0, _traceAxisKeys, 0, traceAxisKeys.length);
    _traceAxisKeyValues = new float[_numTraces][];
    for (int i = 0; i < _numTraces; i++) {
      _traceAxisKeyValues[i] = new float[traceAxisKeyValues[i].length];
      System.arraycopy(traceAxisKeyValues[i], 0, _traceAxisKeyValues[i], 0, traceAxisKeyValues[i].length);
    }

    // Store the z domain and range.
    _startZ = startZ;
    _endZ = endZ;
    _domain = domain;
  }

  /**
   * Constructs a trace section from the specified trace axis keys and values, as well as the domain (TIME or DEPTH)
   * and the z range. The x,y coordinates are computed from the specified 3D seismic geometry. The trace axis keys
   * must include INLINE and XLINE, and can optionally include OFFSET.
   * @param traceAxisKeys the array of trace axis keys used to define the section.
   * @param traceAxisKeyRanges the array of trace axis key values used to define the section.
   * @param domain the domain of the section.
   * @param startZ the starting value of the z range.
   * @param endZ the ending value of the z range.
   */
  public TraceSection(final SectionType type, final SeismicSurvey3d geometry, final TraceAxisKey[] traceAxisKeys, final FloatRange[] traceAxisKeyRanges, final Domain domain, final float startZ, final float endZ) {
    this(type, traceAxisKeys, traceAxisKeyRanges, domain, startZ, endZ);

    int inlineIndex = getTraceAxisKeyIndex(TraceAxisKey.INLINE);
    int xlineIndex = getTraceAxisKeyIndex(TraceAxisKey.XLINE);

    // Allocate storage for the x,y coordinates.
    _xyPoints = new Point3d[_numTraces];

    // Loop thru each of the traces, converting its inline,xline to x,y coordinates.
    for (int i = 0; i < _numTraces; i++) {
      float inline = _traceAxisKeyValues[i][inlineIndex];
      float xline = _traceAxisKeyValues[i][xlineIndex];
      double[] xy = geometry.transformInlineXlineToXY(inline, xline);

      // Store the x,y coordinates.
      _xyPoints[i] = new Point3d(xy[0], xy[1], 0);
    }

    _is2D = false;
  }

  /**
   * Constructs a trace section from the specified trace axis keys and values, as well as the domain (TIME or DEPTH)
   * and the z range. The x,y coordinates are computed from the specified 2D seismic geometry. The trace axis keys
   * must include CDP, and can optionally include INLINE and OFFSET.
   * @param traceAxisKeys the array of trace axis keys used to define the section.
   * @param traceAxisKeyRanges the array of trace axis key values used to define the section.
   * @param domain the domain of the section.
   * @param startZ the starting value of the z range.
   * @param endZ the ending value of the z range.
   */
  public TraceSection(final SectionType type, final SeismicLine2d seismicLine, final TraceAxisKey[] traceAxisKeys, final FloatRange[] traceAxisKeyRanges, final Domain domain, final float startZ, final float endZ) {
    this(type, traceAxisKeys, traceAxisKeyRanges, domain, startZ, endZ);

    int cdpIndex = getTraceAxisKeyIndex(TraceAxisKey.CDP);

    // Allocate storage for the x,y coordinates.
    _xyPoints = new Point3d[_numTraces];

    // Loop thru each of the traces, converting its cdp to x,y coordinates.
    for (int i = 0; i < _numTraces; i++) {
      float cdp = _traceAxisKeyValues[i][cdpIndex];
      double[] xy = seismicLine.transformCDPToXY(cdp);

      // Store the x,y coordinates.
      _xyPoints[i] = new Point3d(xy[0], xy[1], 0);
    }

    _is2D = true;
    _seismicLine = seismicLine;
  }

  /**
   * Constructs a trace section from the specified trace axis keys and values, as well as the domain (TIME or DEPTH)
   * and the z range. This is used to create a regularized section.
   * For example, if the trace axis keys are, in order, INLINE and XLINE, and their respective ranges are 100-105(1) and 201-205(1),
   * then the section will contain 100 traces, in the following order:
   * INLINE  XLINE
   *   100    201
   *   100    202
   *   100    203
   *   101    201
   *   101    202
   *   101    203
   *   102    201
   *   ...    ...
   *    
   * @param traceAxisKeys the array of trace axis keys used to define the section.
   * @param traceAxisKeyRanges the array of trace axis key ranges used to define the section.
   * @param domain the domain of the section.
   * @param startZ the starting value of the z range.
   * @param endZ the ending value of the z range.
   */
  private TraceSection(final SectionType type, final TraceAxisKey[] traceAxisKeys, final FloatRange[] traceAxisKeyRanges, final Domain domain, final float startZ, final float endZ) {

    // TODO can we do better than this? 
    super(type.toString());

    if (traceAxisKeys == null) {
      throw new IllegalArgumentException("The trace axis keys cannot be null.");
    }
    if (traceAxisKeyRanges == null) {
      throw new IllegalArgumentException("The trace axis key ranges cannot be null.");
    }
    if (traceAxisKeys.length != traceAxisKeyRanges.length) {
      throw new IllegalArgumentException("The # of trace axis keys must equal the # of trace axis key ranges.");
    }

    _traceAxisKeyRanges = new FloatRange[traceAxisKeys.length];
    System.arraycopy(traceAxisKeyRanges, 0, _traceAxisKeyRanges, 0, traceAxisKeys.length);

    _type = type;
    _traceAxisKeys = new TraceAxisKey[traceAxisKeys.length];
    System.arraycopy(traceAxisKeys, 0, _traceAxisKeys, 0, traceAxisKeys.length);
    _numTraces = 1;
    for (int i = 0; i < _traceAxisKeys.length; i++) {
      _numTraces *= traceAxisKeyRanges[i].getNumSteps();
    }
    _traceAxisKeyValues = new float[_numTraces][_traceAxisKeys.length];
    int[] indices = new int[_traceAxisKeys.length];
    for (int j = 0; j < _traceAxisKeys.length; j++) {
      indices[j] = 0;
    }

    for (int i = 0; i < _numTraces; i++) {
      for (int j = 0; j < _traceAxisKeys.length; j++) {
        _traceAxisKeyValues[i][j] = traceAxisKeyRanges[j].getStart() + indices[j] * traceAxisKeyRanges[j].getDelta();
      }
      int keyIndex = _traceAxisKeys.length - 1;
      boolean doneIncrementing = false;
      while (!doneIncrementing) {
        indices[keyIndex]++;
        if (indices[keyIndex] >= traceAxisKeyRanges[keyIndex].getNumSteps()) {
          indices[keyIndex] = 0;
          keyIndex--;
        } else {
          doneIncrementing = true;
        }
        if (keyIndex < 0) {
          doneIncrementing = true;
        }
      }
    }

    // Store the z domain and range.
    _domain = domain;
    _startZ = startZ;
    _endZ = endZ;
  }

  public SectionType getSectionType() {
    return _type;
  }

  /**
   * Returns the array of x,y coordinates for the trace positions in the section.
   */
  public Point3d[] getPointsXY() {
    return _xyPoints;
  }

  /**
   * Returns the number of traces in the section.
   */
  public int getNumTraces() {
    return _numTraces;
  }

  /**
   * Returns the domain of the section (TIME or DEPTH).
   */
  public Domain getDomain() {
    return _domain;
  }

  /**
   * Returns the starting z of the section.
   * The unit of the value is that of the application preferences
   * based on the domain in the control points.
   */
  public float getStartZ() {
    return _startZ;
  }

  /**
   * Returns the ending z of the section.
   * The unit of the value is that of the application preferences
   * based on the domain in the control points.
   */
  public float getEndZ() {
    return _endZ;
  }

  /**
   * Returns the index of the specified trace key in the section.
   * @param key the trace key.
   * @return the index of the trace key.
   */
  public int getTraceAxisKeyIndex(final TraceAxisKey key) {
    for (int i = 0; i < _traceAxisKeys.length; i++) {
      if (_traceAxisKeys[i].equals(key)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the array of trace axis keys that define the section.
   */
  public TraceAxisKey[] getTraceAxisKeys() {
    TraceAxisKey[] traceAxisKeys = new TraceAxisKey[_traceAxisKeys.length];
    System.arraycopy(_traceAxisKeys, 0, traceAxisKeys, 0, _traceAxisKeys.length);
    return traceAxisKeys;
  }

  /**
   * Returns the array of trace key values for the section (3D sections).
   * This is a two-dimensional array; the 1st dimension being the # of traces in the section,
   * and the 2nd dimension being the trace keys (INLINE, XLINE, or OFFSET).
   */
  public float[][] getTraceKeyValues3d(final SeismicSurvey3d geometry) {
    boolean containsOffset = containsTraceAxisKey(TraceAxisKey.OFFSET);

    int inlineIndex = getTraceAxisKeyIndex(TraceAxisKey.INLINE);
    int xlineIndex = getTraceAxisKeyIndex(TraceAxisKey.XLINE);
    float[][] traceAxisKeyValues = new float[_numTraces][_traceAxisKeys.length];
    for (int i = 0; i < _numTraces; i++) {

      // Convert inline,xline in specified geometry to x,y.
      double x = _xyPoints[i].getX();
      double y = _xyPoints[i].getY();

      // Convert x,y to inline,xline in requested geometry.
      float[] ixln = geometry.transformXYToInlineXline(x, y, true);
      traceAxisKeyValues[i][inlineIndex] = ixln[0];
      traceAxisKeyValues[i][xlineIndex] = ixln[1];
      if (containsOffset) {
        int offsetIndex = getTraceAxisKeyIndex(TraceAxisKey.OFFSET);
        traceAxisKeyValues[i][offsetIndex] = _traceAxisKeyValues[i][offsetIndex];
      }
    }
    return traceAxisKeyValues;
  }

  /**
   * Returns the array of trace key values for the section (3D sections).
   * This is a two-dimensional array; the 1st dimension being the # of traces in the section,
   * and the 2nd dimension being the trace keys (INLINE, XLINE, or OFFSET).
   */
  public float[][] getTraceKeyValues3d() {
    boolean containsOffset = containsTraceAxisKey(TraceAxisKey.OFFSET);

    int inlineIndex = getTraceAxisKeyIndex(TraceAxisKey.INLINE);
    int xlineIndex = getTraceAxisKeyIndex(TraceAxisKey.XLINE);
    float[][] traceAxisKeyValues = new float[_numTraces][_traceAxisKeys.length];
    for (int i = 0; i < _numTraces; i++) {
      traceAxisKeyValues[i][inlineIndex] = _traceAxisKeyValues[i][inlineIndex];
      traceAxisKeyValues[i][xlineIndex] = _traceAxisKeyValues[i][xlineIndex];
      if (containsOffset) {
        int offsetIndex = getTraceAxisKeyIndex(TraceAxisKey.OFFSET);
        traceAxisKeyValues[i][offsetIndex] = _traceAxisKeyValues[i][offsetIndex];
      }
    }
    return traceAxisKeyValues;
  }

  /**
   * Returns the array of trace key values for the section (2D sections).
   * This is a two-dimensional array; the 1st dimension being the # of traces in the section,
   * and the 2nd dimension being the trace keys (INLINE, XLINE, or OFFSET).
   */
  public float[][] getTraceKeyValues2d(final SeismicLine2d seismicLine) {
    int inlineIndex = getTraceAxisKeyIndex(TraceAxisKey.INLINE);
    int cdpIndex = getTraceAxisKeyIndex(TraceAxisKey.CDP);
    int offsetIndex = getTraceAxisKeyIndex(TraceAxisKey.OFFSET);
    float[][] traceAxisKeyValues = new float[_numTraces][_traceAxisKeys.length];
    for (int i = 0; i < _numTraces; i++) {
      float inline = _traceAxisKeyValues[i][inlineIndex];
      float cdp = _traceAxisKeyValues[i][cdpIndex];

      // TODO: Convert inline,cdp in specified geometry to x,y.
      // TODO: Convert x,y to inline,cdp in requested geometry.
      traceAxisKeyValues[i][inlineIndex] = inline;
      traceAxisKeyValues[i][cdpIndex] = cdp;
      if (offsetIndex != -1) {
        float offset = _traceAxisKeyValues[i][offsetIndex];
        traceAxisKeyValues[i][offsetIndex] = offset;
      }
    }
    return traceAxisKeyValues;
  }

  public float getTraceAxisKeyValue(final int traceIndex, final TraceAxisKey traceAxisKey) {
    return _traceAxisKeyValues[traceIndex][getTraceAxisKeyIndex(traceAxisKey)];
  }

  public FloatRange getTraceAxisKeyRanges(final TraceAxisKey traceAxisKey) {
    return _traceAxisKeyRanges[getTraceAxisKeyIndex(traceAxisKey)];
  }

  /**
   * Returns <i>true</i> if the trace section contains the given trace axis key (e.g. INLINE, OFFSET, etc).
   * If not, then <i>false</i> is returned.
   * @param traceAxisKey the trace axis key to check for.
   */
  public boolean containsTraceAxisKey(final TraceAxisKey traceAxisKey) {
    for (TraceAxisKey key : _traceAxisKeys) {
      if (key.equals(traceAxisKey)) {
        return true;
      }
    }
    return false;
  }

  public float[] getTraceAxisKeyValues(final TraceAxisKey traceAxisKey) {
    float[] values = new float[_numTraces];
    for (int i = 0; i < _numTraces; i++) {
      values[i] = getTraceAxisKeyValue(i, traceAxisKey);
    }
    return values;
  }

  public String getLabelText() {
    if (_is2D) {
      int numTraces = getNumTraces();
      //float inline0 = getTraceAxisKeyValue(0, TraceAxisKey.INLINE);
      //float inline1 = getTraceAxisKeyValue(numTraces - 1, TraceAxisKey.INLINE);
      float xline0 = 0;
      float xline1 = 0;
      String xlineLabel = "Xlines";
      if (containsTraceAxisKey(TraceAxisKey.CDP)) {
        xline0 = getTraceAxisKeyValue(0, TraceAxisKey.CDP);
        xline1 = getTraceAxisKeyValue(numTraces - 1, TraceAxisKey.CDP);
        xlineLabel = "CDPs";
      } else {
        xline0 = getTraceAxisKeyValue(0, TraceAxisKey.XLINE);
        xline1 = getTraceAxisKeyValue(numTraces - 1, TraceAxisKey.XLINE);
      }
      float offset0 = 0;
      float offset1 = 0;
      boolean containsOffset = containsTraceAxisKey(TraceAxisKey.OFFSET);
      if (containsOffset) {
        offset0 = getTraceAxisKeyValue(0, TraceAxisKey.OFFSET);
        offset1 = getTraceAxisKeyValue(numTraces - 1, TraceAxisKey.OFFSET);
      }
      StringBuilder builder = new StringBuilder("Line Section : ");
      builder.append("Line=" + _seismicLine.getDisplayName());
      builder.append(",");
      builder.append(xlineLabel + "=" + xline0 + "-" + xline1);
      if (containsOffset) {
        builder.append(",");
        builder.append("Offsets=" + offset0 + "-" + offset1);
      }
      return builder.toString();
    }
    int numTraces = getNumTraces();
    float inline0 = getTraceAxisKeyValue(0, TraceAxisKey.INLINE);
    float inline1 = getTraceAxisKeyValue(numTraces - 1, TraceAxisKey.INLINE);
    float xline0 = 0;
    float xline1 = 0;
    String xlineLabel = "Xlines";
    if (containsTraceAxisKey(TraceAxisKey.CDP)) {
      xline0 = getTraceAxisKeyValue(0, TraceAxisKey.CDP);
      xline1 = getTraceAxisKeyValue(numTraces - 1, TraceAxisKey.CDP);
      xlineLabel = "CDPs";
    } else {
      xline0 = getTraceAxisKeyValue(0, TraceAxisKey.XLINE);
      xline1 = getTraceAxisKeyValue(numTraces - 1, TraceAxisKey.XLINE);
    }
    float offset0 = 0;
    float offset1 = 0;
    boolean containsOffset = containsTraceAxisKey(TraceAxisKey.OFFSET);
    if (containsOffset) {
      offset0 = getTraceAxisKeyValue(0, TraceAxisKey.OFFSET);
      offset1 = getTraceAxisKeyValue(numTraces - 1, TraceAxisKey.OFFSET);
    }
    StringBuilder builder = new StringBuilder(_type.toString() + " : ");
    switch (_type) {
      case INLINE_SECTION:
        builder.append("Inline=" + inline0);
        builder.append(",");
        builder.append(xlineLabel + "=" + xline0 + "-" + xline1);
        if (containsOffset) {
          builder.append(",");
          builder.append("Offsets=" + offset0 + "-" + offset1);
        }
        break;
      case XLINE_SECTION:
        builder.append("Xline=" + xline0);
        builder.append(",");
        builder.append("Inlines=" + inline0 + "-" + inline1);
        if (containsOffset) {
          builder.append(",");
          builder.append("Offsets=" + offset0 + "-" + offset1);
        }
        break;
      case OFFSET_SECTION:
        builder.append("Offset=" + offset0);
        builder.append(",");
        builder.append("Inlines=" + inline0 + "-" + inline1);
        builder.append(",");
        builder.append("Xlines=" + xline0 + "-" + xline1);
        break;
      case INLINE_XLINE_GATHER:
        builder.append("Inline=" + inline0);
        builder.append(",");
        builder.append("Xline=" + xline0);
        builder.append(",");
        builder.append("Offsets=" + offset0 + "-" + offset1);
        break;
      case INLINE_OFFSET_GATHER:
        builder.append("Inline=" + inline0);
        builder.append(",");
        builder.append("Offset=" + offset0);
        builder.append(",");
        builder.append("Xlines=" + xline0 + "-" + xline1);
        break;
      case XLINE_OFFSET_GATHER:
        builder.append("Xline=" + xline0);
        builder.append(",");
        builder.append("Offset=" + offset0);
        builder.append(",");
        builder.append("Inlines=" + inline0 + "-" + inline1);
        break;
      case INLINE_XLINE_OFFSET_TRACE:
        builder.append("Inline=" + inline0);
        builder.append(",");
        builder.append(xlineLabel + "=" + xline0);
        builder.append(",");
        builder.append("Offset=" + offset0);
        break;
      case IRREGULAR:
        break;
    }
    return builder.toString();
  }

  public boolean hasControlPoints() {
    return _controlPoints.length > 0;
  }

  public int getNumControlPoints() {
    return _controlPoints.length;
  }

  public Point3d[] getControlPoints() {
    Point3d[] controlPoints = new Point3d[_controlPoints.length];
    System.arraycopy(_controlPoints, 0, controlPoints, 0, controlPoints.length);
    return controlPoints;
  }

  public int getNumPanels() {
    return _numPanels;
  }

  public int[] getPanelIndices() {
    int[] panelIndices = new int[_panelIndices.length];
    System.arraycopy(_panelIndices, 0, panelIndices, 0, panelIndices.length);
    return panelIndices;
  }

  public void setZStartAndEnd(final float zStart, final float zEnd) {
    _startZ = zStart;
    _endZ = zEnd;
  }

  public boolean is2D() {
    return _is2D;
  }

  public LineGeometry getLineGeometry() {
    return _seismicLine;
  }

  @Override
  public String toString() {
    final String SEPARATOR = ";";
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(TYPE + " " + _type + SEPARATOR);
    sbuf.append(DOMAIN + " " + _domain + SEPARATOR);
    sbuf.append(START_Z + " " + _startZ + SEPARATOR);
    sbuf.append(END_Z + " " + _endZ + SEPARATOR);
    sbuf.append(IS_2D + " " + (_is2D ? "true" : "false") + SEPARATOR);
    sbuf.append(SEISMIC_LINE + " " + _seismicLine + SEPARATOR); //display name

    sbuf.append(TRACE_AXIS_KEYS + " " + "[");
    String keys = "";
    int n = _traceAxisKeys.length;
    for (int i = 0; i < n; i++) {
      keys += _traceAxisKeys[i];
      if (i != n - 1) {
        keys += ",";
      }
    }
    sbuf.append(keys + "]" + SEPARATOR);

    if (_traceAxisKeyRanges != null) {
      sbuf.append(TRACE_AXIS_KEY_RANGES + " " + "[");
      String ranges = "";
      n = _traceAxisKeyRanges.length;
      for (int i = 0; i < n; i++) {
        ranges += _traceAxisKeyRanges[i];
        if (i != n - 1) {
          ranges += ":";
        }
      }
      sbuf.append(ranges + "]" + SEPARATOR);
    }

    return sbuf.toString();
  }
}
