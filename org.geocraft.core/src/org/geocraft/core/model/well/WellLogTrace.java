/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.well;


import java.sql.Timestamp;

import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


public class WellLogTrace extends Entity {

  /** The well that contains this log. */
  private final Well _well;

  /** The date the log was recorded. */
  private Timestamp _creationDate;

  /** The process or function used to create the log */
  private String _creationRoutine;

  /** The company or person that recorded the log. */
  private String _dataSource;

  /** Pass of the log run. */
  private String _logPass;

  /** Log run number. */
  private int _runNumber;

  /** Name of the logging service. */
  private String _serviceName;

  /** The name of the software used to create the log trace. */
  private String _softwareSource;

  /** The mnemonic for the log trace kind (e.g. GR, RHOB). */
  private String _traceMnemonic;

  /** A user identifiable name for the log trace. */
  private String _traceName;

  /** Indicates what level of processing has been performed on the log trace. */
  private String _traceProcess;

  /** The data type of the trace values. */
  private String _traceType;

  /** Indicates the amount of processing performed for the trace by version number. */
  private int _traceVersion;

  /** The id of the user that created the log. */
  private String _creationUserId;

  /** The id of the user that updated the log. */
  private String _updateUserId;

  /** The unit of measurement for the log trace data. */
  private Unit _dataUnit;

  /** The domain of z values in the log (MD, TVD, TWT, etc). */
  private WellDomain _zDomain;

  /** The z values (can be MD, TVD, TWT, etc). These are stored as doubles to preserve accuracy and match OW R5K */
  private double[] _zValues;

  // The top z value
  private double _zTop;

  // The base z value
  private double _zBase;

  /**
   * The log sample values. The length of the returned trace data array is # of samples * # of elements.
   * If the log is a multi-valued log (i.e. # of elements is greater than one), the first # of samples
   * values are the values for the first trace, the second # of samples values are the values for the
   * second trace, etc.
   * 
   * OpenWorks R5K can store traces of different types of data - may need to expand in future to handle them.
   */
  private float[] _traceData;

  /** The minimum non-null value in the log trace data. */
  private float _minValue;

  /** The maximum non-null value in the log trace data. */
  private float _maxValue;

  /** The value representing a null is the log trace data. */
  private float _nullValue;

  /**
   * Constructs an in-memory well log trace.
   * 
   * @param name the name of the well log trace.
   * @param wellBore the well bore to which the log trace is associated.
   */
  public WellLogTrace(final String name, final Well well) {
    this(name, new InMemoryMapper(WellLogTrace.class), well);
  }

  /**
   * Parameterized constructor.
   */
  public WellLogTrace(final String name, final IMapper mapper, final Well well) {
    super(name, mapper);
    _well = well;
    _well.addWellLogTrace(this);
    _traceData = new float[0];
    _zValues = new double[0];

    // Set the Z top and Z base values
    int basePntr = _zValues.length - 1;
    if (basePntr >= 0) {
      _zTop = _zValues[0];
      _zBase = _zValues[basePntr];
    }
  }

  /**
   * Returns the well with which this log is associated.
   */
  public Well getWell() {
    return _well;
  }

  /**
   * Returns the well top with which this log is associated.
   */
  public double getZTop() {
    load();
    return _zTop;
  }

  /**
   * Returns the well top with which this log is associated.
   */
  public double getZBase() {
    load();
    return _zBase;
  }

  /**
   * Returns the date the log was recorded.
   */
  public Timestamp getCreationDate() {
    load();
    return _creationDate;
  }

  /**
   * Returns the process or function that created the log.
   */
  public String getCreationRoutine() {
    load();
    return _creationRoutine;
  }

  /**
    * Returns the company or person that recorded the log.
    */
  public String getDataSource() {
    load();
    return _dataSource;
  }

  /**
   * Returns the domain of index values in the log (e.g. measured-depth, true-vertical-depth, etc).
   */
  public WellDomain getDomain() {
    load();
    return _zDomain;
  }

  /**
   * Returns the pass of the log run.
   */
  public String getLogPass() {
    load();
    return _logPass;
  }

