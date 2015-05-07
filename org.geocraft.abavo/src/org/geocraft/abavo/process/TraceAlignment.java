/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.process;


import org.geocraft.core.model.datatypes.Trace;


public class TraceAlignment {

  /** The correlation window (in msec) */
  private final int _corrWindow;

  /** The maximum shift (in msec) */
  private final int _maxShift;

  /** The correlation threshold. */
  private final float _corrThreshold;

  /** The smoothing filter length (in msec) */
  private final int _filterLength;

  /** The amplitude threshold (in %). */
  private final float _ampThreshold;

  /** The amplitude threshold window (in msec). */
  private final int _ampThresholdWindow;

  public TraceAlignment(final int corrWindow, final int maxShift, final float corrThreshold, final int filterLength, final float ampThreshold, final int ampThresholdWindow) {
    _corrWindow = corrWindow;
    _maxShift = maxShift;
    _corrThreshold = corrThreshold;
    _filterLength = filterLength;
    _ampThreshold = ampThreshold;
    _ampThresholdWindow = ampThresholdWindow;
  }

  public Trace[] process(final int numSamples, final Trace traceN, final int fndxN, final int lndxN,
      final Trace traceF, final int fndxF, final int lndxF) {
    float deltaZN = traceN.getZDelta();
    float deltaZF = traceF.getZDelta();
    if (deltaZN != deltaZF) {
      throw new IllegalArgumentException("Incompatible sample rates.");
    }
    if (!traceN.getUnitOfZ().equals(traceF.getUnitOfZ())) {
      throw new IllegalArgumentException("Incompatible sample rate units.");
    }
    float sampleRate = traceN.getZDelta();
    int numSamplesN = traceN.getNumSamples();
    int numSamplesF = traceF.getNumSamples();
    float invSampleRate = 1 / sampleRate;
    float amp = _ampThreshold * 0.01f;
    int lag = Math.round(_maxShift * invSampleRate + 1);
    int maxs = (int) (_maxShift * invSampleRate);
    int wlen = (int) (_corrWindow * invSampleRate * 0.5f);
    int alen = (int) (_ampThresholdWindow * invSampleRate * 0.5f);
    int flen = (int) (_filterLength * invSampleRate * 0.5f);
    float rbt = _corrThreshold;
    float rbwt = 0;
    int abcnt = numSamples;
    int ata = fndxN;
    int atb = fndxF;
    float[] trcApp = new float[traceN.getNumSamples()];
    float[] trcBpp = new float[traceF.getNumSamples()];
    float[] data = traceF.getData();

    if (numSamples < wlen * 2 && fndxN - wlen > 0 && lndxN + wlen < numSamplesN && fndxF - wlen > 0
        && lndxF + wlen < numSamplesF) {
      ata = ata - wlen;
      atb = atb - wlen;
      abcnt = abcnt + wlen + wlen;
    }
    System.arraycopy(traceN.getData(), ata, trcApp, 0, abcnt);
    System.arraycopy(traceF.getData(), atb, trcBpp, 0, abcnt);

    double[] trcAlign = alignTrace(trcApp, trcBpp, abcnt, lag, maxs, wlen, amp, alen, rbt, rbwt, flen);

    float[] trcShifted = shiftTrace(trcBpp, trcAlign, abcnt);
    for (int k = 0; k < numSamples; k++) {
      data[fndxF + k] = trcShifted[fndxN - atb + k];
    }
    Trace[] tracesAligned = new Trace[2];
    tracesAligned[0] = traceN;
    tracesAligned[1] = new Trace(traceF.getZStart(), traceF.getZDelta(), traceF.getUnitOfZ(), traceF.getX(),
        traceF.getY(), data, traceF.getStatus());
    return tracesAligned;
  }

