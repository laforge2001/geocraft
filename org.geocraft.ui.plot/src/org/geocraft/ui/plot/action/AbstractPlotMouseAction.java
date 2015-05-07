/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import org.eclipse.swt.events.MouseEvent;
import org.geocraft.ui.plot.defs.ActionMaskType;


public abstract class AbstractPlotMouseAction implements IPlotMouseAction {

  protected PlotActionMask _mask;

  protected String _name;

  protected String _description;

  /**
   * Constructs an instance of AbstractPlotMouseAction.
   * @param mask the action mask (mouse buttons, clicks, modifiers).
   * @param name action name.
   * @param description the action description.
   */
  public AbstractPlotMouseAction(final PlotActionMask mask, final String name, final String description) {
    setMask(mask);
    setName(name);
    setDescription(description);
  }

  public String getName() {
    return _name;
  }

  public String getDescription() {
    return _description;
  }

  public PlotActionMask getMask() {
    return _mask;
  }

  public void setName(final String name) {
    _name = name;
  }

  public void setDescription(final String description) {
    _description = description;
  }

  public void setMask(final PlotActionMask mask) {
    _mask = mask;
  }

  public void mouseDoubleClick(final PlotMouseEvent event) {
    if (_mask.getActionMask().equals(ActionMaskType.MOUSE_DOUBLE_CLICK)) {
      doAction(event);
    }
  }

  public void mouseDown(final PlotMouseEvent event) {
    if (_mask.getActionMask().equals(ActionMaskType.MOUSE_DOWN)) {
      doAction(event);
    }
  }

  public void mouseUp(final PlotMouseEvent event) {
    if (_mask.getActionMask().equals(ActionMaskType.MOUSE_UP)) {
      doAction(event);
    }
  }

  public void mouseMove(final PlotMouseEvent event) {
    ActionMaskType type = _mask.getActionMask();
    if (type.equals(ActionMaskType.MOUSE_MOVE)) {
      doAction(event);
    }
  }

  public void mouseEnter(final PlotMouseEvent event) {
    if (_mask.getActionMask().equals(ActionMaskType.MOUSE_ENTER)) {
      doAction(event);
    }
  }

  public void mouseExit(final PlotMouseEvent event) {
    if (_mask.getActionMask().equals(ActionMaskType.MOUSE_EXIT)) {
      doAction(event);
    }
  }

  public void mouseHover(final PlotMouseEvent event) {
    if (_mask.getActionMask().equals(ActionMaskType.MOUSE_HOVER)) {
      doAction(event);
    }
  }

  protected void doAction(final PlotMouseEvent event) {
    MouseEvent me = event.getMouseEvent();
    if (_mask.getButton() == me.button && _mask.getClickCount() == me.count && _mask.getModifiers() == me.stateMask) {
      actionPerformed(event);
    }
  }
}
