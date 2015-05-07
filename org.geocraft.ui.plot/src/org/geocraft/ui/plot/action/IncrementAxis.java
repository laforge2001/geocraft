/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.plot.IAxisLabelCanvas;
import org.geocraft.ui.plot.IAxisRangeCanvas;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.axis.AxisComposite;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.model.IModelSpace;


/**
 * Defines the action for incrementing of the axis displayed in an axis canvas
 * by a single click of mouse button #1.
 */
public class IncrementAxis extends Action {

  /** The associated plot. */
  private final IPlot _plot;

  /** The axis canvas. */
  private final IAxisLabelCanvas _labelCanvas;

  private final IAxisRangeCanvas _rangeCanvas;

  /** The increment. */
  private int _increment;

  /**
   * The default constructor.
   * @param plot the associated plot.
   * @param axisComposite the axis composite.
   */
  public IncrementAxis(final IPlot plot, final AxisComposite axisComposite) {
    _plot = plot;
    _labelCanvas = axisComposite.getAxisLabelCanvas();
    _rangeCanvas = axisComposite.getAxisRangeCanvas();
    _increment = 0;
  }

  /**
   * Sets the axis increment.
   * @param increment the axis increment.
   */
  public void setIncrement(final int increment) {
    _increment = increment;
  }

  /**
   * Performs the axis increment action.
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

    // Increment and set the new axes to display.
    axisIndex += _increment;
    if (axisIndex < 0) {
      axisIndex = axes.length - 1;
    } else if (axisIndex >= axes.length) {
      axisIndex = 0;
    }
    _labelCanvas.setAxis(axes[axisIndex]);
    _rangeCanvas.setAxis(axes[axisIndex]);
  }
}
