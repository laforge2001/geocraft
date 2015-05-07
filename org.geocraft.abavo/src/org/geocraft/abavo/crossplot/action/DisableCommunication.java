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
 * This action disables the crossplot communication with other applications and plugins.
 * When communication is disabled, points selected in the crossplot will not be broadcast,
 * and the crossplot will not respond to outside signals. At present, however, there are
 * no outside signals to which the crossplot responds.
 */
public class DisableCommunication extends Action {

  /** The crossplot in which to disable communication. */
  private final IABavoCrossplot _crossplot;

  public DisableCommunication(final IABavoCrossplot crossplot) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_BROADCAST));
    setToolTipText("Communication is disabled");
    _crossplot = crossplot;
  }

  @Override
  public void run() {
    // Set the communication flag to disabled.
    _crossplot.getModel().setCommunicationStatus(CommunicationStatus.COMMUNICATION_DISABLED);
  }
}
