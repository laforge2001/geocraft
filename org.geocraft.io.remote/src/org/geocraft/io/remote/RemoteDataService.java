package org.geocraft.io.remote;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.activemq.broker.BrokerService;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.jms.JMSClient;
import org.geocraft.io.jms.JMSClientFactory;


/**
 * RemoteDataService instantiates a listener that listens for
 * incoming data and and broadcasts them on the event bus. 
 */
public class RemoteDataService implements IRemoteDataService {

  /** The logger. */
  private static final ILogger LOG = ServiceProvider.getLoggingService().getLogger(RemoteDataService.class);

  private List<LocalBroker> _localBrokers = new ArrayList<LocalBroker>();

  public RemoteDataService() {
    LOG.debug("Starting Remote Data Service");
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.remote.IRemoteDataService#createAndSubscribe(java.lang.String, java.lang.String, int)
   */
  @Override
  public void createAndSubscribe(String topic, String hostname, int port, String messageSelector) {
    startLocalBroker(hostname, port);
    subscribe(topic, hostname, port, messageSelector);
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.remote.IRemoteDataService#subscribe(java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public void subscribe(String topic, String hostname, int port, String messageSelector) {
    JMSClient client = JMSClientFactory.getJMSClient(hostname, port);

    // listen for incoming job results and rebroadcast them on the message bus. 
    try {
      //      LOG.debug("Listening for topic: " + topic);
      //      LOG.debug("Listening on hostname: " + hostname + " port: " + port);
      client.listen(topic, PassToMessageBusListener.getInstance(), messageSelector);
    } catch (JMSException e) {
      e.printStackTrace();
    }

  }

  /* (non-Javadoc)
   * @see org.geocraft.io.remote.IRemoteDataService#subscribe(java.lang.String)
   */
  @Override
  public void subscribe(String topic, String hostname, int port) {
    JMSClient client = JMSClientFactory.getJMSClient(hostname, port);

    // listen for incoming job results and rebroadcast them on the message bus. 
    try {
      //      LOG.debug("Listening for topic: " + topic);
      //      LOG.debug("Listening on hostname: " + hostname + " port: " + port);
      client.listen(topic, PassToMessageBusListener.getInstance(), null);
    } catch (JMSException e) {
      e.printStackTrace();
    }

  }

  /* (non-Javadoc)
   * @see org.geocraft.io.remote.IRemoteDataService#publish(java.lang.String, java.lang.String, int, java.lang.Object)
   */
  @Override
  public void publish(String topic, String hostname, int port, Serializable message) {
    JMSClient client = JMSClientFactory.getJMSClient(hostname, port);
    ObjectMessage msg;
    try {
      msg = client.createObjectMessage(topic);
      msg.setObject(message);
      client.send(topic, msg);
      //      LOG.debug("Sent " + message.toString() + " to topic: " + topic);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void publish(String topic, String hostname, int port, Message message) {
    JMSClient client = JMSClientFactory.getJMSClient(hostname, port);
    try {
      client.send(topic, message);
      //      LOG.debug("Sent " + message.toString() + " to topic: " + topic);
    } catch (JMSException e) {
      e.printStackTrace();
    }

  }

  public TextMessage createTextMessage(String topic, String hostname, int port) {
    JMSClient client = JMSClientFactory.getJMSClient(hostname, port);
    try {
      return client.createTextMessage(topic);
    } catch (JMSException e) {
      e.printStackTrace();
      return null;
    }
  }

  public ObjectMessage createObjectMessage(String topic, String hostname, int port) {
    JMSClient client = JMSClientFactory.getJMSClient(hostname, port);
    try {
      return client.createObjectMessage(topic);
    } catch (JMSException e) {
      e.printStackTrace();
      return null;
    }
  }

  public void stop(String topic, String hostname, int port) {
    JMSClient client = JMSClientFactory.getJMSClient(hostname, port);
    client.stop(topic);
  }

  public static void main(String args[]) {
    BrokerService broker = new BrokerService();
    try {
      //      broker.setUseJmx(false);
      broker.setDataDirectory("data/");
      broker.addConnector("tcp://localhost:56000");
      broker.start();

      //      subscribe(topic, hostname, port, messageSelector);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.remote.IRemoteDataService#createBroker(java.lang.String, int)
   */
  @Override
  public void startLocalBroker(final String hostname, final int port) {
    LocalBroker localBroker = new LocalBroker(hostname, port);
    _localBrokers.add(localBroker);
    localBroker.start();
  }

  public void stopAllLocalBrokers() {
    for (LocalBroker localBroker : _localBrokers) {
      localBroker.stop();
    }
  }

}
