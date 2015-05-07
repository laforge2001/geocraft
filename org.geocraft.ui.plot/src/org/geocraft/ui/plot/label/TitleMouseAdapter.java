/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.label;


import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.geocraft.ui.plot.ITitleCanvas;


/**
 * The adapter for handling mouse events in the title canvas.
 * Currently, everything except a button #3 click is ignored.
 * The button #3 click triggers the action for editing the
 * properties of the title canvas.
 */
public class TitleMouseAdapter extends MouseAdapter {

  /** The axis canvas. */
  private final ITitleCanvas _titleCanvas;

  /**
   * The default constructor.
   * 
   * @param titleCanvas the title canvas.
   */
  public TitleMouseAdapter(final ITitleCanvas titleCanvas) {
    _titleCanvas = titleCanvas;
  }

  @Override
  public void mouseDown(final MouseEvent event) {
    int button = event.button;
    switch (button) {
      case 1:
      case 2:
        // No action.
        break;
      case 3:
        // Button #3 : edit the title.
        _titleCanvas.editTitle();
        break;
      default:
        throw new IllegalArgumentException("Unrecognized mouse button: " + button);
    }
    _titleCanvas.redraw();
  }

}
