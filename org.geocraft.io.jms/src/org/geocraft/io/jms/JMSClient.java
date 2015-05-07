package org.geocraft.io.jms;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


/**
 * Connect to a JMS broker on the specified host and port number. 
 * 
 * Allows people to register for topics that will be rebroadcast on the message service.
 */
public class JMSClient {

  /** The logger. */
  private final ILogger _logger = ServiceProvider.getLoggingService().getLogger(JMSClient.class);

  private Connection _connection = null;

  private boolean _connected = false;

  private InitialContext _context = null;

  private String _host = null;

  private int _port = -1;

  private Map<String, JMSDestination> _destinations = new HashMap<String, JMSDestination>();

  /**
   * Nest class encapsulates JMS Destination objects
   *
   */
  class JMSDestination {

    Destination _destination = null;

    Session _session = null;

    MessageProducer _producer = null;

    /*MessageConsumer _consumer = null;*/

    Set<MessageConsumer> _consumers = new HashSet<MessageConsumer>();

    public JMSDestination(Destination destination, Session session, MessageProducer producer, MessageConsumer consumer) {
      _destination = destination;
      _session = session;
      _producer = producer;
      _consumers.add(consumer);
      //      _consumer = consumer;
    }
  }

  /**
   * Connections are resource intensive and are only meant to be created once per Application, 
   * so the constructor only arranges the connection once using the host and port as parameters
   * 
   * @param host of the JMS provider (the ActiveMQ) service
   * @param port of the JMS provider (the ActiveMQ) service
    */
  protected JMSClient(String host, int port) {

    if (_connected || host == null) {
      return;
    }

    ServiceProvider.getLoggingService().getLogger(getClass())
        .debug("Connecting to new JMS broker: " + host + " " + port);

    _host = host;
    _port = port;

    try {
      ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) jndiLookup("ConnectionFactory");
      //      PooledConnectionFactory pooledFactory = new PooledConnectionFactory(connectionFactory);
      _connection = connectionFactory.createConnection();
      _connection.start();
      _connected = true;
      ServiceProvider.getLoggingService().getLogger(getClass())
          .debug("Successfully started monitoring the jms server." + _host + "@" + _port);
    } catch (JMSException e) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error("Problem connecting to JMS broker", e);
      e.printStackTrace();
    }
  }

  /**
   * Creates a JMS destination object (either a Queue or Topic type) to handle consuming and producing 
   * JMS messages
   * 
   * @param name the name of the topic or queue
   * @param type if type equals the explicit class name javax.jms.Queue.class this method will designate
   * the message type as a Queue (point to point) otherwise it will set it to be a Topic (pub-sub)
   * @throws JMSException
   */
  public void createDestination(String name, Class type) throws JMSException {
    this.createDestination(name, type, false, Session.AUTO_ACKNOWLEDGE);
  }

  /**
   * Creates a JMS destination object (either a Queue or Topic type) to handle consuming and producing 
   * JMS messages
   * 
   * @param name the name of the topic or queue
   * @param type if type equals the explicit class name javax.jms.Queue.class this method will designate
   * the message type as a Queue (point to point) otherwise it will set it to be a Topic (pub-sub)
   * @param fTransacted boolean to notify that is is a transacted message
   * @param ackMode flag for setting the acknowledge messaging mode
   * @throws JMSException
   */
  public void createDestination(String name, Class type, boolean fTransacted, int ackMode) throws JMSException {
    // If the destination already exists, just return
    //
    if (_destinations.get(name) != null) {
      return;
    }

    // Create the new destination and store it
    //
    Session session = _connection.createSession(fTransacted, ackMode);

    // Look up the destination otherwise create it
    //
    Destination destination = (Destination) jndiLookup(name);
    if (destination == null) {
      // Create a topic or queue as specified
      //
      if (type.getName().equals("javax.jms.Queue")) {
        _logger.debug("createDestination(" + name + ") - creating Queue");
        destination = session.createQueue(name);
      } else {
        _logger.debug("createDestination(" + name + ") - creating Topic");
        destination = session.createTopic(name);
      }
    }

    JMSDestination jmsDest = new JMSDestination(destination, session, null, null);

    _destinations.put(name, jmsDest);
  }

  private InitialContext createContext() throws NamingException {
    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
    String url = String.format("tcp://%s:%d", _host, _port);
    props.setProperty(Context.PROVIDER_URL, url);
    return new InitialContext(props);
  }

  private Object jndiLookup(String name) {
    try {
      if (_context == null) {
        _context = createContext();
      }

      return _context.lookup(name);
    } catch (Exception e) {
      return null;
    }
  }

  private JMSDestination getJMSDestination(String name) throws JMSException {
    JMSDestination jmsDest = _destinations.get(name);

    if (jmsDest == null) {
      _logger.debug("destination was null - so creating");
      this.createDestination(name, Topic.class);
      jmsDest = _destinations.get(name);
    }

    return jmsDest;
  }

  /**
   * creates a JMS TextMessage object given the topic/queue name
   * @param name of the topic/queue
   * @return a new TextMessage
   * @throws JMSException
   */
  public TextMessage createTextMessage(String name) throws JMSException {
    JMSDestination destination = getJMSDestination(name);
    return destination._session.createTextMessage();
  }

  /**
   * creates a JMS ObjectMessage object given the topic/queue name
   * @param name of the topic/queue
   * @return a new ObjectMessage
   * @throws JMSException
   */
  public ObjectMessage createObjectMessage(String name) throws JMSException {
    JMSDestination destination = getJMSDestination(name);
    return destination._session.createObjectMessage();
  }

  /**
   * creates a JMS BytesMessage object given the topic/queue name
   * @param name of the topic/queue
   * @return a new BytesMessage
   * @throws JMSException
   */
  public BytesMessage createBytesMessage(String name) throws JMSException {
    JMSDestination destination = getJMSDestination(name);
    return destination._session.createBytesMessage();
  }

  /**
   * creates a JMS StreamMessage object given the topic/queue name
   * @param name of the topic/queue
   * @return a new StreamMessage
   * @throws JMSException
   */
  public StreamMessage createStreamMessage(String name) throws JMSException {
    JMSDestination destination = getJMSDestination(name);
    return destination._session.createStreamMessage();
  }

  /**
   * Starts an asynchronous listen using the passed in message listener as a callback.
   * When messages are received by this destination name, they will trigger the onMessage() method of
   * the MessageListener
   * 
   * @param destName destination name (of Topic/Queue)
   * @param callback the message listener to handle callbacks
   * @throws JMSException
   */
  public void listen(String destName, MessageListener callback) throws JMSException {
    listen(destName, callback, null);
  }

  /**
   * Starts an asynchronous listen using the passed in message listener as a callback.
   * When messages are received by this destination name, they will trigger the onMessage() method of
   * the MessageListener
   * 
   * @param destName destination name (of Topic/Queue)
   * @param callback the message listener to handle callbacks
   * @param messageSelector this is an SQL query for filtering particular kinds of messages
   * @throws JMSException
   */
  public void listen(String destName, MessageListener callback, String messageSelector) throws JMSException {
    //    _logger.debug("JMSClient.listen(" + destName + ", " + callback + ")");

    JMSDestination jmsDest = getJMSDestination(destName);

    // Set the caller as a topic subcriber or queue receiver as appropriate
    //
    setupAsynchConsumer(jmsDest, callback, messageSelector);

    //    _logger.debug("listen() - Asynchronous listen on destination " + destName);
  }

  /**
   * This is a synchronous listen for the passed in destination name. It will block until a message is
   * received.
   * 
   * @param destName destination name (of Topic/Queue)
   * @return the received message
   * @throws JMSException
   */
  public Message listen(String destName) throws JMSException {
    _logger.debug("listen() - Synchronous listen on destination " + destName);

    JMSDestination jmsDest = getJMSDestination(destName);

    // Setup the consumer and block until a
    // message arrives for this destination
    //
    return setupSynchConsumer(jmsDest, 0);
  }

  /**
   * This is a synchronous listen for the passed in destination name. It will block until a message is
   * received or the timeout has passed.
   * 
   * @param destName destination name (of Topic/Queue)
   * @param timeout the number of milliseconds to wait
   * @return
   * @throws JMSException
   */
  public Message listen(String destName, int timeout) throws JMSException {
    _logger.debug("listen() - Synchronous listen on destination " + destName);

    JMSDestination jmsDest = getJMSDestination(destName);

    // Setup the consumer and block until a
    // message arrives for this destination
    //
    return setupSynchConsumer(jmsDest, timeout);
  }

  /**
   * This is an asynchronous listen, that takes in a Destination object instead of a name
   * 
   * @param dest destination name (of Topic/Queue)
   * @param callback the message listener to handle callbacks
   * @throws JMSException
   */
  public void listen(Destination dest, MessageListener callback) throws JMSException {
    Session s = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer c = s.createConsumer(dest);
    c.setMessageListener(callback);
  }

  /**
   * This is a synchronous listen for the passed in destination. It will block until a message is
   * received.
   * 
   * @param destName destination name (of Topic/Queue)
   * @return the received message
   * @throws JMSException
   */
  public Message listen(Destination dest) throws JMSException {
    Session s = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer c = s.createConsumer(dest);
    Message msg = c.receive();
    s.close();
    return msg;
  }

  /**
   * Sends the given Message to the named destination
   * 
   * @param destName the name of the Topic or Queue
   * @param msg the JMS message to send
   * @throws JMSException
   */
  public void send(String destName, Message msg) throws JMSException {
    JMSDestination jmsDest = getJMSDestination(destName);

    // Make sure we have a message producer created for this destination
    //
    setupProducer(jmsDest);

    // Send the message for this destination
    //
    jmsDest._producer.send(msg);
    //    _logger.debug("send() - message sent");
  }

  /**
   * Sends the given Message to the given destination
   * 
   * @param dest the Topic or Queue object to send to
   * @param msg the JMS message to send
   * @throws JMSException
   */
  public void send(Destination dest, Message msg) throws JMSException {
    Session s = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer p = s.createProducer(dest);
    p.send(msg);
    s.close();
  }

  /**
   * Sends a serializable object to the given destination name
   * 
   * @param destName the name of the Topic or Queue to send to
   * @param obj the object to send
   * @throws JMSException
   */
  public void send(String destName, Serializable obj) throws JMSException {
    JMSDestination jmsDest = getJMSDestination(destName);

    // Make sure we have a message producer created for this destination
    //
    setupProducer(jmsDest);

    // Send the message for this destination
    //
    Message msg = createJMSMessage(obj, jmsDest._session);
    jmsDest._producer.send(msg);
    _logger.debug("send() - message sent");
  }

  /**
   * Helper method that sends a text message to the named destination
   * 
   * @param destName name of the Topic or Queue to send to
   * @param messageText the message to send
   * @throws JMSException
   */
  public void send(String destName, String messageText) throws JMSException {
    this.send(destName, (Serializable) messageText);
  }

  /**
   * shuts down the named destination by closing all of its producers, consumers and the session itself
   * 
   * @param destName name of the topic/queue to shutdown
   */
  public void stop(String destName) {
    try {
      // Look for an existing destination for the given destination
      //
      JMSDestination jmsDest = _destinations.get(destName);
      if (jmsDest != null) {
        // Close out all JMS related state
        //
        if (jmsDest._producer != null) {
          jmsDest._producer.close();
        }
        if (!jmsDest._consumers.isEmpty()/*jmsDest._consumer != null*/) {
          List<MessageConsumer> closedConsumers = new ArrayList<MessageConsumer>();
          for (MessageConsumer consumer : jmsDest._consumers) {
            consumer.close();
            closedConsumers.add(consumer);
          }
          jmsDest._consumers.removeAll(closedConsumers);
        }
        if (jmsDest._session != null) {
          jmsDest._session.close();
        }

        jmsDest._destination = null;
        jmsDest._session = null;
        jmsDest._producer = null;
        jmsDest._consumers = null;

        // Remove the JMS client entry
        //
        _destinations.remove(destName);
        jmsDest = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setupProducer(JMSDestination jmsDest) throws JMSException {
    if (jmsDest._producer != null) {
      return;
    }

    jmsDest._producer = jmsDest._session.createProducer(jmsDest._destination);
  }

  private void setupAsynchConsumer(JMSDestination jmsDest, MessageListener callback, String messageSelector) throws JMSException {
    MessageConsumer consumer = null;
    if (messageSelector != null) {
      consumer = jmsDest._session.createConsumer(jmsDest._destination, messageSelector);
    } else {
      consumer = jmsDest._session.createConsumer(jmsDest._destination);
    }

    consumer.setMessageListener(callback);
    jmsDest._consumers.add(consumer);
  }

  private Message setupSynchConsumer(JMSDestination jmsDest, int timeout) throws JMSException {
    MessageConsumer consumer = jmsDest._session.createConsumer(jmsDest._destination);
    jmsDest._consumers.add(consumer);
    if (timeout > 0) {
      return consumer.receive(timeout);
    }

    return consumer.receive();
  }

  private Message createJMSMessage(Serializable obj, Session session) throws JMSException {
    if (obj instanceof String) {
      TextMessage textMsg = session.createTextMessage();
      textMsg.setText((String) obj);
      return textMsg;
    }
    ObjectMessage objMsg = session.createObjectMessage();
    objMsg.setObject(obj);
    return objMsg;
  }

}
