/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.process;


import junit.framework.TestCase;

import org.geocraft.abavo.process.NearFarToInterceptGradient;


/**
 * Unit tests for the NearFarToInterceptGradient class.
 */
public class NearFarToInterceptGradientTest extends TestCase {

  /**
   * Test the simple case of 0 and 90 degrees, no scaling
   */
  public void testCase1() {
    NearFarToInterceptGradient process = new NearFarToInterceptGradient(0, 90);
    float[] dataA = { 25 }; // near
    float[] dataB = { 100 }; // far
    int numSamples = dataB.length;
    process.process(numSamples, dataA, 0, dataB, 0);
    assertEquals(25f, dataA[0]);
    assertEquals(75f, dataB[0]);
  }

  /**
   * Test the simple case of 30 and 60 degrees, with no scaling
   */
  public void testCase2() {
    NearFarToInterceptGradient process = new NearFarToInterceptGradient(30, 60);
    float[] dataA = { 25 }; // near
    float[] dataB = { 100 }; // far
    int numSamples = dataB.length;
    process.process(numSamples, dataA, 0, dataB, 0);
    assertEquals(-12.5f, dataA[0]);
    assertEquals(150f, dataB[0]);
  }

  /**
   * Test the simple case of 30 and 60 degrees, with scaling
   */
  public void testCase3() {
    NearFarToInterceptGradient process = new NearFarToInterceptGradient(30, 60, 2f, 1.25f);
    float[] dataA = { 25 }; // near
    float[] dataB = { 100 }; // far
    int numSamples = dataB.length;
    process.process(numSamples, dataA, 0, dataB, 0);
    assertEquals(12.5f, dataA[0]);
    assertEquals(150f, dataB[0]);
  }

}
