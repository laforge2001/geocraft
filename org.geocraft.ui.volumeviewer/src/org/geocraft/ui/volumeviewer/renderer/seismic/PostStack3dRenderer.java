package org.geocraft.ui.volumeviewer.renderer.seismic;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.SliceBufferOrder;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.LineGeometry;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;
import org.joml.Vector3f;
import org.joml.Vector4f;


/**
 * Renders a <code>PostStack3d</code> entity in the 3D viewer.
 * Displays inline, xline, and z-slice cuts through the seismic volume
 * as colored grid meshes with per-vertex amplitude coloring.
 */
public class PostStack3dRenderer extends VolumeViewRenderer {

  private PostStack3d _volume;
  private final PostStack3dRendererModel _model;

  private GroupNode _volumeNode;

  /** The mesh used to render an inline slice. */
  private MeshGeometry _inlineSliceMesh;

  /** The mesh used to render an xline slice. */
  private MeshGeometry _xlineSliceMesh;

  /** The mesh used to render a z slice. */
  private MeshGeometry _zSliceMesh;

  /** The trace data for the current inline slice. */
  private TraceData _inlineSliceData;

  /** The trace data for the current xline slice. */
  private TraceData _xlineSliceData;

  /** The trace data for the current z slice. */
  private float[] _zSliceData;

  /** The current inline slice data range. */
  private float[] _inlineRange = { Float.MAX_VALUE, -Float.MAX_VALUE };

  /** The current xline slice data range. */
  private float[] _xlineRange = { Float.MAX_VALUE, -Float.MAX_VALUE };

  /** The current z slice data range. */
  private float[] _zRange = { Float.MAX_VALUE, -Float.MAX_VALUE };

  /** The last inline slice colored. */
  private float _inlineSliceLastColored = Float.MAX_VALUE;

  /** The last xline slice colored. */
  private float _xlineSliceLastColored = Float.MAX_VALUE;

  /** The last z slice colored. */
  private float _zSliceLastColored = Float.MAX_VALUE;

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
    final CoordinateSeries extent = _volume.getExtent();
    final Point3d[] rawPoints = extent.getPointsDirect();
    final Vector3f[] points = VolumeViewerHelper.points3dToVector3(rawPoints);
    if (points == null || points.length == 0) {
      return;
    }

    // Build 24 vertices defining 12 line segments (bounding box outline).
    // points[0..3] are the bottom 4 corners, points[4..7] are the top 4 corners.
    final int nr = points.length; // expected 8
    final Vector3f[] vertex = new Vector3f[24];
    int k = 0;
    for (int i = 0; i < 4; i++) {
      // Bottom edge
      vertex[k] = points[i];
      vertex[nr + k] = points[nr / 2 + i];
      vertex[2 * nr + k] = points[i];
      k++;
      int pos = i + 1;
      if (pos == nr / 2) {
        pos = 0;
      }
      // Next bottom corner / next top corner / top of vertical
      vertex[k] = points[pos];
      vertex[nr + k] = points[nr / 2 + pos];
      vertex[2 * nr + k] = points[nr / 2 + i];
      k++;
    }

    // Pack vertices into a FloatBuffer
    final FloatBuffer buf = FloatBuffer.allocate(24 * 3);
    for (int i = 0; i < 24; i++) {
      buf.put(vertex[i].x);
      buf.put(vertex[i].y);
      buf.put(vertex[i].z);
    }
    buf.flip();

    final LineGeometry boundingBox = new LineGeometry(_volume.getDisplayName() + " bounding box");
    boundingBox.setVertices(buf, 24);
    boundingBox.setColor(new Vector4f(1, 1, 1, 1));
    boundingBox.setLineWidth(1.5f);

    _volumeNode = new GroupNode(_volume.getDisplayName());
    _volumeNode.addChild(boundingBox);

    _viewer.mapSpatial(_volumeNode, this);
    _viewer.addToScene(_volume.getZDomain(), _volumeNode);

