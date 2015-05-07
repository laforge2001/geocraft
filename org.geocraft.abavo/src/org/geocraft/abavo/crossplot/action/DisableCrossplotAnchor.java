/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


/**
 * This action disables the crossplot anchor to the origin (0,0).
 * When the anchor is disabled, the crossplot is free to shift to
 * the center-point of the data series that are plotted, and this
 * center-point will be used as the center of any ellipses that
 * are constructed. Also, regression lines for the data series
 * will not be required to pass thru the origin (0,0) when the
 * anchor is disabled. Note that the center-point will not shift
 * immediately when the anchor is disabled, but will do so when
 * another series is plotted, or a current series is re-plotted.
 *
 * <i>Anchoring the crossplot to the origin (0,0) is generally used
 * on seismic data that contains both positive and negative values,
 * especially in AVO work. If data other than seismic is being
 * plotted (such as attributes that may be all positive), then the
 * anchor should be disabled to allow the plot to center on the
 * true data range and allow for a more representative set of
 * regression lines</i>
 */
public class DisableCrossplotAnchor extends Action {

  /** The crossplot in which to disable the anchor. */
  private final IABavoCrossplot _crossplot;

  public DisableCrossplotAnchor(final IABavoCrossplot crossplot) {
    super("Disable crossplot origin anchor");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_ANCHOR));
    setToolTipText("Crossplot is NOT anchored to origin");
    _crossplot = crossplot;
  }

  @Override
  public void run() {
    // Set the crossplot anchor to disabled.
    _crossplot.getModel().setAnchoredToOrigin(false);
  }
}
