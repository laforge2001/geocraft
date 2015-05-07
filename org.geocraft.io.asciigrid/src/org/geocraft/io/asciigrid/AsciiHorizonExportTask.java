package org.geocraft.io.asciigrid;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public class AsciiHorizonExportTask extends ExportTask implements AsciiFileConstants {

  /** The grid to export. */
  protected Grid3d _grid;

  /** The mapper model for the grid. */
  protected AsciiHorizonMapperModel _mapperModel;

  /**
   * The default constructor.
   */
  public AsciiHorizonExportTask() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    _mapperModel = (AsciiHorizonMapperModel) mapperModel;
  }

  @Override
  public void setEntity(final Entity entity) {
    _grid = (Grid3d) entity;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) {
    AsciiHorizonMapperModel mapper = _mapperModel;
    if (_grid == null) {
      throw new RuntimeException("The task for exporting as an Ascii file has not been initialized.");
    }
    if (mapper == null) {
      throw new RuntimeException("The task for exporting as an Ascii file has not been initialized.");
    }

    // Unpack the model parameters.
    List<Grid3d> horizons = new ArrayList<Grid3d>();
    horizons.add(_grid);
    int numOfHorizons = mapper.getNumOfHorizons();
    if (numOfHorizons > 1) {
      horizons.add(mapper.getHorizon2());
    }
    if (numOfHorizons > 2) {
      horizons.add(mapper.getHorizon3());
    }
    if (numOfHorizons > 3) {
      horizons.add(mapper.getHorizon4());
    }
    if (numOfHorizons > 4) {
      horizons.add(mapper.getHorizon5());
    }

    AreaOfInterest areaOfInterest = mapper.getAreaOfInterest();

    // Get the directory
    String directory = mapper.getDirectory();

    String outputName = mapper.getFileName();

    AbstractAsciiHorizonWriter writer = new AsciiHorizonWriter(mapper);

    // Initialize parameters
    List<String> stringParameters = new ArrayList<String>();
    List<String> parameterHdrs = new ArrayList<String>();
    String outputFileName = outputName + AsciiFileConstants.TEXT_FILE_EXTN;

    writer.writeHorizonData(horizons, areaOfInterest, directory, outputFileName, stringParameters, parameterHdrs);

    // Task is done.
    monitor.done();
  }

}