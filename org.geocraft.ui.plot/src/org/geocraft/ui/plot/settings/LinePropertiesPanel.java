/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.settings;


import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.defs.LineStyle;


public class LinePropertiesPanel extends Composite {

  private final LineProperties _properties;

  private ComboViewer _style;

  private Spinner _width;

  private ColorSelector _color;

  /**
   * @param parent
   * @param style
   * @param prop
   */
  public LinePropertiesPanel(final Composite parent, final int style, final LineProperties prop) {
    super(parent, style);
    _properties = prop;
    setLayout(new GridLayout(2, false));
    createPanel();
  }

  public void createPanel() {
    Label styleL = new Label(this, SWT.NULL);
    styleL.setText("Line Style");
    _style = new ComboViewer(this);
    _style.add(LineStyle.values());
    _style.setSelection(new StructuredSelection(_properties.getStyle()));

    Label widthL = new Label(this, SWT.NULL);
    widthL.setText("Line Width");
    _width = new Spinner(this, SWT.BORDER);
    _width.setValues(_properties.getWidth(), 1, 100, 0, 1, 10);

    Label colorL = new Label(this, SWT.NULL);
    colorL.setText("Line Color");
    _color = new ColorSelector(this);
    _color.setColorValue(_properties.getColor());
  }

  public LineProperties getProperties() {
    return new LineProperties((LineStyle) ((StructuredSelection) _style.getSelection()).getFirstElement(), _color
        .getColorValue(), _width.getSelection());
  }

}
