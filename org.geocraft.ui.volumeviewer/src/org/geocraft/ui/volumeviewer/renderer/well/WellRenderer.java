package org.geocraft.ui.volumeviewer.renderer.well;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText.Alignment;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.Spatial.LightCombineMode;


/**
 * Renders a <code>Well</code> entity in the 3D viewer.
 */
public class WellRenderer extends VolumeViewRenderer {

  /** The well to renderer. */
  private Well _well;

  /** The model of renderer properties. */
  private final WellRendererModel _model;

  private Node _wellNodeInTime;

  private Node _wellNodeInDepth;

  /** The spatial object that provides the 3D view of the well bore in time. */
  private Line _wellBoreInTime;

  /** The spatial object that provides the 3D view of the well bore in depth. */
  private Line _wellBoreInDepth;

  /**
   * Constructs a well renderer.
   */
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
    final WellBore wellBore = _well.getWellBore();

    _wellNodeInTime = new Node(_well.getDisplayName());
    _viewer.mapSpatial(_wellNodeInTime, this);
    _viewer.addToScene(Domain.TIME, _wellNodeInTime);

    _wellNodeInDepth = new Node(_well.getDisplayName());
    _viewer.mapSpatial(_wellNodeInDepth, this);
    _viewer.addToScene(Domain.DISTANCE, _wellNodeInDepth);

    Point3d[] points = wellBore.getPathTWT().getPointsDirect();
    final Vector3[] vertex = VolumeViewerHelper.points3dToVector3(points);

    _wellBoreInTime = new Line(_well.getDisplayName() + " time polyline", vertex, null, null, null);
    _wellBoreInTime.getMeshData().setIndexMode(IndexMode.LineStrip);
    _wellBoreInTime.setRenderBucketType(RenderBucketType.Opaque);
    _wellBoreInTime.setLightCombineMode(LightCombineMode.Off); // no need to light the wire the well bore
    _wellBoreInTime.setLineWidth(_model.getBoreRadius());
    final ColorRGBA boreColor = VolumeViewerHelper.colorToColorRGBA(_model.getBoreColor(), 1);
    _wellBoreInTime.setDefaultColor(boreColor);
    _wellBoreInTime.setModelBound(new OrientedBoundingBox());
    _wellBoreInTime.updateModelBound();

    final BlendState alphaState = new BlendState();
    alphaState.setEnabled(true);
    alphaState.setBlendEnabled(true);
    alphaState.setSourceFunction(SourceFunction.SourceAlpha);
    alphaState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
    _wellBoreInTime.setRenderState(alphaState);

    if (_wellBoreInTime != null) {
      //_viewer.mapSpatial(_wellBoreInTime, this);
      //_viewer.addToScene(Domain.TIME, _wellBoreInTime);
      _wellNodeInTime.attachChild(_wellBoreInTime);

      final SceneText text = getTextLabel(vertex[vertex.length - 1], _well.getDisplayName(), _well.getDisplayName(),
          Alignment.WEST);
      text.setDefaultColor(new ColorRGBA(1, 1, 0, 1));
      _wellNodeInTime.attachChild(text);
    }

    points = wellBore.getPathTVDSS().getPointsDirect();
    final Vector3[] vertex2 = VolumeViewerHelper.points3dToVector3(points);

    _wellBoreInDepth = new Line(_well.getDisplayName() + " depth polyline", vertex2, null, null, null);
    _wellBoreInDepth.getMeshData().setIndexMode(IndexMode.LineStrip);
    _wellBoreInDepth.setRenderBucketType(RenderBucketType.Opaque);
    _wellBoreInDepth.setLightCombineMode(LightCombineMode.Off); // no need to light the wire the well bore
    _wellBoreInDepth.setLineWidth(_model.getBoreRadius());
    final ColorRGBA boreColor2 = VolumeViewerHelper.colorToColorRGBA(_model.getBoreColor(), 1);
    _wellBoreInDepth.setDefaultColor(boreColor2);
    _wellBoreInDepth.setModelBound(new OrientedBoundingBox());
    _wellBoreInDepth.updateModelBound();

    final BlendState alphaState2 = new BlendState();
    alphaState2.setEnabled(true);
    alphaState2.setBlendEnabled(true);
    alphaState2.setSourceFunction(SourceFunction.SourceAlpha);
    alphaState2.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
    _wellBoreInDepth.setRenderState(alphaState2);

