/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field.aoi;


import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class UniqueIdSpecification extends AbstractSpecification {

  private String _uniqueId;

  public UniqueIdSpecification(String uniqueId) {
    _uniqueId = uniqueId;
  }

  public boolean isSatisfiedBy(Object obj) {
    if (SeismicSurvey3dAOI.class.isAssignableFrom(obj.getClass())) {
      SeismicSurvey3dAOI aoi = (SeismicSurvey3dAOI) obj;
      String uniqueId = aoi.getMapper().getModel().getUniqueId();
      return uniqueId.equals(_uniqueId);
    }
    return false;
  }

}
