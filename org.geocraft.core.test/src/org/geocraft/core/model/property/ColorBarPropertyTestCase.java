package org.geocraft.core.model.property;


import junit.framework.TestCase;

import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.map.GrayscaleColorMap;


/**
 * Test case for the <code>ColorBarProperty</code> class.
 */
public class ColorBarPropertyTestCase extends TestCase {

  /**
   * Unit test for the pickle and unpickle methods.
   */
  public void testPickle() {
    // Create a color bar to test.
    int numColors = 64;
    double startValue = -40.87;
    double endValue = 178.23;
    double stepValue = 12.5;
    ColorBar colorBarIn = new ColorBar(numColors, new GrayscaleColorMap(), startValue, endValue, stepValue);

    // Create a pair of color bar properties.
    ColorBarProperty propertyIn = new ColorBarProperty("Color Bar In", colorBarIn);
    ColorBarProperty propertyOut = new ColorBarProperty("Color Bar Out", null);

    // Get the string from the pickle method.
    String pickleStr = propertyIn.pickle();

    // Unpickle the 2nd property using the pickle string.
    propertyOut.unpickle(pickleStr);

    // Get the color bar from the 2nd property.
    ColorBar colorBarOut = propertyOut.get();

    // Check the # of colors are the same.
    assertEquals(numColors, colorBarOut.getNumColors());

    // Check that each color (r,g,b) is the same.
    for (int i = 0; i < numColors; i++) {
      assertEquals(colorBarIn.getColor(i), colorBarOut.getColor(i));
    }

    // Check the start,end,step values are the same.
    assertEquals(startValue, colorBarOut.getStartValue());
    assertEquals(endValue, colorBarOut.getEndValue());
    assertEquals(stepValue, colorBarOut.getStepValue());
  }

}
