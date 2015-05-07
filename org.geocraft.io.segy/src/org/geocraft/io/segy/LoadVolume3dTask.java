/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.factory.model.PreStack3dFactory;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;


/**
 * The background task for loading a SEG-Y volume.
 */
public class LoadVolume3dTask extends ImportTask implements SegyTraceIndexerListener {

  /** The SEG-Y volume mapper properties. */
  private Volume3dMapperModel _mapperModel;

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
  public LoadVolume3dTask() {
    // The no argument constructor for OSGI.
  }

  /**
   * Initializes the task.
   * @param mapperModel the model containing the mapper parameters.
   */
  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    if (!mapperModel.getClass().equals(Volume3dMapperModel.class)) {
      throw new IllegalArgumentException("Invalid model class: " + mapperModel.getClass() + ".");
    }
    _mapperModel = (Volume3dMapperModel) mapperModel;
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

    // Check that the volume type is PostStack3d or PreStack3d.
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
          "Only PostStack2d, PostStack3d and PreStack3d volumes are currently supported for SEG-Y format.");
    }
    monitor.worked(1);
    monitor.done();
  }

  public void tracesIndexed(final VolumeMapperModel mapperModel, final SegyTraceIndexModel traceIndexModel) {

    // Remove the listener if the trace indexer was created.
    if (_traceIndexer != null) {
      _traceIndexer.removeListener(this);
    }

    // Create the PostStack3d or PreStack3d volume.
    IndexType type = mapperModel.getVolumeType();
    switch (type) {
      case POSTSTACK_3D:
        createPostStack3d((Volume3dMapperModel) mapperModel, _monitor, _repository);
        break;
      case PRESTACK_3D:
        createPreStack3d((Volume3dMapperModel) mapperModel, _monitor, _repository);
        break;
      default:
        throw new RuntimeException("Only PostStack3d and PreStack3d are currently supported for SEG-Y 3D.");
    }
  }

  private void createPreStack3d(final Volume3dMapperModel mapperModel, IProgressMonitor monitor, IRepository repository) {

    // Create the SEG-Y mapper.
    PreStack3dMapper mapper = new PreStack3dMapper(mapperModel);

    // Create the prestack3d entity.
    PreStack3d ps3d = PreStack3dFactory.create(mapperModel.getFileName(), mapper);

    // Check if the prestack3d entity already exists in the repository.
    ISpecification filter = new TypeSpecification(PreStack3d.class);
    Map<String, Object> map = repository.get(filter);
    Collection<Object> objects = map.values();
    for (Object object : objects) {
      PreStack3d temp = (PreStack3d) object;
      if (temp.getUniqueID().equals(ps3d.getUniqueID())) {
        monitor.done();
        throw new RuntimeException(getAlreadyExistsErrorMessage(ps3d));
      }
    }

    repository.add(ps3d);
  }

  private void createPostStack3d(final Volume3dMapperModel mapperModel, IProgressMonitor monitor, IRepository repository) {

    // Create the SEG-Y mapper.
    PostStack3dMapper mapper = new PostStack3dMapper(mapperModel);

    // Create the poststack3d entity.
    PostStack3d ps3d = PostStack3dFactory.create(mapperModel.getFileName(), mapper);

    // Check if the poststack3d entity already exists in the repository.
    ISpecification filter = new TypeSpecification(PostStack3d.class);
    Map<String, Object> map = repository.get(filter);
    Collection<Object> objects = map.values();
    for (Object object : objects) {
      PostStack3d temp = (PostStack3d) object;
      if (temp.getUniqueID().equals(ps3d.getUniqueID())) {
        monitor.done();
        throw new RuntimeException(getAlreadyExistsErrorMessage(ps3d));
      }
    }

    repository.add(ps3d);
  }
}
