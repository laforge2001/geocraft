/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


import junit.framework.TestCase;

import org.eclipse.swt.graphics.RGB;


/**
 * Unit tests for the PolygonRegionsModel class.
 */
public class PolygonRegionsModelTest extends TestCase {

  public void testABCrossplotPolygonModel() {
    PolygonRegionsModel model = new PolygonRegionsModel();

    // Test the blocking/unblocking of updates.
    assertFalse(model.isUpdateBlocked());
    model.blockUpdate();
    assertTrue(model.isUpdateBlocked());
    model.unblockUpdate();
    assertFalse(model.isUpdateBlocked());

    // Test the symmetry lock.
    //    assertFalse(model.getSymmetricRegions());
    //    model.setSymmetricRegions(true);
    //    assertTrue(model.getSymmetricRegions());
    //    model.setSymmetricRegions(false);
    //    assertFalse(model.getSymmetricRegions());

    // Test the polygon fill.
    assertFalse(model.getPolygonsFilled());
    model.setPolygonsFilled(true);
    assertTrue(model.getPolygonsFilled());
    model.setPolygonsFilled(false);
    assertFalse(model.getPolygonsFilled());

    // Test the polygon models.
    PolygonModel[] polygonModels = model.getPolygonModels();
    assertEquals(64, polygonModels.length);
    RGB[] colors = model.getPolygonColors();
    assertEquals(64, colors.length);
    for (int i = 0; i < 64; i++) {
      // Test the polygon visibility.
      assertTrue(model.isPolygonVisible(i));
      model.setPolygonVisible(i, false);
      assertFalse(model.isPolygonVisible(i));
      model.setPolygonVisible(i, true);
      assertTrue(model.isPolygonVisible(i));

      // Test the polygon colors.
      assertEquals(colors[i], model.getPolygonColor(i));
      model.setPolygonColor(i, new RGB(i, i + 64, i + 128));
      RGB color = model.getPolygonColor(i);
      assertEquals(i, color.red);
      assertEquals(i + 64, color.green);
      assertEquals(i + 128, color.blue);

      // Test the polygon normalization.
      assertEquals(128.0f, model.getNormalizationFactor());

      // Test the polygon values.
      float value = -126 + i * 4;
      assertEquals(value, model.getPolygonValue(i));
      value = 126 - i * 4;
      model.setPolygonValue(i, value);
      assertEquals(value, model.getPolygonValue(i));
    }
  }
}
