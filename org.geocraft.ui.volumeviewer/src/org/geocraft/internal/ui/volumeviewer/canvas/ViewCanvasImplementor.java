/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.geocraft.internal.ui.volumeviewer.input.VolumeMouseLook;
import org.geocraft.internal.ui.volumeviewer.widget.FocusRods;
import org.geocraft.internal.ui.volumeviewer.widget.FocusRods.ShowMode;
import org.geocraft.ui.volumeviewer.IVolumeViewer;
import org.geocraft.ui.volumeviewer.VolumeViewer;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.lwjgl.LwjglCanvasRenderer;
import com.ardor3d.framework.swt.SwtCanvas;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.intersection.BoundingPickResults;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.TrianglePickData;
import com.ardor3d.intersection.TrianglePickResults;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Ray;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.queue.TransparentRenderBucket;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.renderer.state.ZBufferState.TestFunction;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.Spatial.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.TextureManager;


/**
 * The main logic implementor class for a VolumeView Ardor3D canvas.
 */
public class ViewCanvasImplementor extends LwjglCanvasRenderer implements Scene, Updater {

  private static final double BASE_ZOOM_RATIO = 0.2;

  private static final double MAX_ZOOM_RATIO = 1.0;

  private static final double MIN_ZOOM_RATIO = 0.1;

  private static final double MAX_AZIMUTH = MathUtils.DEG_TO_RAD * 90;

  private static final Vector3 UP_VECTOR = new Vector3(0, 0, 1, false);

  //TODO: At some point we should make the next two values dynamic based on the scene.
  private static final double FAR_PLANE = 10000000;

  private static final double NEAR_PLANE = 80;

  protected final LogicalLayer _logicalLayer;

  private Node _rootNode;

  private final Vector3 _viewFocalPoint = new Vector3();

  private BasicPassManager _manager;

  private double _zExageration = 1;

  private double _zoomDistance = 5;

  private final Vector3 _sunLocation = new Vector3(1, 0, 0);

  private final Vector3 cameraSphereCoords = new Vector3(1, 0, 0);

  private final Vector3 _tempVec = new Vector3();

  private final Vector3 _zoom3A = new Vector3();

  private final Vector3 _zoom3B = new Vector3();

  private final Vector2 _zoom2 = new Vector2();

  private final BoundingSphere _zoomSphere = new BoundingSphere();

  private final Quaternion _tempQuat = new Quaternion();

  private FocusRods _focRods;

  private FocusRods _cursor;

  private boolean _usePerspective = true;

  private final SwtCanvas _canvas;

  private DirectionalLight _sunLight;

  private final IVolumeViewer _view;

  private final BoundingPickResults _bPick = new BoundingPickResults();

  private final TrianglePickResults _tPick = new TrianglePickResults();

  private boolean _showPickPos;

  private RenderPass _scenePass;

  private RenderPass _wireoverPass;

  private RenderPass _widgetPass;

  private final GameTaskQueue _taskQueue = new GameTaskQueue();

  private boolean _isActive;

  private FocusRods _pick;

  private BlendState _textBlend;

  private TextureState _textTexture;

  private ZBufferState _textZBuff;

  private double _zoomFactor = 25f;

  private int _maxTextureSize = 0;

  private boolean _dirty = true;

  private VolumeMouseLook _input;

  public ViewCanvasImplementor(final SwtCanvas canvas, final IVolumeViewer view, final LogicalLayer layer) {
    super(null);
    scene = this;
    _canvas = canvas;
    _view = view;
    _logicalLayer = layer;
  }

