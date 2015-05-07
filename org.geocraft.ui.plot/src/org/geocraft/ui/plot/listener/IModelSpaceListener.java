/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.listener;


import org.geocraft.ui.plot.event.ModelSpaceEvent;


/**
 * Interface for a plot model listener.
 */
public interface IModelSpaceListener {

  /**
   * Invoked when a plot model is updated.
   * 
   * @param modelEvent the plot model event.
   */
  void modelSpaceUpdated(ModelSpaceEvent modelEvent);
}
