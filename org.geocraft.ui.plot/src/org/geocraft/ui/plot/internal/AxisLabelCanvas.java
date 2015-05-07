/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.internal;


import java.beans.PropertyChangeEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.geocraft.ui.plot.IAxisLabelCanvas;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.label.ILabel;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;


public class AxisLabelCanvas extends PlotCanvas implements IAxisLabelCanvas, ControlListener {

  /** The plot axis. */
  private IAxis _axis;

  /** The axis placement. */
  private final AxisPlacement _placement;

  /**
   * Constructs a canvas for rendering axis labels.
   * 
   * @param plot the associated plot.
   * @param colorProperties the plot color properties.
   * @param placement the axis placement. 
   */
  public AxisLabelCanvas(final Composite parent, final IPlot plot, final IAxis axis, final AxisPlacement placement, final CanvasLayoutModel layoutModel) {
    super(parent, plot, SWT.NONE);
    _axis = axis;
    _placement = placement;

    GridData constraints = new GridData();
    constraints.horizontalSpan = 1;
    constraints.verticalSpan = 1;
    constraints.horizontalAlignment = SWT.FILL;
    constraints.verticalAlignment = SWT.FILL;
    if (placement.equals(AxisPlacement.TOP)) {
      constraints.grabExcessHorizontalSpace = true;
      constraints.grabExcessVerticalSpace = false;
      constraints.heightHint = layoutModel.getTopLabelHeight();
    } else if (placement.equals(AxisPlacement.LEFT)) {
      constraints.grabExcessHorizontalSpace = false;
      constraints.grabExcessVerticalSpace = true;
      constraints.widthHint = layoutModel.getLeftLabelWidth();
    } else if (placement.equals(AxisPlacement.RIGHT)) {
      constraints.grabExcessHorizontalSpace = false;
      constraints.grabExcessVerticalSpace = true;
      constraints.widthHint = layoutModel.getRightLabelWidth();
    } else if (placement.equals(AxisPlacement.BOTTOM)) {
      constraints.grabExcessHorizontalSpace = true;
      constraints.grabExcessVerticalSpace = false;
      constraints.heightHint = layoutModel.getBottomLabelHeight();
    } else {
      throw new IllegalArgumentException("Invalid axis placement: " + placement);
    }
    setLayoutData(constraints);
  }

  /**
   * Gets the plot axis currently rendered.
   * 
   * @return the plot axis.
   */
  public IAxis getAxis() {
    return _axis;
  }

  /**
   * Sets the plot axis to render.
   * 
   * @param axis the plot axis.
   */
  public void setAxis(final IAxis axis) {
    _axis = axis;
    redraw();
    notifyListeners();
  }

  /**
   * Returns the placement of the axis label canvas.
   * <p>
   * This will be one of the following: TOP, LEFT, RIGHT or BOTTOM.
   * 
   * @return the axis label canvas placement.
   */
  public AxisPlacement getPlacement() {
    return _placement;
  }

  public void canvasLayoutUpdated(final CanvasLayoutModel layout) {
    Point currentSize = getSize();
    Point newSize = currentSize;
    int wh = layout.getLabelWidthOrHeight(_placement);
    if (_placement.equals(AxisPlacement.TOP) || _placement.equals(AxisPlacement.BOTTOM)) {
      newSize = new Point(currentSize.x, wh);
    } else if (_placement.equals(AxisPlacement.LEFT) || _placement.equals(AxisPlacement.RIGHT)) {
      newSize = new Point(wh, currentSize.y);
    } else {
      throw new RuntimeException("Invalid axis placement: " + _placement);
    }
    setSize(newSize);
  }

  @SuppressWarnings("unused")
  public void controlMoved(final ControlEvent event) {
    // No action required.
  }

  public void controlResized(final ControlEvent event) {
    Point newSize = ((Control) event.widget).getSize();
    setSize(newSize);
    _redraw = true;
    redraw();
  }

  public void paintControl(final PaintEvent event) {
    Point size = getSize();
    renderLabel(event.gc, _axis.getLabel(), new Rectangle(0, 0, size.x, size.y));
  }

  public void propertyChange(final PropertyChangeEvent event) {
    String propertyName = event.getPropertyName();
    String propertyNameSize = "";
    if (_placement.equals(AxisPlacement.TOP)) {
      propertyNameSize = CanvasLayoutModel.TOP_LABEL_HEIGHT;
    } else if (_placement.equals(AxisPlacement.LEFT)) {
      propertyNameSize = CanvasLayoutModel.LEFT_LABEL_WIDTH;
    } else if (_placement.equals(AxisPlacement.RIGHT)) {
      propertyNameSize = CanvasLayoutModel.RIGHT_LABEL_WIDTH;
    } else if (_placement.equals(AxisPlacement.BOTTOM)) {
      propertyNameSize = CanvasLayoutModel.BOTTOM_LABEL_HEIGHT;
    } else {
      return;
    }
    if (propertyName.equals(propertyNameSize)) {
      int size = Integer.parseInt(event.getNewValue().toString());
      if (_placement.equals(AxisPlacement.TOP) || _placement.equals(AxisPlacement.BOTTOM)) {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = false;
        gridData.horizontalSpan = 1;
        gridData.verticalSpan = 1;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.heightHint = size;
        getComposite().setLayoutData(gridData);
      } else if (_placement.equals(AxisPlacement.LEFT) || _placement.equals(AxisPlacement.RIGHT)) {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = false;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalSpan = 1;
        gridData.verticalSpan = 1;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.widthHint = size;
        getComposite().setLayoutData(gridData);
      }
      getComposite().update();
    }
  }

