/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.remote;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

import org.geocraft.core.service.ServiceProvider;


public class PassToMessageBusListener implements MessageListener {

  private static PassToMessageBusListener _instance = null;

  /*
   * Don't think we need multiple instances of this listener for job updates,
   * so using Singleton pattern. Change this if thats not the case...
   */
  static public PassToMessageBusListener getInstance() {
    if (_instance == null) {
      _instance = new PassToMessageBusListener();
    }
    return _instance;
  }

  private PassToMessageBusListener() {
  }

  /* (non-Javadoc)
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
   */
  @Override
  public void onMessage(Message message) {
    if (message instanceof ObjectMessage) {
      ObjectMessage obj = (ObjectMessage) message;
      try {
        Topic topic = (Topic) message.getJMSDestination();

        //TODO need to somehow hide the Message object from everyone else
        ServiceProvider.getMessageService().publish(topic.getTopicName(), obj);
      } catch (JMSException e) {
        e.printStackTrace();
      }

    }

  }
}
