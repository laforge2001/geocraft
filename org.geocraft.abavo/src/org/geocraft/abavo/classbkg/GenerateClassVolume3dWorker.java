/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.classbkg;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.abavo.input.InputProcess3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public class GenerateClassVolume3dWorker extends ABavoAlgorithm3dWorker {

  private ClassificationProcess _classificationProcess;

  private OutputPostStack3dProcess _outputProcess;

  public GenerateClassVolume3dWorker(final int workerID, final IProgressMonitor monitor, final ILogger logger, final IRepository repository, final InputProcess3d inputProcess, final ClassificationProcess classificationProcess, final OutputPostStack3dProcess outputProcess) {
    super(workerID, monitor, logger, repository, inputProcess);
    _classificationProcess = classificationProcess;
    _outputProcess = outputProcess;
  }

  public void run() {
    _monitor.beginTask("", 100);

    while (!_inputProcess.isDone()) {
      TraceData[] traceDataIn = _inputProcess.process();

      if (traceDataIn.length == 0) {
        break;
      }
      TraceData[] traceDataOut = _classificationProcess.process(traceDataIn);
      for (TraceData traceData : traceDataOut) {
        for (Trace trace : traceData.getTraces()) {
          int numSamples = trace.getNumSamples();
          if (numSamples > 0) {
            float[] data = trace.getDataReference();
            if (data[0] == 0f) {
              data[0] = 0.01f;
            }
            if (data[numSamples - 1] == 0f) {
              data[numSamples - 1] = 0.01f;
            }
          }
        }
      }
      _outputProcess.process(traceDataOut);

      if (_monitor.isCanceled()) {
        break;
      }
    }
  }

  @Override
  public void cleanup() {
    // Cleanup the classification process.
    // The output process will be cleaned up by the parent algorithm.
    _inputProcess.cleanup();
    _classificationProcess.cleanup();
  }

}
