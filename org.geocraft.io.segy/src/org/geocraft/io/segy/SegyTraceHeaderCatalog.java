/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.util.HashMap;
import java.util.Map;

import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.HeaderEntry.Format;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;


public class SegyTraceHeaderCatalog {

  public static final HeaderEntry TRACE_SEQ_NUM_IN_LINE = create("TRACE_SEQ_NUM_IN_LINE", Format.INTEGER,
      "Trace Seq. Number in Line",
      "Trace sequence number within line - numbers continue to increase if additional reels are required on same line.");

  public static final HeaderEntry TRACE_SEQ_NUM_IN_REEL = create("TRACE_SEQ_NUM_IN_REEL", Format.INTEGER,
      "Trace Seq. Number in Reel", "Trace sequence number within reel - each reel starts with trace number one.");

  public static final HeaderEntry FIELD_RECORD_NUM = create("FIELD_RECORD_NUM", Format.INTEGER, "Field Record Number",
      "Original field record number.");

  public static final HeaderEntry TRACE_NUM = create("TRACE_NUM", Format.INTEGER, "Trace Number",
      "Trace number within original field record.");

  public static final HeaderEntry SOURCE_POINT_NUM = create("SOURCE_POINT_NUM", Format.INTEGER, "Source Point Number",
      "Energy source point number.");

  public static final HeaderEntry CDP_NUM = create("CDP_NUM", Format.INTEGER, "CDP Number", "CDP ensemble number.");

  public static final HeaderEntry TRACE_NUM_IN_CDP = create("TRACE_NUM_IN_CDP", Format.INTEGER, "Trace Number in CDP",
      "Trace number within the CDP ensemble - each ensemble starts with trace number one.");

  public static final HeaderEntry TRACE_ID = create(
      "TRACE_ID",
      Format.SHORT,
      "Trace Identification",
      "Trace identification code: 1=seismic data, 2=dead, 3=dummy, 4=time break, 5=uphole, 6=sweep, 7=timing, 8=water break, N=optional use.");

  public static final HeaderEntry NUM_VERTICAL_TRACES = create("NUM_VERTICAL_TRACES", Format.SHORT,
      "Number of Vertical Traces",
      "Number of vertically summed traces yielding this trace (1 is one trace, 2 is two summed traces, etc).");

  public static final HeaderEntry NUM_HORIZONTAL_TRACES = create("NUM_HORIZONTAL_TRACES", Format.SHORT,
      "Number of Horizontal Traces",
      "Number of horizontally stacked traces yielding this trace (1 is one trace, 2 is two stacked traces, etc).");

  public static final HeaderEntry DATA_USE = create("DATA_USE", Format.SHORT, "Data Use",
      "Data use: 1=production, 2=test.");

  public static final HeaderEntry SOURCE_RECEIVER_DISTANCE = create("SOURCE_RECEIVER_DISTANCE", Format.INTEGER,
      "Source-Receiver Distance",
      "Distance from source point to receiver group (negative if opposite to direction in which line is shot.");

  public static final HeaderEntry RECEIVER_ELEVATION = create("RECEIVER_ELEVATION", Format.INTEGER,
      "Receiver Elevation",
      "Receiver group elevation; all elevations above sea level are positive and below sea level are negative.");

  public static final HeaderEntry SOURCE_ELEVATION = create("INTEGER_FORMAT", Format.INTEGER, "Source Elevation",
      "Surface elevation at source.");

  public static final HeaderEntry SOURCE_DEPTH = create("SOURCE_DEPTH", Format.INTEGER, "Source Depth",
      "Source depth below surface (a positive number).");

  public static final HeaderEntry DATUM_ELEVATION_AT_RECEIVER = create("DATUM_ELEVATION_AT_RECEIVER", Format.INTEGER,
      "Datum Elevation at Receiver", "Datum elevation at receiver group.");

  public static final HeaderEntry DATUM_ELEVATION_AT_SOURCE = create("DATUM_ELEVATION_AT_SOURCE", Format.INTEGER,
      "Datum Elevation at Source", "Datum elevation at source.");

