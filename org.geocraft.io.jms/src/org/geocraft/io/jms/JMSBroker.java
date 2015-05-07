package org.geocraft.io.jms;


// a wrapper around the Broker .... 
import org.apache.activemq.broker.BrokerService;


public class JMSBroker implements Runnable {

  private BrokerService broker;

  private String _hostname;

  private int _portnum;

  protected JMSBroker(String hostname, int portnum) {
    broker = new BrokerService();
    _hostname = hostname;
    _portnum = portnum;
  }

  private void startServer() throws Exception {
    //int port = 3553;
    //String host = "hololw28";
    // MsgServiceServer broker = new MsgServiceServer(host, port);

    broker.setUseJmx(false); //java management extensions

    // _broker.setDataDirectory(new File(_logdir, DATA_DIRECTORY));
    broker.setPersistent(false);

    String openWireUrl = String.format("tcp://%s:%d", _hostname, _portnum);
    broker.addConnector(openWireUrl);

    //TODO this would allow native code to communicate if we want to add
    //that support
    //      String stompUrl = String.format("stomp://%s:%d", _host, _stompPort);
    //      _broker.addConnector(stompUrl);

    broker.start();

    // broker.startMessagingServer();
    //Wait until the broker has been shutdown so we don't exit the main
    while (!broker.isStarted()) {
      Thread.sleep(1000);
    }
    //    System.out.println("Messaging Service Exiting");
  }

  public void stopServer() throws Exception {
    System.out.println("Messaging Service Exiting");
    broker.stop();
  }

  public boolean isStarted() {
    return broker.isStarted();
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    try {
      startServer();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}