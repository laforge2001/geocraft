/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.geocraft.abavo.ABavoBaseAlgorithm2d;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.repository.IRepository;
import org.geocraft.internal.abavo.ABavoCrossplotRegistry;
import org.geocraft.math.regression.RegressionType;


public class CrossplotAvsB2d extends ABavoBaseAlgorithm2d {

  private CrossplotSeriesProcess _crossplotProcess;

  public CrossplotAvsB2d() {
    super(false, false);
  }

  @Override
  protected String getTaskName() {
    return "Crossplot A vs B";
  }

  @Override
  protected void initialize(IRepository repository, final String lineName) {
    super.initialize(repository, lineName);

    IABavoCrossplot crossplot = ABavoCrossplotRegistry.get().getCrossplots()[0];
    Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    Unit zUnit = getVolumeA().getPostStack2dLine(lineName).getZUnit();
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
  protected void processTraceData(TraceData[] traceData, final String lineName) {
    // Crossplot the trace data.
    _crossplotProcess.process(traceData);
  }
}
