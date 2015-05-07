package org.geocraft.io.javaseis;


import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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


public class VolumeLoadTask extends ImportTask {

  /** The JavaSeis volume mapper properties. */
  private VolumeMapperModel _mapperModel;

  /**
   * The default constructor.
   * This is a no-argument constructor due to its creation via eclipse extension points.
   */
  public VolumeLoadTask() {
    // The no argument constructor for OSGI.
  }

  /**
   * Initializes the task.
   * @param mapperModel the model containing the mapper parameters.
   */
  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    _mapperModel = (VolumeMapperModel) mapperModel;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    // Check if the task has been initialized.
    if (_mapperModel == null) {
      throw new RuntimeException("The task for loading the JavaSeis volume has not been initialized.");
    }

    // Begin the task.
    String volumeName = _mapperModel.getFileName();
    monitor.beginTask("Loading JavaSeis volume: " + volumeName, 4);

    // Check that the volume type is PostStack3d or PreStack3d.
    String volumeType = _mapperModel.getVolumeType();
    if (volumeType.equalsIgnoreCase("PostStack3d")) {
      String outputFilePath = _mapperModel.getDirectory();
      outputFilePath += File.separator;
      outputFilePath += _mapperModel.getFileName();
      loadPostStack3d(logger, monitor, repository);
    } else if (volumeType.equalsIgnoreCase("PreStack3d")) {
      String outputFilePath = _mapperModel.getDirectory();
      outputFilePath += File.separator;
      outputFilePath += _mapperModel.getFileName();
      loadPreStack3d(logger, monitor, repository);
    } else {
      throw new RuntimeException("Only PostStack3d and PreStack3d volumes are currently supported for JavaSeis format.");
    }
  }

  private void loadPostStack3d(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    // Create the JavaSeis mapper.
    monitor.subTask("Creating volume mapper");
    PostStack3dMapper mapper = new PostStack3dMapper(_mapperModel);
    monitor.worked(1);

    // Create the PostStack3d entity.
    monitor.subTask("Creating volume entity");
    PostStack3d ps3d = PostStack3dFactory.create(_mapperModel.getFileName(), mapper);
    monitor.worked(1);

    // Check if the poststack3d entity already exists in the repository.
    monitor.subTask("Checking repository");
    ISpecification filter = new TypeSpecification(PostStack3d.class);
    Map<String, Object> map = repository.get(filter);
    for (Object object : map.values()) {
      PostStack3d temp = (PostStack3d) object;
      if (temp.getUniqueID().equals(ps3d.getUniqueID())) {
        monitor.done();
        throw new CoreException(new Status(IStatus.ERROR, "org.geocraft.io.segy", getAlreadyExistsErrorMessage(ps3d)));
      }
    }
    monitor.worked(1);

    // Add the volume to the repository.
    monitor.subTask("Adding volume to repository");
    repository.add(ps3d);
    monitor.worked(1);

    // Task is done.
    monitor.done();
  }

  private void loadPreStack3d(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    // Create the JavaSeis mapper.
    monitor.subTask("Creating datastore mapper");
    PreStack3dMapper mapper = new PreStack3dMapper(_mapperModel);
    monitor.worked(1);

    // Create the PreStack3d entity.
    monitor.subTask("Creating volume entity");
    PreStack3d ps3d = PreStack3dFactory.create(_mapperModel.getFileName(), mapper);
    monitor.worked(1);

    // Check if the prestack3d entity already exists in the repository.
    monitor.subTask("Checking repository");
    ISpecification filter = new TypeSpecification(PreStack3d.class);
    Map<String, Object> map = repository.get(filter);
    for (Object object : map.values()) {
      PreStack3d temp = (PreStack3d) object;
      if (temp.getUniqueID().equals(ps3d.getUniqueID())) {
        String errorMessage = "Volume already exists in repository: " + ps3d.getDisplayName();
        Status status = new Status(IStatus.ERROR, "org.geocraft.io.segy", errorMessage.toString());
        monitor.done();
        throw new CoreException(status);
      }
    }
    monitor.worked(1);

    // Add the volume to the repository.
    monitor.subTask("Adding volume to repository");
    repository.add(ps3d);
    monitor.worked(1);

    // Task us dibe,
    monitor.done();
  }

}
