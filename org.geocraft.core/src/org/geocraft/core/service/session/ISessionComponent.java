/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.session;


import java.util.HashMap;


/**
 * The interface for system components capable of saving/restoring their session state.
 */
public interface ISessionComponent {

  /**
   * Gets the unique key for the session component to identify it within a session service.
   * 
   * @return the unique key.
   */
  String getKey();

  /**
   * Gets a <code>HashMap</code> session data object containing the session state.
   * 
   * @return the session data object.
   */
  HashMap<String, String> getSessionData();

  /**
   * Sets the session state from a <code>HashMap</code> session data object.
   * 
   * @param sessionData the session data object.
   */
  void setSessionData(HashMap<String, String> sessionData);
}