  public static final HeaderEntry WATER_DEPTH_AT_SOURCE = create("WATER_DEPTH_AT_SOURCE", Format.INTEGER,
      "Water Depth at Source", "Water depth at source.");

  public static final HeaderEntry WATER_DEPTH_AT_GROUP = create("WATER_DEPTH_AT_GROUP", Format.INTEGER,
      "Water Depth at Group", "Water depth at group.");

  public static final HeaderEntry ELEVATION_DEPTH_SCALAR = create(
      "ELEVATION_DEPTH_SCALAR",
      Format.SHORT,
      "Elevation Depth Scalar",
      "Scalar to be applied to all elevations and depths in bytes 41-68 to give real value. If positive, scalar is used as multiplier; if negative, scalar is used as divisor.");

  public static final HeaderEntry COORDINATE_SCALAR = create(
      "COORDINATE_SCALAR",
      Format.SHORT,
      "Coordinate Scalar",
      "Scalar to be applied to all coordinates in bytes 73-88 to give real value. If positive, scalar is used as multiplier; if negative, scalar is used as divisor.");

  public static final HeaderEntry SOURCE_COORDINATE_X = create("SOURCE_COORDINATE_SCALAR", Format.INTEGER,
      "Source Coordinate - X", "Source coordinate - X.");

  public static final HeaderEntry SOURCE_COORDINATE_Y = create("SOURCE_COORDINATE_Y", Format.INTEGER,
      "Source Coordinate - Y", "Source coordinate - Y.");

  public static final HeaderEntry GROUP_COORDINATE_X = create("GROUP_COORDINATE_X", Format.INTEGER,
      "Group Coordinate - X", "Group coordinate - X.");

  public static final HeaderEntry GROUP_COORDINATE_Y = create("GROUP_COORDINATE_Y", Format.INTEGER,
      "Group Coordinate - Y", "Group coordinate - Y.");

  public static final HeaderEntry COORDINATE_UNITS = create("COORDINATE_UNITS", Format.SHORT, "Coordinate Units",
      "Coordinate units: 1=length (meters or feet), 2=seconds of arc.");

  public static final HeaderEntry WEATHERING_VELOCITY = create("WEATHERING_VELOCITY", Format.SHORT,
      "Weathering Velocity", "Weathering velocity.");

  public static final HeaderEntry SUBWEATHERING_VELOCITY = create("SUBWEATHERING_VELOCITY", Format.SHORT,
      "Subweathering Velocity", "Subweathering velocity.");

  public static final HeaderEntry UPHOLE_TIME_AT_SOURCE = create("UPHOLE_TIME_AT_SOURCE", Format.SHORT,
      "Uphole Time at Source", "Uphole time at source.");

  public static final HeaderEntry UPHOLE_TIME_AT_GROUP = create("UPHOLE_TIME_AT_GROUP", Format.SHORT,
      "Uphole Time at Group", "Uphole time at group.");

  public static final HeaderEntry SOURCE_STATIC_CORRECTION = create("SOURCE_STATIC_CORRECTION", Format.SHORT,
      "Source Static Correction", "Source static correction.");

  public static final HeaderEntry GROUP_STATIC_CORRECTION = create("GROUP_STATIC_CORRECTION", Format.SHORT,
      "Group Static Correction", "Group static correction.");

  public static final HeaderEntry TOTAL_STATIC_APPLIED = create("TOTAL_STATIC_APPLIED", Format.SHORT,
      "Total Static Applied", "Total static applied (Zero if no static has been applied).");

  public static final HeaderEntry LAG_TIME_A = create("LAG_TIME_A", Format.SHORT, "Lag Time A",
      "Lag time A. Time in msec between end of 240-byte trace identification header and time break.");

  public static final HeaderEntry LAG_TIME_B = create("LAG_TIME_B", Format.SHORT, "Lag Time B",
      "Lag time B. Time in msec between time break and the initiation time of the energy source.");

