/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.logging;


public interface ILogger {

  /**
   * Logs an information message.
   * 
   * @param message the information message.
   */
  void info(final String message);

  /**
   * Logs an information message with a throwable.
   * 
   * @param message the information message.
   * @param thrown the thrown throwable.
   */
  void info(final String message, final Throwable thrown);

  /**
   * Logs an information message.
   * 
   * @param message the information message.
   */
  void debug(final String message);

  /**
   * Logs an information message with a throwable.
   * 
   * @param message the information message.
   * @param thrown the thrown throwable.
   */
  void debug(final String message, final Throwable thrown);

  /**
   * Logs an error message.
   * 
   * @param message the error message.
   */
  void error(final String message);

  /**
   * Logs an error message with a throwable.
   * 
   * @param message the error message.
   * @param thrown the thrown throwable.
   */
  void error(final String message, final Throwable thrown);

  /**
   * Logs an fatal message.
   * 
   * @param message the fatal message.
   */
  void fatal(final String message);

  /**
   * Logs an fatal message with a throwable.
   * 
   * @param message the fatal message.
   * @param thrown the thrown throwable.
   */
  void fatal(final String message, final Throwable thrown);

  /**
   * Logs an warning message.
   * 
   * @param message the warning message.
   */
  void warn(final String message);

  /**
   * Logs an warning message with a throwable.
   * 
   * @param message the warning message.
   * @param thrown the thrown throwable.
   */
  void warn(final String message, final Throwable thrown);

}