  /**
   * Computes the trace alignment between near and far.
   * @param near the data array for the near trace.
   * @param far the data array for the far trace.
   * @param nsamp the number of samples in the near,far traces.
   * @param lag the lag to check during alignment (samples).
   * @param maxs the maximum shift to allow during alignment (samples).
   * @param wlen the correlation window half-length (samples).
   * @param amp the amplitude threshold (0-1).
   * @param alen the amplitude threshold window half-length (samples).
   * @param rbt the RB correlation threshold (0-1).
   * @param rbwt the RB correlation weight (always zero for pure correlation).
   * @param flen the smoothing filter half-length (samples).
   * @return the aligned far trace.
   */
  protected double[] alignTrace(final float[] near, final float[] far, final int nsamp, final int lag, final int maxs,
      final int wlen, final float amp, final int alen, final float rbt, final float rbwt, final int flen) {

    int i;
    int j;
    double minrms = 0;
    int nx = nsamp + wlen + wlen + lag + lag;
    double[] xm = new double[nx];
    double[] ym = new double[nx];
    double[] xxm = new double[nx];
    double[] yym = new double[nx];
    double[] xxsm = new double[nx];
    double[] yysm = new double[nx];
    double[] shiftm = new double[nsamp + flen + flen];
    double[] fshift = new double[nsamp];
    double[] rbm = new double[nsamp + flen + flen];
    double[] ccm = new double[lag + lag + 1];
    double[] xrms = new double[nsamp];
    double[] norm = new double[nsamp];
    int wlen2 = wlen + wlen + 1;
    int nrms = alen + alen + 1;

    for (i = 0, j = nx - 1; i < wlen + lag; i++, j--) {

      xm[i] = 0;
      xm[j] = 0;
      ym[i] = 0;
      ym[j] = 0;
      xxm[i] = 0;
      xxm[j] = 0;
      yym[i] = 0;
      yym[j] = 0;
    }

    // Set indices to 1st valid samples.
    int x = wlen + lag;
    int y = wlen + lag;
    int xx = wlen + lag;
    int yy = wlen + lag;
    int xxs = wlen + lag;
    int yys = wlen + lag;
    int cc = lag;
    int rb = flen;
    int shift = flen;

    for (i = 0; i < nsamp; i++) {
      xm[x + i] = near[i];
      ym[y + i] = far[i];
      xxm[xx + i] = near[i] * near[i];
      yym[yy + i] = far[i] * far[i];
    }
    for (i = 0; i < flen; i++) {
      shiftm[shift - flen + i] = 0;
      shiftm[shift + nsamp + i] = 0;
      rbm[rb - flen + i] = 0;
      rbm[rb + nsamp + i] = 0;
    }

    // Compute rms of near.
    minrms = computeNearRMS(nsamp, amp, alen, xxm, xrms, nrms, xx);

    // Compute energies.
    computeEnergies(nsamp, lag, maxs, wlen, rbt, minrms, xm, ym, xxm, yym, xxsm, yysm, shiftm, rbm, ccm, xrms, wlen2,
        x, y, xx, yy, xxs, yys, cc, rb, shift);

    // Smooth shift values using RB as a weight.
    smoothShiftValues(nsamp, flen, shiftm, fshift, rbm, norm, rb, shift);

    xm = null;
    ym = null;
    xxm = null;
    yym = null;
    xxsm = null;
    yysm = null;
    shiftm = null;
    rbm = null;
    ccm = null;
    xrms = null;
    norm = null;

    return fshift;
  }

  private double computeNearRMS(final int nsamp, final float amp, final int alen, final double[] xxm,
      final double[] xrms, final int nrms, final int xx) {
    int i;
    int j;
    int k;
    double maxrms = 0;
    xrms[0] = 0;
    for (i = 0; i <= alen; i++) {
      xrms[0] += xxm[xx + i];
    }
    for (i = 1, j = -alen, k = alen + 1; i < nsamp; i++, j++, k++) {
      xrms[i] = xrms[i - 1] - xxm[xx + j] + xxm[xx + k];
    }
    for (i = 0; i < nsamp; i++) {
      xrms[i] = Math.sqrt(xrms[i] / nrms);
      if (xrms[i] > maxrms) {
        maxrms = xrms[i];
      }
    }
    return amp * maxrms;
  }

