package org.geocraft.core.service.logging.log4j;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.logging.ILoggingService;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;


/**
 * Default implementation of a logging service using log4j.
 */
public class DefaultLoggingService implements ILoggingService {

  /**
   * The bundle name to logger mapping. We need it to make sure we don't
   * register the same appender multiple times on the same logger.
   */
  private static Map<String, Logger> _bundleToLogger = new HashMap<String, Logger>();

  private static boolean _handlerInitialized = false;

  /**
   * Constructs a default implementation of a logging service.
   */
  public DefaultLoggingService() {
    System.out.println("Log4J logging service started.");
  }

  public synchronized ILogger getLogger(final Class klass) {
    try {
      String bundleName = getBundleName(klass);
      return new DefaultLogger(getLog4JLogger(bundleName));
    } catch (Exception ex) {
      System.out.println(ex.toString());
    }
    return new DefaultLogger(getLog4JLogger("Unknown bundle"));
  }

  public synchronized ILogger getLogger(final String bundleName) {
    try {
      return new DefaultLogger(getLog4JLogger(bundleName));
    } catch (Exception ex) {
      System.out.println(ex.toString());
    }
    TableCellRenderer r;
    return new DefaultLogger(getLog4JLogger("Unknown bundle"));
  }

  public synchronized Logger getLog4JLogger(final String bundleName) {
    Logger logger = _bundleToLogger.get(bundleName);
    if (logger == null) {
      logger = Logger.getLogger(bundleName);
      // A channel like org.geocraft.geomath is considered the parent of a channel
      // like org.geocraft.geomath.something.
      // So, the appenders from the parent are inherited by the children if the call below is not done.
      logger.setAdditivity(false);
      _bundleToLogger.put(bundleName, logger);
      registerAppenders(logger);
      if (!_handlerInitialized) {
        java.util.logging.Logger.getLogger("").addHandler(new LoggingHandler());
        _handlerInitialized = true;
      }
    }
    return logger;
  }

  /**
   * Registers the appenders to the logger.
   * 
   * @param logger
   *          the logger
   */
  private static synchronized void registerAppenders(final Logger logger) {
    Enumeration appenders = Logger.getRootLogger().getAllAppenders();
    while (appenders.hasMoreElements()) {
      logger.addAppender((Appender) appenders.nextElement());
    }
  }

  /**
   * returns the bundle name of the specified class.
   * 
   * @param c
   *          the specified class
   * @return the bundle name
   * @throws CoreException
   *           the bundle name cannot be found by any reason.
   */
  private static synchronized String getBundleName(final Class c) throws CoreException {
    PackageAdmin admin = LoggingActivator.getPackageAdmin();
    if (admin == null) {
      throw new CoreException(new Status(IStatus.ERROR, LoggingActivator.PLUGIN_ID, "Cannot get PackageAdmin."));
    }
    Bundle bundle = admin.getBundle(c);
    if (bundle == null) {
      throw new CoreException(new Status(IStatus.ERROR, LoggingActivator.PLUGIN_ID, "Cannot get the bundle."));
    }
    return bundle.getSymbolicName();
  }

  protected static ILog getBundleLog(final Class c) throws CoreException {
    // get the bundle for a plug-in
    Bundle b = Platform.getBundle(getBundleName(c));
    return Platform.getLog(b);
  }

}
