/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.defs.CommunicationStatus;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


/**
 * This action enables the crossplot communication with other applications and plugins.
 * When communication is enabled, points selected in the crossplot will be broadcast,
 * and the crossplot will respond to outside signals. At present, however, there are
 * no outside signals to which the crossplot responds.
 */
public class EnableCommunication extends Action {

  /** The crossplot in which to enable communication. */
  private final IABavoCrossplot _crossplot;

  public EnableCommunication(final IABavoCrossplot crossplot) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_BROADCAST));
    setToolTipText("Communication is enabled");
    _crossplot = crossplot;
  }

  @Override
  public void run() {
    // Set the communication flag to enabled.
    _crossplot.getModel().setCommunicationStatus(CommunicationStatus.COMMUNICATION_ENABLED);
  }
}
