/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import junit.framework.TestCase;


/**
 * Unit tests for the EllipseRegionsModelEvent class.
 */
public class EllipseRegionsModelEventTest extends TestCase {

  /**
   * The main unit test.
   */
  public void testMain() {
    EllipseRegionsModel model = new EllipseRegionsModel();
    EllipseRegionsModelEvent.Type type = EllipseRegionsModelEvent.Type.EllipsesUpdated;
    EllipseRegionsModelEvent event = new EllipseRegionsModelEvent(type, model);
    assertNotNull(event.getEllipseRegionsModel());
    assertEquals(model, event.getEllipseRegionsModel());
    assertEquals(EllipseRegionsModelEvent.Type.EllipsesUpdated, event.getType());
  }

}
