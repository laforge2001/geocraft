/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.session;


import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Default implementation of a session service.
 */
public class DefaultSessionService implements ISessionService {

  /** The collection of session components, mapped by their unique keys. */
  private final Map<String, ISessionComponent> _sessionComponents;

  /**
   * Constructs a default implementation of a session service.
   */
  public DefaultSessionService() {
    // Create the collection of session components.
    _sessionComponents = Collections.synchronizedMap(new HashMap<String, ISessionComponent>());
  }

  public synchronized void registerSessionComponent(final ISessionComponent sessionComponent) {
    String key = sessionComponent.getKey();
    if (!_sessionComponents.containsKey(key)) {
      _sessionComponents.put(key, sessionComponent);
    }
  }

  public synchronized void unregisterSessionComponent(final ISessionComponent sessionComponent) {
    String key = sessionComponent.getKey();
    if (_sessionComponents.containsKey(key)) {
      _sessionComponents.remove(key);
    }
  }

  public synchronized void saveSession(final File sessionFile) {
    for (ISessionComponent sessionComponent : _sessionComponents.values().toArray(new ISessionComponent[0])) {
      sessionComponent.getSessionData();
    }
  }

  public synchronized void restoreSession(final File sessionFile) {
    for (ISessionComponent sessionComponent : _sessionComponents.values().toArray(new ISessionComponent[0])) {
      sessionComponent.setSessionData(null);
    }
  }

}