  @Override
  public void init() {

    /** Set up how our camera sees. */
    updateCameraFrustum(_canvas);
    final Vector3 loc = new Vector3(0.0, 0.0, 0.0);
    final Vector3 up = UP_VECTOR.asMutable();
    final Vector3 dir = new Vector3(0.0, 1.0, 0.0);
    final Vector3 left = dir.cross(up, new Vector3());
    /** Move our camera to a correct place and orientation. */
    camera.setFrame(loc, left, up, dir);
    /** Signal that we've changed our camera's location/frustum. */
    camera.update();

    /** Create pass manager and pass*/
    _manager = new BasicPassManager();

    /** Create our basic render pass used for doing the default rendering */
    _scenePass = new RenderPass();
    _scenePass.setZOffset(1);
    _scenePass.setZFactor(1);

    _manager.add(_scenePass);

    /** Create our "wireover" pass, a pass that will draw the wireframe version of its contents over any rendered version. */
    _wireoverPass = new RenderPass();

    final WireframeState wires = new WireframeState();
    wires.setAntialiased(true);
    wires.setLineWidth(.25f);
    _wireoverPass.setPassState(wires);

    // Used to blend the anti-aliased wires into the scene
    final BlendState blend = new BlendState();
    blend.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
    blend.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
    blend.setBlendEnabled(true);
    _wireoverPass.setPassState(blend);

    // Used to allow the wire frame to only show when it is not blocks by another scene entity.
    final ZBufferState depthTest = new ZBufferState();
    depthTest.setEnabled(true);
    depthTest.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    _wireoverPass.setPassState(depthTest);

    // Don't allow the textures to bleed through on the wires
    final TextureState noTextureState = new TextureState();
    noTextureState.setEnabled(false);
    _wireoverPass.setPassState(noTextureState);

    // Use lighting, but no lights - see materials.
    final LightState noLights = new LightState();
    noLights.setEnabled(true);
    noLights.attach(new DirectionalLight());
    _wireoverPass.setPassState(noLights);

    // Our color materials.
    final MaterialState color = new MaterialState();
    color.setEnabled(true);
    color.setAmbient(ColorRGBA.BLACK);
    color.setDiffuse(ColorRGBA.BLACK);
    _wireoverPass.setPassState(color);

    _wireoverPass.setEnabled(true);
    _manager.add(_wireoverPass);

    /** Create rootNode */
    _rootNode = new Node("rootNode");

    _scenePass.add(_rootNode);

    /**
     * Create a ZBuffer to display pixels closest to the camera above
     * farther ones.
     */
    final ZBufferState buf = new ZBufferState();
    buf.setEnabled(true);
    buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

    _rootNode.setRenderState(buf);

    _input = VolumeMouseLook.setupTriggers(_logicalLayer, this);

    // Useful debug scene
    //    addDebugScene();

    // Setup our lighting
    final LightState sceneLights = new LightState();
    sceneLights.setEnabled(true);
    sceneLights.setTwoSidedLighting(true);
    sceneLights.setGlobalAmbient(new ColorRGBA(.3f, .3f, .3f, 1));

    _sunLight = new DirectionalLight();
    _sunLight.setEnabled(true);
    _sunLight.setDiffuse(ColorRGBA.WHITE);
    _sunLight.setSpecular(ColorRGBA.WHITE);

    // Set sun default position - "From South West"
    setSunAzimuth(225.0 * MathUtils.DEG_TO_RAD);
    setSunElevation(45.0 * MathUtils.DEG_TO_RAD);

    sceneLights.attach(_sunLight);
    _rootNode.setRenderState(sceneLights);

    /**
     * Update geometric and rendering information for scene
     */
    setExaggeration(1);
    _rootNode.updateRenderState();
    _rootNode.updateGeometricState(0.0f, true);

    /**
     * Set up a widget pass
     */
    _widgetPass = new RenderPass();
    _widgetPass.setEnabled(true);
    _manager.add(_widgetPass);
    // Create rods
    _focRods = makeRods(false);
    _widgetPass.add(_focRods);
    _cursor = makeRods(true);
    _widgetPass.add(_cursor);
    _cursor.setShowMode(ShowMode.NEVER);
    //    _cursor.setAxisLinesVisible(false);

    _pick = makeRods(false);
    _widgetPass.add(_pick);
    _pick.setShowMode(ShowMode.NEVER);
    _pick.setAxisLinesVisible(false);

    // This will force our camera into a good spot.
    final Callable<Void> exe = new Callable<Void>() {

      public Void call() {
        centerOnSpatial(Orientation.MAP_VIEW, _rootNode);
        return null;
      }
    };
    _taskQueue.enqueue(exe);
  }

  public void glInit() {
    _maxTextureSize = ContextManager.getCurrentContext().getCapabilities().getMaxTextureSize();

    // We do not need two pass at this time.
    ((TransparentRenderBucket) renderer.getQueue().getRenderBucket(RenderBucketType.Transparent))
        .setTwoPassTransparency(false);

    /** Set a black background. */
    renderer.setBackgroundColor(ColorRGBA.BLACK);

  }

  private FocusRods makeRods(final boolean cursor) {
    final FocusRods focusRods = new FocusRods(this, cursor);
    final ZBufferState noZ = new ZBufferState();
    noZ.setEnabled(false);
    focusRods.setRenderState(noZ);
    final BlendState bs = new BlendState();
    bs.setBlendEnabled(true);
    bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
    bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
    focusRods.setRenderState(bs);
    focusRods.updateRenderState();
    focusRods.updateGeometricState(0.0f, true);
    return focusRods;
  }

