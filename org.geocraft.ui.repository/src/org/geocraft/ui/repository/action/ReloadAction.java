/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.repository.action;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.navigator.CommonNavigator;
import org.geocraft.core.model.Entity;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.repository.RepositoryViewData;


public class ReloadAction extends Action {

  /** The repository navigator. */
  private final CommonNavigator _navigator;

  /**
   * The default constructor.
   * 
   * @param navigator the repository navigator.
   */
  public ReloadAction(final CommonNavigator navigator) {
    setText("Reload");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_RELOAD));
    _navigator = navigator;
  }

  @Override
  public void run() {
    Entity[] entities = RepositoryViewData.getSelectedEntities();
    for (Entity entity : entities) {
      entity.markGhost();
      entity.load();
      ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECT_UPDATED, entity);
    }
    _navigator.getCommonViewer().refresh(true);
  }
}
