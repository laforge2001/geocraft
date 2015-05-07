/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.aplusb;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.abavo.classbkg.ABavoAlgorithm3dWorker;
import org.geocraft.abavo.classbkg.OutputPostStack3dProcess;
import org.geocraft.abavo.input.InputProcess3d;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public class GenerateAplusBVolume3dWorker extends ABavoAlgorithm3dWorker {

  private AplusBProcess _aplusbProcess;

  private OutputPostStack3dProcess _outputProcess;

  public GenerateAplusBVolume3dWorker(final int workerID, final IProgressMonitor monitor, final ILogger logger, final IRepository repository, final InputProcess3d inputProcess, final AplusBProcess aplusbProcess, final OutputPostStack3dProcess outputProcess) {
    super(workerID, monitor, logger, repository, inputProcess);
    _aplusbProcess = aplusbProcess;
    _outputProcess = outputProcess;
  }

  public void run() {
    _monitor.beginTask("", 100);

    while (!_inputProcess.isDone()) {
      TraceData[] traceDataIn = _inputProcess.process();

      if (traceDataIn.length == 0) {
        break;
      }
      TraceData[] traceDataOut = _aplusbProcess.process(traceDataIn);
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
    _aplusbProcess.cleanup();
  }
}
