/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

/*
 * Copyright (C) Openspirit Corp 2007 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.dialog;


import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.internal.ui.volumeviewer.widget.FocusRods;
import org.geocraft.ui.volumeviewer.VolumeViewer;


/**
 * Encapsulate a focus or pick cursor.
 */
public class Cursor {

  /** The x location of the cursor. */
  private double _x;

  /** The y location of the cursor. */
  private double _y;

  /** The x location of the cursor. */
  private double _z;

  /** The current coordinate system. */
  private CoordinateSystem _coordinateSystem;

  /** The encapsulated visual cursor. */
  private final FocusRods _cursor;

  /**
   * The constructor.
   * @param x the x location
   * @param y the y location
   * @param z the z location
   * @param cs the coordinate system
   * @param cursor the visual cursor
   * @param exagValue the exaggeration value
   */
  public Cursor(final double x, final double y, final double z, final CoordinateSystem cs, final FocusRods cursor) {
    _coordinateSystem = cs;
    _cursor = cursor;
    setPosition(x, y, z, 1);
  }

  /**
   * Set the current cursor position.
   * @param x the x location
   * @param y the y location
   * @param z the z location
   * @param exagValue the exaggeration value
   */
  public void setPosition(final double x, final double y, final double z, final double exagValue) {
    _x = x;
    _y = y;
    _z = z;
    _cursor.setTranslation(VolumeViewer.toWorldSpace(x, y, z * exagValue));
  }

  public double getX() {
    return _x;
  }

  public double getY() {
    return _y;
  }

  public double getZ() {
    return _z;
  }

  public void setCoordinateSystem(final CoordinateSystem cs) {
    _coordinateSystem = cs;
  }

  public CoordinateSystem getCoordinateSystem() {
    return _coordinateSystem;
  }

  /**
   * Return the visual cursor.
   * @return the visual cursor
   */
  public FocusRods getCursor() {
    return _cursor;
  }
}
