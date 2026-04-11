/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    setSunAzimuth(225.0 * (Math.PI / 180.0));
    setSunElevation(45.0 * (Math.PI / 180.0));

    _mouseLook = VolumeMouseLook.setupTriggers(_inputAdapter, this);
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
    // TODO: mark canvas for redraw
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
    // TODO: compute bounding volume over targets and move camera
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
    // TODO: port camera rotation
  }

  public void panCamera(final float deltaX, final float deltaY) {
    // TODO: port camera panning
  }

  public void zoomCamera(final float scalar) {
    // TODO: port camera zoom
  }

  public void zoomCamera(final float scalar, final int mouseX, final int mouseY) {
    // TODO: port camera zoom-to-point
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
   * Render the scene using the Layer 1 backend.
   * TODO: wire this to a redraw event loop.
   */
  public void render() {
    if (_backend == null || _canvas == null) return;
    drainTaskQueue();
    _camera.setViewport(_canvas.getWidth(), _canvas.getHeight());
    _backend.renderPass(_rootNode, _camera, _lights);
    _backend.renderPass(_wireoverRoot, _camera, _lights);
    _backend.renderPass(_widgetRoot, _camera, _lights);
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
