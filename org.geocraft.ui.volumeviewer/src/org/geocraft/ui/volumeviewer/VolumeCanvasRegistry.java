///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
// */
//package org.geocraft.ui.volumeviewer;
//
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.Callable;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.jobs.IJobManager;
//import org.eclipse.core.runtime.jobs.ISchedulingRule;
//import org.eclipse.core.runtime.jobs.Job;
//import org.eclipse.jface.util.IPropertyChangeListener;
//import org.eclipse.swt.widgets.Display;
//import org.geocraft.core.common.progress.BackgroundTask;
//import org.geocraft.core.common.progress.TaskRunner;
//import org.geocraft.core.model.Entity;
//import org.geocraft.core.model.datatypes.Domain;
//import org.geocraft.core.model.mapper.MapperModelSchedulingRule;
//import org.geocraft.core.service.logging.ILogger;
//import org.geocraft.internal.ui.volumeviewer.canvas.SelectionRenderer;
//import org.geocraft.ui.repository.RepositoryViewData;
//import org.geocraft.ui.viewer.tree.EntityTree;
//import org.geocraft.ui.volumeviewer.renderer.RendererRegistry;
//import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
//import org.geocraft.ui.volumeviewer.tree.IEntityOverlayListener;
//import org.geocraft.ui.volumeviewer.tree.IEntityOverlayListener.Status;
//
//import com.ardor3d.image.Texture;
//import com.ardor3d.math.ColorRGBA;
//import com.ardor3d.math.Vector3;
//import com.ardor3d.scenegraph.Spatial;
//
//
///**
// * A registry for the 3d viewer, to hold collections of rendered entities, renderers and spatial objects.
// */
//public class VolumeCanvasRegistry {
//
//  /** A spatial node to entity renderer mapping. */
//  private final Map<Spatial, AbstractRenderer> _nodeToRenderer = new HashMap<Spatial, AbstractRenderer>();
//
//  /** An entity to spatial node mapping. */
//  private final Map<Entity, Spatial> _entityToNode = new HashMap<Entity, Spatial>();
//
//  /** A domain to entity list of renderers mapping. */
//  private final Map<Domain, List<AbstractRenderer>> _domainToRenderers = new HashMap<Domain, List<AbstractRenderer>>();
//
//  /** The entities tree. */
//  private final EntityTree _tree;
//
//  /** The selected spatial node. */
//  private Spatial _selectedNode;
//
//  /** The current domain. */
//  private Domain _currentDomain = Domain.DISTANCE;
//
//  /** The volume view. */
//  private final IVolumeViewer _view;
//
//  /** The entities currently rendering. The list is cleared after the entities are rendered. */
//  private final List<Entity> _renderingEntities = new ArrayList<Entity>();
//
//  /** A flag to know if the domain was set as the first loaded entity or not. */
//  private boolean _domainSet;
//
//  /** The current exaggeration value. */
//  private float _exaggeration = 1;
//
//  /** The current pick location. */
//  private Vector3 _pickLocation;
//
//  /** The selection outline color. */
//  private ColorRGBA _selectionColor;
//
//  /** The set of entity overlays listeners. Add and removed notifications can be issued. */
//  private final Set<IEntityOverlayListener> _overlayListeners = new HashSet<IEntityOverlayListener>();
//
//  /**
//   * The class constructor.
//   * @param camera the scene graph camera
//   * @param renderer the scene graph renderer
//   * @param tree the entities tree
//   */
//  public VolumeCanvasRegistry(final EntityTree tree, final VolumeView view) {
//    _tree = tree;
//    _view = view;
//    final Entity[] selectedEntities = RepositoryViewData.getSelectedEntities();
//    addEntities(selectedEntities);
//  }
//
//  public VolumeCanvasRegistry(final IVolumeViewer viewer) {
//    _tree = null;
//    _view = viewer;
//  }
//
//  /**
//   * Return the entity renderer for the specified spatial node
//   * @param node the node
//   * @return the entity renderer
//   */
//  public AbstractRenderer getRendererForNode(final Spatial node) {
//    return _nodeToRenderer.get(node);
//  }
//
//  /**
//   * Return the spatial node for the specified entity.
//   * @param entity the entity
//   * @return the spatial node
//   */
//  public Spatial getNodeForEntity(final Entity entity) {
//    return _entityToNode.get(entity);
//  }
//
//  /**
//   * Return the renderer for the specified entity.
//   * @param entity the entity
//   * @return the renderer
//   */
//  public AbstractRenderer getRendererForEntity(final Entity entity) {
//    return _nodeToRenderer.get(_entityToNode.get(entity));
//  }
//
//  /**
//   * Set the viewer message text.
//   * @param message the message
//   */
//  public void setMessage(final String message) {
//    _view.setMessageText(message);
//  }
//
//  /**
//   * Return the maximum size that a texture can have.
//   * @return the texture maximum size
//   */
//  public int getMaximumTextureSize() {
//    return _view.getMaximumTextureSize();
//  }
//
//  /**
//   * Add entities to the viewer.
//   * @param selectedEntities the entities to add
//   */
//  public void addEntities(final Entity[] selectedEntities) {
//    _renderingEntities.addAll(Arrays.asList(selectedEntities));
//    final BackgroundTask task = new BackgroundTask() {
//
//      @Override
//      public Void compute(final ILogger logger, final IProgressMonitor monitor) {
//        monitor.beginTask("Add entities to the 3d viewer", selectedEntities.length);
//        setMessage("");
//        Spatial renderedSpatial = null;
//        int nrEntitiesOtherDomain = 0;
//        int nrEntitiesNotLoaded = 0;
//        final IJobManager manager = Job.getJobManager();
//        for (final Entity entity : selectedEntities) {
//
//          final ISchedulingRule rule = new MapperModelSchedulingRule(entity.getUniqueID());
//
//          monitor.setTaskName("Adding " + entity.getDisplayName());
//          AbstractRenderer renderer = null;
//
//          if (_entityToNode.get(entity) != null) {
//            logger.info("Entity " + entity.getDisplayName() + " already exists in the viewer");
//            if (renderedSpatial == null) {
//              renderedSpatial = _entityToNode.get(entity);
//            }
//          } else {
//            renderer = RendererRegistry.getInstance().getRendererForEntityType(entity.getClass().getName());
//            if (renderer != null) {
//              renderer.setData(_view.getSite().getShell(), entity, VolumeCanvasRegistry.this);
//              if (!renderer.isFake()) {
//                if (!_domainSet && renderer.getDomain() != null && renderer.getDomain() != _currentDomain) {
//                  _currentDomain = renderer.getDomain();
//                  _view.setCurrentDomain(_currentDomain);
//                }
//                _domainSet = true;
//                if (renderer.getDomain() == null || renderer.getDomain() == _currentDomain) {
//                  try {
//                    manager.beginRule(rule, monitor);
//                    final Spatial renderedS = renderer.initSpatialRepresentation();
//
//                    if (renderedS != null) {
//                      _nodeToRenderer.put(renderedS, renderer);
//                      _entityToNode.put(entity, renderedS);
//                      _view.addToScene(renderedS);
//                      if (renderedSpatial == null) {
//                        renderedSpatial = renderedS;
//                      }
//                      // XXX: Uncomment to see wire overlays on ALL objects.
//                      // _view.showWireover(renderedS);
//                    }
//                  } finally {
//                    manager.endRule(rule);
//                  }
//                } else {
//                  _entityToNode.put(entity, null);
//                  nrEntitiesOtherDomain++;
//                }
//              }
//            } else {
//              nrEntitiesNotLoaded++;
//            }
//          }
//          monitor.worked(1);
//        }
//        _renderingEntities.clear();
//        // center at end.
//        if (renderedSpatial != null) {
//          centerOnSpatial(renderedSpatial);
//        }
//
//        if (nrEntitiesOtherDomain > 0) {
//          setMessage(nrEntitiesOtherDomain + " entities are not in the " + _currentDomain
//              + " domain and will be visible only when domain is changed.");
//        } else if (nrEntitiesNotLoaded > 0) {
//          setMessage(nrEntitiesNotLoaded + " entities could not be loaded because no renderers are available.");
//        }
//        monitor.done();
//        return null;
//      }
//    };
//
//    new Thread(new Runnable() {
//
//      public void run() {
//        TaskRunner.runTask(task, "Add entities to the 3d viewer", TaskRunner.LONG);
//        Display.getDefault().asyncExec(new Runnable() {
//
//          public void run() {
//            refreshTree();
//          }
//        });
//      }
//    }).start();
//  }
//
//  /**
//   * Set the selected spatial, at the given pick location.
//   * The selected spatial is outlined and the entity is selected in the tree.
//   * @param selected the selected spatial
//   * @param pickLoc the pick location
//   */
//  public void setSelectedSpatial(final Spatial selected, final Vector3 pickLoc) {
//    _selectedNode = selected;
//    _pickLocation = pickLoc;
//    for (final AbstractRenderer renderer : _nodeToRenderer.values()) {
//      renderer.clearOutline();
//    }
//    _view.makeDirty();
//    final AbstractRenderer renderer = getRendererForNode(_selectedNode);
//    if (_selectedNode == null || renderer == null) {
//      _view.setSelectedRenderer(null);
//      return;
//    }
//    _view.setSelectedRenderer(renderer);
//
//    if (!renderer.renderOutline()) {
//      SelectionRenderer.updateOutline(_selectedNode, _selectionColor);
//    }
//
//    // select the entity node in the tree
//    //    TreeItem[] children = _tree.getTree().getItems();
//    //    TreePath selectedPath = null;
//    //    for (int i = 0; i < children.length && selectedPath == null; i++) {
//    //      selectedPath = RepositoryViewData.selectPropertiesProvider(_tree.getTree(), children[i], renderer.getEntity(),
//    //          null);
//    //      if (selectedPath != null) {
//    //        _tree.setSelection(new TreeSelection(selectedPath));
//    //      }
//    //    }
//  }
//
//  public ColorRGBA getSelectionColor() {
//    return _selectionColor;
//  }
//
//  public void setSelectionColor(final ColorRGBA selColor) {
//    _selectionColor = selColor;
//  }
//
//  public Spatial getSelectedSpatial() {
//    return _selectedNode;
//  }
//
//  public Vector3 getPickLocation() {
//    return _pickLocation;
//  }
//
//  /**
//   * Remove the selected nodes from the viewer.
//   */
//  public void removeSelectedNodes() {
//    //    StructuredSelection selectedNodes = (StructuredSelection) _tree.getSelection();
//    //    List<Object> selectedObjects = new ArrayList<Object>(Arrays.asList(selectedNodes.toArray()));
//    //    for (int i = 0; i < selectedObjects.size(); i++) {
//    //      Object object = selectedObjects.get(i);
//    //      if (object instanceof PropertiesProviderTreeObject) {
//    //        PropertiesProviderTreeObject treeObject = (PropertiesProviderTreeObject) object;
//    //        if (treeObject.getPropertiesProvider() instanceof Entity) {
//    //          removeEntity((Entity) treeObject.getPropertiesProvider());
//    //          selectedObjects.addAll(Arrays.asList(treeObject.getChildren()));
//    //        }
//    //      }
//    //      _tree.remove(object);
//    //      _selectedNode = null;
//    //    }
//    //    refreshTree();
//    //    _view.makeDirty();
//  }
//
//  /**
//   * Clear all the entities from the viewer.
//   */
//  public void clearAll() {
//    final List<Spatial> nodes = new ArrayList<Spatial>(_nodeToRenderer.keySet());
//    for (final Spatial node : nodes) {
//      removeEntity(_nodeToRenderer.get(node).getEntity());
//    }
//    _nodeToRenderer.clear();
//    _overlayListeners.clear();
//    _selectedNode = null;
//    refreshTree();
//    _view.makeDirty();
//  }
//
//  public void removeEntity(final Entity entity) {
//    //    AbstractRenderer renderer = getRendererForEntity(entity);
//    //    if (renderer != null) {
//    //      renderer.dispose();
//    //    }
//    //    Spatial renderedSpatial = _entityToNode.get(entity);
//    //    _nodeToRenderer.remove(renderedSpatial);
//    //    if (renderedSpatial != null) {
//    //      renderedSpatial.removeFromParent();
//    //      _view.removeWireover(renderedSpatial);
//    //    }
//    //    _entityToNode.remove(entity);
//    //    _tree.removeUncheckedPropertiesProvider(entity);
//  }
//
//  /**
//   * Return the list of the currently rendering entities.
//   * @return the currently rendering entities
//   */
//  public List<Entity> getRenderingEntities() {
//    return _renderingEntities;
//  }
//
//  /**
//   * Refresh the entities tree.
//   */
//  public void refreshTree() {
//    //    _tree.getTree().removeAll();
//    //    _tree.addEntities(new HashMap<Object, Object>(_entityToNode));
//  }
//
//  public EntityTree getTree() {
//    return _tree;
//  }
//
//  /**
//   * Centers the camera on the specified node.
//   * @param spatial the node
//   */
//  public void centerOnSpatial(final Spatial spatial) {
//    if (spatial == null) {
//      return;
//    }
//    // call center on in opengl thread.
//    final Callable<Void> exe = new Callable<Void>() {
//
//      public Void call() throws Exception {
//        _view.centerOnSpatial(spatial);
//        return null;
//      }
//    };
//    enqueueGLTask(exe);
//  }
//
//  /**
//   * @param exe
//   */
//  public void enqueueGLTask(final Callable<?> exe) {
//    _view.enqueueGLTask(exe);
//  }
//
//  public Domain getCurrentDomain() {
//    return _currentDomain;
//  }
//
//  /**
//   * Set the current domain.
//   * @param domain the current domain
//   */
//  public void setCurrentDomain(final Domain domain) {
//    // save the renderers of the current domain
//    List<AbstractRenderer> renderers = new ArrayList<AbstractRenderer>();
//    for (final Spatial node : getNodes()) {
//      renderers.add(getRendererForNode(node));
//    }
//    _domainToRenderers.put(_currentDomain, renderers);
//    _currentDomain = domain;
//    final Entity[] entities = new ArrayList<Entity>(_entityToNode.keySet()).toArray(new Entity[0]);
//    clearAll();
//    if (_domainToRenderers.get(_currentDomain) == null) {
//      // no renderers for the new domain, so just add entities
//      addEntities(entities);
//    } else {
//      // add the renderers for the current domain
//      renderers = _domainToRenderers.get(_currentDomain);
//      for (final AbstractRenderer renderer : renderers) {
//        final Spatial node = renderer.getNode();
//        _entityToNode.put(renderer.getEntity(), node);
//        _nodeToRenderer.put(node, renderer);
//        _view.addToScene(node);
//      }
//    }
//    setMessage("Domain changed to " + _currentDomain);
//  }
//
//  public void addPropertyChangeListener(final IPropertyChangeListener listener) {
//    _view.addPropertyChangeListener(listener);
//  }
//
//  public void removePropertyChangeListener(final IPropertyChangeListener listener) {
//    _view.removePropertyChangeListener(listener);
//  }
//
//  /**
//   * Return the rendered entities. 
//   * @return the rendered entities
//   */
//  public Spatial[] getNodes() {
//    return _nodeToRenderer.keySet().toArray(new Spatial[0]);
//  }
//
//  /**
//   * Return all the entities in the tree. 
//   * @return the entities
//   */
//  public Object[] getEntities() {
//    return _entityToNode.keySet().toArray();
//  }
//
//  public void makeCanvasDirty() {
//    _view.makeDirty();
//  }
//
//  public void addWireover(final Spatial spatial) {
//    _view.showWireover(spatial);
//  }
//
//  public void removeWireover(final Spatial spatial) {
//    _view.removeWireover(spatial);
//  }
//
//  public float getExaggeration() {
//    return _exaggeration;
//  }
//
//  public void setExaggeration(final float exag) {
//    _exaggeration = exag;
//  }
//
//  public void cleanupTexture(final Texture tex) {
//    _view.cleanupTexture(tex);
//  }
//
//  public SceneText createSceneText(final String name, final String text, final SceneText.Alignment alignment) {
//    return _view.createSceneText(name, text, alignment);
//  }
//
//  public void addEntityOverlayListener(final IEntityOverlayListener listener) {
//    _overlayListeners.add(listener);
//  }
//
//  public void removeEntityOverlayListener(final IEntityOverlayListener listener) {
//    _overlayListeners.remove(listener);
//  }
//
//  void notifyEntityOverlayListeners(final Entity entity, final Entity overlayEntity, final boolean visible) {
//    Status status = Status.ADDED;
//    if (!visible) {
//      status = Status.REMOVED;
//    }
//    for (final IEntityOverlayListener listener : _overlayListeners) {
//      listener.overlayStatusChanged(this, entity, overlayEntity, status);
//    }
//  }
//}