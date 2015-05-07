/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer;


import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.geocraft.ui.viewer.layer.IViewLayer;


public abstract class AbstractViewerPart extends ViewPart implements IViewerPart {

  private static final String PART_ID = "PartID";

  private IViewer _viewer;

  public IViewer getViewer() {
    return _viewer;
  }

  @Override
  public void createPartControl(final Composite parent) {
    //Create a unique viewer part ID used when restoring state
    String partID = getPartProperty(PART_ID);
    String pid = ViewerUtilities.getViewerPartID(partID);
    if (partID == null || partID.isEmpty()) {
      setPartProperty(PART_ID, pid);
    }

    _viewer = createViewer(parent);
    getSite().setSelectionProvider(_viewer.getLayerViewer());
    hookContextMenu();
  }

  protected abstract IViewer createViewer(Composite parent);

  public void addObjects(final Object[] objects) {
    IViewer viewer = getViewer();
    viewer.addObjects(objects);
  }

  public void removeAllObjects() {
    IViewer viewer = getViewer();
    viewer.removeAllObjects();
  }

  /**
   * Hook the tree context menu.
   */
  protected void hookContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {

      public void menuAboutToShow(final IMenuManager manager) {
        fillContextMenu(manager);
      }
    });
    TreeViewer layerViewer = getViewer().getLayerViewer();
    if (layerViewer != null) {
      Menu menu = menuMgr.createContextMenu(layerViewer.getControl());
      layerViewer.getControl().setMenu(menu);
      getSite().registerContextMenu(menuMgr, layerViewer);
    }
  }

  /**
   * Fill the tree context menu.
   * @param manager the menu manager
   */
  protected void fillContextMenu(final IMenuManager manager) {
    ISelection selection = getSite().getSelectionProvider().getSelection();
    MenuManager layerMenu = new MenuManager("Layer", "org.geocraft.ui.viewer.layer");
    layerMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    manager.add(layerMenu);
    addSelectionSpecificActions(manager, selection);
  }

  public void addSelectionSpecificActions(final IMenuManager manager, final ISelection selection) {
    if (selection == null) {
      return;
    }
    if (selection.getClass().equals(TreeSelection.class)) {
      TreeSelection treeSelection = (TreeSelection) selection;
      Iterator iterator = treeSelection.iterator();
      while (iterator.hasNext()) {
        Object element = iterator.next();
        if (element instanceof IViewLayer) {
          IViewLayer viewLayer = (IViewLayer) element;
          for (IAction action : viewLayer.getActions()) {
            manager.add(action);
          }
        }
      }
    }
  }

  public String getPartId() {
    return this.getPartProperty(PART_ID);
    //return this.getConfigurationElement().getAttribute("id");
  }

  @Override
  public void dispose() {
    super.dispose();
    IViewer viewer = getViewer();
    if (viewer != null) {
      viewer.dispose();
    }
  }

  public void setViewerPartName(String partName) {
    setPartName(partName);
  }
}
