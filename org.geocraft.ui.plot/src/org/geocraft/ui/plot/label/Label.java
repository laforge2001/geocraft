/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.label;


import org.geocraft.core.common.model.AbstractBean;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.defs.Orientation;


/**
 * An implementation of the plot label interface.
 */
public class Label extends AbstractBean implements ILabel {

  public static final String TEXT = "Text";

  public static final String ALIGNMENT = "Alignment";

  public static final String ORIENTATION = "Orientation";

  public static final String VISIBLE = "Visible";

  private String _text;

  private Orientation _orientation;

  private Alignment _alignment;

  private boolean _visible;

  /**
   * A simple constructor.
   * @param text the label text.
   */
  public Label(final String text) {
    this(text, Orientation.HORIZONTAL, Alignment.CENTER, true);
  }

  /**
   * The full constructor.
   * @param text the label text.
   * @param orientation the label orientation.
   * @param alignment the label alignment.
   * @param visible the label visibility.
   */
  public Label(final String text, final Orientation orientation, final Alignment alignment, final boolean visible) {
    setText(text);
    setOrientation(orientation);
    setAlignment(alignment);
    setVisible(visible);
  }

  /**
   * The copy constructor.
   * @param label the label being copied.
   */
  public Label(final ILabel label) {
    this(label.getText(), label.getOrientation(), label.getAlignment(), label.isVisible());
  }

  public String getText() {
    return _text;
  }

  public void setText(final String text) {
    firePropertyChange(TEXT, _text, _text = text);
  }

  public Orientation getOrientation() {
    return _orientation;
  }

  public void setOrientation(final Orientation orientation) {
    firePropertyChange(ORIENTATION, _orientation, _orientation = orientation);
  }

  public Alignment getAlignment() {
    return _alignment;
  }

  public void setAlignment(final Alignment alignment) {
    firePropertyChange(ALIGNMENT, _alignment, _alignment = alignment);
  }

  public boolean isVisible() {
    return _visible;
  }

  public boolean getVisible() {
    return _visible;
  }

  public void setVisible(final boolean visible) {
    firePropertyChange(VISIBLE, _visible, _visible = visible);
  }

  public void dispose() {
    // No action required.
  }
}
