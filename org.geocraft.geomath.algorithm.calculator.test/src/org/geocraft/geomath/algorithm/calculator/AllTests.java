package org.geocraft.geomath.algorithm.calculator;


import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.geocraft.geomath.algorithm.calculator");
    //$JUnit-BEGIN$
    suite.addTestSuite(CalculatorTest.class);
    //$JUnit-END$
    return suite;
  }

}
