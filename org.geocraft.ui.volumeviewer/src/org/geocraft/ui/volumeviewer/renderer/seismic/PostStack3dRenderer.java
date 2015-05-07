package org.geocraft.ui.volumeviewer.renderer.seismic;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.PostStack3d.SliceBufferOrder;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.ui.common.util.algorithm.AlgorithmUtil;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.geocraft.ui.volumeviewer.renderer.grid.SmoothingMethod;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText.Alignment;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.ApplyMode;
import com.ardor3d.image.Texture.CombinerFunctionAlpha;
import com.ardor3d.image.Texture.CombinerFunctionRGB;
import com.ardor3d.image.Texture.CombinerOperandAlpha;
import com.ardor3d.image.Texture.CombinerOperandRGB;
import com.ardor3d.image.Texture.CombinerSource;
import com.ardor3d.image.util.AWTTextureUtil;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Plane;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.BlendState.TestFunction;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.TexCoords;
import com.ardor3d.scenegraph.Spatial.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;


/**
 * Renders a <code>PostStack3d</code> entity in the 3D viewer.
 */
public class PostStack3dRenderer extends VolumeViewRenderer {

  /** The stacked 3D volume to render. */
  private PostStack3d _volume;

  /** The node used as a parent for the slice spatials. */
  private Node _volumeNode;

  private Line _boundingBox;

  /** The spatial used to render an inline slice. */
  private Mesh _inlineSliceQuad;

  /** The spatial used to render an xline slice. */
  private Mesh _xlineSliceQuad;

  /** The spatial used to render a z slice. */
  private Mesh _zSliceQuad;

  /** The plane given by the inline slice. */
  private final Plane _inlineSlicePlane = new Plane();

  /** The plane given by the xline slice. */
  private final Plane _xlineSlicePlane = new Plane();

  /** The plane given by the z slice. */
  private final Plane _zSlicePlane = new Plane();

  /** The model of rendering properties. */
  private final PostStack3dRendererModel _model;

  /** The label lines. */
  private final List<Line> _labels = new ArrayList<Line>();

  /** The label text objects. */
  private final List<SceneText> _labelsText = new ArrayList<SceneText>();

  private Vector3 _extent;

  /** The trace data for the current inline slice. */
  private TraceData _inlineSliceData;

  /** The trace data for the current xline slice. */
  private TraceData _xlineSliceData;

  /** The trace data for the current z slice. */
  private float[] _zSliceData;

  /** The current xline slice data range. */
  protected float[] _xlineRange = AlgorithmUtil.DEFAULT_RANGE;

  /** The current inline slice data range. */
  protected float[] _inlineRange = AlgorithmUtil.DEFAULT_RANGE;

  /** The current z slice slice data range. */
  protected float[] _zRange = AlgorithmUtil.DEFAULT_RANGE;

  /** The last inline slice colored. */
  private float _inlineSliceLastColored = Float.MAX_VALUE;

  /** The last xline slice colored. */
  private float _xlineSliceLastColored = Float.MAX_VALUE;

  /** The last z slice colored. */
  private float _zSliceLastColored = Float.MAX_VALUE;

