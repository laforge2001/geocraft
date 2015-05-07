/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.io.asciigrid;


import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.geomath.algorithm.util.asciiexport.AsciiExportRegistry;
import org.geocraft.geomath.algorithm.util.asciiexport.IAsciiExportStrategy;
import org.geocraft.geomath.algorithm.util.parameters.IParameter;


/**
 * Defines the strategy for exporting ascii data
 */
public class ExportHorizonAsciiData implements IAsciiExportStrategy {

  /**
   * Registers the Horizon ascii data with the registry.
   */
  public static void register() {
    AsciiExportRegistry.getInstance().register("Horizon Ascii data", ExportHorizonAsciiData.class);
  }

  /** The list of global headers. */
  private List<IParameter> _globalHeaderParameters;

  /** The list of horizons containing data. */
  private List<Grid3d> _horizonsWithData;

  /** The list of parameters and the parameter headers */
  private List<String> _stringParameters;

  private List<String> _parameterHdrs;

  /**
   * The default constructor.
   */
  public ExportHorizonAsciiData() {
    _globalHeaderParameters = new ArrayList<IParameter>();
  }

  /**
   * Returns the file extension
   * 
   * @return the file extension
   */
  public String getFileExtension() {
    return "txt";
  }

  /**
   * Returns the array of global header parameters.
   * 
   * @return the array of global header parameters.
   */
  public IParameter[] getGlobalHeaderParameters() {

    IParameter[] parameters = new IParameter[_globalHeaderParameters.size()];
    int nParms = 0;
    for (IParameter header : _globalHeaderParameters) {
      parameters[nParms] = header;
      nParms++;
    }

    return parameters;
  }

  /**
   * Sets the global headers.
   * 
   * @param globalHeaderParameters
   *          the array of global header parameters.
   */
  public void setGlobalHeaders(final List<IParameter> globalHeaderParameters) {
    _globalHeaderParameters = globalHeaderParameters;
  }

  /**
   * Sets the horizons containing data.
   * 
   * @param horizonsWithData
   *          the array of horizons with data to output to the text file.
   */
  public void setHorizonsWithData(final List<Grid3d> horizonsWithData) {
    _horizonsWithData = horizonsWithData;
  }

  /**
   * Sets string parameters.
   * 
   * @param horizonsWithData
   *          the array of horizons with data to output to the text file.
   */
  public void setStringParameters(final List<String> stringParameters, final List<String> parameterHdrs) {

    _stringParameters = stringParameters;
    _parameterHdrs = parameterHdrs;
  }

  /**
   * Formats the global headers.
   * 
   * @return the formatted global headers string.
   */
  public String formatGlobalHeaders() {
    StringBuilder buffer = new StringBuilder();
    for (IParameter parameter : _globalHeaderParameters) {
      StringBuilder line = new StringBuilder();
      Formatter formatter = new Formatter(line);
      String id = parameter.getUniqueID();
      String value = parameter.getValueAsString();
      formatter.format("#%s = %s", id, value);
      buffer.append(formatter.toString());
      buffer.append("\n");
    }
    return buffer.toString();
  }

  /**
    * Formats a trace function.
    * (Traces not used to export horizon data
    * 
    * @param trace
    *          the trace.
    * @param traceNo
    *          the sequential trace number.
    * @return the formatted trace function string.
    */
  public String formatTraceFunction(final Trace trace, final int traceNo, final int sampleDecimation,
      final float gridVal) {
    // return nothing
    return null;
  }

  /**
   * Formats a trace function.
   * (Traces not used to export horizon data
   * 
   * @param trace
   *          the trace.
   * @param traceNo
   *          the sequential trace number.
   * @return the formatted trace function string.
   */
  public String formatTraceFunction(final Trace trace, final int traceNo, final int sampleDecimation) {
    // return nothing
    return null;
  }

  /**
   * Formats a horizon values for at a x, y location.
   * 
   * @param trace
   *          the trace.
   * @param traceNo
   *          the sequential trace number.
   * @return the formatted trace function string.
   */
  public String formatHorizonValues(final double x, final double y, final int nVals) {
    StringBuilder builder = new StringBuilder();
    // The header is needed just before the first line
    if (nVals == 0) {
      builder.append(formatDataHeader());
    }
    Formatter formatter = new Formatter(builder);

    // add the X and Y values to the data
    formatter.format("  %16.4f", x);
    formatter.format(" %16.4f", y);

    // Determine the data in each of the horizons
    for (Grid3d horizon : _horizonsWithData) {
      boolean isValidXY = true;
      // Determine the size of the input horizon
      GridGeometry3d geometry = horizon.getGeometry();
      int nRows = geometry.getNumRows();
      int nCols = geometry.getNumColumns();

      // determine row and column in horizon
      double[] rowcol = geometry.transformXYToRowCol(x, y, true);
      int row = (int) rowcol[0];
      if (row < 0 || row > nRows) {
        isValidXY = false;
      }
      int col = (int) rowcol[1];
      if (col < 0 || col > nCols) {
        isValidXY = false;
      }

      // display value in horizon
      float nullValue = horizon.getNullValue();
      float value = nullValue;
      if (isValidXY) {
        if (horizon.isNull(row, col)) {
          formatter.format(" %20.4e", nullValue);
        } else {
          value = horizon.getValueAtRowCol(row, col);
          formatter.format(" %20.8f", value);
        }
      } else {
        formatter.format(" %20.8e", value);
      }
    }

    // Determine the parameters
    for (String parmStr : _stringParameters) {
      formatter.format(" %16s", parmStr);
    }
    // Add a return
    formatter.format("\n");

    // return the current format line
    return builder.toString();
  }

  /**
   * Formats a header.
   * 
   * @return the formatted header string.
   */
  private String formatDataHeader() {
    StringBuilder builder = new StringBuilder();
    Formatter formatter = new Formatter(builder);

    int nHeaders = 2 + _horizonsWithData.size() + _parameterHdrs.size();
    Object[] headers = new Object[nHeaders];
    String formatStr = "#";
    headers[0] = "X";
    formatStr = formatStr + " %16s";
    headers[1] = "Y";
    formatStr = formatStr + " %16s";
    // Determine the headers for each of the horizons
    int hdrIndx = 2;
    for (Grid3d horizon : _horizonsWithData) {
      headers[hdrIndx] = horizon.getDisplayName();
      formatStr = formatStr + " %16s";
      hdrIndx++;
    }
    // Determine the headers for the parameters
    for (String hdr : _parameterHdrs) {
      headers[hdrIndx] = hdr;
      formatStr = formatStr + " %16s";
      hdrIndx++;
    }
    formatStr = formatStr + "\n";
    formatter.format(formatStr, headers);
    return builder.toString();
  }
}
