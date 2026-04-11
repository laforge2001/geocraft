package org.geocraft.ui.volumeviewer.renderer.grid;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorMapModel;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.joml.Vector3f;


/**
 * Renders a <code>Grid3d</code> entity in the 3D viewer.
 *
 * TODO: port from Ardor3D. Texture generation, triangulated surface
 * construction, and color image updates are stubbed.
 */
public class Grid3dRenderer extends VolumeViewRenderer {

  private Grid3d _surfaceGrid;
  private GridGeometry3d _gridGeometry;
  private Grid3d _rgbGrid;
  private int _numRows;
  private int _numCols;

  private MeshGeometry _meshSpatial;
  private final Grid3dRendererModel _model;

  public Grid3dRenderer() {
    super("");
    _model = new Grid3dRendererModel();
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final Grid3dRendererDialog dialog = new Grid3dRendererDialog(shell, _surfaceGrid.getDisplayName(), this, _surfaceGrid);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addSpatials() {
    _meshSpatial = new MeshGeometry(_surfaceGrid.getDisplayName());
    _viewer.mapSpatial(_meshSpatial, this);
    _viewer.addToScene(_surfaceGrid.getDataDomain(), _meshSpatial);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _surfaceGrid = (Grid3d) objects[0];
    _gridGeometry = _surfaceGrid.getGeometry();
    _numRows = _gridGeometry.getNumRows();
    _numCols = _gridGeometry.getNumColumns();
  }

  @Override
  protected void setNameAndImage() {
    setName(_surfaceGrid);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree("Grids", autoUpdate);
  }

  public void redraw() {
    // TODO: port redraw
  }

  @Override
  public void refresh() {
    // TODO: port refresh
  }

  @Override
  public final Object[] getRenderedObjects() {
    return new Object[] { _surfaceGrid };
  }

  public Grid3dRendererModel getSettingsModel() {
    return _model;
  }

  public SpatialExtent getExtent() {
    return null;
  }

  public void recomputeColorImage() {
    // TODO: port color image recompute
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3f pickLoc) {
    return new ReadoutInfo[0];
  }

  @Override
  public String getShortMessage() {
    final Vector3f pickLoc = _viewer.getPickLocation();
    if (pickLoc != null) {
      return "x=" + pickLoc.x + ", y=" + pickLoc.y + ", z=" + pickLoc.z;
    }
    return "";
  }

  public void dispose() {
    // TODO: port dispose
  }

  @Override
  public void clearOutline() {
    // TODO: port outline
  }

  @Override
  public boolean renderOutline() {
    return false;
  }

  @Override
  public SceneNode[] getSpatials(final Domain domain) {
    if (_meshSpatial != null && domain == _surfaceGrid.getDataDomain()) {
      return new SceneNode[] { _meshSpatial };
    }
    return new SceneNode[0];
  }

  public void updateColors(final ColorMapModel colorMap) {
    // TODO: port color map update
  }

  public void updateRendererModel(final Grid3dRendererModel model) {
    // TODO: port model update
  }
}