  /**
   * Returns the largest value in the trace data.
   */
  public float getMaxValue() {
    load();
    return _maxValue;
  }

  /**
   * Returns the unit of measurement for the log data.
   */
  public Unit getDataUnit() {
    load();
    return _dataUnit;
  }

  /**
   * Returns the minimum non-null value in the log trace data.
   */
  public float getMinValue() {
    load();
    return _minValue;
  }

  /**
   * Returns the maximum non-null value in the log trace data.
   */
  public float getNullValue() {
    load();
    return _nullValue;
  }

  /**
   * Sets the null value of the log trace and recomputes the data
   * range taking into account the new null value. 
   * 
   * @param nullValue the null value to set.
   */
  public void setNullValue(final float nullValue) {
    _nullValue = nullValue;
    if (_traceData != null) {
      computeMinMax();
    }
    setDirty(true);
  }

  /**
   * Returns a flag indicating if there a null data point at the given index.
   * 
   * @param index the index to check.
   * @return <i>true</i> if the data is null; <i>false</i> if not.
   */
  public boolean isNull(final int index) {
    load();
    return isNull(_traceData[index]);
  }

  /**
   * Return a flag indicating if the given value is the same as the null value.
   *
   * @param value to be tested.
   * @return <i>true</i> if the value is the same as the null value; <i>false</i> if not.
   */
  public boolean isNull(final float value) {
    load();
    return MathUtil.isEqual(value, getNullValue());
  }

  /**
   * The multiplicity of trace values per index value. 
   * @return numElements
   */
  // TODO the current tracedata is only 1d but we will want to support gathers etc
  // eventually. Make this method private to hide it from Properties display and 
  // stop people using it. It was called by OS Factory but we can't handle it yet. 
  private int getNumElements() {
    load();
    return 1;
  }

  /**
   * Returns the number of sample values in the trace data.
   */
  // TODO this will need to change when we support gathers. 
  public int getNumSamples() {
    load();
    return _traceData.length;
  }

  /**
   * Returns the log run number.
   */
  public int getRunNumber() {
    load();
    return _runNumber;
  }

  /**
   * Returns the name of the logging service.
   */
  public String getServiceName() {
    load();
    return _serviceName;
  }

  /**
   * Returns the name of the software used to create the log trace.
   */
  public String getSoftwareSource() {
    load();
    return _softwareSource;
  }

  /**
   * Returns the mnemonic for the log trace (e.g. GR, RHOB).
   */
  public String getTraceMnemonic() {
    load();
    return _traceMnemonic;
  }

  /**
   * Returns the user identifiable name for the log trace.
   */
  public String getTraceName() {
    load();
    return _traceName;
  }

  /**
   * Returns the level of processing has been performed on the log trace.
   */
  public String getTraceProcess() {
    load();
    return _traceProcess;
  }

  /**
   * Returns the data type of the trace values.
   */
  public String getTraceType() {
    load();
    return _traceType;
  }

  /**
   * Returns the amount of processing performed for the trace by version number.
   */
  public int getTraceVersion() {
    load();
    return _traceVersion;
  }

  /**
   * Returns the id of the user that created the log.
   */
  public String getCreationUserID() {
    load();
    return _creationUserId;
  }

  /**
   * Returns the id of the user that updated the log.
   */
  public String getUpdateUserID() {
    load();
    return _updateUserId;
  }

  /**
   * Gets the z values for a given domain.
   * The necessary conversion will be done automatically.
   * 
   * @param domain the domain of the z values (MD, TVD, TWT, etc).
   * @return the z values.
   */
  public double[] getZValues(final WellDomain domain) {
    switch (domain) {
      case ONE_WAY_TIME:
        return getOneWayTimes();
      case TWO_WAY_TIME:
        return getTwoWayTimes();
      case MEASURED_DEPTH:
        return getMeasuredDepths();
      case TRUE_VERTICAL_DEPTH:
        return getTrueVerticalDepths();
      case TRUE_VERTICAL_DEPTH_SUBSEA:
        return getTrueVerticalDepthsSubsea();
      default:
        throw new IllegalArgumentException("Invalid domain: " + domain);
    }
  }

