/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.base;


/**
 * The interface for any object that contains a reference
 * to an <code>IPropertiesProvider</code> implementation.
 */
public interface IPropertiesProviderContainer {

  /**
   * Returns the properties provider reference, or <i>null</i> if none.
   */
  IPropertiesProvider getPropertiesProvider();
}
