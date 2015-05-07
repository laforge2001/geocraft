/*
 * Copyright (C) ConocoPhillips 2008-2009 All Rights Reserved.
 */
package org.geocraft.ui.property;


import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.ViewsPlugin;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.geocraft.algorithm.CreateModelessDialog;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.base.AbstractPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProviderContainer;
import org.geocraft.core.model.base.PropertyDescriptor;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.common.tree.TreeBranch;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.property.EditGrid3dProperties.Grid3dItemToEdit;
import org.geocraft.ui.property.EditSeismicDatasetProperties.SeismicDatasetItemToEdit;
import org.geocraft.ui.repository.PropertiesProviderFieldPropertySource;
import org.geocraft.ui.repository.PropertiesProviderUtils;


public class PropertiesView extends ViewPart implements IMessageSubscriber {

  public static final String NAME = "name";

  public static final String DESCRIPTION = "description";

  /** The algorithms tree viewer. */
  private TreeViewer _viewer;

  /** Property descriptors for constructing the property tree/list */
  PropertyDescriptor[] _propertyDescriptors = null;

  private DrillDownAdapter _drillDownAdapter;

  private Label _entityImage;

  private Label _entityName;

  /** Show Categories toolbar action */
  Action _showCategoriesAction;

  /** Show Tree toolbar action */
  Action _showTreeAction;

  /** The action for editing a property. */
  protected Action _editAction;

  /** State of Show Categories action */
  boolean _showCategories = false; //categories shown; otherwise list

  /** State of Show Tree action */
  boolean _showTree = true; //categories expanded; otherwise, collapsed

  // Save the current repository selection
  Object _currentSelectedItem;

  // Save the current repository entity
  Entity _currentSelectedEntity;

  /**
   * Enumeration for the items that can be editted
   */
  public enum EntityTypeToEdit {
    GRID3D("Grid3d"),
    POSTSTACK2DLINE("PostStack2dLine"),
    POSTSTACK3D("PostStack3d"),
    PRESTACK3D("PreStack3d"),
    SEISMICDATASET("SeismicDataSet");

    private String _name;

    EntityTypeToEdit(final String name) {
      _name = name;
    }

    @Override
    public String toString() {
      return _name;
    }
  }

  /**
   * Create a TreeViewer for displaying a property tree
   * @param parent Container housing the property tree
   * @return A TreeViewer
   */
  private TreeViewer createTree(Composite parent) {
    Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
    tree.setHeaderVisible(true);
    tree.setLinesVisible(true);

    GridData data = new GridData(GridData.FILL_BOTH);
    tree.setLayoutData(data);

    TableLayout layout = new TableLayout();
    tree.setLayout(layout);

    this._viewer = new TreeViewer(tree);
    createColumns(_viewer, layout);

    return _viewer;
  }

  /**
   * Create the columns for the property tree table
   * @param tree The property tree.
   * @param layout The layout for the tree.
   */
  private void createColumns(TreeViewer tree, TableLayout layout) {
    TreeViewerColumn viewerCol = new TreeViewerColumn(tree, SWT.NONE);
    TreeColumn col = viewerCol.getColumn();
    col.setResizable(true);
    col.setText("Property");
    layout.addColumnData(new ColumnWeightData(200, true));

    viewerCol = new TreeViewerColumn(tree, SWT.NONE);
    col = viewerCol.getColumn();
    col.setResizable(true);
    col.setText("Value");
    layout.addColumnData(new ColumnWeightData(200, true));
  }

  /**
   * Create actions for toolbar
   */
  private void createActions() {
    _showCategoriesAction = new Action("Show Categories/List") {

      @Override
      public void run() {
        if (_showCategories) { // showing categories, change to show list
          _viewer.setInput(createPropertyTree(_propertyDescriptors, false));
          _showCategories = false;
          _showCategoriesAction.setChecked(false);
        } else { // showing list, change to show categories
          _viewer.setInput(createPropertyTree(_propertyDescriptors, true));
          if (_showTree) {
            _viewer.expandAll();
          } else {
            _viewer.collapseAll();
          }
          _showCategories = true;
          _showCategoriesAction.setChecked(true);
        }
      }
    };
    _showCategoriesAction.setImageDescriptor(ViewsPlugin.getViewImageDescriptor("elcl16/tree_mode.gif"));
    _showCategoriesAction.setToolTipText("Show Categories/List");

    _showTreeAction = new Action("Expand/Collapse") {

      @Override
      public void run() {
        if (_showTree) { // categories expanded, change to collapse
          _viewer.collapseAll();
          _showTree = false;
          _showTreeAction.setChecked(false);
        } else { // categories collapsed, change to expand
          _viewer.expandAll();
          _showTree = true;
          _showTreeAction.setChecked(true);
        }
      }
    };
    _showTreeAction.setImageDescriptor(ViewsPlugin.getViewImageDescriptor("elcl16/filter_ps.gif"));
    _showTreeAction.setToolTipText("Expand/Collapse");

    // add selection listener
    //    _viewer.addSelectionChangedListener(new ISelectionChangedListener() {
    //
    //      public void selectionChanged(SelectionChangedEvent event) {
    //        updateActionEnablement();
    //      }
    //    });

  }

