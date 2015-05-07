/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.core.color.ColorBar;
import org.geocraft.ui.color.ColorBarEditorDialog;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class EditColorBar extends Action {

  private final ColorBar _colorBar;

  public EditColorBar(final ColorBar colorBar) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_COLORBAR));
    _colorBar = colorBar;
  }

  @Override
  public void run() {
    ColorBarEditorDialog dialog = ColorBarEditorDialog.createEditor(_colorBar);
    dialog.open();
  }
}
