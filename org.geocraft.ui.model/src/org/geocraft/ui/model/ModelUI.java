/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.model;


import org.geocraft.internal.ui.model.ModelSharedImages;


public class ModelUI {

  public static final String PLUGIN_ID = "org.geocraft.ui.model";

  private static IModelSharedImages _sharedImages;

  public static IModelSharedImages getSharedImages() {
    if (_sharedImages == null) {
      _sharedImages = new ModelSharedImages();
    }
    return _sharedImages;
  }
}
