/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


/**
 * The interface for listeners to <code>IModel</code> implementations.
 */
public interface IModelListener {

  /**
   * Invoked when a model property is changed.
   * 
   * @param key the key of the model property that was changed.
   */
  void propertyChanged(String key);
}
