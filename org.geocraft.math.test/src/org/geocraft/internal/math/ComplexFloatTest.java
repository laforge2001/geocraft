package org.geocraft.internal.math;

import junit.framework.TestCase;

public class ComplexFloatTest extends TestCase {

	  /**
	   * Test the parameterized constructor.
	   */
	  public void testConstructor2() {
	    float real = 1.23f;
	    float imag = -0.37f;
	    ComplexFloat complex = new ComplexFloat(real, imag);
	    assertEquals(1.23f, complex._real);
	    assertEquals(-0.37f, complex._imag);
	    assertEquals(1.23f, complex.getReal());
	    assertEquals(-0.37f, complex.getImag());

	  }

}
