/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


import java.util.EnumSet;


/**
 * Enumeration for text anchors.
 * These are used for anchoring the text drawing origin.
 */
public enum TextAnchor {
  NORTHWEST("Northwest"), NORTH("North"), NORTHEAST("Northeast"), WEST("West"), CENTER("Center"), EAST("East"), SOUTHWEST(
      "Swouthwest"), SOUTH("South"), SOUTHEAST("Southeast");

  /** The name of the text anchor. */
  private String _name;

  /**
   * Constructs a text anchor.
   * @param name the name of the text anchor.
   */
  TextAnchor(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the text anchor.
   * @return the name of the text anchor.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }

  /**
   * Looks up a text anchor based on its name.
   * @param name the text anchor name.
   * @return the text anchor matching name, or <i>CENTER</i> if not found.
   */
  public static TextAnchor lookup(final String name) {
    EnumSet<TextAnchor> set = EnumSet.range(NORTHWEST, SOUTHEAST);
    for (TextAnchor textAnchor : set) {
      if (textAnchor.getName().equals(name)) {
        return textAnchor;
      }
    }
    return CENTER;
  }

  /**
   * Returns the anchor to use to text, based on a delta-x and delta-y.
   * This is intended for renderers that wantt to display text at the end
   * of a line and want the best placement to avoid the text appearing
   * on top of the line.
   * @param dx the delta-x.
   * @param dy the delta-y.
   * @return the preferred text anchor.
   */
  public static TextAnchor getBest(final double dx, final double dy) {
    TextAnchor anchor = TextAnchor.CENTER;
    double angle = Math.toDegrees(Math.atan2(dy, dx));
    if (angle >= -22.5 && angle <= 22.5) {
      anchor = TextAnchor.EAST;
    } else if (angle >= 22.5 && angle <= 67.5) {
      anchor = TextAnchor.NORTHEAST;
    } else if (angle >= 67.5 && angle <= 112.5) {
      anchor = TextAnchor.NORTH;
    } else if (angle >= 112.5 && angle <= 157.5) {
      anchor = TextAnchor.NORTHWEST;
    } else if (angle >= 157.5 && angle <= 202.5) {
      anchor = TextAnchor.WEST;
    } else if (angle >= -67.5 && angle <= -22.5) {
      anchor = TextAnchor.SOUTHEAST;
    } else if (angle >= -112.5 && angle <= -67.5) {
      anchor = TextAnchor.SOUTH;
    } else if (angle >= -157.5 && angle <= -112.5) {
      anchor = TextAnchor.SOUTHWEST;
    } else if (angle >= -202.5 && angle <= -157.5) {
      anchor = TextAnchor.WEST;
    }
    return anchor;
  }
}
