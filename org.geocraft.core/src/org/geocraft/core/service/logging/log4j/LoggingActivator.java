/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.service.logging.log4j;


import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;


/**
 * The Log activator class.
 */
public class LoggingActivator implements BundleActivator {

  public static final String PLUGIN_ID = "org.geocraft.core.service.logging";

  private static BundleContext _context;

  public void start(final BundleContext context) throws Exception {
    _context = context;
    URL log4j = getClass().getResource("log4j.xml");
    if (log4j != null) {
      DOMConfigurator.configure(log4j);
    }
  }

  public static BundleContext getBundleContext() {
    return _context;
  }

  public static PackageAdmin getPackageAdmin() {

    // allow unit tests to run without OSGi
    if (_context == null) {
      return null;
    }

    ServiceReference reference = _context.getServiceReference(PackageAdmin.class.getName());
    if (reference == null) {
      return null;
    }
    return (PackageAdmin) _context.getService(reference);
  }

  @SuppressWarnings("unused")
  public void stop(final BundleContext context) throws Exception {
    // not used for now
  }
}