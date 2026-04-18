/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.volumeviewer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.CursorLocation;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.model.specification.EntityUniqueIdSpecification;
import org.geocraft.core.rendering.backend.TextureHandle;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.core.service.message.Topic;
import org.geocraft.internal.ui.volumeviewer.canvas.Orientation;
import org.geocraft.internal.ui.volumeviewer.canvas.SelectionRenderer;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasFactory;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
import org.geocraft.internal.ui.volumeviewer.dialog.Cursor;
import org.geocraft.internal.ui.volumeviewer.widget.FocusRods.ShowMode;
import org.geocraft.ui.viewer.AbstractDataViewer;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.action.HelpAction;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.light.LightSourceModel;
import org.geocraft.ui.viewer.toolbar.SharedToolBar;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;
import org.geocraft.ui.viewer.tree.ReadoutPanel;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;
import org.joml.Vector3f;
import org.joml.Vector4f;


public class VolumeViewer extends AbstractDataViewer implements IVolumeViewer, IViewer, IMessageSubscriber {

  /** The error logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(VolumeViewer.class);

  private static LightSourceModel _lightSourceModel = new LightSourceModel();

  private IPreferenceStore _store;

  private final IPropertyChangeListener _viewerPropertyListener;

  private final Set<IPropertyChangeListener> _listeners = new HashSet<IPropertyChangeListener>();

  /** The cursor which is used for broadcast and receive the cursor locations. */
  private Cursor _cursor;

  /** The cursor which is used for receive the cursor selection locations. */
  private Cursor _pickCursor;

  /** The text where the messages are displayed. */
  private final Text _messageText;

  private ViewCanvasImplementor _viewCanvasImpl;

  /** The time domain scene-graph node. */
  private GroupNode _timeDomainNode;

  /** The depth domain scene-graph node. */
  private GroupNode _depthDomainNode;

  private final boolean _broadcastFlag = false;

  private final boolean _receptionFlag = false;

  /** The selected scene node. */
  private SceneNode _selectedNode;

  /** The current pick location. */
  private Vector3f _pickLocation;

  /** The selection outline color. */
  private Vector4f _selectionColor;

  /** The 3D viewer's model of display properties. */
  private VolumeViewerModel _model;

  /** A scene node to entity renderer mapping. */
  private Map<SceneNode, VolumeViewRenderer> _nodeToRenderer;

  private final IWorkbenchPartSite _site;

  private VolumeViewToolBar _toolbar;

  /** The number of opened 3d viewers. */
  private static int _openedViewers;

