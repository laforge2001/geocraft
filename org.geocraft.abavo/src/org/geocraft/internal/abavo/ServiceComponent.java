/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo;


import org.geocraft.math.regression.IRegressionMethodService;


public class ServiceComponent {

  public static final String PLUGIN_ID = "org.geocraft.abavo";

  private static IRegressionMethodService _regressionMethodService;

  public static IRegressionMethodService getRegressionMethodService() {
    return _regressionMethodService;
  }

  public void addService(IRegressionMethodService regressionMethodService) {
    _regressionMethodService = regressionMethodService;
  }

  /**
   * @param regressionMethodService  
   */
  public void removeService(IRegressionMethodService regressionMethodService) {
    _regressionMethodService = null;
  }

}
