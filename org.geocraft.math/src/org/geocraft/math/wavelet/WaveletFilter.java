/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.math.wavelet;


import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.internal.math.ComplexFloat;


/**
 * Defines the WaveletFilter class.
 */
public class WaveletFilter {

  private final int _numSamples;

  private final float _sampleRate;

  private final int _timeZeroSample;

  private final float[] _waveletValues;

  private int _nfft;

  private final Wavelet _wavelet;

  private ComplexFloat[] _ggw;

  private ComplexFloat[] _ggs;

  private ComplexFloat[] _ggr;

  private ComplexFloat[] _ggp;

  private float[] _tempf;

  private boolean _allocated;

  /** cos and sin array. */
  private float[] _trigTbl;

  private int _cosIndex; // index into _trigTbl where cos starts.

  private int _sinIndex; // index into _trigTbl where sin starts.

  private float[] _wg; // Goertzel.

  private int _w1; // Goertzel delayed 1 value.

  private int _w2; // Goertzel delayed 2 values.

  private int _wf; // temp. sin and cos table for Goertzel.

  private int _numPrimFactors; // number of prime factors.

  private int _nSetup; // value of N from last call.

  private final int[] _pf = new int[20]; // i-th prime factor of N

  // (ascending order).

  private final int[] _pga = new int[20]; // N/product 0..i of _pf[i].

  private final int[] _pbit = new int[20]; // reverse mapping bits.

  /**
   * Constructs a wavelet filter process.
   * 
   * @param wavelet the wavelet used to build the filter.
   */
  public WaveletFilter(final Wavelet wavelet) {
    this("Wavelet Filter", wavelet);
  }

  /**
   * Constructs a wavelet filter process.
   * 
   * @param name the wavelet filter process name.
   * @param wavelet the wavelet used to build the filter.
   */
  public WaveletFilter(final String name, final Wavelet wavelet) {

    if (wavelet == null) {
      throw new IllegalArgumentException("Cannot create a new wavelet filter with a null wavelet");
    }

    _wavelet = wavelet;
    _numSamples = wavelet.getNumSamples();
    float startTime = 0;

    Unit zUnit = UnitPreferences.getInstance().getTimeUnit();

    float timeInterval = wavelet.getTimeInterval();
    _sampleRate = Unit.convert(timeInterval, zUnit, Unit.MILLISECONDS);

    float timeStart = wavelet.getTimeStart();
    startTime = Unit.convert(timeStart, zUnit, Unit.MILLISECONDS);

    // float time_end = wavelet.getTimeEnd();
    _timeZeroSample = (int) (-startTime / _sampleRate);
    _waveletValues = wavelet.getValues();
    _ggw = null;
    _ggs = null;
    _ggr = null;
    _ggp = null;
    _allocated = false;
  }

  /**
   * Checks if the specified trace sample rate matches the wavelet sample rate.
   * 
   * @param sampleRate the sample rate of the trace data.
   */
  public void checkSampleRate(final float sampleRate) throws Exception {

    if (sampleRate != _sampleRate) {
      StringBuffer buffer = new StringBuffer("Exception:\n");

      buffer.append("The wavelet sample rate (" + _sampleRate + " msec) ");
      buffer.append("does not match\n");
      buffer.append("the seismic sample rate (" + sampleRate + " msec).");
      throw new Exception(buffer.toString());
    }
  }

  /**
   * Filters the trace.
   * 
   * @param trc the input trace array.
   * @param scnt the number of samples.
   * @param sstt the index of the 1st sample in the trace array.
   * @param trcNumSamp the full length of the trace array.
   * @param sr the input trace sample rate
   * @return zero if successful; otherwise non-zero.
   */
  public float[] filterTrace(final float[] trc, final int scnt, final int sstt, final int trcNumSamp, final float sr) throws Exception {
    if (_wavelet == null) {
      return trc;
    }
    checkSampleRate(sr);
    return filterTrace(trc, scnt, sstt, trcNumSamp);
  }