  /**
   * XXX: Will remove later.
   */
  private void addDebugScene() {
    final MaterialState ms = new MaterialState();
    ms.setMaterialFace(MaterialFace.FrontAndBack);
    ms.setEnabled(true);
    ms.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
    {
      // top box
      final Vector3 max = new Vector3(500, 500, 500);
      final Vector3 min = new Vector3(-500, -500, -500);

      final Box box = new Box("Box", min, max);
      box.setModelBound(new BoundingBox());
      box.updateModelBound();
      box.setTranslation(new Vector3(0, 0, 0));
      box.setRenderBucketType(RenderBucketType.Skip);
      _rootNode.attachChild(box);

      box.setRandomColors();
      box.setRenderState(ms);
    }
    {
      // east box
      final Vector3 max = new Vector3(300, 300, 300);
      final Vector3 min = new Vector3(-300, -300, -300);

      final Box box = new Box("Box", min, max);
      box.setModelBound(new BoundingBox());
      box.updateModelBound();
      box.setTranslation(new Vector3(1000, 0, 0));
      box.setRenderBucketType(RenderBucketType.Skip);
      _rootNode.attachChild(box);

      box.setRandomColors();
      box.setRenderState(ms);
    }
    {
      // north box
      final Vector3 max = new Vector3(300, 300, 300);
      final Vector3 min = new Vector3(-300, -300, -300);

      final Box box = new Box("Box", min, max);
      box.setModelBound(new BoundingBox());
      box.updateModelBound();
      box.setTranslation(new Vector3(0, 1000, 0));
      box.setRenderBucketType(RenderBucketType.Skip);
      _rootNode.attachChild(box);

      box.setRenderState(ms);
    }
    {
      // middle box
      final Vector3 max = new Vector3(300, 300, 300);
      final Vector3 min = new Vector3(-300, -300, -300);

      final Box box = new Box("Box", min, max);
      box.setModelBound(new BoundingBox());
      box.updateModelBound();
      box.setTranslation(new Vector3(0, 0, 1000));
      box.setRenderBucketType(RenderBucketType.Skip);
      _rootNode.attachChild(box);

      box.setRandomColors();
      box.setRenderState(ms);
    }
    {
      // bottom box
      final Vector3 max = new Vector3(100, 100, 100);
      final Vector3 min = new Vector3(-100, -100, -100);

      final Box box = new Box("Box", min, max);
      box.setModelBound(new BoundingBox());
      box.updateModelBound();
      box.setTranslation(new Vector3(0, 0, 2000));
      box.setRenderBucketType(RenderBucketType.Skip);
      _rootNode.attachChild(box);

      box.setRandomColors();
      box.setRenderState(ms);
    }
  }

  @MainThread
  public void update(final double tpf) {
    _focRods.setInteracting(false);

    _logicalLayer.checkTriggers(tpf);

    _taskQueue.execute();

    _manager.updatePasses(tpf);

    /** Update controllers/render states/transforms/bounds for rootNode. */
    _rootNode.updateGeometricState(tpf, true);

    _focRods.setTranslation(_viewFocalPoint);
    _focRods.update(camera, tpf, _isActive);
    _cursor.update(camera, tpf, _isActive);
    _pick.update(camera, tpf, _isActive);
  }

  @MainThread
  public boolean renderUnto(final Renderer r) {
    if (_maxTextureSize == 0) {
      glInit();
    }
    if (_dirty) {
      if (VolumeViewer.getOpenedViewers() > 1) {
        final RenderContext context = ContextManager.getCurrentContext();
        context.invalidateStates();
        r.clearBuffers();
      }

      _manager.renderPasses(r);
      //      Debugger.drawBounds(_rootNode, r);

      if (_isActive && _view.getSelectedSpatial() != null) {
        SelectionRenderer.drawOutline(r);
      }

      _dirty = false;
      return true;
    }
    //    _dirty = true;
    return false;
  }

  /**
   * Apply the current sphere coords to the camera and update the "look at" position
   */
  public void updateCamera() {
    if (camera == null) {
      return;
    }
    MathUtils.sphericalToCartesianZ(cameraSphereCoords, _tempVec);
    final Vector3 direction = camera.getDirection(Vector3.getTempInstance()).set(_tempVec).negateLocal();
    final Vector3 loc = _tempVec.multiplyLocal(_zoomDistance).addLocal(_viewFocalPoint);
    final Vector3 up = camera.getUp(Vector3.getTempInstance());
    // Compute left using spherical information...  this removes possibility of singularity at poles.
    final Vector3 left = camera.getLeft(Vector3.getTempInstance());
    left.set(0, -1, 0);
    _tempQuat.fromAngles(0, 0, cameraSphereCoords.getY());
    _tempQuat.apply(left, left);

    up.set(direction).crossLocal(left).normalizeLocal();
    camera.setAxes(left, up, direction);
    camera.setLocation(Vector3.getTempInstance().set(loc.getX(), loc.getY(), loc.getZ()));
    makeDirty();
  }

  public void zoomCamera(final double scalar) {
    if (_zoomDistance == NEAR_PLANE && scalar > 0) {
      return;
    }
    // grab current spherical position of camera and alter x
    _zoomDistance -= scalar * getZoomSpeed();
    applyZoom();
  }