  /**
   * Returns the one-way-time (TWT) values.
   */
  public double[] getOneWayTimes() {
    load();

    double[] OWTs = new double[0];
    switch (_zDomain) {
      case ONE_WAY_TIME:
        // If the log trace domain is OWT, then simply copy the z values.
        OWTs = Utilities.copyDoubleArray(_zValues);
        break;
      case TWO_WAY_TIME:
      case MEASURED_DEPTH:
      case TRUE_VERTICAL_DEPTH:
      case TRUE_VERTICAL_DEPTH_SUBSEA:
        // Calculate the TWT values and then divide by 2.
        OWTs = getTwoWayTimes();
        for (int i = 0; i < OWTs.length; i++) {
          OWTs[i] /= 2;
        }
        break;
      default:
        throw new RuntimeException("Invalid domain: " + _zDomain);
    }
    return OWTs;
  }

  /**
   * Returns the two-way-time (TWT) values.
   */
  public double[] getTwoWayTimes() {
    load();

    WellBore wellBore = _well.getWellBore();
    double[] TWTs = new double[0];
    switch (_zDomain) {
      case ONE_WAY_TIME:
        // If the log trace domain is OWT, then simply copy the z values and multiply by 2.
        TWTs = Utilities.copyDoubleArray(_zValues);
        for (int i = 0; i < TWTs.length; i++) {
          TWTs[i] *= 2;
        }
        break;
      case TWO_WAY_TIME:
        // If the log trace domain is TWT, then simply copy the z values.
        TWTs = Utilities.copyDoubleArray(_zValues);
        break;
      case MEASURED_DEPTH:
      case TRUE_VERTICAL_DEPTH:
      case TRUE_VERTICAL_DEPTH_SUBSEA:
        // Calculate the TWT array by converting from MD.
        float[] boreTWTs = wellBore.getZValues(WellDomain.TWO_WAY_TIME);
        float[] boreMDs = wellBore.getZValues(WellDomain.MEASURED_DEPTH);
        double[] MDs = getMeasuredDepths();

        // If there is insufficient information, then return a zero-length array.
        if (boreTWTs.length == 0 || boreMDs.length == 0 || MDs.length == 0) {
          return new double[0];
        }

        TWTs = new double[MDs.length];

        int j = 0;
        for (int i = 0; i < TWTs.length; i++) {
          for (; j < boreMDs.length; j++) {
            if (boreMDs[j] >= MDs[i]) {
              break;
            }
          }

          double prevMD;
          double currentMD;
          double prevTWT;
          double currentTWT;
          if (j >= boreMDs.length) {
            if (boreMDs.length < 2) {
              TWTs[i] = 0;
              continue;
            }
            prevTWT = boreTWTs[boreTWTs.length - 2];
            currentTWT = boreTWTs[boreTWTs.length - 1];
            prevMD = boreMDs[boreMDs.length - 2];
            currentMD = boreMDs[boreMDs.length - 1];
          } else if (j < 1) {
            TWTs[i] = boreTWTs[0];
            continue;
          } else {
            prevMD = boreMDs[j - 1];
            currentMD = boreMDs[j];
            prevTWT = boreTWTs[j - 1];
            currentTWT = boreTWTs[j];
          }
          TWTs[i] = ((MDs[i] - prevMD) / (currentMD - prevMD) * (currentTWT - prevTWT) + prevTWT);
        }
        break;
      default:
        throw new RuntimeException("Invalid domain: " + _zDomain);
    }
    return TWTs;
  }

