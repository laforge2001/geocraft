/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.message;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocraft.core.service.message.IMessageService;
import org.geocraft.core.service.message.IMessageSubscriber;


public class DefaultMessageService implements IMessageService {

  /** The collection of subscriber lists, mapped by topic. */
  private Map<String, List<IMessageSubscriber>> _subscribers;

  public DefaultMessageService() {
    System.out.println("Default message service created.");
    _subscribers = Collections.synchronizedMap(new HashMap<String, List<IMessageSubscriber>>());
  }

  public synchronized void publish(String topic, Object message) {
    // Get the list of subscribers for the specified topic.
    List<IMessageSubscriber> subscribers = _subscribers.get(topic);

    // If the list is null, then simply return.
    if (subscribers == null) {
      return;
    }

    // Otherwise, send the message object to all of the subscribers in the list.
    for (IMessageSubscriber subscriber : subscribers.toArray(new IMessageSubscriber[0])) {
      subscriber.messageReceived(topic, message);
    }
  }

  public synchronized void subscribe(String topic, IMessageSubscriber subscriber) {
    // Get the list of subscribers for the specified topic.
    List<IMessageSubscriber> subscribers = _subscribers.get(topic);

    // If the list is null, then create a new list and add it to the map.
    if (subscribers == null) {
      subscribers = Collections.synchronizedList(new ArrayList<IMessageSubscriber>());
      _subscribers.put(topic, subscribers);
    }

    // If the list does not contain the subscriber, then add it.
    if (!subscribers.contains(subscriber)) {
      subscribers.add(subscriber);
    }
  }

  public synchronized void unsubscribe(String topic, IMessageSubscriber subscriber) {
    // Get the list of subscribers for the specified topic.
    List<IMessageSubscriber> subscribers = _subscribers.get(topic);

    // If the list is null, then simply return.
    if (subscribers == null) {
      return;
    }

    // Otherwise, if the list contains the subscriber, remove it.
    if (subscribers.contains(subscriber)) {
      subscribers.remove(subscriber);
    }
  }

}
