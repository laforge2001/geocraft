package org.geocraft.io.segy;


import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.factory.model.PostStack2dFactory;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.ISeismicLineCoordinateTransform;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SimpleSeismicLineCoordinateTransform;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;


public class LoadVolume2dTask extends ImportTask implements SegyTraceIndexerListener {

  /** The SEG-Y volume mapper properties. */
  private Volume2dMapperModel _mapperModel;

  /** The progress monitor. */
  private IProgressMonitor _monitor;

  /** The repository. */
  private IRepository _repository;

  /** The SEG-Y trace indexer. */
  private SegyTraceIndexer _traceIndexer;

  /**
   * The default constructor.
   * This is a no-argument constructor due to its creation via eclipse extension points.
   */
  public LoadVolume2dTask() {
    // The no argument constructor for OSGI.
  }

  /**
   * Initializes the task.
   * @param mapperModel the model containing the mapper parameters.
   */
  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    if (!mapperModel.getClass().equals(Volume2dMapperModel.class)) {
      throw new IllegalArgumentException("Invalid model class: " + mapperModel.getClass() + ".");
    }
    _mapperModel = (Volume2dMapperModel) mapperModel;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) {
    if (_mapperModel == null) {
      throw new RuntimeException("The task for loading the SEG-Y volume has not been initialized.");
    }

    _monitor = monitor;
    _repository = repository;

    // Begin the task.
    String fileName = _mapperModel.getFileName();
    String fileExtn = _mapperModel.getFileExtension();
    monitor.beginTask("Loading SEG-Y volume: " + fileName + fileExtn, 2);
    monitor.worked(1);

    // Check that the volume type is PostStack2d or PreStack2d.
    IndexType volumeType = _mapperModel.getVolumeType();
    if (volumeType.equals(IndexType.POSTSTACK_2D) || volumeType.equals(IndexType.POSTSTACK_3D)
        || volumeType.equals(IndexType.PRESTACK_3D)) {
      String outputFilePath = _mapperModel.getDirectory();
      outputFilePath += File.separator;
      outputFilePath += _mapperModel.getFileName();
      outputFilePath += _mapperModel.getFileExtension();

      // Check if the SEG-Y file needs indexing.
      if (SegyUtil.needIndexing(outputFilePath)) {
        monitor.subTask("Creating trace index");
        // If so, start an indexing task.
        _traceIndexer = new SegyTraceIndexer(_mapperModel, volumeType);
        _traceIndexer.addListener(this);
        TaskRunner.runTask(_traceIndexer, "SEG-Y Trace Indexer");
      } else {
        // If not, then continue on.
        tracesIndexed(_mapperModel, null);
      }
    } else {
      throw new RuntimeException(
          "Only PostStack2d, PostStack2d and PreStack2d volumes are currently supported for SEG-Y format.");
    }
    monitor.worked(1);
    monitor.done();
  }

  public void tracesIndexed(final VolumeMapperModel mapperModel, final SegyTraceIndexModel traceIndexModel) {

    // Remove the listener if the trace indexer was created.
    if (_traceIndexer != null) {
      _traceIndexer.removeListener(this);
    }

    // Create the PostStack2d or PreStack2d volume.
    IndexType type = mapperModel.getVolumeType();
    switch (type) {
      case POSTSTACK_2D:
        try {
          createPostStack2d((Volume2dMapperModel) mapperModel, _monitor, _repository);
        } catch (Exception ex) {
          throw new RuntimeException(ex.getMessage(), ex);
        }
        break;
      default:
        throw new RuntimeException("Only PostStack2d and PreStack2d are currently supported for SEG-Y 2D.");
    }
  }

  private void createPostStack2d(final Volume2dMapperModel mapperModel, IProgressMonitor monitor, IRepository repository) throws Exception {

    // Create the SEG-Y mapper.
    PostStack2dLineMapper mapper = new PostStack2dLineMapper(mapperModel, true);
    SegyTraceIndex traceIndex = mapper.getTraceIndex();

    int lineNumber = 1;
    String lineName = "Line " + lineNumber;
    FloatRange cdpRange = new FloatRange(traceIndex.getTraceKeyMin(0), traceIndex.getTraceKeyMax(0),
        traceIndex.getTraceKeyInc(0));
    FloatRange shotpointRange = new FloatRange(cdpRange.getStart(), cdpRange.getEnd(), cdpRange.getDelta());
    String filePath = mapperModel.getFilePath();
    CoordinateSeries xyCoordinates = PostStack2dLineMapper.getCoordinateSeries(traceIndex,
        mapperModel.getXcoordByteLoc(), mapperModel.getYcoordByteLoc(), true, filePath, mapperModel.getUnitOfXY(),
        mapperModel.getUnitOfZ().getDomain());
    ISeismicLineCoordinateTransform coordTransform = new SimpleSeismicLineCoordinateTransform(cdpRange, shotpointRange);

    SeismicLine2d[] seismicLines = new SeismicLine2d[1];
    seismicLines[0] = new SeismicLine2d(lineName, lineNumber, cdpRange, shotpointRange.getStart(),
        shotpointRange.getEnd(), xyCoordinates, coordTransform);
    SeismicSurvey2d seismicSurvey = new SeismicSurvey2d(mapperModel.getFileName(), seismicLines);

    // Create the poststack2d entity.
    PostStack2d ps2d = PostStack2dFactory.createInMemory(mapperModel.getFileName(), seismicSurvey, mapperModel
        .getUnitOfZ().getDomain());
    PostStack2dLine poststackLine = new PostStack2dLine(lineName + ":" + mapperModel.getFileName(), mapper,
        seismicSurvey, lineName, lineNumber, ps2d);
    ps2d.addPostStack2dLine(lineNumber, poststackLine);

    // Check if the poststack2d entity already exists in the repository.
    ISpecification filter = new TypeSpecification(PostStack2d.class);
    Map<String, Object> map = repository.get(filter);
    Collection<Object> objects = map.values();
    for (Object object : objects) {
      PostStack2d temp = (PostStack2d) object;
      if (temp.getUniqueID().equals(ps2d.getUniqueID())) {
        monitor.done();
        throw new RuntimeException(getAlreadyExistsErrorMessage(ps2d));
      }
    }

    repository.add(ps2d);
    repository.add(poststackLine);
  }
}
