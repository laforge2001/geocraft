/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.logging;


public class NullLoggingService implements ILoggingService {

  private final ILogger _logger;

  public NullLoggingService() {
    _logger = new NullLogger();
  }

  public ILogger getLogger(final String bundleName) {
    return _logger;
  }

  public ILogger getLogger(final Class klass) {
    return _logger;
  }

}