  public static final HeaderEntry DELAY_RECORDING_TIME = create(
      "DELAY_RECORDING_TIME",
      Format.SHORT,
      "Delay Recording Time",
      "Delay recording time. Time in msec between initiation time of energy source and time when recording of data samples begins (for deep water work if data recording does not start at zero time).");

  public static final HeaderEntry MUTE_TIME_START = create("MUTE_TIME_START", Format.SHORT, "Mute Time Start",
      "Mute time - start.");

  public static final HeaderEntry MUTE_TIME_END = create("MUTE_TIME_END", Format.SHORT, "Mute Time End",
      "Mute time - end.");

  public static final HeaderEntry NUM_SAMPLES = create("NUM_SAMPLES", Format.SHORT, "Number of Samples",
      "Number of samples in this trace.");

  public static final HeaderEntry SAMPLE_INTERVAL = create("SAMPLE_INTERVAL", Format.SHORT, "Sample Interval",
      "Sample interval in usec for this trace.");

  public static final HeaderEntry GAIN_TYPE = create("GAIN_TYPE", Format.SHORT, "Gain Type",
      "Gain type of field instruments: 1=fixed, 2=binary, 3=floating point, N=optional use.");

  public static final HeaderEntry INSTRUMENT_GAIN_CONSTANT = create("INSTRUMENT_GAIN_CONSTANT", Format.SHORT,
      "Instrument Gain Constant", "Instrument gain constant.");

  public static final HeaderEntry INSTRUMENT_INITIAL_GAIN = create("INSTRUMENT_INITIAL_GAIN", Format.SHORT,
      "Instrument Initial Gain", "Instrument early of initial gain (db).");

  public static final HeaderEntry CORRELATED = create("CORRELATED", Format.SHORT, "Correlated",
      "Correlated: 1=no, 2=yes.");

  public static final HeaderEntry SWEEP_FREQ_AT_START = create("SWEEP_FREQ_AT_START", Format.SHORT,
      "Sweep Freq. at Start", "Sweep frequency at start.");

  public static final HeaderEntry SWEEP_FREQ_AT_END = create("SWEEP_FREQ_AT_END", Format.SHORT, "Sweep Freq. at End",
      "Sweep frequency at end.");

  public static final HeaderEntry SWEEP_LENGTH = create("SWEEP_LENGTH", Format.SHORT, "Sweep Length",
      "Sweep length in ms");

  public static final HeaderEntry SWEEP_TYPE = create("SWEEP_TYPE", Format.SHORT, "Sweep Type",
      "Sweep type: 1=linear, 2=parabolic, 3=exponential, 4=other.");

  public static final HeaderEntry SWEEP_TAPER_LENGTH_START = create("SWEEP_TAPER_LENGTH_START", Format.SHORT,
      "Sweep Taper at Start", "Sweep trace taper length at start in ms.");

  public static final HeaderEntry SWEEP_TAPER_LENGTH_END = create("SWEEP_TAPER_LENGTH_END", Format.SHORT,
      "Sweep Taper at End", "Sweep trace taper length at end in ms.");

  public static final HeaderEntry TAPER_TYPE = create("TAPER_TYPE", Format.SHORT, "Taper Type",
      "Taper type: 1=linear, 2=cos^2, 3=other.");

  public static final HeaderEntry ALIAS_FILTER_FREQ = create("ALIAS_FILTER_FREQ", Format.SHORT, "Alias Filter Freq.",
      "Alias filter frequency, if used.");

  public static final HeaderEntry ALIAS_FILTER_SLOPE = create("ALIAS_FILTER_SLOPE", Format.SHORT, "Alias Filter Slope",
      "Alias filter slope.");

  public static final HeaderEntry NOTCH_FILTER_FREQ = create("NOTCH_FILTER_FREQ", Format.SHORT, "Notch Filter Freq.",
      "Notch filter frequency, if used.");

  public static final HeaderEntry NOTCH_FILTER_SLOPE = create("NOTCH_FILTER_SLOPE", Format.SHORT, "Notch Filter Slope",
      "Notch filter slope.");

