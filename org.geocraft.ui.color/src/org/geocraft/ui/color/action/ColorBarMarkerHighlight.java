/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.color.action;


import org.eclipse.jface.action.Action;
import org.geocraft.core.color.ColorBar;
import org.geocraft.ui.color.ColorBarEditor;


/**
 * Defines an action for setting the mode of a color bar editor to "Marker Highlight".
 */
public class ColorBarMarkerHighlight extends Action {

  private final ColorBarEditor _editor;

  public ColorBarMarkerHighlight(final ColorBarEditor editor) {
    super("Marker Highlight");
    _editor = editor;
  }

  @Override
  public void run() {
    _editor.setEditMode(ColorBar.Mode.MARKER);
  }

}
