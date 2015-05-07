/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySource;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProviderContainer;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.internal.ui.volumeviewer.action.CenterOnNodeAction;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.repository.PropertiesProviderFieldPropertySource;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.layer.LayerPropertySource;
import org.geocraft.ui.volumeviewer.renderer.grid.SmoothingMethod;

import com.ardor3d.image.Texture.MagnificationFilter;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;


/**
 * This class defines the abstract base class for all 3D view renderers.
 */
public abstract class VolumeViewRenderer implements IPropertiesProviderContainer, IAdaptable, IRenderer {

  /** The name of the renderer. */
  protected String _name;

  /** The tree image for the layer. */
  protected Image _image;

  protected boolean _blockUpdate = false;

  protected boolean _includeBounds;

  /** The parent shell for popup dialogs, etc. */
  protected Shell _shell;

  /** The action to popup the settings editor dialog. */
  protected Action _editSettings;

  /** The flag for showing the readout info. */
  protected boolean _showReadout;

  /** The list of pop-up actions registered with the plot layer. */
  protected List<Action> _actions;

  /** The flag for appending cursor info. */
  protected boolean _appendCursorInfo;

  /** The 3D viewer in which to render. */
  protected VolumeViewer _viewer;

  /** The current domain. */
  protected Domain _domain;

  /** If enabled, an interpolation smoothing will be applied over the textures. */
  private SmoothingMethod _smoothing;

  /** The magnification filter used for rendering textures. */
  private MagnificationFilter _magFilter = MagnificationFilter.NearestNeighbor;

  /** The minification filter used for rendering textures. */
  private MinificationFilter _minFilter = MinificationFilter.NearestNeighborLinearMipMap;

  /** The aniso level used for rendering textures. */
  private float _aniso = 0.0f;

  /** The flag for showing the renderer's readout info. */
  private boolean _showReadoutInfo = true;

  /** The labels display status, for the renderers supporting labels. */
  private boolean _showLabels = true;

