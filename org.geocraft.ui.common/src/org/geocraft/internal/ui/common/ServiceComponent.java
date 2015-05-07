/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.common;


import org.eclipse.jface.resource.ImageRegistry;


public class ServiceComponent {

  public static final String PLUGIN_ID = "org.geocraft.ui.common";

  private static ImageRegistry _imageRegistry;

  public static synchronized ImageRegistry getImageRegistry() {
    if (_imageRegistry == null) {
      _imageRegistry = new ImageRegistry();
    }
    return _imageRegistry;
  }

}