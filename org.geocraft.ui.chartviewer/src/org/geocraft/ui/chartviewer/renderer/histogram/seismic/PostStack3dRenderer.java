/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram.seismic;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.ui.chartviewer.HistogramChartViewer;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.chartviewer.renderer.histogram.HistogramViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;


/**
 * Simple renderer for <code>PostStack3d</code> entities in the histogram view.
 */
public class PostStack3dRenderer extends HistogramViewRenderer {

  /** The volume being rendered. */
  private PostStack3d _ps3d;

  /** The model of rendering properties for the volume. */
  private PostStack3dRendererModel _model = new PostStack3dRendererModel();

  @Override
  public void setData(Shell shell, HistogramChartViewer viewer, final Object[] objects) {
    super.setData(shell, viewer, objects);
    updateHistogramData(_ps3d, _model);
  }

  @Override
  protected void addPopupMenuActions() {
    PostStack3dRendererDialog dialog = new PostStack3dRendererDialog(getShell(), _ps3d.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.SEISMIC_FOLDER, autoUpdate);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _ps3d };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _ps3d = (PostStack3d) objects[0];
    _model.setColor(_ps3d.getDisplayColor());
  }

  @Override
  protected void setNameAndImage() {
    setName(_ps3d.getDisplayName());
    setImage(ModelUI.getSharedImages().getImage(_ps3d));
  }

  public PostStack3dRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  protected boolean autoAdjustBounds() {
    return false;
  }

  /**
   * Returns the model of rendering properties.
   */
  public void updateSettings(PostStack3dRendererModel model) {
    _model.updateFrom(model);
    updateHistogramData(_ps3d, _model);
  }

  /**
   * Updates the rendering properties based on the given model.
   * 
   * @param model the model containing updated rendering properties.
   */
  public void updateHistogramData(final HistogramData histogramData) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        _histogramData = histogramData;
        removeAllShapes();
        addPlotShapes();
        redraw();
        getViewer().getPlot().adjustBounds(_modelSpace, true, true);
      }
    });
  }

  /**
   * Creates a histogram data object from a PostStack3d entity.
   * 
   * @param volume the volume from which to extract histogram data.
   * @return the histogram data for the volume.
   */
  private void updateHistogramData(PostStack3d volume, PostStack3dRendererModel model) {
    PostStack3dHistogramTask task = new PostStack3dHistogramTask(volume, model, this);
    TaskRunner.runTask(task, "PostStack3d Histogram", TaskRunner.INTERACTIVE);
  }

  @Override
  protected HistogramData createHistogramData() {
    return _histogramData;
  }
}
