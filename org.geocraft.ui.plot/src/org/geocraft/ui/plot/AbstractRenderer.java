/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySource;
import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProviderContainer;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.repository.PropertiesProviderFieldPropertySource;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.layer.LayerPropertySource;


public abstract class AbstractRenderer<V extends IViewer> extends PlotLayer implements IPropertiesProviderContainer,
    IAdaptable, IRenderer {

  /** The parent viewer in which to render. */
  private V _viewer;

  /** The parent shell for popup dialogs, etc. */
  private Shell _shell;

  /** The action to popup the settings editor dialog. */
  protected Action _editSettings;

  /**
   * Constructs a renderer with the specified name.
   * @param name the renderer name.
   */
  public AbstractRenderer(final String name) {
    super(name);
  }

  public String getUniqueID() {
    String objectID = "";
    for (Object object : getRenderedObjects()) {
      if (object instanceof Entity) {
        objectID = ((Entity) object).getUniqueID();
        break;
      }
    }
    return getClass().getName() + " " + objectID;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof AbstractRenderer && ((AbstractRenderer) object).getRenderedObjects().length > 0) {
      Object[] thisRenderedObjects = getRenderedObjects();
      Object[] thatRenderedObjects = ((AbstractRenderer) object).getRenderedObjects();
      if (thisRenderedObjects.length != thatRenderedObjects.length) {
        return false;
      }
      for (int i = 0; i < thisRenderedObjects.length; i++) {
        if (!thisRenderedObjects[i].equals(thatRenderedObjects[i])) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    HashCode hashCode = new HashCode();
    hashCode.add(getRenderedObjects());
    return super.hashCode();
  }

  public void setData(final Shell shell, V viewer, final Object[] objects) {
    setData(shell, viewer, objects, true);
  }

  /**
   * Sets the data to be rendered.
   * <p>
   * This is necessary because the renderer is being created using OSGI, which
   * required a no-argument constructor. Thus, the object to render has to be
   * supplied after creation via this method.
   * 
   * @param shell the shell to use for popup dialogs, etc.
   * @param objects the array of objects to render.
   * @param viewer the viewer in which to render.
   */
  public void setData(final Shell shell, V viewer, final Object[] objects, boolean autoUpdate) {

    // Store the parent shell, viewer and rendered objects.
    _shell = shell;
    setViewer(viewer);
    setRenderedObjects(objects);

    // Update the name and icon of the renderer based on the survey.
    setNameAndImage();

    // Add the popup menu actions.
    addPopupMenuActions();

    // Draw the plot shapes.
    clear();
    addPlotShapes();

    // Add this renderer to the viewer tree.
    addToLayerTree(autoUpdate);
  }

  protected final V getViewer() {
    return _viewer;
  }

  protected void setViewer(V viewer) {
    _viewer = viewer;
  }

  protected final Shell getShell() {
    return _shell;
  }

  /**
   * Sets the objects being rendered.
   * Each renderer should store an internal reference of the specific
   * type of objects being rendered.
   * @param objects the array of objects to render.
   */
  protected abstract void setRenderedObjects(Object[] objects);

  /**
   * Sets the name and image of the renderer in the tree view.
   */
  protected abstract void setNameAndImage();

  /**
   * Adds the renderer-specific actions to the popup menu in the tree view.
   */
  protected abstract void addPopupMenuActions();

  /**
   * Adds the plot shapes that represent the rendered object.
   */
  protected abstract void addPlotShapes();

  /**
   * Adds the renderer to the layered tree view.
   */
  protected void addToLayerTree() {
    addToLayerTree(true);
  }

  /**
   * Adds the renderer to the layered tree view.
   * 
   * @param autoUpdate <i>true</i> to auto-update the view bounds; otherwise <i>false</i>.
   */
  protected abstract void addToLayerTree(boolean autoUpdate);

  /**
   * Triggers a redraw of the renderer.
   */
  public abstract void redraw();

  /**
   * Return the data selection for this renderer at the given point.
   * The data selection can consist of one or more entities. Each
   * renderer must implement its own logic to check if the specified
   * x,y coordinate represents a "selection" of its rendered object(s).
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the data selection; or <i>null</i> if the x,y coordinate represents no selection.
   */
  public abstract DataSelection getDataSelection(final double x, final double y);

  /**
   * Triggers the action to popup the renderer settings dialog.
   */
  public void editSettings() {
    if (_editSettings != null) {
      Display.getDefault().asyncExec(new Runnable() {

        public void run() {
          _editSettings.run();
        }
      });
    }
  }

  /**
   * Adds the "Settings..." action to the button #3 popup menu for the renderer
   * in the layer tree.
   * @param width the preferred width for the settings dialog.
   * @param height the preferred height for the settings dialog.
   */
  protected void addSettingsPopupMenuAction(final Dialog dialog, final int width, final int height) {
    // Create the action to popup the settings dialog.
    _editSettings = new Action("Settings...") {

      @Override
      public void run() {
        if (dialog.getShell() == null || dialog.getShell().isDisposed()) {
          dialog.create();
          dialog.getShell().pack();
          Point size = dialog.getShell().computeSize(width, height);
          dialog.getShell().setSize(size);
        }
        // We are already on the SWT thread, but we ensure
        // this way that the setActive() will be called after the open().
        Display.getDefault().asyncExec(new Runnable() {

          public void run() {
            dialog.getShell().setActive();
          }
        });
        dialog.open();
      }
    };
    _editSettings.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_EDIT));

    // Add the action to the popup menu.
    addPopupMenuAction(_editSettings);
  }

  /**
   * Adds the renderer to the layered model tree, under the folder layer
   * specified by the folder name.
   * @param folderName the name of the folder layer.
   * @param autoUpdate <i>true</i> to auto-update the view bounds; otherwise <i>false</i>.
   */
  protected void addToLayerTree(final String folderName, boolean autoUpdate) {
    addToLayerTree(null, "", folderName, autoUpdate);
  }

  /**
   * Adds the renderer to the layered model tree.
   * This method first adds the parent object and then insert the renderer
   * underneath it in the layered model. If the parent object is <i>null</i>,
   * then it simply attempts to add the renderer under the folder layer
   * specified by the folder name.
   * @param viewer the viewer in which to add the renderer.
   * @param parentObject the parent object to add before this renderer.
   * @param parentObjectName the name of the parent object to add.
   * @param folderName the name of the folder layer.
   * @param autoUpdate <i>true</i> to auto-update the view bounds; otherwise <i>false</i>.
   */
  protected abstract void addToLayerTree(final Object parentObject, final String parentObjectName,
      final String folderName, final boolean autoUpdate);

  public Object getAdapter(final Class adapter) {
    if (adapter.equals(IPropertySource.class)) {
      for (Object object : getRenderedObjects()) {
        if (object instanceof IPropertiesProvider) {
          return new PropertiesProviderFieldPropertySource((IPropertiesProvider) object);
        }
      }
      return new LayerPropertySource(getName(), "");
    }
    return null;
  }

  public IPropertiesProvider getPropertiesProvider() {
    for (Object object : getRenderedObjects()) {
      if (object instanceof IPropertiesProvider) {
        return (IPropertiesProvider) object;
      }
    }
    return null;
  }

  protected final void setName(final Entity entity) {
    IRepository repository = ServiceProvider.getRepository();
    String varName = repository.lookupVariableName(entity);
    if (varName == null || varName.isEmpty()) {
      setName(entity.getDisplayName());
      return;
    }
    setName(varName + "=" + entity.getDisplayName());
  }
}
