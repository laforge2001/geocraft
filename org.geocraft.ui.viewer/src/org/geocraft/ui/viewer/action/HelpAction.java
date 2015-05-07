/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class HelpAction extends Action {

  private String _contextId;

  public HelpAction() {
    setToolTipText("Help");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_HELP));
  }

  public HelpAction(final String contextId) {
    this();
    _contextId = contextId;
  }

  @Override
  public void run() {
    if (_contextId != null) {
      PlatformUI.getWorkbench().getHelpSystem().displayHelp(_contextId);
    } else {
      PlatformUI.getWorkbench().getHelpSystem().displayDynamicHelp();
    }
  }
}
