/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.jms;


import java.util.HashMap;
import java.util.Map;


public class JMSClientFactory {

  private static Map<String, JMSClient> _clients = new HashMap<String, JMSClient>();

  /**
   * Factory method to ensure that GeoCraft only connects once to
   * each host:port address. 
   * 
   * @param host machine name "localhost" or "hopelx01"
   * @param port e.g. 5107 or 6005;
   */
  public static JMSClient getJMSClient(String host, int port) {
    JMSClient client = _clients.get(host + ":" + port);
    if (client == null) {
      client = new JMSClient(host, port);
      _clients.put(host + ":" + port, client);
    }
    return client;
  }
}