  public void zoomCamera(final double scalar, final int canvasX, final int canvasY) {
    if (_zoomDistance == NEAR_PLANE && scalar > 0) {
      return;
    }
    final double factor = scalar * getZoomSpeed();
    _zoomDistance -= factor;

    // rewrite to do pinning, focus shift, etc.
    // Figure out a unit vector pointing in the direction of our mouse and store in _zoom3A
    if (_usePerspective) {
      _zoom2.set(canvasX, canvasY);
      camera.getWorldCoordinates(_zoom2, 1.0, _zoom3A).subtractLocal(camera.getLocation(Vector3.getTempInstance()));
    } else {
      final double aspect = (double) _canvas.getSize().x / _canvas.getSize().y;
      final double dx = (double) canvasX / _canvas.getSize().x;
      final double dy = (double) (_canvas.getSize().y - canvasY) / _canvas.getSize().y;
      _zoom3A.addLocal(camera.getLeft(Vector3.getTempInstance()).multiplyLocal(-(dx - .5) * _zoomDistance * aspect));
      _zoom3A.addLocal(camera.getUp(Vector3.getTempInstance()).multiplyLocal(-(dy - .5) * _zoomDistance));
      _zoom3A.addLocal(camera.getDirection(Vector3.getTempInstance()).multiplyLocal(_zoomDistance));
    }
    _zoom3A.normalizeLocal();

    // Make a unit vector pointing in the direction our camera is facing and store in _zoom3B
    camera.getDirection(_zoom3B).normalizeLocal();

    // figure out the angle between these vectors
    final double angle = _zoom3A.smallestAngleBetween(_zoom3B);

    // Use that angle to figure out the distance between the intersection of these vectors
    // and a plane at "factor" distance from the camera
    final double opp = Math.tan(angle) * factor;

    // Use pythagoras to determine distance from camera to _zoom3A's plane intersection  
    _zoom2.set(opp, factor);
    final double hyp = _zoom2.length();

    // Make a vector representing that intersection and then project it onto the camera's 
    // viewing plane (basically creating a vector for our 3 side of the triangle
    _zoom3A.multiplyLocal(hyp);
    camera.getDirection(Vector3.getTempInstance()).cross(_zoom3A, _zoom3A)
        .crossLocal(camera.getDirection(Vector3.getTempInstance()));

    // Now, either add or subtract this new vector from our view focus and then do zoom as normal! 
    if (factor > 0) {
      _viewFocalPoint.addLocal(_zoom3A);
    } else {
      _viewFocalPoint.subtractLocal(_zoom3A);
    }

    // Complete the zoom
    applyZoom();
  }

  private void applyZoom() {
    if (_zoomDistance < NEAR_PLANE) {
      // reset zoom distance
      _zoomDistance = NEAR_PLANE;
    }
    if (!isUsePerspective()) {
      setParallelFrustum();
    }
    updateCamera();

    if (_zoomSphere.getRadius() > 0) {

      // set camera distance
      final double angle1 = Math.atan(camera.getFrustumTop() / camera.getFrustumNear());
      final double d1 = _zoomSphere.radius / MathUtils.sin(angle1);
      final double angle2 = Math.atan(camera.getFrustumRight() / camera.getFrustumNear());
      final double d2 = _zoomSphere.radius / MathUtils.sin(angle2);
      final double bestDistance = Math.max(d1, d2) - NEAR_PLANE;

      final int percent = (int) (100 * (1 - (_zoomDistance - NEAR_PLANE) / bestDistance));
      _view.setMessageText("Zoom: " + percent + "%");
    }
  }

  /**
   * @return
   */
  private double getZoomSpeed() {
    if (_rootNode != null && _rootNode.getWorldBound() != null) {
      _zoomSphere.setRadius(0);
      _zoomSphere.getCenter().set(_rootNode.getWorldBound().getCenter());
      _zoomSphere.mergeLocal(_rootNode.getWorldBound());
      if (_zoomSphere.getRadius() > 0) {
        final double baseSpeed = _zoomSphere.getRadius() * BASE_ZOOM_RATIO;
        final double minSpeed = _zoomSphere.getRadius() * MIN_ZOOM_RATIO;
        final double maxSpeed = _zoomSphere.getRadius() * MAX_ZOOM_RATIO;
        final double speed = baseSpeed * _zoomDistance / _zoomSphere.getRadius();
        return Math.min(Math.max(speed, minSpeed), maxSpeed);
      }
    }
    return 10; // default in case there are no bounds for some reason
  }

  /**
   * @param factor
   */
  public void rotateCamera(final double factorH, final double factorV) {
    // grab current spherical position of camera and increase angle
    cameraSphereCoords.setY(cameraSphereCoords.getY() + factorH);
    cameraSphereCoords.setZ(cameraSphereCoords.getZ() - factorV);
    if (cameraSphereCoords.getZ() > MAX_AZIMUTH) {
      cameraSphereCoords.setZ(MAX_AZIMUTH);
    } else if (cameraSphereCoords.getZ() < -MAX_AZIMUTH) {
      cameraSphereCoords.setZ(-MAX_AZIMUTH);
    }
    updateCamera();
  }

  /**
   * @param horizontalShift
   * @param verticalShift
   */
  public void panCamera(final double horizontalShift, final double verticalShift) {
    final Vector3 offset = _tempVec;
    // Calculate a rough estimate of the ratio between screen pixels and units at the focal point.
    final double nearDist = _canvas.getSize().y / 0.828427124; // (2 * MathUtils.tan(MathUtils.HALF_PI / 4f)) - based on FOV = 45 degrees
    final double ratio = (nearDist + _zoomDistance) / nearDist;
    if (horizontalShift != 0) {
      camera.getLeft(Vector3.getTempInstance()).multiply(horizontalShift * ratio, offset);
      _viewFocalPoint.addLocal(offset);
    }
    if (verticalShift != 0) {
      camera.getUp(Vector3.getTempInstance()).multiply(verticalShift * ratio, offset);
      _viewFocalPoint.addLocal(offset);
    }
    updateCamera();
  }

