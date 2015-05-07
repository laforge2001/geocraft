/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.color.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.geocraft.core.color.ColorBar;
import org.geocraft.ui.color.ColorBarEditor;


/**
 * Defines an action for setting the mode of a color bar editor to "Non Editable".
 */
public class ColorBarNonEditable extends Action {

  private final ColorBarEditor _editor;

  public ColorBarNonEditable(final ColorBarEditor editor) {
    super("Non Editable");
    _editor = editor;
  }

  @Override
  public void run() {
    _editor.setEditMode(ColorBar.Mode.NONE);
    Cursor cursor = new Cursor(null, SWT.CURSOR_ARROW);
    _editor.getCanvas().setCursor(cursor);
    cursor.dispose();
    _editor.initMarkers();
  }

}
