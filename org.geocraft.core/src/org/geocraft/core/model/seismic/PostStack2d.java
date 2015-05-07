/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocraft.core.common.util.Sorting;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.mapper.IMapper;


/**
 * This class defines a stacked 2D pseudo-volume, which represents a composite of 2D seismic datasets
 * defined on one or more lines in a 2D seismic survey.
 */
public class PostStack2d extends Entity {

  /** The 2D seismic survey on which the 2D seismic datasets are defined. */
  private final SeismicSurvey2d _seismicSurvey;

  /** The list of line numbers on which the 2D seismic datasets are defined. */
  private final List<Integer> _lineNumbers;

  /** The collection of 2D seismic datasets, mapped by the numbers of the seismic lines on which they are defined. */
  private final Map<Integer, PostStack2dLine> _poststackMap;

  /** The z domain of the 2D seismic datasets. */
  private final Domain _zDomain;

  /**
   * Constructs a lazy-loaded <code>PostStack2d</code> entity.
   * 
   * @param name the name of the 2D composite volume.
   * @param mapper the underlying mapper to use for datastore access.
   * @param seismicSurvey the 2D seismic survey on which the seismic datasets are defined.
   * @param zDomain the z domain (e.g. TIME or DEPTH) of the seismic datasets.
   */
  public PostStack2d(final String displayName, final IMapper mapper, final SeismicSurvey2d seismicSurvey, final Domain zDomain) {
    super(displayName, mapper);
    _seismicSurvey = seismicSurvey;
    _zDomain = zDomain;

    // Initialize the line number list.
    _lineNumbers = Collections.synchronizedList(new ArrayList<Integer>());

    // Initialize the collection of poststack lines.
    _poststackMap = Collections.synchronizedMap(new HashMap<Integer, PostStack2dLine>());
  }

  /**
   * Returns the 2D seismic survey on which the dataset is defined.
   * 
   * @return the 2D seismic survey.
   */
  public SeismicSurvey2d getSurvey() {
    //This does not trigger a load, since the seismic survey is
    //specified in the constructor and cannot be changed.
    return _seismicSurvey;
  }

  /**
   * Returns the z domain (e.g. TIME or DEPTH) of the 2D seismic datasets.
   * 
   * @return the z domain.
   */
  public Domain getZDomain() {
    // This does not trigger a load, since the seismic survey is
    // specified in the constructor and cannot be changed.
    return _zDomain;
  }

  /**
   * Returns the list of line numbers on which 2D seismic datasets are defined.
   * 
   * @return the list of line numbers.
   */
  public int[] getLineNumbers() {
    load();
    int[] lineNumbers = new int[_lineNumbers.size()];
    for (int i = 0; i < lineNumbers.length; i++) {
      lineNumbers[i] = _lineNumbers.get(i).intValue();
    }
    return lineNumbers;
  }

  /**
   * Returns an array of the line names on which 2D seismic datasets are defined.
   * <p>
   * The returned array can be optionally sorted by name using an alpha-numeric comparator.
   * 
   * @param sortByName <i>true</i> to sort the returned list; otherwise <i>false</i>
   * @return the array of line names.
   */
  public String[] getLineNames(final boolean sortByName) {
    load();
    int[] lineNumbers = getLineNumbers();
    List<String> lineNames = Collections.synchronizedList(new ArrayList<String>());
    for (int lineNumber : lineNumbers) {
      lineNames.add(_seismicSurvey.getLineByNumber(lineNumber).getDisplayName());
    }
    if (sortByName) {
      Collections.sort(lineNames, Sorting.ALPHANUMERIC_COMPARATOR);
    }
    return lineNames.toArray(new String[0]);
  }

  /**
   * Adds a 2D seismic dataset to the volume for the given line.
   * <p>
   * If one already exists for the given line, then it is replaced with the new one.
   * 
   * @param lineNumber the seismic line number.
   * @param poststackLine the 2D seismic dataset to add.
   */
  public void addPostStack2dLine(final int lineNumber, final PostStack2dLine poststackLine) {
    // Add an entry to the line number list only if it does not already exist.
    if (!_lineNumbers.contains(lineNumber)) {
      _lineNumbers.add(lineNumber);
    }
    // Put the entry in the entity map, replacing any existing ones.
    _poststackMap.put(lineNumber, poststackLine);
  }

  /**
   * Returns an array of the 2D seismic datasets contained in the volume.
   * <p>
   * The returned array can be optionally sorted by line name using an alpha-numeric comparator.
   * 
   * @param sortByName <i>true</i> to sort the returned list; otherwise <i>false</i>
   * @return the array of 2D seismic datasets.
   */
  public PostStack2dLine[] getPostStack2dLines(final boolean sortByName) {
    load();
    String[] lineNames = getLineNames(sortByName);
    PostStack2dLine[] poststacks = new PostStack2dLine[lineNames.length];
    for (int i = 0; i < lineNames.length; i++) {
      poststacks[i] = getPostStack2dLine(lineNames[i]);
    }
    return poststacks;
  }

  /**
   * Gets the 2D seismic dataset defined on the given seismic line.
   * 
   * @param lineNumber the seismic line number.
   * @return the 2D dataset for the line.
   * @throws IllegalArgumentException if the line number is invalid or no 2D seismic dataset exists for the line.
   */
  public PostStack2dLine getPostStack2dLine(final int lineNumber) {
    load();

    // Validate the line number exists in the survey.
    getSurvey().getLineByNumber(lineNumber);

    // Validate the line number exists in the volume.
    if (!containsPostStack2d(lineNumber)) {
      throw new IllegalArgumentException("Invalid line #: " + lineNumber);
    }

    return _poststackMap.get(lineNumber);
  }

