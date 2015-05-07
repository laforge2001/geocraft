/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


/**
 * This action enables the crossplot anchor to the origin (0,0).
 * When the anchor is enabled, the crossplot is restrained from
 * shifting shift to the center-point of the data series that are
 * plotted, forcing the origin (0,0) to be used as the center of
 * the plot, and forcing regressions thru the data to pass thru
 * the origin (0,0). Also, the origin will be used as the center
 * of any ellipses that are constructed. Note that the center-point
 * will not immediately shift when the anchor is enabled, but will
 * do so when another series is plotted, or a current series is
 * plotted again.
 * 
 * <i>Anchoring the crossplot to the origin (0,0) is generally used
 * on seismic data that contains both positive and negative values,
 * especially in AVO work. If data other than seismic is being
 * plotted (such as attributes that may be all positive), then the
 * anchor should be disabled to allow the plot to center on the
 * true data range and allow for a more representative set of
 * regression lines</i>
 */
public class EnableCrossplotAnchor extends Action {

  /** The crossplot in which to disable the anchor. */
  private final IABavoCrossplot _crossplot;

  public EnableCrossplotAnchor(final IABavoCrossplot crossplot) {
    super("Enable crossplot origin anchor");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_ANCHOR));
    setToolTipText("Crossplot is anchored to origin");
    _crossplot = crossplot;
  }

  @Override
  public void run() {
    // Set the crossplot anchor to enabled.
    _crossplot.getModel().setAnchoredToOrigin(true);
  }
}
