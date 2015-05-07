/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer;


import com.ardor3d.math.ColorRGBA;


/**
 * An interface to describe the color provider renderers.
 */
public interface IColorProvider {

  ColorRGBA getColor();

  void setColor(ColorRGBA color);

}
