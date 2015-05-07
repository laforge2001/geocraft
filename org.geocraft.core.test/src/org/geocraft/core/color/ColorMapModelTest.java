package org.geocraft.core.color;


import junit.framework.TestCase;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorMapModel;


public class ColorMapModelTest extends TestCase {

  public void testConstructors() {
    // Create a simple color model.
    int numColors = 3;
    RGB[] rgbs = new RGB[numColors];
    rgbs[0] = new RGB(255, 0, 0);
    rgbs[1] = new RGB(0, 255, 0);
    rgbs[2] = new RGB(0, 0, 255);
    ColorMapModel model = new ColorMapModel(rgbs);

    // Test the number of colors.
    assertEquals(numColors, model.getNumColors());

    // Test the colors.
    for (int i = 0; i < numColors; i++) {
      assertEquals(rgbs[i], model.getColor(i));
    }

    // Create a copy of the color model.
    ColorMapModel model2 = new ColorMapModel(model);

    // Test the number of colors.
    assertEquals(numColors, model2.getNumColors());

    // Test the colors.
    for (int i = 0; i < numColors; i++) {
      assertEquals(rgbs[i], model2.getColor(i));
    }
  }

  public void testColorReversal() {
    // Create a simple color model.
    int numColors = 3;
    RGB[] rgbs = new RGB[numColors];
    rgbs[0] = new RGB(255, 0, 0);
    rgbs[1] = new RGB(0, 255, 0);
    rgbs[2] = new RGB(0, 0, 255);
    ColorMapModel model = new ColorMapModel(rgbs);

    // Reverse the colors.
    model.reverseColors();

    // Test the colors have been reversed.
    assertEquals(numColors, model.getNumColors());
    for (int i = 0; i < numColors; i++) {
      assertEquals(rgbs[numColors - 1 - i], model.getColor(i));
    }
  }

  public void testColorSetters() {
    // Create a simple color model.
    int numColors = 3;
    RGB[] rgbs = new RGB[numColors];
    rgbs[0] = new RGB(255, 0, 0);
    rgbs[1] = new RGB(0, 255, 0);
    rgbs[2] = new RGB(0, 0, 255);
    ColorMapModel model = new ColorMapModel(rgbs);

    // Replace one of the colors.
    RGB rgbTest = new RGB(255, 255, 0);
    model.setColor(1, rgbTest);

    // Test the getColor(index) method.
    assertEquals(rgbTest, model.getColor(1));

    // Test the getColors() method.
    assertEquals(rgbs[0], model.getColors()[0]);
    assertEquals(rgbTest, model.getColors()[1]);
    assertEquals(rgbs[2], model.getColors()[2]);
  }
}
