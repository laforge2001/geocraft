/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.event;


import org.geocraft.core.model.datatypes.Coordinate;


public class CursorLocation extends Event {

  public enum TimeOrDepth {
    TIME,
    DEPTH,
    NONE;
  }

  /** The x,y,z coordinates of the cursor location. */
  private final Coordinate _location;

  private final TimeOrDepth _timeOrDepth;

  /** The (optional) offset coordinate of the cursor location. */
  private final float _offset;

  /**
   * Constructs a cursor location of x,y,z coordinates.
   * 
   * @param location the cursor coordinate.
   * @param senderId a unique id for the sending component.
   */
  public CursorLocation(final Coordinate location, final TimeOrDepth timeOrDepth, final String senderId) {
    this(location, timeOrDepth, 0, senderId);
  }

  /**
   * Constructs a cursor location of x,y,z and offset coordinates.
   * 
   * @param location the cursor coordinate.
   * @param senderId a unique id for the sending component.
   */
  public CursorLocation(final Coordinate location, final TimeOrDepth timeOrDepth, final float offset, final String senderId) {
    super(senderId);
    _location = location;
    _timeOrDepth = timeOrDepth;
    _offset = offset;
  }

  public String getSender() {
    return _senderId;
  }

  public Coordinate getLocation() {
    return _location;
  }

  public TimeOrDepth getTimeOrDepth() {
    return _timeOrDepth;
  }

  public float getOffset() {
    return _offset;
  }

}
