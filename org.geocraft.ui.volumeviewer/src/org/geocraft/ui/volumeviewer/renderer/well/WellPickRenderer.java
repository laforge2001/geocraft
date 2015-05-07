package org.geocraft.ui.volumeviewer.renderer.well;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.core.model.well.WellDomain;
import org.geocraft.core.model.well.WellPick;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.layer.ViewGroupLayer;
import org.geocraft.ui.volumeviewer.RendererViewLayer;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.extension.shape.Disk;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.Spatial.LightCombineMode;


/**
 * Renders a <code>WellPick</code> entity in the 3D viewer.
 */
public class WellPickRenderer extends VolumeViewRenderer {

  /** The name of the sub-folder in which to put renderers for well logs. */
  private static final String LOGS_SUBFOLDER = "Logs";

  /** The name of the sub-folder in which to put renderers for well picks. */
  private static final String PICKS_SUBFOLDER = "Picks";

  /** The well pick to render. */
  private WellPick _wellPick;

  /** The model of rendering properties. */
  private final WellPickRendererModel _model;

  /** The spatial object that provides the 3D view of the well pick in time. */
  private Disk _wellPickDiskTime;

  /** The spatial object that provides the 3D view of the well pick in depth. */
  private Disk _wellPickDiskDepth;

  /** The base pick radius using for scaling the spatial. */
  private final double _baseRadius = IWellRendererConstants.DEFAULT_WELL_PICK_RADIUS;

  /**
   * Constructs a well pick renderer.
   */
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
    final WellBore wellBore = _wellPick.getWell().getWellBore();

    final Point3d wellLocationDepth = wellBore.getLocationFromMeasuredDepth(_wellPick
        .getValue(WellDomain.MEASURED_DEPTH), WellDomain.TRUE_VERTICAL_DEPTH_SUBSEA);
    Vector3 pickLocationDepth = null;
    if (wellLocationDepth != null) {
      pickLocationDepth = new Vector3(wellLocationDepth.getX(), wellLocationDepth.getY(), wellLocationDepth.getZ());
    }

    final Point3d wellLocationTime = wellBore.getLocationFromMeasuredDepth(_wellPick
        .getValue(WellDomain.MEASURED_DEPTH), WellDomain.TWO_WAY_TIME);
    Vector3 pickLocationTime = null;
    if (wellLocationTime != null) {
      pickLocationTime = new Vector3(wellLocationTime.getX(), wellLocationTime.getY(), wellLocationTime.getZ());
    }

    float dipAngle = 0;
    double dipAzimuth = 0;
    if (!Float.isNaN(_wellPick.getDipAngle())) {
      dipAngle = _wellPick.getDipAngle();
      dipAngle = (float) Math.toRadians(dipAngle);
    }
    if (!Float.isNaN(_wellPick.getDipAzimuth())) {
      dipAzimuth = _wellPick.getDipAzimuth();
      dipAzimuth = Math.toRadians(dipAzimuth);
    }
    final Vector3 axis = new Vector3((float) Math.cos(dipAzimuth), (float) -Math.sin(dipAzimuth), 0);
    axis.normalizeLocal();

    if (pickLocationTime != null) {
      _wellPickDiskTime = new Disk(_wellPick.getDisplayName(), 2, 20, _model.getPickRadius());
      _wellPickDiskTime.setRotation(Matrix3.getTempInstance().fromAngleAxis(dipAngle, axis));
      _wellPickDiskTime.setTranslation(pickLocationTime);
      _wellPickDiskTime.setLightCombineMode(LightCombineMode.Off); // no need to light the wire the wells
      _wellPickDiskTime.getMeshData().setIndexMode(IndexMode.TriangleStrip);
      _wellPickDiskTime.setDefaultColor(VolumeViewerHelper.colorToColorRGBA(_wellPick.getPickColor(), 1));
      _wellPickDiskTime.setModelBound(new OrientedBoundingBox());
      _wellPickDiskTime.updateModelBound();

      final BlendState alphaState = new BlendState();
      alphaState.setEnabled(true);
      alphaState.setBlendEnabled(true);
      alphaState.setSourceFunction(SourceFunction.SourceAlpha);
      alphaState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
      _wellPickDiskTime.setRenderState(alphaState);

      _viewer.mapSpatial(_wellPickDiskTime, this);
      _viewer.addToScene(Domain.TIME, _wellPickDiskTime);
    }

