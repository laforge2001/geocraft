/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import org.geocraft.ui.plot.IAxisLabelCanvas;
import org.geocraft.ui.plot.IAxisRangeCanvas;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.axis.AxisComposite;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.model.IModelSpace;


public class SetAxis {

  /** The associated plot. */
  private final IPlot _plot;

  /** The axis canvas. */
  private final IAxisLabelCanvas _labelCanvas;

  private final IAxisRangeCanvas _rangeCanvas;

  /**
   * The default constructor.
   * @param plot the associated plot.
   * @param axisComposite the axis composite.
   */
  public SetAxis(final IPlot plot, final AxisComposite axisComposite) {
    _plot = plot;
    _labelCanvas = axisComposite.getAxisLabelCanvas();
    _rangeCanvas = axisComposite.getAxisRangeCanvas();
  }

  /**
   * Performs the axis set action.
   */
  public void run(final int index) {

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
    if (index < 0 || index >= axes.length) {
      throw new IllegalArgumentException("Invalid axis index: " + index);
    }

    // Set the new axes to display.
    axisIndex = index;
    _labelCanvas.setAxis(axes[axisIndex]);
    _rangeCanvas.setAxis(axes[axisIndex]);
  }
}
