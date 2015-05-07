/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.factory.model;




/**
 * The only way to create a WellPick object is via this Factory.
 */
public class WellPickFactory {

  private static WellPickFactory _factory;

  private WellPickFactory() {
    // singleton
  }

  public static synchronized WellPickFactory getInstance() {
    if (_factory == null) {
      _factory = new WellPickFactory();
    }
    return _factory;
  }

}