  public static final HeaderEntry LOW_CUT_FREQ = create("LOW_CUT_FREQ", Format.SHORT, "Low Cut Freq.",
      "Low cut frequency, if used.");

  public static final HeaderEntry HIGH_CUT_FREQ = create("HIGH_CUT_FREQ", Format.SHORT, "High Cut Freq.",
      "High cut frequency.");

  public static final HeaderEntry LOW_CUT_SLOPE = create("LOW_CUT_SLOPE", Format.SHORT, "Low Cut Slope",
      "Low cut slope.");

  public static final HeaderEntry HIGH_CUT_SLOPE = create("HIGH_CUT_SLOPE", Format.SHORT, "High Cut Slope",
      "High cut slope.");

  public static final HeaderEntry YEAR_DATA_RECORDED = create("YEAR_DATA_RECORDED", Format.SHORT, "Year Data Recorded",
      "Year data recorded.");

  public static final HeaderEntry DAY_OF_YEAR = create("DAY_OF_YEAR", Format.SHORT, "Day of Year", "Day of year.");

  public static final HeaderEntry HOUR_OF_DAY = create("HOUR_OF_DAY", Format.SHORT, "Hour of Day",
      "Hour of day (24 hour clock).");

  public static final HeaderEntry MINUTE_OF_HOUR = create("MINUTE_OF_HOUR", Format.SHORT, "Minute of Hour",
      "Minute of hour.");

  public static final HeaderEntry SECOND_OF_MINUTE = create("SECOND_OF_MINUTE", Format.SHORT, "Second of Minute",
      "Second of minute.");

  public static final HeaderEntry TIME_BASIS_CODE = create("TIME_BASIS_CODE", Format.SHORT, "Time Basis Code",
      "Time basis code: 1=local, 2=GMT, 3=other.");

  public static final HeaderEntry TRACE_WEIGHTING_FACTOR = create("TRACE_WEIGHTING_FACTOR", Format.SHORT,
      "Trace Weighting Factor",
      "Trace weighting factor - defined as 2^-N volts for the least significant bit (N=0,1,...32767).");

  public static final HeaderEntry GEOPHONE_GROUP_ROLL = create("GEOPHONE_GROUP_ROLL", Format.SHORT,
      "Geophone Group Roll", "Geophone group number of roll switch position one.");

  public static final HeaderEntry GEOPHONE_GROUP_FIRST_TRACE = create("GEOPHONE_GROUP_FIRST_TRACE", Format.SHORT,
      "Geophone Group First Trace", "Geophone group number of trace number one within original field record.");

  public static final HeaderEntry GEOPHONE_GROUP_LAST_TRACE = create("GEOPHONE_GROUP_LAST_TRACE", Format.SHORT,
      "Geophone Group Last Trace", "Geophone group number of last trace within original field record.");

  public static final HeaderEntry GAP_SIZE = create("GAP_SIZE", Format.SHORT, "Gap Size",
      "Gap size (total number of groups dropped).");

  public static final HeaderEntry OVERTRAVEL = create("OVERTRAVEL", Format.SHORT, "Overtravel",
      "Overtravel associated with taper at beginning or end of line: 1=down (or behind), 2=up (or ahead).");

