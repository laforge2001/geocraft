/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.message;


/**
 * The interface for message subscribers.
 */
public interface IMessageSubscriber {

  /**
   * Invoked when a message is received from the message service.
   * 
   * @param topic the message topic (key).
   * @param message the message object.
   */
  void messageReceived(String topic, Object message);
}
