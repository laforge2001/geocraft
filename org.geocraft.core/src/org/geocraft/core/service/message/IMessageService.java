/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.message;


/**
 * The interface for a messaging service.
 */
public interface IMessageService {

  /**
   * Adds a subscriber to the message service for the given topic (key).
   * 
   * @param topic the message topic (key).
   * @param subscriber the message subscriber to add.
   */
  void subscribe(String topic, IMessageSubscriber subscriber);

  /**
   * Removes a subscriber from the message service for the given topic (key).
   * 
   * @param topic the message topic (key).
   * @param subscriber the message subscriber to remove.
   */
  void unsubscribe(String topic, IMessageSubscriber subscriber);

  /**
   * Publishes a message on the message service.
   * 
   * @param topic the message topic (key).
   * @param message the message object.
   */
  void publish(String topic, Object message);
}
