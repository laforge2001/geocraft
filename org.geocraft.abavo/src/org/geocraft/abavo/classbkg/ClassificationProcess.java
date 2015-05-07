/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.classbkg;


import java.util.Arrays;

import org.geocraft.abavo.defs.ABavoDataMode;
import org.geocraft.abavo.defs.ClassificationMethod;
import org.geocraft.abavo.input.AbstractInputProcess;
import org.geocraft.abavo.process.ITraceProcess;
import org.geocraft.abavo.process.MaskFloatArray;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;


/**
 * This class is a process that can be added to an <code>ABavoBaseTask</code>
 * to perform classification of trace samples based on defined regions.
 * Trace samples are evaluated by means of an <code>IRegionsClassifier</code>,
 * which is supplied to the constructor. The regions classifier returns
 * a classification value based on the supplied A,B values from the
 * traces passed in. The input traces (which must be A first, B second)
 * are discarded, and new traces of classification values are returned.
 */
public class ClassificationProcess implements ITraceProcess {

  /** The regions classifier. */
  private final IRegionsClassifier _classifier;

  private final ClassificationMethod _method;

  private boolean _classifyBySample;

  private boolean _classifyByPeakTrough;

  /**
   * Constructs a classification process with the specified
   * regions classifier.
   * @param classifier the regions classifier.
   */
  public ClassificationProcess(final IRegionsClassifier classifier, ClassificationMethod method) {
    _classifier = classifier;
    _method = method;
    switch (method) {
      case CLASS_OF_SAMPLE:
        _classifyBySample = true;
        _classifyByPeakTrough = false;
        break;
      case CLASS_OF_PEAK_TROUGH:
        _classifyBySample = false;
        _classifyByPeakTrough = true;
        break;
    }
  }

  public ClassificationProcess(ClassificationProcess classificationProcess) {
    this(classificationProcess._classifier, classificationProcess._method);
  }

  public String getName() {
    return _classifier.getName();
  }

  public void initialize() {
    // No action required.
  }

  public void cleanup() {
    // No action required.
  }

  public TraceData[] process(final TraceData[] traceDataIn) {
    // If the trace data array size is zero, simple return.
    if (traceDataIn.length <= 0) {
      return traceDataIn;
    }

    // If the trace data array size is not exactly 4, throw an exception.
    if (traceDataIn.length != 2) {
      throw new RuntimeException("Number of input trace data objects must be 2!");
    }

    // The 1st trace data is A.
    TraceData traceDataA = traceDataIn[AbstractInputProcess.PRE_PROCESSES_A_TRACE];

    // The 2nd trace data is B.
    TraceData traceDataB = traceDataIn[AbstractInputProcess.PRE_PROCESSES_B_TRACE];

    // Check that the number of traces match.
    if (traceDataA.getNumTraces() != traceDataB.getNumTraces()) {
      throw new RuntimeException("Number of input traces do not match!");
    }

    // Allocate an output array of traces.
    Trace[] tracesC = new Trace[traceDataA.getNumTraces()];
    for (int i = 0; i < traceDataA.getNumTraces(); i++) {
      Trace traceA = traceDataA.getTrace(i);
      Trace traceB = traceDataB.getTrace(i);
      float[] a = traceA.getData();
      float[] b = traceB.getData();
      float[] ptMask = Arrays.copyOf(a, a.length);
      MaskFloatArray.process(ptMask, ABavoDataMode.PEAKS_AND_TROUGHS);
      int[] ptBlockMask = MaskFloatArray.maskPeaksAndTroughsBlock(a);
      float[] c = new float[a.length];
      // Loop thru each sample.
      for (int k = 0; k < traceA.getNumSamples(); k++) {
        boolean isPeakTrough = ptMask[k] < 0 || ptMask[k] > 0;
        if (_classifyBySample || _classifyByPeakTrough && isPeakTrough) {
          // Get the classification value of each sample.
          double classValue = _classifier.processAB(a[k], b[k]);
          if (Double.isNaN(classValue)) {
            classValue = 0;
          }
          c[k] = (float) classValue;
          if (_classifyByPeakTrough) {
            int ptFlag = ptBlockMask[k];
            for (int j = k - 1; j >= 0; j--) {
              if (ptBlockMask[j] != ptFlag) {
                break;
              }
              c[j] = (float) classValue;
            }
            for (int j = k + 1; j < traceA.getNumSamples(); j++) {
              if (ptBlockMask[j] != ptFlag) {
                break;
              }
              c[j] = (float) classValue;
            }
          }
        }
      }

      // Populate the array of output traces.
      tracesC[i] = new Trace(traceA, c);
    }

    // Return the output trace data.
    TraceData[] traceDataOut = new TraceData[1];
    traceDataOut[0] = new TraceData(tracesC);
    return traceDataOut;
  }

}
