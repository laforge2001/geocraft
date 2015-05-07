/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.internal;


import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.ui.plot.ICanvas;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.listener.ICanvasListener;


/**
 * The basic implementation of a plot canvas. A plot canvas is used to display a
 * portion of the overall plot.
 */
public abstract class PlotCanvas extends Canvas implements ICanvas, PaintListener {

  /** The associated plot. */
  protected IPlot _plot;

  /** The plot canvas text properties. */
  protected TextProperties _textProperties;

  /** The canvas width. */
  protected int _width;

  /** The canvas height. */
  protected int _height;

  /** The 'sized' flag: false until size has been set. */
  protected boolean _isSized;

  /** The 'redraw' flag. */
  protected boolean _redraw;

  protected List<ICanvasListener> _listeners;

  public PlotCanvas(final Composite parent, final IPlot plot, final int style) {
    this(parent, plot, style, SWT.NORMAL);
  }

  public PlotCanvas(final Composite parent, final IPlot plot, final int style, final int fontStyle) {
    super(parent, style);
    _plot = plot;
    _textProperties = new TextProperties();
    Font font = _textProperties.getFont();
    FontData fontData = font.getFontData()[0];
    _textProperties.setFont(new Font(null, fontData.getName(), fontData.getHeight(), fontStyle));
    _isSized = false;
    _redraw = true;
    _listeners = Collections.synchronizedList(new ArrayList<ICanvasListener>());
    addPaintListener(this);
  }

  public Composite getComposite() {
    return this;
  }

  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(_width, _height);
  }

  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
    int increment = 10000;
    return increment;
  }

  public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
    int increment = 10000;
    return increment;
  }

  @Override
  public void redraw() {
    _redraw = true;
    super.redraw();
  }

  public void addCanvasListener(final ICanvasListener listener) {
    _listeners.add(listener);
  }

  public void removeCanvasListener(final ICanvasListener listener) {
    _listeners.remove(listener);
  }

  protected void notifyListeners() {
    ICanvasListener[] listeners = _listeners.toArray(new ICanvasListener[0]);
    for (ICanvasListener listener : listeners) {
      listener.canvasUpdated(this);
    }
  }

  @Override
  public void dispose() {
    _textProperties.dispose();
    super.dispose();
  }
}