  /**
   * Filters the trace.
   * 
   * @param trc the input trace array.
   * @param scnt the number of samples.
   * @param sstt the index of the 1st sample in the trace array.
   * @param trcNumSamp the full length of the trace array.
   * @return zero if successful; otherwise non-zero.
   */
  public double[] filterTrace(final double[] trc, final int scnt, final int sstt, final int trcNumSamp) {
    if (_wavelet == null) {
      return trc;
    }
    float[] temp = new float[trc.length];
    for (int i = 0; i < temp.length; i++) {
      temp[i] = (float) trc[i];
    }
    temp = filterTrace(temp, scnt, sstt, trcNumSamp);
    double[] result = new double[temp.length];
    for (int i = 0; i < temp.length; i++) {
      result[i] = temp[i];
    }
    return result;
  }

  /**
   * Filters the trace.
   * 
   * @param trc the input trace array.
   * @param scnt the number of samples.
   * @param sstt the index of the 1st sample in the trace array.
   * @param trcNumSamp the full length of the trace array.
   * @return zero if successful; otherwise non-zero.
   */
  public float[] filterTrace(final float[] trc, final int scnt, final int sstt, final int trcNumSamp) {
    if (_wavelet == null) {
      return trc;
    }
    float rms;
    int i;
    int nfft;
    int prePad;
    int postPad;
    int maxPad;
    float[] work = new float[trc.length];
    System.arraycopy(trc, 0, work, 0, trc.length);

    nfft = sizeFFT(scnt + _numSamples + _numSamples - 2);
    if (!_allocated || nfft != _nfft) {

      float[] tempf = new float[nfft];
      int minNfft = Math.min(nfft, _nfft);

      if (_tempf != null) {
        for (i = 0; i < minNfft; i++) {
          tempf[i] = _tempf[i];
        }
      }
      _tempf = tempf;

      ComplexFloat[] ggw = new ComplexFloat[nfft];

      for (i = 0; i < nfft; i++) {
        ggw[i] = new ComplexFloat(0, 0);
      }
      if (_ggw != null) {
        for (i = 0; i < minNfft; i++) {
          ggw[i] = _ggw[i];
        }
      }
      _ggw = ggw;

      ComplexFloat[] ggs = new ComplexFloat[nfft];

      for (i = 0; i < nfft; i++) {
        ggs[i] = new ComplexFloat(0, 0);
      }
      if (_ggs != null) {
        for (i = 0; i < minNfft; i++) {
          ggs[i] = _ggs[i];
        }
      }
      _ggs = ggs;

      ComplexFloat[] ggr = new ComplexFloat[nfft];

      for (i = 0; i < nfft; i++) {
        ggr[i] = new ComplexFloat(0, 0);
      }
      if (_ggr != null) {
        for (i = 0; i < minNfft; i++) {
          ggr[i] = _ggr[i];
        }
      }
      _ggr = ggr;

      ComplexFloat[] ggp = new ComplexFloat[nfft];

      for (i = 0; i < nfft; i++) {
        ggp[i] = new ComplexFloat(0, 0);
      }
      if (_ggp != null) {
        for (i = 0; i < minNfft; i++) {
          ggp[i] = _ggp[i];
        }
      }
      _ggp = ggp;
      _nfft = nfft;
      padSync(_waveletValues, 0, _tempf, _numSamples, _timeZeroSample, nfft);
      fft10(_tempf, nfft, _ggw);
      _allocated = true;
    }
    maxPad = sstt;
    prePad = _numSamples - _timeZeroSample - 1;
    if (prePad > maxPad) {
      prePad = maxPad;
    }
    maxPad = trcNumSamp - scnt - sstt;
    postPad = _timeZeroSample;
    if (postPad > maxPad) {
      postPad = maxPad;
    }
    padSync(work, sstt - prePad, _tempf, scnt + prePad + postPad, prePad, nfft);
    rms = vrms(_tempf, nfft);
    fft10(_tempf, nfft, _ggs);
    for (i = 0; i < nfft; i++) {
      _ggp[i]._real = _ggw[i]._real * _ggs[i]._real - _ggw[i]._imag * _ggs[i]._imag;
      _ggp[i]._imag = _ggs[i]._real * _ggw[i]._imag + _ggs[i]._imag * _ggw[i]._real;
    }
    ifft10(_ggp, nfft, _ggr);
    for (i = 0; i < nfft; i++) {
      _tempf[i] = _ggr[i].getReal();
    }
    rms /= vrms(_tempf, nfft);
    for (i = 0; i < scnt; i++) {
      work[i + sstt] = _tempf[i] * rms;
    }
    return work;
  }