  public VolumeViewer(final Composite parent, final IWorkbenchPartSite site) {
    super(parent, false, false, true);
    _site = site;

    _viewCanvasImpl.setViewFocus(Orientation.MAP_VIEW);

    _messageText = new Text(this, SWT.SINGLE | SWT.READ_ONLY);
    final GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = false;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.verticalAlignment = SWT.FILL;
    layoutData.horizontalSpan = 2;
    _messageText.setLayoutData(layoutData);
    _messageText.setForeground(new Color(null, 0, 0, 200));

    // start a thread that will issue a repaint request every 1 second
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (!VolumeViewer.this.isDisposed()) {
          try {
            Thread.sleep(1000);
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
          VolumeViewer.this.makeDirty();
        }
      }
    }, "3D Viewer repaint thread").start();

    final String currentCenter = _store.getString(VolumeViewerPreferencePage.CURRENT_CENTER);
    final String projectionMode = _store.getString(VolumeViewerPreferencePage.PROJECTION_MODE);
    final RGB selectionColor = PreferenceConverter.getColor(_store, VolumeViewerPreferencePage.SELECTION_COLOR);
    setPreferences(currentCenter, projectionMode, selectionColor);

    _viewerPropertyListener = new VolumeViewerPropertyListener(this, _store);
    _store.addPropertyChangeListener(_viewerPropertyListener);

    _openedViewers++;
  }

  @Override
  protected void hookContextMenu() {
    // TODO: Implement this.
  }

  private Composite _canvasComposite;
  private boolean _canvasInitialized = false;

  @Override
  protected void initializeCanvas(final Composite canvasComposite) {
    _model = new VolumeViewerModel();
    _store = VolumeViewerPreferencePage.PREFERENCE_STORE;
    _nodeToRenderer = new HashMap<SceneNode, VolumeViewRenderer>();
    // Defer GL canvas creation until data is actually added.
    // This prevents the NEWT window from taking over the workbench
    // when Eclipse restores a saved layout with an empty volume view.
    _canvasComposite = canvasComposite;
  }

  /**
   * Create the GL canvas on first use (when data is added to the viewer).
   * Called lazily from addObjects().
   */
  private synchronized void ensureCanvasCreated() {
    if (_canvasInitialized || _canvasComposite == null) return;
    _canvasInitialized = true;
    final int depthBits = _store.getInt(VolumeViewerPreferencePage.DEPTH_BITS);
    _viewCanvasImpl = ViewCanvasFactory.makeCanvas(_canvasComposite, this, depthBits);
    _timeDomainNode = new GroupNode(TIME_DOMAIN);
    _depthDomainNode = new GroupNode(DEPTH_DOMAIN);
    _viewCanvasImpl.addToScene(_timeDomainNode);
    _viewCanvasImpl.addToScene(_depthDomainNode);
    _canvasComposite.layout(true);
  }

  @Override
  protected void initializeToolBars() {
    final SharedToolBar sharedToolbar = getSharedToolBar();
    sharedToolbar.addPushButton(new HelpAction("org.geocraft.ui.mapviewer.MapPlot"));

    final SimpleToolBar toolbar = addCustomToolBar();
    _toolbar = new VolumeViewToolBar(toolbar, this, _viewCanvasImpl);

    final IModelListener lightSourceListener = new IModelListener() {
      @Override
      public void propertyChanged(final String key) {
        Display.getDefault().asyncExec(new Runnable() {
          @Override
          public void run() {
            _viewCanvasImpl.setSunAzimuth(_lightSourceModel.getAzimuth() * (Math.PI / 180.0));
            _viewCanvasImpl.setSunElevation(_lightSourceModel.getElevation() * (Math.PI / 180.0));
          }
        });
      }
    };
    _lightSourceModel.addListener(lightSourceListener);
  }

  @Override
  public void dispose() {
    super.dispose();
    _store.removePropertyChangeListener(_viewerPropertyListener);
    _openedViewers--;
  }

  @Override
  public void cursorUpdated(final double x, final double y, final boolean broadcast) {
    final ReadoutPanel readoutPanel = getReadoutPanel();
    if (readoutPanel != null && readoutPanel.isVisible()) {
      readoutPanel.update(getViewReadoutInfo(x, y));
      for (final IRenderer renderer : getRenderers()) {
        if (renderer.showReadoutInfo()) {
          final ReadoutInfo info = renderer.getReadoutInfo(x, y);
          readoutPanel.update(info);
        }
      }
      readoutPanel.updateForm();
    }
  }

  @Override
  protected ReadoutInfo getViewReadoutInfo(final double x, final double y) {
    return new ReadoutInfo("");
  }

  public IRenderer[] getRenderers() {
    return getVolumeViewRenderers();
  }

  public void home() {
    _viewCanvasImpl.setViewFocus(Orientation.MAP_VIEW);
    final SceneNode[] spatial = null;
    _viewCanvasImpl.centerOnSpatial(Orientation.MAP_VIEW, spatial);
  }

  public void pan(final boolean enabled) {
    // Not implemented for 3D viewer.
  }

  public void print() {
    // Not implemented for 3D viewer.
  }

  @Override
  public void addObjects(final Object[] objects) {
    addObjects(false, objects);
  }

  @Override
  public void addObjects(final boolean block, final Object... objects) {
    ensureCanvasCreated();
    final Shell shell = getShell();
    final BackgroundTask task = new BackgroundTask() {

      @Override
      public Void compute(final ILogger logger, final IProgressMonitor monitor) {
        final List<SceneNode> allTimeSpatials = new ArrayList<SceneNode>();
        final List<SceneNode> allDepthSpatials = new ArrayList<SceneNode>();
        monitor.beginTask("Adding objects to the 3D viewer", 1 + objects.length);
        try {
          Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
              for (final Object obj : objects) {
                final Object object = obj;
                final List<IConfigurationElement> configsSingle = VolumeViewRendererRegistry.findRenderer(shell, object);
                for (final IConfigurationElement config : configsSingle) {
                  try {
                    boolean rendererExists = false;
                    final VolumeViewRenderer newRenderer = VolumeViewRendererRegistry.createRenderer(config);
                    for (final VolumeViewRenderer renderer : getVolumeViewRenderers()) {
                      if (renderer.getClass().equals(newRenderer.getClass())) {
                        if (renderer.getRenderedObjects()[0].equals(object)) {
                          rendererExists = true;
                          break;
                        }
                      }
                    }
                    if (!rendererExists) {
                      if (Entity.class.isAssignableFrom(object.getClass())) {
                        final Entity entity = (Entity) object;
                        try {
                          entity.load();
                        } catch (final Exception ex) {
                          ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.getMessage(), ex);
                        }
                      }
                      newRenderer.setData(shell, VolumeViewer.this, new Object[] { object });
                      final SceneNode[] timeSpatials = newRenderer.getSpatials(Domain.TIME);
                      final SceneNode[] depthSpatials = newRenderer.getSpatials(Domain.DISTANCE);
                      for (final SceneNode spatial : timeSpatials) {
                        allTimeSpatials.add(spatial);
                      }
                      for (final SceneNode spatial : depthSpatials) {
                        allDepthSpatials.add(spatial);
                      }
                    }
                  } catch (final Exception ex) {
                    ServiceProvider.getLoggingService().getLogger(getClass())
                        .error("Error creating renderer for " + object.toString(), ex);
                  }
                }
                monitor.worked(1);
                if (monitor.isCanceled()) {
                  break;
                }
              }

              final int numTimeSpatials = allTimeSpatials.size();
              final int numDepthSpatials = allDepthSpatials.size();
              if (numTimeSpatials > 0 && numDepthSpatials == 0) {
                VolumeViewer.this._toolbar.setZDomain(VolumeViewZDomain.TIME);
              } else if (numDepthSpatials > 0 && numTimeSpatials == 0) {
                VolumeViewer.this._toolbar.setZDomain(VolumeViewZDomain.DEPTH);
              }
              switch (VolumeViewer.this._model.getZDomain()) {
                case TIME:
                  if (numTimeSpatials > 0) {
                    VolumeViewer.this.centerOnSpatial(allTimeSpatials.toArray(new SceneNode[0]));
                  }
                  break;
                case DEPTH:
                  if (numDepthSpatials > 0) {
                    VolumeViewer.this.centerOnSpatial(allDepthSpatials.toArray(new SceneNode[0]));
                  }
                  break;
              }
            }
          });
        } finally {
          monitor.done();
        }
        Display.getDefault().syncExec(new Runnable() {
          @Override
          public void run() {
            for (final VolumeViewRenderer renderer : getVolumeViewRenderers()) {
              // redraw renderer if needed
            }
          }
        });
        monitor.worked(1);
        return null;
      }
    };

    if (block) {
      TaskRunner.runTask(task, "Add entities to the 3D viewer");
    } else {
      new Thread(new Runnable() {
        @Override
        public void run() {
          TaskRunner.runTask(task, "Add entities to the 3D viewer");
        }
      }).start();
    }
  }

  /**
   * Returns an array of the current 3D view renderers.
   */
  private VolumeViewRenderer[] getVolumeViewRenderers() {
    final List<VolumeViewRenderer> renderers = new ArrayList<VolumeViewRenderer>();
    for (final IViewLayer viewLayer : getLayerModel().getLayers()) {
      if (viewLayer instanceof RendererViewLayer) {
        final RendererViewLayer rendererLayer = (RendererViewLayer) viewLayer;
        renderers.add(rendererLayer.getRenderer());
      }
    }
    return renderers.toArray(new VolumeViewRenderer[0]);
  }

  public RGB getBackgroundViewColor() {
    if (_model == null) {
      return new RGB(0, 0, 0);
    }
    return _model.getBackgroundColor();
  }

  public void setBackgroundViewColor(final RGB color) {
    _model.setBackgroundColor(color);
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        final Vector4f colorRGBA = VolumeViewerHelper.rgbToColorRGBA(color, 1);
        try {
          _viewCanvasImpl.setBackground(colorRGBA);
        } catch (final NullPointerException npe) {
          LOGGER.warn("Cannot set background color of 3D viewer");
        }
      }
    });
  }

  public void setCursorStyle(final int cursorStyle) {
    // TODO Auto-generated method stub
  }

  public void zoomIn() {
    // TODO Auto-generated method stub
  }

  public void zoomOut() {
    // TODO Auto-generated method stub
  }

  public void zoomWindow(final boolean enabled) {
    // Not implemented for 3D viewer.
  }

  @SuppressWarnings("unchecked")
  public void messageReceived(final String topic, final Object message) {
    if (topic.equals(Topic.REPOSITORY_OBJECTS_REMOVED)) {
      final HashMap<String, Object> deletedItems = (HashMap<String, Object>) message;
      removeUnloadedEntities(deletedItems);
    }
    if (_receptionFlag) {
      if (topic.equals(Topic.CURSOR_LOCATION) || topic.equals(Topic.CURSOR_SELECTION_LOCATION)) {
        final CursorLocation cl = (CursorLocation) message;
        Point3d p = null;
        if (!cl.isSender("CursorDialog")) {
          p = cl.getLocation().getPoint();
          if (topic.equals(Topic.CURSOR_LOCATION)
              && (_cursor == null || _cursor.getCursor().getShowMode() == ShowMode.NEVER)
              || topic.equals(Topic.CURSOR_SELECTION_LOCATION)
                  && (_pickCursor == null || _pickCursor.getCursor().getShowMode() == ShowMode.NEVER)) {
            addCursor(false, topic.equals(Topic.CURSOR_SELECTION_LOCATION));
          }
          _viewCanvasImpl.setShowPickPos(topic.equals(Topic.CURSOR_SELECTION_LOCATION));
          if (topic.equals(Topic.CURSOR_LOCATION)) {
            _cursor.setPosition((float) p.getX(), (float) p.getY(), (float) p.getZ(),
                _viewCanvasImpl.getExaggeration());
          } else if (topic.equals(Topic.CURSOR_SELECTION_LOCATION)) {
            _pickCursor.setPosition((float) p.getX(), (float) p.getY(), (float) p.getZ(),
                _viewCanvasImpl.getExaggeration());
          }
          makeDirty();
        }
      }
    }
  }

  /**
   * Set the viewer message text.
   */
  @Override
  public void setMessageText(final String message) {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (!_messageText.isDisposed()) {
          _messageText.setText(message);
        }
      }
    });
  }

  /**
   * Show the settings dialog corresponding to the provided spatial.
   */
  @Override
  public void showSettingsDialog(final SceneNode spatial) {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (spatial != null) {
          final VolumeViewRenderer renderer = _nodeToRenderer.get(spatial);
          if (renderer != null) {
            renderer.editSettings();
          }
        }
      }
    });
  }

  @Override
  public void setPreferences(final String currentCenter, final String projectionModeStr, final RGB selectionColor) {
    final ShowMode showMode = ShowMode.getModeForId(currentCenter);
    _viewCanvasImpl.getFocusRods().setShowMode(showMode);
    final ProjectionMode projectionMode = ProjectionMode.lookup(projectionModeStr);
    if (projectionMode != null) {
      _viewCanvasImpl.setUsePerspective(projectionMode == ProjectionMode.PERSPECTIVE);
    }
    _selectionColor = VolumeViewerHelper.rgbToColorRGBA(selectionColor, 1);
  }

  @Override
  public void addPropertyChangeListener(final IPropertyChangeListener listener) {
    _store.addPropertyChangeListener(listener);
    _listeners.add(listener);
  }

  @Override
  public void addToScene(final SceneNode spatial) {
    _viewCanvasImpl.addToScene(spatial);
  }

  public void removeFromScene(final SceneNode spatial) {
    _viewCanvasImpl.removeFromScene(spatial);
  }

  public ViewCanvasImplementor getCanvasImplementor() {
    return _viewCanvasImpl;
  }

  public void addToScene(final Domain domain, final SceneNode spatial) {
    if (domain == Domain.TIME) {
      _timeDomainNode.addChild(spatial);
    } else if (domain == Domain.DISTANCE) {
      _depthDomainNode.addChild(spatial);
    }
  }

  public void removeFromScene(final Domain domain, final SceneNode spatial) {
    if (domain == Domain.TIME) {
      _timeDomainNode.removeChild(spatial);
    } else if (domain == Domain.DISTANCE) {
      _depthDomainNode.removeChild(spatial);
    }
  }

  public void centerOnSpatial(final Orientation orientation, final SceneNode... targets) {
    _viewCanvasImpl.centerOnSpatial(orientation, targets);
  }

  @Override
  public void centerOnSpatial(final SceneNode... targets) {
    final Callable<Void> exe = new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        _viewCanvasImpl.centerOnSpatial(targets);
        return null;
      }
    };
    enqueueGLTask(exe);
  }

  public void centerOnSpatial(final SceneNode spatial) {
    if (spatial == null) {
      return;
    }
    final Callable<Void> exe = new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        final SceneNode[] spatials = new SceneNode[] { spatial };
        VolumeViewer.this.centerOnSpatial(spatials);
        return null;
      }
    };
    enqueueGLTask(exe);
  }

  @Override
  public void enqueueGLTask(final Callable<?> exe) {
    _viewCanvasImpl.getTaskQueue().enqueue(exe);
  }

  @Override
  public void cleanupTexture(final TextureHandle tex) {
    _viewCanvasImpl.cleanupTexture(tex);
  }

  @Override
  public SceneText createSceneText(final String name, final String text, final SceneText.Alignment alignment) {
    return _viewCanvasImpl.createSceneText(name, text, alignment);
  }

  @Override
  public int getMaximumTextureSize() {
    return _viewCanvasImpl.getMaxTextureSize();
  }

  @Override
  public IWorkbenchPartSite getSite() {
    return _site;
  }

  @Override
  public void removePropertyChangeListener(final IPropertyChangeListener listener) {
    _store.removePropertyChangeListener(listener);
    _listeners.remove(listener);
  }

  public Domain getCurrentDomain() {
    return _model.getZDomain().getDomain();
  }

  @Override
  public void setCurrentDomain(final Domain domain) {
    _model.setZDomain(VolumeViewZDomain.lookup(domain));
    if (domain == Domain.TIME) {
      _viewCanvasImpl.removeFromScene(_depthDomainNode);
      _viewCanvasImpl.addToScene(_timeDomainNode);
    } else if (domain == Domain.DISTANCE) {
      _viewCanvasImpl.removeFromScene(_timeDomainNode);
      _viewCanvasImpl.addToScene(_depthDomainNode);
    }
    setMessageText("Domain changed to " + domain);
    makeDirty();
  }

  @Override
  public void setSelectedRenderer(final Object object) {
    final VolumeViewRenderer renderer = (VolumeViewRenderer) object;
    if (renderer == null) {
      return;
    }

    final Vector3f pickLoc = getPickLocation();
    final ReadoutInfo[] infos = renderer.getReadoutData(pickLoc);
    if (infos != null) {
      for (final ReadoutInfo info : infos) {
        if (info != null) {
          _readoutPanel.update(info);
        }
      }
    }
    _readoutPanel.updateForm();

    if (_viewCanvasImpl.isShowPickPos()) {
      setMessageText(renderer.getShortMessage());
      _mainFolder.setSelection(1);
    } else {
      setMessageText("");
    }
  }

  @Override
  public void showWireover(final SceneNode spatial) {
    if (_viewCanvasImpl != null) {
      _viewCanvasImpl.showWireover(spatial);
    }
  }

  @Override
  public void removeWireover(final SceneNode spatial) {
    if (_viewCanvasImpl != null) {
      _viewCanvasImpl.removeWireover(spatial);
    }
  }

  @Override
  public void makeDirty() {
    if (_viewCanvasImpl != null) {
      _viewCanvasImpl.makeDirty();
    }
  }

  /**
   * Add the cursor to the canvas.
   */
  public void addCursor(final boolean showDialog, final boolean pickCursor) {
    if (_cursor == null && !pickCursor) {
      _cursor = new Cursor(0, 0, 0, ApplicationPreferences.getInstance().getDepthCoordinateSystem(),
          _viewCanvasImpl.getCursor());
    }
    Cursor currentCursor = _cursor;
    if (_pickCursor == null && pickCursor) {
      _pickCursor = new Cursor(0, 0, 0, ApplicationPreferences.getInstance().getDepthCoordinateSystem(),
          _viewCanvasImpl.getPick());
    }
    if (pickCursor) {
      currentCursor = _pickCursor;
    }
    final Vector3f center = new Vector3f(_viewCanvasImpl.getViewFocus());
    center.z = (float) (center.z / _viewCanvasImpl.getExaggeration());
    currentCursor.setPosition(center.x, center.y, center.z, _viewCanvasImpl.getExaggeration());
    final VolumeViewZDomain currentDomain = _model.getZDomain();
    if (currentDomain == VolumeViewZDomain.TIME) {
      currentCursor.setCoordinateSystem(ApplicationPreferences.getInstance().getTimeCoordinateSystem());
    } else if (currentDomain == VolumeViewZDomain.DEPTH) {
      currentCursor.setCoordinateSystem(ApplicationPreferences.getInstance().getDepthCoordinateSystem());
    } else {
      throw new RuntimeException("Invalid domain: " + currentDomain);
    }
    currentCursor.getCursor().setShowMode(ShowMode.ALWAYS);
  }

  private void removeUnloadedEntities(final HashMap<String, Object> deletedItems) {
    removeObjects(deletedItems.values().toArray());
  }

  public Model getViewerModel() {
    return _model;
  }

  public void updateFromModel() {
    _toolbar.setZDomain(_model.getZDomain());
    _toolbar.setZScaling(_model.getZScaling());
    _toolbar.setProjection(_model.getProjection());
    _toolbar.setShowPickLocation(_model.getShowPickLocation());
    final List<SceneNode> spatials = new ArrayList<SceneNode>();
    for (final VolumeViewRenderer renderer : getVolumeViewRenderers()) {
      switch (_model.getZDomain()) {
        case TIME:
          for (final SceneNode spatial : renderer.getSpatials(Domain.TIME)) {
            spatials.add(spatial);
          }
          break;
        case DEPTH:
          for (final SceneNode spatial : renderer.getSpatials(Domain.DISTANCE)) {
            spatials.add(spatial);
          }
          break;
      }
    }
    centerOnSpatial(spatials.toArray(new SceneNode[0]));
    makeDirty();
  }

  public void addRenderer(final String klass, final Map<String, String> props, final String uniqueId) {
    VolumeViewRenderer renderer = null;
    try {
      renderer = VolumeViewRendererRegistry.findRenderer(klass);
    } catch (final Exception ex) {
      LOGGER.error("Cannot find renderer " + klass);
      LOGGER.error("  message: " + ex.getMessage());
    }
    if (renderer == null) {
      return;
    }

    renderer.setViewer(VolumeViewer.this);
    final Model model = renderer.getSettingsModel();
    if (model != null) {
      final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      final Map<String, Object> results = ServiceProvider.getRepository()
          .get(new EntityUniqueIdSpecification(uniqueId));
      final Object[] objects = results.values().toArray();
      renderer.setData(shell, VolumeViewer.this, objects, false);
      model.unpickle(props);
      renderer.refresh();
    }
  }

  @Override
  public Vector3f getPickLocation() {
    return _pickLocation;
  }

  @Override
  public SceneNode getSelectedSpatial() {
    return _selectedNode;
  }

  /**
   * Set the selected spatial, at the given pick location.
   */
  @Override
  public final synchronized void setSelectedSpatial(final SceneNode selected, final Vector3f pickLoc) {
    _selectedNode = selected;
    _pickLocation = pickLoc;
    for (final VolumeViewRenderer renderer : _nodeToRenderer.values()) {
      renderer.clearOutline();
    }
    makeDirty();
    final VolumeViewRenderer renderer = _nodeToRenderer.get(_selectedNode);
    if (_selectedNode == null || renderer == null) {
      setSelectedRenderer(null);
      return;
    }
    setSelectedRenderer(renderer);
    renderer.triggerClickAction(pickLoc, selected);

    if (!renderer.renderOutline()) {
      SelectionRenderer.updateOutline(_selectedNode, _selectionColor);
    }
  }

  @Override
  public void mapSpatial(final SceneNode spatial, final Object renderer) {
    _nodeToRenderer.put(spatial, (VolumeViewRenderer) renderer);
  }

  public void unmapSpatial(final SceneNode spatial) {
    _nodeToRenderer.remove(spatial);
  }

  public double getExaggeration() {
    return _viewCanvasImpl.getExaggeration();
  }

  public void setExaggeration(final double exaggeration) {
    _model.setZScaling(exaggeration);
    _viewCanvasImpl.setExaggeration(exaggeration);
    makeDirty();
  }

  public LightSourceModel getLightSourceModel() {
    return _lightSourceModel;
  }

  @Override
  protected void checkAspectRatio() {
    // Nothing to do.
  }

  @Override
  protected IRenderer createRenderer(final IConfigurationElement configElement) throws Exception {
    return VolumeViewRendererRegistry.createRenderer(configElement);
  }

  @Override
  protected List<IConfigurationElement> findRenderer(final Shell shell, final Object object) {
    return VolumeViewRendererRegistry.findRenderer(shell, object);
  }

  @Override
  protected String[] getMessageSubscriptionTopics() {
    return new String[] { Topic.CURSOR_LOCATION, Topic.CURSOR_SELECTION_LOCATION, Topic.DATA_SELECTION,
        Topic.DATA_DESELECTION, Topic.REPOSITORY_OBJECTS_REMOVED };
  }

  @Override
  protected void initializeViewerSpecificFeatures() {
  }

  @Override
  protected void setRendererData(final IRenderer renderer, final Shell shell, final Object[] objects,
      final boolean autoUpdate) {
    ((VolumeViewRenderer) renderer).setData(shell, VolumeViewer.this, objects, autoUpdate);
  }

  @Override
  protected void updateAll() {
    makeDirty();
  }

  public static int getOpenedViewers() {
    return _openedViewers;
  }

  @Override
  public SceneNode[] getNodes() {
    return _nodeToRenderer.keySet().toArray(new SceneNode[0]);
  }

  public VolumeViewRenderer getRendererForNode(final SceneNode node) {
    return _nodeToRenderer.get(node);
  }

  public void removeSelectedNodes() {
    // TODO: port selected-node removal
  }

  public void clearAll() {
    _nodeToRenderer.clear();
    _selectedNode = null;
    refreshTree();
    makeDirty();
  }

  public void refreshTree() {
    // TODO: refresh the entities tree
  }

  public void removeEntity(final Entity entity) {
    // TODO: port entity removal
  }

  /**
   * Convert an OpenGL coordinate (Z up) to World coordinate (positive Z is down.)
   */
  public static Vector3f toWorldSpace(final Vector3f glPoint) {
    return new Vector3f(glPoint.x, glPoint.y, -glPoint.z);
  }

  public static Vector3f toWorldSpace(final double x, final double y, final double z) {
    return new Vector3f((float) x, (float) y, (float) -z);
  }
}
