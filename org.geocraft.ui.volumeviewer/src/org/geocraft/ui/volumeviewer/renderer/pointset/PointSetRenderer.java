package org.geocraft.ui.volumeviewer.renderer.pointset;


import java.nio.FloatBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.PointSetAttribute;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.geocraft.ui.volumeviewer.VolumeViewer;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.Spatial.LightCombineMode;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.util.geom.BufferUtils;


public class PointSetRenderer extends VolumeViewRenderer implements IPointSetRendererConstants {

  /** The pointset being rendered. */
  private PointSet _pointSet;

  private final List<Sphere> _spatials;

  /** The model of renderer properties. */
  private final PointSetRendererModel _model;

  /**
   * Constructs a pointset renderer.
   */
  public PointSetRenderer() {
    super("");
    _model = new PointSetRendererModel();
    _spatials = new ArrayList<Sphere>();
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final PointSetRendererDialog dialog = new PointSetRendererDialog(shell, _pointSet.getDisplayName(), this, _pointSet);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addSpatials() {
    final int numPoints = _pointSet.getNumPoints();
    final List<Point3d> pts = new ArrayList<Point3d>();
    for (int i = 0; i < numPoints; i++) {
      pts.add(_pointSet.getPoint(i));
    }

    final Vector3[] vertex = VolumeViewerHelper.points3dToVector3(pts.toArray(new Point3d[0]));
    final int decimation = _model.getDecimation();
    ColorRGBA[] colors = null;
    if (_model.getColorByAttribute()) {
      final String colorAttribute = _model.getColorAttribute();
      colors = getColors(_pointSet, decimation, colorAttribute);
    }
    for (int i = 0, j = 0; i < numPoints; i += decimation, j++) {
      final Sphere sphere = new Sphere(_pointSet.getDisplayName());//(_pointSet.getDisplayName(), vertex, null, null, null);
      sphere.setRenderBucketType(RenderBucketType.Opaque);
      sphere.setLightCombineMode(LightCombineMode.Off); // no need to light the wire the points
      final Vector3 center = vertex[i];
      sphere.setData(center, 8, 8, _model.getPointSize());
      final RGB rgb = _model.getPointColor();
      final ColorRGBA colorRGBA = VolumeViewerHelper.colorToColorRGBA(rgb, 1);
      sphere.setDefaultColor(colorRGBA);
      sphere.setSolidColor(colorRGBA);
      sphere.setModelBound(new OrientedBoundingBox());
      if (_model.getColorByAttribute()) {
        sphere.setSolidColor(colors[j]);
        sphere.setDefaultColor(colors[j]);
      }
      // Set the point index to be the user-data for retrieval during a pick operation.
      final int pointIndex = i;
      sphere.setUserData(Integer.toString(pointIndex));

      sphere.updateModelBound();
      _viewer.addToScene(_pointSet.getZDomain(), sphere);
      _viewer.mapSpatial(sphere, this);
      _spatials.add(sphere);
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(null, null, IViewer.POINTSET_FOLDER, autoUpdate);
  }

  @Override
  public void redraw() {
    updateRendererModel(_model);
  }

  @Override
  public void refresh() {
    updateRendererModel(_model);
  }

  @Override
  protected void setNameAndImage() {
    setName(_pointSet);
    setImage(ModelUI.getSharedImages().getImage(_pointSet));
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    if (objects == null || objects.length == 0) {
      throw new IllegalArgumentException("No objects specified.");
    } else if (!PointSet.class.isAssignableFrom(objects[0].getClass())) {
      throw new IllegalArgumentException("Invalid object: " + objects[0]);
    }
    _pointSet = (PointSet) objects[0];
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (int i = 0; i < _pointSet.getNumPoints(); i++) {
      final float value = (float) _pointSet.getZ(i);
      min = Math.min(min, value);
      max = Math.max(max, value);
    }
    _model.getColorBar().setStartValue(min);
    _model.getColorBar().setEndValue(max);
  }

  public Object[] getRenderedObjects() {
    return new Object[] { _pointSet };
  }

  public PointSetRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  public void clearOutline() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean renderOutline() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3 pickLoc) {
    final VolumeViewer viewer = getViewer();
    final Spatial spatial = viewer.getSelectedSpatial();
    if (spatial instanceof Sphere) {
      final Sphere sphere = (Sphere) spatial;
      final int pointIndex = Integer.parseInt(sphere.getUserData().toString());
      final List<String> keys = new ArrayList<String>();
      final List<String> values = new ArrayList<String>();
      if (pointIndex >= 0 && pointIndex < _pointSet.getNumPoints()) {
        final String[] attributeNames = _pointSet.getAttributeNames();
        final String[] attributeValues = _pointSet.getAttributeValues(pointIndex);
        for (int j = 0; j < attributeNames.length; j++) {
          keys.add(attributeNames[j]);
          values.add(attributeValues[j]);
        }
      }
      final ReadoutInfo[] info = new ReadoutInfo[1];
      info[0] = new ReadoutInfo(_pointSet.toString(), keys, values);
      return info;
    }
    return new ReadoutInfo[0];
  }

  @Override
  public Spatial[] getSpatials(final Domain domain) {
    // Return the spatial, but only if the domain matches.
    if (domain == _pointSet.getZDomain()) {
      return _spatials.toArray(new Spatial[0]);
    }
    return new Spatial[0];
  }

  /**
   * Updates the model of rendering properties by copying from another model.
   * 
   * @param model the model from which to copy properties.
   */
  public void updateRendererModel(final PointSetRendererModel model) {
    // Update the model.
    _model.updateFrom(model);

    // Update the point size and color.
    final int decimation = _model.getDecimation();
    final ColorRGBA color = VolumeViewerHelper.colorToColorRGBA(model.getPointColor(), 1);
    ColorRGBA[] colors = null;
    final boolean colorByAttribute = _model.getColorByAttribute();
    if (colorByAttribute) {
      final String colorAttribute = _model.getColorAttribute();
      colors = getColors(_pointSet, decimation, colorAttribute);
    }
    final boolean sizeByAttribute = _model.getSizeByAttribute();
    final String sizeAttributeName = _model.getSizeAttribute();
    final float sizeAttributeMin = _model.getSizeAttributeMin();
    final float sizeAttributeMax = _model.getSizeAttributeMax();
    final int pointSizeMin = _model.getPointSizeMin();
    final int pointSizeMax = _model.getPointSizeMax();
    PointSetAttribute sizeAttr = null;
    if (sizeByAttribute) {
      sizeAttr = _pointSet.getAttribute(sizeAttributeName);
    }
    final boolean thresholdByAttribute = _model.getThresholdByAttribute();
    final String thresholdAttributeName = _model.getThresholdAttribute();
    final double thresholdAttributeMin = _model.getThresholdAttributeMin();
    final double thresholdAttributeMax = _model.getThresholdAttributeMax();
    PointSetAttribute thresholdAttr = null;
    if (thresholdByAttribute) {
      thresholdAttr = _pointSet.getAttribute(thresholdAttributeName);
    }
    final Domain zDomain = _pointSet.getZDomain();
    for (int i = 0; i < _spatials.size(); i++) {
      final Sphere sphere = _spatials.get(i);
      if (decimation > 1 && i % decimation != 0) {
        _viewer.removeFromScene(zDomain, sphere);
        continue;
      }
      _viewer.addToScene(zDomain, sphere);
      sphere.setSolidColor(color);
      sphere.setDefaultColor(color);
      final Point3d mpt = _pointSet.getPoint(i);
      final Vector3 center = new Vector3(mpt.getX(), mpt.getY(), mpt.getZ());
      int pointSize = _model.getPointSize();
      if (sizeByAttribute) {
        float sizeAttrValue = 0;
        if (sizeAttributeName.equals(Z_ATTRIBUTE)) {
          sizeAttrValue = (float) mpt.getZ();
        } else {
          switch (sizeAttr.getType()) {
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
              sizeAttrValue = sizeAttr.getFloat(i);
              break;
            default:
              sizeAttrValue = sizeAttributeMin;
              break;
          }
        }
        float percent = (sizeAttrValue - sizeAttributeMin) / (sizeAttributeMax - sizeAttributeMin);
        percent = Math.max(percent, 0);
        percent = Math.min(percent, 1);
        pointSize = Math.round(pointSizeMin + percent * (pointSizeMax - pointSizeMin));
        pointSize = Math.max(pointSize, 1);
      }
      sphere.setData(center, 8, 8, pointSize);
      if (colorByAttribute) {
        final ColorRGBA c = colors[i];
        sphere.setSolidColor(c);
        sphere.setDefaultColor(c);
      }
      if (thresholdByAttribute) {
        if (thresholdAttr.getType() == PointSetAttribute.Type.TIMESTAMP) {
          final Timestamp thresholdAttrValue = thresholdAttr.getTimestamp(i);
          if (thresholdAttrValue.getTime() < thresholdAttributeMin
              || thresholdAttrValue.getTime() > thresholdAttributeMax) {
            _viewer.removeFromScene(zDomain, sphere);
          } else {
            _viewer.addToScene(zDomain, sphere);
          }
        } else {
          final float thresholdAttrValue = thresholdAttr.getFloat(i);
          if (thresholdAttrValue < thresholdAttributeMin || thresholdAttrValue > thresholdAttributeMax) {
            _viewer.removeFromScene(zDomain, sphere);
          } else {
            _viewer.addToScene(zDomain, sphere);
          }
        }
      }
    }
    _viewer.makeDirty();
  }

  /**
   * Returns a float buffer containing the vertices to use for rendering the spatial.
   * 
   * @param pointSet the point set.
   * @param decimation the decimation factor.
   * @param attribute the attribute to use.
   * @return the float buffer of vertices.
   */
  private FloatBuffer getVertices(final PointSet pointSet, final int decimation) {
    final int numPoints = pointSet.getNumPoints();
    final List<Point3d> pts = new ArrayList<Point3d>();
    for (int i = 0; i < numPoints; i += decimation) {
      final Point3d pt = _pointSet.getPoint(i);
      pts.add(pt);
    }
    final Vector3[] vertices = VolumeViewerHelper.points3dToVector3(pts.toArray(new Point3d[0]));
    return BufferUtils.createFloatBuffer(vertices);
  }

  /**
   * Returns a float buffer containing the colors to use for rendering the spatial.
   * 
   * @param pointSet the point set.
   * @param decimation the decimation factor.
   * @param colorAttributeName the attribute to use.
   * @return the float buffer of colors.
   */
  private ColorRGBA[] getColors(final PointSet pointSet, final int decimation, final String colorAttributeName) {
    final int numPoints = pointSet.getNumPoints();
    final List<ColorRGBA> rgbs = new ArrayList<ColorRGBA>();
    double min = Double.MAX_VALUE;
    double max = -min;
    if (colorAttributeName == Z_ATTRIBUTE) {
      for (int i = 0; i < numPoints; i += decimation) {
        final float value = (float) _pointSet.getZ(i);
        final RGB rgb = _model.getColorBar().getColor(value, true);
        final ColorRGBA color = VolumeViewerHelper.colorToColorRGBA(rgb, 1);
        rgbs.add(color);
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    } else {
      float colorAttrValue = 0;
      for (int i = 0; i < numPoints; i += decimation) {
        final PointSetAttribute colorAttr = _pointSet.getAttribute(colorAttributeName);
        switch (colorAttr.getType()) {
          case SHORT:
          case INTEGER:
          case LONG:
          case FLOAT:
          case DOUBLE:
            colorAttrValue = colorAttr.getFloat(i);
            break;
          case TIMESTAMP:
            colorAttrValue = colorAttr.getTimestamp(i).getTime();
            break;
          default:
            colorAttrValue = (float) _model.getColorBar().getStartValue();
            break;
        }
        final RGB rgb = _model.getColorBar().getColor(colorAttrValue, true);
        final ColorRGBA color = VolumeViewerHelper.colorToColorRGBA(rgb, 1);
        rgbs.add(color);
        min = Math.min(min, colorAttrValue);
        max = Math.max(max, colorAttrValue);
      }
    }
    //return BufferUtils.createFloatBuffer(rgbs.toArray(new ColorRGBA[0]));
    return rgbs.toArray(new ColorRGBA[0]);
  }

  //private PointSetPickDialog _pickDialog = null;

  //  @Override
  //  public ReadoutInfo doPickAction(final PickRecord pickRecord) {
  //    final Spatial spatial = pickRecord.getSpatial();
  //    if (spatial instanceof Sphere) {
  //      final Sphere sphere = (Sphere) spatial;
  //      final int pointIndex = Integer.parseInt(sphere.getUserData().toString());
  //      final List<String> keys = new ArrayList<String>();
  //      final List<String> values = new ArrayList<String>();
  //      if (pointIndex >= 0 && pointIndex < _pointSet.getNumPoints()) {
  //        final String[] attributeNames = _pointSet.getAttributeNames();
  //        final float[] attributeValues = _pointSet.getAttributeValues(pointIndex);
  //        for (int j = 0; j < attributeNames.length; j++) {
  //          keys.add(attributeNames[j]);
  //          values.add("" + attributeValues[j]);
  //        }
  //      }
  //      return new ReadoutInfo(_pointSet.toString(), keys, values);
  //    }
  //    return null;
  //  }

  @Override
  public void triggerClickAction(final Vector3 pickLoc, final Spatial spatial) {
    if (spatial instanceof Sphere) {
      final Sphere sphere = (Sphere) spatial;
      final int pointIndex = Integer.parseInt(sphere.getUserData().toString());
      _pointSet.triggerPointClickAction(pointIndex);
    }
  }
}
