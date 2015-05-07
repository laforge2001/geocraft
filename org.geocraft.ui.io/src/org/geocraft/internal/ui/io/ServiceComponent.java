/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.io;


import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.repository.IRepository;
import org.geocraft.ui.form2.IDatastoreAccessorUIService;


public class ServiceComponent {

  private static IDatastoreAccessorService _datastoreAccessorService;

  private static IDatastoreAccessorUIService _datastoreAccessorUIService;

  private static IRepository _repository;

  public static final String PLUGIN_ID = "org.geocraft.ui.io";

  public static IRepository getRepository() {
    return _repository;
  }

  public static IDatastoreAccessorService getDatastoreAccessorService() {
    return _datastoreAccessorService;
  }

  public static IDatastoreAccessorUIService getDatastoreAccessorUIService() {
    return _datastoreAccessorUIService;
  }

  public void setRepository(IRepository repository) {
    _repository = repository;
  }

  public void setDatastoreAccessorService(IDatastoreAccessorService service) {
    _datastoreAccessorService = service;
  }

  public void setDatastoreAccessorUIService(IDatastoreAccessorUIService service) {
    _datastoreAccessorUIService = service;
  }

  /**
   * @param repository unused but required by osgi
   */
  public void unsetRepository(IRepository repository) {
    _repository = null;
  }

  /**
   * @param service unused but required by osgi
   */
  public void unsetDatastoreAccessorService(IDatastoreAccessorService service) {
    _datastoreAccessorService = null;
  }

  /**
   * @param service unused but required by osgi
   */
  public void unsetDatastoreAccessorUIService(IDatastoreAccessorUIService service) {
    _datastoreAccessorUIService = null;
  }

}
