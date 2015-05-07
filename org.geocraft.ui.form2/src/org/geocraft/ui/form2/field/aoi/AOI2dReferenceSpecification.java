/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field.aoi;


import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class AOI2dReferenceSpecification extends AbstractSpecification {

  public boolean isSatisfiedBy(Object obj) {
    if (obj instanceof PostStack2d) {
      return true;
    } else if (obj instanceof Grid2d) {
      Grid2d grid = (Grid2d) obj;
      return grid.getGridGeometry() instanceof SeismicSurvey2d;
    }
    return false;
  }

}
