/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.jms;


import javax.jms.Message;
import javax.jms.MessageListener;

import org.geocraft.core.service.ServiceProvider;


/**
 * Remembers the topic that you subscribed with 
 * so that it can pass events on to the correct 
 * topic in the GeoCraft message bus. 
 */
public class JMSTopicConsumer implements MessageListener {

  String _topic;

  public JMSTopicConsumer(String topic) {
    ServiceProvider.getLoggingService().getLogger(getClass())
        .debug("Creating a new message listener for topic: " + topic);
    _topic = topic;
  }

  @Override
  public void onMessage(Message msg) {
    // broadcast received message unchanged to the eventbus service
    //    ServiceProvider.getLoggingService().getLogger(getClass())
    //        .debug("Unrecognized JMS message type - rebroadcasting unchanged.");
    ServiceProvider.getMessageService().publish(_topic, msg);
  }
}
