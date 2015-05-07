package org.geocraft.math;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.geocraft.internal.math.ComplexFloatTest;


public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.geocraft.math");
    //$JUnit-BEGIN$
    suite.addTestSuite(ComplexFloatTest.class);
    //$JUnit-END$
    return suite;
  }
}
