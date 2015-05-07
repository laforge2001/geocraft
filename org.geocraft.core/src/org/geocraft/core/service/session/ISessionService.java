/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.session;


import java.io.File;


/**
 * The interface for a session service.
 */
public interface ISessionService {

  /**
   * Adds a session component to the session service.
   * <p>
   * When saving a session, the service will query each registered session component
   * to obtain its session state.
   * 
   * @param sessionComponent the session component to register.
   */
  void registerSessionComponent(ISessionComponent sessionComponent);

  /**
   * Unregisters a session component with the session service.
   * <p>
   * Components should be unregistered when they are disposed of.
   * 
   * @param sessionComponent the session component to unregister.
   */
  void unregisterSessionComponent(ISessionComponent sessionComponent);

  /**
   * Saves session information to the specified session file.
   * <p>
   * If the file exists, it will be completely overwritten.
   * 
   * @param sessionFile the session file.
   */
  void saveSession(File sessionFile);

  /**
   * Restores session information from the specified session file.
   * 
   * @param sessionFile the session file.
   */
  void restoreSession(File sessionFile);
}
