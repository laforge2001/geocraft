/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.color;


import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.Labels;


public class ColorBarCanvas extends Canvas implements PaintListener {

  private final Font FONT = new Font(null, "Courier", 10, SWT.NORMAL);

  private final Color BACKGROUND_COLOR = new Color(null, 255, 255, 255);

  private final Color FOREGROUND_COLOR = new Color(null, 0, 0, 0);

  /** The color bar to display. */
  protected ColorBar _colorBar;

  /** The width of the margins around the left/right sides of the color bar. */
  protected int _marginWidth;

  /** The height of the margins around the top/bottom sides of the color bar. */
  protected int _marginHeight;

  /**
   * Constructs a color bar panel.
   * @param colorBar the color bar.
   * @param marginWidth the margin width.
   * @param marginHeight the margin height.
   */
  public ColorBarCanvas(final Composite parent, final ColorBar colorBar, final int marginWidth, final int marginHeight) {
    super(parent, SWT.DOUBLE_BUFFERED);
    addPaintListener(this);
    _colorBar = colorBar;
    _marginWidth = marginWidth;
    _marginHeight = marginHeight;
    int barWidth = 100;
    int barHeight = 256;
    setSize(barWidth + _marginWidth * 2, barHeight + _marginHeight * 2);
    setVisible(true);
  }

  /**
   * Gets the color at the specified index. Will return the marker color if in marker editing mode
   * and the index is between the markers.
   * @param index the index of the color to get.
   * @return the color of the specified index.
   */
  public Color createColor(final int index) {
    return new Color(null, _colorBar.getColor(index));
  }

  /**
   * Sets the color at the specified index.
   * @param index the index of the color to set.
   * @param color the color to set in the specified index.
   */
  public void setColor(final int index, final RGB color) {
    if (index > 0 && index < _colorBar.getNumColors()) {
      _colorBar.setColor(index, color);
    }
  }

  /**
   * Sets the color array.
   * @param colors the colors to set in the color bar.
   */
  public void setColors(final RGB[] colors) {
    _colorBar.setColors(colors);
  }

  /**
   * Sets the margin sizes around the color bar.
   * @param width the margin width to set.
   * @param height the margin height to set.
   */
  public void setMarginSize(final int width, final int height) {
    Point size = getSize();
    int barWidth = size.x;
    int barHeight = size.y;
    _marginWidth = width;
    _marginHeight = height;
    setSize(barWidth + _marginWidth * 2, barHeight + _marginHeight * 2);
  }

  /**
   * Sets the color bar in the panel.
   * @param colorBar the color bar to set.
   */
  public void setColorBar(final ColorBar colorBar) {
    _colorBar = colorBar;
    redraw();
    update();
  }

  /**
   * Gets the color cell based on the specified panel coordinates;
   * @param y the y-coordinate
   * @return the color cell associated with the x,y coordinates.
   */
  protected int getColorCell(final int y) {
    int numColors = _colorBar.getNumColors();
    Point size = getSize();
    int barHeight = size.y - _marginHeight * 2;
    double colorHeight = size.y - _marginHeight - _marginHeight;
    colorHeight /= numColors;

    // remove the offset due to the topmost margin
    int cell = y - _marginHeight;

    // the cursor might be outside the range of the color bar so use
    // a modified mod(%) to coral it back into the range 0 to N
    cell = MathUtil.mod(cell, barHeight);

    cell = (int) (cell / colorHeight);

    if (cell < 0 || cell >= _colorBar.getNumColors()) {
      throw new RuntimeException("Color cell (" + cell + ") is invalid.");
    }

    return cell;

  }

  /**
   * Draws the color bar, the scales and the margins. Note: overrides the default method.
   * @param g the Graphics object.
   */
  public void paintControl(final PaintEvent event) {
    GC gc = event.gc;
    drawUpper(gc);
    drawLower(gc);
    double[] leftScale = Labels.computeLabels(0, _colorBar.getNumColors(), 10);
    drawScale(gc, 0, _marginWidth - 1, _marginWidth - 10, 1, _colorBar.getNumColors(), leftScale[2], true);

    // double[] rightScale = Labels.computeLabels((float)_colorBar.getStartValue(),
    // (float)_colorBar.getEndValue(), 20);
    Point size = getSize();
    int x0 = size.x - _marginWidth;
    int x1 = size.x - _marginWidth;
    int xTick = size.x - _marginWidth + 10;
    double topValue = _colorBar.getStartValue();
    double bottomValue = _colorBar.getEndValue();
    if (_colorBar.isReversedRange()) {
      topValue = _colorBar.getEndValue();
      bottomValue = _colorBar.getStartValue();
    }
    drawScale(gc, x0, x1, xTick, topValue, bottomValue, _colorBar.getStepValue(), false);
    drawBar(gc);
  }

