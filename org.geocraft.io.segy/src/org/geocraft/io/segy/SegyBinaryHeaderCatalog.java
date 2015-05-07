/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.util.HashMap;
import java.util.Map;

import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.HeaderEntry.Format;


/**
 * The catalog of SEG-Y binary header elements.
 */
public class SegyBinaryHeaderCatalog {

  public static final HeaderEntry JOB_IDENTIFICATION = new HeaderEntry("JOB_ID", "Job ID",
      "Job identification number.", Format.INTEGER, 1);

  public static final HeaderEntry LINE_NUM = new HeaderEntry("LINE_NUM", "Line Number",
      "Line number (only one line per reel).", Format.INTEGER, 1);

  public static final HeaderEntry REEL_NUM = new HeaderEntry("REEL_NUM", "Reel Number", "Reel number.", Format.INTEGER,
      1);

  public static final HeaderEntry TRACES_PER_RECORD = new HeaderEntry("TRACES_PER_RECORD", "Traces per Record",
      "Number of data traces per record.", Format.SHORT, 1);

  public static final HeaderEntry AUX_TRACES_PER_RECORD = new HeaderEntry("AUX_TRACES_PER_RECORD",
      "Aux. Traces per Record", "Number of auxiliary traces per record.", Format.SHORT, 1);

  public static final HeaderEntry SAMPLE_INTERVAL = new HeaderEntry("SAMPLE_INTERVAL", "Sample Interval",
      "Sample interval in usec (for this reel of data).", Format.SHORT, 1);

  public static final HeaderEntry SAMPLE_INTERVAL_ORIG = new HeaderEntry("SAMPLE_INTERVAL_ORIG",
      "Sample Interval (original)", "Sample interval in usec (for original field recording).", Format.SHORT, 1);

  public static final HeaderEntry SAMPLES_PER_TRACE = new HeaderEntry("SAMPLES_PER_TRACE", "Samples per Trace",
      "Number of samples per data trace (for this reel of data).", Format.SHORT, 1);

  public static final HeaderEntry SAMPLES_PER_TRACE_ORIG = new HeaderEntry("SAMPLES_PER_TRACE_ORIG",
      "Samples per Trace (original)", "Number of samples per data trace (for original field recording).", Format.SHORT,
      1);

  public static final HeaderEntry SAMPLE_FORMAT_CODE = new HeaderEntry("SAMPLE_FORMAT_CODE", "Sample Format Code",
      "Data sample format code.", Format.SHORT, 1);

  public static final HeaderEntry CDP_FOLD = new HeaderEntry("CDP_FOLD", "CDP Fold",
      "CDP fold (expected number of data traces per CDP ensemble).", Format.SHORT, 1);

  public static final HeaderEntry TRACE_SORTING_CODE = new HeaderEntry(
      "TRACE_SORTING_CODE",
      "Trace Sorting Code",
      "Trace sorting code, 100); 1=no sorting, 2=CDP ensemble, 3=single fold continuous profile, 4=horizontally stacked.",
      Format.SHORT, 1);

  public static final HeaderEntry VERTICAL_SUM_CODE = new HeaderEntry("VERTICAL SUM_CODE", "Vertical Sum Code",
      "Vertical sum code: 1=no sum, 2=two sum, N=N sum.", Format.SHORT, 1);

  public static final HeaderEntry SWEEP_FREQ_START = new HeaderEntry("SWEEP_FREQ_START", "Sweep Frequency at Start",
      "Sweep frequency at start.", Format.SHORT, 1);

  public static final HeaderEntry SWEEP_FREQ_END = new HeaderEntry("SWEEP_FREQ_END", "Sweep Frequency at End",
      "Sweep frequency at end.", Format.SHORT, 1);

  public static final HeaderEntry SWEEP_LENGTH = new HeaderEntry("SWEEP_LENGTH", "Sweep Length",
      "Sweep length in msec:.", Format.SHORT, 1);

  public static final HeaderEntry SWEEP_TYPE_CODE = new HeaderEntry("SWEEP_TYPE_CODE", "Sweep Type Code",
      "Sweep type code: 1=linear, 2=parabolic, 3=exponential, 4=other.", Format.SHORT, 1);

  public static final HeaderEntry TRACE_NUM_OF_SWEEP_CHANNEL = new HeaderEntry("TRACE_NUM_OF_SWEEP_CHANNEL",
      "Trace Number of Sweep Channel", "Trace number of sweep channel.", Format.SHORT, 1);

  public static final HeaderEntry TAPER_LENGTH_START = new HeaderEntry("TAPER_LENGTH_START", "Taper Length Start",
      "Sweep trace taper length in msec at start (the taper starts at zero time and is effectvie for this length.",
      Format.SHORT, 1);

  public static final HeaderEntry TAPER_LENGTH_END = new HeaderEntry(
      "TAPER_LENGTH_END",
      "Taper Length End",
      "Sweep trace taper length in msec at end (the ending taper starts at sweep length minus the taper length at end.",
      Format.SHORT, 1);