  private int fft10(final float[] a, final int na, final ComplexFloat[] gr) {

    // a: input time data of dim(na).
    // na: dimension of input time data.
    // gr: output data (real & imag) of dim(na).
    float ftrfact; // Factor 2 PI.
    float aW; // Real part of first point of butterfly.
    float bW; // Imag part of first point of butterfly.
    float cW; // Real part of second point of butterfly.
    float dW; // Imag part of second point of butterfly.
    float w0; // temporary real and imaginary accummulators.
    int radix;
    int radix2;
    int ipt;
    int i; // General index.
    int j; // General index.
    int jf; // indexing prime factor.
    int porg; // position of original data for reverse mapping.
    int istrt;
    int bfloc; // butterfly current location.
    int bfstep; // step size for bfloc.
    int di;
    int w; // representative angle omega.
    int wloc;
    int dw; // delta step for wloc.

    ftrfact = (float) (2 * Math.PI);
    if (na == _nSetup) {
      for (i = 1; i < _numPrimFactors; i++) {
        _pbit[i] = 0;
      }
    } else {

      if (_nSetup > 0) {
        _trigTbl = null;
        _wg = null;
      }

      // Find prime factors of na.
      _pf[0] = 1;
      _pga[0] = na;
      for (_numPrimFactors = 1, j = na, jf = 2; j > 1;) {

        if (j % jf == 0) {

          if (_numPrimFactors > 19) {
            return -3; /* Too many factors */
          }
          if (jf > 13) {
            return -2; /* Factor too large */
          }
          j /= jf;
          _pbit[_numPrimFactors] = 0;
          _pga[_numPrimFactors] = j;
          _pf[_numPrimFactors++] = jf;
        } else {
          jf++;
        }
      }
      _wg = new float[6 * jf];
      _w1 = 0;
      _w2 = 2 * jf;
      _wf = 4 * jf;

      // Generate cosine table.
      if (na % 4 == 0) {
        _trigTbl = new float[5 * na / 4 + 1];
      } else {
        _trigTbl = new float[2 * na + 1];
      }
      if (_trigTbl == null) {
        return -1; // na too large.
      }

      _nSetup = na;
      if (na % 2 == 0) {
        for (j = 0; j <= na / 4; j++) {
          _trigTbl[j] = (float) Math.sin(ftrfact * j / na);
          _trigTbl[na / 2 - j] = _trigTbl[j];
          _trigTbl[na / 2 + j] = -_trigTbl[j];
          _trigTbl[na - j] = -_trigTbl[j];
          _trigTbl[na + j] = _trigTbl[j];
        }
      } else {
        for (j = 0; j <= na / 2; j++) {
          _trigTbl[j] = (float) Math.sin(ftrfact * j / na);
          _trigTbl[na - j] = -_trigTbl[j];
        }
      }
      _sinIndex = 0;
      if (na % 4 == 0) {
        _cosIndex = na / 4;
      } else {

        if (na % 2 == 0) {
          for (j = 0; j <= na / 4; j++) {
            _trigTbl[na + j] = (float) Math.cos(ftrfact * j / na);
            _trigTbl[na + na / 2 - j] = -_trigTbl[na + j];
            _trigTbl[na + na / 2 + j] = -_trigTbl[na + j];
            _trigTbl[na + na - j] = _trigTbl[na + j];
          }
        } else {
          for (j = 0; j <= na / 2; j++) {
            _trigTbl[na + j] = (float) Math.cos(ftrfact * j / na);
            _trigTbl[na + na - j] = _trigTbl[na + j];
          }
        }
        _cosIndex = na;
      }
    }

    // Reorder data.
    gr[0]._real = a[0];
    gr[0]._imag = 0;
    for (i = 1, porg = 0; i < na; i++) {

      // NOTE: moved porg to inside loop for(j=1; porg+=_pga[j],
      // ++_pbit[j]>=_pf[j]; j++) {
      porg += _pga[1];
      for (j = 1; ++_pbit[j] >= _pf[j]; j++) {
        porg -= _pga[j - 1];
        _pbit[j] = 0;
        porg += _pga[j + 1];
      }
      gr[i]._real = a[porg];
      gr[i]._imag = 0;
    }

    doButterflies(na, gr);
    return 0;
  }

