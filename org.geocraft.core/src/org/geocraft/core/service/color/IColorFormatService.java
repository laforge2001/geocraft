/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.color;

import org.geocraft.core.color.ColorFormatDescription;


/**
 * The interface for the color format services.
 * The service allows other bundles to query for all the color formats
 * currently registered. These are returned as color formats.
 */
public interface IColorFormatService {

  /**
   * Returns all the available color formats registered with with service.
   * 
   * @return all the available color formats.
   */
  ColorFormatDescription[] getAll();
}
