/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.axis;


import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;


public class AxisMouseAdapter implements MouseListener, MouseMoveListener {

  /** The axis canvas. */
  private final AxisComposite _axisComposite;

  /**
   * The default constructor.
   * 
   * @param axisComposite the axis composite.
   */
  public AxisMouseAdapter(final AxisComposite axisComposite) {
    _axisComposite = axisComposite;
  }

  public void mouseDoubleClick(final MouseEvent event) {
    // No action defined.
  }

  public void mouseDown(final MouseEvent event) {
    int button = event.button;
    switch (button) {
      case 1:
        // Button #1 : increment the model axis.
        _axisComposite.incrementAxis(1);
        break;
      case 2:
        // Button #2 : decrement the model axis.
        _axisComposite.incrementAxis(-1);
        break;
      case 3:
        // Button #3 : edit the axis.
        _axisComposite.editAxis();
        break;
      default:
        throw new IllegalArgumentException("Unrecognized mouse button: " + button);
    }
    _axisComposite.getAxisLabelCanvas().redraw();
    _axisComposite.getAxisRangeCanvas().redraw();
    // _modelCanvas.update(UpdateLevel.Redraw);
  }

  public void mouseUp(final MouseEvent event) {
    // No action defined.
  }

  public void mouseMove(final MouseEvent event) {
    // No action defined.
  }
}
