/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.logging.log4j;


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;


/**
 * 
 * wrapper appender that takes in log4j logging events and 
 * converts them into things eclipse can show in its log view.
 * 
 * extending AppenderSkeleton versus implementing Appender
 * allows control from the log4j.xml configuration file 
 * (no recompiling needed)
 *
 */
public class EclipseLogAppender extends AppenderSkeleton {

  private ILog getBundleILog(final String bundleName) {
    // get the bundle for a plug-in
    Bundle b = Platform.getBundle(bundleName);
    if (b == null) {
      return null;
    }
    return Platform.getLog(b);
  }

  private int getSeverity(final LoggingEvent ev) {
    Level level = ev.getLevel();
    if (level == Level.FATAL || level == Level.ERROR) {
      return IStatus.ERROR;
    } else if (level == Level.WARN) {
      return IStatus.WARNING;
    } else if (level == Level.INFO) {
      return IStatus.INFO;
    } else {// debug, trace and custom levels
      return IStatus.OK;
    }
  }

  @Override
  protected void append(LoggingEvent event) {
    String bundleName = event.getLoggerName();
    ILog eclipseLogger = getBundleILog(bundleName);
    Throwable th = null;
    if (event.getThrowableInformation() != null) {
      th = event.getThrowableInformation().getThrowable();
    }
    if (eclipseLogger != null) {
      eclipseLogger.log(new Status(getSeverity(event), Platform.getBundle(bundleName).getSymbolicName(), (String) event
          .getMessage(), th));
    }
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean requiresLayout() {
    // TODO Auto-generated method stub
    return false;
  }

}
