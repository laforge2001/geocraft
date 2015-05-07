package org.geocraft.core;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.geocraft.core.service.message.DefaultMessageServiceTestCase;


public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test cases for org.geocraft.core");
    //$JUnit-BEGIN$
    suite.addTestSuite(DefaultMessageServiceTestCase.class);
    //$JUnit-END$
    return suite;
  }

}
