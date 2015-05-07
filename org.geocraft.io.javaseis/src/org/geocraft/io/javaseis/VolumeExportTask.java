package org.geocraft.io.javaseis;


import java.io.IOException;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.util.TraceIterator;
import org.geocraft.io.util.TraceIteratorFactory;


public class VolumeExportTask extends ExportTask {

  /** The seismic dataset to export. */
  private SeismicDataset _volume;

  /** The JavaSeis volume mapper properties. */
  private VolumeMapperModel _model;

  /**
   * The default constructor.
   * This is a no-argument constructor due to its creation via eclipse extension points.
   */
  public VolumeExportTask() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void setEntity(final Entity entity) {
    _volume = (SeismicDataset) entity;
  }

  @Override
  public void setMapperModel(final MapperModel model) {
    _model = (VolumeMapperModel) model;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    if (_volume == null || _model == null) {
      throw new RuntimeException("The task for exporting as JavaSeis volume has not been initialized.");
    }
    if (_volume instanceof PostStack3d) {
      exportAsPostStack3d((PostStack3d) _volume, logger, monitor);
    } else if (_volume instanceof PreStack3d) {
      exportAsPreStack3d((PreStack3d) _volume, logger, monitor);
    }
  }

  /**
   * The internal method for exporting the a poststack3d volume as JavaSeis.
   * @param ps3d the poststack3d volume.
   * @param monitor the progress monitor.
   * @throws CoreException thrown on create/write errors.
   */
  private void exportAsPostStack3d(final PostStack3d ps3d, final ILogger logger, final IProgressMonitor monitor) throws CoreException {
    // Begin the task.
    String volumeName = ps3d.getDisplayName();
    String fileName = _model.getFileName();
    monitor.beginTask("Exporting as JavaSeis Volume: " + fileName, 100);

    // Create a JavaSeis PostStack3d mapper.
    PostStack3dMapper mapper = new PostStack3dMapper(_model);
    try {
      mapper.create(ps3d);

      int oldWork = 0;
      TraceIterator iterator = TraceIteratorFactory.create(ps3d);
      while (iterator.hasNext()) {
        TraceData traceData = iterator.next();
        mapper.putTraces(ps3d, traceData);
        int completion = (int) iterator.getCompletion();
        int work = completion - oldWork;
        oldWork = completion;
        monitor.worked(work);
        monitor.subTask(iterator.getMessage());
        if (monitor.isCanceled()) {
          monitor.subTask("Cleanup of canceled job");
          break;
        }
      }
      mapper.close();

      // Log the exported message.
      if (monitor.isCanceled()) {
        logger.info(volumeName + " exported (Job canceled before completion).");
      } else {
        logger.info(volumeName + " exported (Job completed).");
      }
    } catch (IOException ex) {
      throw new CoreException(ValidationStatus.error(ex.toString(), ex));
    }

    // Task is done.
    monitor.done();

  }

  /**
   * The internal method for exporting the a poststack3d volume as JavaSeis.
   * @param ps3d the poststack3d volume.
   * @param monitor the progress monitor.
   * @throws CoreException thrown on create/write errors.
   */
  private void exportAsPostStack3dOld(final PostStack3d ps3d, final ILogger logger, final IProgressMonitor monitor) throws CoreException {
    // Begin the task.
    String volumeName = ps3d.getDisplayName();
    String fileName = _model.getFileName();
    monitor.beginTask("Exporting as JavaSeis Volume: " + fileName, 100);

    String sectionName = "";
    int numSections = 0;
    float inlineStart = ps3d.getInlineStart();
    float inlineEnd = ps3d.getInlineEnd();
    float inlineDelta = ps3d.getInlineDelta();
    float xlineStart = ps3d.getXlineStart();
    float xlineEnd = ps3d.getXlineEnd();
    float xlineDelta = ps3d.getXlineDelta();

    // Update the storage order, if auto-calculated.
    PostStack3d.StorageOrder storageOrder = PostStack3d.StorageOrder.lookupByName(_model.getStorageOrder());
    if (storageOrder.equals(PostStack3d.StorageOrder.AUTO_CALCULATED)) {
      storageOrder = ps3d.getPreferredOrder();
      _model.setStorageOrder(storageOrder.getTitle());
    }

    // Create a JavaSeis PostStack3d mapper.
    PostStack3dMapper mapper = new PostStack3dMapper(_model);
    try {
      mapper.create(ps3d);

      // Check the mapper to get the preferred storage order.
      if (storageOrder.equals(PostStack3d.StorageOrder.INLINE_XLINE_Z)) {
        sectionName = "inline";
        numSections = ps3d.getNumInlines();
      } else if (storageOrder.equals(PostStack3d.StorageOrder.XLINE_INLINE_Z)) {
        sectionName = "xline";
        numSections = ps3d.getNumXlines();
      } else {
        throw new IllegalArgumentException("Invalid storage order for JavaSeis.");
      }

      // Loop thru the sections (inlines or xlines).
      int oldWork = 0;
      for (int i = 0; i < numSections; i++) {
        float zStart = ps3d.getZStart();
        float zEnd = ps3d.getZEnd();
        float section = 0;

        if (storageOrder.equals(PostStack3d.StorageOrder.INLINE_XLINE_Z)) {
          section = inlineStart + i * inlineDelta;

          monitor.subTask("Writing " + sectionName + " " + section);

          // Read the trace section from the PostStack3d.
          TraceData traceData = ps3d.getInline(section, xlineStart, xlineEnd, zStart, zEnd);

          // Write the trace section to the JavaSeis file.
          mapper.putInline(ps3d, section, xlineStart, xlineEnd, zStart, zEnd, traceData);
        } else if (storageOrder.equals(PostStack3d.StorageOrder.XLINE_INLINE_Z)) {
          section = xlineStart + i * xlineDelta;

          monitor.subTask("Writing " + sectionName + " " + section);

          // Read the trace section from the PostStack3d.
          TraceData traceData = ps3d.getXline(section, inlineStart, inlineEnd, zStart, zEnd);

          // Write the trace section to the JavaSeis file.
          mapper.putXline(ps3d, section, inlineStart, inlineEnd, zStart, zEnd, traceData);
        }

        int completion = Math.round(100 * (float) (i + 1) / numSections);
        int work = completion - oldWork;
        oldWork = completion;
        monitor.worked(work);
        if (monitor.isCanceled()) {
          monitor.subTask("Cleanup of canceled job");
          break;
        }
      }
      mapper.close();

      // Log the exported message.
      if (monitor.isCanceled()) {
        logger.info(volumeName + " exported (Job canceled before completion).");
      } else {
        logger.info(volumeName + " exported (Job completed).");
      }
    } catch (IOException ex) {
      throw new CoreException(ValidationStatus.error(ex.toString(), ex));
    }

    // Task is done.
    monitor.done();
  }

