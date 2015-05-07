/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


import junit.framework.TestCase;


/**
 * Unit tests for the PolygonRegionsModelEvent class.
 */
public class PolygonRegionsModelEventTest extends TestCase {

  /**
   * The main unit test.
   */
  public void testMain() {
    PolygonRegionsModel model = new PolygonRegionsModel();
    PolygonRegionsModelEvent.Type type = PolygonRegionsModelEvent.Type.PolygonsUpdated;
    int[] indices = { 1, 3, 56 };
    PolygonRegionsModelEvent event = new PolygonRegionsModelEvent(type, model, indices);
    assertNotNull(event.getPolygonRegionsModel());
    assertEquals(model, event.getPolygonRegionsModel());
    int[] indicesOut = event.getPolygonIndices();
    assertNotNull(indicesOut);
    assertEquals(3, indicesOut.length);
    assertEquals(indices[0], indicesOut[0]);
    assertEquals(indices[1], indicesOut[1]);
    assertEquals(indices[2], indicesOut[2]);
  }

}
