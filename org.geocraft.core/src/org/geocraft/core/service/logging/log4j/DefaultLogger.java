/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.logging.log4j;


import org.apache.log4j.Logger;
import org.geocraft.core.service.logging.ILogger;


public class DefaultLogger implements ILogger {

  /** The log4j logger. */
  private Logger _logger;

  /**
   * Constructs a default logger using a log4j logger.
   * 
   * @param logger the log4j logger.
   */
  public DefaultLogger(Logger logger) {
    _logger = logger;
  }

  public void debug(String message) {
    _logger.debug(message);
  }

  public void debug(String message, Throwable thrown) {
    _logger.debug(message, thrown);
  }

  public void error(String message) {
    _logger.error(message);
  }

  public void error(String message, Throwable thrown) {
    _logger.error(message, thrown);
  }

  public void fatal(String message) {
    _logger.fatal(message);
  }

  public void fatal(String message, Throwable thrown) {
    _logger.fatal(message, thrown);
  }

  public void info(String message) {
    _logger.info(message);
  }

  public void info(String message, Throwable thrown) {
    _logger.info(message, thrown);
  }

  public void warn(String message) {
    _logger.warn(message);
  }

  public void warn(String message, Throwable thrown) {
    _logger.warn(message, thrown);
  }

}