  /**
   * The internal method for exporting the a prestack3d volume as JavaSeis.
   * @param ps3d the prestack3d volume.
   * @param monitor the progress monitor.
   * @throws CoreException thrown on create/write errors.
   */
  private void exportAsPreStack3d(final PreStack3d ps3d, final ILogger logger, final IProgressMonitor monitor) throws CoreException {
    // Begin the task.
    String volumeName = ps3d.getDisplayName();
    String fileName = _model.getFileName();
    monitor.beginTask("Exporting as JavaSeis Volume: " + fileName, 100);

    // Create a JavaSeis PreStack3d mapper.
    PreStack3dMapper mapper = new PreStack3dMapper(_model);
    try {
      mapper.create(ps3d);

      int oldWork = 0;
      TraceIterator iterator = TraceIteratorFactory.create(ps3d);
      while (iterator.hasNext()) {
        TraceData traceData = iterator.next();
        mapper.putTraces(ps3d, traceData);
        int completion = (int) iterator.getCompletion();
        int work = completion - oldWork;
        oldWork = completion;
        monitor.worked(work);
        if (monitor.isCanceled()) {
          monitor.subTask("Cleanup of canceled job");
          break;
        }
      }
      mapper.close();

      // Log the exported message.
      if (monitor.isCanceled()) {
        logger.info(volumeName + " exported (Job canceled before completion).");
      } else {
        logger.info(volumeName + " exported (Job completed).");
      }
    } catch (IOException ex) {
      throw new CoreException(ValidationStatus.error(ex.toString(), ex));
    }

    // Task is done.
    monitor.done();

  }

