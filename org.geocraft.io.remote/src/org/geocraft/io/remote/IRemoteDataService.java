/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.remote;


import java.io.Serializable;

import javax.jms.Message;


public interface IRemoteDataService {

  public void startLocalBroker(String hostname, int port);

  public void stopAllLocalBrokers();

  public void createAndSubscribe(String topic, String hostname, int port, String messageSelector);

  /**
   * This subscribes to a topic and applies a filter using the message selector parameter.
   * The message selector is an SQL compatible string
   * 
   * @param topic topic being subscribed to
   * @param hostname the host where the JMS provider resides
   * @param port the port where the JMS provider resides
   * @param messageSelector the SQL formatted query string
   */
  public void subscribe(String topic, String hostname, int port, String messageSelector);

  /**
   * @param Topic
   * @param hostname
   * @param port
   */
  public void subscribe(String topic, String hostname, int port);

  /**
   * @param topic
   * @param hostname
   * @param port
   * @param message
   */
  public void publish(String topic, String hostname, int port, Serializable message);

  /**
   * @param topic
   * @param hostname
   * @param port
   * @param message
   */
  public void publish(String topic, String hostname, int port, Message message);

  public Message createTextMessage(String topic, String hostname, int port);

  public Message createObjectMessage(String topic, String hostname, int port);

  public void stop(String topic, String hostname, int port);
}