  /**
   * The base constructor for a 3D view renderer.
   * 
   * @param name the renderer name.
   */
  public VolumeViewRenderer(final String name) {
    _name = name;
    final Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
    _image = new Image(image.getDevice(), image, SWT.IMAGE_COPY);
    _appendCursorInfo = false;
    _actions = Collections.synchronizedList(new ArrayList<Action>());
    for (final Action action : createDefaultActions()) {
      _actions.add(action);
    }
    _includeBounds = true;
    _showReadout = true;
    final PropertyStore store = PropertyStoreFactory.getStore(VolumeViewerPreferencePage.ID);
    _showLabels = store.getBoolean(VolumeViewerPreferencePage.SHOW_LABELS);
  }

  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    _name = name;
  }

  public Image getImage() {
    return _image;
  }

  /**
  * Sets the image to use for the renderer icon.
  * 
  * @param image the renderer image.
  */
  public void setImage(final Image image) {
    _image = image;
  }

  @Override
  public final boolean equals(final Object object) {
    if (object instanceof VolumeViewRenderer && ((VolumeViewRenderer) object).getRenderedObjects().length > 0) {
      final Object[] thisRenderedObjects = getRenderedObjects();
      final Object[] thatRenderedObjects = ((VolumeViewRenderer) object).getRenderedObjects();
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
  public final int hashCode() {
    final HashCode hashCode = new HashCode();
    hashCode.add(getRenderedObjects());
    return super.hashCode();
  }

  public void setData(final Shell shell, final VolumeViewer viewer, final Object[] objects) {
    setData(shell, viewer, objects, true);
  }

  /**
   * Sets the data to be renderered.
   * This is necessary because the renderer is being created using OSGI, which
   * required a no-argument constructor. Thus, the object to render has to be
   * supplied after creation via this method.
   * @param shell the shell to use for popup dialogs, etc.
   * @param objects the array of objects to render.
   * @param viewer the viewer in which to render.
   */
  public final void setData(final Shell shell, final VolumeViewer viewer, final Object[] objects,
      final boolean autoUpdate) {

    // Store the parent shell, viewer and rendered objects.
    _shell = shell;
    setViewer(viewer);
    setRenderedObjects(objects);

    // Update the name and icon of the renderer based on the survey.
    setNameAndImage();

    // Add the popup menu actions.
    addPopupMenuAction(new CenterOnNodeAction(_viewer, this));
    addPopupMenuActions();

    // Draw the plot shapes.
    clear();
    addSpatials();

    // Add this renderer to the viewer tree.
    addToLayerTree(autoUpdate);
  }

  public final VolumeViewer getViewer() {
    return _viewer;
  }

  /**
   * Sets the viewer in which to render.
   * 
   * @param viewer the viewer.
   */
  protected final void setViewer(final VolumeViewer viewer) {
    _viewer = viewer;
  }

  public final void setVisible(final boolean flag) {
    final Domain[] domains = { Domain.TIME, Domain.DISTANCE };
    for (final Domain domain : domains) {
      final Spatial[] spatials = getSpatials(domain);
      if (spatials != null) {
        for (final Spatial spatial : spatials) {
          if (flag) {
            _viewer.addToScene(domain, spatial);
          } else {
            _viewer.removeFromScene(domain, spatial);
          }
        }
      }
    }
  }

  public abstract Spatial[] getSpatials(Domain domain);

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
  protected abstract void addSpatials();

  /**
   * Gets the actions associated with the renderer.
   * These will be used in the popup menu in the tree viewer.
   * 
   * @return the renderer actions.
   */
  public Action[] getActions() {
    return _actions.toArray(new Action[0]);
  }

  /**
   * Adds an action to the renderer.
   * 
   * @param action the action to add.
   */
  protected void addAction(final Action action) {
    _actions.add(action);
  }

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

  public void editSettings() {
    if (_editSettings != null) {
      Display.getDefault().asyncExec(new Runnable() {

        public void run() {
          _editSettings.run();
        }
      });
    }
  }

  protected final synchronized void addPopupMenuAction(final Action action) {
    _actions.add(action);
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
          final Point size = dialog.getShell().computeSize(width, height);
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
    _editSettings.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(
        org.geocraft.ui.common.image.ISharedImages.IMG_EDIT));

    // Add the action to the popup menu.
    addPopupMenuAction(_editSettings);
  }

  /**
   * Adds the renderer to the layered model tree, under the folder layer
   * specified by the folder name.
   * @param folderName the name of the folder layer.
   * @param autoUpdate <i>true</i> to auto-update the view bounds; otherwise <i>false</i>.
   */
  protected void addToLayerTree(final String folderName, final boolean autoUpdate) {
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
  protected void addToLayerTree(final Object parentObject, final String parentObjectName, final String folderName,
      final boolean autoUpdate) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        // First add the parent object.
        if (parentObject != null) {
          _viewer.addObjects(true, new Object[] { parentObject });
        }

        // Create a view layer for this renderer.
        final VolumeViewRenderer renderer = VolumeViewRenderer.this;
        final IViewLayer viewLayer = new RendererViewLayer(renderer.getName(), renderer);

        // Attempt to find the parent layer in the layered model.
        final IViewLayer[] layers = _viewer.getLayerModel().getChildren(_viewer.findFolderLayer(folderName));
        IViewLayer parentLayer = null;
        if (parentObjectName != null && parentObjectName.length() > 0) {
          for (int k = 0; k < layers.length && parentLayer == null; k++) {
            if (layers[k].getName().equals(parentObjectName)) {
              parentLayer = layers[k];
              break;
            }
          }
        }

        // If no parent layer found, default to a top-level folder layer.
        if (parentLayer == null) {
          parentLayer = _viewer.findFolderLayer(folderName);
        }

        // Add the layer to the layered model.
        _viewer.getLayerModel().addLayer(viewLayer, parentLayer);
      }
    });
  }

  public Object getAdapter(final Class adapter) {
    if (adapter.equals(IPropertySource.class)) {
      for (final Object object : getRenderedObjects()) {
        if (object instanceof IPropertiesProvider) {
          return new PropertiesProviderFieldPropertySource((IPropertiesProvider) object);
        }
      }
      return new LayerPropertySource(getName(), "");
    }
    return null;
  }

  public IPropertiesProvider getPropertiesProvider() {
    for (final Object object : getRenderedObjects()) {
      if (object instanceof IPropertiesProvider) {
        return (IPropertiesProvider) object;
      }
    }
    return null;
  }

  /**
   * Return the magnification filter used for rendering textures.
   * @return the magnification filter used for rendering textures
   */
  public final synchronized MagnificationFilter getMagnificationFilter() {
    return _magFilter;
  }

  /**
   * Return the minification filter used for rendering textures.
   * @return the minification filter used for rendering textures
   */
  public final synchronized MinificationFilter getMinificationFilter() {
    return _minFilter;
  }

  /**
   * Return the aniso level used for rendering textures.
   * @return the aniso level used for rendering textures
   */
  public final synchronized float getAnisoLevel() {
    return _aniso;
  }

  /**
   * Return the smoothing status.
   * @return the smoothing status
   */
  public final synchronized SmoothingMethod getSmoothing() {
    return _smoothing;
  }

  /**
   * Set the smoothing status.
   * @param smoothing the smoothing status
   */
  public final synchronized void setSmoothing(final SmoothingMethod smoothing) {
    _smoothing = smoothing;
    _magFilter = MagnificationFilter.NearestNeighbor;
    _minFilter = MinificationFilter.NearestNeighborLinearMipMap;
    _aniso = 0.0f;
    if (_smoothing != null && _smoothing == SmoothingMethod.INTERPOLATION) {
      _magFilter = MagnificationFilter.Bilinear;
      _minFilter = MinificationFilter.Trilinear;
      _aniso = 1.0f;
    }
  }

  public Domain getDomain() {
    return _domain;
  }

  public void setDomain(final Domain domain) {
    _domain = domain;
  }

  public abstract void clearOutline();

  public abstract boolean renderOutline();

  /**
   * Return a renderer specific message, to be displayed on the bottom side of the viewer.
   * @return a short message
   */
  public String getShortMessage() {
    final Vector3 pickLoc = _viewer.getPickLocation();
    if (pickLoc != null) {
      return "x=" + pickLoc.getX() + ", y=" + pickLoc.getY() + ", z=" + pickLoc.getZ();
    }
    return "";
  }

  /**
   * Returns an array of the renderer's readout info for the given pick location.
   * 
   * @param pickLoc the pick location.
   * @return an array of readout info.
   */
  public abstract ReadoutInfo[] getReadoutData(Vector3 pickLoc);

  public final ReadoutInfo getReadoutInfo(final double x, final double y) {
    // This does not apply for the 3D viewer, so it is implemented as final.
    return null;
  }

  public final boolean showReadoutInfo() {
    return _showReadoutInfo;
  }

  public final void showReadoutInfo(final boolean show) {
    _showReadoutInfo = show;
  }

  public void clear() {
    final Domain[] domains = { Domain.TIME, Domain.DISTANCE };
    for (final Domain domain : domains) {
      for (final Spatial spatial : getSpatials(domain)) {
        _viewer.unmapSpatial(spatial);
        _viewer.removeFromScene(domain, spatial);
      }
    }
  }

  protected final void setName(final Entity entity) {
    final IRepository repository = ServiceProvider.getRepository();
    final String varName = repository.lookupVariableName(entity);
    if (varName == null || varName.isEmpty()) {
      setName(entity.getDisplayName());
      return;
    }
    setName(varName + "=" + entity.getDisplayName());
  }

  /**
   * Creates the default actions for the renderer.
   * Others can be added later using the <code>addAction</code> method.
   * 
   * @return the default renderer actions.
   */
  protected Action[] createDefaultActions() {
    return new Action[0];
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#isVisible()
   */
  @Override
  public boolean isVisible() {
    // TODO Auto-generated method stub
    return false;
  }

  public String getUniqueID() {
    String objectID = "";
    for (final Object object : getRenderedObjects()) {
      if (object instanceof Entity) {
        objectID = ((Entity) object).getUniqueID();
        break;
      }
    }
    return getClass().getName() + " " + objectID;
  }

  /**
   * Return the show labels status.
   * @return the show labels status
   */
  public boolean getShowLabels() {
    return _showLabels;
  }

  /**
   * Set the show labels status.
   * @param showLabels the show labels status
   */
  public void setShowLabels(final boolean showLabels) {
    _showLabels = showLabels;
  }

  /**
   * @param pickLoc
   * @param selected
   */
  public void triggerClickAction(final Vector3 pickLoc, final Spatial selected) {
    // Does nothing by default. Renderers may override this method.
  }

  //  /**
  //   * Triggered renderer-specific action to be taken with a pick is made on one of the renderer spatials.
  //   * 
  //   * @param pickRecord
  //   */
  //  public ReadoutInfo doPickAction(final PickRecord pickRecord) {
  //    // Does nothing by default. Renderers should override this as needed.
  //    return null;
  //  }
}
