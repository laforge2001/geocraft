/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.waveletviewer;


import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;


public interface IWaveletViewer extends IViewer {

  /** The string constant name for the viewer folder containing wavelets. */
  public static final String WAVELET_FOLDER = "Wavelets";

  public static final String WAVELET_SUBPLOT = "Wavelet";

  public static final String AMPLITUDE_SPECTRUM_SUBPLOT = "Amplitude Spectrum";

  public static final String PHASE_SPECTRUM_SUBPLOT = "Phase Spectrum";

  IPlot getPlot();

  /**
   * Returns the layered model of the viewer.
   */
  ILayeredModel getLayerModel();

  /**
   * Looks up one of the default folder layers based on its name.
   * @param name the name to search for.
   * @return the folder layer matching the name.
   */
  IViewLayer findFolderLayer(String name);

  /**
   * Adds objects to the map viewer.
   * @param objects the array of objects to add.
   * @param block <i>true</i> to block the UI; otherwise <i>false</i>.
   */
  void addObjects(Object[] objects, boolean block);
}
