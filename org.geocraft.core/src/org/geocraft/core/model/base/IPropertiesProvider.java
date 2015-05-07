/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.base;


public interface IPropertiesProvider {

  /**
   * Returns the array of name/value pair display properties
   * used to describe the properties provider.
   */
  public abstract Object[][] getDisplayableProperties();

  /**
   * Returns the type of properties provider.
   */
  public abstract String getType();

  /**
   * Returns the display name of the properties provider.
   */
  public abstract String getDisplayName();

}