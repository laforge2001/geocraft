/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.light.LightSourceDialog;
import org.geocraft.ui.viewer.light.LightSourceModel;
import org.geocraft.ui.volumeviewer.VolumeViewer;


public class EditLightSource extends Action {

  /** The 3D viewer in which to edit the light source. */
  private final VolumeViewer _viewer;

  public EditLightSource(final VolumeViewer viewer) {
    super();
    setToolTipText("Edit the light source location");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_LIGHT));
    _viewer = viewer;
  }

  @Override
  public void run() {
    final LightSourceModel model = _viewer.getLightSourceModel();
    final Shell parentShell = _viewer.getShell();
    final FormDialog dialog = new LightSourceDialog(parentShell, model);
    dialog.create();
    dialog.getShell().setText("Light Source");
    dialog.getShell().setSize(400, 500);
    dialog.open();
  }
}
