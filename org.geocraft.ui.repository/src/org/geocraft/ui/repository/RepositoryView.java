/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.common.tree.TreeBranch;
import org.geocraft.ui.common.tree.TreeRoot;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.repository.RepositoryViewCollator.SortMethod;
import org.geocraft.ui.repository.action.ExportAction;
import org.geocraft.ui.repository.action.LoadAction;
import org.geocraft.ui.repository.action.LoadFromDatastore;
import org.geocraft.ui.repository.action.ReloadAction;
import org.geocraft.ui.repository.action.SaveAction;


/**
 * The repository view.
 */
public class RepositoryView extends CommonNavigator implements ISelectionChangedListener, IMessageSubscriber {

  /** The logger. */

  /** The root node. */
  private final TreeRoot _root;

  /** The navigator drill down adapter. */
  protected DrillDownAdapter _drillDownAdapter;

  /** The action for bringing up the list of data load types. */
  protected Action _loadAction;

  /** The action for unloading the currently selected item(s). */
  protected Action _unloadAction;

  /** The action for reloading the currently selected item(s). */
  protected Action _reloadAction;

  /** The action for reseting the current contents of the repository. */
  protected Action _resetAction;

  /** A toggle button to switch between showing or not the variable name in the tree. */
  protected Action _sortByVars;

  /** The repository common viewer. */
  private RepositoryViewer _viewer;

