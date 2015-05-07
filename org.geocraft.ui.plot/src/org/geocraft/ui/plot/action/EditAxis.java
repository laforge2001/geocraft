/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.ui.plot.IAxisLabelCanvas;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.axis.AxisComposite;
import org.geocraft.ui.plot.axis.AxisEditorDialog;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.model.IModelSpace;


public class EditAxis extends Action {

  private final IPlot _plot;

  private final IAxisLabelCanvas _labelCanvas;

  /**
   * The default constructor.
   * @param plot the associated plot.
   * @param axisComposite the axis composite.
   */
  public EditAxis(final IPlot plot, final AxisComposite axisComposite) {
    _plot = plot;
    _labelCanvas = axisComposite.getAxisLabelCanvas();
  }

  /**
   * Performs the axis edit.
   */
  @Override
  public void run() {

    // Get all the models in the associated plot.
    IModelSpace[] models = _plot.getModelSpaces();

    // Build an array of the available axes (depends on the placement).
    IAxis[] axes = new IAxis[models.length];
    AxisPlacement placement = _labelCanvas.getPlacement();
    if (placement.equals(AxisPlacement.TOP) || placement.equals(AxisPlacement.BOTTOM)) {
      for (int i = 0; i < axes.length; i++) {
        axes[i] = models[i].getAxisX();
      }
    } else if (placement.equals(AxisPlacement.LEFT) || placement.equals(AxisPlacement.RIGHT)) {
      for (int i = 0; i < axes.length; i++) {
        axes[i] = models[i].getAxisY();
      }
    }

    // Determine which axis is currently displayed.
    int axisIndex = -1;
    IAxis oldAxis = _labelCanvas.getAxis();
    for (int i = 0; i < axes.length; i++) {
      if (axes[i].equals(oldAxis)) {
        axisIndex = i;
        break;
      }
    }

    if (axisIndex == -1) {
      throw new RuntimeException("Axis not found.");
    }

    // Bring up the axis editor.
    final IAxis axis = axes[axisIndex];
    Shell shell = _plot.getPlotComposite().getShell();
    AxisEditorDialog dialog = new AxisEditorDialog(shell, _plot, axis);
    dialog.create();
    dialog.getShell().setText("Axis Editor");
    dialog.setBlockOnOpen(true);
    dialog.open();
  }
}
