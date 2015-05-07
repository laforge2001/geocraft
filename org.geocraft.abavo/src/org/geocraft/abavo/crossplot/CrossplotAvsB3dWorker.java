/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.abavo.classbkg.ABavoAlgorithm3dWorker;
import org.geocraft.abavo.input.InputProcess3d;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public class CrossplotAvsB3dWorker extends ABavoAlgorithm3dWorker {

  private CrossplotSeriesProcess _crossplotProcess;

  public CrossplotAvsB3dWorker(final int workerID, final IProgressMonitor monitor, final ILogger logger, final IRepository repository, final InputProcess3d inputProcess, final CrossplotSeriesProcess crossplotProcess) {
    super(workerID, monitor, logger, repository, inputProcess);
    _crossplotProcess = crossplotProcess;
  }

  public void run() {
    _monitor.beginTask("", 100);

    while (!_inputProcess.isDone()) {
      TraceData[] traceDataIn = _inputProcess.process();
      if (traceDataIn.length == 0) {
        break;
      }
      _crossplotProcess.process(traceDataIn);

      if (_monitor.isCanceled()) {
        break;
      }
    }
  }

  @Override
  public void cleanup() {
    // Nothing to do.
    // The crossplot process will be cleaned up by the parent algorithm.
    _inputProcess.cleanup();
  }

}
