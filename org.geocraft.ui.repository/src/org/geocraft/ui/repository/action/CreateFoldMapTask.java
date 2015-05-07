/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.repository.action;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.util.TraceIterator;
import org.geocraft.io.util.TraceIteratorFactory;


/**
 * The task for creating a fold map of traces in a seismic volume.
 * The fold map is represented as a grid entity, with the values in
 * each cell representing the number of traces existing in the volume
 * at that location. At present only <code>PostStack3d</code> and
 * <code>PreStack3d</code> volumes are supported.
 */
public class CreateFoldMapTask extends BackgroundTask {

  /** The seismic dataset for which to create the fold map. */
  private SeismicDataset _dataset;

  public CreateFoldMapTask(SeismicDataset dataset) {
    _dataset = dataset;
  }

  @Override
  public Object compute(ILogger logger, IProgressMonitor monitor) {
    IRepository repository = ServiceProvider.getRepository();
    if (repository == null) {
      logger.error("Could not find repository.");
      monitor.done();
      return null;
    }
    if (_dataset instanceof PostStack3d) {
      createFoldMapForPostStack3d((PostStack3d) _dataset, logger, monitor, repository);
    } else if (_dataset instanceof PreStack3d) {
      createFoldMapForPreStack3d((PreStack3d) _dataset, logger, monitor, repository);
    }
    return null;
  }

  /**
   * Constructs a fold map for a 3D poststack volume.
   * @param ps3d the 3D poststack volume.
   * @param logger the logger.
   * @param monitor the progress monitor.
   * @param repository the repository in which to put the fold map.
   */
  private void createFoldMapForPostStack3d(PostStack3d ps3d, ILogger logger, IProgressMonitor monitor,
      IRepository repository) {
    monitor.beginTask("Creating fold map for " + ps3d.getDisplayName(), 100);

    SeismicSurvey3d geometry = ps3d.getSurvey();
    int numRows = geometry.getNumRows();
    int numCols = geometry.getNumColumns();
    float[][] foldValues = new float[numRows][numCols];
    TraceIterator iterator = TraceIteratorFactory.create(ps3d);
    int previousWork = 0;
    while (iterator.hasNext()) {
      TraceData traceData = iterator.next();
      Trace[] traces = traceData.getTraces();
      for (int i = 0; i < traces.length; i++) {
        if (!traces[i].isMissing()) {
          double x = traces[i].getX();
          double y = traces[i].getY();
          double[] rowcol = geometry.transformXYToRowCol(x, y, true);
          int row = (int) (rowcol[0] + 0.5);
          int col = (int) (rowcol[1] + 0.5);
          foldValues[row][col]++;
        }
      }
      int currentWork = (int) iterator.getCompletion();
      int work = currentWork - previousWork;
      monitor.worked(work);
      previousWork = currentWork;
    }
    Grid3d foldMap = Grid3dFactory.createInMemory(ps3d.getDisplayName() + "-FoldMap", geometry, Unit.UNDEFINED,
        foldValues, 0f);
    repository.add(foldMap);

    monitor.done();
    return;
  }

  /**
   * Constructs a fold map for a 3D prestack volume.
   * @param ps3d the 3D prestack volume.
   * @param logger the logger.
   * @param monitor the progress monitor.
   * @param repository the repository in which to put the fold map.
   */
  private void createFoldMapForPreStack3d(PreStack3d ps3d, ILogger logger, IProgressMonitor monitor,
      IRepository repository) {
    monitor.beginTask("Creating fold map for " + ps3d.getDisplayName(), 100);

    SeismicSurvey3d geometry = ps3d.getSurvey();
    int numRows = geometry.getNumRows();
    int numCols = geometry.getNumColumns();
    float[][] foldValues = new float[numRows][numCols];
    TraceIterator iterator = TraceIteratorFactory.create(ps3d);
    int previousWork = 0;
    while (iterator.hasNext()) {
      TraceData traceData = iterator.next();
      Trace[] traces = traceData.getTraces();
      for (int i = 0; i < traces.length; i++) {
        if (!traces[i].isMissing()) {
          double x = traces[i].getX();
          double y = traces[i].getY();
          double[] rowcol = geometry.transformXYToRowCol(x, y, true);
          int row = (int) (rowcol[0] + 0.5);
          int col = (int) (rowcol[1] + 0.5);
          foldValues[row][col]++;
        }
      }
      int currentWork = (int) iterator.getCompletion();
      int work = currentWork - previousWork;
      monitor.worked(work);
      previousWork = currentWork;
    }
    Grid3d foldMap = Grid3dFactory.createInMemory(ps3d.getDisplayName() + "-FoldMap", geometry, Unit.UNDEFINED,
        foldValues, 0f);
    repository.add(foldMap);

    monitor.done();
    return;
  }
}
