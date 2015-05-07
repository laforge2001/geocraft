/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.sectionviewer.ISectionViewer;


public class ToggleGroupEnableAction extends Action {

  /** The associated section viewer. */
  private final ISectionViewer _viewer;

  private final boolean _enable;

  public ToggleGroupEnableAction(final ISectionViewer viewer, final boolean enable) {
    _viewer = viewer;
    _enable = enable;
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_FRAMES));
    if (enable) {
      setToolTipText("Toggle groups currently enabled");
    } else {
      setToolTipText("Toggle groups currently disabled");
    }
  }

  @Override
  public void run() {
    // We are already on the SWT thread, but we ensure this way that the setActive() will be called after the open().
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        _viewer.enableToggleGroups(_enable);
      }
    });
  }
}
