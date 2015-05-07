/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;


/**
 * Unit tests for the <code>SeismicLine2d</code> class.
 */
public class SeismicLine2dTest extends TestCase {

  /**
   * Test the 2D seismic line geometry.
   */
  public void testSeismicLine2d() {
    // Create a seismic line geometry for testing.
    int lineNumber = 5;
    float cdpStart = 1;
    float cdpEnd = 5;
    float cdpDelta = 2;
    FloatRange cdpRange = new FloatRange(cdpStart, cdpEnd, cdpDelta);
    float shotpointStart = 10;
    float shotpointEnd = 11;
    float shotpointDelta = 0.5f;
    FloatRange shotpointRange = new FloatRange(shotpointStart, shotpointEnd, shotpointDelta);
    Point3d[] points = new Point3d[3];
    points[0] = new Point3d(1, 2, 0);
    points[1] = new Point3d(7.5, 4.5, 0);
    points[2] = new Point3d(8, 9, 0);
    CoordinateSeries xyCoords = CoordinateSeries.create(points, null);
    SeismicLine2d seismicLine = new SeismicLine2d("foo", lineNumber, cdpRange, shotpointRange.getStart(), shotpointRange
        .getEnd(), xyCoords, new SimpleSeismicLineCoordinateTransform(cdpRange, shotpointRange));

    // Test the line number.
    assertEquals(lineNumber, seismicLine.getNumber());

    // Test the CDP range.
    assertEquals(cdpStart, seismicLine.getCDPStart());
    assertEquals(cdpEnd, seismicLine.getCDPEnd());
    assertEquals(cdpDelta, seismicLine.getCDPDelta());

    // Test the Shotpoint range.
    assertEquals(shotpointStart, seismicLine.getShotpointStart());
    assertEquals(shotpointEnd, seismicLine.getShotpointEnd());

    // Test the transform of cell to CDP.
    assertEquals(cdpStart, seismicLine.transformBinToCdp(0));
    assertEquals(3.0f, seismicLine.transformBinToCdp(1.23));
    assertEquals(cdpEnd, seismicLine.transformBinToCdp(1.5));

    // Test that asking for a cell beyond the start throws exception.
    try {
      seismicLine.transformBinToCdp(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a cell beyond the end throws exception.
    try {
      seismicLine.transformBinToCdp(4);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test the transform of CDP to cell.
    assertEquals(0.0, seismicLine.transformCdpToBin(cdpStart));
    assertEquals(1.0, seismicLine.transformCdpToBin(3));
    assertEquals(2.0, seismicLine.transformCdpToBin(cdpEnd));

    // Test that asking for a CDP beyond the start throws exception.
    try {
      seismicLine.transformCdpToBin(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a CDP beyond the end throws exception.
    try {
      seismicLine.transformCdpToBin(6);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test the transform of cell to shotpoint.
    assertEquals(shotpointStart, seismicLine.transformBinToShotpoint(0));
    assertEquals(10.5f, seismicLine.transformBinToShotpoint(1.23));
    assertEquals(shotpointEnd, seismicLine.transformBinToShotpoint(1.5));

    // Test that asking for a cell beyond the start throws exception.
    try {
      seismicLine.transformBinToShotpoint(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a cell beyond the end throws exception.
    try {
      seismicLine.transformBinToShotpoint(4);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test the transform of shotpoint to cell.
    assertEquals(0.0, seismicLine.transformShotpointToBin(shotpointStart));
    assertEquals(1.0, seismicLine.transformShotpointToBin(10.5f));
    assertEquals(2.0, seismicLine.transformShotpointToBin(shotpointEnd));

    // Test that asking for a shotpoint beyond the start throws exception.
    try {
      seismicLine.transformShotpointToBin(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a shotpoint beyond the end throws exception.
    try {
      seismicLine.transformShotpointToBin(6);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test the transform of CDP to shotpoint.
    assertEquals(10.0f, seismicLine.transformCDPToShotpoint(cdpStart));
    assertEquals(10.5f, seismicLine.transformCDPToShotpoint(3));
    assertEquals(11.0f, seismicLine.transformCDPToShotpoint(cdpEnd));

    // Test that asking for a CDP beyond the start throws exception.
    try {
      seismicLine.transformCDPToShotpoint(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a CDP beyond the end throws exception.
    try {
      seismicLine.transformCDPToShotpoint(6);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test the transform of shotpoint to CDP                  .
    assertEquals(1.0f, seismicLine.transformShotpointToCDP(shotpointStart));
    assertEquals(3.0f, seismicLine.transformShotpointToCDP(10.5f));
    assertEquals(5.0f, seismicLine.transformShotpointToCDP(shotpointEnd));

    // Test that asking for a shotpoint beyond the start throws exception.
    try {
      seismicLine.transformShotpointToCDP(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a shotpoint beyond the end throws exception.
    try {
      seismicLine.transformShotpointToCDP(6);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test the transform of CDP to x,y.
    assertEquals(1.0, seismicLine.transformCDPToXY(cdpStart)[0]);
    assertEquals(2.0, seismicLine.transformCDPToXY(cdpStart)[1]);
    assertEquals(7.5, seismicLine.transformCDPToXY(3)[0]);
    assertEquals(4.5, seismicLine.transformCDPToXY(3)[1]);
    assertEquals(8.0, seismicLine.transformCDPToXY(cdpEnd)[0]);
    assertEquals(9.0, seismicLine.transformCDPToXY(cdpEnd)[1]);

    // Test that asking for a CDP beyond the start throws exception.
    try {
      seismicLine.transformCDPToXY(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a CDP beyond the end throws exception.
    try {
      seismicLine.transformCDPToXY(6);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test the transform of shotpoint to x,y.
    assertEquals(1.0, seismicLine.transformShotpointToXY(shotpointStart)[0]);
    assertEquals(2.0, seismicLine.transformShotpointToXY(shotpointStart)[1]);
    assertEquals(7.5, seismicLine.transformShotpointToXY(10.5f)[0]);
    assertEquals(4.5, seismicLine.transformShotpointToXY(10.5f)[1]);
    assertEquals(8.0, seismicLine.transformShotpointToXY(shotpointEnd)[0]);
    assertEquals(9.0, seismicLine.transformShotpointToXY(shotpointEnd)[1]);

    // Test that asking for a shotpoint beyond the start throws exception.
    try {
      seismicLine.transformShotpointToXY(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a shotpoint beyond the end throws exception.
    try {
      seismicLine.transformShotpointToXY(6);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Create another seismic line geometry.
    SeismicLine2d seismicLine2 = new SeismicLine2d("bar", lineNumber, cdpRange, shotpointRange.getStart(), shotpointRange
        .getEnd(), xyCoords, new SimpleSeismicLineCoordinateTransform(cdpRange, shotpointRange));

    // Test the geometry equality.
    assertFalse(seismicLine.equals(seismicLine2));
    assertTrue(seismicLine.matchesGeometry(seismicLine2));

    // Create another seismic line geometry with a different line number.
    SeismicLine2d geometry3 = new SeismicLine2d("bar", lineNumber + 1, cdpRange, shotpointRange.getStart(),
        shotpointRange.getEnd(), xyCoords, new SimpleSeismicLineCoordinateTransform(cdpRange, shotpointRange));

    // Test the geometry equality.
    assertFalse(seismicLine.equals(geometry3));

    // Create another seismic line geometry with a different CDP range.
    SeismicLine2d geometry4 = new SeismicLine2d("bar", lineNumber, new FloatRange(2, 4, 1), shotpointRange.getStart(),
        shotpointRange.getEnd(), xyCoords, new SimpleSeismicLineCoordinateTransform(cdpRange, shotpointRange));

    // Test the geometry equality.
    assertFalse(seismicLine.equals(geometry4));

    // Create another seismic line geometry with a different shotpoint range.
    SeismicLine2d geometry5 = new SeismicLine2d("bar", lineNumber, cdpRange, 10f, 10.5f, xyCoords,
        new SimpleSeismicLineCoordinateTransform(cdpRange, shotpointRange));

    // Test the geometry equality.
    assertFalse(seismicLine.equals(geometry5));
  }

}
