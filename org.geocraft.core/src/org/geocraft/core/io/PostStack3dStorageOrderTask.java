/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.io;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.service.logging.ILogger;


public class PostStack3dStorageOrderTask extends BackgroundTask {

  private StorageOrder _storageOrder = StorageOrder.AUTO_CALCULATED;

  private final PostStack3d _ps3d;

  public PostStack3dStorageOrderTask(final PostStack3d ps3d) {
    _ps3d = ps3d;
  }

  @Override
  public Object compute(final ILogger logger, final IProgressMonitor monitor) {
    int totalWork = PostStack3dLoadHelper.ALLOWED_NUMBER_OF_ATTEMPTS * (1 + PostStack3dLoadHelper.NUM_TEST_SAMPLES * 2);
    totalWork = 1 + PostStack3dLoadHelper.NUM_TEST_SAMPLES * 2;
    monitor.beginTask("Determining storage order...", totalWork);
    try {
      // Calculate the storage order
      _storageOrder = PostStack3dLoadHelper.getStorageOrder(_ps3d, (IPostStack3dMapper) _ps3d.getMapper(), monitor);
    } catch (Exception ex) {
      _storageOrder = StorageOrder.INLINE_XLINE_Z;
    } finally {
      synchronized (this) {
        notifyAll();
      }
    }
    return _storageOrder;
  }

  public static StorageOrder getStorageOrder(final PostStack3d ps3d) {
    PostStack3dStorageOrderTask task = new PostStack3dStorageOrderTask(ps3d);
    StorageOrder storageOrder = (StorageOrder) TaskRunner.runTask(task, "Calculating Storage Order");
    return storageOrder;
  }
}
