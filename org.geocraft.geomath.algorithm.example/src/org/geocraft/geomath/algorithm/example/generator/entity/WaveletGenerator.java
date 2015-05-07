/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.core.repository.IRepository;
import org.geocraft.math.wavelet.RickerWavelet;


/**
 * This class generates synthetic, in-memory wells, bores, logs and picks.
 */
public final class WaveletGenerator {

  /** The repository in which to add wavelets. */
  private IRepository _repository;

  /**
   * Constructs a synthetic wavelet generator.
   * 
   * @param repository the repository in which to add generated wavelets.
   */
  public WaveletGenerator(final IRepository repository) {
    _repository = repository;
  }

  /**
   * Creates an in-memory Ricker wavelet and adds it to the repository.
   * 
   * @param numSamples the number of samples in the wavelet.
   * @param sampleRate the sample interval of the wavelet.
   * @param frequency the frequency of the wavelet.
   */
  public void addRickerWavelet(int numSamples, float sampleRate, float frequency) {
    Wavelet wavelet = RickerWavelet.createWavelet(frequency, sampleRate, numSamples);
    wavelet.setDirty(false);
    _repository.add(wavelet);
  }
}
