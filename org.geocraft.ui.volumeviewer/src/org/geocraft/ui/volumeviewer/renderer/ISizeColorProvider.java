/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer;




/**
 * An interface to describe the size and color provider renderers.
 */
public interface ISizeColorProvider extends IColorProvider {

  int getSize();

  void setSize(int size);
}
