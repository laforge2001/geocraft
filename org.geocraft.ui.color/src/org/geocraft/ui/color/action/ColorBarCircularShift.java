/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.color.action;


import org.eclipse.jface.action.Action;
import org.geocraft.core.color.ColorBar;
import org.geocraft.ui.color.ColorBarEditor;


/**
 * Defines an action for setting the mode of a color bar editor to "Circular Shift".
 */
public class ColorBarCircularShift extends Action {

  private final ColorBarEditor _editor;

  public ColorBarCircularShift(final ColorBarEditor editor) {
    super("Circular Shift");
    _editor = editor;
  }

  @Override
  public void run() {
    _editor.setEditMode(ColorBar.Mode.CIRCULAR);
  }

}
