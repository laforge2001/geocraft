/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer;


/**
 * Enumeration of the blending types.
 * <p>
 * None: No blending.
 * Simple: Simple weighted blending of 2 entities.
 * Triple: Advanced RGB blending of 3 entities (1 entity for Red, 1 for Green, 1 for Blue).
 */
public enum BlendingType {
  NONE("None"),
  WEIGHTED("2-Grid Weighted Blending"),
  RGB_BLENDING("3-Grid RGB Blending");

  private String _text;

  private BlendingType(final String text) {
    _text = text;
  }

  @Override
  public String toString() {
    return _text;
  }
}