  /**
   * Gets the 2D seismic dataset defined on the given seismic line.
   * 
   * @param lineName the seismic line name.
   * @return the 2D dataset for the line; or <i>null</i> if none exists.
   * @throws IllegalArgumentException if the line name is invalid or no 2D seismic dataset exists for the line.
   */
  public PostStack2dLine getPostStack2dLine(final String lineName) {
    load();
    SeismicLine2d seismicLine = getSurvey().getLineByName(lineName);
    return getPostStack2dLine(seismicLine.getNumber());
  }

  /**
   * Returns the number of lines in the volume.
   * <p>
   * This represents the number of 2D seismic datasets in the volume.
   * 
   * @return the number of lines in the volume.
   */
  public int getNumLines() {
    load();
    return _lineNumbers.size();
  }

  /**
   * Returns the number of CDPs (i.e. # of traces) in the volume for the given line.
   * 
   * @param lineNumber the seismic line number.
   * @return the number of CDPs.
   * @throws IllegalArgumentException if the line number is invalid or no 2D seismic dataset exists for the line.
   */
  public int getNumCdps(final int lineNumber) {
    load();
    return getPostStack2dLine(lineNumber).getNumCdps();
  }

  /**
   * Returns the CDP range (start,end,delta) in the volume for the given line.
   * 
   * @param lineNumber the seismic line number.
   * @return the CDP range.
   * @throws IllegalArgumentException if the line number is invalid or no 2D seismic dataset exists for the line.
   */
  public FloatRange getCdpRange(final int lineNumber) {
    load();
    return getPostStack2dLine(lineNumber).getCdpRange();
  }

  /**
   * Returns the shotpoint start value in the volume for the given line.
   * 
   * @param lineNumber the seismic line number.
   * @return the shotpoint range.
   * @throws IllegalArgumentException if the line number is invalid or no 2D seismic dataset exists for the line.
   */
  public float getShotpointStart(final int lineNumber) {
    load();
    return getPostStack2dLine(lineNumber).getShotpointStart();
  }

  /**
   * Returns the shotpoint end value in the volume for the given line.
   * 
   * @param lineNumber the seismic line number.
   * @return the shotpoint range.
   * @throws IllegalArgumentException if the line number is invalid or no 2D seismic dataset exists for the line.
   */
  public float getShotpointEnd(final int lineNumber) {
    load();
    return getPostStack2dLine(lineNumber).getShotpointEnd();
  }

  /**
   * Returns the z range (start,end,delta) in the volume for the given line.
   * 
   * @param lineNumber the seismic line number.
   * @return the z range.
   * @throws IllegalArgumentException if the line number is invalid or no 2D seismic dataset exists for the line.
   */
  public FloatRange getZRange(final int lineNumber) {
    load();
    return getPostStack2dLine(lineNumber).getZRange();
  }

  /**
   * Returns a flag indicating if the volume contains a 2D seismic dataset defined on the given seismic line.
   * 
   * @param lineNumber the number of the seismic line to check.
   * @return <i>true</i> if it is defined on the line; <i>false</i> if not.
   */
  public boolean containsPostStack2d(final int lineNumber) {
    load();
    if (_lineNumbers.contains(lineNumber) && _poststackMap.containsKey(lineNumber)) {
      return true;
    }
    return false;
  }

  /**
   * Returns a flag indicating if the volume contains a 2D seismic dataset defined on the given seismic line.
   * 
   * @param lineName the name of the seismic line to check.
   * @return <i>true</i> if it is defined on the line; <i>false</i> if not.
   */
  public boolean containsPostStack2d(final String lineName) {
    load();
    SeismicLine2d seismicLine = getSurvey().getLineByName(lineName);
    if (seismicLine == null) {
      return false;
    }
    return containsPostStack2d(seismicLine.getNumber());
  }

  /**
   * Gets the specified traces from the 2D seismic dataset defined on the given line..
   * <p>
   * The traces will be read from the underlying datastore.
   * Any requested traces that fall outside the bounds of the dataset will be returned and flagged as missing.
   * 
   * @param the seismic line number.
   * @param cdps the array of CDP coordinates.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the array of traces.
   * @throws IllegalArgumentException if the line number is invalid or no 2D seismic dataset exists for the line.
   */
  public TraceData getTraces(final int lineNumber, final float[] cdps, final float zStart, final float zEnd) {
    load();
    return getPostStack2dLine(lineNumber).getTraces(cdps, zStart, zEnd);
  }

  /**
   * Puts the specified traces to the 2D seismic dataset defined on the given line.
   * <p>
   * The traces will be written to the underlying datastore.
   * Any traces that fall outside the bounds of the dataset or flagged as missing will be ignored.
   * 
   * @param lineNumber the seismic line number.
   * @param traceData the trace data object containing the traces to write.
   * @throws IllegalArgumentException if the line number is invalid or no 2D seismic dataset exists for the line.
   */
  public void putTraces(final int lineNumber, final TraceData traceData) {
    getPostStack2dLine(lineNumber).putTraces(traceData);
  }

  /**
   * Closes all the 2D seismic datasets from I/O operations.
   */
  public void close() {
    for (PostStack2dLine poststack : getPostStack2dLines(false)) {
      poststack.close();
    }
    setDirty(false);
  }
}
