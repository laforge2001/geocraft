/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


import java.util.EnumSet;


/**
 * Enumeration for image anchors.
 * Options include the upper left, upper right,
 * lower left, lower right, and center.
 * These are used for anchoring the image drawing origin.
 */
public enum ImageAnchor {
  UpperLeft("Upper Left"), UpperRight("Upper Right"), LowerLeft("Lower Left"), LowerRight("Lower Right"), Center("Center");

  /** The name of the image anchor. */
  private String _name;

  /**
   * Constructs a image anchor.
   * @param name the name of the image anchor.
   */
  ImageAnchor(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the image anchor.
   * @return the name of the image anchor.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }

  /**
   * Looks up a image anchor based on its name.
   * @param name the image anchor name.
   * @return the image anchor matching name, or <i>CENTER</i> if not found.
   */
  public static ImageAnchor lookup(final String name) {
    EnumSet<ImageAnchor> set = EnumSet.range(UpperLeft, Center);
    for (ImageAnchor imageAnchor : set) {
      if (imageAnchor.getName().equals(name)) {
        return imageAnchor;
      }
    }
    return Center;
  }
}
