/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.logging;


/**
 * The interface for a logging service.
 */
public interface ILoggingService {

  /**
   * Gets the logger for the specified bundle.
   * 
   * @param bundleName the bundle name.
   * @return the logger.
   */
  ILogger getLogger(String bundleName);

  /**
   * Gets the logger for the bundle in which the specified class is defined.
   * 
   * @param klass the class.
   * @return the logger.
   */
  ILogger getLogger(Class klass);
}
