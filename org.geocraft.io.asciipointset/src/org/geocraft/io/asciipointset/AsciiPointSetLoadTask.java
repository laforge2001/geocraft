/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciipointset;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public class AsciiPointSetLoadTask extends ImportTask {

  protected AsciiPointSetMapperModel _mapperModel;

  @Override
  public void setMapperModel(final MapperModel model) {
    _mapperModel = (AsciiPointSetMapperModel) model;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    AsciiPointSetMapper mapper = new AsciiPointSetMapper(_mapperModel);
    PointSet pointSet = new PointSet(_mapperModel.getFileName(), mapper);
    pointSet.setZUnit(_mapperModel.getZUnit());
    repository.add(pointSet);
  }
}
