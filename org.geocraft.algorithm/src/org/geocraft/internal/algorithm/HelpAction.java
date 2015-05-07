/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.algorithm;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class HelpAction extends Action {

  public HelpAction() {
    setToolTipText("Help");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_HELP));
  }

  @Override
  public void run() {
    PlatformUI.getWorkbench().getHelpSystem().displayDynamicHelp();
  }
}
