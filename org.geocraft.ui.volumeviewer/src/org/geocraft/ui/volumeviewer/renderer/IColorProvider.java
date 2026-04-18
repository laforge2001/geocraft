/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.volumeviewer.renderer;


import org.joml.Vector4f;


/**
 * An interface to describe the color provider renderers.
 */
public interface IColorProvider {

  Vector4f getColor();

  void setColor(Vector4f color);

}
