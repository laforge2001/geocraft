/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.mapper;


import org.geocraft.core.model.validation.IValidation;


public class InMemoryMapperModel extends MapperModel {

  private String _uniqueId;

  public InMemoryMapperModel() {
    // Nothing to do.
  }

  public InMemoryMapperModel(final String id) {
    _uniqueId = id;
  }

  @Override
  public String getUniqueId() {
    return _uniqueId;
  }

  @Override
  public void updateUniqueId(final String uniqueId) {
    _uniqueId = uniqueId;
  }

  public void validate(final IValidation validation) {
    // No validation to perform.
  }

  @Override
  public boolean existsInStore() {
    return false;
  }

  @Override
  public boolean existsInStore(final String name) {
    return false;
  }
}
