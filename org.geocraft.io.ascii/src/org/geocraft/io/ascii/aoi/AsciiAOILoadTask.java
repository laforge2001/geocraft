package org.geocraft.io.ascii.aoi;


import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;


public class AsciiAOILoadTask extends ImportTask {

  /** The mapper model. */
  protected AsciiAOIMapperModel _mapperModel;

  /**
   * The default constructor.
   */
  public AsciiAOILoadTask() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    _mapperModel = (AsciiAOIMapperModel) mapperModel;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, final IRepository repository) throws CoreException {
    if (_mapperModel == null) {
      throw new RuntimeException("The task for loading the ASCII AOI has not been initialized.");
    }

    // Begin the task.
    String aoiName = _mapperModel.getFileName();
    String fileName = aoiName + AsciiAOIMapperModel.AOI_FILE_EXTN;
    monitor.beginTask("Loading ASCII AOI: " + fileName, 4);

    // Create the ASCII AOI mapper.
    monitor.subTask("Creating datastore mapper");
    IMapper aoiMapper = new AsciiAOIMapper(_mapperModel);
    monitor.worked(1);

    // Create the AOI entity.
    monitor.subTask("Creating AOI entity");
    String aoiID = aoiMapper.getUniqueID();
    String aoiType = _mapperModel.getAOIType();
    AreaOfInterest aoi = null;
    if (aoiType.equals(AsciiAOIConstants.MAP_POLYGON_AOI)) {
      aoi = new MapPolygonAOI(aoiName, aoiMapper);
    } else if (aoiType.equals(AsciiAOIConstants.INLINE_XLINE_AOI)) {
      aoi = new SeismicSurvey3dAOI(aoiName, aoiMapper);
    } else if (aoiType.equals(AsciiAOIConstants.LINE_SHOTPOINT_AOI)) {
      aoi = new SeismicSurvey2dAOI(aoiName, aoiMapper);
    } else {
      throw new CoreException(new Status(IStatus.ERROR, "com.cop.spark.io.ascii", "AOI type not supported: " + aoiType));
    }
    monitor.worked(1);

    // Check if AOI already exists in repository.
    monitor.subTask("Checking repository");
    ISpecification filter = new TypeSpecification(MapPolygonAOI.class);
    Map<String, Object> map = repository.get(filter);
    for (Object object : map.values()) {
      MapPolygonAOI temp = (MapPolygonAOI) object;
      if (aoiID.equals(temp.getUniqueID())) {
        monitor.done();
        throw new CoreException(new Status(IStatus.ERROR, "com.cop.spark.io.ascii", getAlreadyExistsErrorMessage(aoi)));
      }
    }
    monitor.worked(1);

    // Add the AOI to the repository.
    monitor.subTask("Adding AOI to repository");
    repository.add(aoi);
    monitor.worked(1);

    // Task is done.
    monitor.done();
  }
}
