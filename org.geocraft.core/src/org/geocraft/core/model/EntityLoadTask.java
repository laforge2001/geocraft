/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


import java.io.IOException;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.service.logging.ILogger;


public class EntityLoadTask extends BackgroundTask {

  private final Entity _entity;

  public EntityLoadTask(final Entity entity) {
    _entity = entity;
  }

  @Override
  public Object compute(final ILogger logger, final IProgressMonitor monitor) throws CoreException {
    try {
      _entity.getMapper().read(_entity, monitor);
      _entity.setDirty(false);
    } catch (IOException ex) {
      ex.toString();
      throw new CoreException(ValidationStatus.error(ex.getMessage()));
    }
    return null;
  }

}