  public static final HeaderEntry TAPER_TYPE = new HeaderEntry("TAPER_TYPE", "Taper Type",
      "Taper type: 1=linear, 2=cos^2, 3=other.", Format.SHORT, 1);

  public static final HeaderEntry CORR_DATA_TRACES = new HeaderEntry("CORR_DATA_TRACES", "Correlated Data Traces",
      "Correlated data traces: 1=no, 2=yes.", Format.SHORT, 1);

  public static final HeaderEntry BINARY_GAIN_RECOVERED = new HeaderEntry("BINARY_GAIN_RECOVERED",
      "Binary Gain Recovered", "Binary gain recovered: 1=yes, 2=no.", Format.SHORT, 1);

  public static final HeaderEntry AMPL_RECOVERY_METHOD = new HeaderEntry("AMPL_RECOVERY_METHOD",
      "Amplitude Recovery Method", "Amplitude recovery method: 1=none, 2=spherical divergence, 3=AGC, 4=other.",
      Format.SHORT, 1);

  public static final HeaderEntry MEASUREMENT_SYSTEM = new HeaderEntry("MEASUREMENT_SYSTEM", "Measurement System",
      "Measurement system: 1=meters, 2=feet.", Format.SHORT, 1);

  public static final HeaderEntry IMPULSE_SIGNAL_POLARITY = new HeaderEntry(
      "IMPULSE_SIGNAL_POLARITY",
      "Impulse Signal Polarity",
      "Impulse signal polarity: 1=Increase in pressure gives negative number on tape, 2=Increase in pressure gives positive number on tape.",
      Format.SHORT, 1);

  public static final HeaderEntry VIBRATORY_POLARITY_CODE = new HeaderEntry("VIBRATORY_POLARITY_CODE",
      "Vibratory Polarity Code", "Vibratory polarity code.", Format.SHORT, 1);

  // SEG-Y rev1 additions

  public static final HeaderEntry SEGY_FORMAT_REVISION_NUMBER = new HeaderEntry("SEGY_FORMAT_REVISION_NUMBER",
      "SEG-Y Format Revision Number", "SEG-Y format revision number.", Format.SHORT, 1);

  public static final HeaderEntry FIXED_TRACE_LENGTH_FLAG = new HeaderEntry("FIXED_TRACE_LENGTH_FLAG",
      "Fixed Trace Length Flag", "Fixed trace length flag: 1=fixed length, 0=variable length.", Format.SHORT, 1);

  public static final HeaderEntry NUMBER_OF_EXTENDED_HEADERS = new HeaderEntry(
      "NUMBER_OF_EXTENDED_HEADERS",
      "Number of Extended Textual Headers",
      "Number of extended textual headers: -1=variable # of extended headers, 0=no extended headers, N=N extended headers",
      Format.SHORT, 1);

  private static Map<HeaderEntry, Integer> _byteOffsets;
  static {
    index();
  }

  /**
   * Gets the list of standard SEG-Y binary header elements.
   * @return the list of standard SEG-Y binary header elements.
   */
  public static HeaderEntry[] getStandardList() {
    HeaderEntry[] headerEntries = { JOB_IDENTIFICATION, LINE_NUM, REEL_NUM, TRACES_PER_RECORD, AUX_TRACES_PER_RECORD,
        SAMPLE_INTERVAL, SAMPLE_INTERVAL_ORIG, SAMPLES_PER_TRACE, SAMPLES_PER_TRACE_ORIG, SAMPLE_FORMAT_CODE, CDP_FOLD,
        TRACE_SORTING_CODE, VERTICAL_SUM_CODE, SWEEP_FREQ_START, SWEEP_FREQ_END, SWEEP_LENGTH, SWEEP_TYPE_CODE,
        TRACE_NUM_OF_SWEEP_CHANNEL, TAPER_LENGTH_START, TAPER_LENGTH_END, TAPER_TYPE, CORR_DATA_TRACES,
        BINARY_GAIN_RECOVERED, AMPL_RECOVERY_METHOD, MEASUREMENT_SYSTEM, IMPULSE_SIGNAL_POLARITY,
        VIBRATORY_POLARITY_CODE };
    return headerEntries;
  }

  public static void index() {
    _byteOffsets = new HashMap<HeaderEntry, Integer>();
    int byteOffset = 0;
    for (HeaderEntry headerEntry : getStandardList()) {
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
    _byteOffsets.put(SEGY_FORMAT_REVISION_NUMBER, 300);
    _byteOffsets.put(FIXED_TRACE_LENGTH_FLAG, 302);
    _byteOffsets.put(NUMBER_OF_EXTENDED_HEADERS, 304);
  }

  public static int getByteOffset(HeaderEntry headerEntry) {
    return _byteOffsets.get(headerEntry);
  }
}
