package org.geocraft.ui.volumeviewer.renderer.well;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.well.WellPick;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.core.rendering.scene.SphereGeometry;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.joml.Vector3f;


/**
 * Renders a <code>WellPick</code> entity in the 3D viewer.
 *
 * TODO: port Ardor3D Disk geometry to a Layer 1 equivalent.
 */
public class WellPickRenderer extends VolumeViewRenderer {

  private WellPick _wellPick;
  private final WellPickRendererModel _model;

  private SphereGeometry _wellPickDiskTime;
  private SphereGeometry _wellPickDiskDepth;

  private final double _baseRadius = IWellRendererConstants.DEFAULT_WELL_PICK_RADIUS;

  public WellPickRenderer() {
    super("");
    _model = new WellPickRendererModel();
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final WellPickRendererDialog dialog = new WellPickRendererDialog(shell, _wellPick.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addSpatials() {
    // TODO: port well pick disk geometry
    _wellPickDiskTime = new SphereGeometry(_wellPick.getDisplayName() + " time", (float) _baseRadius);
    _wellPickDiskDepth = new SphereGeometry(_wellPick.getDisplayName() + " depth", (float) _baseRadius);
    _viewer.mapSpatial(_wellPickDiskTime, this);
    _viewer.mapSpatial(_wellPickDiskDepth, this);
    _viewer.addToScene(Domain.TIME, _wellPickDiskTime);
    _viewer.addToScene(Domain.DISTANCE, _wellPickDiskDepth);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _wellPick = (WellPick) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_wellPick);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree("Wells", autoUpdate);
  }

  @Override
  public SceneNode[] getSpatials(final Domain domain) {
    if (domain == Domain.TIME && _wellPickDiskTime != null) {
      return new SceneNode[] { _wellPickDiskTime };
    } else if (domain == Domain.DISTANCE && _wellPickDiskDepth != null) {
      return new SceneNode[] { _wellPickDiskDepth };
    }
    return new SceneNode[0];
  }

  @Override
  public void clearOutline() {
    // TODO: port outline
  }

  public void redraw() {
    // TODO: port redraw
  }

  @Override
  public boolean renderOutline() {
    return false;
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _wellPick };
  }

  public WellPickRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  public void refresh() {
    // TODO: port refresh
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3f pickLoc) {
    return new ReadoutInfo[0];
  }

  public SpatialExtent getExtent() {
    return null;
  }

  public void updateRendererModel(final WellPickRendererModel model) {
    // TODO: port model update
  }
}
