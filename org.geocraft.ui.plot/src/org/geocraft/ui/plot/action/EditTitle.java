/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.ITitleCanvas;
import org.geocraft.ui.plot.label.TitleEditorDialog;


public class EditTitle extends Action {

  private final IPlot _plot;

  private final ITitleCanvas _titleCanvas;

  /**
   * The default constructor.
   * @param plot the associated plot.
   * @param titleCanvas the title canvas.
   */
  public EditTitle(final IPlot plot, final ITitleCanvas titleCanvas) {
    _plot = plot;
    _titleCanvas = titleCanvas;
  }

  /**
   * Performs the title edit.
   */
  @Override
  public void run() {

    // Bring up the title editor.
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        TitleEditorDialog dialog = new TitleEditorDialog(new Shell(Display.getDefault()), _plot, _titleCanvas
            .getLabel());
        dialog.create();
        dialog.open();
      }
    });
  }
}
