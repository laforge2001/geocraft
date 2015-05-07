/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.internal.iconviewer;


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * Displays all of the icons that are available in the currently loaded bundles. 
 * This bundle has an activator because we need to query the context for 
 * the loaded bundles.
 */
public class IconViewerActivator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.geocraft.geomath.algorithm.iconviewer";

  private static BundleContext _context;

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    _context = context;
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    super.stop(context);
  }

  public static BundleContext getBundleContext() {
    return _context;
  }
}