  public static final HeaderEntry[] STANDARD_ENTRIES = {

  TRACE_SEQ_NUM_IN_LINE, TRACE_SEQ_NUM_IN_REEL, FIELD_RECORD_NUM, TRACE_NUM, SOURCE_POINT_NUM, CDP_NUM,
      TRACE_NUM_IN_CDP, TRACE_ID, NUM_VERTICAL_TRACES, NUM_HORIZONTAL_TRACES, DATA_USE, SOURCE_RECEIVER_DISTANCE,
      RECEIVER_ELEVATION, SOURCE_ELEVATION, SOURCE_DEPTH, DATUM_ELEVATION_AT_RECEIVER, DATUM_ELEVATION_AT_SOURCE,
      WATER_DEPTH_AT_SOURCE, WATER_DEPTH_AT_GROUP, ELEVATION_DEPTH_SCALAR, COORDINATE_SCALAR, SOURCE_COORDINATE_X,
      SOURCE_COORDINATE_Y, GROUP_COORDINATE_X, GROUP_COORDINATE_Y, COORDINATE_UNITS, WEATHERING_VELOCITY,
      SUBWEATHERING_VELOCITY, UPHOLE_TIME_AT_SOURCE, UPHOLE_TIME_AT_GROUP, SOURCE_STATIC_CORRECTION,
      GROUP_STATIC_CORRECTION, TOTAL_STATIC_APPLIED, LAG_TIME_A, LAG_TIME_B, DELAY_RECORDING_TIME, MUTE_TIME_START,
      MUTE_TIME_END, NUM_SAMPLES, SAMPLE_INTERVAL, GAIN_TYPE, INSTRUMENT_GAIN_CONSTANT, INSTRUMENT_INITIAL_GAIN,
      CORRELATED, SWEEP_FREQ_AT_START, SWEEP_FREQ_AT_END, SWEEP_LENGTH, SWEEP_TYPE, SWEEP_TAPER_LENGTH_START,
      SWEEP_TAPER_LENGTH_END, TAPER_TYPE, ALIAS_FILTER_FREQ, ALIAS_FILTER_SLOPE, NOTCH_FILTER_FREQ, NOTCH_FILTER_SLOPE,
      LOW_CUT_FREQ, HIGH_CUT_FREQ, LOW_CUT_SLOPE, HIGH_CUT_SLOPE, YEAR_DATA_RECORDED, DAY_OF_YEAR, HOUR_OF_DAY,
      MINUTE_OF_HOUR, SECOND_OF_MINUTE, TIME_BASIS_CODE, TRACE_WEIGHTING_FACTOR, GEOPHONE_GROUP_ROLL,
      GEOPHONE_GROUP_FIRST_TRACE, GEOPHONE_GROUP_LAST_TRACE, GAP_SIZE, OVERTRAVEL };

  public static final HeaderEntry[] POSTSTACK2D_ENTRIES = {

  TRACE_SEQ_NUM_IN_LINE, TRACE_SEQ_NUM_IN_REEL, FIELD_RECORD_NUM, TRACE_NUM, SOURCE_POINT_NUM, CDP_NUM,
      TRACE_NUM_IN_CDP, TRACE_ID, NUM_VERTICAL_TRACES, NUM_HORIZONTAL_TRACES, DATA_USE, SOURCE_RECEIVER_DISTANCE,
      RECEIVER_ELEVATION, SOURCE_ELEVATION, SOURCE_DEPTH, DATUM_ELEVATION_AT_RECEIVER, DATUM_ELEVATION_AT_SOURCE,
      WATER_DEPTH_AT_SOURCE, WATER_DEPTH_AT_GROUP, ELEVATION_DEPTH_SCALAR, COORDINATE_SCALAR, SOURCE_COORDINATE_X,
      SOURCE_COORDINATE_Y, GROUP_COORDINATE_X, GROUP_COORDINATE_Y, COORDINATE_UNITS, WEATHERING_VELOCITY,
      SUBWEATHERING_VELOCITY, UPHOLE_TIME_AT_SOURCE, UPHOLE_TIME_AT_GROUP, SOURCE_STATIC_CORRECTION,
      GROUP_STATIC_CORRECTION, TOTAL_STATIC_APPLIED, LAG_TIME_A, LAG_TIME_B, DELAY_RECORDING_TIME, MUTE_TIME_START,
      MUTE_TIME_END, NUM_SAMPLES, SAMPLE_INTERVAL, GAIN_TYPE, INSTRUMENT_GAIN_CONSTANT, INSTRUMENT_INITIAL_GAIN,
      CORRELATED, SWEEP_FREQ_AT_START, SWEEP_FREQ_AT_END, SWEEP_LENGTH, SWEEP_TYPE, SWEEP_TAPER_LENGTH_START,
      SWEEP_TAPER_LENGTH_END, TAPER_TYPE, ALIAS_FILTER_FREQ, ALIAS_FILTER_SLOPE, NOTCH_FILTER_FREQ, NOTCH_FILTER_SLOPE,
      LOW_CUT_FREQ, HIGH_CUT_FREQ, LOW_CUT_SLOPE, HIGH_CUT_SLOPE, YEAR_DATA_RECORDED, DAY_OF_YEAR, HOUR_OF_DAY,
      MINUTE_OF_HOUR, SECOND_OF_MINUTE, TIME_BASIS_CODE, TRACE_WEIGHTING_FACTOR, GEOPHONE_GROUP_ROLL,
      GEOPHONE_GROUP_FIRST_TRACE, GEOPHONE_GROUP_LAST_TRACE, GAP_SIZE, OVERTRAVEL, TraceHeaderCatalog.INLINE_NO,
      TraceHeaderCatalog.CDP_NO, TraceHeaderCatalog.SHOTPOINT_NO, TraceHeaderCatalog.X, TraceHeaderCatalog.Y };

