/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.message;


import org.bushe.swing.event.EventTopicSubscriber;
import org.geocraft.core.service.message.IMessageSubscriber;


public class MessageSubscriber implements EventTopicSubscriber {

  public IMessageSubscriber _subscriber;

  public MessageSubscriber(IMessageSubscriber subscriber) {
    _subscriber = subscriber;
  }

  public IMessageSubscriber getSubscriber() {
    return _subscriber;
  }

  public void onEvent(String topic, Object message) {
    System.out.println("OnEvent: " + topic + " " + message + "...");
    if (_subscriber != null) {
      System.out.println("...sending MessageReceived: " + topic + " " + message);
      _subscriber.messageReceived(topic, message);
    }
  }

}