    // Turn on the default slices.
    final boolean result = calculateRange(_model.getInlineSliceVisible(), _model.getInlineSlice(),
        _model.getXlineSliceVisible(), _model.getXlineSlice(), _model.getZSliceVisible(), _model.getZSlice());
    if (result) {
      _viewer.makeDirty();
    }
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    if (objects == null || objects.length == 0) {
      throw new IllegalArgumentException("No objects specified.");
    } else if (!PostStack3d.class.isAssignableFrom(objects[0].getClass())) {
      throw new IllegalArgumentException("Invalid object: " + objects[0]);
    }
    _volume = (PostStack3d) objects[0];
    final int inlineSliceIndex = _volume.getNumInlines() / 2;
    final int xlineSliceIndex = _volume.getNumXlines() / 2;
    final int zSliceIndex = _volume.getNumSamplesPerTrace() / 2;
    _model.setInlineSlice(_volume.getInlineStart() + inlineSliceIndex * _volume.getInlineDelta());
    _model.setXlineSlice(_volume.getXlineStart() + xlineSliceIndex * _volume.getXlineDelta());
    _model.setZSlice(_volume.getZStart() + zSliceIndex * _volume.getZDelta());
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
    // not used
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
    updateRendererModel(_model);
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
    synchronized (_model) {
      final boolean result = calculateRange(_model.getInlineSliceVisible(), _model.getInlineSlice(),
          _model.getXlineSliceVisible(), _model.getXlineSlice(), _model.getZSliceVisible(), _model.getZSlice());
      if (!result) {
        if (_model.getInlineSliceVisible()) {
          addInline(_model.getInlineSlice(), true);
        }
        if (_model.getXlineSliceVisible()) {
          addXline(_model.getXlineSlice(), true);
        }
        if (_model.getZSliceVisible()) {
          addSlice(_model.getZSlice(), true);
        }
      }
    }
    _viewer.makeDirty();
  }

  /**
   * Calculate the slices color range, read trace data, and rebuild geometry.
   */
  public synchronized boolean calculateRange(final boolean inlineVisible, final float currentInline,
      final boolean xlineVisible, final float currentXline, final boolean zVisible, final float currentZ) {
    boolean update = false;

    final StorageOrder order = _volume.getPreferredOrder();
    if (order == StorageOrder.INLINE_XLINE_Z) {
      if (inlineVisible && currentInline != _inlineSliceLastColored) {
        _inlineSliceLastColored = currentInline;
        update = true;
        _inlineSliceData = _volume.getInline(currentInline, _volume.getXlineStart(), _volume.getXlineEnd(),
            _volume.getZStart(), _volume.getZEnd());
        _inlineRange = getRange(_inlineSliceData.getData());
      }
      if (xlineVisible && currentXline != _xlineSliceLastColored) {
        _xlineSliceLastColored = currentXline;
        update = true;
        _xlineSliceData = _volume.getXline(currentXline, _volume.getInlineStart(), _volume.getInlineEnd(),
            _volume.getZStart(), _volume.getZEnd());
        _xlineRange = getRange(_xlineSliceData.getData());
      }
    } else {
      // XLINE_INLINE_Z order: read xline first for efficiency
      if (xlineVisible && currentXline != _xlineSliceLastColored) {
        _xlineSliceLastColored = currentXline;
        update = true;
        _xlineSliceData = _volume.getXline(currentXline, _volume.getInlineStart(), _volume.getInlineEnd(),
            _volume.getZStart(), _volume.getZEnd());
        _xlineRange = getRange(_xlineSliceData.getData());
      }
      if (inlineVisible && currentInline != _inlineSliceLastColored) {
        _inlineSliceLastColored = currentInline;
        update = true;
        _inlineSliceData = _volume.getInline(currentInline, _volume.getXlineStart(), _volume.getXlineEnd(),
            _volume.getZStart(), _volume.getZEnd());
        _inlineRange = getRange(_inlineSliceData.getData());
      }
    }

    if (zVisible && currentZ != _zSliceLastColored) {
      _zSliceLastColored = currentZ;
      update = true;
      _zSliceData = _volume.getSlice(_volume.getInlineStart(), _volume.getInlineEnd(),
          _volume.getXlineStart(), _volume.getXlineEnd(), currentZ, SliceBufferOrder.INLINE_XLINE, Float.NaN);
      _zRange = getRange(_zSliceData);
    }

    if (update) {
      // Compute combined data range across all visible slices
      float min = Float.MAX_VALUE;
      float max = -Float.MAX_VALUE;
      if (inlineVisible && _inlineRange[0] < Float.MAX_VALUE) {
        min = Math.min(min, _inlineRange[0]);
        max = Math.max(max, _inlineRange[1]);
      }
      if (xlineVisible && _xlineRange[0] < Float.MAX_VALUE) {
        min = Math.min(min, _xlineRange[0]);
        max = Math.max(max, _xlineRange[1]);
      }
      if (zVisible && _zRange[0] < Float.MAX_VALUE) {
        min = Math.min(min, _zRange[0]);
        max = Math.max(max, _zRange[1]);
      }
      // For seismic amplitude, use symmetric range
      final float maxAbs = Math.max(Math.abs(min), Math.abs(max));
      min = -maxAbs;
      max = maxAbs;

      // Now rebuild the slice geometry
      if (inlineVisible && _inlineSliceData != null) {
        addInline(currentInline, true);
      }
      if (xlineVisible && _xlineSliceData != null) {
        addXline(currentXline, true);
      }
      if (zVisible && _zSliceData != null) {
        addSlice(currentZ, true);
      }
      return true;
    }
    return false;
  }

  /**
   * Add an inline slice as a colored grid mesh.
   * The inline slice is a vertical plane along the xline direction.
   */
  public void addInline(final float value, final boolean drawTexture) {
    if (_inlineSliceData == null) {
      return;
    }
    final float[] inlines = { value, value };
    final float[] xlines = { _volume.getXlineStart(), _volume.getXlineEnd() };
    final Point3d[] planeTop = _volume.getSurvey().transformInlineXlineToXY(inlines, xlines).getPointsDirect();
    final float zStart = _volume.getZStart();
    final float zEnd = _volume.getZEnd();
    final Vector3f v1 = VolumeViewerHelper.point3dToVector3(planeTop[0]).add(0, 0, zStart);
    final Vector3f v2 = VolumeViewerHelper.point3dToVector3(planeTop[1]).add(0, 0, zStart);
    final Vector3f v3 = VolumeViewerHelper.point3dToVector3(planeTop[1]).add(0, 0, zEnd);
    final Vector3f v4 = VolumeViewerHelper.point3dToVector3(planeTop[0]).add(0, 0, zEnd);

    final int cols = _inlineSliceData.getNumTraces();
    final int rows = _inlineSliceData.getNumSamples();
    final float[] data = _inlineSliceData.getData();

    // Remove old mesh
    if (_inlineSliceMesh != null) {
      _volumeNode.removeChild(_inlineSliceMesh);
    }

    _inlineSliceMesh = buildSliceGrid(_volume.getDisplayName() + " inline",
        new Vector3f[] { v1, v2, v3, v4 }, cols, rows, data, _inlineRange[0], _inlineRange[1]);
    _volumeNode.addChild(_inlineSliceMesh);

    _model.setInlineSlice(value);
    _model.setInlineSliceVisible(true);
    _viewer.makeDirty();
  }

  /**
   * Add an xline slice as a colored grid mesh.
   * The xline slice is a vertical plane along the inline direction.
   */
  public void addXline(final float value, final boolean drawTexture) {
    if (_xlineSliceData == null) {
      return;
    }
    final float[] inlines = { _volume.getInlineStart(), _volume.getInlineEnd() };
    final float[] xlines = { value, value };
    final Point3d[] planeTop = _volume.getSurvey().transformInlineXlineToXY(inlines, xlines).getPointsDirect();
    final float zStart = _volume.getZStart();
    final float zEnd = _volume.getZEnd();
    final Vector3f v1 = VolumeViewerHelper.point3dToVector3(planeTop[0]).add(0, 0, zStart);
    final Vector3f v2 = VolumeViewerHelper.point3dToVector3(planeTop[1]).add(0, 0, zStart);
    final Vector3f v3 = VolumeViewerHelper.point3dToVector3(planeTop[1]).add(0, 0, zEnd);
    final Vector3f v4 = VolumeViewerHelper.point3dToVector3(planeTop[0]).add(0, 0, zEnd);

    final int cols = _xlineSliceData.getNumTraces();
    final int rows = _xlineSliceData.getNumSamples();
    final float[] data = _xlineSliceData.getData();

    // Remove old mesh
    if (_xlineSliceMesh != null) {
      _volumeNode.removeChild(_xlineSliceMesh);
    }

    _xlineSliceMesh = buildSliceGrid(_volume.getDisplayName() + " xline",
        new Vector3f[] { v1, v2, v3, v4 }, cols, rows, data, _xlineRange[0], _xlineRange[1]);
    _volumeNode.addChild(_xlineSliceMesh);

    _model.setXlineSlice(value);
    _model.setXlineSliceVisible(true);
    _viewer.makeDirty();
  }

  /**
   * Add a z (horizontal) slice as a colored grid mesh.
   */
  public void addSlice(final float value, final boolean drawTexture) {
    if (_zSliceData == null) {
      return;
    }
    final float inlineStart = _volume.getInlineStart();
    final float inlineEnd = _volume.getInlineEnd();
    final float xlineStart = _volume.getXlineStart();
    final float xlineEnd = _volume.getXlineEnd();
    // Get the 4 corners of the horizontal slice plane
    final float[] inlines = { inlineEnd, inlineStart, inlineStart, inlineEnd };
    final float[] xlinesArr = { xlineStart, xlineEnd, xlineStart, xlineEnd };
    final Point3d[] planePoints = _volume.getSurvey().transformInlineXlineToXY(inlines, xlinesArr).getPointsDirect();
    // Build corners: v1=(ILend,XLstart), v2=(ILstart,XLend), v3=(ILstart,XLstart), v4=(ILend,XLend)
    // Data order is INLINE_XLINE: inline varies slowest
    // We need: top-left=ILstart/XLstart, top-right=ILstart/XLend, bottom-right=ILend/XLend, bottom-left=ILend/XLstart
    final Vector3f v1 = VolumeViewerHelper.point3dToVector3(planePoints[2]).add(0, 0, value); // ILstart,XLstart
    final Vector3f v2 = VolumeViewerHelper.point3dToVector3(planePoints[1]).add(0, 0, value); // ILstart,XLend
    final Vector3f v3 = VolumeViewerHelper.point3dToVector3(planePoints[3]).add(0, 0, value); // ILend,XLend
    final Vector3f v4 = VolumeViewerHelper.point3dToVector3(planePoints[0]).add(0, 0, value); // ILend,XLstart

    final int cols = _volume.getNumXlines();
    final int rows = _volume.getNumInlines();

    // Remove old mesh
    if (_zSliceMesh != null) {
      _volumeNode.removeChild(_zSliceMesh);
    }

    _zSliceMesh = buildSliceGrid(_volume.getDisplayName() + " z-slice",
        new Vector3f[] { v1, v2, v3, v4 }, cols, rows, _zSliceData, _zRange[0], _zRange[1]);
    _volumeNode.addChild(_zSliceMesh);

    _model.setZSlice(value);
    _model.setZSliceVisible(true);
    _viewer.makeDirty();
  }

  /**
   * Build a grid mesh for a slice with per-vertex amplitude coloring.
   *
   * @param name      mesh name
   * @param corners   4 corner vertices: [0]=top-left, [1]=top-right, [2]=bottom-right, [3]=bottom-left
   * @param cols      number of columns in the grid
   * @param rows      number of rows in the grid
   * @param data      seismic amplitude values (rows * cols)
   * @param dataMin   minimum amplitude for color mapping
   * @param dataMax   maximum amplitude for color mapping
   * @return the mesh geometry
   */
  private MeshGeometry buildSliceGrid(String name, Vector3f[] corners, int cols, int rows,
      float[] data, float dataMin, float dataMax) {
    final int numVerts = cols * rows;
    final FloatBuffer verts = FloatBuffer.allocate(numVerts * 3);
    final FloatBuffer colors = FloatBuffer.allocate(numVerts * 4);

    for (int r = 0; r < rows; r++) {
      final float v = rows > 1 ? (float) r / (rows - 1) : 0;
      for (int c = 0; c < cols; c++) {
        final float u = cols > 1 ? (float) c / (cols - 1) : 0;
        // Bilinear interpolation of corners
        final Vector3f p = bilinearInterp(corners[0], corners[1], corners[2], corners[3], u, v);
        verts.put(p.x).put(p.y).put(p.z);
        // Color from data
        final int dataIdx = r * cols + c;
        final float val = (dataIdx < data.length) ? data[dataIdx] : 0;
        final Vector4f color = amplitudeToColor(val, dataMin, dataMax);
        colors.put(color.x).put(color.y).put(color.z).put(color.w);
      }
    }
    verts.flip();
    colors.flip();

    // Build triangle indices
    final int numTris = (cols - 1) * (rows - 1) * 2;
    final IntBuffer indices = IntBuffer.allocate(numTris * 3);
    for (int r = 0; r < rows - 1; r++) {
      for (int c = 0; c < cols - 1; c++) {
        final int i = r * cols + c;
        indices.put(i).put(i + 1).put(i + cols);
        indices.put(i + 1).put(i + cols + 1).put(i + cols);
      }
    }
    indices.flip();

    final MeshGeometry mesh = new MeshGeometry(name);
    mesh.setVertices(verts, numVerts);
    mesh.setColors(colors);
    mesh.setIndices(indices, numTris);
    return mesh;
  }

  /**
   * Bilinear interpolation between 4 corner points.
   * topLeft(0,0), topRight(1,0), bottomRight(1,1), bottomLeft(0,1)
   */
  private Vector3f bilinearInterp(Vector3f topLeft, Vector3f topRight, Vector3f bottomRight, Vector3f bottomLeft,
      float u, float v) {
    // top edge: lerp topLeft -> topRight
    final float topX = topLeft.x + (topRight.x - topLeft.x) * u;
    final float topY = topLeft.y + (topRight.y - topLeft.y) * u;
    final float topZ = topLeft.z + (topRight.z - topLeft.z) * u;
    // bottom edge: lerp bottomLeft -> bottomRight
    final float botX = bottomLeft.x + (bottomRight.x - bottomLeft.x) * u;
    final float botY = bottomLeft.y + (bottomRight.y - bottomLeft.y) * u;
    final float botZ = bottomLeft.z + (bottomRight.z - bottomLeft.z) * u;
    // vertical: lerp top -> bottom
    return new Vector3f(
        topX + (botX - topX) * v,
        topY + (botY - topY) * v,
        topZ + (botZ - topZ) * v);
  }

  /**
   * Map a seismic amplitude to a blue-white-red color.
   */
  private Vector4f amplitudeToColor(float value, float min, float max) {
    if (max == min) {
      return new Vector4f(1, 1, 1, 1);
    }
    float t = (value - min) / (max - min);
    t = Math.max(0, Math.min(1, t));
    if (t < 0.5f) {
      final float s = t * 2; // 0 to 1
      return new Vector4f(s, s, 1, 1); // blue to white
    } else {
      final float s = (t - 0.5f) * 2; // 0 to 1
      return new Vector4f(1, 1 - s, 1 - s, 1); // white to red
    }
  }

  /**
   * Compute the min/max range of the given data array, ignoring NaN and null values.
   */
  private float[] getRange(float[] data) {
    if (data == null) {
      return new float[] { Float.MAX_VALUE, -Float.MAX_VALUE };
    }
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (final float value : data) {
      if (!Float.isNaN(value) && value != PostStack3d.NULL_VALUE) {
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    }
    return new float[] { min, max };
  }

  public SpatialExtent getExtent() {
    return null;
  }

  public void updateRendererModel(final PostStack3dRendererModel model) {
    _model.updateFrom(model);
    refresh();
  }

  public void colorsChanged(final ColorMapEvent event) {
    // Refresh slices when colors change
    refresh();
  }

  public void setInlineSlice(final boolean inlineSliceVisible, final float inlineSlice) {
    _model.setInlineSliceVisible(inlineSliceVisible);
    _model.setInlineSlice(inlineSlice);
    if (inlineSliceVisible) {
      refresh();
    } else {
      if (_inlineSliceMesh != null) {
        _volumeNode.removeChild(_inlineSliceMesh);
        _inlineSliceMesh = null;
        _viewer.makeDirty();
      }
    }
  }

  public void setXlineSlice(final boolean xlineSliceVisible, final float xlineSlice) {
    _model.setXlineSliceVisible(xlineSliceVisible);
    _model.setXlineSlice(xlineSlice);
    if (xlineSliceVisible) {
      refresh();
    } else {
      if (_xlineSliceMesh != null) {
        _volumeNode.removeChild(_xlineSliceMesh);
        _xlineSliceMesh = null;
        _viewer.makeDirty();
      }
    }
  }

  public void setZSlice(final boolean zSliceVisible, final float zSlice) {
    _model.setZSliceVisible(zSliceVisible);
    _model.setZSlice(zSlice);
    if (zSliceVisible) {
      refresh();
    } else {
      if (_zSliceMesh != null) {
        _volumeNode.removeChild(_zSliceMesh);
        _zSliceMesh = null;
        _viewer.makeDirty();
      }
    }
  }

  public void setSlices(final boolean inlineSliceVisible, final float inlineSlice,
      final boolean xlineSliceVisible, final float xlineSlice, final boolean zSliceVisible, final float zSlice) {
    _model.setInlineSliceVisible(inlineSliceVisible);
    _model.setInlineSlice(inlineSlice);
    _model.setXlineSliceVisible(xlineSliceVisible);
    _model.setXlineSlice(xlineSlice);
    _model.setZSliceVisible(zSliceVisible);
    _model.setZSlice(zSlice);
    // Reset last-colored markers so calculateRange re-reads data
    _inlineSliceLastColored = Float.MAX_VALUE;
    _xlineSliceLastColored = Float.MAX_VALUE;
    _zSliceLastColored = Float.MAX_VALUE;
    refresh();
  }

  public double[] getDataMinimumAndMaximum() {
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    if (_inlineRange[0] < Float.MAX_VALUE) {
      min = Math.min(min, _inlineRange[0]);
      max = Math.max(max, _inlineRange[1]);
    }
    if (_xlineRange[0] < Float.MAX_VALUE) {
      min = Math.min(min, _xlineRange[0]);
      max = Math.max(max, _xlineRange[1]);
    }
    if (_zRange[0] < Float.MAX_VALUE) {
      min = Math.min(min, _zRange[0]);
      max = Math.max(max, _zRange[1]);
    }
    if (min == Float.MAX_VALUE) {
      return new double[] { 0, 1 };
    }
    return new double[] { min, max };
  }

  public void dispose() {
    if (_volumeNode != null) {
      if (_inlineSliceMesh != null) {
        _volumeNode.removeChild(_inlineSliceMesh);
        _inlineSliceMesh = null;
      }
      if (_xlineSliceMesh != null) {
        _volumeNode.removeChild(_xlineSliceMesh);
        _xlineSliceMesh = null;
      }
      if (_zSliceMesh != null) {
        _volumeNode.removeChild(_zSliceMesh);
        _zSliceMesh = null;
      }
    }
  }
}
