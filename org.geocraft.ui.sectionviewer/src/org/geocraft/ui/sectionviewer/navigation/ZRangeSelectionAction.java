/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.navigation;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.sectionviewer.ISectionViewer;


/**
 * This class defines the action that brings up the dialog used to select the z range of the section viewer.
 */
public class ZRangeSelectionAction extends Action {

  /** The dialog used to select the z range. */
  private ZRangeSelectionDialog _zRangeSelectionDialog;

  /** The associated section viewer. */
  private final ISectionViewer _viewer;

  public ZRangeSelectionAction(final ISectionViewer viewer) {
    _viewer = viewer;
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_Z_RANGE));
    setToolTipText("Open the z-range selection dialog");
  }

  @Override
  public void run() {
    // We are already on the SWT thread, but we ensure this way that the setActive() will be called after the open().
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        if (_zRangeSelectionDialog == null) {
          Shell shell = new Shell(Display.getDefault().getActiveShell(), SWT.ON_TOP | SWT.APPLICATION_MODAL);
          _zRangeSelectionDialog = new ZRangeSelectionDialog(shell, _viewer);
        }
        if (_zRangeSelectionDialog.getShell() == null || _zRangeSelectionDialog.getShell().isDisposed()) {
          _zRangeSelectionDialog.create();
          _zRangeSelectionDialog.getShell().pack();
          Point size = _zRangeSelectionDialog.getShell().computeSize(250, SWT.DEFAULT);
          _zRangeSelectionDialog.getShell().setSize(size);
        }
        _zRangeSelectionDialog.getShell().setActive();
        _zRangeSelectionDialog.open();
      }
    });
  }
}