  /**
   * Returns the measured depth (MD) values.
   */
  public double[] getMeasuredDepths() {
    load();

    float[] boreMDs;

    WellBore wellBore = _well.getWellBore();
    double[] MDs = new double[0];
    switch (_zDomain) {
      case ONE_WAY_TIME:
      case TWO_WAY_TIME:
        // Calculate the MD array by converting from TWT.
        // Calculate the TVD array by converting from TVD.
        boreMDs = wellBore.getZValues(WellDomain.MEASURED_DEPTH);
        float[] boreTWTs = wellBore.getZValues(WellDomain.TWO_WAY_TIME);
        double[] TWTs = getTwoWayTimes();

        // If there is insufficient information, then return a zero-length array.
        if (boreMDs.length == 0 || boreTWTs.length == 0 || TWTs.length == 0) {
          return new double[0];
        }

        MDs = new double[TWTs.length];

        int j = 0;
        for (int i = 0; i < MDs.length; i++) {
          for (; j < boreTWTs.length; j++) {
            if (boreTWTs[j] >= TWTs[i]) {
              break;
            }
          }

          double prevTWT;
          double currentTWT;
          double prevMD;
          double currentMD;
          if (j >= boreTWTs.length) {
            if (boreTWTs.length < 2) {
              MDs[i] = 0;
              continue;
            }
            prevMD = boreMDs[boreMDs.length - 2];
            currentMD = boreMDs[boreMDs.length - 1];
            prevTWT = boreTWTs[boreTWTs.length - 2];
            currentTWT = boreTWTs[boreTWTs.length - 1];
          } else if (j < 1) {
            MDs[i] = boreMDs[0];
            continue;
          } else {
            prevTWT = boreTWTs[j - 1];
            currentTWT = boreTWTs[j];
            prevMD = boreMDs[j - 1];
            currentMD = boreMDs[j];
          }
          MDs[i] = (TWTs[i] - prevTWT) / (currentTWT - prevTWT) * (currentMD - prevMD) + prevMD;
        }
        break;
      case MEASURED_DEPTH:
        // If the log trace domain is MD, then simply copy the z values.
        MDs = Utilities.copyDoubleArray(_zValues);
        break;
      case TRUE_VERTICAL_DEPTH:
      case TRUE_VERTICAL_DEPTH_SUBSEA:
        // Calculate the TVD array by converting from TVD.
        boreMDs = wellBore.getZValues(WellDomain.MEASURED_DEPTH);
        float[] boreTVDs = wellBore.getZValues(WellDomain.TRUE_VERTICAL_DEPTH);
        double[] TVDs = getTrueVerticalDepths();
        MDs = new double[TVDs.length];

        // If there is insufficient information, then return a zero-length array.
        if (boreMDs.length == 0 || boreTVDs.length == 0 || TVDs.length == 0) {
          return new double[0];
        }

        j = 0;
        for (int i = 0; i < MDs.length; i++) {
          for (; j < boreTVDs.length; j++) {
            if (boreTVDs[j] >= TVDs[i]) {
              break;
            }
          }

          double prevTVD;
          double currentTVD;
          double prevMD;
          double currentMD;
          if (j >= boreTVDs.length) {
            if (boreTVDs.length < 2) {
              MDs[i] = 0;
              continue;
            }
            prevMD = boreMDs[boreMDs.length - 2];
            currentMD = boreMDs[boreMDs.length - 1];
            prevTVD = boreTVDs[boreTVDs.length - 2];
            currentTVD = boreTVDs[boreTVDs.length - 1];
          } else if (j < 1) {
            MDs[i] = boreMDs[0];
            continue;
          } else {
            prevTVD = boreTVDs[j - 1];
            currentTVD = boreTVDs[j];
            prevMD = boreMDs[j - 1];
            currentMD = boreMDs[j];
          }
          MDs[i] = (TVDs[i] - prevTVD) / (currentTVD - prevTVD) * (currentMD - prevMD) + prevMD;
        }
        break;
      default:
        throw new RuntimeException("Invalid domain: " + _zDomain);
    }
    return MDs;
  }

