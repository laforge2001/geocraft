/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.logging.log4j;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;


/**
 * Handler for the java.util.logging messages.
 * We use it to translate and direct the messages to the Eclipse log view.
 */
public class LoggingHandler extends Handler {

  private final Map<String, String> _classNameToBundleName = new HashMap<String, String>();

  public LoggingHandler() {
    // don't log info messages
    Logger.getLogger("com.jme").setLevel(Level.WARNING);
    Logger.getLogger("com.jmex").setLevel(Level.WARNING);
  }

  @Override
  public void close() throws SecurityException {
    // TODO Auto-generated method stub
  }

  @Override
  public void flush() {
    // TODO Auto-generated method stub
  }

  @Override
  public void publish(final LogRecord record) {
    String bundleName = getBundleNameForClassName(record.getSourceClassName());
    ILog eclipseLogger = getBundleILog(bundleName);
    Throwable thrown = record.getThrown();
    if (eclipseLogger != null) {
      eclipseLogger.log(new Status(getSeverity(record), Platform.getBundle(bundleName).getSymbolicName(), record
          .getMessage(), thrown));
    }
  }

  // we do need this method because we do want to handle the java.util.logging messages
  private String getBundleNameForClassName(final String className) {
    String bundleName = "org.geocraft.product";
    PackageAdmin packageAdmin = LoggingActivator.getPackageAdmin();
    if (packageAdmin == null) {
      return bundleName;
    }
    if (_classNameToBundleName.get(className) != null) {
      return _classNameToBundleName.get(className);
    }
    ExportedPackage[] packages = packageAdmin.getExportedPackages(className.substring(0, className.lastIndexOf(".")));
    if (packages != null && packages.length > 0) {
      bundleName = packages[0].getExportingBundle().getSymbolicName();
      _classNameToBundleName.put(className, bundleName);
    }
    return bundleName;
  }

  private int getSeverity(final LogRecord record) {
    Level level = record.getLevel();
    if (level == Level.SEVERE) {
      return IStatus.ERROR;
    } else if (level == Level.WARNING) {
      return IStatus.WARNING;
    } else if (level == Level.INFO || level == Level.CONFIG) {
      return IStatus.INFO;
    }
    return IStatus.OK;
  }

  private ILog getBundleILog(final String bundleName) {
    // get the bundle for a plug-in
    Bundle b = Platform.getBundle(bundleName);
    if (b == null) {
      return null;
    }
    return Platform.getLog(b);
  }

}
