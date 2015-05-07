/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.jms;


import java.io.Serializable;
import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageSubscriber;


public class JMSQueueProducer implements IMessageSubscriber {

  MessageProducer _producer;

  Session _session;

  String _topicName;

  public JMSQueueProducer(Session session, MessageProducer producer, String inputTopicName) {
    _session = session;
    _producer = producer;
    _topicName = inputTopicName;

    ServiceProvider.getLoggingService().getLogger(getClass()).debug("Subscribing to: " + _topicName);
    ServiceProvider.getMessageService().subscribe(_topicName, this);
  }

  public void broadcast(Serializable payload) {

    ServiceProvider.getLoggingService().getLogger(getClass()).debug("sending serialized payload to the JMS queue");
    try {
      ObjectMessage msg = _session.createObjectMessage();
      msg.setObject(payload);
      _producer.send(msg);
    } catch (JMSException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void messageReceived(String topicName, Object payload) {

    long[] pile = (long[]) payload;

    ServiceProvider.getLoggingService().getLogger(getClass()).debug("Received pile event " + Arrays.toString(pile));

    if (payload == null || !(payload instanceof Serializable)) {
      throw new RuntimeException("cannot broadcast this payload to JMS queue " + _topicName + " " + payload);
    }

    broadcast((Serializable) payload);
  }
}
