package org.geocraft.ui.volumeviewer.renderer.seismic;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.joml.Vector3f;


/**
 * Renders a <code>PostStack3d</code> entity in the 3D viewer.
 *
 * TODO: port from Ardor3D. Slice/texture building and
 * Z-domain conversion are stubbed out.
 */
public class PostStack3dRenderer extends VolumeViewRenderer {

  private PostStack3d _volume;
  private final PostStack3dRendererModel _model;

  private GroupNode _volumeNode;

  public PostStack3dRenderer() {
    super("");
    _model = new PostStack3dRendererModel();
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final PostStack3dRendererDialog dialog = new PostStack3dRendererDialog(shell, _volume.getDisplayName(), this, _volume);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addSpatials() {
    _volumeNode = new GroupNode(_volume.getDisplayName());
    _viewer.mapSpatial(_volumeNode, this);
    _viewer.addToScene(_volume.getZDomain(), _volumeNode);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _volume = (PostStack3d) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_volume);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree("Seismic", autoUpdate);
  }

  @Override
  public void clearOutline() {
    // TODO: port outline
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3f pickLoc) {
    return new ReadoutInfo[0];
  }

  @Override
  public SceneNode[] getSpatials(final Domain domain) {
    if (_volumeNode != null && domain == _volume.getZDomain()) {
      return new SceneNode[] { _volumeNode };
    }
    return new SceneNode[0];
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
    return new Object[] { _volume };
  }

  public PostStack3dRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  public void refresh() {
    // TODO: port refresh
  }

  public synchronized boolean calculateRange(final boolean inlineVisible, final float currentInline,
      final boolean xlineVisible, final float currentXline, final boolean zVisible, final float currentZ) {
    // TODO: port range calculation
    return false;
  }

  public SpatialExtent getExtent() {
    return null;
  }

  public void updateRendererModel(final PostStack3dRendererModel model) {
    // TODO: port model update
  }

  public void colorsChanged(final ColorMapEvent event) {
    // TODO: port color update
  }

  public void addXline(final float value, final boolean drawTexture) {
    // TODO: port xline slice
  }

  public void addInline(final float value, final boolean drawTexture) {
    // TODO: port inline slice
  }

  public void addSlice(final float value, final boolean drawTexture) {
    // TODO: port z slice
  }

  public void dispose() {
    // TODO: port dispose
  }

  public void setInlineSlice(final boolean inlineSliceVisible, final float inlineSlice) {
    // TODO: port inline slice visibility
  }

  public void setXlineSlice(final boolean xlineSliceVisible, final float xlineSlice) {
    // TODO: port xline slice visibility
  }

  public void setZSlice(final boolean zSliceVisible, final float zSlice) {
    // TODO: port z slice visibility
  }

  public void setSlices(final boolean inlineSliceVisible, final float inlineSlice,
      final boolean xlineSliceVisible, final float xlineSlice, final boolean zSliceVisible, final float zSlice) {
    // TODO: port slice update
  }

  public double[] getDataMinimumAndMaximum() {
    return new double[] { 0, 1 };
  }
}
