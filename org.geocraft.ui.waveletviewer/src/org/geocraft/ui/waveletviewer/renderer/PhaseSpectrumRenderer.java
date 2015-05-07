package org.geocraft.ui.waveletviewer.renderer;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.math.wavelet.WaveletFilter;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.waveletviewer.IWaveletViewer;
import org.geocraft.ui.waveletviewer.PreferencePage;
import org.geocraft.ui.waveletviewer.WaveletViewPart;


public class PhaseSpectrumRenderer extends WaveletViewRenderer {

  private static IPreferenceStore _preferenceStore = PropertyStoreFactory.getStore(PreferencePage.ID);

  private Wavelet _wavelet;

  private IPlotPolyline _waveletPolyline;

  public PhaseSpectrumRenderer() {
    super("Phase Spectrum Renderer");
    showReadoutInfo(false);
  }

  @Override
  protected void addToLayerTree(boolean autoUpdate) {
    addToLayerTree(IWaveletViewer.WAVELET_FOLDER, autoUpdate);
  }

  @Override
  public DataSelection getDataSelection(double x, double y) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void setNameAndImage() {
    setName(_wavelet);
    setImage(ModelUI.getSharedImages().getImage(_wavelet));
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _wavelet };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _wavelet = (Wavelet) objects[0];
  }

  @Override
  protected void addPlotShapes() {
    RGB lineColor = WaveletViewPart.getRGB(_wavelet);
    RGB pointColor = new RGB(0, 0, 0);
    _waveletPolyline = new PlotPolyline();
    _waveletPolyline.setEditable(false);
    _waveletPolyline.setSelectable(false);
    _waveletPolyline.setTextColor(lineColor);
    _waveletPolyline.setPointColor(pointColor);
    _waveletPolyline.setPointSize(0);
    _waveletPolyline.setPointStyle(PointStyle.NONE);
    _waveletPolyline.setLineColor(lineColor);
    _waveletPolyline.setLineWidth(2);
    _waveletPolyline.setLineStyle(LineStyle.SOLID);

    int nn = _wavelet.getNumSamples();
    int fnn = computeSizeOfFFT(Math.max((int) (1024 / _wavelet.getTimeInterval()), nn));
    float[] ampWavelet = new float[fnn / 2 + 1];
    float[] phaseWavelet = new float[fnn / 2 + 1];
    doFFT(_wavelet, ampWavelet, phaseWavelet, nn, -(int) (_wavelet.getTimeStart() / _wavelet.getTimeInterval()), fnn);
    fnn = fnn / 2 + 1;
    float ampMaxY = 0;
    for (int i = 0; i < fnn; i++) {
      ampMaxY = Math.max(ampMaxY, ampWavelet[i]);
    }
    float phaseEndTime = 500f / _wavelet.getTimeInterval();
    float phaseTimeInterval = 500f / _wavelet.getTimeInterval() / (fnn - 1);

    float min_freq_spec = _preferenceStore.getInt(PreferencePage.AMPLITUDE_SPECTRUM_THRESHOLD);
    fnn = 1 + (int) (phaseEndTime / phaseTimeInterval);
    //float endFrequency = _preferenceStore.getInt(PreferencePage.MAXIMUM_FREQUENCY);
    boolean unwrapPhase = _preferenceStore.getBoolean(PreferencePage.UNWRAP_PHASE);

    float[] ybase = new float[fnn];
    int yval = 0;
    int fval = 0;
    float maxy = ampMaxY;
    min_freq_spec *= maxy / 100;
    ybase[yval] = 0;
    for (int i = 1; i < fnn; i++) {
      if (ampWavelet[fval + i] < min_freq_spec) {
        ybase[yval + i] = ybase[yval + i - 1];
      } else {
        ybase[yval + i] = phaseWavelet[i];
      }
    }
    float prvph = ybase[yval];
    float phwraps = 0;
    float minwraps = -180;
    float maxwraps = 180;
    for (int i = 0; i < fnn; i++) {
      float t = i * phaseTimeInterval;
      float pf = 0;
      if (!unwrapPhase) {
        pf = range(-180, ybase[yval], 180);
      } else {
        pf = ybase[yval];
        if (Math.abs(pf - prvph) > 180) {
          if (pf > prvph) {
            phwraps -= 360;
            if (phwraps < minwraps) {
              minwraps = phwraps;
              //XintPlotSetMinimum(plotstruct, 0.0, minwraps - 180);
            }
          } else {
            phwraps += 360;
            if (phwraps > maxwraps) {
              maxwraps = phwraps;
              //XintPlotSetMaximum(plotstruct, pend_freq, maxwraps + 180);
            }
          }
        }
        prvph = pf;
        pf += phwraps;
      }
      IPlotPoint plotPoint = new PlotPoint(t, pf, 0);
      plotPoint.setPropertyInheritance(true);
      _waveletPolyline.addPoint(plotPoint);
      yval++;
      fval++;
    }
    float phaseMin = _preferenceStore.getInt(PreferencePage.MINIMUM_PHASE);
    float phaseMax = _preferenceStore.getInt(PreferencePage.MAXIMUM_PHASE);
    if (phaseMin != -180 || phaseMax != 180) {
      minwraps = phaseMin;
      maxwraps = phaseMax;
      // set minimums to 0 and minwraps
      // set maximums to endFreq and maxwraps
    }
    if (unwrapPhase) {
      if (maxwraps - minwraps > 5400) {
        // set ticks to 360 and 1440
      } else {
        // set ticks to 90 and 360
      }
    }

    addShape(_waveletPolyline);
  }

  /**
   * @param Wavelet the time wavelet.
   * @param ampWavelet the output amplitude array (length = fn/2+1).
   * @param phaseWavelet the output phase array (length = fn/2+1).
   * @param n the length of the time array.
   * @param z the zero time position in time array.
   * @param fn the length of the frequency arrays (av and pv).
   */
  private void doFFT(final Wavelet wavelet, final float[] ampWavelet, final float[] phaseWavelet, final int n,
      final int z, final int fn) {
    WaveletFilter filter = new WaveletFilter(wavelet);
    filter.fft(ampWavelet, phaseWavelet, n, z, fn);
  }

  private float range(final float a, final float b, final float c) {
    return a < b ? b < c ? b : c : a;
  }

  private int computeSizeOfFFT(final int n) {
    int i;
    int alt_size;
    for (i = 2; i < n; i <<= 1) {
      // Do nothing.
    }
    int size = i;
    if ((alt_size = i / 4 * 3) >= n) {
      size = alt_size;
    }
    if ((alt_size = i / 8 * 5) >= n) {
      size = alt_size;
    }
    if ((alt_size = i / 16 * 9) >= n) {
      size = alt_size;
    }
    return size;
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#getSettingsModel()
   */
  @Override
  public Model getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }
}
