/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.abavo.ABavoBaseAlgorithm3d;
import org.geocraft.abavo.classbkg.ABavoAlgorithm3dWorker;
import org.geocraft.abavo.input.InputProcess3d;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.abavo.ABavoCrossplotRegistry;
import org.geocraft.math.regression.RegressionType;
import org.geocraft.ui.form2.FormSection;


public class CrossplotAvsB3d extends ABavoBaseAlgorithm3d {

  private CrossplotSeriesProcess _crossplotProcess;

  public CrossplotAvsB3d() {
    super(false);
  }

  @Override
  protected String getTaskName() {
    return "Crossplot A vs B";
  }

  @Override
  protected void addVolumeFields(FormSection section) {
    section.addEntityComboField(_volumeA, PostStack3d.class);
    section.addEntityComboField(_volumeB, PostStack3d.class);
  }

  @Override
  protected void initialize(IRepository repository) {
    super.initialize(repository);

    IABavoCrossplot crossplot = ABavoCrossplotRegistry.get().getCrossplots()[0];
    Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    Unit zUnit = getVolumeA().getZUnit();
    Domain domain = getVolumeA().getZDomain();

    // Create the crossplot series process.
    RegressionType regressionType = RegressionType.Origin;
    if (!crossplot.getModel().getAnchoredToOrigin()) {
      regressionType = RegressionType.Offset;
    }
    _crossplotProcess = new CrossplotSeriesProcess(crossplot, xyUnit, zUnit, domain, regressionType);
  }

  @Override
  public void cleanup() {
    super.cleanup();
    if (_crossplotProcess != null) {
      _crossplotProcess.cleanup();
    }
  }

  @Override
  protected void processTraceData(TraceData[] traceData) {
    // Crossplot the trace data.
    _crossplotProcess.process(traceData);
  }

  @Override
  protected ABavoAlgorithm3dWorker createWorker(final int workerID, final IProgressMonitor monitor,
      final ILogger logger, final IRepository repository, final InputProcess3d inputProcess) {
    CrossplotAvsB3dWorker worker = new CrossplotAvsB3dWorker(workerID, monitor, logger, repository, inputProcess,
        _crossplotProcess);
    return worker;
  }

  @Override
  public int getNumWorkerThreads() {
    return 1;
  }
}
