///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
// */
//package org.geocraft.ui.volumeviewer;
//
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Scanner;
//import java.util.Set;
//import java.util.concurrent.Callable;
//
//import org.eclipse.jface.action.IMenuListener;
//import org.eclipse.jface.action.IMenuManager;
//import org.eclipse.jface.action.MenuManager;
//import org.eclipse.jface.action.Separator;
//import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.jface.preference.IPreferenceStore;
//import org.eclipse.jface.preference.PreferenceConverter;
//import org.eclipse.jface.util.IPropertyChangeListener;
//import org.eclipse.jface.viewers.CheckStateChangedEvent;
//import org.eclipse.jface.viewers.ICheckStateListener;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.custom.SashForm;
//import org.eclipse.swt.dnd.DND;
//import org.eclipse.swt.dnd.DropTarget;
//import org.eclipse.swt.dnd.DropTargetAdapter;
//import org.eclipse.swt.dnd.DropTargetEvent;
//import org.eclipse.swt.dnd.TextTransfer;
//import org.eclipse.swt.dnd.Transfer;
//import org.eclipse.swt.events.DisposeEvent;
//import org.eclipse.swt.events.DisposeListener;
//import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.graphics.RGB;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.layout.FormLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Menu;
//import org.eclipse.swt.widgets.TabFolder;
//import org.eclipse.swt.widgets.TabItem;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.ui.IActionBars;
//import org.eclipse.ui.IPartListener2;
//import org.eclipse.ui.IWorkbenchActionConstants;
//import org.eclipse.ui.IWorkbenchPartReference;
//import org.eclipse.ui.part.ViewPart;
//import org.geocraft.core.common.preferences.PropertyStoreFactory;
//import org.geocraft.core.model.Entity;
//import org.geocraft.core.model.datatypes.Domain;
//import org.geocraft.core.model.datatypes.Point3d;
//import org.geocraft.core.model.event.CursorLocation;
//import org.geocraft.core.model.event.DataSelection;
//import org.geocraft.core.model.grid.Grid3d;
//import org.geocraft.core.model.preferences.ApplicationPreferences;
//import org.geocraft.core.model.seismic.PostStack3d;
//import org.geocraft.core.model.seismic.PreStack3d;
//import org.geocraft.core.model.seismic.SeismicDataset;
//import org.geocraft.core.service.ServiceProvider;
//import org.geocraft.core.service.message.IMessageSubscriber;
//import org.geocraft.core.service.message.Topic;
//import org.geocraft.internal.ui.volumeviewer.canvas.Orientation;
//import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasFactory;
//import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
//import org.geocraft.internal.ui.volumeviewer.dialog.Cursor;
//import org.geocraft.internal.ui.volumeviewer.dialog.CursorDialog;
//import org.geocraft.internal.ui.volumeviewer.toolbar.MainVolumeToolbar;
//import org.geocraft.internal.ui.volumeviewer.toolbar.VolumeToolbar;
//import org.geocraft.internal.ui.volumeviewer.widget.FocusRods.ShowMode;
//import org.geocraft.ui.common.FormLayoutHelper;
//import org.geocraft.ui.repository.PropertiesProviderTreeObject;
//import org.geocraft.ui.repository.RepositoryViewData;
//import org.geocraft.ui.viewer.ReadoutInfo;
//import org.geocraft.ui.viewer.tree.EntityTree;
//import org.geocraft.ui.viewer.tree.ReadoutPanel;
//import org.geocraft.ui.volumeviewer.preference.AbstractViewerPreferencePage;
//import org.geocraft.ui.volumeviewer.preference.ViewerPreferencePage;
//import org.geocraft.ui.volumeviewer.renderer.AbstractRenderer;
//import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
//import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;
//import org.geocraft.ui.volumeviewer.tree.EntityTreeContentProvider;
//
//import com.ardor3d.image.Texture;
//import com.ardor3d.math.ColorRGBA;
//import com.ardor3d.math.Vector3;
//import com.ardor3d.scenegraph.Spatial;
//
//
///**
// * The main class for the volume viewer.
// */
//public class VolumeView extends ViewPart implements IVolumeViewer, IMessageSubscriber {
//
//  /** The volume viewer canvas registry. */
//  private VolumeCanvasRegistry _registry;
//
//  /** The tab folder panel. */
//  private TabFolder _mainFolder;
//
//  /** The entities tree. */
//  private EntityTree _tree;
//
//  /** The other volume toolbar. */
//  private VolumeToolbar _toolbar;
//
//  /** The text where the messages are displayed. */
//  private Text _messageText;
//
//  /** The readout panel. */
//  private ReadoutPanel _readoutPanel;
//
//  /** The cursor which is used for broadcast and receive the cursor locations. */
//  private Cursor _cursor;
//
//  /** The cursor which is used for receive the cursor selection locations. */
//  private Cursor _pickCursor;
//
//  private ViewCanvasImplementor _viewImpl;
//
//  /** The canvas panel. */
//  private Composite _canvasComposite;
//
//  /** The tab to display the readout data. */
//  private TabItem _readoutTab;
//
//  /** The number of opened 3d viewers. */
//  private static int _openedViewers;
//
//  private SashForm _sash;
//
//  /** A flag to keep the receive broadcast message status. */
//  private boolean _broadcastReceive;
//
//  private final IPreferenceStore _store = PropertyStoreFactory.getStore(AbstractViewerPreferencePage.ID);
//
//  private final IPropertyChangeListener _viewerPropertyListener = new ViewerPropertyListener(this, _store);
//
//  private final Set<IPropertyChangeListener> _listeners = new HashSet<IPropertyChangeListener>();
//
//  @Override
//  public void createPartControl(final Composite parent) {
//    final Composite mainPanel = new Composite(parent, SWT.NULL);
//    mainPanel.setLayout(new FormLayout());
//    _sash = new SashForm(mainPanel, SWT.NONE);
//
//    // Make a composite to act as our GL canvas parent
//    _canvasComposite = new Composite(_sash, SWT.NULL);
//    _canvasComposite.setLayout(new FillLayout());
//    _sash.setLayoutData(new FormLayoutHelper().getData(0, 0, 100, 0, 0, 65, 100, -20));
//    // Send canvas composite off for canvas creation
//    _store.setDefault(ViewerPreferencePage.CURRENT_CENTER_KEY, ShowMode.ON_INTERACT.getId());
//    _store.setDefault(ViewerPreferencePage.PROJECTION_MODE_KEY, "perspective");
//    _store.setDefault(ViewerPreferencePage.DEPTH_BITS_KEY, 24);
//    _store.setDefault(ViewerPreferencePage.SHOW_LABELS_KEY, true);
//    PreferenceConverter.setDefault(_store, ViewerPreferencePage.SELECTION_COLOR_KEY, VolumeViewerHelper
//        .colorRGBAToRGB(ColorRGBA.CYAN));
//    _viewImpl = ViewCanvasFactory
//        .makeCanvas(_canvasComposite, this, _store.getInt(ViewerPreferencePage.DEPTH_BITS_KEY));
//
//    getViewSite().getPage().addPartListener(new IPartListener2() {
//
//      @Override
//      public void partActivated(final IWorkbenchPartReference partRef) {
//        if (partRef.getPart(false) == VolumeView.this) {
//          _viewImpl.setCurrent(true);
//        }
//      }
//
//      @Override
//      @SuppressWarnings("unused")
//      public void partBroughtToTop(final IWorkbenchPartReference partRef) {
//        // does nothing for now
//      }
//
//      @Override
//      @SuppressWarnings("unused")
//      public void partClosed(final IWorkbenchPartReference partRef) {
//        // does nothing for now
//      }
//
//      @Override
//      public void partDeactivated(final IWorkbenchPartReference partRef) {
//        if (partRef.getPart(false) == VolumeView.this) {
//          _viewImpl.setCurrent(false);
//        }
//      }
//
//      @Override
//      @SuppressWarnings("unused")
//      public void partHidden(final IWorkbenchPartReference partRef) {
//        // does nothing for now
//      }
//
//      @Override
//      @SuppressWarnings("unused")
//      public void partInputChanged(final IWorkbenchPartReference partRef) {
//        // does nothing for now
//      }
//
//      @Override
//      @SuppressWarnings("unused")
//      public void partOpened(final IWorkbenchPartReference partRef) {
//        // does nothing for now
//      }
//
//      @Override
//      @SuppressWarnings("unused")
//      public void partVisible(final IWorkbenchPartReference partRef) {
//        // does nothing for now
//      }
//    });
//
//    // start a thread that will issue a repaint request every 1 second, if any nodes in the viewer
//    new Thread(new Runnable() {
//
//      public void run() {
//        while (!mainPanel.isDisposed()) {
//          try {
//            Thread.sleep(1000);
//          } catch (final InterruptedException e) {
//            e.printStackTrace();
//          }
//          if (_registry != null && _registry.getNodes().length > 0) {
//            _registry.makeCanvasDirty();
//          }
//        }
//      }
//    }, "3D Viewer repaint thread").start();
//    _toolbar = new VolumeToolbar(mainPanel, SWT.NONE, _viewImpl);
//    _toolbar.setLayoutData(new FormLayoutHelper().getData(0, 0, 100, 0, 0, 0, 0, 65));
//
//    _mainFolder = new TabFolder(_sash, SWT.TOP);
//    final TabItem entityTab = new TabItem(_mainFolder, SWT.NONE);
//    entityTab.setText("Entity List");
//    _readoutTab = new TabItem(_mainFolder, SWT.NONE);
//    _readoutTab.setText("Readout");
//
//    // Initialize the cursor information display in the tree panel. 
//    _readoutPanel = new ReadoutPanel(_mainFolder, SWT.NONE);
//    _readoutTab.setControl(_readoutPanel);
//
//    final EntityTreeContentProvider contentProvider = new EntityTreeContentProvider();
//    _tree = new EntityTree(_mainFolder, contentProvider, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//    contentProvider.setTree(_tree);
//    _tree.addCheckStateListener(new ICheckStateListener() {
//
//      @Override
//      public void checkStateChanged(final CheckStateChangedEvent event) {
//        if (event.getElement() instanceof PropertiesProviderTreeObject
//            && ((PropertiesProviderTreeObject) event.getElement()).getPropertiesProvider() instanceof Entity) {
//          final PropertiesProviderTreeObject treeObject = (PropertiesProviderTreeObject) event.getElement();
//          final Entity selectedEntity = (Entity) treeObject.getPropertiesProvider();
//          final Spatial node = _registry.getNodeForEntity(selectedEntity);
//          if (selectedEntity instanceof SeismicDataset
//              && treeObject.getParent() instanceof PropertiesProviderTreeObject) {
//            final PropertiesProviderTreeObject parentObject = (PropertiesProviderTreeObject) treeObject.getParent();
//            if (parentObject.getPropertiesProvider() instanceof SeismicDataset) {
//              // it means the selected node is an overlay
//              _registry.notifyEntityOverlayListeners((SeismicDataset) parentObject.getPropertiesProvider(),
//                  selectedEntity, event.getChecked());
//              return;
//            }
//          }
//          if (node != null) {
//            final boolean checked = event.getChecked();
//            if (checked) {
//              _tree.removeUncheckedPropertiesProvider(selectedEntity);
//            } else {
//              _tree.addUncheckedPropertiesProvider(selectedEntity);
//            }
//            _viewImpl.setVisible(node, checked);
//          }
//          if (selectedEntity instanceof Grid3d) {
//            final Spatial[] nodes = _registry.getNodes();
//            for (final Spatial n : nodes) {
//              final AbstractRenderer renderer = _registry.getRendererForNode(n);
//              if (renderer.getEntity() instanceof PostStack3d || renderer.getEntity() instanceof PreStack3d) {
//                // refresh such as the intersection between the horizons and volumes to be rendered
//                renderer.refresh();
//              }
//            }
//          }
//        }
//      }
//    });
//    getSite().setSelectionProvider(_tree);
//    entityTab.setControl(_tree.getTree());
//
//    parent.addDisposeListener(new DisposeListener() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void widgetDisposed(final DisposeEvent e) {
//        _registry.clearAll();
//        mainPanel.dispose();
//        _tree.dispose();
//        _toolbar.dispose();
//      }
//    });
//
//    _sash.setWeights(new int[] { 2, 1 });
//    _messageText = new Text(mainPanel, SWT.SINGLE | SWT.READ_ONLY);
//    _messageText.setLayoutData(new FormLayoutHelper().getData(0, 0, 100, 0, 100, -20, 100, 0));
//    _messageText.setForeground(new Color(null, 0, 0, 200));
//
//    final IActionBars bars = getViewSite().getActionBars();
//    final MainVolumeToolbar mainToolbar = new MainVolumeToolbar(this, _viewImpl);
//    mainToolbar.initToolbar(bars.getToolBarManager());
//    hookContextMenu();
//    initDragAndDrop();
//
//    final Entity[] entities = RepositoryViewData.getSelectedEntities();
//
//    _registry = new VolumeCanvasRegistry(_tree, this);
//    _toolbar.setCanvasRegistry(_registry);
//    _viewImpl.setViewFocus(Orientation.MAP_VIEW);
//    ServiceProvider.getMessageService().subscribe(Topic.CURSOR_LOCATION, this);
//    ServiceProvider.getMessageService().subscribe(Topic.CURSOR_SELECTION_LOCATION, this);
//    ServiceProvider.getMessageService().subscribe(Topic.DATA_SELECTION, this);
//    ServiceProvider.getMessageService().subscribe(Topic.DATA_DESELECTION, this);
//    ServiceProvider.getMessageService().subscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
//
//    setPreferences(_store.getString(ViewerPreferencePage.CURRENT_CENTER_KEY), _store
//        .getString(ViewerPreferencePage.PROJECTION_MODE_KEY), PreferenceConverter.getColor(_store,
//        ViewerPreferencePage.SELECTION_COLOR_KEY));
//    _openedViewers++;
//
//    _store.addPropertyChangeListener(_viewerPropertyListener);
//  }
//
//  public void setPreferences(final String currentCenter, final String projectionMode, final RGB selColor) {
//    final ShowMode mode = ShowMode.getModeForId(currentCenter);
//    _viewImpl.getFocusRods().setShowMode(mode);
//    _viewImpl.setUsePerspective(projectionMode.equals("perspective"));
//    _registry.setSelectionColor(VolumeViewerHelper.rgbToColorRGBA(selColor, 1));
//  }
//
//  public void setCurrentDomain(final Domain domain) {
//    _toolbar.setDomain(domain);
//  }
//
//  public void addPropertyChangeListener(final IPropertyChangeListener listener) {
//    _store.addPropertyChangeListener(listener);
//    _listeners.add(listener);
//  }
//
//  public void removePropertyChangeListener(final IPropertyChangeListener listener) {
//    _store.removePropertyChangeListener(listener);
//  }
//
//  /**
//   * Set the viewer message text.
//   * @param message the message
//   */
//  public void setMessageText(final String message) {
//    Display.getDefault().asyncExec(new Runnable() {
//
//      public void run() {
//        if (!_messageText.isDisposed()) {
//          _messageText.setText(message);
//        }
//      }
//    });
//  }
//
//  /**
//   * Set the entity tree visible.
//   * @param visible the visibility of the tree
//   */
//  public void setEntityListVisible(final boolean visible) {
//    final int[] weights = new int[] { 2, 1 };
//    if (!visible) {
//      weights[1] = 0;
//    }
//    _sash.setWeights(weights);
//  }
//
//  public VolumeCanvasRegistry getRegistry() {
//    return _registry;
//  }
//
//  /**
//   * Set the selected renderer and update the readout panel and the bottom text, if needed. 
//   * @param renderer the selected renderer. 
//   */
//  public void setSelectedRenderer(final Object value) {
//    final AbstractRenderer renderer = (AbstractRenderer) value;
//    if (!_readoutPanel.isVisible() || renderer == null) {
//      return;
//    }
//
//    final ReadoutInfo[] infos = renderer.getReadoutData();
//    for (final ReadoutInfo info : infos) {
//      _readoutPanel.update(info);
//    }
//    _readoutPanel.updateForm();
//
//    if (_viewImpl.isShowPickPos()) {
//      setMessageText(renderer.getShortMessage());
//      _mainFolder.setSelection(1);
//    } else {
//      setMessageText("");
//    }
//  }
//
//  /**
//   * Show the settings dialog corresponding to the provided spatial.
//   * @param spatial the spatial
//   */
//  public void showSettingsDialog(final Spatial spatial) {
//    Display.getDefault().asyncExec(new Runnable() {
//
//      public void run() {
//        final AbstractRenderer renderer = _registry.getRendererForNode(spatial);
//        if (renderer != null && renderer.getSettingsDialog() != null) {
//          final Dialog dialog = renderer.getSettingsDialog();
//          if (dialog.getShell() == null || dialog.getShell().isDisposed()) {
//            dialog.create();
//            dialog.getShell().pack();
//            final Point size = dialog.getShell().computeSize(SWT.DEFAULT, 600);
//            dialog.getShell().setSize(size);
//          }
//          dialog.getShell().setActive();
//          dialog.open();
//        }
//      }
//    });
//  }
//
//  @Override
//  public void setFocus() {
//    // Do nothing... for now
//  }
//
//  /**
//   * Hook the tree context menu.
//   */
//  private void hookContextMenu() {
//    final MenuManager menuMgr = new MenuManager("#PopupMenu");
//    menuMgr.addMenuListener(new IMenuListener() {
//
//      public void menuAboutToShow(final IMenuManager manager) {
//        fillContextMenu(manager);
//      }
//    });
//    final Menu menu = menuMgr.createContextMenu(_tree.getControl());
//    _tree.getControl().setMenu(menu);
//    getSite().registerContextMenu(menuMgr, _tree);
//  }
//
//  /**
//   * Fill the tree context menu.
//   * @param manager the menu manager
//   */
//  private void fillContextMenu(final IMenuManager manager) {
//    final MenuManager entityMenu = new MenuManager("Entity", "org.geocraft.ui.volumeviewer.entity");
//    entityMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//    manager.add(entityMenu);
//  }
//
//  /**
//   * Initialize the drag and drop for the volume canvas.
//   */
//  private void initDragAndDrop() {
//    final DropTarget target = new DropTarget(_canvasComposite, DND.DROP_COPY | DND.DROP_MOVE);
//    target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
//    target.addDropListener(new DropTargetAdapter() {
//
//      @Override
//      public void dragOver(final DropTargetEvent event) {
//        event.detail = DND.DROP_COPY;
//      }
//
//      @Override
//      public void drop(final DropTargetEvent event) {
//        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
//          final String vars = (String) event.data;
//          final List<Entity> entities = new ArrayList<Entity>();
//          final Scanner scanner = new Scanner(vars).useDelimiter(",");
//          while (scanner.hasNext()) {
//            final Entity entity = (Entity) ServiceProvider.getRepository().get(scanner.next());
//            if (entity != null) {
//              entities.add(entity);
//            }
//          }
//          _registry.addEntities(entities.toArray(new Entity[0]));
//        }
//      }
//    });
//  }
//
//  @Override
//  public void dispose() {
//    _store.removePropertyChangeListener(_viewerPropertyListener);
//    for (final IPropertyChangeListener listener : _listeners) {
//      removePropertyChangeListener(listener);
//    }
//    _listeners.clear();
//
//    ServiceProvider.getMessageService().unsubscribe(Topic.CURSOR_LOCATION, this);
//    ServiceProvider.getMessageService().unsubscribe(Topic.CURSOR_SELECTION_LOCATION, this);
//    ServiceProvider.getMessageService().unsubscribe(Topic.DATA_SELECTION, this);
//    ServiceProvider.getMessageService().unsubscribe(Topic.DATA_DESELECTION, this);
//    ServiceProvider.getMessageService().unsubscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
//    super.dispose();
//    _openedViewers--;
//  }
//
//  /**
//   * @param orientation
//   * @param targets
//   * @see org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor#centerOnSpatial(org.geocraft.internal.ui.volumeviewer.canvas.Orientation, com.jme.scene.Spatial[])
//   */
//  public void centerOnSpatial(final Orientation orientation, final Spatial... targets) {
//    _viewImpl.centerOnSpatial(orientation, targets);
//  }
//
//  /**
//   * @param targets
//   * @see org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor#centerOnSpatial(com.jme.scene.Spatial[])
//   */
//  public void centerOnSpatial(final Spatial... targets) {
//    _viewImpl.centerOnSpatial(targets);
//  }
//
//  public int getMaximumTextureSize() {
//    return _viewImpl.getMaxTextureSize();
//  }
//
//  /**
//   * Add the cursor to the canvas. 
//   * @param showDialog if the cursor dialog should be displayed
//   */
//  public void addCursor(final boolean showDialog, final boolean pickCursor) {
//    if (_cursor == null && !pickCursor) {
//      _cursor = new Cursor(0, 0, 0, ApplicationPreferences.getInstance().getDepthCoordinateSystem(), _viewImpl
//          .getCursor());
//    }
//    Cursor currentCursor = _cursor;
//    if (_pickCursor == null && pickCursor) {
//      _pickCursor = new Cursor(0, 0, 0, ApplicationPreferences.getInstance().getDepthCoordinateSystem(), _viewImpl
//          .getPick());
//    }
//    if (pickCursor) {
//      currentCursor = _pickCursor;
//    }
//    final Vector3 center = _viewImpl.getViewFocus().clone();
//    center.setZ(center.getZ() / _viewImpl.getExaggeration());
//    currentCursor.setPosition(center.getX(), center.getY(), center.getZ(), _viewImpl.getExaggeration());
//    if (_registry.getCurrentDomain() == Domain.TIME) {
//      currentCursor.setCoordinateSystem(ApplicationPreferences.getInstance().getTimeCoordinateSystem());
//    } else {
//      currentCursor.setCoordinateSystem(ApplicationPreferences.getInstance().getDepthCoordinateSystem());
//    }
//    currentCursor.getCursor().setShowMode(ShowMode.ALWAYS);
//    if (showDialog) {
//      final Vector3[] points = _viewImpl.getFrustumCornersAtZ(_viewImpl.getCameraLocation().distance(center));
//      final int maximum = (int) Math.round(Math.max(Math.abs(points[1].getY() - points[0].getY()), Math.abs(points[1]
//          .getX()
//          - points[0].getX())));
//      new CursorDialog(getSite().getShell(), _registry, _viewImpl, currentCursor, maximum).open();
//      currentCursor.getCursor().setShowMode(ShowMode.NEVER);
//    }
//  }
//
//  public void setCursorReceive(final boolean receive) {
//    _broadcastReceive = receive;
//  }
//
//  /**
//   * @param x
//   * @param y
//   * @param z
//   * @see org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor#setViewFocus(float, float, float)
//   */
//  public void setViewFocus(final float x, final float y, final float z) {
//    _viewImpl.setViewFocus(x, y, z);
//  }
//
//  /**
//   * @param orientation
//   * @see org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor#setViewFocus(org.geocraft.internal.ui.volumeviewer.canvas.Orientation)
//   */
//  public void setViewFocus(final Orientation orientation) {
//    _viewImpl.setViewFocus(orientation);
//  }
//
//  /**
//   * @param point
//   * @see org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor#setViewFocus(com.jme.math.Vector3)
//   */
//  public void setViewFocus(final Vector3 point) {
//    _viewImpl.setViewFocus(point);
//  }
//
//  /**
//   * @param mousePoint
//   * @see org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor#setViewFocus(int, int)
//   */
//  public void setViewFocus(final int x, final int y) {
//    _viewImpl.setViewFocus(x, y);
//  }
//
//  /**
//   * @param spatial
//   * @param visible
//   * @see org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor#setVisible(com.jme.scene.Spatial, boolean)
//   */
//  public void setVisible(final Spatial spatial, final boolean visible) {
//    _viewImpl.setVisible(spatial, visible);
//  }
//
//  public void addToScene(final Spatial spat) {
//    _viewImpl.addToScene(spat);
//  }
//
//  /**
//   * Show/Hide any and all wireover displays in this view
//   * @param enable if false, any wireover displays in this view will be hidden during rendering.
//   */
//  public void toggleWireover(final boolean enable) {
//    _viewImpl.toggleWireover(enable);
//  }
//
//  /**
//   * Add a wireover display for the given Spatial
//   * @param spat the Spatial to view a wireover display on
//   */
//  public void showWireover(final Spatial spat) {
//    _viewImpl.showWireover(spat);
//  }
//
//  /**
//   * Remove any wireover display for the given Spatial (if one exists).  
//   * This also happens by default if the spatial is removed from the scene via the registry's removeEntity.
//   * @param spat the Spatial to remove the wireover for
//   */
//  public void removeWireover(final Spatial spat) {
//    _viewImpl.removeWireover(spat);
//  }
//
//  public void makeDirty() {
//    _viewImpl.makeDirty();
//  }
//
//  private void removeUnloadedEntities(final HashMap<String, Object> deletedItems) {
//    final Set keys = deletedItems.keySet();
//    for (final Iterator it = keys.iterator(); it.hasNext();) {
//
//      final Object testMe = deletedItems.get(it.next().toString());
//      if (testMe instanceof Entity) {
//        final Entity test = (Entity) testMe;
//        getRegistry().removeEntity(test);
//      }
//    }
//    getRegistry().refreshTree();
//  }
//
//  @SuppressWarnings("unchecked")
//  public void messageReceived(final String topic, final Object data) {
//    if (topic.equals(Topic.REPOSITORY_OBJECTS_REMOVED)) {
//      final HashMap<String, Object> deletedItems = (HashMap<String, Object>) data;
//      removeUnloadedEntities(deletedItems);
//    }
//    if (_broadcastReceive) {
//      if (topic.equals(Topic.CURSOR_LOCATION) || topic.equals(Topic.CURSOR_SELECTION_LOCATION)) {
//        final CursorLocation cl = (CursorLocation) data;
//        Point3d p = null;
//        if (!cl.isSender("CursorDialog")) {
//          p = cl.getLocation().getPoint();
//          if (topic.equals(Topic.CURSOR_LOCATION)
//              && (_cursor == null || _cursor.getCursor().getShowMode() == ShowMode.NEVER)
//              || topic.equals(Topic.CURSOR_SELECTION_LOCATION)
//              && (_pickCursor == null || _pickCursor.getCursor().getShowMode() == ShowMode.NEVER)) {
//            addCursor(false, topic.equals(Topic.CURSOR_SELECTION_LOCATION));
//          }
//          _viewImpl.setShowPickPos(topic.equals(Topic.CURSOR_SELECTION_LOCATION));
//          if (topic.equals(Topic.CURSOR_LOCATION)) {
//            _cursor.setPosition((float) p.getX(), (float) p.getY(), (float) p.getZ(), _viewImpl.getExaggeration());
//          } else if (topic.equals(Topic.CURSOR_SELECTION_LOCATION)) {
//            _pickCursor.setPosition((float) p.getX(), (float) p.getY(), (float) p.getZ(), _viewImpl.getExaggeration());
//          }
//          // do this in order to update the readout panel
//          // we don't do this because we receive a point having z=0 from the map viewer, so a pick could intersect other objects
//          //        _viewImpl.doPickRealWorldCoordinates(VolumeViewerHelper.point3dToVector3(p));
//          makeDirty();
//        }
//      } else if (topic.equals(Topic.DATA_SELECTION)) {
//        final List<DataSelection> dataSelections = (List<DataSelection>) data;
//        for (final DataSelection dataSelection : dataSelections) {
//          final Object[] selectedObjects = dataSelection.getSelectedObjects();
//          final float[] selectedPosition = dataSelection.getSelectedPosition();
//          for (final Object selectedObject : selectedObjects) {
//            if (selectedObject instanceof Entity) {
//              final AbstractRenderer renderer = _registry.getRendererForEntity((Entity) selectedObject);
//              if (renderer != null) {
//                renderer.setCurrentPosition(selectedPosition);
//              }
//            }
//          }
//        }
//      }
//    }
//  }
//
//  public static int getOpenedViewers() {
//    return _openedViewers;
//  }
//
//  /**
//   * @param exe
//   */
//  public void enqueueGLTask(final Callable<?> exe) {
//    _viewImpl.getTaskQueue().enqueue(exe);
//  }
//
//  /**
//   * @param tex the Texture object to cleanup
//   */
//  public void cleanupTexture(final Texture tex) {
//    _viewImpl.cleanupTexture(tex);
//  }
//
//  /**
//   * @param name the scenegraph name of this new SceneText Spatial object. 
//   * @param text the initial text value for this SceneText
//   * @param alignment the alignment of the text label relative to its anchor point.
//   * @return a new SceneText object.
//   */
//  public SceneText createSceneText(final String name, final String text, final SceneText.Alignment alignment) {
//    return _viewImpl.createSceneText(name, text, alignment);
//  }
//
//  /**
//   * Convert an OpenGL coordinate (Z up) to World coordinate (where positive Z is down.)
//   * @param glPoint
//   * @return worldPoint
//   */
//  public static Vector3 toWorldSpace(final Vector3 glPoint) {
//    final Vector3 worldPoint = glPoint.clone();
//    worldPoint.setZ(worldPoint.getZ() * -1); // Flip Z to convert from OpenGL point to world coords.
//    return worldPoint;
//  }
//
//  /**
//   * @param x
//   * @param y
//   * @param z
//   * @return
//   */
//  public static Vector3 toWorldSpace(final double x, final double y, final double z) {
//    return toWorldSpace(new Vector3(x, y, z));
//  }
//
//  public Vector3 getPickLocation() {
//    final VolumeCanvasRegistry registry = getRegistry();
//    if (registry == null) {
//      return null;
//    }
//    return registry.getPickLocation();
//  }
//
//  public Spatial getSelectedSpatial() {
//    final VolumeCanvasRegistry registry = getRegistry();
//    if (registry == null) {
//      return null;
//    }
//    return getRegistry().getSelectedSpatial();
//  }
//
//  public void setSelectedSpatial(final Spatial selected, final Vector3 pickLoc) {
//    final VolumeCanvasRegistry registry = getRegistry();
//    if (registry == null) {
//      return;
//    }
//    registry.setSelectedSpatial(selected, pickLoc);
//  }
//
//  public void mapSpatial(final Spatial spatial, final Object renderer) {
//    // TODO Auto-generated method stub
//
//  }
//
//  public Spatial[] getNodes() {
//    return getRegistry().getNodes();
//  }
//
//}