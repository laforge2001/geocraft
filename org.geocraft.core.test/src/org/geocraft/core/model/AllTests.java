package org.geocraft.core.model;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test suite for core model tests.
 * With Tycho, tests are discovered automatically by tycho-surefire-plugin.
 * This suite exists for running tests from the Eclipse IDE.
 */
public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Core Tests");
    return suite;
  }

}
