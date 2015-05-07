/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


/**
 * This action sets the orthographic projection method in a 3D viewer.
 */
public final class SetOrthgraphicProjection extends Action {

  /** The view canvas on which to set the projection method. */
  private final ViewCanvasImplementor _viewCanvasImpl;

  public SetOrthgraphicProjection(final ViewCanvasImplementor viewCanvasImpl) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_ORTHOGRAPHIC));
    setToolTipText("Orthgraphic projection");
    _viewCanvasImpl = viewCanvasImpl;
  }

  @Override
  public void run() {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        _viewCanvasImpl.setUsePerspective(false);
      }
    });
  }
}
