package org.geocraft.io.modspec;


import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;


public class GridLoadTask extends ImportTask {

  /** The mapper model. */
  protected GridMapperModel _mapperModel;

  /**
   * The default constructor.
   */
  public GridLoadTask() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    _mapperModel = (GridMapperModel) mapperModel;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    if (_mapperModel == null) {
      throw new RuntimeException("The task for loading the ModSpec grid has not been initialized.");
    }

    // Begin the task.
    String gridName = _mapperModel.getFileName();
    String fileName = gridName + ModSpecGridConstants.GRID_FILE_EXTN;
    monitor.beginTask("Loading ModSpec Grid: " + fileName, 4);

    // Create the ModSpec grid mapper.
    monitor.subTask("Creating datastore mapper");
    GridMapper gridMapper = new GridMapper(_mapperModel);
    monitor.worked(1);

    // Create the grid entity.
    monitor.subTask("Creating grid entity");
    String gridID = gridMapper.getUniqueID();
    Grid3d grid = new Grid3d(gridName, gridMapper);
    monitor.worked(1);

    // Check if grid already exists in repository.
    monitor.subTask("Checking repository");
    ISpecification filter = new TypeSpecification(Grid3d.class);
    Map<String, Object> map = repository.get(filter);
    for (Object object : map.values()) {
      Grid3d temp = (Grid3d) object;
      if (gridID.equals(temp.getUniqueID())) {
        monitor.done();
        throw new CoreException(
            new Status(IStatus.ERROR, "org.geocraft.io.modspec", getAlreadyExistsErrorMessage(grid)));
      }
    }
    monitor.worked(1);

    grid.setDisplayColor(new RGB(255, 0, 0));

    // Add the grid to the repository.
    monitor.subTask("Adding grid to repository");
    repository.add(grid);
    monitor.worked(1);

    // Task is done.
    monitor.done();
  }
}
