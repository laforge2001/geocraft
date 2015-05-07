/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.math.wavelet;


import org.geocraft.core.factory.model.WaveletFactory;
import org.geocraft.core.model.seismic.Wavelet;


/**
 * A class to generate a Ricker Wavelet.
 */
public class RickerWavelet {

  /**
   * Generates a Ricker wavelet.
   * 
   * @param freq the frequency.
   * @param sampleRate the sample rate.
   * @param nSamples the number of samples.
   * @return the Ricker wavelet.
   */
  public static Wavelet createWavelet(final float freq, final float sampleRate, final int numSamples) {
    float[] samples = new float[numSamples];
    for (int i = 0; i < numSamples; i++) {
      double u = Math.PI * freq * sampleRate * (i - numSamples / 2) / 1000;
      samples[i] = (float) ((1 - 2 * u * u) / Math.exp(u * u));
    }
    float startTime = -sampleRate * (numSamples / 2);
    return WaveletFactory.createInMemory("Ricker" + freq, startTime, sampleRate, samples, "float", "zero");
  }
}
