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


public class AmplitudeSpectrumRenderer extends WaveletViewRenderer {

  private static IPreferenceStore _preferenceStore = PropertyStoreFactory.getStore(PreferencePage.ID);

  private Wavelet _wavelet;

  private IPlotPolyline _waveletPolyline;

  public AmplitudeSpectrumRenderer() {
    super("Amplitude Spectrum Renderer");
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
    float ampEndTime = 500f / _wavelet.getTimeInterval();
    float ampTimeInterval = 500f / _wavelet.getTimeInterval() / (fnn - 1);

    fnn = 1 + (int) (ampEndTime / ampTimeInterval);
    float endFrequency = _preferenceStore.getInt(PreferencePage.MAXIMUM_FREQUENCY);
    float maxY = ampMaxY;

    float global_fq_max = 0;
    String scaling = _preferenceStore.getString(PreferencePage.FREQUENCY_DOMAIN_SCALING);
    if (scaling.equals(PreferencePage.MANUAL_SCALING)) {
      maxY = _preferenceStore.getInt(PreferencePage.FREQUENCY_DOMAIN_SCALE_FACTOR);
      global_fq_max = 0;
    } else {
      if (maxY > global_fq_max) {
        // rescale all
        global_fq_max = maxY;
      }
      maxY = global_fq_max;
    }
    float pminfy = 0;
    float pmaxfy = 1;
    String spectrumType = _preferenceStore.getString(PreferencePage.SPECTRUM_TYPE);
    if (spectrumType.equals(PreferencePage.DECIBEL_POWER_SPECTRUM)) {
      pminfy = _preferenceStore.getInt(PreferencePage.DECIBEL_RANGE);
      pmaxfy = 0;
    }

    float[] ybase = ampWavelet;
    int yval = 0;

    for (int i = 0; i < fnn; i++) {
      float t = i * ampTimeInterval;
      float pf = 0;
      if (spectrumType.equals(PreferencePage.AMPLITUDE_SPECTRUM)) {
        pf = ybase[yval] / maxY;
      } else if (spectrumType.equals(PreferencePage.LINEAR_POWER_SPECTRUM)) {
        pf = ybase[yval] / maxY * ybase[yval] / maxY;
      } else if (spectrumType.equals(PreferencePage.DECIBEL_POWER_SPECTRUM)) {
        pf = ybase[yval] / maxY;
        if (pf < 1e-10) {
          pf = -200;
        } else {
          pf = (float) (20. * Math.log10(pf));
        }
      } else {
        pf = 0;
      }
      pf = range(pminfy, pf, pmaxfy);
      IPlotPoint plotPoint = new PlotPoint(t, pf, 0);
      plotPoint.setPropertyInheritance(true);
      _waveletPolyline.addPoint(plotPoint);
      yval++;
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

  public void draw() {
    addShape(_waveletPolyline);
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
