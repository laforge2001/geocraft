/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.repository.action;


import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonNavigator;
import org.geocraft.core.model.Entity;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class SaveAction extends Action {

  /** The repository navigator. */
  private final CommonNavigator _navigator;

  /** The entities to export. */
  private final Entity[] _entities;

  /**
   * The default constructor.
   * 
   * @param navigator the repository navigator.
   */
  public SaveAction(final CommonNavigator navigator, final Entity entity) {
    this(navigator, new Entity[] { entity });
  }

  /**
   * The default constructor.
   * 
   * @param navigator the repository navigator.
   */
  public SaveAction(final CommonNavigator navigator, final Entity[] entities) {
    setText("Save");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_SAVE_AS));
    _navigator = navigator;
    _entities = entities;
    int numDirty = 0;
    for (Entity entity : entities) {
      if (entity.isDirty()) {
        numDirty++;
        break;
      }
    }
    setEnabled(numDirty > 0);
  }

  @Override
  public void run() {
    for (Entity entity : _entities) {
      try {
        entity.update();
      } catch (IOException ex) {
        Display display = Display.getDefault();
        Shell shell = new Shell(display);
        MessageDialog.openError(shell, "Save Error", ex.getMessage());
      }
    }
    _navigator.getCommonViewer().refresh(true);
  }
}