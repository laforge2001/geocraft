/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.mapviewer.viewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.viewer.light.LightSourceDialog;
import org.geocraft.ui.viewer.light.LightSourceModel;


public class EditLightSource extends Action {

  /** The map viewer in which to edit the light source. */
  private final IMapViewer _viewer;

  public EditLightSource(final IMapViewer viewer) {
    super();
    setToolTipText("Edit the light source location");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_LIGHT));
    _viewer = viewer;
  }

  @Override
  public void run() {
    LightSourceModel model = _viewer.getLightSourceModel();
    Shell parentShell = _viewer.getPlot().getModelSpaceCanvas().getComposite().getShell();
    FormDialog dialog = new LightSourceDialog(parentShell, model);
    dialog.create();
    dialog.getShell().setText("Light Source");
    dialog.getShell().setSize(400, 500);
    dialog.open();
  }
}