  /**
   * Reset the camera frustum.  This should be called when the canvas is resized.
   *  
   * @param height the new height of the canvas
   * @param width the new width of the canvas
   */
  protected void updateCameraFrustum(final SwtCanvas canvas) {
    // Ensure that the canvas is visible before updating the frustrum.
    if (canvas == null || canvas.isDisposed() || !canvas.isVisible()) {
      return;
    }
    if (camera != null) {
      final Callable<?> exe = new Callable<Object>() {

        public Object call() {
          if (isUsePerspective()) {

            final int width = canvas.getSize().x;
            final int height = canvas.getSize().y;

            final double aspect = width / (double) height;

            if (Double.isNaN(aspect)) {
              Thread.dumpStack();
            }

            camera.setFrustumPerspective(45.0, aspect, NEAR_PLANE, FAR_PLANE);
          } else {
            setParallelFrustum();
          }
          camera.update();
          makeDirty();
          return null;
        }
      };
      _taskQueue.enqueue(exe);
    }
  }

  /**
   * 
   */
  protected void setParallelFrustum() {

    final double aspect = (double) _canvas.getSize().x / (double) _canvas.getSize().y;
    final double halfZoom = _zoomDistance * .5;
    camera.setFrustum(NEAR_PLANE, FAR_PLANE, -halfZoom * aspect, halfZoom * aspect, -halfZoom, halfZoom);

  }

  public void setViewFocus(final double x, final double y, final double z) {
    _viewFocalPoint.set(x, y, z);
    updateCamera();
  }

  public void setViewFocus(final Vector3 point) {
    setViewFocus(point.getX(), point.getY(), point.getZ());
  }

  public void setViewFocus(final int x, final int y) {
    // do a pick
    final Vector3 store = new Vector3();
    final ArrayList<PickRecord> picks = getSpatialsAt(x, y);
    if (picks.size() > 0) {
      // spin it back around...
      store.set(picks.get(0).getLocation().multiplyLocal(_rootNode.getWorldScale(Vector3.getTempInstance())));
      final double distance = store.distance(camera.getLocation(Vector3.getTempInstance()));
      _zoomDistance = distance > NEAR_PLANE ? distance < FAR_PLANE ? distance : FAR_PLANE : NEAR_PLANE;
      setViewFocus(store);
      applyZoom();
    }
  }

  public void setViewFocus(final Orientation orientation) {
    cameraSphereCoords.setY(-orientation.getAzimuthRadians() + MathUtils.HALF_PI);
    cameraSphereCoords.setZ(orientation.getElevationRadians());
    updateCamera();
  }

  /**
   * Center our view on the given spatial(s), adjusting the zoomDistance to fit the 
   * bounding sphere of the target(s) inside the view.
   * 
   * XXX: Does not quite work for parallel mode.
   * XXX: Certain views (like map view) could benefit from a bounding that ignored one of the axis)
   * 
   * @param targets
   */
  public void centerOnSpatial(final Spatial... targets) {
    if (targets == null) {
      centerOnSpatial(_rootNode);
      return;
    }
    // Make a sphere to zoom to
    if (targets.length == 1 && targets[0].getWorldBound() instanceof BoundingSphere) {
      targets[0].getWorldBound().clone(_zoomSphere);
    } else {
      // go through each target and merge into the sphere
      boolean first = true;
      _zoomSphere.setRadius(0);
      int i = 0;
      for (final Spatial spat : targets) {
        final BoundingVolume bound = spat.getWorldBound();
        if (bound != null) {
          if (first) {
            _zoomSphere.setCenter(bound.getCenter());
            first = false;
          }
          _zoomSphere.mergeLocal(bound);
        }
        i++;
      }
      // added this in order to make it work with bounding spheres with radius=0
      // we had this use case on the PointRenderer, when the PointFeature defined one single point
      if (_zoomSphere.getRadius() == 0) {
        _zoomSphere.setRadius(10);
      }
    }
    // set camera distance
    final double angle1 = Math.atan(camera.getFrustumTop() / camera.getFrustumNear());
    final double d1 = _zoomSphere.radius / MathUtils.sin(angle1);
    final double angle2 = Math.atan(camera.getFrustumRight() / camera.getFrustumNear());
    final double d2 = _zoomSphere.radius / MathUtils.sin(angle2);
    _zoomDistance = Math.max(d1, d2);
    _viewFocalPoint.set(_zoomSphere.getCenter());
    applyZoom();
  }

  public void centerOnSpatial(final Orientation orientation, final Spatial... targets) {
    setViewFocus(orientation);
    centerOnSpatial(targets);
  }

  public void setVisible(final Spatial spatial, final boolean visible) {
    if (visible) {
      spatial.setCullHint(CullHint.Dynamic);
    } else {
      spatial.setCullHint(CullHint.Always);
    }
    makeDirty();
  }

  public double getExaggeration() {
    return _zExageration;
  }