  /**
   * The internal method for exporting the a prestack3d volume as JavaSeis.
   * @param ps3d the prestack3d volume.
   * @param monitor the progress monitor.
   * @throws CoreException thrown on create/write errors.
   */
  private void exportAsPreStack3dOld(final PreStack3d ps3d, final ILogger logger, final IProgressMonitor monitor) throws CoreException {
    // Begin the task.
    String volumeName = ps3d.getDisplayName();
    String fileName = _model.getFileName();
    monitor.beginTask("Exporting as JavaSeis Volume: " + fileName, 100);

    String sectionName = "";
    int numSections = 0;
    int numSubSections = 0;
    float inlineStart = ps3d.getInlineStart();
    float inlineEnd = ps3d.getInlineEnd();
    float inlineDelta = ps3d.getInlineDelta();
    float xlineStart = ps3d.getXlineStart();
    float xlineEnd = ps3d.getXlineEnd();
    float xlineDelta = ps3d.getXlineDelta();
    float offsetStart = ps3d.getOffsetStart();
    float offsetEnd = ps3d.getOffsetEnd();
    float offsetDelta = ps3d.getOffsetDelta();

    // Update the storage order, if auto-calculated.
    PreStack3d.StorageOrder storageOrder = PreStack3d.StorageOrder.lookupByName(_model.getStorageOrder());
    if (storageOrder.equals(PreStack3d.StorageOrder.AUTO_CALCULATED)) {
      storageOrder = ps3d.getPreferredOrder();
      _model.setStorageOrder(storageOrder.getName());
    }

    // Create a JavaSeis PreStack3d mapper.
    PreStack3dMapper mapper = new PreStack3dMapper(_model);
    try {
      mapper.create(ps3d);

      // Check the mapper to get the preferred storage order.
      if (storageOrder.equals(PreStack3d.StorageOrder.INLINE_XLINE_OFFSET_Z)) {
        sectionName = "inline";
        numSections = ps3d.getNumInlines();
        numSubSections = ps3d.getNumXlines();
      } else if (storageOrder.equals(PreStack3d.StorageOrder.INLINE_OFFSET_XLINE_Z)) {
        sectionName = "inline";
        numSections = ps3d.getNumInlines();
        numSubSections = ps3d.getNumOffsets();
      } else if (storageOrder.equals(PreStack3d.StorageOrder.XLINE_INLINE_OFFSET_Z)) {
        sectionName = "xline";
        numSections = ps3d.getNumXlines();
        numSubSections = ps3d.getNumInlines();
      } else if (storageOrder.equals(PreStack3d.StorageOrder.XLINE_OFFSET_INLINE_Z)) {
        sectionName = "xline";
        numSections = ps3d.getNumXlines();
        numSubSections = ps3d.getNumOffsets();
      } else if (storageOrder.equals(PreStack3d.StorageOrder.OFFSET_INLINE_XLINE_Z)) {
        sectionName = "offset";
        numSections = ps3d.getNumOffsets();
        numSubSections = ps3d.getNumInlines();
      } else if (storageOrder.equals(PreStack3d.StorageOrder.OFFSET_XLINE_INLINE_Z)) {
        sectionName = "offset";
        numSections = ps3d.getNumOffsets();
        numSubSections = ps3d.getNumXlines();
      } else {
        throw new IllegalArgumentException("Invalid storage order for JavaSeis.");
      }

      // Loop thru the sections (inlines or xlines or offsets).
      int oldWork = 0;
      float section = 0;
      for (int i = 0; i < numSections; i++) {
        for (int j = 0; j < numSubSections; j++) {
          float zStart = ps3d.getZStart();
          float zEnd = ps3d.getZEnd();

          TraceData traceData = null;
          if (storageOrder.equals(PreStack3d.StorageOrder.INLINE_XLINE_OFFSET_Z)) {
            section = inlineStart + i * inlineDelta;
            float xline = xlineStart + j * xlineDelta;

            // Read the trace section from the PreStack3d.
            traceData = ps3d.getTracesByInlineXline(section, xline, offsetStart, offsetEnd, zStart, zEnd);
          } else if (storageOrder.equals(PreStack3d.StorageOrder.XLINE_INLINE_OFFSET_Z)) {
            section = xlineStart + i * xlineDelta;
            float inline = inlineStart + j * inlineDelta;

            // Read the trace section from the PreStack3d.
            traceData = ps3d.getTracesByInlineXline(inline, section, offsetStart, offsetEnd, zStart, zEnd);
          } else if (storageOrder.equals(PreStack3d.StorageOrder.INLINE_OFFSET_XLINE_Z)) {
            section = inlineStart + i * inlineDelta;
            float offset = offsetStart + j * offsetDelta;

            // Read the trace section from the PreStack3d.
            traceData = ps3d.getTracesByInlineOffset(section, offset, xlineStart, xlineEnd, zStart, zEnd);
          } else if (storageOrder.equals(PreStack3d.StorageOrder.XLINE_OFFSET_INLINE_Z)) {
            section = xlineStart + i * xlineDelta;
            float offset = offsetStart + j * offsetDelta;

            // Read the trace section from the PreStack3d.
            traceData = ps3d.getTracesByXlineOffset(section, offset, inlineStart, inlineEnd, zStart, zEnd);
          } else if (storageOrder.equals(PreStack3d.StorageOrder.OFFSET_INLINE_XLINE_Z)) {
            section = offsetStart + i * offsetDelta;
            float inline = inlineStart + j * inlineDelta;

            // Read the trace section from the PreStack3d.
            traceData = ps3d.getTracesByInlineOffset(inline, section, xlineStart, xlineEnd, zStart, zEnd);
          } else if (storageOrder.equals(PreStack3d.StorageOrder.OFFSET_XLINE_INLINE_Z)) {
            section = offsetStart + i * offsetDelta;
            float xline = xlineStart + j * xlineDelta;

            // Read the trace section from the PreStack3d.
            traceData = ps3d.getTracesByXlineOffset(xline, section, inlineStart, inlineEnd, zStart, zEnd);
          }

          if (traceData != null) {
            // Write the trace section to the JavaSeis file.
            mapper.putTraces(ps3d, traceData);
          }

        }

        int completion = Math.round(100 * (float) (i + 1) / numSections);
        int work = completion - oldWork;
        oldWork = completion;
        monitor.worked(work);
        monitor.subTask("Writing " + sectionName + " " + section);
        if (monitor.isCanceled()) {
          monitor.subTask("Cleanup of canceled job");
          break;
        }
      }
      mapper.close();

      // Log the exported message.
      if (monitor.isCanceled()) {
        logger.info(volumeName + " exported (Job canceled before completion).");
      } else {
        logger.info(volumeName + " exported (Job completed).");
      }
    } catch (IOException ex) {
      throw new CoreException(ValidationStatus.error(ex.toString(), ex));
    }

    // Task is done.
    monitor.done();
  }

}
