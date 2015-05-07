/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.color;

import org.geocraft.core.color.ColorMapDescription;


/**
 * The interface for the color map services.
 * The service allows other bundles to query for all the color maps
 * currently registered. These are returned as color maps.
 */
public interface IColorMapService {

  /**
   * Returns all the available color maps registered with with service.
   * 
   * @return all the available color maps.
   */
  ColorMapDescription[] getAll();
}