  public void setExaggeration(final double exagValue) {
    final double dif = exagValue / _zExageration;
    _zExageration = exagValue;
    _rootNode.setScale(1, 1, -1 * _zExageration);
    _viewFocalPoint.setZ(_viewFocalPoint.getZ() * dif);
    if (_pick != null && _pick.getShowMode() == ShowMode.ALWAYS) {
      final Vector3 trans = _pick.getTranslation(Vector3.getTempInstance());
      final double z = trans.getZ() * dif;
      trans.setZ(z);
      _pick.setTranslation(trans);
    }
    _rootNode.updateGeometricState(0, true);
    // force update of selection box
    _view.setSelectedSpatial(_view.getSelectedSpatial(), _view.getPickLocation());
    updateCamera();
    makeDirty();
  }

  public void setUsePerspective(final boolean usePerspective) {
    this._usePerspective = usePerspective;
    camera.setParallelProjection(!usePerspective);
    updateCameraFrustum(_canvas);
  }

  public boolean isUsePerspective() {
    return _usePerspective;
  }

  /**
   * @return the sun's azimuth in radians  (0 == north)
   */
  public double getSunAzimuth() {
    // converts from polar coords / -Z up where 0 == east
    return MathUtils.HALF_PI - _sunLocation.getY();
  }

  /**
   * @param azimuth the new azimuth for the sun, in radians  (0 == north)
   */
  public void setSunAzimuth(final double azimuth) {
    // converts to polar coords / -Z up where 0 == east
    _sunLocation.setY(MathUtils.HALF_PI - azimuth);
    updateSunPosition();
  }

  /**
   * @return the sun's elevation in radians (PI/2 == high noon)
   */
  public double getSunElevation() {
    return _sunLocation.getZ();
  }

  /**
   * @param elevation the new elevation for the sun, in radians (PI/2 == high noon)
   */
  public void setSunElevation(final double elevation) {
    _sunLocation.setZ(elevation);
    updateSunPosition();
  }

  public boolean isShowPickPos() {
    return _showPickPos;
  }

  public void setShowPickPos(final boolean show) {
    _showPickPos = show;
  }

  /**
   * @return the directional light representing the sun
   */
  public DirectionalLight getSunLight() {
    return _sunLight;
  }

  private void updateSunPosition() {
    _sunLight.setDirection(MathUtils.sphericalToCartesianZ(_sunLocation, Vector3.getTempInstance()).negateLocal());
    makeDirty();
  }

  /**
   * 
   * @return view focus location in <b>GL coordinates</b>.
   */
  public Vector3 getViewFocus() {
    return VolumeViewer.toWorldSpace(_viewFocalPoint);
  }

  public Vector3 getCameraLocation() {
    return camera.getLocation(Vector3.getTempInstance());
  }

  public void addToScene(final Spatial spatial) {
    final Callable<Void> exe = new Callable<Void>() {

      public Void call() {

        _rootNode.attachChild(spatial);
        spatial.updateRenderState();
        makeDirty();
        return null;
      }
    };
    _taskQueue.enqueue(exe);
  }

  public void removeFromScene(final Spatial spatial) {
    final Callable<Void> exe = new Callable<Void>() {

      public Void call() {

        _rootNode.detachChild(spatial);
        spatial.updateRenderState();
        makeDirty();
        return null;
      }
    };
    _taskQueue.enqueue(exe);
  }

  public void toggleWireover(final boolean enable) {
    _wireoverPass.setEnabled(enable);
  }

  public boolean isWireoverEnabled() {
    return _wireoverPass.isEnabled();
  }

  public void showWireover(final Spatial spatial) {
    final Callable<Void> exe = new Callable<Void>() {

      public Void call() {

        _wireoverPass.add(spatial);
        makeDirty();
        return null;
      }
    };
    _taskQueue.enqueue(exe);
  }

  public void removeWireover(final Spatial spatial) {
    final Callable<Void> exe = new Callable<Void>() {

      public Void call() {

        _wireoverPass.remove(spatial);
        makeDirty();
        return null;
      }
    };
    _taskQueue.enqueue(exe);
  }

  public FocusRods getFocusRods() {
    return _focRods;
  }

  public FocusRods getCursor() {
    return _cursor;
  }

  public FocusRods getPick() {
    return _pick;
  }

  public void doPickRealWorldCoordinates(final Vector3 worldCoordinates) {
    final Vector3 screenCoords = camera.getScreenCoordinates(worldCoordinates);
    doPick((int) Math.round(screenCoords.getX()), (int) Math.round(screenCoords.getY()), false, worldCoordinates);
  }

  public void doPick(final int x, final int y, final boolean showSettingsDialog, final Vector3 worldPoint) {
    final ArrayList<PickRecord> selectedSpats = getSpatialsAt(x, y);
    if (selectedSpats.size() == 0) {
      _view.setSelectedSpatial(null, null);
      _pick.setShowMode(ShowMode.NEVER);
      return;
    }

    // TODO: SHOW A SELECT LIST HERE?
    final PickRecord pr = selectedSpats.get(0);
    Vector3 worldPosition = worldPoint;
    if (worldPosition == null) {
      worldPosition = pr.getLocation();
    } else {
      worldPosition.addLocal(0, 0, pr.getLocation().getZ());
    }
    _view.setSelectedSpatial(pr.getSpatial(), worldPosition);
    if (_showPickPos) {
      if (worldPosition != null) {
        worldPosition = VolumeViewer.toWorldSpace(worldPosition);
        worldPosition.setZ(worldPosition.getZ() * _zExageration);
      }
      _pick.setTranslation(worldPosition);
      _pick.setShowMode(ShowMode.ALWAYS);
    } else {
      _pick.setShowMode(ShowMode.NEVER);
    }
    if (showSettingsDialog) {
      _view.showSettingsDialog(pr.getSpatial());
    }
  }

