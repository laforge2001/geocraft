/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.logging;


public class NullLogger implements ILogger {

  public void debug(final String message) {
    System.out.println("DEBUG: " + message);
  }

  public void debug(final String message, final Throwable thrown) {
    System.out.println("DEBUG: " + message);
  }

  public void error(final String message) {
    System.out.println("ERROR: " + message);
  }

  public void error(final String message, final Throwable thrown) {
    System.out.println("ERROR: " + message);
  }

  public void fatal(final String message) {
    System.out.println("FATAL: " + message);
  }

  public void fatal(final String message, final Throwable thrown) {
    System.out.println("FATAL: " + message);
  }

  public void info(final String message) {
    System.out.println("INFO: " + message);
  }

  public void info(final String message, final Throwable thrown) {
    System.out.println("INFO: " + message);
  }

  public void warn(final String message) {
    System.out.println("WARNING: " + message);
  }

  public void warn(final String message, final Throwable thrown) {
    System.out.println("WARNING: " + message);
  }

}
