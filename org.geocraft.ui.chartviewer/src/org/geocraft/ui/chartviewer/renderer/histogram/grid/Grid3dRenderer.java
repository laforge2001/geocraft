package org.geocraft.ui.chartviewer.renderer.histogram.grid;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.chartviewer.renderer.histogram.HistogramViewRenderer;
import org.geocraft.ui.chartviewer.renderer.histogram.grid.Grid3dRendererModel.DataBounds;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;


/**
 * Simple renderer for <code>Grid3d</code> entities in the histogram view.
 */
public class Grid3dRenderer extends HistogramViewRenderer {

  /** The grid being rendered. */
  private Grid3d _grid;

  /** The model of rendering properties for the grid. */
  private Grid3dRendererModel _model = new Grid3dRendererModel();

  @Override
  protected void addPopupMenuActions() {
    Grid3dRendererDialog dialog = new Grid3dRendererDialog(getShell(), _grid.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.GRID_FOLDER, autoUpdate);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _grid };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _grid = (Grid3d) objects[0];
    _model.setColor(_grid.getDisplayColor());
  }

  @Override
  protected void setNameAndImage() {
    setName(_grid.getDisplayName());
    setImage(ModelUI.getSharedImages().getImage(_grid));
  }

  /**
   * Returns the model of rendering properties.
   */
  public Grid3dRendererModel getSettingsModel() {
    return _model;
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
   * Creates a histogram data object from a Grid3d entity.
   * The histogram data is created by reading every cell in
   * the grid, ignoring those with null values.
   * 
   * @param grid the grid from which to extract histogram data.
   * @return the histogram data for the grid.
   */
  @Override
  protected HistogramData createHistogramData() {
    float[][] values = _grid.getValues();
    GridGeometry3d geometry = _grid.getGeometry();
    float minValue = _grid.getMinValue();
    float maxValue = _grid.getMaxValue();
    int numRows = values.length;
    int numCols = values[0].length;
    int numPoints = 0;
    float[] values1D = new float[0];

    // Create a histogram data object based on all the data or an area-of-interest.
    if (_model.getDataBounds().equals(DataBounds.USE_AOI)) {
      List<Float> temp = new ArrayList<Float>();
      AreaOfInterest aoi = _model.getAOI();
      for (int row = 0; row < numRows; row++) {
        for (int col = 0; col < numCols; col++) {
          double[] xy = geometry.transformRowColToXY(row, col);
          if (aoi.contains(xy[0], xy[1])) {
            numPoints++;
            temp.add(_grid.getValueAtRowCol(row, col));
          }
        }
      }
      values1D = new float[numPoints];
      for (int i = 0; i < numPoints; i++) {
        values1D[i] = temp.get(i);
      }
    } else {
      values1D = new float[numRows * numCols];
      for (int row = 0; row < numRows; row++) {
        System.arraycopy(values[row], 0, values1D, row * numCols, numCols);
      }
    }
    return new HistogramData(_grid.getDisplayName(), values1D, _grid.getNullValue(), _model.getNumCells(), minValue,
        maxValue, _model.getColor());
  }

}
