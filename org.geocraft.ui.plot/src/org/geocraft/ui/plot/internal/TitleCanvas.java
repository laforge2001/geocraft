/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.internal;


import java.beans.PropertyChangeEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.ITitleCanvas;
import org.geocraft.ui.plot.action.EditTitle;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.label.ILabel;
import org.geocraft.ui.plot.label.TitleMouseAdapter;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;


/**
 * The title canvas is the top component in the plot composite.
 * It contains a label for displaying a plot title. The properties
 * of the title label can be modified by right-clicking on the
 * title canvas. This brings up an editor for specifying the
 * title text, font, color, etc. The title canvas can be hidden
 * by editing the appropriate value in the canvas layout model.
 */
public class TitleCanvas extends PlotCanvas implements ITitleCanvas {

  /** The plot label label. */
  private ILabel _label;

  /** The action for editing the title. */
  private final EditTitle _editTitle;

  /**
   * The default constructor.
   * 
   * @param plot the associated plot.
   * @param colorProperties the plot color properties.
   * @param label the associated plot label.
   */
  public TitleCanvas(final Composite parent, final IPlot plot, final ILabel label, final CanvasLayoutModel layoutModel) {
    super(parent, plot, SWT.NONE);
    _label = label;

    // Add the plot title mouse adapter to the canvas.
    TitleMouseAdapter mouseAdapter = new TitleMouseAdapter(this);
    getComposite().addMouseListener(mouseAdapter);

    // Create the action for editing the title.
    _editTitle = new EditTitle(_plot, this);

    GridData constraints = new GridData();
    constraints.grabExcessHorizontalSpace = true;
    constraints.grabExcessVerticalSpace = false;
    constraints.horizontalSpan = 5;
    constraints.verticalSpan = 1;
    constraints.horizontalAlignment = SWT.FILL;
    constraints.verticalAlignment = SWT.FILL;
    constraints.heightHint = layoutModel.getTitleHeight();
    setLayoutData(constraints);
  }

  /**
   * Returns the label displayed in the canvas.
   */
  public ILabel getLabel() {
    return _label;
  }

  /**
   * Sets the label to display in the canvas.
   */
  public void setLabel(final ILabel label) {
    _label = label;
  }

  /**
   * Triggers the action for editing the title properties.
   */
  public void editTitle() {
    _editTitle.run();
  }

  /**
   * Sets the text properties of the title canvas.
   * 
   * @param font the text font.
   * @param color the text color.
   */
  public void setTextProperties(final Font font, final RGB color) {
    _textProperties.setFont(font);
    _textProperties.setColor(color);
  }

  /**
   * Renders the title in the canvas.
   */
  public void paintControl(final PaintEvent event) {
    ILabel label = getLabel();
    if (!label.isVisible()) {
      return;
    }

    Point size = getSize();
    Rectangle rect = new Rectangle(0, 0, size.x, size.y);
    int x0 = 0;
    int y0 = 0;
    int x1 = x0 + rect.width - 1;
    int y1 = y0 + rect.height - 1;

    GC graphics = event.gc;
    graphics.setBackground(getBackground());
    graphics.fillRectangle(rect);

    String text = label.getText();
    Font textFont = _textProperties.getFont();
    Alignment alignment = label.getAlignment();

    graphics.setFont(textFont);
    Color textColor = new Color(graphics.getDevice(), _textProperties.getColor());
    graphics.setForeground(textColor);
    textColor.dispose();

    FontMetrics metrics = graphics.getFontMetrics();

    int x = 0;
    int y = 0;
    int textHeight = metrics.getHeight();
    int textWidth = metrics.getAverageCharWidth() * text.length();
    if (alignment.equals(Alignment.TOP)) {
      x = (x0 + x1) / 2 - textWidth / 2;
      y = y0;
    } else if (alignment.equals(Alignment.LEFT)) {
      x = x0;
      y = (y0 + y1) / 2 - textHeight / 2;
    } else if (alignment.equals(Alignment.RIGHT)) {
      x = x1 - textWidth;
      y = (y0 + y1) / 2 - textHeight / 2;
    } else if (alignment.equals(Alignment.BOTTOM)) {
      x = (x0 + x1) / 2 - textWidth / 2;
      y = y1 - textHeight;
    } else if (alignment.equals(Alignment.CENTER)) {
      x = (x0 + x1) / 2 - textWidth / 2;
      y = (y0 + y1) / 2 - textHeight / 2;
    } else {
      throw new IllegalArgumentException("Invalid label alignment: " + alignment);
    }
    graphics.setTextAntialias(SWT.ON);
    graphics.drawText(text, x, y);
  }

  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getPropertyName().equals(CanvasLayoutModel.TITLE_VISIBLE)) {
      boolean visible = Boolean.getBoolean(event.getNewValue().toString());
      setVisible(visible);
    } else if (event.getPropertyName().equals(CanvasLayoutModel.TITLE_HEIGHT)) {
      int height = Integer.parseInt(event.getNewValue().toString());
      GridData gridData = new GridData();
      gridData.grabExcessHorizontalSpace = true;
      gridData.grabExcessVerticalSpace = false;
      gridData.horizontalSpan = 3;
      gridData.verticalSpan = 1;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.verticalAlignment = SWT.FILL;
      gridData.heightHint = height;
      getComposite().setLayoutData(gridData);
      getComposite().update();
    }
  }
}