  /**
   * Returns the true-vertical-depth (TVD) trace index values.
   */
  public double[] getTrueVerticalDepths() {
    load();

    WellBore wellBore = _well.getWellBore();
    double[] TVDs = new double[0];
    switch (_zDomain) {
      case ONE_WAY_TIME:
      case TWO_WAY_TIME:
      case MEASURED_DEPTH:
        // Calculate the TVD array by converting from MD.
        float[] boreTVDs = wellBore.getZValues(WellDomain.TRUE_VERTICAL_DEPTH);
        float[] boreMDs = wellBore.getZValues(WellDomain.MEASURED_DEPTH);
        double[] MDs = getMeasuredDepths();

        // If there is insufficient information, then return a zero-length array.
        if (boreTVDs.length == 0 || boreMDs.length == 0 || MDs.length == 0) {
          return new double[0];
        }

        TVDs = new double[MDs.length];

        int j = 0;
        for (int i = 0; i < TVDs.length; i++) {
          for (; j < boreMDs.length; j++) {
            if (boreMDs[j] >= MDs[i]) {
              break;
            }
          }

          double prevMD;
          double currentMD;
          double prevTVD;
          double currentTVD;
          if (j >= boreMDs.length) {
            if (boreMDs.length < 2) {
              TVDs[i] = 0;
              continue;
            }
            prevTVD = boreTVDs[boreTVDs.length - 2];
            currentTVD = boreTVDs[boreTVDs.length - 1];
            prevMD = boreMDs[boreMDs.length - 2];
            currentMD = boreMDs[boreMDs.length - 1];
          } else if (j < 1) {
            TVDs[i] = boreTVDs[0];
            continue;
          } else {
            prevMD = boreMDs[j - 1];
            currentMD = boreMDs[j];
            prevTVD = boreTVDs[j - 1];
            currentTVD = boreTVDs[j];
          }
          TVDs[i] = (MDs[i] - prevMD) / (currentMD - prevMD) * (currentTVD - prevTVD) + prevTVD;
        }
        break;
      case TRUE_VERTICAL_DEPTH:
        // If the log trace domain is TVD, then simply copy the z values.
        TVDs = Utilities.copyDoubleArray(_zValues);
        break;
      case TRUE_VERTICAL_DEPTH_SUBSEA:
        float elevation = wellBore.getElevation();
        // If the log trace domain is TVDSS, then convert the depths by adding the elevation.
        TVDs = Utilities.copyDoubleArray(_zValues);
        for (int i = 0; i < TVDs.length; i++) {
          TVDs[i] += elevation;
        }
        break;
      default:
        throw new RuntimeException("Invalid domain: " + _zDomain);
    }
    return TVDs;
  }

  /**
   * Returns the true-vertical-depth-subsea (TVDSS) values.
   */
  public double[] getTrueVerticalDepthsSubsea() {
    load();

    WellBore wellBore = _well.getWellBore();
    double[] TVDSSs = new double[0];
    switch (_zDomain) {
      case ONE_WAY_TIME:
      case TWO_WAY_TIME:
      case MEASURED_DEPTH:
      case TRUE_VERTICAL_DEPTH:
        // Calculate the TVD values and then subtract the elevation.
        float elevation = wellBore.getElevation();
        TVDSSs = getTrueVerticalDepths();
        for (int i = 0; i < TVDSSs.length; i++) {
          TVDSSs[i] -= elevation;
        }
        break;
      case TRUE_VERTICAL_DEPTH_SUBSEA:
        // If the log trace domain is TVDSS, then simply copy the z values.
        TVDSSs = Utilities.copyDoubleArray(_zValues);
        break;
      default:
        throw new RuntimeException("Invalid domain: " + _zDomain);
    }
    return TVDSSs;
  }

  /**
   * Returns the date the log was recorded.
   */
  public void setCreationDate(final Timestamp creationDate) {
    _creationDate = creationDate;
    setDirty(true);
  }

  /**
   * Returns the date the log was recorded.
   */
  public void setCreationRoutine(final String creationRoutine) {
    _creationRoutine = creationRoutine;
    setDirty(true);
  }

  /**
   * Sets the company or person that recorded the log.
   */
  public void setDataSource(final String dataSource) {
    _dataSource = dataSource;
    setDirty(true);
  }