  private void doButterflies(final int na, final ComplexFloat[] gr) {
    float aW;
    float bW;
    float cW;
    float dW;
    float w0;
    int radix;
    int radix2;
    int ipt;
    int i;
    int j;
    int istrt;
    int bfloc;
    int bfstep;
    int di;
    int w;
    int wloc;
    int dw;
    // Do Butterflies.
    for (i = 1; i < _numPrimFactors; i++) {

      // General radix butterfly.
      radix = _pf[i];
      radix2 = 2 * radix;
      dw = _pga[i];
      if (radix == 2) { // special on radix 2.

        if (i == 1) { // Special on first butterfly.
          for (ipt = 0; ipt < na; ipt += 2) {
            aW = gr[ipt]._real;
            gr[ipt]._real += gr[ipt + 1]._real;
            gr[ipt + 1]._real = aW - gr[ipt + 1]._real;
          }
        }
        if (i == 2) { // special on second butterfly.

          for (ipt = 0; ipt < na; ipt += 4) {
            aW = gr[ipt]._real;
            gr[ipt]._real += gr[ipt + 2]._real;
            gr[ipt + 2]._real = aW - gr[ipt + 2]._real;
            gr[ipt + 1]._imag = gr[ipt + 3]._real;
            gr[ipt + 3]._imag = -gr[ipt + 3]._real;
            gr[ipt + 3]._real = gr[ipt + 1]._real;
          }
        }
        if (i > 2) { // other radix 2 algorithms.

          dw = na / radix;
          bfstep = na / _pga[i];
          di = na / _pga[i - 1];
          for (wloc = 0, istrt = 0; wloc < dw; wloc += _pga[i], istrt += 1) {

            for (bfloc = istrt; bfloc < na; bfloc += bfstep) {

              ipt = bfloc;
              aW = gr[ipt]._real;
              bW = gr[ipt]._imag;
              cW = gr[ipt + di]._real;
              dW = gr[ipt + di]._imag;
              _wg[_wf + 0] = _trigTbl[_cosIndex + wloc];
              _wg[_wf + 1] = _trigTbl[_sinIndex + wloc];
              _wg[_w1 + 0] = cW * _wg[_wf + 0] - dW * _wg[_wf + 1];
              _wg[_w1 + 1] = dW * _wg[_wf + 0] + cW * _wg[_wf + 1];
              gr[ipt]._real = aW + _wg[_w1 + 0];
              gr[ipt]._imag = bW + _wg[_w1 + 1];
              gr[ipt + di]._real = aW - _wg[_w1 + 0];
              gr[ipt + di]._imag = bW - _wg[_w1 + 1];
            }
          }
        }
      } else { // other sized radixes.

        dw = na / radix;
        bfstep = na / _pga[i];
        di = na / _pga[i - 1];
        for (wloc = 0, istrt = 0; wloc < dw; wloc += _pga[i], istrt += 1) {

          for (bfloc = istrt; bfloc < na; bfloc += bfstep) {

            ipt = bfloc + radix * di;
            ipt -= di;
            aW = gr[ipt]._real;
            bW = gr[ipt]._imag;
            ipt -= di;
            cW = gr[ipt]._real;
            dW = gr[ipt]._imag;

            for (j = 0, w = wloc; j < radix2; j += 2, w += dw) {
              _wg[_wf + j] = 2 * _trigTbl[_cosIndex + w];
              _wg[_wf + j + 1] = _trigTbl[_sinIndex + w];
              _wg[_w2 + j] = aW;
              _wg[_w2 + j + 1] = bW;
              _wg[_w1 + j] = cW + _wg[_wf + j] * _wg[_w2 + j];
              _wg[_w1 + j + 1] = dW + _wg[_wf + j] * _wg[_w2 + j + 1];
            }

            // This is some ugly code! I am guessing the dude wanted a while
            // loop
            // here but it is hard to tell.
            // for (; (ipt -= di) >= bfloc;) {
            while ((ipt -= di) >= bfloc) {
              aW = gr[ipt]._real;
              bW = gr[ipt]._imag;
              for (j = 0; j < radix2; j += 2) {
                w0 = aW + _wg[_w1 + j] * _wg[_wf + j] - _wg[_w2 + j];
                _wg[_w2 + j] = _wg[_w1 + j];
                _wg[_w1 + j] = w0;
                w0 = bW + _wg[_w1 + j + 1] * _wg[_wf + j] - _wg[_w2 + j + 1];
                _wg[_w2 + j + 1] = _wg[_w1 + j + 1];
                _wg[_w1 + j + 1] = w0;
              }
            }

            for (j = 0, ipt = bfloc; j < radix2; j += 2, ipt += di) {
              gr[ipt]._real = (float) (_wg[_w1 + j] - 0.5 * _wg[_wf + j] * _wg[_w2 + j] - _wg[_wf + j + 1]
                  * _wg[_w2 + j + 1]);
              gr[ipt]._imag = (float) (_wg[_w1 + j + 1] - 0.5 * _wg[_wf + j] * _wg[_w2 + j + 1] + _wg[_wf + j + 1]
                  * _wg[_w2 + j]);
            }
          }
        }
      }
    }
  }