  /**
   * Draws the upper margin of the color bar.
   * @param g the Graphics object.
   */
  protected void drawUpper(final GC gc) {
    Point size = getSize();
    int barWidth = size.x - _marginHeight * 2;
    int x = _marginWidth;
    int y = 0;

    gc.setBackground(BACKGROUND_COLOR);
    gc.fillRectangle(x, y, size.x, _marginHeight);
    gc.setForeground(FOREGROUND_COLOR);
    gc.drawLine(x, _marginHeight - 1, x + barWidth, _marginHeight - 1);
  }

  /**
   * Draws the lower margin of the color bar.
   * @param g the Graphics object.
   */
  protected void drawLower(final GC gc) {
    Point size = getSize();
    int barWidth = size.x - _marginHeight * 2;
    int x = _marginWidth;
    int y = size.y - _marginHeight;

    gc.setBackground(BACKGROUND_COLOR);
    gc.fillRectangle(x, y, size.x, _marginHeight);
    gc.setForeground(FOREGROUND_COLOR);
    gc.drawLine(x, y, x + barWidth, y);
  }

  /**
   * Draws the left margin of the color bar.
   * @param g the Graphics object.
   */
  protected void drawLeft(final GC gc) {
    int x = 0;
    int y = 0;
    Point size = getSize();
    gc.setForeground(BACKGROUND_COLOR);
    gc.fillRectangle(x, y, _marginWidth, size.y);
    gc.setForeground(FOREGROUND_COLOR);
    gc.drawLine(_marginWidth - 1, _marginHeight - 1, _marginWidth - 1, size.y - _marginHeight);
  }

  /**
   * Draws the actual color bar.
   * @param g the Graphics object.
   */
  protected void drawBar(final GC gc) {
    Point size = getSize();
    int i;
    int barWidth = size.x - _marginWidth - _marginWidth;
    int barHeight = size.y - _marginHeight - _marginHeight;
    double colorHeight = (double) barHeight / _colorBar.getNumColors();
    int colorWidth = barWidth;
    gc.setBackground(BACKGROUND_COLOR);
    int x0 = _marginWidth;
    int y0 = _marginHeight;
    gc.fillRectangle(x0, y0, barWidth, barHeight);
    int numColors = _colorBar.getNumColors();
    for (i = 0; i < numColors; i++) {
      Color color = createColor(i);
      gc.setBackground(color);
      y0 = (int) Math.round(_marginHeight + i * colorHeight);
      int height = (int) Math.round(_marginHeight + (i + 1) * colorHeight) - y0;
      gc.fillRectangle(x0, y0, colorWidth, height);
      color.dispose();
    }
  }

  /**
   * Draw the vertical axis, ticks and labels along one side of the color bar.
   * @param g graphics context
   * @param x0 the left most x coordinate
   * @param x1 the x coordinate of the vertical axis line
   * @param xTick the x coordinate of the end of the tick
   * @param startScale lowest z value
   * @param endScale highest z value
   * @param stepScale z value increment
   * @param offsetLeft if true means offset the text to the left
   */
  protected void drawScale(final GC gc, final int x0, final int x1, final int xTick, final double topScale,
      final double bottomScale, final double stepScale, final boolean offsetLeft) {
    Point size = getSize();

    // set up dimensions
    int height = size.y;
    int barHeight = height - _marginHeight * 2;
    double colorHeight = height - _marginHeight - _marginHeight;
    colorHeight /= _colorBar.getNumColors();

    // draw a black box in the entire area of the scale
    gc.setBackground(BACKGROUND_COLOR);
    gc.fillRectangle(x0, 0, _marginWidth, height);
    gc.setForeground(FOREGROUND_COLOR);

    // font stuff
    gc.setFont(FONT);
    FontMetrics metrics = gc.getFontMetrics();
    int textHeight = metrics.getHeight();

    // ask for a reasonable number of labels
    List<Double> labels = Labels.getLabels(bottomScale, topScale, 10);

    for (int i = 0; i < labels.size(); i++) {

      double value = labels.get(i);

      String text = "" + String.format(Labels.getFormat(value), value);

      int yoffset = (int) MathUtil.scale(0, barHeight, bottomScale, topScale, labels.get(i));

      int xoffset = 0;
      if (offsetLeft) {
        xoffset = metrics.getAverageCharWidth() * text.length();
      }

      int x = xTick + 5;
      if (offsetLeft) {
        x = xTick - xoffset - 5;
      }

      int y = Math.round(height - _marginHeight - yoffset - textHeight / 2);
      //int y = (int) Math.round(_marginHeight + yoffset + (textHeight / 2) - (colorHeight / 2) - 2);
      gc.drawString(text, x, y);

      // draw the tick mark
      int y1 = height - _marginHeight - yoffset - 1;
      //int y1 = _marginHeight + yoffset - 1;
      gc.drawLine(xTick, y1, x1, y1);
    }

    // draw a vertical line the entire height of the axis
    int y1 = _marginHeight - 1;
    int y2 = _marginHeight - 1 + barHeight + 1;
    gc.drawLine(x1, y1, x1, y2);
  }

  @Override
  public void dispose() {
    FONT.dispose();
    BACKGROUND_COLOR.dispose();
    FOREGROUND_COLOR.dispose();
  }

}
