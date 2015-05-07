/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer;


/**
 * An interface to describe the transparency and color provider renderers.
 */
public interface ITransparencyColorProvider extends IColorProvider {

  float getTransparency();

  void setTransparency(float transparency);
}
