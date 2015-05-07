/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import org.geocraft.core.io.IDatastoreAccessorService;


public class ServiceComponent {

  private static IDatastoreAccessorService _datastoreAccessorService;

  private static IDatastoreAccessorUIService _datastoreAccessorUIService;

  public static final String PLUGIN_ID = "org.geocraft.ui.form2";

  public static IDatastoreAccessorService getDatastoreAccessorService() {
    return _datastoreAccessorService;
  }

  public void setDatastoreAccessorService(IDatastoreAccessorService service) {
    _datastoreAccessorService = service;
  }

  public void unsetDatastoreAccessorService(IDatastoreAccessorService service) {
    _datastoreAccessorService = null;
  }

  public static IDatastoreAccessorUIService getDatastoreAccessorUIService() {
    return _datastoreAccessorUIService;
  }

  public void setDatastoreAccessorUIService(IDatastoreAccessorUIService service) {
    _datastoreAccessorUIService = service;
  }

  public void unsetDatastoreAccessorUIService(IDatastoreAccessorUIService service) {
    _datastoreAccessorUIService = null;
  }
}