  private int ifft10(final ComplexFloat[] a, final int na, final ComplexFloat[] gr) {

    // a: input frequency data of dim(na).
    // na: dimension of input frequency data.
    // gr: output data (real & imag) of dim(na).
    float ftrfact; // Factor 2 PI.
    float aW; // Real part of first point of butterfly.
    float bW; // Imag part of first point of butterfly.
    float cW; // Real part of second point of butterfly.
    float dW; // Imag part of second point of butterfly.
    float w0; // temporary real and imaginary accummulators.
    int radix;
    int radix2;
    int ipt;
    int i; // General index.
    int j; // General index.
    int jf; // indexing prime factor.
    int porg; // position of original data for reverse mapping.
    int istrt;
    int bfloc; // butterfly current location.
    int bfstep; // step size for bfloc.
    int di;
    int w; // representative angle omega.
    int wloc;
    int dw; // delta step for wloc.

    ftrfact = (float) (2 * Math.PI);
    if (na == _nSetup) {
      for (i = 1; i < _numPrimFactors; i++) {
        _pbit[i] = 0;
      }
    } else {

      if (_nSetup > 0) {
        _trigTbl = null;
        _wg = null;
      }

      // Find prime factors of na.
      _pf[0] = 1;
      _pga[0] = na;
      for (_numPrimFactors = 1, j = na, jf = 2; j > 1;) {

        if (j % jf == 0) {

          if (_numPrimFactors > 19) {
            return -3; // Too many factors.
          }
          if (jf > 13) {
            return -2; // Factor too large.
          }
          j /= jf;
          _pbit[_numPrimFactors] = 0;
          _pga[_numPrimFactors] = j;
          _pf[_numPrimFactors++] = jf;
        } else {
          jf++;
        }
      }
      _wg = new float[6 * jf];
      _w1 = 0;
      _w2 = 2 * jf;
      _wf = 4 * jf;

      // Generate cosine table.
      if (na % 4 == 0) {
        _trigTbl = new float[5 * na / 4 + 1];
      } else {
        _trigTbl = new float[2 * na + 1];
      }
      if (_trigTbl == null) {
        return -1; // na too large
      }

      _nSetup = na;
      if (na % 2 == 0) {
        for (j = 0; j <= na / 4; j++) {
          _trigTbl[j] = (float) Math.sin(ftrfact * j / na);
          _trigTbl[na / 2 - j] = _trigTbl[j];
          _trigTbl[na / 2 + j] = -_trigTbl[j];
          _trigTbl[na - j] = -_trigTbl[j];
          _trigTbl[na + j] = _trigTbl[j];
        }
      } else {
        for (j = 0; j <= na / 2; j++) {
          _trigTbl[j] = (float) Math.sin(ftrfact * j / na);
          _trigTbl[na - j] = -_trigTbl[j];
        }
      }
      _sinIndex = 0;
      if (na % 4 == 0) {
        _cosIndex = na / 4;
      } else {

        if (na % 2 == 0) {
          for (j = 0; j <= na / 4; j++) {
            _trigTbl[na + j] = (float) Math.cos(ftrfact * j / na);
            _trigTbl[na + na / 2 - j] = -_trigTbl[na + j];
            _trigTbl[na + na / 2 + j] = -_trigTbl[na + j];
            _trigTbl[na + na - j] = _trigTbl[na + j];
          }
        } else {
          for (j = 0; j <= na / 2; j++) {
            _trigTbl[na + j] = (float) Math.cos(ftrfact * j / na);
            _trigTbl[na + na - j] = _trigTbl[na + j];
          }
        }
        _cosIndex = na;
      }
    }

    // Reorder data.
    gr[0] = a[0];
    for (i = 1, porg = 0; i < na; i++) {

      // NOTE: moved porg to inside loop for(j=1; porg+=_pga[j],
      // ++_pbit[j]>=_pf[j]; j++) {
      porg += _pga[1];
      for (j = 1; ++_pbit[j] >= _pf[j]; j++) {
        porg -= _pga[j - 1];
        _pbit[j] = 0;
        porg += _pga[j + 1];
      }
      gr[i] = a[porg];
    }

    // Do Butterflies.
    for (i = 1; i < _numPrimFactors; i++) {

      // General radix butterfly.
      radix = _pf[i];
      radix2 = 2 * radix;
      dw = _pga[i];
      if (radix == 2) { // special on radix 2.

        dw = na / radix;
        bfstep = na / _pga[i];
        di = na / _pga[i - 1];
        for (wloc = 0, istrt = 0; wloc < dw; wloc += _pga[i], istrt += 1) {

          for (bfloc = istrt; bfloc < na; bfloc += bfstep) {

            ipt = bfloc;
            aW = gr[ipt]._real;
            bW = gr[ipt]._imag;
            cW = gr[ipt + di]._real;
            dW = gr[ipt + di]._imag;
            _wg[_wf + 0] = _trigTbl[_cosIndex + wloc];
            _wg[_wf + 1] = _trigTbl[_sinIndex + wloc];
            _wg[_w1 + 0] = cW * _wg[_wf + 0] + dW * _wg[_wf + 1];
            _wg[_w1 + 1] = dW * _wg[_wf + 0] - cW * _wg[_wf + 1];
            gr[ipt]._real = aW + _wg[_w1 + 0];
            gr[ipt]._imag = bW + _wg[_w1 + 1];
            gr[ipt + di]._real = aW - _wg[_w1 + 0];
            gr[ipt + di]._imag = bW - _wg[_w1 + 1];
          }
        }
      } else { // other sized radixes.

        dw = na / radix;
        bfstep = na / _pga[i];
        di = na / _pga[i - 1];
        for (wloc = 0, istrt = 0; wloc < dw; wloc += _pga[i], istrt += 1) {

          for (bfloc = istrt; bfloc < na; bfloc += bfstep) {

            ipt = bfloc + radix * di;
            ipt -= di;
            aW = gr[ipt]._real;
            bW = gr[ipt]._imag;
            ipt -= di;
            cW = gr[ipt]._real;
            dW = gr[ipt]._imag;
            for (j = 0, w = wloc; j < radix2; j += 2, w += dw) {
              _wg[_wf + j] = 2 * _trigTbl[_cosIndex + w];
              _wg[_wf + j + 1] = _trigTbl[_sinIndex + w];
              _wg[_w2 + j] = aW;
              _wg[_w2 + j + 1] = bW;
              _wg[_w1 + j] = cW + _wg[_wf + j] * _wg[_w2 + j];
              _wg[_w1 + j + 1] = dW + _wg[_wf + j] * _wg[_w2 + j + 1];
            }

            // This is some ugly code! I am guessing the dude wanted a while
            // loop
            // here but it is hard to tell.
            // for (; (ipt -= di) >= bfloc;) {
            while ((ipt -= di) >= bfloc) {

              aW = gr[ipt]._real;
              bW = gr[ipt]._imag;
              for (j = 0; j < radix2; j += 2) {
                w0 = aW + _wg[_w1 + j] * _wg[_wf + j] - _wg[_w2 + j];
                _wg[_w2 + j] = _wg[_w1 + j];
                _wg[_w1 + j] = w0;
                w0 = bW + _wg[_w1 + j + 1] * _wg[_wf + j] - _wg[_w2 + j + 1];
                _wg[_w2 + j + 1] = _wg[_w1 + j + 1];
                _wg[_w1 + j + 1] = w0;
              }
            }
            for (j = 0, ipt = bfloc; j < radix2; j += 2, ipt += di) {
              gr[ipt]._real = (float) (_wg[_w1 + j] - 0.5 * _wg[_wf + j] * _wg[_w2 + j] + _wg[_wf + j + 1]
                  * _wg[_w2 + j + 1]);
              gr[ipt]._imag = (float) (_wg[_w1 + j + 1] - 0.5 * _wg[_wf + j] * _wg[_w2 + j + 1] - _wg[_wf + j + 1]
                  * _wg[_w2 + j]);
            }
          }
        }
      }
    }
    for (i = 0; i < na; i++) {
      gr[i]._real /= na;
      gr[i]._imag /= na;
    }
    return 0;
  }

