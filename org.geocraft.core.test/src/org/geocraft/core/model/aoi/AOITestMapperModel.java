/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.aoi;


import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.validation.IValidation;


public class AOITestMapperModel extends MapperModel {

  private String _uniqueId;

  public AOITestMapperModel() {
    _uniqueId = "foo";
  }

  @Override
  public boolean existsInStore() {
    return true;
  }

  @Override
  public boolean existsInStore(String name) {
    return true;
  }

  @Override
  public String getUniqueId() {
    return _uniqueId;
  }

  @Override
  public void updateUniqueId(String name) {
    _uniqueId = name;
  }

  public void validate(IValidation results) {
    // Nothing to do.
  }

}
