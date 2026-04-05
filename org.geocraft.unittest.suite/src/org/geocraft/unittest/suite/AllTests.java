package org.geocraft.unittest.suite;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Master test suite for all GeoCraft unit tests.
 * With Tycho, tests are discovered automatically by tycho-surefire-plugin.
 * This suite exists for running tests from the Eclipse IDE.
 */
public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("All Tests");
    return suite;
  }

}
