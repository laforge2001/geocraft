/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.axis;


import org.geocraft.ui.plot.IAxisLabelCanvas;
import org.geocraft.ui.plot.IAxisRangeCanvas;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.EditAxis;
import org.geocraft.ui.plot.action.IncrementAxis;
import org.geocraft.ui.plot.action.SetAxis;


public class AxisComposite {

  /** The label canvas for the plot axis. */
  private final IAxisLabelCanvas _labelCanvas;

  /** The range canvas for the plot axis. */
  private final IAxisRangeCanvas _rangeCanvas;

  /** The action for incrementing the axis. */
  private final IncrementAxis _incrementAxis;

  /** The action for setting the axis. */
  private final SetAxis _setAxis;

  /** The action for editing the axis. */
  private final EditAxis _editAxis;

  public AxisComposite(final IPlot plot, final IAxisLabelCanvas labelCanvas, final IAxisRangeCanvas rangeCanvas) {

    // Add the plot axis mouse adapter to the canvases.
    AxisMouseAdapter mouseAdapter = new AxisMouseAdapter(this);
    _labelCanvas = labelCanvas;
    _rangeCanvas = rangeCanvas;
    _labelCanvas.getComposite().addMouseListener(mouseAdapter);
    _rangeCanvas.getComposite().addMouseListener(mouseAdapter);

    // Create the action for increment/decrementing the axis.
    _incrementAxis = new IncrementAxis(plot, this);

    // Create the action for setting the axis.
    _setAxis = new SetAxis(plot, this);

    // Create the action for editing the axis.
    _editAxis = new EditAxis(plot, this);

  }

  public IAxisLabelCanvas getAxisLabelCanvas() {
    return _labelCanvas;
  }

  public IAxisRangeCanvas getAxisRangeCanvas() {
    return _rangeCanvas;
  }

  public void incrementAxis(final int increment) {
    _incrementAxis.setIncrement(increment);
    _incrementAxis.run();
  }

  public void editAxis() {
    _editAxis.run();
  }

  public void setAxis(final int index) {
    _setAxis.run(index);
  }
}
