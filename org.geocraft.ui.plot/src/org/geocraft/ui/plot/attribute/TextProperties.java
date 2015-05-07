/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.attribute;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.model.AbstractBean;
import org.geocraft.ui.plot.defs.TextAnchor;


public class TextProperties extends AbstractBean {

  /** The text font. */
  private Font _textFont;

  /** The text color (as RGB). */
  private RGB _textColor;

  /** The text color. */
  private TextAnchor _textAnchor;

  public TextProperties() {
    _textFont = new Font(null, "SansSerif", 8, SWT.NORMAL);
    _textColor = new RGB(0, 0, 0);
    _textAnchor = TextAnchor.CENTER;
  }

  public TextProperties(final Font font, final RGB color, final TextAnchor anchor) {
    setFont(font);
    setColor(color);
    setAnchor(anchor);
  }

  /**
   * The copy constructor.
   * @param textProps the text properties to copy.
   */
  public TextProperties(final TextProperties textProps) {
    this(textProps.getFont(), textProps.getColor(), textProps.getAnchor());
  }

  public Font getFont() {
    return _textFont;
  }

  public RGB getColor() {
    return _textColor;
  }

  public TextAnchor getAnchor() {
    return _textAnchor;
  }

  public void setFont(final Font font) {
    if (font != null && _textFont != null && font.equals(_textFont)) {
      return;
    }
    Font textFontNew = new Font(null, font.getFontData());
    Font textFontOld = _textFont;
    _textFont = textFontNew;
    firePropertyChange("textFont", textFontOld, textFontNew);
    if (textFontOld != null) {
      textFontOld.dispose();
    }
  }

  public void setColor(final RGB color) {
    firePropertyChange("textColor", _textColor, _textColor = color);
  }

  public void setAnchor(final TextAnchor anchor) {
    firePropertyChange("textAnchor", _textAnchor, _textAnchor = anchor);
  }

  public void dispose() {
    _textFont.dispose();
  }
}
