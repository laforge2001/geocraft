package org.geocraft.core.service.message;


import junit.framework.TestCase;


public class DefaultMessageServiceTestCase extends TestCase implements IMessageSubscriber {

  /** Event bus test topic. */
  private static final String TEST = "Test";

  private static DefaultMessageService MESSAGE_SERVICE = new DefaultMessageService();

  /** A flag to be used to track the received events. */
  private boolean _eventReceived = false;

  /**
   * The constructor.
   * 
   * @param testName
   *          the test name
   */
  public DefaultMessageServiceTestCase(final String testName) {
    super(testName);
    MESSAGE_SERVICE.subscribe(TEST, this);
  }

  /**
   * Event bus test.
   */
  public void testEvent() {
    MESSAGE_SERVICE.publish(TEST, TEST);
    assertTrue(_eventReceived);
    _eventReceived = false;
    MESSAGE_SERVICE.unsubscribe(TEST, this);
    MESSAGE_SERVICE.publish(TEST, TEST);
    assertFalse(_eventReceived);
  }

  /**
   * Called whenever an EventBus event is received.
   * 
   * @param topic
   *          the topic name
   * @param data
   *          the data transmitted from the publisher
   */
  public void messageReceived(final String topic, final Object data) {
    _eventReceived = data != null && data.toString().equals(TEST);
  }

}
