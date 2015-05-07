package org.geocraft.io.ascii.aoi;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public class AsciiAOIExportTask extends ExportTask {

  /** The AOI to export. */
  protected AreaOfInterest _aoi;

  /** The mapper model for the AOI. */
  protected AsciiAOIMapperModel _mapperModel;

  /**
   * The default constructor.
   */
  public AsciiAOIExportTask() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    _mapperModel = (AsciiAOIMapperModel) mapperModel;
  }

  @Override
  public void setEntity(final Entity entity) {
    _aoi = (AreaOfInterest) entity;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, final IRepository repository) {
    if (_aoi == null) {
      throw new RuntimeException("The task for exporting as ASCII AOI has not been initialized.");
    }

    if (_aoi instanceof MapPolygonAOI) {
      _mapperModel.setAOIType(AsciiAOIConstants.MAP_POLYGON_AOI);
    } else if (_aoi instanceof SeismicSurvey3dAOI) {
      _mapperModel.setAOIType(AsciiAOIConstants.INLINE_XLINE_AOI);
    } else if (_aoi instanceof SeismicSurvey2dAOI) {
      _mapperModel.setAOIType(AsciiAOIConstants.LINE_SHOTPOINT_AOI);
    }

    if (_mapperModel == null) {
      throw new RuntimeException("The task for exporting as ASCII AOI has not been initialized.");
    }

    // Begin the task.
    String gridName = _aoi.getDisplayName();
    String fileName = _mapperModel.getFileName() + AsciiAOIMapperModel.AOI_FILE_EXTN;
    monitor.beginTask("Exporting as ASCII AOI: " + fileName, 4);

    // Create the mapper.
    monitor.subTask("Creating datastore mapper");
    IMapper mapper = new AsciiAOIMapper(_mapperModel);
    monitor.worked(1);

    try {
      // Delete the entry from the datastore if it already exists.
      if (mapper.existsInStore()) {
        monitor.subTask("Deleting existing file: " + fileName);
        mapper.delete(_aoi);
      }
      monitor.worked(1);

      // Create an entry in the datastore.
      monitor.subTask("Creating output file: " + fileName);
      mapper.create(_aoi);
      monitor.worked(1);

      // Update the entry in the datastore.
      monitor.subTask("Writing output file: " + fileName);
      mapper.update(_aoi);
      monitor.worked(1);

      // Log the exported message.
      logger.info(gridName + " exported to " + _mapperModel.getFilePath() + ".");
    } catch (Exception ex) {
      logger.warn(ex.toString());
    }

    // Task is done.
    monitor.done();
  }

}
