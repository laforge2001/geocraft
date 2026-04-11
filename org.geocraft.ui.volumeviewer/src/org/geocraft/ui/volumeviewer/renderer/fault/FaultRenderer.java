package org.geocraft.ui.volumeviewer.renderer.fault;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.fault.FaultInterpretation;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.joml.Vector3f;


/**
 * Renders a <code>FaultInterpretation</code> entity in the 3D viewer.
 *
 * TODO: port from Ardor3D. Segment lines and triangulation
 * construction are stubbed.
 */
public class FaultRenderer extends VolumeViewRenderer {

  private FaultInterpretation _fault;
  private final FaultRendererModel _model;

  private GroupNode _faultNode;

  public FaultRenderer() {
    super("Fault Renderer");
    _model = new FaultRendererModel();
  }

  @Override
  protected void addSpatials() {
    _faultNode = new GroupNode(_fault.getDisplayName());
    _viewer.mapSpatial(_faultNode, this);
    _viewer.addToScene(_fault.getZDomain(), _faultNode);
    // TODO: port segment lines and triangulation
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _fault = (FaultInterpretation) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_fault);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree("Faults", autoUpdate);
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final FaultRendererDialog dialog = new FaultRendererDialog(shell, _fault.getDisplayName(), this, _fault);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
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
    return new Object[] { _fault };
  }

  public FaultRendererModel getSettingsModel() {
    return _model;
  }

  public SpatialExtent getExtent() {
    return null;
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3f pickLoc) {
    return new ReadoutInfo[0];
  }

  @Override
  public SceneNode[] getSpatials(final Domain domain) {
    if (_faultNode != null && domain == _fault.getZDomain()) {
      return new SceneNode[] { _faultNode };
    }
    return new SceneNode[0];
  }

  @Override
  public void clearOutline() {
    // TODO: port outline
  }

  @Override
  public boolean renderOutline() {
    return false;
  }

  public void updateRendererModel(final FaultRendererModel model) {
    // TODO: port model update
  }
}