  public RepositoryView() {
    _root = new TreeRoot();
    Image image = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_DEF_VIEW);
    setTitleImage(image);
    ServiceProvider.getMessageService().subscribe(Topic.REPOSITORY_OBJECT_SELECTED, this);
  }

  @Override
  public IAdaptable getInitialInput() {
    return _root;
  }

  @Override
  public void createPartControl(final Composite aParent) {
    super.createPartControl(aParent);
    _drillDownAdapter = new DrillDownAdapter(getCommonViewer());
    makeActions();
    hookContextMenu();
    contributeToActionBars();
    getCommonViewer().getTree().setLinesVisible(false);
    getSite().setSelectionProvider(getCommonViewer());
    setTitleToolTip("Repository");
    getCommonViewer().addSelectionChangedListener(this);
    getCommonViewer().setSorter(new RepositoryViewSorter(SortMethod.BY_ENTITY_NAME));
    //ServiceComponent.setSortByVars(_sortByVars.isChecked());
  }

  @Override
  protected CommonViewer createCommonViewer(final Composite parent) {
    _viewer = new RepositoryViewer(getViewSite().getId(), parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    //    _viewer.getTree().addKeyListener(new KeyAdapter() {
    //
    //      @Override
    //      public void keyPressed(KeyEvent e) {
    //        if (e.keyCode == SWT.F3) {
    //          Entity[] entities = RepositoryViewData.getSelectedEntities();
    //          for (Entity entity : entities) {
    //            entity.markGhost();
    //            entity.load();
    //            ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECT_UPDATED, entity);
    //          }
    //        }
    //      }
    //
    //    });
    return _viewer;
  }

  private void hookContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {

      public void menuAboutToShow(final IMenuManager manager) {
        fillContextMenu(manager);
      }
    });
    Menu menu = menuMgr.createContextMenu(getCommonViewer().getControl());
    getCommonViewer().getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, getCommonViewer());
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalPullDown(final IMenuManager manager) {
    manager.add(_loadAction);
    manager.add(new Separator());
    manager.add(_unloadAction);
    manager.add(_resetAction);
    manager.add(new Separator());
    manager.add(_sortByVars);
  }

  private void fillContextMenu(final IMenuManager manager) {
    MenuManager loadMenu = new MenuManager("Load", "org.geocraft.ui.repository.loadview");
    manager.add(loadMenu);
    //manager.add(_loadAction);
    manager.add(_unloadAction);
    manager.add(new Separator());
    manager.add(_reloadAction);
    manager.add(new Separator());
    _drillDownAdapter.addNavigationActions(manager);
    MenuManager viewMenu = new MenuManager("View", "org.geocraft.ui.repository.plotview");
    MenuManager taskMenu = new MenuManager("Tasks", "org.geocraft.ui.repository.taskview");
    // Other plug-ins can contribute there actions here
    viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    manager.add(viewMenu);// Other plug-ins can contribute there actions here
    taskMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    manager.add(taskMenu);
    addSelectionSpecificActions(manager);
  }

  private void fillLocalToolBar(final IToolBarManager manager) {
    manager.removeAll();
    ActionContributionItem loadCI = new ActionContributionItem(_loadAction);
    //loadCI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
    manager.add(loadCI);
    ActionContributionItem unloadCI = new ActionContributionItem(_unloadAction);
    //unloadCI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
    manager.add(unloadCI);
    manager.add(new ActionContributionItem(_resetAction));
    manager.add(new Separator());
    manager.add(new ActionContributionItem(_sortByVars));
    manager.add(new Separator());
    _drillDownAdapter.addNavigationActions(manager);
  }

  private void makeActions() {
    _loadAction = new LoadAction(this);
    _loadAction.setText("Load...");
    _loadAction.setToolTipText("Load from data store");
    _loadAction.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_IMPORT));
    _unloadAction = new Action() {

      @Override
      public void run() {
        String message = "Unload the selected variables?";
        if (MessageDialog.openQuestion(getViewSite().getShell(), "Unload", message)) {
          unload();
        }
      }
    };
    _unloadAction.setText("Unload");
    _unloadAction.setToolTipText("Unload the selected entities");
    _unloadAction.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_DELETE));

    _reloadAction = new ReloadAction(this);

    _resetAction = new Action() {

      @Override
      public void run() {
        String message = "Unload all variables and clear the shell command history?";
        if (MessageDialog.openQuestion(getViewSite().getShell(), "Reset", message)) {
          IRepository repository = ServiceProvider.getRepository();
          repository.clear();
        }
      }
    };
    _resetAction.setText("Clear");
    _resetAction.setToolTipText("Clear the entire contents of the repository");
    _resetAction.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_RESET));

    _sortByVars = new Action("Current sort: " + SortMethod.BY_ENTITY_NAME, SWT.TOGGLE) {

      @Override
      public void run() {
        //ServiceComponent.setSortByVars(_sortByVars.isChecked());
        SortMethod sortMethod = SortMethod.BY_ENTITY_NAME;
        if (_sortByVars.isChecked()) {
          sortMethod = SortMethod.BY_VAR_NAME;
        }
        RepositoryView.this.getCommonViewer().setSorter(new RepositoryViewSorter(sortMethod));
        _viewer.refresh(true);
        _sortByVars.setToolTipText("Current sort: " + sortMethod);
      }
    };
    //_sortByVars.setToolTipText("Sort by variable names in the tree");
    _sortByVars.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_SORT));
    _sortByVars.setChecked(false);
  }

  public void selectionChanged(final SelectionChangedEvent event) {
    //Notify the PropertyView to display the properties of the selection
    ServiceProvider.getMessageService().publish(Topic.REPOSITORY_NODE_SELECTED, event);

    List<Entity> selectedEntities = new ArrayList<Entity>();
    StructuredSelection sel = (StructuredSelection) event.getSelection();
    List<Object> selectedObjects = new ArrayList<Object>();
    selectedObjects.addAll(Arrays.asList(sel.toArray()));
    for (int i = 0; i < selectedObjects.size(); i++) {
      Object selectedObject = selectedObjects.get(i);
      if (selectedObject instanceof TreeBranch) {
        TreeBranch branch = (TreeBranch) selectedObject;
        if (selectedObject instanceof PropertiesProviderTreeObject
            && ((PropertiesProviderTreeObject) selectedObject).getPropertiesProvider() instanceof Entity) {
          selectedEntities.add((Entity) ((PropertiesProviderTreeObject) selectedObject).getPropertiesProvider());
        }
        selectedObjects.addAll(Arrays.asList(branch.getChildren()));
      }
    }
    RepositoryViewData.setSelectedObjects(selectedObjects);
    RepositoryViewData.setSelectedEntities(selectedEntities);
  }

  public void addSelectionSpecificActions(final IMenuManager manager) {

    ISelection selection = getSite().getSelectionProvider().getSelection();
    IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
    if (datastoreAccessorService == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error("No service provider found.");
      return;
    }

    if (selection.getClass().equals(TreeSelection.class)) {
      TreeSelection treeSelection = (TreeSelection) selection;
      Iterator iterator = treeSelection.iterator();
      while (iterator.hasNext()) {
        Object element = iterator.next();
        if (element.getClass().equals(TreeBranch.class)) {
          TreeBranch folder = (TreeBranch) element;
          IDatastoreAccessor[] datastoreAccessors = datastoreAccessorService.getDatastoreAccessors(
              folder.getLoadOptions(), IOMode.INPUT);
          for (IDatastoreAccessor datastoreAccessor : datastoreAccessors) {
            System.out.println("Supported: " + datastoreAccessor.getName());
          }
          if (datastoreAccessors.length > 0) {
            for (IDatastoreAccessor datastoreAccessor : datastoreAccessors) {
              manager.findMenuUsingPath("org.geocraft.ui.repository.loadview").add(
                  new LoadFromDatastore(datastoreAccessor));
            }
          }
        }
      }
    }

    IDatastoreAccessor[] datastoreAccessors = datastoreAccessorService.getDatastoreAccessors();
    final CommonNavigator navigator = this;
    if (selection.getClass().equals(TreeSelection.class)) {
      TreeSelection treeSelection = (TreeSelection) selection;
      Iterator iterator = treeSelection.iterator();
      List<Entity> entityList = new ArrayList<Entity>();
      List<Class> classList = new ArrayList<Class>();
      while (iterator.hasNext()) {
        Object element = iterator.next();
        if (element.getClass().equals(PropertiesProviderTreeObject.class)) {
          PropertiesProviderTreeObject entityObject = (PropertiesProviderTreeObject) element;
          if (entityObject.getPropertiesProvider() instanceof Entity) {
            Entity entity = (Entity) entityObject.getPropertiesProvider();
            entityList.add(entity);
            if (!classList.contains(entity.getClass())) {
              classList.add(entity.getClass());
            }
          }
        }
      }

      manager.add(new SaveAction(navigator, entityList.toArray(new Entity[0])));

      for (IDatastoreAccessor datastoreAccessor : datastoreAccessors) {
        boolean canSaveTheseEntitiesToThisDatastore = false;
        if (datastoreAccessor.canOutput()) {
          canSaveTheseEntitiesToThisDatastore = true;
          String[] entityClasses = datastoreAccessor.getSupportedEntityClassNames();
          for (Class klass : classList) {
            boolean canSaveThisClass = false;
            for (String entityClasse : entityClasses) {
              if (klass.getSimpleName().equals(entityClasse)) {
                canSaveThisClass = true;
                break;
              }
            }
            if (!canSaveThisClass) {
              canSaveTheseEntitiesToThisDatastore = false;
              break;
            }
          }
        }
        if (canSaveTheseEntitiesToThisDatastore && classList.size() > 0) {
          manager.add(new ExportAction(navigator, entityList.toArray(new Entity[0])));
          break;
        }
      }
    }
  }

  /**
   * Called when an EventBus event is received.
   * 
   * @param topic the topic name
   * @param data the data transmitted from the publisher
   */
  public void messageReceived(final String topic, final Object data) {
    if (topic.equals(Topic.REPOSITORY_OBJECT_SELECTED)) {
      Display.getDefault().asyncExec(new Runnable() {

        public void run() {
          TreePath[] expanded = getCommonViewer().getExpandedTreePaths();
          IPropertiesProvider propProvider = (IPropertiesProvider) data;
          // we need to expand the tree to make it load all the items
          getCommonViewer().expandAll();
          getCommonViewer().collapseAll();
          getCommonViewer().setExpandedTreePaths(expanded);

          TreeItem[] children = getCommonViewer().getTree().getItems();
          TreePath selectedPath = null;
          for (int i = 0; i < children.length && selectedPath == null; i++) {
            selectedPath = RepositoryViewData.selectPropertiesProvider(getCommonViewer().getTree(), children[i],
                propProvider, selectedPath);
            if (selectedPath != null) {
              _viewer.setSelection(new TreeSelection(selectedPath));
              setFocus();
            }
          }
        }
      });
    }
  }

  public void unload() {
    // Get the repository view selection.
    Object[] selectedTreeObjects = RepositoryViewData.getSelectedObjects();
    IRepository repository = ServiceProvider.getRepository();
    List<String> keys = new ArrayList<String>();
    // Iterate thru the selection items.
    for (Object treeObject : selectedTreeObjects) {
      if (treeObject instanceof PropertiesProviderTreeObject) {
        // Get the entity from the tree object and lookup its key.
        PropertiesProviderTreeObject entityObject = (PropertiesProviderTreeObject) treeObject;
        Object object = entityObject.getPropertiesProvider();
        if (object instanceof PostStack3d) {
          PostStack3d ps3d = (PostStack3d) object;
          ps3d.close();
        } else if (object instanceof PreStack3d) {
          PreStack3d ps3d = (PreStack3d) object;
          ps3d.close();
        }
        String key = repository.lookupVariableName(object);
        keys.add(key);
      } else {
        ServiceProvider.getLoggingService().getLogger(getClass())
            .warn("Cannot unload the " + treeObject.toString() + " node at present.");
      }
    }
    // Remove the entities from the repository.
    repository.remove(keys.toArray(new String[0]));
  }

  class RepositoryViewer extends CommonViewer {

    /**
     * @param viewerId
     * @param parent
     * @param style
     */
    public RepositoryViewer(final String viewerId, final Composite parent, final int style) {
      super(viewerId, parent, style);
    }

    @Override
    protected void initDragAndDrop() {
      addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
          new DragSourceListener() {

            public void dragFinished(final DragSourceEvent event) {
              // No action for drag finished.
            }

            public void dragSetData(final DragSourceEvent event) {
              StringBuffer vars = new StringBuffer("");
              Entity[] entities = RepositoryViewData.getSelectedEntities();
              for (Entity entity : entities) {
                vars.append(ServiceProvider.getRepository().lookupVariableName(entity) + ",");
              }
              event.data = vars.toString();
            }

            public void dragStart(final DragSourceEvent event) {
              Entity[] entities = RepositoryViewData.getSelectedEntities();
              if (entities.length > 0) {
                Image image = ModelUI.getSharedImages().getImage(entities[0]);
                if (image != null) {
                  event.image = image;
                }
              }
              event.doit = entities.length > 0;
            }
          });
    }

  }

  @Override
  /**
   * @deprecated The implementation of PropertyView no longer is based on PropertySheet
   */
  public Object getAdapter(Class key) {
    if (key == IPropertySheetPage.class) {
      return new UnsortedPropertySheetPage();
    }

    return super.getAdapter(key);
  }
}