  public PostStack3dRenderer() {
    super("");
    _model = new PostStack3dRendererModel();
    setSmoothing(SmoothingMethod.INTERPOLATION);
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(_shell);
    final PostStack3dRendererDialog dialog = new PostStack3dRendererDialog(shell, _volume.getDisplayName(), this,
        _volume);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addSpatials() {
    final Vector3[] points = VolumeViewerHelper.points3dToVector3(_volume.getExtent().getPointsDirect());
    if (points == null || points.length == 0) {
      return;
    }

    // outline coords are defined in real world points, 
    final Vector3[] vertex = new Vector3[24]; // 24 is the number of vertices that define the outline
    final int nr = points.length;
    int k = 0;
    for (int i = 0; i < 4; i++) {
      vertex[k] = points[i];
      vertex[nr + k] = points[nr / 2 + i];
      vertex[2 * nr + k] = points[i];
      k++;
      int pos = i + 1;
      if (pos == nr / 2) {
        pos = 0;
      }
      vertex[k] = points[pos];
      vertex[nr + k] = points[nr / 2 + pos];
      vertex[2 * nr + k] = points[nr / 2 + i];
      k++;
    }
    _volumeNode = new Node(_volume.getDisplayName());
    _boundingBox = new Line(_volume.getDisplayName() + " bounding box", vertex, null, null, null);
    setLineSettings(_boundingBox, true);

    // update the geometric state of our top level node so the bounds are correct. ( do it before attaching to root so scale is definitely 1:1)
    _volumeNode.updateGeometricState(0, true);
    _extent = ((OrientedBoundingBox) _boundingBox.getWorldBound()).getExtent().clone();
    addLabels();
    _volumeNode.updateGeometricState(0, true);

    _viewer.mapSpatial(_volumeNode, this);
    _viewer.addToScene(_volume.getZDomain(), _volumeNode);

    // Go ahead an turn on the default slices.
    final boolean result = calculateRange(_model.getInlineSliceVisible(), _model.getInlineSlice(), _model
        .getXlineSliceVisible(), _model.getXlineSlice(), _model.getZSliceVisible(), _model.getZSlice(), 0);
    if (result) {
      _viewer.makeDirty();
    }
  }

  /**
   * Add the labels to the scene.
   */
  private void addLabels() {
    for (final Line line : _labels) {
      line.removeFromParent();
    }
    _labels.clear();
    for (final SceneText text : _labelsText) {
      text.removeFromParent();
    }
    _labelsText.clear();
    if (getShowLabels()) {
      final long length = Math.min(Math.round(_extent.getX() / 10), Math
          .round((_extent.getX() + _extent.getY() + _extent.getZ()) / 100));
      addInlineLabels(length / (float) _viewer.getExaggeration());
      addXlineLabels(length / (float) _viewer.getExaggeration());
      addZLabels(length);
    }
    _volumeNode.updateRenderState();
    _viewer.makeDirty();
  }

  /**
   * Add the inline labels.
   * 
   * @param length the label line length.
   */
  private void addInlineLabels(final float length) {
    final float inlineStart = _volume.getInlineStart();
    final float inlineEnd = _volume.getInlineEnd();
    final float xlineStart = _volume.getXlineStart();
    final float zEnd = _volume.getZEnd();
    final float[] xlines = new float[] { xlineStart, xlineStart };
    final List<Double> labels = Labels.getLabels(inlineStart, inlineEnd, 10);
    final Vector3[] vertex = new Vector3[2];
    for (final double label : labels) {
      final SeismicSurvey3d survey = _volume.getSurvey();
      final Point3d[] points = survey.transformInlineXlineToXY(new float[] { (float) label, (float) label }, xlines)
          .getPointsDirect();
      vertex[0] = VolumeViewerHelper.point3dToVector3(points[0]).addLocal(0, 0, zEnd);
      vertex[1] = VolumeViewerHelper.point3dToVector3(points[1]).addLocal(0, 0, zEnd + length);
      final Line line = new Line(_volume.getDisplayName() + " Inline label " + label, vertex, null, null, null);
      setLineSettings(line, false);
      _labels.add(line);
      final SceneText text = getTextLabel(vertex[1], _volume.getDisplayName() + " Inline label text " + label, label,
          Alignment.NORTH);
      text.setDefaultColor(new ColorRGBA(1, 1, 0, 1));
      _labelsText.add(text);
      _volumeNode.attachChild(text);
    }
  }

  /**
   * Add the xline labels.
   * 
   * @param length the label line length.
   */
  private void addXlineLabels(final float length) {
    final float inlineStart = _volume.getInlineStart();
    final float xlineStart = _volume.getXlineStart();
    final float xlineEnd = _volume.getXlineEnd();
    final float zEnd = _volume.getZEnd();
    final float[] inlines = new float[] { inlineStart, inlineStart };
    final List<Double> labels = Labels.getLabels(xlineStart, xlineEnd, 10);
    final Vector3[] vertex = new Vector3[2];
    for (final double label : labels) {
      final SeismicSurvey3d survey = _volume.getSurvey();
      final Point3d[] points = survey.transformInlineXlineToXY(inlines, new float[] { (float) label, (float) label })
          .getPointsDirect();
      vertex[0] = VolumeViewerHelper.point3dToVector3(points[0]).addLocal(0, 0, zEnd);
      vertex[1] = VolumeViewerHelper.point3dToVector3(points[1]).addLocal(0, 0, zEnd + length);
      final Line line = new Line(_volume.getDisplayName() + " Xline label " + label, vertex, null, null, null);
      setLineSettings(line, false);
      _labels.add(line);
      final SceneText text = getTextLabel(vertex[1], _volume.getDisplayName() + " Xline label text " + label, label,
          Alignment.NORTH);
      text.setDefaultColor(new ColorRGBA(1, 1, 0, 1));
      _labelsText.add(text);
      _volumeNode.attachChild(text);
    }
  }

  /**
   * Add the z labels.
   * 
   * @param length the label line length
   */
  private void addZLabels(final long length) {
    final float inlineStart = _volume.getInlineStart();
    final float xlineStart = _volume.getXlineStart();
    final float[] xlines = new float[] { xlineStart, xlineStart };
    final float[] inlines = new float[] { inlineStart, inlineStart };
    final List<Double> labels = Labels.getLabels(_volume.getZStart(), _volume.getZEnd(), 15);
    final Vector3[] vertex = new Vector3[2];
    for (final double label : labels) {
      final SeismicSurvey3d survey = _volume.getSurvey();
      final Point3d[] points = survey.transformInlineXlineToXY(inlines, xlines).getPointsDirect();
      vertex[0] = VolumeViewerHelper.point3dToVector3(points[0]).addLocal(0, 0, (float) label);
      vertex[1] = VolumeViewerHelper.point3dToVector3(points[1]).addLocal(-length, -length, (float) label);
      final Line line = new Line(_volume.getDisplayName() + " Z label " + label, vertex, null, null, null);
      setLineSettings(line, false);
      _labels.add(line);
      final SceneText text = getTextLabel(vertex[1], _volume.getDisplayName() + " Z label text " + label, label,
          Alignment.WEST);
      text.setDefaultColor(new ColorRGBA(1, 1, 0, 1));
      _labelsText.add(text);
      _volumeNode.attachChild(text);
    }
  }

  /**
   * Set the visual settings of the specified line.
   * 
   * @param line the line.
   * @param bounding if the line should have a bounding volume.
   */
  private void setLineSettings(final Line line, final boolean bounding) {
    line.getMeshData().setIndexMode(IndexMode.Lines);
    line.setRenderBucketType(RenderBucketType.Opaque);
    line.setLightCombineMode(LightCombineMode.Off); // no need to light the wire frame boxes
    line.setLineWidth(1.5f);
    line.setAntialiased(true);
    line.setSolidColor(ColorRGBA.WHITE);
    line.setDefaultColor(ColorRGBA.WHITE);
    if (bounding) {
      line.setModelBound(new OrientedBoundingBox());
      line.updateModelBound();
    } else {
      line.setModelBound(null);
    }
    _volumeNode.attachChild(line);

    // Setup line to blend for anti-aliasing.
    final BlendState alphaState = new BlendState();
    alphaState.setEnabled(true);
    alphaState.setBlendEnabled(true);
    alphaState.setSourceFunction(SourceFunction.SourceAlpha);
    alphaState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
    line.setRenderState(alphaState);
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
  protected SceneText getTextLabel(final Vector3 loc, final String name, final double label,
      final SceneText.Alignment alignment) {
    String text = (float) label + "";
    final int value = (int) label;
    if (value == label) {
      text = value + "";
    }
    final SceneText sceneText = _viewer.createSceneText(name, text, alignment);
    sceneText.setTranslation(loc);
    sceneText.setDefaultColor(ColorRGBA.WHITE);
    return sceneText;
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.SEISMIC_FOLDER, autoUpdate);
  }

  @Override
  public void clearOutline() {
    // TODO Auto-generated method stub

  }

  @Override
  public ReadoutInfo[] getReadoutData(final Vector3 pickLoc) {
    final List<ReadoutInfo> result = new ArrayList<ReadoutInfo>();
    //result.add(super.getReadoutData()[0]);

    final List<String> keys = new ArrayList<String>();
    final List<String> vals = new ArrayList<String>();

    final float[] inlineXline = _volume.getSurvey().transformXYToInlineXline(pickLoc.getX(), pickLoc.getY(), true);
    final float z = Math.round(pickLoc.getZ() / _volume.getZDelta()) * _volume.getZDelta();

    keys.add("IL");
    keys.add("XL");
    keys.add("Z");

    vals.add(inlineXline[0] + "");
    vals.add(inlineXline[1] + "");
    vals.add(z + "");

    result.add(new ReadoutInfo("Nearest slices", keys, vals));

    // TODO: replace getXline() with getSamples() when available
    //      float[] samples = _volume.getSamples(new float[] { inlineXline[0] }, new float[] { inlineXline[1] }, new float[] { z });
    //final float[] samples = _volume.getXline(inlineXline[1], inlineXline[0], inlineXline[0], z, z).getData();

    //for (ISeismic3dOverlayRenderer renderer : _renderers) {
    //  result.add(renderer.getReadoutData(this, inlineXline[0], inlineXline[1], z, getCurrentOffset()));
    //}

    return result.toArray(new ReadoutInfo[result.size()]);
  }

  @Override
  public Spatial[] getSpatials(final Domain domain) {
    if (domain == _volume.getZDomain()) {
      return new Spatial[] { _volumeNode };
    }
    return new Spatial[0];
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
    setName(_volume);
    setImage(ModelUI.getSharedImages().getImage(_volume));
  }

  public Object[] getRenderedObjects() {
    return new Object[] { _volume };
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
    _model.setDataUnit(_volume.getDataUnit());
    _model.setInlineSlice(_volume.getInlineStart() + inlineSliceIndex * _volume.getInlineDelta());
    _model.setXlineSlice(_volume.getXlineStart() + xlineSliceIndex * _volume.getXlineDelta());
    _model.setZSlice(_volume.getZStart() + zSliceIndex * _volume.getZDelta());
  }

  public PostStack3dRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  public void refresh() {
    // call in opengl thread.
    final Callable<Void> exe = new Callable<Void>() {

      public Void call() throws Exception {
        synchronized (_model) {
          final boolean result = calculateRange(_model.getInlineSliceVisible(), _model.getInlineSlice(), _model
              .getXlineSliceVisible(), _model.getXlineSlice(), _model.getZSliceVisible(), _model.getZSlice(), 0);
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
          addLabels();
        }
        return null;
      }
    };
    _viewer.enqueueGLTask(exe);
  }

  /**
   * Calculate the slices color range.
   * 
   * @param inlineVisible if the inline is visible
   * @param currentInline the current inline
   * @param xlineVisible if the xline is visible
   * @param currentXline the current xline
   * @param zVisible if the z slice is visible
   * @param currentZ the current z slice
   * @param offset not used here
   * @return the slices repaint status
   */
  public synchronized boolean calculateRange(final boolean inlineVisible, final float currentInline,
      final boolean xlineVisible, final float currentXline, final boolean zVisible, final float currentZ,
      final float offset) {
    final boolean lockRange = false;//getColorPanel().isLockRange();
    boolean update = false;

    final StorageOrder order = _volume.getPreferredOrder();
    if (order == StorageOrder.INLINE_XLINE_Z) {
      if (inlineVisible && currentInline != _inlineSliceLastColored) {
        _inlineSliceLastColored = currentInline;
        update = true;
        _inlineSliceData = _volume.getInline(currentInline, _volume.getXlineStart(), _volume.getXlineEnd(), _volume
            .getZStart(), _volume.getZEnd());
        if (!lockRange) {
          final float[] traceValues = _inlineSliceData.getData();
          _inlineRange = AlgorithmUtil.getTraceRange(traceValues);
        }
      }

      if (xlineVisible && currentXline != _xlineSliceLastColored) {
        _xlineSliceLastColored = currentXline;
        update = true;
        _xlineSliceData = _volume.getXline(currentXline, _volume.getInlineStart(), _volume.getInlineEnd(), _volume
            .getZStart(), _volume.getZEnd());
        if (!lockRange) {
          final float[] traceValues = _xlineSliceData.getData();
          _xlineRange = AlgorithmUtil.getTraceRange(traceValues);
        }
      }
    } else if (order == StorageOrder.XLINE_INLINE_Z) {
      if (xlineVisible && currentXline != _xlineSliceLastColored) {
        _xlineSliceLastColored = currentXline;
        update = true;
        _xlineSliceData = _volume.getXline(currentXline, _volume.getInlineStart(), _volume.getInlineEnd(), _volume
            .getZStart(), _volume.getZEnd());
        if (!lockRange) {
          final float[] traceValues = _xlineSliceData.getData();
          _xlineRange = AlgorithmUtil.getTraceRange(traceValues);
        }
      }

      if (inlineVisible && currentInline != _inlineSliceLastColored) {
        _inlineSliceLastColored = currentInline;
        update = true;
        _inlineSliceData = _volume.getInline(currentInline, _volume.getXlineStart(), _volume.getXlineEnd(), _volume
            .getZStart(), _volume.getZEnd());
        if (!lockRange) {
          final float[] traceValues = _inlineSliceData.getData();
          _inlineRange = AlgorithmUtil.getTraceRange(traceValues);
        }
      }
    }

    if (zVisible && currentZ != _zSliceLastColored) {
      _zSliceLastColored = currentZ;
      update = true;
      _zSliceData = _volume.getSlice(_volume.getInlineStart(), _volume.getInlineEnd(), _volume.getXlineStart(), _volume
          .getXlineEnd(), currentZ, SliceBufferOrder.INLINE_XLINE, Float.NaN);
      if (!lockRange) {
        _zRange = AlgorithmUtil.getTraceRange(_zSliceData);
      }
    }

    if (update) {
      //      for (final ISeismic3dOverlayRenderer renderer : _renderers) {
      //        renderer.calculateRange(this, xlineVisible, currentXline, inlineVisible, currentInline, zVisible, currentZ,
      //            getCurrentOffset());
      //      }
      return setCurrentRange();
    }
    return false;
  }

  public SpatialExtent getExtent() {
    // TODO Auto-generated method stub
    return null;
  }

  public void updateRendererModel(final PostStack3dRendererModel model) {
    _model.updateFrom(model);
    refresh();
  }

  public void colorsChanged(final ColorMapEvent event) {
    final ColorBar colorBarOld = _model.getColorBar();
    if (event.getColorMapModel().getNumColors() != colorBarOld.getNumColors()) {
      final ColorBar colorBar = new ColorBar(event.getColorMapModel(), colorBarOld.getStartValue(), colorBarOld
          .getEndValue(), colorBarOld.getStepValue());
      colorBar.setReversedRange(colorBarOld.isReversedRange());
      _model.setColorBar(colorBar);
      colorBarOld.dispose();
      return;
    }
    _model.getColorBar().setColors(event.getColorMapModel().getColors());

    final boolean drawTexture = true;

    final boolean reversePolarity = _model.getReversePolarity();
    final int transparency = _model.getTransparency();

    if (_inlineSliceQuad != null) {
      if (drawTexture) {
        final Texture texture = AWTTextureUtil.loadTexture(SeismicDatasetHelper.createTexture(_inlineSliceData
            .getData(), _model.getColorBar(), _inlineSliceData.getNumTraces(), _inlineSliceData.getNumSamples(),
            _inlineSliceData.getTraces(), _viewer.getMaximumTextureSize(), reversePolarity, transparency),
            getMinificationFilter(), Image.Format.GuessNoCompression, false);
        texture.setMagnificationFilter(getMagnificationFilter());
        texture.setAnisotropicFilterPercent(getAnisoLevel());
        setPrimaryTexture(_inlineSliceQuad, texture);
      }
      _inlineSliceQuad.updateRenderState();
    }

    if (_xlineSliceQuad != null) {
      if (drawTexture) {
        final Texture texture = AWTTextureUtil.loadTexture(SeismicDatasetHelper.createTexture(
            _xlineSliceData.getData(), _model.getColorBar(), _xlineSliceData.getNumTraces(), _xlineSliceData
                .getNumSamples(), _xlineSliceData.getTraces(), _viewer.getMaximumTextureSize(), reversePolarity,
            transparency), getMinificationFilter(), Image.Format.GuessNoCompression, false);
        texture.setMagnificationFilter(getMagnificationFilter());
        texture.setAnisotropicFilterPercent(getAnisoLevel());
        setPrimaryTexture(_xlineSliceQuad, texture);
      }
      _xlineSliceQuad.updateRenderState();
    }

    if (_zSliceQuad != null) {
      if (drawTexture) {
        final Texture texture = AWTTextureUtil.loadTexture(SeismicDatasetHelper.createTexture(_zSliceData, _model
            .getColorBar(), _volume.getNumInlines(), _volume.getNumXlines(), null, _viewer.getMaximumTextureSize(),
            reversePolarity, transparency), getMinificationFilter(), Image.Format.GuessNoCompression, false);
        texture.setMagnificationFilter(getMagnificationFilter());
        texture.setAnisotropicFilterPercent(getAnisoLevel());
        setPrimaryTexture(_zSliceQuad, texture);
      }
      _zSliceQuad.updateRenderState();
    }

    _viewer.makeDirty();
  }

  public void addXline(final float value, final boolean drawTexture) {
    final float[] inlines = { _volume.getInlineStart(), _volume.getInlineEnd() };
    final float[] xlines = { value, value };
    final Point3d[] planeTop = _volume.getSurvey().transformInlineXlineToXY(inlines, xlines).getPointsDirect();
    final float zStart = _volume.getZStart();
    final float zEnd = _volume.getZEnd();
    final Vector3 v1 = VolumeViewerHelper.point3dToVector3(planeTop[0]).addLocal(0, 0, zStart);
    final Vector3 v2 = VolumeViewerHelper.point3dToVector3(planeTop[1]).addLocal(0, 0, zStart);
    final Vector3 v3 = VolumeViewerHelper.point3dToVector3(planeTop[1]).addLocal(0, 0, zEnd);
    final Vector3 v4 = VolumeViewerHelper.point3dToVector3(planeTop[0]).addLocal(0, 0, zEnd);
    final FloatBuffer verts = BufferUtils.createFloatBuffer(v1, v2, v3, v4);
    _xlineSlicePlane.setPlanePoints(v1, v2, v3);
    final TexCoords uvs = new TexCoords(BufferUtils.createFloatBuffer(0, 0, 0, 1, 1, 1, 1, 0));
    final IntBuffer indices = BufferUtils.createIntBuffer(new int[] { 0, 1, 2, 0, 2, 3 });
    remove(_xlineSliceQuad);
    _xlineSliceQuad = new Mesh(_volume.getDisplayName() + " xline");
    _xlineSliceQuad.reconstruct(verts, null, null, uvs, indices);
    buildTextureQuad(_xlineSliceQuad);

    //System.out.println("********** draw xline " + value + "  " + drawTexture);

    final boolean reversePolarity = _model.getReversePolarity();
    final int transparency = _model.getTransparency();
    if (drawTexture) {
      _model.setXlineSlice(value);
      _model.setXlineSliceVisible(true);
      final Texture texture = AWTTextureUtil.loadTexture(SeismicDatasetHelper.createTexture(_xlineSliceData.getData(),
          _model.getColorBar(), _xlineSliceData.getNumTraces(), _xlineSliceData.getNumSamples(), _xlineSliceData
              .getTraces(), _viewer.getMaximumTextureSize(), reversePolarity, transparency), getMinificationFilter(),
          Image.Format.GuessNoCompression, false);
      texture.setMagnificationFilter(getMagnificationFilter());
      texture.setAnisotropicFilterPercent(getAnisoLevel());
      setPrimaryTexture(_xlineSliceQuad, texture);
      //addSecondaryTextures(_xlineQuad, PostStack3dSlice.XLINE, value);
    }
    _volumeNode.attachChild(_xlineSliceQuad);
    //_viewer.addToScene(_xlineQuad);
    _xlineSliceQuad.updateRenderState();
    _viewer.makeDirty();
  }

  public void addInline(final float value, final boolean drawTexture) {
    final float[] inlines = { value, value };
    final float[] xlines = { _volume.getXlineStart(), _volume.getXlineEnd() };
    final Point3d[] planeTop = _volume.getSurvey().transformInlineXlineToXY(inlines, xlines).getPointsDirect();
    final float zStart = _volume.getZStart();
    final float zEnd = _volume.getZEnd();
    final Vector3 v1 = VolumeViewerHelper.point3dToVector3(planeTop[0]).addLocal(0, 0, zStart);
    final Vector3 v2 = VolumeViewerHelper.point3dToVector3(planeTop[1]).addLocal(0, 0, zStart);
    final Vector3 v3 = VolumeViewerHelper.point3dToVector3(planeTop[1]).addLocal(0, 0, zEnd);
    final Vector3 v4 = VolumeViewerHelper.point3dToVector3(planeTop[0]).addLocal(0, 0, zEnd);
    final FloatBuffer verts = BufferUtils.createFloatBuffer(v1, v2, v3, v4);
    _inlineSlicePlane.setPlanePoints(v1, v2, v3);
    final TexCoords uvs = new TexCoords(BufferUtils.createFloatBuffer(0, 0, 0, 1, 1, 1, 1, 0));
    final IntBuffer indices = BufferUtils.createIntBuffer(new int[] { 0, 1, 2, 0, 2, 3 });
    remove(_inlineSliceQuad);
    _inlineSliceQuad = new Mesh(_volume.getDisplayName() + " inline");
    _inlineSliceQuad.reconstruct(verts, null, null, uvs, indices);
    buildTextureQuad(_inlineSliceQuad);

    //System.out.println("********** draw inline " + value + "  " + drawTexture);

    final boolean reversePolarity = _model.getReversePolarity();
    final int transparency = _model.getTransparency();
    if (drawTexture) {
      _model.setInlineSlice(value);
      _model.setInlineSliceVisible(true);
      final Texture texture = AWTTextureUtil.loadTexture(SeismicDatasetHelper.createTexture(_inlineSliceData.getData(),
          _model.getColorBar(), _inlineSliceData.getNumTraces(), _inlineSliceData.getNumSamples(), _inlineSliceData
              .getTraces(), _viewer.getMaximumTextureSize(), reversePolarity, transparency), getMinificationFilter(),
          Image.Format.GuessNoCompression, false);
      texture.setMagnificationFilter(getMagnificationFilter());
      texture.setAnisotropicFilterPercent(getAnisoLevel());

      setPrimaryTexture(_inlineSliceQuad, texture);
      //addSecondaryTextures(_inlineQuad, PostStack3dSlice.INLINE, value);
    }
    _volumeNode.attachChild(_inlineSliceQuad);
    //_viewer.addToScene(_inlineQuad);
    _inlineSliceQuad.updateRenderState();

    _viewer.makeDirty();
  }

  public void addSlice(final float value, final boolean drawTexture) {
    final float inlineStart = _volume.getInlineStart();
    final float inlineEnd = _volume.getInlineEnd();
    final float xlineStart = _volume.getXlineStart();
    final float xlineEnd = _volume.getXlineEnd();
    final float[] inlines = { inlineEnd, inlineStart, inlineStart, inlineEnd };
    final float[] xlines = { xlineStart, xlineEnd, xlineStart, xlineEnd };
    final Point3d[] planeTop = _volume.getSurvey().transformInlineXlineToXY(inlines, xlines).getPointsDirect();
    // compute all the 4 points as the volume could be rotated
    final Vector3 v1 = VolumeViewerHelper.point3dToVector3(planeTop[0]).addLocal(0, 0, value);
    final Vector3 v2 = VolumeViewerHelper.point3dToVector3(planeTop[1]).addLocal(0, 0, value);
    final Vector3 v3 = VolumeViewerHelper.point3dToVector3(planeTop[2]).addLocal(0, 0, value);
    final Vector3 v4 = VolumeViewerHelper.point3dToVector3(planeTop[3]).addLocal(0, 0, value);
    final FloatBuffer verts = BufferUtils.createFloatBuffer(v1, v3, v2, v4);
    _zSlicePlane.setPlanePoints(v1, v3, v2);
    final TexCoords uvs = new TexCoords(BufferUtils.createFloatBuffer(0, 1, 0, 0, 1, 0, 1, 1));
    final IntBuffer indices = BufferUtils.createIntBuffer(new int[] { 0, 1, 2, 0, 2, 3 });
    remove(_zSliceQuad);
    _zSliceQuad = new Mesh(_volume.getDisplayName() + " slice");
    _zSliceQuad.reconstruct(verts, null, null, uvs, indices);
    buildTextureQuad(_zSliceQuad);

    //System.out.println("********** draw z " + value + "  " + drawTexture);

    final boolean reversePolarity = _model.getReversePolarity();
    final int transparency = _model.getTransparency();
    if (drawTexture) {
      _model.setZSlice(value);
      _model.setZSliceVisible(true);
      final Texture texture = AWTTextureUtil.loadTexture(SeismicDatasetHelper.createTexture(_zSliceData, _model
          .getColorBar(), _volume.getNumInlines(), _volume.getNumXlines(), null, _viewer.getMaximumTextureSize(),
          reversePolarity, transparency), getMinificationFilter(), Image.Format.GuessNoCompression, false);
      texture.setMagnificationFilter(getMagnificationFilter());
      texture.setAnisotropicFilterPercent(getAnisoLevel());

      setPrimaryTexture(_zSliceQuad, texture);
      //addSecondaryTextures(_zQuad, PostStack3dSlice.ZSLICE, value);
    }
    _volumeNode.attachChild(_zSliceQuad);
    //_viewer.addToScene(_zQuad);
    _zSliceQuad.updateRenderState();

    _viewer.makeDirty();
  }

  private void buildTextureQuad(final Mesh quad) {
    quad.setRenderBucketType(RenderBucketType.Transparent);
    final BlendState alphaState = new BlendState();
    alphaState.setEnabled(true);
    alphaState.setBlendEnabled(false);
    alphaState.setTestEnabled(true);
    alphaState.setTestFunction(TestFunction.GreaterThan);
    alphaState.setReference(0);
    //    alphaState.setSourceFunction(SourceFunction.SourceAlpha);
    //    alphaState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
    quad.setRenderState(alphaState);
    quad.setLightCombineMode(LightCombineMode.Off);
    quad.getMeshData().copyTextureCoordinates(0, 1, 1);
    quad.setModelBound(new BoundingBox());
    quad.updateModelBound();
    final TextureState textureS = new TextureState();
    quad.setRenderState(textureS);
  }

  public synchronized void setPrimaryTexture(final Mesh sectionMesh, final Texture primaryTexture) {
    if (sectionMesh != null && primaryTexture != null) {
      final TextureState textureState = (TextureState) sectionMesh.getLocalRenderState(StateType.Texture);
      textureState.setTexture(primaryTexture, 0);
    }
  }

  //  private void addSecondaryTextures(final Mesh quad, final PostStack3dSlice slice, final float value) {
  //    for (final ISeismic3dOverlayRenderer renderer : _renderers) {
  //      try {
  //        renderer.render(this, quad, slice, value, getCurrentOffset());
  //      } catch (final Exception ex) {
  //        LOGGER.warn("Could not render secondary texture for " + renderer.getTitle(), ex);
  //      }
  //    }
  //  }

  public void setSecondaryTexture(final Mesh crossSection, final Texture secondaryTexture, final float blendAmount) {
    final TextureState textureS = (TextureState) crossSection.getLocalRenderState(StateType.Texture);
    textureS.setTexture(secondaryTexture, 1);
    secondaryTexture.setApply(ApplyMode.Combine);

    // RGB Combine
    secondaryTexture.setCombineFuncRGB(CombinerFunctionRGB.Interpolate);
    secondaryTexture.setCombineOp0RGB(CombinerOperandRGB.SourceColor);
    secondaryTexture.setCombineSrc0RGB(CombinerSource.Previous);
    secondaryTexture.setCombineOp1RGB(CombinerOperandRGB.SourceColor);
    secondaryTexture.setCombineSrc1RGB(CombinerSource.CurrentTexture);
    secondaryTexture.setCombineOp2RGB(CombinerOperandRGB.SourceColor);
    secondaryTexture.setCombineSrc2RGB(CombinerSource.Constant);
    secondaryTexture.setBlendColor(new ColorRGBA(1 - blendAmount, 1 - blendAmount, 1 - blendAmount, 1));

    // Alpha
    secondaryTexture.setCombineFuncAlpha(CombinerFunctionAlpha.Replace);
    secondaryTexture.setCombineOp0Alpha(CombinerOperandAlpha.SourceAlpha);
    secondaryTexture.setCombineSrc0Alpha(CombinerSource.Previous);
  }

  /**
   * Cleanup the texture and other resources hold by a slice.
   * @param sliceQuad the slice quad
   */
  private void remove(final Mesh sliceQuad) {
    System.out.println("removing sliceQuad: " + sliceQuad);
    if (sliceQuad != null) {
      _volumeNode.detachChild(sliceQuad);

      // Remove any old textures being used.
      final TextureState ts = (TextureState) sliceQuad.getLocalRenderState(StateType.Texture);
      int k = 0;
      while (ts != null && ts.getTexture(k) != null) {
        final Texture t = ts.getTexture(k);
        if (t.getTextureId() > 0) {
          _viewer.cleanupTexture(t);
        }
        k++;
      }
    }
    //    for (ISeismic3dOverlayRenderer renderer : _renderers) {
    //      renderer.clear(this, null);
    //    }
    _viewer.makeDirty();
  }

  public void dispose() {
    //_registry.removePropertyChangeListener(_listener);
    _model.getColorBar().dispose();
    _labels.clear();
    _labelsText.clear();
    _inlineSliceData = null;
    _xlineSliceData = null;
    _zSliceData = null;
  }

  /**
   * Set the current color range.
   * @return the slices repaint status
   */
  protected boolean setCurrentRange() {
    boolean absoluteMax = false;
    final Unit dataUnit = _volume.getDataUnit();
    if (dataUnit == Unit.SEISMIC_AMPLITUDE) {
      absoluteMax = true;
    }
    float min = AlgorithmUtil.DEFAULT_RANGE[0];
    float max = AlgorithmUtil.DEFAULT_RANGE[1];
    final NormalizationMethod normalization = _model.getNormalizationMethod();
    switch (normalization) {
      case BY_MAXIMUM:
        min = Math.min(_xlineRange[0], _inlineRange[0]);
        min = Math.min(min, _zRange[0]);
        max = Math.max(_xlineRange[1], _inlineRange[1]);
        max = Math.max(max, _zRange[1]);
        if (absoluteMax) {
          final float maxAbs = Math.max(Math.abs(min), Math.abs(max));
          min = -maxAbs;
          max = maxAbs;
        }
        break;
      case BY_LIMITS:
        min = (float) _model.getColorBar().getStartValue();
        max = (float) _model.getColorBar().getEndValue();
        break;
      case BY_AVERAGE:
        break;
      case BY_TRACE_AVERAGE:
        break;
      case BY_TRACE_MAXIMUM:
        break;
    }
    if (min != AlgorithmUtil.DEFAULT_RANGE[0] && max != AlgorithmUtil.DEFAULT_RANGE[1]) {
      _model.getColorBar().setRange(min, max, (max - min) / 10);
      refresh();
      return true;
    }
    _model.getColorBar().setRange(0, 0, 1);
    return false;
  }

  public void setInlineSlice(final boolean inlineSliceVisible, final float inlineSlice) {
    _model.setInlineSliceVisible(inlineSliceVisible);
    _model.setInlineSlice(inlineSlice);
    if (inlineSliceVisible) {
      refresh();
    } else {
      if (_inlineSliceQuad != null) {
        _volumeNode.detachChild(_inlineSliceQuad);
      }
    }
  }

  public void setXlineSlice(final boolean xlineSliceVisible, final float xlineSlice) {
    _model.setXlineSliceVisible(xlineSliceVisible);
    _model.setXlineSlice(xlineSlice);
    if (xlineSliceVisible) {
      refresh();
    } else {
      if (_xlineSliceQuad != null) {
        _volumeNode.detachChild(_xlineSliceQuad);
      }
    }
  }

  public void setZSlice(final boolean zSliceVisible, final float zSlice) {
    _model.setZSliceVisible(zSliceVisible);
    _model.setZSlice(zSlice);
    if (zSliceVisible) {
      refresh();
    } else {
      if (_zSliceQuad != null) {
        _volumeNode.detachChild(_zSliceQuad);
      }
    }
  }

  public void setSlices(final boolean inlineSliceVisible, final float inlineSlice, final boolean xlineSliceVisible,
      final float xlineSlice, final boolean zSliceVisible, final float zSlice) {
    _model.setInlineSliceVisible(inlineSliceVisible);
    _model.setInlineSlice(inlineSlice);
    _model.setXlineSliceVisible(xlineSliceVisible);
    _model.setXlineSlice(xlineSlice);
    _model.setZSliceVisible(zSliceVisible);
    _model.setZSlice(zSlice);
    refresh();
  }

  public double[] getDataMinimumAndMaximum() {
    double min = AlgorithmUtil.DEFAULT_RANGE[0];
    double max = AlgorithmUtil.DEFAULT_RANGE[1];
    min = Math.min(_xlineRange[0], _inlineRange[0]);
    min = Math.min(min, _zRange[0]);
    max = Math.max(_xlineRange[1], _inlineRange[1]);
    max = Math.max(max, _zRange[1]);
    return new double[] { min, max };
  }
}
