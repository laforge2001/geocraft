package org.geocraft.unittest.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.rcpquickstart.bundletestcollector.BundleTestCollector;

public class AllUiTests extends BundleTestCollector {
	
	public static Test suite() {
		BundleTestCollector testCollector = new BundleTestCollector();

		TestSuite suite = new TestSuite("All Tests");

		/*
		 * assemble as many collections as you like based on bundle, package and
		 * classname filters
		 */
		testCollector.collectTests(suite, "org.geocraft.", "org.geocraft.",
				"*UiTst");

		return suite;

	}

}
