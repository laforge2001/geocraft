package org.geocraft.ui.chartviewer.renderer.histogram.well;


import org.eclipse.swt.SWT;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.chartviewer.renderer.histogram.HistogramViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;


/**
 * Simple renderer for <code>WellLogTrace</code> entities in the histogram view.
 */
public class WellLogTraceRenderer extends HistogramViewRenderer {

  /** The log trace being rendered. */
  private WellLogTrace _logTrace;

  /** The model of rendering properties for the log trace. */
  private WellLogTraceRendererModel _model = new WellLogTraceRendererModel();

  @Override
  protected void addPopupMenuActions() {
    WellLogTraceRendererDialog dialog = new WellLogTraceRendererDialog(getShell(), _logTrace.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.WELL_FOLDER, autoUpdate);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _logTrace };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _logTrace = (WellLogTrace) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_logTrace.getDisplayName());
    setImage(ModelUI.getSharedImages().getImage(_logTrace));
  }

  /**
   * Returns the model of rendering properties.
   */
  public WellLogTraceRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the rendering properties based on the given model.
   * 
   * @param model the model containing updated rendering properties.
   */
  public void updateSettings(WellLogTraceRendererModel model) {
    _model.updateFrom(model);
    removeAllShapes();
    addPlotShapes();
    redraw();
    getViewer().getPlot().adjustBounds(_modelSpace, true, true);
  }

  /**
   * Creates a histogram data object from a WellLogTrace entity.
   * The histogram data is created by reading every cell in
   * the logTrace, ignoring those with null values.
   * 
   * @param logTrace the log trace from which to extract histogram data.
   * @return the histogram data for the logTrace.
   */
  @Override
  protected HistogramData createHistogramData() {
    float[] values = _logTrace.getTraceData();
    float minValue = Float.MAX_VALUE;
    float maxValue = -Float.MAX_VALUE;
    for (int i = 0; i < values.length; i++) {
      if (!_logTrace.isNull(i)) {
        minValue = Math.min(minValue, values[i]);
        maxValue = Math.max(maxValue, values[i]);
      }
    }
    return new HistogramData(_logTrace.getDisplayName(), values, _logTrace.getNullValue(), _model.getNumCells(),
        minValue, maxValue, _model.getColor());
  }
}
