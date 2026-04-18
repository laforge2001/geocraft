package org.geocraft.ui.volumeviewer.renderer.pointset;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.core.rendering.scene.SphereGeometry;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.joml.Vector3f;


/**
 * Renders a <code>PointSet</code> entity in the 3D viewer.
 *
 * TODO: port from Ardor3D. Point sphere placement and
 * per-point color/attribute updates are stubbed.
 */
public class PointSetRenderer extends VolumeViewRenderer implements IPointSetRendererConstants {

  private PointSet _pointSet;
  private final List<SphereGeometry> _spatials;
  private final PointSetRendererModel _model;

  public PointSetRenderer() {
    super("");
    _model = new PointSetRendererModel();
    _spatials = new ArrayList<>();
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final PointSetRendererDialog dialog = new PointSetRendererDialog(shell, _pointSet.getDisplayName(), this, _pointSet);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addSpatials() {
    // TODO: port from Ardor3D: build SphereGeometry per point
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _pointSet = (PointSet) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_pointSet);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree("Point Sets", autoUpdate);
  }

  public void redraw() {
    // TODO: port redraw
  }

  @Override
  public void refresh() {
    // TODO: port refresh
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _pointSet };
  }

  public PointSetRendererModel getSettingsModel() {
    return _model;
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
  public ReadoutInfo[] getReadoutData(final Vector3f pickLoc) {
    return new ReadoutInfo[0];
  }

  @Override
  public SceneNode[] getSpatials(final Domain domain) {
    return _spatials.toArray(new SceneNode[0]);
  }

  public void updateRendererModel(final PointSetRendererModel model) {
    // TODO: port model update
  }

  @Override
  public void triggerClickAction(final Vector3f pickLoc, final SceneNode spatial) {
    // TODO: port click action
  }
}