  private void computeEnergies(final int nsamp, final int lag, final int maxs, final int wlen, final float rbt,
      final double minrms, final double[] xm, final double[] ym, final double[] xxm, final double[] yym,
      final double[] xxsm, final double[] yysm, final double[] shiftm, final double[] rbm, final double[] ccm,
      final double[] xrms, final int wlen2, final int x, final int y, final int xx, final int yy, final int xxs,
      final int yys, final int cc, final int rb, final int shift) {
    int i;
    int j;
    int k;
    int ilag;
    xxsm[xxs - lag] = 0;
    yysm[yys - lag] = 0;
    for (j = -(lag + wlen); j <= wlen - lag; j++) {
      xxsm[xxs - lag] += xxm[xx + j];
      yysm[yys - lag] += yym[yy + j];
    }
    for (i = 1 - lag, j = -(lag + wlen), k = -lag + wlen + 1; i < nsamp + lag; i++, j++, k++) {
      xxsm[xxs + i] = xxsm[xxs + i - 1] - xxm[xx + j] + xxm[xx + k];
      yysm[yys + i] = yysm[yys + i - 1] - yym[yy + j] + yym[yy + k];
    }
    for (i = 0; i < nsamp; i++) {

      if (xrms[i] > minrms) {

        double maxcorr = rbt;

        ilag = 0;
        shiftm[shift + i] = 0;
        for (j = i - lag; j <= i + lag; j++) {

          double corr = 0;

          for (k = 0; k < wlen2; k++) {
            corr += ym[y + i - wlen + k] * xm[x + j - wlen + k];
          }
          if (yysm[yys + i] > 0 && xxsm[xxs + j] > 0) {
            corr /= Math.sqrt(yysm[yys + i] * xxsm[xxs + j]);
            if (corr > 1) {
              corr = 1;
              // TODO: ServiceProvider.getLoggingService().getLogger(getClass()).warn("Bad correlation value!");
            }
          } else {
            corr = 0;
          }
          ccm[cc + i - j] = corr;
          if (corr > maxcorr) {
            maxcorr = corr;
            ilag = i - j;
          }
        }
        if (Math.abs(ilag) < lag && maxcorr > rbt) {
          shiftm[shift + i] = ilag - 0.5 * (ccm[cc + ilag + 1] - ccm[cc + ilag - 1])
              / (ccm[cc + ilag + 1] - 2 * ccm[cc + ilag] + ccm[cc + ilag - 1]);
          if (Math.abs(shiftm[shift + i]) >= maxs) {
            shiftm[shift + i] = 0;
          }
        } else {
          shiftm[shift + i] = 0;
        }
        rbm[rb + i] = maxcorr;
      } else {
        shiftm[shift + i] = 0;
        rbm[rb + i] = 0;
      }
    }
  }

  private void smoothShiftValues(final int nsamp, final int flen, final double[] shiftm, final double[] fshift,
      final double[] rbm, final double[] norm, final int rb, final int shift) {
    int i;
    int j;
    int k;
    if (flen > 0) {

      norm[0] = 0;
      fshift[0] = 0;
      for (i = 0; i <= flen; i++) {
        norm[0] += rbm[rb + i];
        fshift[0] += shiftm[shift + i] * rbm[rb + i];
      }
      for (i = 1, j = -flen, k = flen + 1; i < nsamp; i++, j++, k++) {
        fshift[i] = fshift[i - 1] - shiftm[shift + j] * rbm[rb + j] + shiftm[shift + k] * rbm[rb + k];
        norm[i] = norm[i - 1] - rbm[rb + j] + rbm[rb + k];
      }
      for (i = 0; i < nsamp; i++) {
        fshift[i] /= norm[i];
      }
    } else {
      for (i = 0; i < nsamp; i++) {
        fshift[i] = shiftm[shift + i];
      }
    }
  }

  /**
   * Shifts a trace based on the shift.
   * @param in the data array for the trace to shift.
   * @param shift the data array of shift values.
   * @param nsamp the number of samples in the trace arrays.
   * @return the shifted trace.
   */
  public float[] shiftTrace(final float[] in, final double[] shift, final int nsamp) {

    int i;
    int j;
    float[] out = new float[nsamp];

    for (i = 0; i < nsamp; i++) {

      int ishift = (int) shift[i];
      double fshift = shift[i] - ishift;

      j = i + ishift;
      if (fshift > 0.5) {
        fshift -= 1;
        ishift++;
      }
      if (fshift < -0.5) {
        fshift += 1;
        ishift--;
      }
      if (j >= 0 && j < nsamp) {
        out[i] = in[j];
      } else {
        out[i] = in[i];
      }
      if (Math.abs(fshift) > 0.05 && j >= 1 && j < nsamp - 1) {
        out[i] = (float) (0.5 * ((in[j + 1] - 2 * in[j] + in[j - 1]) * fshift * fshift + (in[j + 1] - in[j - 1])
            * fshift + 2 * in[j]));
      }
    }

    return out;
  }

}
