/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.asciiexport;


import java.util.List;

import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.geomath.algorithm.util.parameters.IParameter;


/**
 * The interface for the various ASCII export strategies.
 */
public interface IAsciiExportStrategy {

  /**
   * Returns the file extension for the export strategy.
   * @return the file extension for the export strategy.
   */
  String getFileExtension();

  /**
   * Returns the array of available/required global header keywords. If an array of size zero
   * is returned, then the keywords are arbitrary and can be anything.
   * @return the array of available/required global header keywords.
   */
  IParameter[] getGlobalHeaderParameters();

  /**
   * Sets the global headers based on the specified parameters.
   * @param globalHeaderParameters to global header parameters.
   */
  void setGlobalHeaders(List<IParameter> globalHeaderParameters);

  /**
   * Writes the global header records to the AVF file.
   */
  String formatGlobalHeaders();

  /**
   * Formats the specified trace to the output file using the specified ASCII format.
   * @param traces the trace.
   * @param traceNo the sequential trace number.
   */
  String formatTraceFunction(Trace trace, int traceNo, int sampleDecimation, float gridValue);
}
