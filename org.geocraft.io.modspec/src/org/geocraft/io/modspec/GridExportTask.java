package org.geocraft.io.modspec;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;


public class GridExportTask extends ExportTask implements ModSpecGridConstants {

  /** The grid to export. */
  protected Grid3d _grid;

  /** The mapper model for the grid. */
  protected GridMapperModel _mapperModel;

  /**
   * The default constructor.
   */
  public GridExportTask() {
    // The no argument constructor for OSGI.
  }

  public void setMapperModel(final MapperModel mapperModel) {
    _mapperModel = (GridMapperModel) mapperModel;
  }

  public void setEntity(final Entity entity) {
    _grid = (Grid3d) entity;
  }

  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) {
    if (_grid == null) {
      throw new RuntimeException("The task for exporting as ModSpec grid has not been initialized.");
    }
    if (_mapperModel == null) {
      throw new RuntimeException("The task for exporting as ModSpec grid has not been initialized.");
    }

    // Begin the task.
    String gridName = _grid.getDisplayName();
    String fileName = _mapperModel.getFileName() + ModSpecGridConstants.GRID_FILE_EXTN;
    monitor.beginTask("Exporting as ModSpec Grid: " + fileName, 4);

    // Create the mapper.
    monitor.subTask("Creating datastore mapper");
    IMapper mapper = new GridMapper(_mapperModel);
    monitor.worked(1);

    try {
      // Delete the entry from the datastore if it already exists.
      if (mapper.existsInStore()) {
        monitor.subTask("Deleting existing file: " + fileName);
        mapper.delete(_grid);
      }
      monitor.worked(1);

      // Create an entry in the datastore.
      monitor.subTask("Creating output file: " + fileName);
      mapper.create(_grid);
      monitor.worked(1);

      // Update the entry in the datastore.
      monitor.subTask("Writing output file: " + fileName);
      mapper.update(_grid);
      monitor.worked(1);

      // Log the exported message.
      logger.info(gridName + " exported.");
    } catch (Exception ex) {
      logger.warn(ex.toString());
    }

    // Task is done.
    monitor.done();
  }

}