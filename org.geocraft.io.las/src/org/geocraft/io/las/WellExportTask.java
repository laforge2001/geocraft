package org.geocraft.io.las;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.FloatMeasurementSeries;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public class WellExportTask extends ExportTask {

  private Well _well;

  private WellMapperModel _mapperModel;

  public WellExportTask() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void setEntity(final Entity entity) {
    _well = (Well) entity;

  }

  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    _mapperModel = (WellMapperModel) mapperModel;

  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, final IRepository repository) throws CoreException {
    if (_well == null) {
      throw new RuntimeException("The task for exporting as LAS has not been initialized.");
    }
    if (_mapperModel == null) {
      throw new RuntimeException("The task for exporting as LAS has not been initialized.");
    }

    Map<String, FloatMeasurementSeries> wellLogTraces = createWellLogTraceMap();
    // FIXME: WellFactory.getInstance().create(mapper, name, identifier, identifierType)
    // FIXME: WellBoreFactory.create(repository, _wellBore, _wellBore.getDisplayName(), new WellBoreMapper(_mapperModel),
    // FIXME:    wellLogTraces);
    logger.info(_well.getDisplayName() + " exported.");
    // Task is done.
    monitor.done();

  }

  private Map<String, FloatMeasurementSeries> createWellLogTraceMap() {
    WellLogTrace[] logTraces = _well.getWellLogTraces();
    Map<String, FloatMeasurementSeries> wellLogTraces = new HashMap<String, FloatMeasurementSeries>();
    for (WellLogTrace trace : logTraces) {
      wellLogTraces.put(trace.getDisplayName(), new FloatMeasurementSeries(trace.getTraceData(), trace.getDataUnit()));
    }
    return wellLogTraces;
  }

}
