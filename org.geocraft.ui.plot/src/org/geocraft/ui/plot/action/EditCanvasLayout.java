/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.layout.CanvasLayoutEditorDialog;


public class EditCanvasLayout extends Action {

  private final IPlot _plot;

  /**
   * Constructs the action for editing the canvas layout of a plot.
   * @param plotComposite the plot composite.
   */
  public EditCanvasLayout(final IPlot plot) {
    _plot = plot;
  }

  /**
   * Performs the canvas layout edit.
   */
  @Override
  public void run() {
    // Bring up the canvas layout editor.
    CanvasLayoutEditorDialog dialog = CanvasLayoutEditorDialog.create(_plot);
    dialog.open();
  }
}
