/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.core.rendering.backend.TextureHandle;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.camera.Light;
import org.geocraft.core.rendering.pick.DefaultPickEngine;
import org.geocraft.core.rendering.pick.PickResult;
import org.geocraft.core.rendering.pick.Ray;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.internal.ui.volumeviewer.input.VolumeMouseLook;
import org.geocraft.internal.ui.volumeviewer.widget.FocusRods;
import org.geocraft.internal.ui.volumeviewer.widget.FocusRods.ShowMode;
import org.geocraft.rendering.jogl.JoglRenderBackend;
import org.geocraft.rendering.jogl.JoglSceneWalker;
import org.geocraft.rendering.jogl.JoglSwtCanvas;
import org.geocraft.rendering.jogl.SwtInputAdapter;
import org.geocraft.ui.volumeviewer.IVolumeViewer;
import org.geocraft.ui.volumeviewer.VolumeViewer;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;


/**
 * Main rendering implementor for the volume viewer. Wraps the Layer 1
 * RenderBackend and JoglSwtCanvas.
 *
 * TODO: port from Ardor3D LwjglCanvasRenderer. Most rendering logic is
 * currently stubbed - this class preserves the public API that other files
 * in the volume viewer bundle depend on so the bundle compiles.
 */
public class ViewCanvasImplementor {

  private final RenderBackend _backend;
  private final JoglSwtCanvas _canvas;
  private final SwtInputAdapter _inputAdapter;
  private final IVolumeViewer _view;

  private final GroupNode _rootNode = new GroupNode("root");
  private final GroupNode _wireoverRoot = new GroupNode("wireover");
  private final GroupNode _widgetRoot = new GroupNode("widgets");

  private final Camera _camera = new Camera();
  private final Light _sunLight = new Light();
  private final Light[] _lights = new Light[] { _sunLight };

  private final TaskQueue _taskQueue = new TaskQueue();

  private final FocusRods _focRods;
  private final FocusRods _cursor;
  private final FocusRods _pick;

  private final Vector3f _viewFocalPoint = new Vector3f();
  private volatile float _cameraDistance = 5000f;
  private volatile float _cameraAzimuth = (float) Math.toRadians(225);
  private volatile float _cameraElevation = (float) Math.toRadians(30);
  private double _exaggeration = 1.0;
  private double _sunAzimuth;
  private double _sunElevation;
  private int _maxTextureSize = 4096;
  private boolean _showPickPos = false;
  private boolean _usePerspective = true;
  private Vector4f _background = new Vector4f(0, 0, 0, 1);
  private VolumeMouseLook _mouseLook;

  public ViewCanvasImplementor(final RenderBackend backend, final JoglSwtCanvas canvas,
      final SwtInputAdapter inputAdapter, final IVolumeViewer view) {
    _backend = backend;
    _canvas = canvas;
    _inputAdapter = inputAdapter;
    _view = view;

    _focRods = new FocusRods(this, false);
    _cursor = new FocusRods(this, true);
    _pick = new FocusRods(this, false);
    _cursor.setShowMode(ShowMode.NEVER);
    _pick.setShowMode(ShowMode.NEVER);
    _widgetRoot.addChild(_focRods);
    _widgetRoot.addChild(_cursor);
    _widgetRoot.addChild(_pick);

    // Default camera setup
    _camera.setPerspective((float) Math.toRadians(45), 1f, 0.1f, 100000f);
    updateCameraFromSpherical();

    setSunAzimuth(225.0 * (Math.PI / 180.0));
    setSunElevation(45.0 * (Math.PI / 180.0));

    _mouseLook = VolumeMouseLook.setupTriggers(_inputAdapter, this);
    // Also register on the NEWT window directly — NEWT captures its own
    // input events and SWT listeners on the parent don't receive them.
    canvas.addInputListener(_mouseLook);

    // Start the JOGL render loop via the canvas's RenderCallback.
    // The FPSAnimator drives rendering at 30fps on its own thread.
    // GL2 context is already current inside the callbacks.
    canvas.startAnimator(30, new JoglSwtCanvas.RenderCallback() {
      @Override
      public void onInit(GL2 gl, String rendererName) {
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glClearColor(_background.x, _background.y, _background.z, _background.w);
        System.out.println("[ViewCanvasImplementor] GL init: " + rendererName);
      }

      @Override
      public void onDisplay(GL2 gl) {
        render(gl);
      }

      @Override
      public void onReshape(GL2 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
      }
    });
  }

