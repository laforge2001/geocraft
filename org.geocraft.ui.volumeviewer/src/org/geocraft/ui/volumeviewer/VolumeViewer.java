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

import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;


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
  private Node _timeDomainNode;

  /** The depth domain scene-graph node. */
  private Node _depthDomainNode;

  private final boolean _broadcastFlag = false;

  private final boolean _receptionFlag = false;

  /** The selected spatial node. */
  private Spatial _selectedNode;

  /** The current pick location. */
  private Vector3 _pickLocation;

  /** The selection outline color. */
  private ColorRGBA _selectionColor;

  /** The 3D viewer's model of display properties. */
  private VolumeViewerModel _model;

  /** A spatial node to entity renderer mapping. */
  private Map<Spatial, VolumeViewRenderer> _nodeToRenderer;

  private final IWorkbenchPartSite _site;

  private VolumeViewToolBar _toolbar;

  /** The number of opened 3d viewers. */
  private static int _openedViewers;

  public VolumeViewer(final Composite parent, final IWorkbenchPartSite site) {
    super(parent, false, false, true);
    _site = site;

    /// TODO: _toolbar.setCanvasRegistry(_registry);
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

    // start a thread that will issue a repaint request every 1 second, if any nodes in the viewer
    new Thread(new Runnable() {

      public void run() {
        while (!VolumeViewer.this.isDisposed()) {
          try {
            Thread.sleep(1000);
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
          // TODO:
          //if (_registry != null && _registry.getNodes().length > 0) {
          VolumeViewer.this.makeDirty();
          //}
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

  @Override
  protected void initializeCanvas(final Composite canvasComposite) {
    _model = new VolumeViewerModel();

    // Send canvas composite off for canvas creation.
    _store = VolumeViewerPreferencePage.PREFERENCE_STORE;
    // _store.setDefault(ViewerPreferencePage.CURRENT_CENTER_KEY, ShowMode.ON_INTERACT.getId());
    // _store.setDefault(ViewerPreferencePage.PROJECTION_MODE_KEY, "perspective");
    // _store.setDefault(ViewerPreferencePage.DEPTH_BITS_KEY, 24);
    // _store.setDefault(ViewerPreferencePage.SHOW_LABELS_KEY, true);
    //  PreferenceConverter.setDefault(_store, ViewerPreferencePage.SELECTION_COLOR_KEY, VolumeViewerHelper
    // colorRGBAToRGB(ColorRGBA.CYAN);
    _nodeToRenderer = new HashMap<Spatial, VolumeViewRenderer>();

    final int depthBits = _store.getInt(VolumeViewerPreferencePage.DEPTH_BITS);
    _viewCanvasImpl = ViewCanvasFactory.makeCanvas(canvasComposite, this, depthBits);
    _timeDomainNode = new Node(TIME_DOMAIN);
    _depthDomainNode = new Node(DEPTH_DOMAIN);
    _viewCanvasImpl.addToScene(_timeDomainNode);
  }

  @Override
  protected void initializeToolBars() {
    // Add the help action to the shared toolbar.
    final SharedToolBar sharedToolbar = getSharedToolBar();
    sharedToolbar.addPushButton(new HelpAction("org.geocraft.ui.mapviewer.MapPlot"));

    // Create a custom toolbar just for the 3D viewer.
    final SimpleToolBar toolbar = addCustomToolBar();
    _toolbar = new VolumeViewToolBar(toolbar, this, _viewCanvasImpl);

    // Create a listener for the light source model shared across all map viewers.
    final IModelListener lightSourceListener = new IModelListener() {

      public void propertyChanged(final String key) {
        Display.getDefault().asyncExec(new Runnable() {

          public void run() {
            // If the light source model has changed, and the renderer is
            // using shaded relief, then trigger a redraw.
            _viewCanvasImpl.setSunAzimuth(_lightSourceModel.getAzimuth() * MathUtils.DEG_TO_RAD);
            _viewCanvasImpl.setSunElevation(_lightSourceModel.getElevation() * MathUtils.DEG_TO_RAD);
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

    // If the panel is not visible we don't need to update it. 
    final ReadoutPanel readoutPanel = getReadoutPanel();
    if (readoutPanel != null && readoutPanel.isVisible()) {

      // If the panel is not visible we don't need to update it. 
      readoutPanel.update(getViewReadoutInfo(x, y));

      for (final IRenderer renderer : getRenderers()) {
        if (renderer.showReadoutInfo()) {
          final ReadoutInfo info = renderer.getReadoutInfo(x, y);
          readoutPanel.update(info);
        }
      }

      readoutPanel.updateForm();
    }

    ///_plot.cursorUpdated(x, y, broadcast);
  }

  @Override
  protected ReadoutInfo getViewReadoutInfo(final double x, final double y) {
    // TODO:
    return new ReadoutInfo("");
  }

  public IRenderer[] getRenderers() {
    return getVolumeViewRenderers();
  }

  public void home() {
    _viewCanvasImpl.setViewFocus(Orientation.MAP_VIEW);
    final Spatial[] spatial = null;
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
    //loadEntities(objects);
    final Shell shell = getShell();
    final BackgroundTask task = new BackgroundTask() {

      /**
       * The task for adding entities to the 3D viewer.
       * @param logger the logger.
       * @param monitor the progress monitor. 
       */

      @Override
      public Void compute(final ILogger logger, final IProgressMonitor monitor) {
        final List<Spatial> allTimeSpatials = new ArrayList<Spatial>();
        final List<Spatial> allDepthSpatials = new ArrayList<Spatial>();
        // Begin the task.
        monitor.beginTask("Adding objects to the 3D viewer", 1 + objects.length);
        try {
          Display.getDefault().asyncExec(new Runnable() {

            public void run() {

              // Lookup renderers for the individual objects.
              for (final Object obj : objects) {
                final Object object = obj;
                final List<IConfigurationElement> configsSingle = VolumeViewRendererRegistry
                    .findRenderer(shell, object);
                for (final IConfigurationElement config : configsSingle) {
                  //VolumeViewRenderer rendererSingle = RendererRegistry.selectRenderer("Renderer: " + object.toString(),
                  //    configsSingle, !multiRendererSelected)
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
                      final Spatial[] timeSpatials = newRenderer.getSpatials(Domain.TIME);
                      final Spatial[] depthSpatials = newRenderer.getSpatials(Domain.DISTANCE);
                      for (final Spatial spatial : timeSpatials) {
                        allTimeSpatials.add(spatial);
                      }
                      for (final Spatial spatial : depthSpatials) {
                        allDepthSpatials.add(spatial);
                      }
                    }
                  } catch (final Exception ex) {
                    ServiceProvider.getLoggingService().getLogger(getClass())
                        .error("Error creating renderer for " + object.toString(), ex);
                  }
                }

                // Update the progress monitor.
                monitor.worked(1);
                if (monitor.isCanceled()) {
                  break;
                }
              }

              // Lastly, center on the spatials.
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
                    VolumeViewer.this.centerOnSpatial(allTimeSpatials.toArray(new Spatial[0]));
                  }
                  break;
                case DEPTH:
                  if (numDepthSpatials > 0) {
                    VolumeViewer.this.centerOnSpatial(allDepthSpatials.toArray(new Spatial[0]));
                  }
                  break;
              }
            }

          });

        } finally {
          monitor.done();
        }
        // Lastly, trigger a redraw of all the renderers.
        Display.getDefault().syncExec(new Runnable() {

          public void run() {
            for (final VolumeViewRenderer renderer : getVolumeViewRenderers()) {
              //System.out.println("redrawing renderer...");
              //renderer.redraw();
            }
          }
        });
        monitor.worked(1);
        return null;
      }
    };

    if (block) {
      // If blocking the UI, run the task with the default JOIN flag.
      TaskRunner.runTask(task, "Add entities to the 3D viewer");
    } else {
      // Otherwise, run it in another thread.
      new Thread(new Runnable() {

        public void run() {
          TaskRunner.runTask(task, "Add entities to the 3D viewer");
        }
      }).start();
    }
    //    new Thread(new Runnable() {
    //
    //      public void run() {
    //        TaskRunner.runTask(task, "Add entities to the 3d viewer", TaskRunner.LONG);
    //        Display.getDefault().asyncExec(new Runnable() {
    //
    //          public void run() {
    //            VolumeViewer.this.refreshTree();
    //          }
    //        });
    //      }
    //    }).start();
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

      public void run() {
        final ColorRGBA colorRGBA = VolumeViewerHelper.rgbToColorRGBA(color, 1);
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
            _cursor
                .setPosition((float) p.getX(), (float) p.getY(), (float) p.getZ(), _viewCanvasImpl.getExaggeration());
          } else if (topic.equals(Topic.CURSOR_SELECTION_LOCATION)) {
            _pickCursor.setPosition((float) p.getX(), (float) p.getY(), (float) p.getZ(),
                _viewCanvasImpl.getExaggeration());
          }
          // do this in order to update the readout panel
          // we don't do this because we receive a point having z=0 from the 3D viewer, so a pick could intersect other objects
          //        _viewImpl.doPickRealWorldCoordinates(VolumeViewerHelper.point3dToVector3(p));
          makeDirty();
        }
      } else if (topic.equals(Topic.DATA_SELECTION)) {
        // TODO:
        //        List<DataSelection> dataSelections = (List<DataSelection>) message;
        //        for (DataSelection dataSelection : dataSelections) {
        //          Object[] selectedObjects = dataSelection.getSelectedObjects();
        //          float[] selectedPosition = dataSelection.getSelectedPosition();
        //          for (Object selectedObject : selectedObjects) {
        //            if (selectedObject instanceof Entity) {
        //              VolumeViewRenderer renderer = _registry.getRendererForEntity((Entity) selectedObject);
        //              if (renderer != null) {
        //                renderer.setCurrentPosition(selectedPosition);
        //              }
        //            }
        //          }
        //        }
      }
    }
  }

  /**
   * Set the viewer message text.
   * 
   * @param message the message text.
   */
  public void setMessageText(final String message) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        if (!_messageText.isDisposed()) {
          _messageText.setText(message);
        }
      }
    });
  }

  /**
   * Show the settings dialog corresponding to the provided spatial.
   * 
   * @param spatial the spatial.
   */
  public void showSettingsDialog(final Spatial spatial) {
    Display.getDefault().asyncExec(new Runnable() {

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

  //  /**
  //   * Show the settings dialog corresponding to the provided spatial.
  //   * 
  //   * @param spatial the spatial.
  //   */
  //  public void doRendererAction(final PickRecord pickRecord) {
  //    Display.getDefault().asyncExec(new Runnable() {
  //
  //      public void run() {
  //        final Spatial spatial = pickRecord.getSpatial();
  //        if (spatial != null) {
  //          final VolumeViewRenderer renderer = _nodeToRenderer.get(spatial);
  //          if (renderer != null) {
  //            //final ReadoutInfo[] info = renderer.getReadoutData(pickRecord.getLocation());
  //            final ReadoutInfo info = renderer.doPickAction(pickRecord);
  //            if (info != null) {
  //              final ReadoutPanel readoutPanel = getReadoutPanel();
  //              if (readoutPanel != null && readoutPanel.isVisible()) {
  //
  //                // If the panel is not visible we don't need to update it. 
  //                readoutPanel.update(info);
  //                readoutPanel.updateForm();
  //              }
  //            }
  //          }
  //        }
  //      }
  //    });
  //  }

  public void setPreferences(final String currentCenter, final String projectionModeStr, final RGB selectionColor) {
    final ShowMode showMode = ShowMode.getModeForId(currentCenter);
    _viewCanvasImpl.getFocusRods().setShowMode(showMode);
    final ProjectionMode projectionMode = ProjectionMode.lookup(projectionModeStr);
    if (projectionMode != null) {
      _viewCanvasImpl.setUsePerspective(projectionMode == ProjectionMode.PERSPECTIVE);
    }
    _selectionColor = VolumeViewerHelper.rgbToColorRGBA(selectionColor, 1);
  }

  public void addPropertyChangeListener(final IPropertyChangeListener listener) {
    _store.addPropertyChangeListener(listener);
    _listeners.add(listener);
  }

  public void addToScene(final Spatial spatial) {
    _viewCanvasImpl.addToScene(spatial);
  }

  public void removeFromScene(final Spatial spatial) {
    _viewCanvasImpl.removeFromScene(spatial);
  }

  public ViewCanvasImplementor getCanvasImplementor() {
    return _viewCanvasImpl;
  }

  public void addToScene(final Domain domain, final Spatial spatial) {
    if (domain == Domain.TIME) {
      _timeDomainNode.attachChild(spatial);
    } else if (domain == Domain.DISTANCE) {
      _depthDomainNode.attachChild(spatial);
    }
  }

  public void removeFromScene(final Domain domain, final Spatial spatial) {
    if (domain == Domain.TIME) {
      _timeDomainNode.detachChild(spatial);
    } else if (domain == Domain.DISTANCE) {
      _depthDomainNode.detachChild(spatial);
    }
  }

  public void centerOnSpatial(final Orientation orientation, final Spatial... targets) {
    _viewCanvasImpl.centerOnSpatial(orientation, targets);
  }

  public void centerOnSpatial(final Spatial... targets) {
    //_viewCanvasImpl.centerOnSpatial(targets);

    final Callable<Void> exe = new Callable<Void>() {

      public Void call() throws Exception {
        _viewCanvasImpl.centerOnSpatial(targets);
        return null;
      }
    };
    enqueueGLTask(exe);
  }

  /**
   * Centers the camera on the specified node.
   * 
   * @param spatial the node.
   */
  public void centerOnSpatial(final Spatial spatial) {
    if (spatial == null) {
      return;
    }
    // Call center on in opengl thread.
    final Callable<Void> exe = new Callable<Void>() {

      public Void call() throws Exception {
        final Spatial[] spatials = new Spatial[] { spatial };
        VolumeViewer.this.centerOnSpatial(spatials);
        return null;
      }
    };
    enqueueGLTask(exe);
  }

  public void enqueueGLTask(final Callable<?> exe) {
    _viewCanvasImpl.getTaskQueue().enqueue(exe);
  }

  public void cleanupTexture(final Texture tex) {
    _viewCanvasImpl.cleanupTexture(tex);
  }

  public SceneText createSceneText(final String name, final String text, final SceneText.Alignment alignment) {
    return _viewCanvasImpl.createSceneText(name, text, alignment);
  }

  public int getMaximumTextureSize() {
    return _viewCanvasImpl.getMaxTextureSize();
  }

  public IWorkbenchPartSite getSite() {
    return _site;
  }

  public void removePropertyChangeListener(final IPropertyChangeListener listener) {
    _store.removePropertyChangeListener(listener);
    _listeners.remove(listener);
  }

  public Domain getCurrentDomain() {
    return _model.getZDomain().getDomain();
  }

  /**
   * Set the current domain.
   * 
   * @param domain the current domain.
   */
  public void setCurrentDomain(final Domain domain) {
    // Set the current domain.
    _model.setZDomain(VolumeViewZDomain.lookup(domain));

    if (domain == Domain.TIME) {
      // If time domain, then remove the depth domain node and add the time domain node.
      _viewCanvasImpl.removeFromScene(_depthDomainNode);
      _viewCanvasImpl.addToScene(_timeDomainNode);
    } else if (domain == Domain.DISTANCE) {
      // If depth domain, then remove the time domain node and add the depth domain node.
      _viewCanvasImpl.removeFromScene(_timeDomainNode);
      _viewCanvasImpl.addToScene(_depthDomainNode);
    }
    setMessageText("Domain changed to " + domain);
    makeDirty();
  }

  public void setSelectedRenderer(final Object object) {
    final VolumeViewRenderer renderer = (VolumeViewRenderer) object;
    if (renderer == null) {
      return;
    }

    final Vector3 pickLoc = getPickLocation();
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

  public void showWireover(final Spatial spatial) {
    if (_viewCanvasImpl != null) {
      _viewCanvasImpl.showWireover(spatial);
    }
  }

  public void removeWireover(final Spatial spatial) {
    if (_viewCanvasImpl != null) {
      _viewCanvasImpl.removeWireover(spatial);
    }
  }

  public void makeDirty() {
    if (_viewCanvasImpl != null) {
      _viewCanvasImpl.makeDirty();
    }
  }

  /**
   * Add the cursor to the canvas. 
   * 
   * @param showDialog if the cursor dialog should be displayed.
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
    final Vector3 center = _viewCanvasImpl.getViewFocus().clone();
    center.setZ(center.getZ() / _viewCanvasImpl.getExaggeration());
    currentCursor.setPosition(center.getX(), center.getY(), center.getZ(), _viewCanvasImpl.getExaggeration());
    final VolumeViewZDomain currentDomain = _model.getZDomain();
    if (currentDomain == VolumeViewZDomain.TIME) {
      currentCursor.setCoordinateSystem(ApplicationPreferences.getInstance().getTimeCoordinateSystem());
    } else if (currentDomain == VolumeViewZDomain.DEPTH) {
      currentCursor.setCoordinateSystem(ApplicationPreferences.getInstance().getDepthCoordinateSystem());
    } else {
      throw new RuntimeException("Invalid domain: " + currentDomain);
    }
    currentCursor.getCursor().setShowMode(ShowMode.ALWAYS);
    // TODO:
    //    if (showDialog) {
    //      Vector3[] points = _viewCanvasImpl.getFrustumCornersAtZ(_viewCanvasImpl.getCameraLocation().distance(center));
    //      int maximum = (int) Math.round(Math.max(Math.abs(points[1].getY() - points[0].getY()), Math.abs(points[1].getX()
    //          - points[0].getX())));
    //      new CursorDialog(getSite().getShell(), _registry, _viewCanvasImpl, currentCursor, maximum).open();
    //      currentCursor.getCursor().setShowMode(ShowMode.NEVER);
    //    }
  }

  private void removeUnloadedEntities(final HashMap<String, Object> deletedItems) {
    removeObjects(deletedItems.values().toArray());
  }

  public Model getViewerModel() {
    return _model;
  }

  public void updateFromModel() {
    // Update the toolbar settings.
    //_toolbar.setBackgroundColor(_model.getBackgroundColor());
    _toolbar.setZDomain(_model.getZDomain());
    _toolbar.setZScaling(_model.getZScaling());
    _toolbar.setProjection(_model.getProjection());
    _toolbar.setShowPickLocation(_model.getShowPickLocation());
    final List<Spatial> spatials = new ArrayList<Spatial>();
    for (final VolumeViewRenderer renderer : getVolumeViewRenderers()) {
      switch (_model.getZDomain()) {
        case TIME:
          for (final Spatial spatial : renderer.getSpatials(Domain.TIME)) {
            spatials.add(spatial);
          }
          break;
        case DEPTH:
          for (final Spatial spatial : renderer.getSpatials(Domain.DISTANCE)) {
            spatials.add(spatial);
          }
          break;
      }
    }
    centerOnSpatial(spatials.toArray(new Spatial[0]));
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

    // Set the 3D viewer in which to render.
    renderer.setViewer(VolumeViewer.this);
    // Set the renderer's model properties.
    final Model model = renderer.getSettingsModel();
    if (model != null) {
      // Set the data entity to be rendered and add renderer to viewer.
      final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      final Map<String, Object> results = ServiceProvider.getRepository()
          .get(new EntityUniqueIdSpecification(uniqueId));
      final Object[] objects = results.values().toArray();
      renderer.setData(shell, VolumeViewer.this, objects, false);
      model.unpickle(props);
      renderer.refresh();
    }
  }

  public Vector3 getPickLocation() {
    return _pickLocation;
  }

  public Spatial getSelectedSpatial() {
    return _selectedNode;
  }

  /**
   * Set the selected spatial, at the given pick location.
   * The selected spatial is outlined and the entity is selected in the tree.
   * @param selected the selected spatial
   * @param pickLoc the pick location
   */
  public final synchronized void setSelectedSpatial(final Spatial selected, final Vector3 pickLoc) {
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

    // select the entity node in the tree
    //    TreeItem[] children = _tree.getTree().getItems();
    //    TreePath selectedPath = null;
    //    for (int i = 0; i < children.length && selectedPath == null; i++) {
    //      selectedPath = RepositoryViewData.selectPropertiesProvider(_tree.getTree(), children[i], renderer.getEntity(),
    //          null);
    //      if (selectedPath != null) {
    //        _tree.setSelection(new TreeSelection(selectedPath));
    //      }
    //    }
  }

  //  public VolumeCanvasRegistry getRegistry() {
  //    System.out.println("ASKING FOR REGISTRY: NULL\n");
  //    // TODO Auto-generated method stub
  //    return null;
  //  }

  public void mapSpatial(final Spatial spatial, final Object renderer) {
    _nodeToRenderer.put(spatial, (VolumeViewRenderer) renderer);
  }

  public void unmapSpatial(final Spatial spatial) {
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

  public Spatial[] getNodes() {
    return _nodeToRenderer.keySet().toArray(new Node[0]);
  }

  public VolumeViewRenderer getRendererForNode(final Spatial node) {
    return _nodeToRenderer.get(node);
  }

  public void removeSelectedNodes() {
    //    final StructuredSelection selectedNodes = (StructuredSelection) _tree.getSelection();
    //    final List<Object> selectedObjects = new ArrayList<Object>(Arrays.asList(selectedNodes.toArray()));
    //    for (int i = 0; i < selectedObjects.size(); i++) {
    //      final Object object = selectedObjects.get(i);
    //      if (object instanceof PropertiesProviderTreeObject) {
    //        final PropertiesProviderTreeObject treeObject = (PropertiesProviderTreeObject) object;
    //        if (treeObject.getPropertiesProvider() instanceof Entity) {
    //          removeEntity((Entity) treeObject.getPropertiesProvider());
    //          selectedObjects.addAll(Arrays.asList(treeObject.getChildren()));
    //        }
    //      }
    //      _tree.remove(object);
    //      _selectedNode = null;
    //    }
    //    refreshTree();
    //    makeDirty();
  }

  public void clearAll() {
    final List<Spatial> nodes = new ArrayList<Spatial>(_nodeToRenderer.keySet());
    for (final Spatial node : nodes) {
      //removeEntity(_nodeToRenderer.get(node).getEntity());
    }
    _nodeToRenderer.clear();
    //_overlayListeners.clear();
    _selectedNode = null;
    refreshTree();
    makeDirty();
  }

  /**
  * Refresh the entities tree.
  */
  public void refreshTree() {
    //_tree.getTree().removeAll();
    //_tree.addEntities(new HashMap<Object, Object>(_entityToNode));
  }

  public void removeEntity(final Entity entity) {
    //    AbstractRenderer renderer = getRendererForEntity(entity);
    //    if (renderer != null) {
    //      renderer.dispose();
    //    }
    //    Spatial renderedSpatial = _entityToNode.get(entity);
    //    _nodeToRenderer.remove(renderedSpatial);
    //    if (renderedSpatial != null) {
    //      renderedSpatial.removeFromParent();
    //      _view.removeWireover(renderedSpatial);
    //    }
    //    _entityToNode.remove(entity);
    //    _tree.removeUncheckedPropertiesProvider(entity);
  }

  /**
  * Convert an OpenGL coordinate (Z up) to World coordinate (where positive Z is down.)
  * @param glPoint
  * @return worldPoint
  */
  public static Vector3 toWorldSpace(final Vector3 glPoint) {
    final Vector3 worldPoint = glPoint.clone();
    worldPoint.setZ(worldPoint.getZ() * -1); // Flip Z to convert from OpenGL point to world coords.
    return worldPoint;
  }

  /**
  * @param x
  * @param y
  * @param z
  * @return
  */
  public static Vector3 toWorldSpace(final double x, final double y, final double z) {
    return toWorldSpace(new Vector3(x, y, z));
  }
}