  /**
   * @param x
   * @param y
   * @param pickStore
   * @return
   */
  private ArrayList<PickRecord> getSpatialsAt(final int x, final int y) {
    final Vector3 pick = new Vector3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    final Ray mouseRay = new Ray();
    final Vector3 direction = Vector3.getTempInstance();
    final Vector3 origin = Vector3.getTempInstance();
    if (camera.isParallelProjection()) {
      camera.getDirection(direction);
      camera.getLocation(origin);
      final double aspect = (double) _canvas.getSize().x / (double) _canvas.getSize().y;
      final double dx = (double) x / _canvas.getSize().x;
      final double dy = (double) (_canvas.getSize().y - y) / _canvas.getSize().y;
      origin.addLocal(camera.getLeft(Vector3.getTempInstance()).multiplyLocal(-(dx - .5f) * _zoomDistance * aspect));
      origin.addLocal(camera.getUp(Vector3.getTempInstance()).multiplyLocal(-(dy - .5f) * _zoomDistance));
    } else {
      final Vector2 screenPosition = new Vector2(x, y);
      camera.getWorldCoordinates(screenPosition, 0, origin);
      camera.getWorldCoordinates(screenPosition, 0.3f, direction).subtractLocal(origin).normalizeLocal();
    }
    mouseRay.setDirection(direction);
    mouseRay.setOrigin(origin);

    // First check for a triangle collision.  If we have one, let's use the closest one to determine selected node.
    _tPick.clear();
    _tPick.setCheckDistance(true);

    PickingUtil.findPick(_rootNode, mouseRay, _tPick);
    final ArrayList<PickRecord> results = new ArrayList<PickRecord>();
    Spatial selectedNode = null;
    PickData pd = null;
    for (int i = 0; i < _tPick.getNumber(); i++) {
      pd = _tPick.getPickData(i);
      if (!Double.isInfinite(pd.getDistance())) {
        if (pd instanceof TrianglePickData) {
          final TrianglePickData tpd = (TrianglePickData) pd;
          if (tpd.getTargetMesh().getCullHint() != CullHint.Always) {
            if (pick != null) {
              pick.set(mouseRay.getDirection(Vector3.getTempInstance())).normalizeLocal()
                  .multiplyLocal(tpd.getDistance()).addLocal(mouseRay.getOrigin(Vector3.getTempInstance()));
            }
            selectedNode = tpd.getTargetMesh();
            //while (selectedNode.getParent() != null && selectedNode.getParent() != _rootNode) {
            //  selectedNode = selectedNode.getParent();
            //}
            if (selectedNode != null) {
              final PickRecord pr = new PickRecord(selectedNode, tpd.getDistance(), pick.clone().divideLocal(
                  _rootNode.getWorldScale(Vector3.getTempInstance())), PickRecord.Type.Triangle);
              if (!results.contains(pr)) {
                results.add(pr);
              }
            }
          }
        }
      }
    }

    // If no triangle was hit, use bounding volumes.
    if (selectedNode == null) {
      _bPick.clear();
      _bPick.setCheckDistance(true);
      PickingUtil.findPick(_rootNode, mouseRay, _bPick);
      for (int i = 0; i < _bPick.getNumber(); i++) {
        pd = _bPick.getPickData(i);
        if (!Double.isInfinite(pd.getDistance())) {
          if (pd.getTargetMesh().getCullHint() != CullHint.Always) {
            if (pick != null) {
              pick.set(mouseRay.getDirection(Vector3.getTempInstance())).normalizeLocal()
                  .multiplyLocal(pd.getDistance()).addLocal(mouseRay.getOrigin(Vector3.getTempInstance()));
            }
            selectedNode = pd.getTargetMesh();
            while (selectedNode.getParent() != null && selectedNode.getParent() != _rootNode) {
              selectedNode = selectedNode.getParent();
            }
            if (selectedNode != null) {
              final PickRecord pr = new PickRecord(selectedNode, pd.getDistance(), pick.clone().divideLocal(
                  _rootNode.getWorldScale(Vector3.getTempInstance())), PickRecord.Type.Bounding);
              if (!results.contains(pr)) {
                results.add(pr);
              }
            }
          }
        }
      }
    }

    return results;
  }

  public synchronized void resizeCanvas(final int width, final int height) {

    final int fWidth = width <= 0 ? 1 : width;
    final int fHeight = height <= 0 ? 1 : height;

    camera.resize(fWidth, fHeight);

    if (isUsePerspective()) {
      camera.setFrustumPerspective(45.0, fWidth / (double) fHeight, NEAR_PLANE, FAR_PLANE);
    } else {
      setParallelFrustum();
    }

    camera.update();
    makeDirty();
  }

