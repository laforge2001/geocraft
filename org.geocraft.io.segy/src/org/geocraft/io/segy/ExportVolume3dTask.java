/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


/**
 * The background task for exporting a SEG-Y volume.
 */
public class ExportVolume3dTask extends ExportTask {

  /** The seismic dataset to export. */
  private SeismicDataset _volume;

  /** The SEG-Y volume mapper properties. */
  private Volume3dMapperModel _mapperModel;

  /**
   * The default constructor.
   * This is a no-argument constructor due to its creation via eclipse extension points.
   */
  public ExportVolume3dTask() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void setMapperModel(final MapperModel model) {
    _mapperModel = (Volume3dMapperModel) model;
  }

  @Override
  public void setEntity(final Entity entity) {
    _volume = (SeismicDataset) entity;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    if (_volume == null) {
      throw new RuntimeException("The task for exporting as SEG-Y Volume has not been initialized.");
    }
    if (_mapperModel == null) {
      throw new RuntimeException("The task for exporting as SEG-Y Volume has not been initialized.");
    }
    if (_volume.getClass().equals(PostStack3d.class)) {
      exportAsPostStack3d((PostStack3d) _volume, logger, monitor);
    } else if (_volume.getClass().equals(PreStack3d.class)) {
      exportAsPreStack3d((PreStack3d) _volume, logger, monitor);
    } else {
      throw new CoreException(ValidationStatus.error("Dataset is not 3D: " + _volume.getClass().getName()));
    }
  }