  /**
   * Sets the properties for rendering text in this axis label canvas.
   * 
   * @param textFont the text font.
   * @param textColor the text color.
   */
  public void setTextProperties(final Font textFont, final RGB textColor) {
    _textProperties.setFont(textFont);
    _textProperties.setColor(textColor);
  }

  /**
   * Renders the axis labels.
   * 
   * @param gc the graphics object.
   * @param the axis label to render.
   * @param rect the rectangle in which to render.
   */
  private void renderLabel(final GC graphics, final ILabel label, final Rectangle rect) {
    // If the label is not visible, simply return.
    if (!label.isVisible()) {
      return;
    }

    graphics.setTextAntialias(SWT.ON);
    graphics.setAntialias(SWT.ON);

    graphics.setBackground(getBackground());
    graphics.fillRectangle(rect);

    String text = label.getText();
    Font textFont = _textProperties.getFont();
    Orientation orientation = label.getOrientation();
    Alignment alignment = label.getAlignment();
    alignment = Alignment.CENTER;

    Color textColor = new Color(graphics.getDevice(), _textProperties.getColor());
    graphics.setFont(textFont);
    graphics.setForeground(textColor);
    textColor.dispose();

    FontMetrics metrics = graphics.getFontMetrics();

    // Render the label based on its orientation.
    switch (orientation) {
      case HORIZONTAL:
        renderHorizontalLabel(graphics, _placement, alignment, text, metrics, rect);
        break;
      case VERTICAL:
        renderVerticalLabel(graphics, _placement, alignment, text, metrics, rect);
        break;
    }
  }

  /**
   * Renders horizontally-oriented axis labels.
   * 
   * @param gc the graphics object.
   * @param placement the axis placement.
   * @param alignment the label alignment.
   * @param text the label text.
   * @param metrics the font metrics.
   * @param rect the rectangle in which to render.
   */
  private void renderHorizontalLabel(final GC gc, final AxisPlacement placement, final Alignment alignment,
      final String text, final FontMetrics metrics, final Rectangle rect) {

    int x0 = rect.x;
    int y0 = rect.y;
    int x1 = x0 + rect.width - 1;
    int y1 = y0 + rect.height - 1;

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
    gc.drawText(text, x, y);
  }

  /**
   * Renders vertically-oriented axis labels.
   * 
   * @param gc the graphics object.
   * @param placement the axis placement.
   * @param alignment the label alignment.
   * @param text the label text.
   * @param metrics the font metrics.
   * @param rect the rectangle in which to render.
   */
  private void renderVerticalLabel(final GC gc, final AxisPlacement placement, final Alignment alignment,
      final String text, final FontMetrics metrics, final Rectangle rect) {

    int x0 = rect.x;
    int y0 = rect.y;
    int x1 = x0 + rect.width - 1;
    int y1 = y0 + rect.height - 1;

    int x = 0;
    int y = 0;
    int textHeight = metrics.getAverageCharWidth() * text.length();
    int textWidth = metrics.getHeight();
    double rotation = 0;

    if (placement.equals(AxisPlacement.LEFT)) {
      rotation = -90;
      if (alignment.equals(Alignment.TOP)) {
        x = (x0 + x1) / 2 - textWidth / 2;
        y = y0 + textHeight;
      } else if (alignment.equals(Alignment.LEFT)) {
        x = x0;
        y = (y0 + y1) / 2 + textHeight / 2;
      } else if (alignment.equals(Alignment.RIGHT)) {
        x = x1 - textWidth;
        y = (y0 + y1) / 2 + textHeight / 2;
      } else if (alignment.equals(Alignment.BOTTOM)) {
        x = (x0 + x1) / 2 - textWidth / 2;
        y = y1;
      } else if (alignment.equals(Alignment.CENTER)) {
        x = (x0 + x1) / 2 - textWidth / 2;
        y = (y0 + y1) / 2 + textHeight / 2;
      } else {
        throw new IllegalArgumentException("Invalid label alignment: " + alignment);
      }
    } else if (placement.equals(AxisPlacement.RIGHT)) {
      rotation = 90;
      if (alignment.equals(Alignment.TOP)) {
        x = (x0 + x1) / 2 + textWidth / 2;
        y = y0;
      } else if (alignment.equals(Alignment.LEFT)) {
        x = x0 + textWidth;
        y = (y0 + y1) / 2 - textHeight / 2;
      } else if (alignment.equals(Alignment.RIGHT)) {
        x = x1;
        y = (y0 + y1) / 2 - textHeight / 2;
      } else if (alignment.equals(Alignment.BOTTOM)) {
        x = (x0 + x1) / 2 + textWidth / 2;
        y = y1 - textHeight;
      } else if (alignment.equals(Alignment.CENTER)) {
        x = (x0 + x1) / 2 + textWidth / 2;
        y = (y0 + y1) / 2 - textHeight / 2;
      } else {
        throw new IllegalArgumentException("Invalid label alignment: " + alignment);
      }
    }
    Transform transform = new Transform(getDisplay());
    transform.translate(x, y);
    transform.rotate((float) rotation);
    transform.translate(-x, -y);
    gc.setTransform(transform);
    gc.drawText(text, x, y);
    gc.setTransform(null);
    transform.dispose();
  }
}
