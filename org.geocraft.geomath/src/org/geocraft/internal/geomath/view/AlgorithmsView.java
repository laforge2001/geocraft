/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;
import org.geocraft.algorithm.StandaloneAlgorithmEditorInput;
import org.geocraft.algorithm.StandaloneAlgorithmRegistry;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.internal.geomath.PLUGIN;


/**
 * The algorithms view.
 */
public class AlgorithmsView extends ViewPart implements IMessageSubscriber {

  /** The algorithms tree viewer. */
  private TreeViewer _viewer;

  private DrillDownAdapter _drillDownAdapter;

  /** The algorithms tree filtering. */
  private Text _filterText;

  /** The default text into the text filter widget. */
  private static final String FILTER_TEXT = "type filter text";

  /**
   * This is a callback that will allow us to create the _viewer and initialize it.
   */
  @Override
  public void createPartControl(final Composite parent) {
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    parent.setLayout(gridLayout);
    GridData panelLayoutData = new GridData();
    panelLayoutData.grabExcessHorizontalSpace = true;
    panelLayoutData.grabExcessVerticalSpace = false;
    panelLayoutData.horizontalAlignment = SWT.FILL;
    panelLayoutData.verticalAlignment = SWT.CENTER;

    _filterText = new Text(parent, SWT.SINGLE);
    _filterText.setLayoutData(panelLayoutData);
    _filterText.setText(FILTER_TEXT);

    _viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    GridData viewerLayoutData = new GridData();
    viewerLayoutData.grabExcessHorizontalSpace = true;
    viewerLayoutData.grabExcessVerticalSpace = true;
    viewerLayoutData.horizontalAlignment = SWT.FILL;
    viewerLayoutData.verticalAlignment = SWT.FILL;
    _viewer.getTree().setLayoutData(viewerLayoutData);
    _filterText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(final KeyEvent event) {
        _viewer.collapseAll();
        _viewer.refresh();
        if (!getFilterText().equals("")) {
          _viewer.expandAll();
        }
      }
    });

    _drillDownAdapter = new DrillDownAdapter(_viewer);
    _viewer.setContentProvider(new ViewContentProvider(this));
    _viewer.setLabelProvider(new ViewLabelProvider());
    _viewer.setSorter(new ViewerSorter());
    _viewer.setInput(getViewSite());
    getSite().setSelectionProvider(_viewer);
    _viewer.addFilter(new ViewerFilter() {

      @Override
      public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element instanceof TreeObject && ((TreeObject) element).isLeaf()) {
          TreeObject object = (TreeObject) element;
          return object.getName().toLowerCase().indexOf(getFilterText().toLowerCase()) >= 0;
        }
        return true;
      }
    });

    _viewer.getTree().addMouseTrackListener(new MouseTrackAdapter() {

      @Override
      public void mouseHover(final MouseEvent event) {
        if (_viewer != null) {
          Tree tree = _viewer.getTree();
          TreeItem item = tree.getItem(new Point(event.x, event.y));
          if (item != null) {
            Object data = item.getData();
            if (data.getClass().equals(TreeObject.class)) {
              TreeObject treeObject = (TreeObject) data;
              IStandaloneAlgorithmDescription algorithm = treeObject.getStandaloneAlgorithm();
              if (algorithm != null) {
                tree.setToolTipText(treeObject.getStandaloneAlgorithm().getToolTip());
              } else {
                tree.setToolTipText("");
              }
            } else {
              tree.setToolTipText("");
            }
          }
        }
      }
    });
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();
    ServiceProvider.getMessageService().subscribe(StandaloneAlgorithmRegistry.ALGORITHM_REGISTRY_UPDATED, this);
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

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillContextMenu(final IMenuManager manager) {
    _drillDownAdapter.addNavigationActions(manager);
    // Other plug-ins can contribute the actions here
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
  }

  private void fillLocalToolBar(final IToolBarManager manager) {
    _drillDownAdapter.addNavigationActions(manager);
  }

  private void hookDoubleClickAction() {
    _viewer.addDoubleClickListener(new IDoubleClickListener() {

      public void doubleClick(final DoubleClickEvent event) {
        ISelection selection = _viewer.getSelection();
        TreeObject obj = (TreeObject) ((IStructuredSelection) selection).getFirstElement();
        if (obj.isLeaf()) {
          try {
            IStandaloneAlgorithmDescription algorithm = obj.getStandaloneAlgorithm();
            if (algorithm != null) {
              StandaloneAlgorithmEditorInput input = StandaloneAlgorithmRegistry.getInstance()
                  .getEditorInput(algorithm);
              getSite().getPage().openEditor(input, "org.geocraft.algorithm.StandaloneAlgorithmEditor");
            }
          } catch (PartInitException e) {
            ServiceProvider.getLoggingService().getLogger(getClass()).warn("Cannot display algorithm editor", e);
          }
        }
      }
    });
  }

  @Override
  public Object getAdapter(final Class adapter) {
    if (IContextProvider.class.equals(adapter)) {
      return new ContextProvider(PLUGIN.PLUGIN_ID + ".context_dynamichelpview", (IStructuredSelection) _viewer
          .getSelection());
    }
    return super.getAdapter(adapter);
  }

  /**
   * Passing the focus request to the filter text.
   */
  @Override
  public void setFocus() {
    _filterText.setFocus();
    _filterText.selectAll();
  }

  /**
   * Return the algorithm filter text.
   * @return the filter text
   */
  private String getFilterText() {
    String text = _filterText.getText();
    if (text.length() > 0 && !text.equals(FILTER_TEXT)) {
      return text;
    }
    return "";
  }

  public void messageReceived(String topic, Object message) {
    if (topic.equals(StandaloneAlgorithmRegistry.ALGORITHM_REGISTRY_UPDATED)) {
      if (_viewer != null) {
        _viewer.setContentProvider(new ViewContentProvider(this));
      }
    }
  }

  @Override
  public void dispose() {
    ServiceProvider.getMessageService().unsubscribe(StandaloneAlgorithmRegistry.ALGORITHM_REGISTRY_UPDATED, this);
    super.dispose();
  }
}