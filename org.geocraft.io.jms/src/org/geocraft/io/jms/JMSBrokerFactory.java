/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.jms;


import java.util.HashMap;
import java.util.Map;


public class JMSBrokerFactory {

  private static Map<String, JMSBroker> _brokers = new HashMap<String, JMSBroker>();

  /**
   * Factory method to ensure that GeoCraft only connects once to
   * each host:port address. 
   * 
   * @param host machine name "localhost" or "hopelx01"
   * @param port e.g. 5107 or 6005;
   */
  public static JMSBroker getJMSBroker(String host, int port) {
    JMSBroker broker = _brokers.get(host + ":" + port);
    if (broker == null) {
      broker = new JMSBroker(host, port);
      _brokers.put(host + ":" + port, broker);
      new Thread(broker, "JMS Broker").start();
    }
    return broker;
  }

}
