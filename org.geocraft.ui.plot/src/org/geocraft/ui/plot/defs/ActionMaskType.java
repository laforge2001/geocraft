/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for action mask types.
 * These are used to hook-up plot interactions via
 * the mouse or keyboard.
 */
public enum ActionMaskType {
  /** KeyPressed event type. */
  KEY_PRESSED,
  /** KeyReleased event type. */
  KEY_RELEASED,
  /** KeyTyped event type. */
  KEY_TYPED,
  /** MouseDoubleClick event type. */
  MOUSE_DOUBLE_CLICK,
  /** MouseDownevent type. */
  MOUSE_DOWN,
  /** MouseUp event type. */
  MOUSE_UP,
  /** MouseMove event type. */
  MOUSE_MOVE,
  /** MouseEnter event type. */
  MOUSE_ENTER,
  /** MouseExit event type. */
  MOUSE_EXIT,
  /** MouseHover event type. */
  MOUSE_HOVER,
}