  public static final HeaderEntry[] POSTSTACK3D_ENTRIES = {

  TRACE_SEQ_NUM_IN_LINE, TRACE_SEQ_NUM_IN_REEL, FIELD_RECORD_NUM, TRACE_NUM, SOURCE_POINT_NUM, CDP_NUM,
      TRACE_NUM_IN_CDP, TRACE_ID, NUM_VERTICAL_TRACES, NUM_HORIZONTAL_TRACES, DATA_USE, SOURCE_RECEIVER_DISTANCE,
      RECEIVER_ELEVATION, SOURCE_ELEVATION, SOURCE_DEPTH, DATUM_ELEVATION_AT_RECEIVER, DATUM_ELEVATION_AT_SOURCE,
      WATER_DEPTH_AT_SOURCE, WATER_DEPTH_AT_GROUP, ELEVATION_DEPTH_SCALAR, COORDINATE_SCALAR, SOURCE_COORDINATE_X,
      SOURCE_COORDINATE_Y, GROUP_COORDINATE_X, GROUP_COORDINATE_Y, COORDINATE_UNITS, WEATHERING_VELOCITY,
      SUBWEATHERING_VELOCITY, UPHOLE_TIME_AT_SOURCE, UPHOLE_TIME_AT_GROUP, SOURCE_STATIC_CORRECTION,
      GROUP_STATIC_CORRECTION, TOTAL_STATIC_APPLIED, LAG_TIME_A, LAG_TIME_B, DELAY_RECORDING_TIME, MUTE_TIME_START,
      MUTE_TIME_END, NUM_SAMPLES, SAMPLE_INTERVAL, GAIN_TYPE, INSTRUMENT_GAIN_CONSTANT, INSTRUMENT_INITIAL_GAIN,
      CORRELATED, SWEEP_FREQ_AT_START, SWEEP_FREQ_AT_END, SWEEP_LENGTH, SWEEP_TYPE, SWEEP_TAPER_LENGTH_START,
      SWEEP_TAPER_LENGTH_END, TAPER_TYPE, ALIAS_FILTER_FREQ, ALIAS_FILTER_SLOPE, NOTCH_FILTER_FREQ, NOTCH_FILTER_SLOPE,
      LOW_CUT_FREQ, HIGH_CUT_FREQ, LOW_CUT_SLOPE, HIGH_CUT_SLOPE, YEAR_DATA_RECORDED, DAY_OF_YEAR, HOUR_OF_DAY,
      MINUTE_OF_HOUR, SECOND_OF_MINUTE, TIME_BASIS_CODE, TRACE_WEIGHTING_FACTOR, GEOPHONE_GROUP_ROLL,
      GEOPHONE_GROUP_FIRST_TRACE, GEOPHONE_GROUP_LAST_TRACE, GAP_SIZE, OVERTRAVEL, TraceHeaderCatalog.INLINE_NO,
      TraceHeaderCatalog.XLINE_NO, TraceHeaderCatalog.X, TraceHeaderCatalog.Y };