  //    private void updateActionEnablement() {
  //      IStructuredSelection sel = (IStructuredSelection) _viewer.getSelection();
  //      _showCategoriesAction.setEnabled(sel.size() > 0);
  //    }

  private void makeActions() {
    _editAction = new Action() {

      @Override
      public void run() {
        editProperty();
      }
    };
    _editAction.setText("Edit...");
    _editAction.setToolTipText("Edit the property");
    _editAction.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_IMPORT));
  }

  private void editProperty() {
    // Determine the selected property
    IStructuredSelection sel = (IStructuredSelection) _viewer.getSelection();
    Object object = sel.getFirstElement();
    TreeParent treeItem = (TreeParent) object;
    String property = treeItem.getProperty();

    // Determine the entity string name
    String entityName = _entityName.getText();

    // Determine if the current entity can be edited
    boolean editFlag = false;
    boolean entityFlag = false;
    boolean grid3dFlag = false;
    boolean seismicDatasetFlag = false;
    Grid3dItemToEdit grid3dItemToEdit = null;
    SeismicDatasetItemToEdit seismicDatasetItemToEdit = null;
    for (EntityTypeToEdit item : EntityTypeToEdit.values()) {
      if (entityName.startsWith(item.toString())) {
        entityFlag = true;
      }
    }

    // Determine if the current property can be edited
    if (entityFlag) {
      if (entityName.startsWith(EntityTypeToEdit.POSTSTACK2DLINE.toString())
          || entityName.startsWith(EntityTypeToEdit.POSTSTACK3D.toString())
          || entityName.startsWith(EntityTypeToEdit.PRESTACK3D.toString())) {
        String[] items = entityName.split(":");
        if (items.length > 1) {
          entityName = EntityTypeToEdit.SEISMICDATASET + ":" + items[1];
        }
      }

      if (entityName.startsWith(EntityTypeToEdit.GRID3D.toString())) {
        for (Grid3dItemToEdit item : Grid3dItemToEdit.values()) {
          if (property.equals(item.toString())) {
            grid3dItemToEdit = item;
            editFlag = true;
          }
        }
        grid3dFlag = true;
      } else if (entityName.startsWith(EntityTypeToEdit.SEISMICDATASET.toString())) {
        for (SeismicDatasetItemToEdit item : SeismicDatasetItemToEdit.values()) {
          if (property.equals(item.toString())) {
            seismicDatasetItemToEdit = item;
            editFlag = true;
          }
        }
        seismicDatasetFlag = true;
      }
    }

    // Display to the user which properties that he can edit
    if (entityFlag) {
      if (!editFlag) {
        String message = "You can currently edit only the following properties: \n";
        int nItems = 0;
        if (grid3dFlag) {
          for (Grid3dItemToEdit item : Grid3dItemToEdit.values()) {
            if (nItems == 0) {
              message = message + item.toString();
            } else {
              message = message + ", " + item.toString();
            }
            nItems++;
          }
        } else if (seismicDatasetFlag) {
          for (SeismicDatasetItemToEdit item : SeismicDatasetItemToEdit.values()) {
            if (nItems == 0) {
              message = message + item.toString();
            } else {
              message = message + ", " + item.toString();
            }
            nItems++;
          }
        }
        MessageDialog.openWarning(getViewSite().getShell(), "Edit", message);
      }

      // Display to the user which entities that he can edit
    } else {
      String message = "You can currently edit only the following entities: \n";
      int nItems = 0;
      for (EntityTypeToEdit item : EntityTypeToEdit.values()) {
        if (nItems == 0) {
          message = message + item.toString();
        } else {
          message = message + ", " + item.toString();
        }
        nItems++;
      }
      MessageDialog.openWarning(getViewSite().getShell(), "Edit", message);
    }

    // determine the dialog that allow the user to edit the property
    if (editFlag) {
      CreateModelessDialog currentDialog = null;
      // Determine if we need the editGrid3d dialog
      if (grid3dFlag) {
        // Set the grid based on the current selected entity
        Grid3d grid = (Grid3d) _currentSelectedEntity;
        // Edit property if the grid was found
        if (grid != null) {
          final EditGrid3dProperties editGrid3d = new EditGrid3dProperties();
          editGrid3d.setInputGrid(grid);
          editGrid3d.setItemToEdit(grid3dItemToEdit);
          editGrid3d.setSelectedItem(_currentSelectedItem);
          currentDialog = new CreateModelessDialog(null, editGrid3d);
        }
        // Determine if we need the editSeismicDataset dialog
      } else if (seismicDatasetFlag) {
        // Set the volume based on the current selected entity
        SeismicDataset volume = (SeismicDataset) _currentSelectedEntity;
        // Edit property if the volume was found
        if (volume != null) {
          final EditSeismicDatasetProperties editSeismicDataset = new EditSeismicDatasetProperties();
          editSeismicDataset.setInputVolume(volume);
          editSeismicDataset.setItemToEdit(seismicDatasetItemToEdit);
          editSeismicDataset.setSelectedItem(_currentSelectedItem);
          currentDialog = new CreateModelessDialog(null, editSeismicDataset);
        }
      }

      final CreateModelessDialog dialog = currentDialog;

      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
          RegisterPropertyEditing.setDialog(dialog);
          dialog.create();
          dialog.getShell().setSize(1080, 480);
          dialog.open();
        }
      });
    }
  }

  private void hookContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {

      public void menuAboutToShow(final IMenuManager manager) {
        fillContextMenu(manager);
      }
    });
    Menu menu = menuMgr.createContextMenu(_viewer.getControl());
    _viewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, _viewer);
  }

  private void fillContextMenu(final IMenuManager manager) {
    manager.add(_editAction);
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalToolBar(final IToolBarManager manager) {
    _drillDownAdapter.addNavigationActions(manager);
  }

  /**
   * Create the Properties toolbar
   */
  private void createToolbar() {
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(_showCategoriesAction);
    mgr.add(_showTreeAction);
  }

  /**
   * Callback for creating the property tree viewer and initializing it. 
   * @parent The container for this view part of the workbench.
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    IMessageService messageService = ServiceProvider.getMessageService();
    // register to receive notification a repository tree node was selected
    messageService.subscribe(Topic.REPOSITORY_NODE_SELECTED, this);
    // register to receive notification a property tree node was selected via double clicking on it
    messageService.subscribe(Topic.PROPERTY_NODE_SELECTED, this);

    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    parent.setLayout(gridLayout);

    Composite entityComposite = new Composite(parent, SWT.NULL);
    entityComposite.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));
    entityComposite.setLayout(GridLayoutHelper.createLayout(2, false));
    GridData headerLayoutData = GridLayoutHelper.createLayoutData(false, false, SWT.FILL, SWT.FILL, 1, 1);
    headerLayoutData.minimumWidth = 40;
    _entityImage = new Label(entityComposite, SWT.NULL);
    _entityImage.setLayoutData(headerLayoutData);
    _entityImage.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_BLANK));
    _entityName = new Label(entityComposite, SWT.NULL);
    _entityName.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));

    createTree(parent);

    //Create actions for toolbar
    //NOTE: must be performed AFTER _viewer created
    createActions();
    //Create Properties toolbar
    createToolbar();

    _drillDownAdapter = new DrillDownAdapter(_viewer);
    _viewer.setContentProvider(new PropertyViewContentProvider());
    _viewer.setLabelProvider(new PropertyViewLabelProvider());
    //Turn sorting off
    _viewer.setSorter(null);
    _viewer.setInput(createPropertyTree(true));
    //start off showing categories (instead of a linear list)
    _showCategories = false;
    _showCategoriesAction.setEnabled(true);
    _showCategoriesAction.setChecked(false);
    //start off by exapnding the categories
    _showTreeAction.setChecked(true);
    _viewer.expandAll();
    getSite().setSelectionProvider(_viewer);

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    //do nothing. This part does not take focus from within the workbench
  }

  private void hookDoubleClickAction() {
    _viewer.addDoubleClickListener(new IDoubleClickListener() {

      public void doubleClick(final DoubleClickEvent event) {
        //ISelection selection = _viewer.getSelection();
        //TreeObject obj = (TreeObject) ((IStructuredSelection) selection).getFirstElement();

        Tree tree = _viewer.getTree();
        TreeItem[] selection = tree.getSelection();
        if (selection.length == 0) {
          return;
        }
        String field = selection[0].getText(0);
        Object data = selection[0].getData();
        // if the value of a property is a properties provider, then display its properties
        if (data instanceof TreeParent) {
          Object value = ((TreeParent) data).getValue();
          if (value instanceof AbstractPropertiesProvider) {
            ServiceProvider.getMessageService().publish(Topic.PROPERTY_NODE_SELECTED, data);
          }
        }
      }
    });
  }

  /**
   * Called when a subscripted message is received.
   * 
   * @param topic The topic name
   * @param data The data transmitted from the publisher
   */
  public void messageReceived(final String topic, final Object data) {
    // Save the selected item from the repository
    _currentSelectedItem = data;

    if (this._entityName.isDisposed() || !this._entityName.isVisible()) {
      return;
    }
    if (topic.equals(Topic.REPOSITORY_NODE_SELECTED)) {
      // What matters is the event, not the sender of the event.
      ISelection theSelection = ((SelectionChangedEvent) data).getSelection();
      if (theSelection instanceof TreeSelection) {
        final Object selectedObject = ((StructuredSelection) theSelection).getFirstElement();
        if (selectedObject != null) {
          String text = "";
          Image image = null;

          //IPropertySource propertySource = null;
          if (selectedObject instanceof IPropertiesProviderContainer) {
            IPropertiesProvider propertiesProvider = ((IPropertiesProviderContainer) selectedObject)
                .getPropertiesProvider();
            if (propertiesProvider != null) {
              text = propertiesProvider.getType() + " : " + propertiesProvider.getDisplayName();
              image = ModelUI.getSharedImages().getImage(propertiesProvider);
            }
            _entityName.setText(text);
            _entityImage.setImage(image);
            if (propertiesProvider instanceof Entity) {
              IJobChangeListener listener = new JobChangeAdapter() {

                @Override
                public void done(IJobChangeEvent event) {
                  PropertiesProviderFieldPropertySource source = new PropertiesProviderFieldPropertySource(
                      ((IPropertiesProviderContainer) selectedObject).getPropertiesProvider());
                  _propertyDescriptors = (PropertyDescriptor[]) source.getPropertyDescriptors();
                  showProperties(_propertyDescriptors);
                }
              };
              Entity entity = (Entity) propertiesProvider;
              entity.load(listener);
              _currentSelectedEntity = entity;
            } else {
              PropertiesProviderFieldPropertySource source = new PropertiesProviderFieldPropertySource(
                  ((IPropertiesProviderContainer) selectedObject).getPropertiesProvider());
              _propertyDescriptors = (PropertyDescriptor[]) source.getPropertyDescriptors();
              showProperties(_propertyDescriptors);
            }
            //propertySource = source;
          } else if (selectedObject instanceof TreeBranch) {
            // root node of the repository tree
            _entityName.setText(selectedObject.toString());
            image = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
            _entityImage.setImage(image);

            Object source = ((TreeBranch) selectedObject).getAdapter(IPropertySource.class);
            IPropertyDescriptor[] descriptors = ((IPropertySource) source).getPropertyDescriptors();
            // Note: TreeBranch descriptors are Eclipse descriptors. Convert to out property descriptors
            _propertyDescriptors = new PropertyDescriptor[descriptors.length];
            int idx = 0;
            for (IPropertyDescriptor desc : descriptors) {
              if (((String) desc.getId()).equals(NAME)) {
                _propertyDescriptors[idx] = new PropertyDescriptor(desc.getId(), desc.getDisplayName(),
                    ((TreeBranch) selectedObject).getName());
                _propertyDescriptors[idx].setCategory("");
                idx++;
                continue;
              }
              if (((String) desc.getId()).equals(DESCRIPTION)) {
                _propertyDescriptors[idx] = new PropertyDescriptor(desc.getId(), desc.getDisplayName(),
                    ((TreeBranch) selectedObject).getDescription());
                idx++;
                continue;
              }

            }
            showProperties(_propertyDescriptors);
            //propertySource = (IPropertySource) source;
          }
        }
      }
    } else if (topic.equals(Topic.PROPERTY_NODE_SELECTED)) {
      // The parameter, data, is the selected property node (TreeParent).
      // Its value is an instance of AbstractPropertiesProvider.
      Object value = ((TreeParent) data).getValue();
      if (value != null) {
        String text = ((TreeParent) data).getValueString();
        Image image = ((TreeParent) data).getValueImage();
        _entityName.setText(text);
        _entityImage.setImage(image);

        _propertyDescriptors = PropertiesProviderUtils.getInstance().getPropertyDescriptors(
            ((AbstractPropertiesProvider) value));
        // create property tree from the property descriptors
        _viewer.setInput(createPropertyTree(_propertyDescriptors, _showCategories));
        //leave display controls alone
        //_showCategories = true;
        //_showCategoriesAction.setChecked(true);
        //_showTreeAction.setChecked(true);
        if (_showTree) {
          _viewer.expandAll();
        }
      }
    }
  }

  protected void showProperties(final PropertyDescriptor[] propertyDescriptors) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        // create property tree from the property descriptors
        _viewer.setInput(createPropertyTree(propertyDescriptors, _showCategories));
        //leave display controls alone
        //_showCategories = true;
        //_showCategoriesAction.setChecked(true);
        //_showTreeAction.setChecked(true);
        if (_showTree) {
          _viewer.expandAll();
        }
      }
    });
  }

  /**
   * PropertyView's label provider. Provides the text and its icon
   * label (if any) to be displayed as a pair in a column.
   *
   */
  private class PropertyViewLabelProvider extends LabelProvider {

    //NOTE: The TreeViewer calls getText() and getImage() when it updates
    // each column cell passing them the tree node. It is up to the label
    // provider to keep track of what column it is on.

    //NOTE: getText() and getImage() are unconditionally called in that order.
    int col = 1;

    @Override
    public String getText(final Object obj) {
      if (obj instanceof TreeParent) {
        TreeParent node = (TreeParent) obj;
        if (col == 1) {
          return node.toString();
        }
        if (col == 2) {
          return node.getValueString();
        }
      }
      return obj.toString();
    }

    @Override
    public Image getImage(final Object obj) {
      if (obj instanceof TreeParent) {
        TreeParent node = (TreeParent) obj;
        if (col == 1) {
          col++;
          return node.getPropertyImage();
        }
        if (col == 2) {
          col = 1;
          return node.getValueImage();
        }
      }
      return null;
    }
  }

  private class HeaderLabelProvider extends ColumnLabelProvider {

    String _label = "";

    public HeaderLabelProvider(String label) {
      _label = label;
    }

    @Override
    public String getText(final Object obj) {
      System.out.println("HeaderLabelProvideer::getText: label=" + _label);
      return _label;
    }
  }

  /**
   * Create an empty property tree
   * @param treeOrList If true, create a property tree; otherwise, a property list
   * @return Root node of an empty property tree
   */
  private TreeParent createPropertyTree(boolean treeOrList) {
    return new TreeParent("", "", null, null, null);
  }

  /*
   * Create property tree hierarchy - categories and their properties. 
   * @param propertyDescriptors Property descriptors used to construct the property tree
   * @param treeOrList If true, create a property tree; otherwise, a property list
   * @ return Root node of property tree
   */
  private TreeParent createPropertyTree(PropertyDescriptor[] propertyDescriptors, boolean treeOrList) {
    TreeParent invisibleRoot = new TreeParent("", "", null, null, null);
    if (treeOrList) { // create a property tree
      String currentCategory = "";
      TreeParent categoryNode = invisibleRoot;
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
        String category = propertyDescriptor.getCategory();
        String property = propertyDescriptor.getDisplayName();
        //Object propertyID = propertyDescriptor.getId();
        Object value = propertyDescriptor.getValue(); //propertySource.getPropertyValue(propertyID);
        Image propertyImage = propertyDescriptor.getLabelProvider().getImage(property);
        Image valueImage = propertyDescriptor.getLabelProvider().getImage(value);
        // check if starting a new category
        if (category != null && !category.equals(currentCategory)) {
          categoryNode = new TreeParent(category, "", null, null, null);
          currentCategory = category;
          invisibleRoot.addChild(categoryNode);
        }
        categoryNode.addChild(new TreeParent(category, property, value, propertyImage, valueImage));
      }
    } else { // create a property list 
      TreeParent listNode = invisibleRoot;
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
        String property = propertyDescriptor.getDisplayName();
        //Object propertyID = propertyDescriptor.getId();
        Object value = propertyDescriptor.getValue(); //propertySource.getPropertyValue(propertyID);
        Image propertyImage = propertyDescriptor.getLabelProvider().getImage(property);
        Image valueImage = propertyDescriptor.getLabelProvider().getImage(value);
        // check if starting a new category
        listNode.addChild(new TreeParent("", property, value, propertyImage, valueImage));
      }
    }

    return invisibleRoot;
  }
}
