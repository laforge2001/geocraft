package org.geocraft.ui.volumeviewer.renderer.well;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.LineGeometry;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.joml.Vector3f;


/**
 * Renders a <code>Well</code> entity in the 3D viewer.
 *
 * TODO: port Ardor3D line/material building to Layer 1 LineGeometry.
 * Scene hierarchy is preserved; geometry construction is stubbed.
 */
public class WellRenderer extends VolumeViewRenderer {

  private Well _well;
  private final WellRendererModel _model;

  private GroupNode _wellNodeInTime;
  private GroupNode _wellNodeInDepth;
  private LineGeometry _wellBoreInTime;
  private LineGeometry _wellBoreInDepth;

  public WellRenderer() {
    super("");
    _model = new WellRendererModel();
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final WellRendererDialog dialog = new WellRendererDialog(shell, _well.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addSpatials() {
    _wellNodeInTime = new GroupNode(_well.getDisplayName());
    _viewer.mapSpatial(_wellNodeInTime, this);
    _viewer.addToScene(Domain.TIME, _wellNodeInTime);

    _wellNodeInDepth = new GroupNode(_well.getDisplayName());
    _viewer.mapSpatial(_wellNodeInDepth, this);
    _viewer.addToScene(Domain.DISTANCE, _wellNodeInDepth);

    _wellBoreInTime = new LineGeometry(_well.getDisplayName() + " time polyline");
    _wellBoreInDepth = new LineGeometry(_well.getDisplayName() + " depth polyline");
    // TODO: port from Ardor3D: build line vertex buffers from well bore path
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _well = (Well) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_well);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree("Wells", autoUpdate);
  }

  @Override
  public SceneNode[] getSpatials(final Domain domain) {
    if (domain == Domain.TIME && _wellBoreInTime != null) {
      return new SceneNode[] { _wellBoreInTime };
    } else if (domain == Domain.DISTANCE && _wellBoreInDepth != null) {
      return new SceneNode[] { _wellBoreInDepth };
    }
    return new SceneNode[0];
  }

  @Override
  public void clearOutline() {
    // TODO: port outline
  }

  public void redraw() {
    // TODO: port well redraw
  }

  @Override
  public boolean renderOutline() {
    return false;
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _well };
  }

  public WellRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  public void refresh() {
    // TODO: port well refresh
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3f pickLoc) {
    return new ReadoutInfo[0];
  }

  public SpatialExtent getExtent() {
    return null;
  }

  public void updateRendererModel(final WellRendererModel model) {
    // TODO: port model update
  }

}
