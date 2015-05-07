package org.geocraft.unittest.suite;


import junit.framework.Test;
import junit.framework.TestSuite;

import com.rcpquickstart.bundletestcollector.BundleTestCollector;


public class AllTests extends BundleTestCollector {

  public static Test suite() {
    BundleTestCollector testCollector = new BundleTestCollector();

    TestSuite suite = new TestSuite("All Tests");

    /**
     * Assemble as many collections as you like based on bundle, package and
     * classname filters.
     */
    testCollector.collectTests(suite, "org.geocraft.", "org.geocraft.", "*Test");
    testCollector.collectTests(suite, "org.geocraft.", "org.geocraft.", "*TestCase");
    testCollector.collectTests(suite, "com.cop.spark.", "com.cop.spark.", "*Test");
    testCollector.collectTests(suite, "com.cop.spark.", "com.cop.spark.", "*TestCase");
    //		testCollector.collectTests(suite, "org.geocraft.", "org.geocraft.",
    //				"*UiTst");

    return suite;

  }

}
