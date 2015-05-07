/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.internal;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.listener.IPlotMouseListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


public class PlotMouseAdapter implements MouseListener, MouseMoveListener, MouseTrackListener, MouseWheelListener {

  private final IPlot _plot;

  private final IModelSpaceCanvas _canvas;

  private final List<IPlotMouseListener> _listeners;

  private MouseEvent _prevScrollEvent = null;

  private int _scrollCount = 0;

  public PlotMouseAdapter(final IPlot plot, final IModelSpaceCanvas canvas) {
    _plot = plot;
    _canvas = canvas;
    _listeners = Collections.synchronizedList(new ArrayList<IPlotMouseListener>());
  }

  public void addListener(final IPlotMouseListener listener) {
    if (!_listeners.contains(listener)) {
      _listeners.add(listener);
    }
  }

  public void removeListener(final IPlotMouseListener listener) {
    _listeners.remove(listener);
  }

  public void mouseDoubleClick(final MouseEvent e) {
    PlotMouseEvent event = transformMouseEvent(e);
    for (IPlotMouseListener listener : getListeners()) {
      listener.mouseDoubleClick(event);
    }
  }

  public void mouseDown(final MouseEvent e) {
    PlotMouseEvent event = transformMouseEvent(e);
    for (IPlotMouseListener listener : getListeners()) {
      listener.mouseDown(event);
    }
  }

  public void mouseUp(final MouseEvent e) {
    PlotMouseEvent event = transformMouseEvent(e);
    for (IPlotMouseListener listener : getListeners()) {
      listener.mouseUp(event);
    }
  }

  public void mouseMove(final MouseEvent e) {
    PlotMouseEvent event = transformMouseEvent(e);
    for (IPlotMouseListener listener : getListeners()) {
      listener.mouseMove(event);
    }
  }

  public void mouseEnter(final MouseEvent e) {
    PlotMouseEvent event = transformMouseEvent(e);
    for (IPlotMouseListener listener : getListeners()) {
      listener.mouseEnter(event);
    }
  }

  public void mouseExit(final MouseEvent e) {
    PlotMouseEvent event = transformMouseEvent(e);
    for (IPlotMouseListener listener : getListeners()) {
      listener.mouseExit(event);
    }
  }

  public void mouseHover(final MouseEvent e) {
    PlotMouseEvent event = transformMouseEvent(e);
    for (IPlotMouseListener listener : getListeners()) {
      listener.mouseHover(event);
    }
  }

  public void dispose() {
    _listeners.clear();
  }

  private IPlotMouseListener[] getListeners() {
    return _listeners.toArray(new IPlotMouseListener[0]);
  }

  private PlotMouseEvent transformMouseEvent(final MouseEvent event) {
    int xPixel = event.x;
    int yPixel = event.y;
    Point size = _canvas.getComposite().getSize();
    IModelSpace modelSpace = _plot.getActiveModelSpace();
    int width = size.x;
    int height = size.y;
    int modelEndX = width - 1;
    int modelEndY = height - 1;
    Point pixelCoord = new Point(xPixel, yPixel);
    Point2D.Double modelCoord = null;
    if (xPixel >= 0 && xPixel <= modelEndX && yPixel >= 0 && yPixel <= modelEndY) {
      modelCoord = new Point2D.Double(0, 0);
      _canvas.transformPixelToModel(modelSpace, xPixel, yPixel, modelCoord);
    }
    return new PlotMouseEvent(event, _plot, _canvas, pixelCoord, modelCoord);
  }

  public void clear() {
    _listeners.clear();
  }

  public void mouseScrolled(final MouseEvent event) {
    processMouseScrolled(event, 1);
    //    // Check if a previous scroll events is stored.
    //    if (_prevScrollEvent != null) {
    //      // If one is stored, check if the state mask and count (scroll direction) are identical.
    //      // If yes, and the time difference is less than 200 msec, then take no immediate action
    //      // but keep track of the number of times this has occurred.
    //      // If no, then continue on and process the event.
    //      if (event.stateMask == _prevScrollEvent.stateMask && event.count == _prevScrollEvent.count) {
    //        // The state mask and count are the same (same type of scroll event).
    //        if (event.time - _prevScrollEvent.time < 2 && _scrollCount < 3) {
    //          // Time differential is short, so skip and increment stored count.
    //          _scrollCount++;
    //          return;
    //        }
    //        // Time differential is long, so process the stored events.
    //        processMouseScrolled(_prevScrollEvent, Math.max(_scrollCount, 1));
    //      } else {
    //        // The state mask and count are different (different type of scroll event).
    //        processMouseScrolled(event, 1);
    //      }
    //    } else {
    //      // If no previous mouse scroll events exist, then process the current one.
    //      processMouseScrolled(event, 1);
    //    }
    //    // Store the mouse scroll event.
    //    _prevScrollEvent = event;
    //    _scrollCount = 0;
  }

  private void processMouseScrolled(final MouseEvent event, final int scrollCount) {
    int xPixel = event.x;
    int yPixel = event.y;
    double horizontalZoomFactor = 1.1;
    double verticalZoomFactor = 1.1;
    // Check state mask.
    // If Shift key is pressed, then only zoom vertically.
    // If Control key is pressed, then only zoom horizontally.
    if (event.stateMask == SWT.SHIFT) {
      horizontalZoomFactor = 1;
    } else if (event.stateMask == SWT.CTRL) {
      verticalZoomFactor = 1;
    }
    double horizontalZoom = Math.pow(horizontalZoomFactor, scrollCount);
    double verticalZoom = Math.pow(verticalZoomFactor, scrollCount);
    if (event.count > 0) {
      _plot.zoom(1 / horizontalZoom, 1 / verticalZoom, xPixel, yPixel);
    } else {
      _plot.zoom(horizontalZoom, verticalZoom, xPixel, yPixel);
    }
  }

}
