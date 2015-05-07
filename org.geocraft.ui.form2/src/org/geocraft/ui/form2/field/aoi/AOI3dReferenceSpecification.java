/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field.aoi;


import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class AOI3dReferenceSpecification extends AbstractSpecification {

  public boolean isSatisfiedBy(Object obj) {
    if (obj instanceof PostStack3d || obj instanceof PreStack3d) {
      return true;
    } else if (obj instanceof Grid3d) {
      Grid3d grid = (Grid3d) obj;
      return grid.getGeometry() instanceof SeismicSurvey3d;
    }
    return false;
  }

}
