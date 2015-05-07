/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram.grid;


import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.geometry.LineGeometry;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.chartviewer.renderer.histogram.HistogramViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;


/**
 * Simple renderer for <code>Grid2d</code> entities in the histogram view.
 */
public class Grid2dRenderer extends HistogramViewRenderer {

  /** The grid being rendered. */
  private Grid2d _grid;

  /** The model of rendering properties for the grid. */
  private Grid3dRendererModel _model = new Grid3dRendererModel();

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.GRID_FOLDER, autoUpdate);
  }

  public Object[] getRenderedObjects() {
    return new Object[] { _grid };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _grid = (Grid2d) objects[0];
    _model.setColor(_grid.getDisplayColor());
  }

  @Override
  protected void setNameAndImage() {
    setName(_grid.getDisplayName());
    setImage(ModelUI.getSharedImages().getImage(_grid));
  }

  public Grid3dRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  public DataSelection getDataSelection(double x, double y) {
    // If the layer is not visible, the histogram polygon does not exist, or the point is outside, then return null.
    if (!isVisible() || _histogramPolygon == null || !_histogramPolygon.isVisible()
        || !_histogramPolygon.isPointInside(x, y)) {
      return null;
    }
    // Otherwise return the a valid data selection object.
    DataSelection selection = new DataSelection(getClass().getSimpleName());
    selection.setSelectedObjects(new Object[] { _grid });
    return selection;
  }

  /**
   * Updates the rendering properties based on the given model.
   * 
   * @param model the model containing updated rendering properties.
   */
  public void updateSettings(Grid3dRendererModel model) {
    _model.updateFrom(model);
    removeAllShapes();
    addPlotShapes();
    redraw();
    getViewer().getPlot().adjustBounds(_modelSpace, true, true);
  }

  /**
   * Creates a histogram data object from a Grid2d entity.
   * The histogram data is created by reading every cell in
   * the grid, ignoring those with null values.
   * 
   * @param grid the grid from which to extract histogram data.
   * @return the histogram data for the grid.
   */
  @Override
  protected HistogramData createHistogramData() {
    GridGeometry2d gridGeometry = _grid.getGridGeometry();
    float minValue = _grid.getMinValue();
    float maxValue = _grid.getMaxValue();
    int numRows = gridGeometry.getNumRows();
    float[] values1D = new float[0];
    int totalBins = 0;
    for (int i = 0; i < numRows; i++) {
      totalBins += gridGeometry.getNumColumns(i);
    }
    values1D = new float[totalBins];
    int index = 0;
    for (LineGeometry lineGeometry : gridGeometry.getLines()) {
      float[] values = _grid.getValues(lineGeometry.getNumber());
      int numBins = lineGeometry.getNumBins();
      System.arraycopy(values, 0, values1D, index, numBins);
      index += numBins;
    }
    return new HistogramData(_grid.getDisplayName(), values1D, _grid.getNullValue(), totalBins, minValue, maxValue,
        _model.getColor());
  }

}
