/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySource;


/**
 * This class defines a top-level folder layer.
 * <p>
 * It is not rendered in the viewer, but contains a collection of child layers
 * that are displayed. This allows, for example, the grouping of all the wells
 * in the layered model under a single view group layer so that the user can
 * show/hide all the wells with a single mouse click.
 */
public class FolderLayer extends ViewGroupLayer implements IAdaptable {

  /** The description of the folder. */
  private final String _description;

  /**
   * Constructs a top-level folder layer.
   * <p>
   * By default, the unique ID will be the same as the name.
   * 
   * @param name the folder layer name.
   * @param description the description of the folder layer.
   */
  public FolderLayer(final String name, final String description) {
    this(name, name, description);
  }

  /**
   * Constructs a top-level folder layer.
   * 
   * @param name the folder layer name.
   * @param uniqueID the folder layer unique ID.
   * @param description the description of the folder layer.
   */
  public FolderLayer(final String name, final String uniqueID, final String description) {
    super(name, uniqueID, false, false);
    _description = description;
  }

  @Override
  public Image getImage() {
    // Return the image of a folder.
    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
  }

  @Override
  public Object getAdapter(final Class adapter) {
    if (adapter.equals(IPropertySource.class)) {
      return new LayerPropertySource(getName(), _description);
    }
    return null;
  }
}
