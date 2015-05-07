/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;

import org.geocraft.ui.plot.defs.ActionMaskType;


/**
 * Defines a mask for defining an action to be taken, based on an event ID,
 * mouse button, modifiers and mouse click count.
 */
public class PlotActionMask extends Object {

  protected ActionMaskType _type;

  protected int _button;

  protected int _clickCount;

  protected int _modifiers;

  /**
   * Creates a PlotActionMask with specified action, event ID,
   * button, modifiers and click count.
   * @param type the type of the event (MousePressed, MouseMoved, etc).
   * @param button the mouse button.
   * @param clickCount the mouse click count.
   * @param modifiers the mouse modifiers (MouseEvent.SHIFT_MASK, MouseEvent.CTRL_MASK, etc).
   */
  public PlotActionMask(final ActionMaskType type, final int button, final int clickCount, final int modifiers) {
    _type = type;
    _button = button;
    _clickCount = clickCount;
    _modifiers = modifiers;
  }

  /**
   * Gets the type of the event associated with the mask (MousePressed, MouseMoved, etc).
   * @return the type of the event associated with the mask.
   */
  public ActionMaskType getActionMask() {
    return _type;
  }

  /**
   * Gets the mouse button associated with the mask.
   * @return the mouse button associated with the mask.
   */
  public int getButton() {
    return _button;
  }

  /**
   * Gets the mouse click count associated with the mask.
   * @return the mouse click count associated with the mask.
   */
  public int getClickCount() {
    return _clickCount;
  }

  /**
   * Gets the modifiers associated with the mask.
   * @return the modifiers associated with the mask.
   */
  public int getModifiers() {
    return _modifiers;
  }
}