  public void padSync(final float[] idata, final int i0, final float[] odata, final int leni, final int posz,
      final int leno) {
    int i;
    int j;
    for (i = posz, j = 0; i < 0; i++, j++) {
      odata[j] = 0;
    }
    for (; i < leni; i++, j++) {
      odata[j] = idata[i0 + i];
    }
    for (; i < leno && j < leno; i++, j++) {
      odata[j] = 0;
    }
    for (i = 0; i < leni && j < leno; i++, j++) {
      odata[j] = idata[i0 + i];
    }
    for (; j < leno; j++) {
      odata[j] = 0;
    }
  }

  /**
   * Computes the minimum FFT size.
   * 
   * @param n the length of a data array.
   * @return the minimum FFT size.
   */
  public int sizeFFT(final int n) {

    // n: length of array.
    int i = 2;
    int size;
    int altSize;

    for (i = 2; i < n;) {
      i <<= 1;
    }
    size = i;
    altSize = i / 4 * 3;
    if (altSize >= n) {
      size = altSize;
    }
    altSize = i / 8 * 5;
    if (altSize >= n) {
      size = altSize;
    }
    altSize = i / 16 * 9;
    if (altSize >= n) {
      size = altSize;
    }
    return size;
  }

  /**
   * Computes the root-mean-square of the specified array.
   * 
   * @param x the input array.
   * @param nx the length of the input array.
   * @return the root-mean-square value.
   */
  private float vrms(final float[] x, final int nx) {

    float ssq = 0;

    for (int i = 0; i < nx; i++) {
      ssq += x[i] * x[i];
    }

    return (float) Math.sqrt(ssq / nx);
  }

