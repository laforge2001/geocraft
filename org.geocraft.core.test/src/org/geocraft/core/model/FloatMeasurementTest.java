/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.FloatMeasurement;
import org.geocraft.core.model.datatypes.Unit;


public class FloatMeasurementTest extends TestCase {

  public void testEquals() {
    FloatMeasurement fm1 = new FloatMeasurement(100, Unit.ACRE);
    FloatMeasurement fm2 = new FloatMeasurement(100, Unit.ACRE);
    FloatMeasurement fm3 = new FloatMeasurement(100, Unit.KILOMETERS_PER_HOUR);

    assertEquals(fm1, fm2);
    assertNotSame(fm2, fm3);
  }

  public void testHash() {
    FloatMeasurement fm1 = new FloatMeasurement(100, Unit.ACRE);
    FloatMeasurement fm2 = new FloatMeasurement(100, Unit.ACRE);
    FloatMeasurement fm3 = new FloatMeasurement(100, Unit.KILOMETERS_PER_HOUR);
    FloatMeasurement fm4 = new FloatMeasurement(200, Unit.KILOMETERS_PER_HOUR);

    assertTrue("Should be same hash", fm1.hashCode() == fm2.hashCode());
    assertFalse("Should be different", fm2.hashCode() == fm3.hashCode());
    assertFalse("Should have different hashes", fm3.hashCode() == fm4.hashCode());
  }

}
