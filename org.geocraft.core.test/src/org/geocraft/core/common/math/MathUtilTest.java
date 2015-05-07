package org.geocraft.core.common.math;


import junit.framework.TestCase;


public class MathUtilTest extends TestCase {

  public MathUtilTest(final String testName) {
    super(testName);
  }

  /**
   * Test of computeFormat method.
   */
  public void testComputeFormat() {

    String[][] tests = {

    { "500000000000", "10", "%10.5g" }, { "50000000000", "10", "%10.5g" }, { "5000000000", "10", "%10.0f" },
        { "500000000", "10", "%10.0f" }, { "50000000", "10", "%10.1f" }, { "5000000", "10", "%10.2f" },
        { "500000", "10", "%10.3f" }, { "50000", "10", "%10.4f" }, { "5000", "10", "%10.5f" },
        { "500", "10", "%10.6f" }, { "50", "10", "%10.7f" }, { "5", "10", "%10.8f" }, { "0.5", "10", "%10.8f" },
        { "-500000000000", "10", "%10.4g" }, { "-50000000000", "10", "%10.4g" }, { "-5000000000", "10", "%10.4g" },
        { "-500000000", "10", "%10.0f" }, { "-50000000", "10", "%10.0f" }, { "-5000000", "10", "%10.1f" },
        { "-500000", "10", "%10.2f" }, { "-50000", "10", "%10.3f" }, { "-5000", "10", "%10.4f" },
        { "-500", "10", "%10.5f" }, { "-50", "10", "%10.6f" }, { "-5", "10", "%10.7f" }, { "-0.5", "10", "%10.7f" },
        { "-0.05", "10", "%10.7f" }, { "0", "10", "%10.7f" }, { "-0", "10", "%10.7f" }, };

    for (String[] test : tests) {
      double range = Double.parseDouble(test[0]);
      int width = Integer.parseInt(test[1]);
      String result = MathUtil.computeFormat(range, width);
      String format = test[2];

      System.out.format(result + "   %s\n", range, result);
      assertEquals(format, result);
    }
  }

  public void testModulus() {

    assertEquals(5 % 2, 1); // what we expect

    assertFalse(-5 % 2 == 5 % 2); // Java % is strange/broken!
    assertFalse(5 % -2 == -1); // Java % is strange/broken!

    assertEquals(MathUtil.mod(5, 2), 1);
    assertEquals(MathUtil.mod(-5, 2), 1);

    assertEquals(-2 % 2, 0);
    assertEquals(MathUtil.mod(-2, 2), 0);

    assertEquals(MathUtil.mod(19, 64), 19);
    assertEquals(MathUtil.mod(-19, 64), 45);

    assertEquals(MathUtil.mod(9, 5), 4);
    assertEquals(MathUtil.mod(-9, 5), 1);

    // assertEquals(MathUtil.mod(9, -5), -1);
    // assertEquals(MathUtil.mod(-9, -5), -5);
  }

  public void testScale() {

    assertEquals(50, MathUtil.scale(0, 100, 1, 2, 1.5), Constants.DOUBLE_DELTA);
    assertEquals(50, MathUtil.scale(0, 100, -1, -2, -1.5), Constants.DOUBLE_DELTA);
  }

  public void testEqual() {
    assertTrue(MathUtil.isEqual(0.0f, 0.0d));
    assertFalse(MathUtil.isEqual(1, 0));
    assertTrue(MathUtil.isEqual(1e38, 1e37 * 10f));
    assertTrue(MathUtil.isEqual(-1, -1));
    assertTrue(MathUtil.isEqual(0.000000000001, 0.00000000000099999999));
    assertFalse(MathUtil.isEqual(0.000000000001, 0.0000000000009999));
    assertTrue(MathUtil.isEqual(-9999.999f, (float) -9999.999));
    assertTrue(MathUtil.isEqual(-9999.999f, -9999.999));
  }

  public void testConvert() {
    float[] singleFloat = { 1, 2, 3, 4, 5, 6 };

    // convert to a multi float
    float[][] multiDouble = MathUtil.convert(singleFloat, 2, 3);

    double[][] expectedDouble = { { 1, 2, 3 }, { 4, 5, 6 } };
    for (int i = 0; i < expectedDouble.length; i++) {
      for (int j = 0; j < expectedDouble[i].length; j++) {
        assertEquals(multiDouble[i][j], expectedDouble[i][j], Constants.DOUBLE_DELTA);
      }
    }

    // convert back to a singleFloat
    float[] result = MathUtil.convert(multiDouble);

    for (int i = 0; i < singleFloat.length; i++) {
      assertEquals(singleFloat[i], result[i], Constants.FLOAT_DELTA);
    }

  }

  public void testResample() {
    float[] dataIn = { 2, 5, 8, -3, -6, 7, 100, 43, -999, 3 };
    float[] dataOut = MathUtil.resample(dataIn);
    assertEquals(dataIn.length * 2 - 1, dataOut.length);
    for (int i = 0; i < dataIn.length; i++) {
      assertEquals(dataOut[i * 2], dataIn[i], Constants.FLOAT_DELTA);
    }
  }

  public void testComputePercentile() {
    float[][] data = new float[][] { { 99, 2, 3, 4 }, { 9, 10, 11, 99 }, { 5, 6, 7, 8 } };
    float[] result = MathUtil.computePercentiles(data, 99, 10);
    assertEquals(3, result[0], Constants.FLOAT_DELTA);
    assertEquals(10, result[1], Constants.FLOAT_DELTA);
  }
}
