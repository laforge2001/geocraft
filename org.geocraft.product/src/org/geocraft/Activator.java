/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class Activator implements BundleActivator {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.geocraft.product";

  // The shared instance
  private static Activator _activator;

  public Activator() {
    // do nothing
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    _activator = this;
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    _activator = null;
  }

  /**
   * Returns the shared instance
   * @return the shared instance
   */
  public static Activator getDefault() {
    return _activator;
  }

}
