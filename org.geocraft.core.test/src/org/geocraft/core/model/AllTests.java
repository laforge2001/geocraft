package org.geocraft.core.model;


import junit.framework.Test;
import junit.framework.TestSuite;

import com.rcpquickstart.bundletestcollector.BundleTestCollector;


public class AllTests {

  public static Test suite() {
    BundleTestCollector testCollector = new BundleTestCollector();

    TestSuite suite = new TestSuite("All Tests");

    /*
     * assemble as many collections as you like based on bundle, package and
     * classname filters
     */
    testCollector.collectTests(suite, "org.geocraft.", "org.geocraft.internal.core.", "*Test");

    return suite;

  }
  //  public static Test suite() {
  //    TestSuite suite = new TestSuite("Test for org.geocraft.core.model.persistence");
  //    suite.addTestSuite(MementoTest.class);
  //    return suite;
  //  }

}