  public JoglSwtCanvas getCanvas() {
    return _canvas;
  }

  public IVolumeViewer getViewer() {
    return _view;
  }

  public TaskQueue getTaskQueue() {
    return _taskQueue;
  }

  public void makeDirty() {
    // With FPSAnimator, rendering is continuous — no explicit redraw needed.
    // The next display() callback will pick up any changes.
  }

  public int getMaxTextureSize() {
    return _maxTextureSize;
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

  public double getExaggeration() {
    return _exaggeration;
  }

  public void setExaggeration(final double exagValue) {
    _exaggeration = exagValue;
    // TODO: apply exaggeration to scene transform
  }

  public double getSunAzimuth() {
    return _sunAzimuth;
  }

  public void setSunAzimuth(final double azimuth) {
    _sunAzimuth = azimuth;
    updateSunLight();
  }

  public double getSunElevation() {
    return _sunElevation;
  }

  public void setSunElevation(final double elevation) {
    _sunElevation = elevation;
    updateSunLight();
  }

  private void updateSunLight() {
    final float x = (float) (Math.cos(_sunElevation) * Math.sin(_sunAzimuth));
    final float y = (float) (Math.cos(_sunElevation) * Math.cos(_sunAzimuth));
    final float z = (float) Math.sin(_sunElevation);
    _sunLight.setType(Light.Type.DIRECTIONAL);
    _sunLight.setDirection(new Vector3f(-x, -y, -z));
  }

  public void setBackground(final Vector4f color) {
    _background = new Vector4f(color);
  }

  public Vector4f getBackground() {
    return new Vector4f(_background);
  }

  public boolean isShowPickPos() {
    return _showPickPos;
  }

  public void setShowPickPos(final boolean show) {
    _showPickPos = show;
  }

  public void setUsePerspective(final boolean perspective) {
    _usePerspective = perspective;
    // TODO: reconfigure camera projection
  }

  public Vector3f getViewFocus() {
    return new Vector3f(_viewFocalPoint);
  }

  public void setViewFocus(final Orientation orientation) {
    // TODO: port orientation-based view focus
  }

  public void setViewFocus(final int screenX, final int screenY) {
    // TODO: port screen-space view focus from picking
  }

  public void addToScene(final SceneNode spatial) {
    _rootNode.addChild(spatial);
  }

  public void removeFromScene(final SceneNode spatial) {
    _rootNode.removeChild(spatial);
  }

  public void showWireover(final SceneNode spatial) {
    // TODO: port wireover pass
  }

  public void removeWireover(final SceneNode spatial) {
    // TODO: port wireover pass
  }

  public void toggleWireover(final SceneNode spatial) {
    // TODO: port wireover pass
  }

  public void centerOnSpatial(final SceneNode... targets) {
    centerOnSpatial(null, targets);
  }

  public void centerOnSpatial(final Orientation orientation, final SceneNode... targets) {
    if (targets == null || targets.length == 0) {
      return;
    }
    // Compute bounding box over all target scene graphs
    final Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    final Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
    for (final SceneNode target : targets) {
      collectBounds(target, min, max);
    }
    if (min.x > max.x) {
      return; // no geometry found
    }
    // Set focal point to bounding box center
    _viewFocalPoint.set(
        (min.x + max.x) * 0.5f,
        (min.y + max.y) * 0.5f,
        (min.z + max.z) * 0.5f);
    // Set distance to fit the bounding box (diagonal / 2 / tan(fov/2))
    final float dx = max.x - min.x;
    final float dy = max.y - min.y;
    final float dz = max.z - min.z;
    final float diagonal = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    _cameraDistance = diagonal * 1.2f; // 1.2x to give some margin
    if (_cameraDistance < 1f) {
      _cameraDistance = 1000f;
    }
    updateCameraFromSpherical();
  }

  /**
   * Recursively collect vertex bounds from LineGeometry children.
   */
  private void collectBounds(final SceneNode node, final Vector3f min, final Vector3f max) {
    if (node instanceof org.geocraft.core.rendering.scene.LineGeometry) {
      final org.geocraft.core.rendering.scene.LineGeometry line =
          (org.geocraft.core.rendering.scene.LineGeometry) node;
      final java.nio.FloatBuffer verts = line.getVertices();
      if (verts != null) {
        verts.rewind();
        for (int i = 0; i < line.getVertexCount(); i++) {
          final float x = verts.get();
          final float y = verts.get();
          final float z = verts.get();
          min.min(new Vector3f(x, y, z));
          max.max(new Vector3f(x, y, z));
        }
        verts.rewind();
      }
    }
    for (final SceneNode child : node.getChildren()) {
      collectBounds(child, min, max);
    }
  }

  public SceneText createSceneText(final String name, final String text, final SceneText.Alignment alignment) {
    return new SceneText(name, text, alignment);
  }

  public void cleanupTexture(final TextureHandle tex) {
    if (tex != null && _backend != null && _backend.getTextureLoader() != null) {
      _backend.getTextureLoader().disposeTexture(tex);
    }
  }

  public void rotateCamera(final float deltaAzimuth, final float deltaElevation) {
    _cameraAzimuth += deltaAzimuth;
    // Clamp elevation to avoid gimbal lock (stay within -89 to +89 degrees)
    _cameraElevation = Math.max((float) Math.toRadians(-89),
        Math.min((float) Math.toRadians(89), _cameraElevation + deltaElevation));
    updateCameraFromSpherical();
  }

  public void panCamera(final float deltaX, final float deltaY) {
    // Compute camera right and up vectors relative to the view direction
    final Vector3f eye = computeCameraPosition();
    final Vector3f forward = new Vector3f(_viewFocalPoint).sub(eye).normalize();
    final Vector3f worldUp = new Vector3f(0, 0, 1);
    final Vector3f right = new Vector3f(forward).cross(worldUp).normalize();
    final Vector3f up = new Vector3f(right).cross(forward).normalize();
    // Scale pan by distance so it feels consistent
    final float panScale = _cameraDistance * 0.001f;
    final Vector3f offset = new Vector3f(right).mul(-deltaX * panScale)
        .add(new Vector3f(up).mul(deltaY * panScale));
    _viewFocalPoint.add(offset);
    updateCameraFromSpherical();
  }

  public void zoomCamera(final float scalar) {
    _cameraDistance *= (1f + scalar * 0.1f);
    if (_cameraDistance < 1f) {
      _cameraDistance = 1f;
    }
    updateCameraFromSpherical();
  }

  public void zoomCamera(final float scalar, final int mouseX, final int mouseY) {
    // For now, same as basic zoom
    zoomCamera(scalar);
  }

  public void doPick(final int screenX, final int screenY, final boolean rightClick, final Object extra) {
    if (_canvas == null) return;
    final Vector2f screenPos = new Vector2f(screenX, screenY);
    final Ray ray = _camera.getPickRay(screenPos);
    final List<PickResult> results = new DefaultPickEngine().pickTriangles(_rootNode, ray);
    if (!results.isEmpty()) {
      final PickResult r = results.get(0);
      _view.setSelectedSpatial(r.getNode(), VolumeViewer.toWorldSpace(r.getWorldPosition()));
    } else {
      _view.setSelectedSpatial(null, null);
    }
  }

  public void doPickRealWorldCoordinates(final Vector3f point) {
    // TODO: port world-space picking
  }

  public Vector3f getCameraLocation() {
    return _camera.getLocation();
  }

  public Vector3f[] getFrustumCornersAtZ(final double z) {
    // TODO: port frustum corner computation
    return new Vector3f[] { new Vector3f(), new Vector3f() };
  }

  public SwtInputAdapter getInput() {
    return _inputAdapter;
  }

  public void setCurrent() {
    if (_canvas != null) {
      _canvas.makeCurrent();
    }
  }

  public void setVisible(final boolean visible) {
    // TODO: port canvas visibility toggle
  }

  /**
   * Compute the camera position from spherical coordinates around the focal point.
   * Azimuth is measured from Y-axis (north) clockwise when viewed from above.
   * Elevation is measured from the horizontal plane.
   * The "up" direction is Z-positive.
   */
  private Vector3f computeCameraPosition() {
    final float cosEl = (float) Math.cos(_cameraElevation);
    final float sinEl = (float) Math.sin(_cameraElevation);
    final float cosAz = (float) Math.cos(_cameraAzimuth);
    final float sinAz = (float) Math.sin(_cameraAzimuth);
    // Spherical to Cartesian (Z-up convention)
    final float x = _viewFocalPoint.x + _cameraDistance * cosEl * sinAz;
    final float y = _viewFocalPoint.y + _cameraDistance * cosEl * cosAz;
    final float z = _viewFocalPoint.z + _cameraDistance * sinEl;
    return new Vector3f(x, y, z);
  }

  /**
   * Recompute camera location and orientation from the current spherical
   * coordinate state (_cameraDistance, _cameraAzimuth, _cameraElevation)
   * orbiting around _viewFocalPoint.
   */
  private void updateCameraFromSpherical() {
    final Vector3f eye = computeCameraPosition();
    _camera.setLocation(eye);
    _camera.lookAt(new Vector3f(_viewFocalPoint), new Vector3f(0, 0, 1));
  }

  /**
   * Render using a GL2 context that is already current (called from GLEventListener.display).
   */
  private static boolean _debugOnce = true;

  private void render(GL2 gl) {
    drainTaskQueue();
    int w = _canvas.getWidth();
    int h = _canvas.getHeight();
    if (w <= 0 || h <= 0) return;
    _camera.setViewport(w, h);

    if (_debugOnce) {
      System.out.println("[render] rootNode children: " + _rootNode.getChildren().size());
      System.out.println("[render] camera loc: " + _camera.getLocation()
          + " focal: " + _viewFocalPoint + " dist: " + _cameraDistance);
      System.out.println("[render] viewport: " + w + "x" + h);
      System.out.println("[render] backend: " + _backend);
      _debugOnce = false;
    }

    // Render the scene directly via a JoglRenderBackend, or manually if
    // the backend OSGi service wasn't available (Eclipse PDE dev mode).
    if (_backend instanceof JoglRenderBackend) {
      ((JoglRenderBackend) _backend).renderPass(gl, _rootNode, _camera, _lights, null);
    } else {
      // Fallback: render directly without the backend service
      gl.glViewport(0, 0, w, h);
      gl.glClear(com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT | com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT);
      float[] m = new float[16];
      gl.glMatrixMode(0x1701 /* GL_PROJECTION */);
      _camera.getProjectionMatrix().get(m);
      gl.glLoadMatrixf(m, 0);
      gl.glMatrixMode(0x1700 /* GL_MODELVIEW */);
      _camera.getViewMatrix().get(m);
      gl.glLoadMatrixf(m, 0);
      new JoglSceneWalker().walk(gl, _rootNode, null);
    }
  }

  /** Legacy render for non-NEWT paths. */
  public void render() {
    // With NEWT+FPSAnimator, rendering is driven by the GLEventListener.
    // This method is kept for API compatibility.
  }

  private void drainTaskQueue() {
    Callable<?> task;
    while ((task = _taskQueue.poll()) != null) {
      try {
        task.call();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  /**
   * Simple Callable task queue stand-in for Ardor3D GameTaskQueue.
   */
  public static final class TaskQueue {
    private final ConcurrentLinkedQueue<Callable<?>> _queue = new ConcurrentLinkedQueue<>();

    public void enqueue(final Callable<?> exe) {
      if (exe != null) _queue.offer(exe);
    }

    public Callable<?> poll() {
      return _queue.poll();
    }
  }
}