  /**
   * @return 
   * 
   */
  public SwtCanvas getCanvas() {
    return _canvas;
  }

  public void makeDirty() {
    _dirty = true;
  }

  public GameTaskQueue getTaskQueue() {
    return _taskQueue;
  }

  /**
   * Set this canvas as the current active one.
   * @param current true if made active, false if made inactive
   */
  public void setCurrent(final boolean current) {
    _isActive = current;
    makeDirty();
  }

  public void cleanupTexture(final Texture tex) {
    final Callable<Void> exe = new Callable<Void>() {

      public Void call() {
        TextureManager.releaseTexture(tex, renderer);
        return null;
      }
    };
    _taskQueue.enqueue(exe);
  }

  public SceneText createSceneText(final String name, final String text, final SceneText.Alignment alignment) {
    final SceneText sText = new SceneText(name, text, alignment);

    // alpha blend
    if (_textBlend == null) {
      _textBlend = new BlendState();
      _textBlend.setBlendEnabled(true);
      _textBlend.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
      _textBlend.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
    }
    sText.setRenderState(_textBlend);

    // texture
    if (_textTexture == null) {
      _textTexture = new TextureState();
      final URL textureURL = ViewCanvasImplementor.class.getResource("fonttexture.tga");
      final TextureKey tkey = new TextureKey(textureURL, true, Image.Format.GuessNoCompression,
          Texture.MinificationFilter.Trilinear);
      final Texture tex = TextureManager.loadFromKey(tkey, null, null);
      tex.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
      _textTexture.setTexture(tex);
      _textTexture.setEnabled(true);
    }
    sText.setRenderState(_textTexture);

    // depth testing
    if (_textZBuff == null) {
      _textZBuff = new ZBufferState();
      _textZBuff.setWritable(false);
      _textZBuff.setEnabled(true);
      _textZBuff.setFunction(TestFunction.LessThanOrEqualTo);
    }
    sText.setRenderState(_textZBuff);

    return sText;
  }

  public int getMaxTextureSize() {
    return _maxTextureSize;
  }

  /**
   * Calculates the top/left and bottom/right corners of a rectangle described by taking a 
   * slice of the view frustum at the given distance from the camera.
   * 
   * @param zDistance 
   *            the distance from the camera (along its direction vector) to grab our plane
   * @return the top/left and bottom/right corners as a 2 value Vector3 array, or null if 
   *         the zDistance is not inside the near/far planes of the camera.
   */
  public Vector3[] getFrustumCornersAtZ(final double zDistance) {
    if (zDistance > camera.getFrustumFar() || zDistance < camera.getFrustumNear()) {
      return null;
    }

    final Vector3[] corners = new Vector3[2];

    if (_usePerspective) {
      final double heightAtZ = zDistance * camera.getFrustumTop() / camera.getFrustumNear();
      final double widthAtZ = heightAtZ * camera.getFrustumRight() / camera.getFrustumTop();
      final Vector3 camUp = camera.getUp(Vector3.getTempInstance()).multiplyLocal(heightAtZ);
      final Vector3 camLeft = camera.getLeft(Vector3.getTempInstance()).multiplyLocal(widthAtZ);

      final Vector3 center = camera.getDirection(Vector3.getTempInstance()).multiplyLocal(zDistance)
          .addLocal(camera.getLocation(Vector3.getTempInstance()));
      final Vector3 topLeft = center.add(camUp, Vector3.getTempInstance()).addLocal(camLeft);
      final Vector3 bottomRight = center.subtract(camUp, Vector3.getTempInstance()).subtractLocal(camLeft);
      topLeft.setZ(topLeft.getZ() * -1);
      bottomRight.setZ(bottomRight.getZ() * -1);
      corners[0] = topLeft;
      corners[1] = bottomRight;
    } else {
      final Vector3 camUp = camera.getUp(Vector3.getTempInstance()).multiplyLocal(camera.getFrustumTop());
      final Vector3 camLeft = camera.getLeft(Vector3.getTempInstance()).multiplyLocal(camera.getFrustumLeft());

      final Vector3 center = camera.getDirection(Vector3.getTempInstance()).multiplyLocal(zDistance)
          .addLocal(camera.getLocation(Vector3.getTempInstance()));
      final Vector3 topLeft = center.add(camUp, Vector3.getTempInstance()).addLocal(camLeft);
      final Vector3 bottomRight = center.subtract(camUp, Vector3.getTempInstance()).subtractLocal(camLeft);
      topLeft.setZ(topLeft.getZ() * -1);
      bottomRight.setZ(bottomRight.getZ() * -1);
      corners[0] = topLeft;
      corners[1] = bottomRight;
    }

    return corners;
  }

  public double getZoomFactor() {
    return _zoomFactor;
  }

  public void setZoomFactor(final double zoomFactor) {
    _zoomFactor = zoomFactor;
  }

  public void setBackground(final ColorRGBA color) {
    renderer.setBackgroundColor(color);
  }

  public VolumeMouseLook getInput() {
    return _input;
  }

  public IVolumeViewer getViewer() {
    return _view;
  }
}