  public static final HeaderEntry[] PRESTACK3D_ENTRIES = {

  TRACE_SEQ_NUM_IN_LINE, TRACE_SEQ_NUM_IN_REEL, FIELD_RECORD_NUM, TRACE_NUM, SOURCE_POINT_NUM, CDP_NUM,
      TRACE_NUM_IN_CDP, TRACE_ID, NUM_VERTICAL_TRACES, NUM_HORIZONTAL_TRACES, DATA_USE, SOURCE_RECEIVER_DISTANCE,
      RECEIVER_ELEVATION, SOURCE_ELEVATION, SOURCE_DEPTH, DATUM_ELEVATION_AT_RECEIVER, DATUM_ELEVATION_AT_SOURCE,
      WATER_DEPTH_AT_SOURCE, WATER_DEPTH_AT_GROUP, ELEVATION_DEPTH_SCALAR, COORDINATE_SCALAR, SOURCE_COORDINATE_X,
      SOURCE_COORDINATE_Y, GROUP_COORDINATE_X, GROUP_COORDINATE_Y, COORDINATE_UNITS, WEATHERING_VELOCITY,
      SUBWEATHERING_VELOCITY, UPHOLE_TIME_AT_SOURCE, UPHOLE_TIME_AT_GROUP, SOURCE_STATIC_CORRECTION,
      GROUP_STATIC_CORRECTION, TOTAL_STATIC_APPLIED, LAG_TIME_A, LAG_TIME_B, DELAY_RECORDING_TIME, MUTE_TIME_START,
      MUTE_TIME_END, NUM_SAMPLES, SAMPLE_INTERVAL, GAIN_TYPE, INSTRUMENT_GAIN_CONSTANT, INSTRUMENT_INITIAL_GAIN,
      CORRELATED, SWEEP_FREQ_AT_START, SWEEP_FREQ_AT_END, SWEEP_LENGTH, SWEEP_TYPE, SWEEP_TAPER_LENGTH_START,
      SWEEP_TAPER_LENGTH_END, TAPER_TYPE, ALIAS_FILTER_FREQ, ALIAS_FILTER_SLOPE, NOTCH_FILTER_FREQ, NOTCH_FILTER_SLOPE,
      LOW_CUT_FREQ, HIGH_CUT_FREQ, LOW_CUT_SLOPE, HIGH_CUT_SLOPE, YEAR_DATA_RECORDED, DAY_OF_YEAR, HOUR_OF_DAY,
      MINUTE_OF_HOUR, SECOND_OF_MINUTE, TIME_BASIS_CODE, TRACE_WEIGHTING_FACTOR, GEOPHONE_GROUP_ROLL,
      GEOPHONE_GROUP_FIRST_TRACE, GEOPHONE_GROUP_LAST_TRACE, GAP_SIZE, OVERTRAVEL, TraceHeaderCatalog.INLINE_NO,
      TraceHeaderCatalog.XLINE_NO, TraceHeaderCatalog.OFFSET, TraceHeaderCatalog.X, TraceHeaderCatalog.Y };

  private static HeaderEntry create(final String id, final Format format, final String name, final String description) {
    return new HeaderEntry(id, name, description, format, 1);
  }

  private static Map<HeaderEntry, Integer> _byteOffsets;
  static {
    index();
  }

  public static void index() {
    _byteOffsets = new HashMap<HeaderEntry, Integer>();
    int byteOffset = 0;
    for (HeaderEntry headerEntry : STANDARD_ENTRIES) {
      _byteOffsets.put(headerEntry, byteOffset);
      switch (headerEntry.getFormat()) {
        case SHORT:
          byteOffset += 2;
          break;
        case INTEGER:
          byteOffset += 4;
          break;
        default:
          throw new RuntimeException("Invalid entry for SEG-Y trace header: " + headerEntry);
      }
    }
  }

  public static int getByteOffset(HeaderEntry headerEntry) {
    Integer byteOffset = _byteOffsets.get(headerEntry);
    if (byteOffset != null) {
      return _byteOffsets.get(headerEntry);
    }
    return -1;
  }
}
