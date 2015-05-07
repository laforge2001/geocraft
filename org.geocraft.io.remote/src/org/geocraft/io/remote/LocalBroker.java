/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.remote;


import java.net.URI;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


public class LocalBroker {

  /** The logger. */
  private static final ILogger LOG = ServiceProvider.getLoggingService().getLogger("org.geocraft.io.remote");

  private String _hostname;

  private int _port;

  private BrokerService _broker = null;

  /**
   * @param hostname
   * @param port
   */
  public LocalBroker(String hostname, int port) {
    _hostname = hostname;
    _port = port;
  }

  /**
   * 
   */
  public void start() {
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          _broker = BrokerFactory.createBroker(new URI("broker:tcp://" + _hostname + ":" + _port));
          //          _broker.stop();
          _broker.addConnector("tcp://" + _hostname + ":" + _port);
          _broker.setBrokerName("myBroker");
          _broker.setDataDirectory("data/");
          LOG.debug("Starting local broker at : " + _hostname + ":" + _port);
          _broker.start();
        } catch (Exception e) {
          e.printStackTrace();
        }

      }
    });
    t.start();

  }

  public void stop() {
    if (_broker != null) {
      try {
        LOG.debug("Stopping local broker at : " + _hostname + ":" + _port);
        _broker.stop();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    System.out.println("localbroker is dying!!!");
    super.finalize();
  }

}
