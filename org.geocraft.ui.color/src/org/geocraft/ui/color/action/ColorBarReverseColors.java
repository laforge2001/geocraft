/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.color.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.color.ColorBarEditor;


public class ColorBarReverseColors extends Action {

  private final ColorBarEditor _editor;

  public ColorBarReverseColors(final ColorBarEditor editor) {
    super("Reverse Colors");
    _editor = editor;
  }

  @Override
  public void run() {
    _editor.reverseColors();
  }
}