    if (pickLocationDepth != null) {
      _wellPickDiskDepth = new Disk(_wellPick.getDisplayName(), 2, 20, _model.getPickRadius());
      _wellPickDiskDepth.setRotation(Matrix3.getTempInstance().fromAngleAxis(dipAngle, axis));
      _wellPickDiskDepth.setTranslation(pickLocationDepth);
      _wellPickDiskDepth.setLightCombineMode(LightCombineMode.Off); // no need to light the wire the wells
      _wellPickDiskDepth.getMeshData().setIndexMode(IndexMode.TriangleStrip);
      _wellPickDiskDepth.setDefaultColor(VolumeViewerHelper.colorToColorRGBA(_wellPick.getPickColor(), 1));
      _wellPickDiskDepth.setModelBound(new OrientedBoundingBox());
      _wellPickDiskDepth.updateModelBound();

      final BlendState alphaState2 = new BlendState();
      alphaState2.setEnabled(true);
      alphaState2.setBlendEnabled(true);
      alphaState2.setSourceFunction(SourceFunction.SourceAlpha);
      alphaState2.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
      _wellPickDiskDepth.setRenderState(alphaState2);

      _viewer.mapSpatial(_wellPickDiskDepth, this);
      _viewer.addToScene(Domain.DISTANCE, _wellPickDiskDepth);
    }

  }

  @Override
  public Spatial[] getSpatials(final Domain domain) {
    if (domain == Domain.TIME && _wellPickDiskTime != null) {
      return new Spatial[] { _wellPickDiskTime };
    } else if (domain == Domain.DISTANCE && _wellPickDiskDepth != null) {
      return new Spatial[] { _wellPickDiskDepth };
    }
    return new Spatial[0];
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    final Well well = _wellPick.getWell();
    addToLayerTree(well, well.getDisplayName(), IViewer.WELL_FOLDER, autoUpdate);
  }

  /**
   * Adds the renderer to the layered model tree.
   * This method first adds the parent object and then insert the renderer
   * underneath it in the layered model. If the parent object is <i>null</i>,
   * then it simply attempts to add the renderer under the folder layer
   * specified by the folder name.
   * @param viewer the viewer in which to add the renderer.
   * @param parentObject the parent object to add before this renderer.
   * @param parentObjectName the name of the parent object to add.
   * @param folderName the name of the folder layer.
   * @param autoUpdate <i>true</i> to auto-update the view bounds; otherwise <i>false</i>.
   */
  @Override
  protected void addToLayerTree(final Object parentObject, final String parentObjectName, final String folderName,
      final boolean autoUpdate) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        // First add the parent object.
        if (parentObject != null) {
          _viewer.addObjects(true, new Object[] { parentObject });
        }

        // Create a view layer for this renderer.
        final VolumeViewRenderer renderer = WellPickRenderer.this;
        final IViewLayer viewLayer = new RendererViewLayer(renderer.getName(), renderer);

        // Attempt to find the parent layer in the layered model.
        final IViewLayer[] layers = _viewer.getLayerModel().getChildren(_viewer.findFolderLayer(folderName));
        IViewLayer parentLayer = null;
        if (parentObjectName != null && parentObjectName.length() > 0) {
          for (int k = 0; k < layers.length && parentLayer == null; k++) {
            String layerName = layers[k].getName();
            final String[] substrings = layerName.split("=");
            if (substrings.length == 2) {
              layerName = substrings[1];
            }
            if (layerName.equals(parentObjectName)) {
              parentLayer = layers[k];
              break;
            }
          }
        }
        IViewLayer picksSubfolder = null;
        final IViewLayer[] subfolders = _viewer.getLayerModel().getChildren(parentLayer);
        if (subfolders == null || subfolders.length == 0) {
          final ViewGroupLayer logsSubfolder = new ViewGroupLayer(LOGS_SUBFOLDER, LOGS_SUBFOLDER + " "
              + parentLayer.getUniqueID());
          picksSubfolder = new ViewGroupLayer(PICKS_SUBFOLDER, PICKS_SUBFOLDER + " " + parentLayer.getUniqueID());
          _viewer.getLayerModel().addLayer(logsSubfolder, parentLayer);
          _viewer.getLayerModel().addLayer(picksSubfolder, parentLayer);
        } else {
          for (final IViewLayer subfolder : subfolders) {
            if (subfolder.getName().equals(PICKS_SUBFOLDER)) {
              picksSubfolder = subfolder;
              break;
            }
          }
        }

        // If no "picks" layer found, default to a top-level folder layer.
        if (picksSubfolder == null) {
          picksSubfolder = _viewer.findFolderLayer(folderName);
        }

        // Add the layer to the layered model.
        _viewer.getLayerModel().addLayer(viewLayer, picksSubfolder);
      }
    });
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
    setName(_wellPick);
    setImage(ModelUI.getSharedImages().getImage(_wellPick));
  }

  public Object[] getRenderedObjects() {
    return new Object[] { _wellPick };
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    if (objects == null || objects.length == 0) {
      throw new IllegalArgumentException("No objects specified.");
    } else if (!WellPick.class.isAssignableFrom(objects[0].getClass())) {
      throw new IllegalArgumentException("Invalid object: " + objects[0]);
    }
    _wellPick = (WellPick) objects[0];
  }

  public WellPickRendererModel getSettingsModel() {
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
    final WellBore wellBore = _wellPick.getWell().getWellBore();
    Point3d[] points = new Point3d[0];
    if (_viewer.getCurrentDomain() == Domain.TIME) {
      points = wellBore.getPathTWT().getPointsDirect();
      zmin = _wellPick.getValue(WellDomain.TWO_WAY_TIME);
      zmax = zmin;
    } else if (_viewer.getCurrentDomain() == Domain.DISTANCE) {
      points = wellBore.getPathTVDSS().getPointsDirect();
      zmin = _wellPick.getValue(WellDomain.TRUE_VERTICAL_DEPTH_SUBSEA);
      zmax = zmin;
    }
    for (final Point3d point : points) {
      final double x = point.getX();
      final double y = point.getY();
      xmin = Math.min(xmin, x);
      xmax = Math.max(xmax, x);
      ymin = Math.min(ymin, y);
      ymax = Math.max(ymax, y);
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

  public void updateRendererModel(final WellPickRendererModel model) {
    _model.updateFrom(model);
    final double scale = _model.getPickRadius() / _baseRadius;
    final ColorRGBA pickColor = VolumeViewerHelper.colorToColorRGBA(_model.getPickColor(), 1);
    if (_wellPickDiskTime != null) {
      _wellPickDiskTime.setScale(scale);
      _wellPickDiskTime.setDefaultColor(pickColor);
    } else if (_wellPickDiskDepth != null) {
      _wellPickDiskDepth.setScale(scale);
      _wellPickDiskDepth.setDefaultColor(pickColor);
    }
  }
}