    if (_wellBoreInDepth != null) {
      //_viewer.mapSpatial(_wellBoreInDepth, this);
      //_viewer.addToScene(Domain.DISTANCE, _wellBoreInDepth);
      _wellNodeInDepth.attachChild(_wellBoreInDepth);

      final SceneText text = getTextLabel(vertex[vertex.length - 1], _well.getDisplayName(), _well.getDisplayName(),
          Alignment.WEST);
      text.setDefaultColor(new ColorRGBA(1, 1, 0, 1));
      _wellNodeInDepth.attachChild(text);
    }
  }

  /**
   * Build and return a scene text for a label.
   * 
   * @param loc the text location.
   * @param name the spatial name.
   * @param label the text to be rendered.
   * @param alignment the text alignment.
   * @return the scene text spatial.
   */
  protected SceneText getTextLabel(final Vector3 loc, final String name, final String text,
      final SceneText.Alignment alignment) {
    final SceneText sceneText = _viewer.createSceneText(name, text, alignment);
    sceneText.setTranslation(loc);
    sceneText.setDefaultColor(ColorRGBA.WHITE);
    return sceneText;
  }

  @Override
  public Spatial[] getSpatials(final Domain domain) {
    if (domain == Domain.TIME) {
      return new Spatial[] { _wellBoreInTime };
    } else if (domain == Domain.DISTANCE) {
      return new Spatial[] { _wellBoreInDepth };
    }
    return new Spatial[0];
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(null, null, IViewer.WELL_FOLDER, autoUpdate);
  }

  @Override
  public void clearOutline() {
    // TODO Auto-generated method stub

  }

  @Override
  public void redraw() {
    updateRendererModel(_model);
  }

  @Override
  public boolean renderOutline() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected void setNameAndImage() {
    setName(_well);
    setImage(ModelUI.getSharedImages().getImage(_well));
  }

  public Object[] getRenderedObjects() {
    return new Object[] { _well };
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    if (objects == null || objects.length == 0) {
      throw new IllegalArgumentException("No objects specified.");
    } else if (!Well.class.isAssignableFrom(objects[0].getClass())) {
      throw new IllegalArgumentException("Invalid object: " + objects[0]);
    }
    _well = (Well) objects[0];
  }

  public WellRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  public void refresh() {
    updateRendererModel(_model);
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3 pickLoc) {
    // TODO Auto-generated method stub
    return null;
  }

  public SpatialExtent getExtent() {
    final double[] xs = new double[2];
    final double[] ys = new double[2];
    final double[] zs = new double[2];
    double xmin = Double.MAX_VALUE;
    double xmax = -Double.MAX_VALUE;
    double ymin = Double.MAX_VALUE;
    double ymax = -Double.MAX_VALUE;
    double zmin = Double.MAX_VALUE;
    double zmax = -Double.MAX_VALUE;
    final WellBore wellBore = _well.getWellBore();
    Point3d[] points = new Point3d[0];
    if (_viewer.getCurrentDomain() == Domain.TIME) {
      points = wellBore.getPathTWT().getPointsDirect();
    } else if (_viewer.getCurrentDomain() == Domain.DISTANCE) {
      points = wellBore.getPathTVDSS().getPointsDirect();
    }
    for (final Point3d point : points) {
      final double x = point.getX();
      final double y = point.getY();
      final double z = point.getZ();
      xmin = Math.min(xmin, x);
      xmax = Math.max(xmax, x);
      ymin = Math.min(ymin, y);
      ymax = Math.max(ymax, y);
      zmin = Math.min(zmin, z);
      zmax = Math.max(zmax, z);
    }
    xs[0] = xmin;
    xs[1] = xmax;
    ys[0] = ymin;
    ys[1] = ymax;
    zs[0] = zmin;
    zs[1] = zmax;
    final SpatialExtent extent = new SpatialExtent(xs, ys, zs, Domain.TIME);
    return extent;
  }

  public void updateRendererModel(final WellRendererModel model) {
    _model.updateFrom(model);
    final int boreRadius = _model.getBoreRadius();
    final ColorRGBA boreColor = VolumeViewerHelper.colorToColorRGBA(_model.getBoreColor(), 1);
    _wellBoreInTime.setLineWidth(boreRadius);
    _wellBoreInTime.setDefaultColor(boreColor);

    _wellBoreInDepth.setLineWidth(boreRadius);
    _wellBoreInDepth.setDefaultColor(boreColor);
  }

}