  /**
   * Sets the pass of the log run.
   */
  public void setLogPass(final String logPass) {
    _logPass = logPass;
    setDirty(true);
  }

  /**
   * Sets the log run number.
   */
  public void setRunNumber(final int runNumber) {
    _runNumber = runNumber;
    setDirty(true);
  }

  /**
   * Sets the name of the logging service.
   */
  public void setServiceName(final String serviceName) {
    _serviceName = serviceName;
    setDirty(true);
  }

  /**
   * Sets the name of the software used to create the log trace.
   */
  public void setSoftwareSource(final String softwareSource) {
    _softwareSource = softwareSource;
    setDirty(true);
  }

  /**
   * Sets the mnemonic for the log trace (e.g. GR, RHOB).
   */
  public void setTraceMnemonic(final String traceMnemonic) {
    _traceMnemonic = traceMnemonic;
    setDirty(true);
  }

  /**
   * Sets the user identifiable name for the log trace.
   */
  public void setTraceName(final String traceName) {
    _traceName = traceName;
    setDirty(true);
  }

  /**
   * Sets the level of processing has been performed on the log trace.
   */
  public void setTraceProcess(final String traceProcess) {
    _traceProcess = traceProcess;
    setDirty(true);
  }

  /**
   * Sets the type of the trace data values.
   */
  public void setTraceType(final String traceType) {
    _traceType = traceType;
    setDirty(true);
  }

  /**
   * Sets the amount of processing performed for the trace by version number.
   */
  public void setTraceVersion(final int traceVersion) {
    _traceVersion = traceVersion;
    setDirty(true);
  }

  /**
   * Sets the id of the user that created the log.
   */
  public void setCreationUserID(final String userID) {
    _creationUserId = userID;
    setDirty(true);
  }

  /**
   * Sets the id of the user that updated the log.
   */
  public void setUpdateUserID(final String userID) {
    _updateUserId = userID;
    setDirty(true);
  }

  /**
   * Returns the trace data.
   */
  public float[] getTraceData() {
    load();
    return Utilities.copyFloatArray(_traceData);
  }

  /**
   * Set the trace data.
   * 
   * @param the array of data values.
   * @param dataUnit the unit of measurement for the data values.
   * @param nullValue the value representing <i>null</i>.
   */
  public void setTraceData(final float[] traceData, final Unit dataUnit, final float nullValue) {
    _traceData = Utilities.copyFloatArray(traceData);
    _dataUnit = dataUnit;
    _nullValue = nullValue;
    computeMinMax();
    setDirty(true);
  }

  /**
   * Sets the z values.
   * 
   * @param zValues the array of z values.
   * @param zDomain the domain of the z values (MD, TVD, TWT, etc).
   */
  public void setZValues(final double[] zValues, final WellDomain zDomain) {
    _zValues = Utilities.copyDoubleArray(zValues);
    _zDomain = zDomain;

    // Set the Z top and Z base values
    int basePntr = _zValues.length - 1;
    if (basePntr >= 0) {
      _zTop = _zValues[0];
      _zBase = _zValues[basePntr];
    }
    setDirty(true);
  }

  /**
   * Computes the minimum and maximum non-null values
   * contained in the log trace. If the log trace is entirely null,
   * then the min/max values are set to the null value.
   */
  protected void computeMinMax() {
    // Find the minimum and maximum non-null values.
    float minValue = Float.MAX_VALUE;
    float maxValue = -Float.MAX_VALUE;
    boolean allNulls = true;
    int numSamples = _traceData.length;
    for (int k = 0; k < numSamples; k++) {
      if (!MathUtil.isEqual(_traceData[k], _nullValue)) {
        float value = _traceData[k];
        minValue = Math.min(minValue, value);
        maxValue = Math.max(maxValue, value);
        allNulls = false;
      }
    }
    // If the log trace is all nulls, then set the min/max to the null value.
    // Otherwise use the computed min/max values.
    if (!allNulls) {
      _minValue = minValue;
      _maxValue = maxValue;
    } else {
      _minValue = _nullValue;
      _maxValue = _nullValue;
    }
  }
}
