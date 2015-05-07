/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import junit.framework.TestCase;


/**
 *
 */
public class LabelsTest extends TestCase {

  public LabelsTest(final String testName) {
    super(testName);
  }

  public void testLabels() {

    // simple tests that illustrate how it works. 
    guessStepLabelTester(0, 10, 2, new String[] { "0", "10" });
    guessStepLabelTester(0, 10, 5, new String[] { "0", "5", "10" });

    // test it chooses sensible start and end values. 
    guessStepLabelTester(1.5, 10.5, 5, new String[] { "2", "4", "6", "8", "10" });
    guessStepLabelTester(0, -10, 5, new String[] { "0", "-5", "-10" });

    // test negative start value
    guessStepLabelTester(-21.5, 10.5, 10, new String[] { "-20", "-15", "-10", "-5", "0", "5", "10" });

    // test decreasing range 
    guessStepLabelTester(0.2, -10.3, 5, new String[] { "0", "-5", "-10" });

    // test small numbers
    guessStepLabelTester(0, 0.001, 5, new String[] { "0", "0.0005", "0.001" });
    guessStepLabelTester(0, 0.00001, 7, new String[] { "0", "0.000002", "0.000004", "0.000006", "0.000008", "0.00001" });

    // test large numbers
    guessStepLabelTester(1000000, 1000005, 5, new String[] { "1000000", "1000001", "1000002", "1000003", "1000004",
        "1000005" });
    guessStepLabelTester(-1000000, 1000000, 5, new String[] { "-1000000", "-500000", "0", "500000", "1000000" });

    // test it handles same start and end
    guessStepLabelTester(0, 0, 5, new String[] { "0" });
    fixedStepLabelTester(1000000, 1000000, 0.1, new String[] { "1000000" });

    fixedStepLabelTester(0, 10, 2.0, new String[] { "0", "2", "4", "6", "8", "10" });
  }

  private void guessStepLabelTester(final double start, final double end, final int numLabels, final String[] expected) {
    System.out.println(start + " " + end + " " + numLabels);
    Labels labels = new Labels(start, end, numLabels);
    String[] results = labels.getZLabels();
    assertEquals(expected.length, results.length);
    for (int i = 0; i < expected.length; i++) {
      System.out.println(expected[i] + " " + results[i]);
      assertEquals(expected[i], results[i]);
    }
  }

  private void fixedStepLabelTester(final double start, final double end, final double step, final String[] expected) {
    Labels labels = new Labels(start, end, step);
    String[] results = labels.getZLabels();
    assertEquals(expected.length, results.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], results[i]);
    }
  }

  public void testFormat() {

    String[][] tests = new String[][] { { "100", "%3.0f" }, { "10", "%2.0f" }, { "1", "%1.0f" }, { "0", "%1.0f" },
        { "900", "%3.0f" }, { "90", "%2.0f" }, { "9", "%1.0f" },

        { "0.100", "%2.1f" }, { "0.0100", "%3.2f" }, { "0.00100", "%4.3f" },

        { "0.900", "%2.1f" }, { "0.0900", "%3.2f" }, { "0.00900", "%4.3f" },

        { "0.0000000000000000000001", "%1.0f" }, { "0.1000000000000000000001", "%2.1f" },

        { "0.9000000000000000000001", "%2.1f" }, { "0.9000000134110451", "%2.1f" },

        { "-0", "%1.0f" }, { "-0.1", "%2.1f" }, { "-0.01", "%3.2f" }, { "-0.001", "%4.3f" },

        { "-2", "%1.0f" }, { "-0.2", "%2.1f" }, { "-0.02", "%3.2f" }, { "-0.002", "%4.3f" },

        { "-9", "%1.0f" }, { "-0.9", "%2.1f" }, { "-0.09", "%3.2f" }, { "-0.009", "%4.3f" },

    };

    for (String[] test : tests) {
      double val = Double.parseDouble(test[0]);
      String oldFormat = test[1];
      String newFormat = Labels.getFormat(val);
      assertEquals("problem with " + val, oldFormat, newFormat);
    }
  }

}
