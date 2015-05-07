/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram;


import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.geocraft.ui.chartviewer.AbstractChartViewer;
import org.geocraft.ui.chartviewer.HistogramChartViewer;
import org.geocraft.ui.chartviewer.data.HistogramData;


/**
 * Simple renderer for <code>HistogramData</code> objects.
 */
public class HistogramDataRenderer extends HistogramViewRenderer {

  @Override
  public void setData(final Shell shell, final HistogramChartViewer viewer, final Object[] objects) {
    _histogramData = (HistogramData) objects[0];
    super.setData(shell, viewer, objects);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(AbstractChartViewer.DEFAULT_FOLDER, autoUpdate);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _histogramData };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _histogramData = (HistogramData) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_histogramData.getDisplayName());
    setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
  }

  public HistogramDataRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected HistogramData createHistogramData() {
    return _histogramData;
  }

}
