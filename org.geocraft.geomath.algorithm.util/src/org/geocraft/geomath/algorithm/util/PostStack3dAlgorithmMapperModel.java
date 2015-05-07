/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.util;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.validation.IValidation;


public class PostStack3dAlgorithmMapperModel extends MapperModel {

  private static int counter = 1;

  private String _uniqueID;

  public PostStack3dAlgorithmMapperModel() {
    // Temporary hack to get a unique ID.
    _uniqueID = "PostStack3dAlgorithmMapper" + counter++;
  }

  @Override
  public String getUniqueId() {
    return _uniqueID;
  }

  @Override
  public void updateUniqueId(final String name) {
    _uniqueID = name;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.common.model2.IModel#validate(org.geocraft.core.common.model2.validation.IValidation)
   */
  @Override
  public void validate(IValidation validation) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean existsInStore() {
    return false;
  }

  @Override
  public boolean existsInStore(String name) {
    return false;
  }

  /**
   * @param domain
   */
  public void setDomain(Domain domain) {
    // TODO Auto-generated method stub

  }
}
