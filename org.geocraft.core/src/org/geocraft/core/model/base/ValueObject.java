/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.base;

import java.io.Serializable;


public abstract class ValueObject extends AbstractPropertiesProvider implements Serializable {

  private String _displayName;

  public ValueObject(final String displayName) {
    _displayName = displayName;
  }

  @Override
  public String getType() {
    return getClass().getSimpleName();
  }

  @Override
  public String getDisplayName() {
    return _displayName;
  }

  public void setDisplayName(final String name) {
    _displayName = name;
  }

}
