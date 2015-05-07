package org.geocraft.ui.waveletviewer.renderer;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.Wavelet;
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


public class WaveletRenderer extends WaveletViewRenderer {

  private static IPreferenceStore _preferenceStore = PropertyStoreFactory.getStore(PreferencePage.ID);

  private Wavelet _wavelet;

  private IPlotPolyline _waveletPolyline;

  public WaveletRenderer() {
    super("Wavelet Renderer");
    showReadoutInfo(false);
  }

  @Override
  protected void addPlotShapes() {
    double ymin = Double.MAX_VALUE;
    double ymax = -Double.MAX_VALUE;

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
    float[] values = _wavelet.getValues();
    for (int i = 0; i < _wavelet.getNumSamples(); i++) {
      ymin = Math.min(ymin, values[i]);
      ymax = Math.max(ymax, values[i]);
    }

    double ymaxabs = Math.max(Math.abs(ymin), Math.abs(ymax));
    String scaling = _preferenceStore.getString(PreferencePage.TIME_DOMAIN_SCALING);
    if (scaling.equals(PreferencePage.MANUAL_SCALING)) {
      ymaxabs = 1;
    }
    for (int i = 0; i < _wavelet.getNumSamples(); i++) {
      float x = _wavelet.getTimeStart() + i * _wavelet.getTimeInterval();
      IPlotPoint plotPoint = new PlotPoint(x, values[i] / ymaxabs, 0);
      plotPoint.setPropertyInheritance(true);
      _waveletPolyline.addPoint(plotPoint);
    }
    addShape(_waveletPolyline);
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

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#getSettingsModel()
   */
  @Override
  public Model getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }
}