  /**
   * The internal method for exporting the a poststack3d volume as SEG-Y.
   * @param ps3d the poststack3d volume.
   * @param monitor the progress monitor.
   * @throws CoreException thrown on create/write errors.
   */
  private void exportAsPostStack3d(final PostStack3d ps3d, final ILogger logger, final IProgressMonitor monitor) throws CoreException {
    // Begin the task.
    String volumeName = ps3d.getDisplayName();
    String fileName = _mapperModel.getFileName() + _mapperModel.getFileExtension();
    monitor.beginTask("Exporting as SEG-Y Volume: " + fileName, 100);

    String sectionName = "";
    int numSections = 0;
    float inlineStart = ps3d.getInlineStart();
    float inlineEnd = ps3d.getInlineEnd();
    float inlineDelta = ps3d.getInlineDelta();
    float xlineStart = ps3d.getXlineStart();
    float xlineEnd = ps3d.getXlineEnd();
    float xlineDelta = ps3d.getXlineDelta();

    // Update the storage order, if auto-calculated.
    StorageOrder storageOrder = StorageOrder.lookupByName(_mapperModel.getStorageOrder());
    if (storageOrder.equals(StorageOrder.AUTO_CALCULATED)) {
      storageOrder = ps3d.getPreferredOrder();
      _mapperModel.setStorageOrder(storageOrder.getTitle());
    }

    // Create a SEG-Y PostStack3d mapper.
    PostStack3dMapper mapper = new PostStack3dMapper(_mapperModel, false);
    try {
      mapper.create(ps3d);

      // Check the mapper to get the preferred storage order.
      if (storageOrder.equals(StorageOrder.INLINE_XLINE_Z)) {
        sectionName = "inline";
        numSections = ps3d.getNumInlines();
      } else if (storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
        sectionName = "xline";
        numSections = ps3d.getNumXlines();
      } else {
        throw new IllegalArgumentException("Invalid storage order for SEG-Y.");
      }

      // Loop thru the sections (inlines or xlines).
      int oldWork = 0;
      for (int i = 0; i < numSections && !monitor.isCanceled(); i++) {
        float zStart = ps3d.getZStart();
        float zEnd = ps3d.getZEnd();
        float section = 0;

        TraceData traceData = null;
        if (storageOrder.equals(StorageOrder.INLINE_XLINE_Z)) {
          section = inlineStart + i * inlineDelta;

          // Read the trace section from the PostStack3d.
          traceData = ps3d.getInline(section, xlineStart, xlineEnd, zStart, zEnd);

        } else if (storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
          section = xlineStart + i * xlineDelta;

          // Read the trace section from the PostStack3d.
          traceData = ps3d.getXline(section, inlineStart, inlineEnd, zStart, zEnd);
        }

        monitor.subTask("Writing " + sectionName + " " + section);

        // Write the trace section to the SEG-Y file.
        if (traceData != null) {
          Trace[] tracesIn = traceData.getTraces();
          List<Trace> tracesOut = new ArrayList<Trace>();
          for (Trace traceIn : tracesIn) {
            Trace traceOut = new Trace(traceIn);
            int offset = 0;
            Header hdr = traceIn.getHeader();
            if (hdr instanceof SegyTraceHeader) {
              traceOut.setHeader(hdr);
            } else {
              HeaderDefinition hdrDef = hdr.getHeaderDefinition();
              if (hdrDef.contains(SegyTraceHeaderCatalog.SOURCE_RECEIVER_DISTANCE)) {
                offset = hdr.getInteger(SegyTraceHeaderCatalog.SOURCE_RECEIVER_DISTANCE);
              }
              if (hdrDef.contains(TraceHeaderCatalog.OFFSET)) {
                offset = hdr.getInteger(TraceHeaderCatalog.OFFSET);
              }
              SegyTraceHeader header = new SegyTraceHeader(SegyTraceHeader.POSTSTACK3D_HEADER_DEF);
              //            header.getDefinition().add(TraceHeaderCatalog.INLINE_NUM);
              //            header.getDefinition().add(TraceHeaderCatalog.XLINE_NUM);
              //            header.getDefinition().add(TraceHeaderCatalog.OFFSET);
              header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(traceIn.getInline()));
              header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(traceIn.getXline()));
              header.putInteger(SegyTraceHeaderCatalog.SOURCE_RECEIVER_DISTANCE, offset);
              //header.putFloat(TraceHeaderCatalog.OFFSET, offset);
              traceOut.setHeader(header);
            }
            tracesOut.add(traceOut);

            // Ensure that if the corner point traces are missing that they
            // still get written out (as dead traces), since the SEG-Y loader
            // needs the corner points to establish geometry.
            if ((traceIn.getInline() == ps3d.getInlineStart() || traceIn.getInline() == ps3d.getInlineEnd())
                && (traceIn.getXline() == ps3d.getXlineStart() || traceIn.getXline() == ps3d.getXlineEnd())) {
              System.out.println("FOUND CORNER: " + traceIn.getInline() + " " + traceIn.getXline());
              if (traceOut.isMissing()) {
                traceOut.setStatus(Status.Dead);
              }
            }
          }
          mapper.putTraces(ps3d, new TraceData(tracesOut.toArray(new Trace[0])));
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
   * The internal method for exporting the a prestack3d volume as SEG-Y.
   * @param ps3d the prestack3d volume.
   * @param monitor the progress monitor.
   * @throws CoreException thrown on create/write errors.
   */
  private void exportAsPreStack3d(final PreStack3d ps3d, final ILogger logger, final IProgressMonitor monitor) throws CoreException {
    //    // Begin the task.
    //    String volumeName = ps3d.getDisplayName();
    //    String fileName = _mapperModel.getFileName() + _mapperModel.getFileExtension();
    //    monitor.beginTask("Exporting as SEG-Y Volume: " + fileName, 100);
    //
    //    float inlineStart = ps3d.getInlineStart();
    //    float inlineDelta = ps3d.getInlineDelta();
    //    float xlineStart = ps3d.getXlineStart();
    //    float xlineDelta = ps3d.getXlineDelta();
    //    float offsetStart = ps3d.getOffsetStart();
    //    float offsetEnd = ps3d.getOffsetEnd();
    //
    //    // Update the storage order, if auto-calculated.
    //    //    StorageOrder storageOrder = StorageOrder.lookupByName(_model.getStorageOrder());
    //    //    if (storageOrder.equals(StorageOrder.AUTO_CALCULATED)) {
    //    //      storageOrder = ps3d.get
    //    //      _model.setStorageOrder(storageOrder.getTitle());
    //    //    }
    //
    //    // Create a SEG-Y PreStack3d mapper.
    //    PreStack3dMapper mapper = new PreStack3dMapper(_mapperModel, false);
    //    try {
    //      mapper.create(ps3d);
    //
    //      // Loop thru the sections (inlines,xlines).
    //      int oldWork = 0;
    //      for (int i = 0; i < ps3d.getNumInlines() && !monitor.isCanceled(); i++) {
    //        float zStart = ps3d.getZStart();
    //        float zEnd = ps3d.getZEnd();
    //        float inline = inlineStart + i * inlineDelta;
    //
    //        monitor.subTask("Writing inline " + inline);
    //
    //        for (int j = 0; j < ps3d.getNumXlines(); j++) {
    //          float xline = xlineStart + j * xlineDelta;
    //
    //          // Read the trace section from the PreStack3d.
    //          TraceData traceData = ps3d.getTracesByInlineXline(inline, xline, offsetStart, offsetEnd, zStart, zEnd);
    //
    //          // Write the trace section to the SEG-Y file.
    //          if (traceData != null) {
    //            Trace[] tracesIn = traceData.getTraces();
    //            List<Trace> tracesOut = new ArrayList<Trace>();
    //            for (Trace traceIn : tracesIn) {
    //              float offset = 0;
    //              Header hdr = traceIn.getHeader();
    //              if (hdr.contains(TraceHeaderCatalog.OFFSET)) {
    //                offset = hdr.getFloat(TraceHeaderCatalog.OFFSET);
    //              }
    //              Trace traceOut = new Trace(traceIn);
    //              SegyTraceHeader header = new SegyTraceHeader();
    //              header.getDefinition().add(TraceHeaderCatalog.INLINE_NUM);
    //              header.getDefinition().add(TraceHeaderCatalog.XLINE_NUM);
    //              header.getDefinition().add(TraceHeaderCatalog.OFFSET);
    //              header.putFloat(TraceHeaderCatalog.INLINE_NUM, traceIn.getInline());
    //              header.putFloat(TraceHeaderCatalog.XLINE_NUM, traceIn.getXline());
    //              header.putFloat(TraceHeaderCatalog.OFFSET, offset);
    //              header.putInt(SegyTraceHeaderCatalog.SOURCE_RECEIVER_DISTANCE, Math.round(offset));
    //              traceOut.setHeader(header);
    //              tracesOut.add(traceOut);
    //            }
    //            mapper.putTraces(ps3d, new TraceData(tracesOut.toArray(new Trace[0])));
    //          }
    //        }
    //
    //        int completion = Math.round(100 * (float) (i + 1) / ps3d.getNumInlines());
    //        int work = completion - oldWork;
    //        oldWork = completion;
    //        monitor.worked(work);
    //        if (monitor.isCanceled()) {
    //          monitor.subTask("Cleanup of canceled job");
    //          break;
    //        }
    //      }
    //      mapper.close();
    //
    //      // Log the exported message.
    //      if (monitor.isCanceled()) {
    //        logger.info(volumeName + " exported (Job canceled before completion).");
    //      } else {
    //        logger.info(volumeName + " exported (Job completed).");
    //      }
    //    } catch (IOException ex) {
    //      throw new CoreException(ValidationStatus.error(ex.toString(), ex));
    //    }
    //
    //    // Task is done.
    //    monitor.done();
  }

}
