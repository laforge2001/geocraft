/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.gocad.pointset;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public class PointSetLoadTask extends ImportTask {

  protected PointSetMapperModel _mapperModel;

  @Override
  public void setMapperModel(final MapperModel model) {
    _mapperModel = (PointSetMapperModel) model;
  }

  /**
   * @throws CoreException  
   */
  @Override
  public void run(final ILogger logger, final IProgressMonitor monitor, IRepository repository) throws CoreException {
    PointSetMapper mapper = new PointSetMapper(_mapperModel);
    PointSet pointSet = new PointSet(_mapperModel.getFileName(), mapper);
    repository.add(pointSet);
  }
}

/*
 
GOCAD VSet 1
HEADER{
name:Blume
}
VRTX 40 211.0763 125.6311 3.7694 0.0074 -0.0071 -0.0265
VRTX 41 211.4848 125.6332 3.7644 0.0051 0.0028 -0.0447
VRTX 42 211.8737 125.6354 3.7680 0.0072 0.0005 -0.0563
VRTX 43 212.2845 125.6260 3.7713 0.0019 -0.0011 -0.0722
END

*/