  /**
   * @param amplitude
   * @param phase
   * @param n
   * @param z
   * @param fn
   */
  public void fft(final float[] amplitude, final float[] phase, final int n, final int z, final int fn) {
    float[] ff = new float[fn];
    padSync(_wavelet.getValues(), 0, ff, n, z, fn);
    ComplexFloat[] gg = new ComplexFloat[fn];
    for (int i = 0; i < fn; i++) {
      gg[i] = new ComplexFloat(0, 0);
    }
    int rtn = fft10(ff, fn, gg);
    int i1 = fn / 2 + 1;
    if (rtn != 0) {
      String message = "OK";
      if (rtn == -3) {
        message = "Too many factors.";
      } else if (rtn == -2) {
        message = "Factor too large.";
      } else if (rtn == -1) {
        message = "N too large.";
      }
      System.out.println("FFT Error " + rtn + "; " + message);
      for (int i = 0; i < i1; i++) {
        amplitude[i] = 0;
        phase[i] = 0;
      }
    } else {
      double deg = -180 / Math.PI;
      for (int i = 0; i < i1; i++) {
        amplitude[i] = (float) Math.sqrt(gg[i].getReal() * gg[i].getReal() + gg[i].getImag() * gg[i].getImag());
        if (gg[i].getReal() == 0 && gg[i].getImag() == 0) {
          phase[i] = 0;
        } else {
          phase[i] = (float) (deg * Math.atan2(gg[i].getImag(), gg[i].getReal()));
        }
      }
    }

    ff = null;
    gg = null;
  }
}
