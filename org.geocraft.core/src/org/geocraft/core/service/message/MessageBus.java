/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.message;


import java.util.List;

import org.bushe.swing.event.ThreadSafeEventService;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.core.service.message.IMessageSubscriber;


/**
 * The message bus is a simple wrapper on top of the Event Bus
 * library that should ensure everyone is using the same bus 
 * consistently. 
 * 
 * This design presumes we will only ever need a single event
 * bus which may not be valid. 
 * 
 * If people want to use a separate event bus then we want to know 
 * about it. We could extend this class then or perhaps there will 
 * always be a need for a simple interface into the default GeoCraft 
 * event bus. 
 */
public class MessageBus implements IMessageService {

  /** The event service. */
  private ThreadSafeEventService _eventService;

  public MessageBus() {
    System.out.println("EventBus message service created.");
    _eventService = new ThreadSafeEventService();
  }

  public synchronized void publish(String topic, Object message) {
    System.out.println("EventBus " + _eventService + ": publishing " + topic + " " + message);
    _eventService.publish(topic, message);
    for (Object object : _eventService.getSubscribers(topic)) {
      System.out.println("...subscribers: " + ((MessageSubscriber) object).getSubscriber());
    }
  }

  public synchronized void subscribe(String topic, IMessageSubscriber subscriber) {
    List subscribers = _eventService.getSubscribers(topic);
    for (Object object : subscribers) {
      if (object instanceof MessageSubscriber) {
        MessageSubscriber temp = (MessageSubscriber) object;
        if (temp.getSubscriber() != null && temp.getSubscriber().equals(subscriber)) {
          return;
        }
      }
    }
    System.out.println("EventBus " + _eventService + ": " + subscriber + " subscribing to " + topic);
    _eventService.subscribe(topic, new MessageSubscriber(subscriber));
    for (Object object : _eventService.getSubscribers(topic)) {
      System.out.println("...subscribers: " + ((MessageSubscriber) object).getSubscriber());
    }
  }

  public synchronized void unsubscribe(String topic, IMessageSubscriber subscriber) {
    List subscribers = _eventService.getSubscribers(topic);
    for (Object object : subscribers) {
      if (object instanceof MessageSubscriber) {
        MessageSubscriber temp = (MessageSubscriber) object;
        if (temp.getSubscriber() != null && temp.getSubscriber().equals(subscriber)) {
          _eventService.unsubscribe(topic, temp);
          System.out.println(subscriber + " unsubscribing to " + topic);
          for (Object obj : _eventService.getSubscribers(topic)) {
            System.out.println("...subscribers: " + ((MessageSubscriber) object).getSubscriber());
          }
          return;
        }
      }
    }
  }
}
