/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.mapviewer;


import org.geocraft.core.repository.IRepository;


/**
 * The service component class for the bundle.
 */
public class ServiceComponent {

  /** The entities repository. */
  private static IRepository _repository;

  /**
   * Return the entities repository.
   * @return the repository
   */
  public static IRepository getRepository() {
    return _repository;
  }

  public void addService(IRepository r) {
    _repository = r;
  }

  /**
   * @param r required by osgi 
   */
  public void removeService(IRepository r) {
    _repository = null;
  }
}
