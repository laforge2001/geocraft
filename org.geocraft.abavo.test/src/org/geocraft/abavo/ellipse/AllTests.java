/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.geocraft.abavo.ellipse");
    //$JUnit-BEGIN$
    suite.addTestSuite(EllipseRegionsModelEventTest.class);
    suite.addTestSuite(EllipseRegionsClassifierTest.class);
    //$JUnit-END$
    return suite;
  }

}
