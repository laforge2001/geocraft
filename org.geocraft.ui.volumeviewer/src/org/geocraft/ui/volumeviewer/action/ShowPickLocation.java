/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class ShowPickLocation extends Action {

  private final ViewCanvasImplementor _viewCanvasImpl;

  private final boolean _show;

  public ShowPickLocation(final ViewCanvasImplementor viewCanvasImpl, final boolean show) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_POS_SIZE));
    _viewCanvasImpl = viewCanvasImpl;
    _show = show;
    if (show) {
      setToolTipText("The display of the pick location is enabled");
    } else {
      setToolTipText("The display of the pick location is disabled");
    }
  }

  @Override
  public void run() {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        _viewCanvasImpl.setShowPickPos(_show);
      }
    });
  }
}
