package org.geocraft.io.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import junit.framework.TestCase;

public class JmsClientTest extends TestCase {
	private JMSBroker _jmsBroker;
	private JMSClient _jmsClient;

	private String _testMessage;

	private static String TEST_DESTINATION = "testDestination";
	private static String TEST_MESSAGE = "TEST MESSAGE";

	@Override
	protected void setUp() throws Exception {
		_jmsBroker = new JMSBroker("localhost", 61616);
		new Thread(_jmsBroker).start();

		_jmsClient = JMSClientFactory.getJMSClient("localhost", 61616);
	}

	@Override
	protected void tearDown() throws Exception {
		_jmsBroker.stopServer();
	}

	public void testListenStringMessageListener() {

		Runnable listenRunner = new Runnable() {

			@Override
			public void run() {
				try {
					Message testMessage = _jmsClient.listen(TEST_DESTINATION);
					TextMessage textMessage = (TextMessage) testMessage;
					_testMessage = textMessage.getText();
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		};
		Thread listenThread = new Thread(listenRunner);
		listenThread.start();

		try {
			_jmsClient.send(TEST_DESTINATION, TEST_MESSAGE);
			listenThread.join();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		assertEquals(_testMessage, TEST_MESSAGE);

	}

}
